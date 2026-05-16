package com.lk.langermap.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    regionDrawableResId: Int,
    onNavigateToOverlay: (photoUri: String, regionDrawableResId: Int) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Stato permesso fotocamera
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher per richiedere il permesso
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    // Launcher per la galleria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onNavigateToOverlay(it.toString(), regionDrawableResId)
        }
    }

    // imageCapture viene inizializzato quando la camera parte
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    // Richiedi permesso al primo avvio se non ce l'hai
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // ── PREVIEW FOTOCAMERA ────────────────────────────────
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onImageCaptureReady = { capture ->
                    imageCapture = capture
                },
                lifecycleOwner = lifecycleOwner
            )
        } else {
            // ── MESSAGGIO PERMESSO NEGATO ─────────────────────────
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Permesso fotocamera necessario",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Concedi permesso")
                }
            }
        }

        // ── BOTTONE BACK (in alto a sinistra) ─────────────────────
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(44.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Indietro",
                tint = Color.White
            )
        }

        // ── BARRA INFERIORE ───────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Bottone galleria (sinistra)
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .size(52.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
              //  Icon(
               //   imageVector = Icons.Default.Collections,
                //    contentDescription = "Galleria",
                 //   tint = Color.White,
                  //  modifier = Modifier.size(28.dp)
                //)
            }

            // Bottone scatto (centro)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    IconButton(
                        onClick = {
                            imageCapture?.let { capture ->
                                takePhoto(
                                    context = context,
                                    imageCapture = capture,
                                    executor = executor,
                                    onSuccess = { uri ->
                                        onNavigateToOverlay(
                                            uri.toString(),
                                            regionDrawableResId
                                        )
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .then(
                                    Modifier.background(
                                        Color.LightGray.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                )
                        )
                    }
                }
            }

            // Spazio bilanciamento (destra)
            Spacer(modifier = Modifier.size(52.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// PREVIEW CAMERAX con AndroidView (bridge tra View e Compose)
// ─────────────────────────────────────────────────────────────────
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptureReady: (ImageCapture) -> Unit,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                onImageCaptureReady(imageCapture)

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

// ─────────────────────────────────────────────────────────────────
// SCATTO FOTO
// ─────────────────────────────────────────────────────────────────
private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onSuccess: (Uri) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY)
            .format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val uri = output.savedUri ?: Uri.fromFile(photoFile)
                onSuccess(uri)
            }

            override fun onError(exc: ImageCaptureException) {
                exc.printStackTrace()
            }
        }
    )
}

