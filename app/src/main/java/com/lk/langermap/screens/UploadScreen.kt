package com.lk.langermap.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lk.langermap.R
import com.lk.langermap.ui.theme.*

@Preview
@Composable
fun UploadScreen(
    regionDrawableResId: Int = 0,
    regionName: String? = null,
    initialImageUri: Uri? = null,
    onImageSelected: (Uri?) -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToSettings: (String) -> Unit = {},
    onNavigateToOverlay: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var selectedImageUri by remember { mutableStateOf(initialImageUri) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        onImageSelected(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.w))
            .statusBarsPadding()
    ) {

        // ── HEADER ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    tint = colorResource(id = R.color.b)
                )
            }
            Text(
                text       = "Upload patient image",
                textAlign = TextAlign.Center,
                fontSize   = 18.sp,
                fontFamily = robotoSemiBold,
                color      = colorResource(id = R.color.b),
                modifier   = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            TextButton(
                onClick = {
                    selectedImageUri?.let { uri -> onNavigateToOverlay(uri.toString()) }
                },
                enabled = selectedImageUri != null
            ) {
                Text(
                    text     = stringResource(id = R.string.btn_next),
                    color    = if (selectedImageUri != null)
                        colorResource(id = R.color.teal)
                    else
                        colorResource(id = R.color.b).copy(alpha = 0.3f),
                    fontSize = 16.sp,
                    fontFamily = robotoSemiBold
                )
            }

        }

        // ── REGION ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(
                    color = colorResource(id = R.color.lav_light),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_choosen_region),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Chosen region: $regionName",
                fontFamily = robotoRegular,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── IMAGE PREVIEW ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(330.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.lav_light),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { filePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_upload_image),
                        contentDescription = null,
                        tint = colorResource(id = R.color.lav_light),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upload image",
                        fontFamily = robotoRegular,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.lav_light)
                    )
                    Text(
                        text = "JPEG, PNG -- max 10 MB",
                        fontFamily = robotoRegular,
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.gray)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // ── DIVIDER ───────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = colorResource(id = R.color.lav_light))
            Text(
                text = "or choose source",
                fontSize = 12.sp,
                color = colorResource(id = R.color.gray),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = colorResource(id = R.color.lav_light))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── "TAKE PHOTO" ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(
                    color = colorResource(id = R.color.l_grey_trasl),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onNavigateToCamera() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_take_photo),
                contentDescription = null,
                tint = colorResource(id = R.color.b),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Take photo with camera",
                fontFamily = robotoRegular,
                color = colorResource (id = R.color.b),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── "UPLOAD" ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(
                    color = colorResource(id = R.color.l_grey_trasl),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { filePickerLauncher.launch("image/*") }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_upload_file),
                contentDescription = null,
                tint = colorResource(id = R.color.b),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Upload from files",
                fontFamily = robotoRegular,
                color = colorResource (id = R.color.b),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(8.dp))

        // ── SETTINGS AND DELETE ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 59.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    selectedImageUri?.let { uri ->
                        onNavigateToSettings(uri.toString())
                    }
                },
                enabled = selectedImageUri != null,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.lav_light),
                    disabledContainerColor = colorResource(id = R.color.lav_light_trasl)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_crop),
                    modifier = Modifier.size(24.dp),
                    tint = colorResource(id = R.color.w),
                    contentDescription = "Settings"
                )
            }
            Button(
                onClick = {
                    selectedImageUri = null
                    onImageSelected(null)
                          },
                enabled = selectedImageUri != null,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.lav_light),
                    disabledContainerColor = colorResource(id = R.color.lav_light_trasl)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    modifier = Modifier.size(24.dp),
                    tint = colorResource(id = R.color.w),
                    contentDescription = "Delete"
                )
            }
        }
    }
}