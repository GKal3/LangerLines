package com.lk.langermap.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.net.URLEncoder
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

@Composable
fun nav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        // HOME
        composable("home") {
            HomeScreen(
                onNavigateToRegion = {              // nome originale di HomeScreen.kt
                    navController.navigate("sex")
                }
            )
        }

        // SEX
        composable("sex") {
            SexScreen(
                onNavigateToRegion = { sex ->
                    navController.navigate("region/$sex")
                }
            )
        }

        // SELEZIONE REGIONE  →  riceve {sex} dalla route
        composable(
            route = "region/{sex}",
            arguments = listOf(
                navArgument("sex") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sex = backStackEntry.arguments?.getString("sex") ?: ""
            RegionScreen(
                sex = sex,
                onNavigateToCamera = { drawableResId ->
                    navController.navigate("camera/$drawableResId")
                }
            )
        }
// FOTOCAMERA → naviga a edit invece che direttamente a overlay
        composable(
            route = "camera/{regionDrawableResId}",
            arguments = listOf(
                navArgument("regionDrawableResId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val drawableResId = backStackEntry.arguments?.getInt("regionDrawableResId") ?: 0
            CameraScreen(
                regionDrawableResId = drawableResId,
                onNavigateToOverlay = { photoUri, resId ->
                    val encoded = URLEncoder.encode(photoUri, "UTF-8")
                    navController.navigate("edit/$encoded/$resId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // EDIT PHOTO (crop / rotate / mirror)
        composable(
            route = "edit/{photoUri}/{regionDrawableResId}",
            arguments = listOf(
                navArgument("photoUri")            { type = NavType.StringType },
                navArgument("regionDrawableResId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            // ← context preso dal composable, non dal NavController
            val context       = LocalContext.current
            val encodedUri    = backStackEntry.arguments?.getString("photoUri") ?: ""
            val photoUri      = Uri.parse(URLDecoder.decode(encodedUri, "UTF-8"))
            val drawableResId = backStackEntry.arguments?.getInt("regionDrawableResId") ?: 0

            EditPhotoScreen(
                photoUri = photoUri,
                onBack   = { navController.popBackStack() },
                onApply  = { editedBitmap ->
                    // Salva il bitmap editato in cache e naviga all'overlay
                    val savedUri = saveBitmapToCache(editedBitmap, context)
                    val encoded  = URLEncoder.encode(savedUri.toString(), "UTF-8")
                    navController.navigate("overlay/$encoded/$drawableResId") {
                        // Rimuove edit dallo stack: Back dall'overlay → torna a camera
                        popUpTo("edit/{photoUri}/{regionDrawableResId}") { inclusive = true }
                    }
                }
            )
        }

        // OVERLAY
        composable(
            route = "overlay/{photoUri}/{regionDrawableResId}",
            arguments = listOf(
                navArgument("photoUri")            { type = NavType.StringType },
                navArgument("regionDrawableResId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val encodedUri    = backStackEntry.arguments?.getString("photoUri") ?: ""
            val photoUri      = URLDecoder.decode(encodedUri, "UTF-8")
            val drawableResId = backStackEntry.arguments?.getInt("regionDrawableResId") ?: 0
            OverlayScreen(
                photoUri   = photoUri,
                overlayRes = drawableResId,
                onBack     = { navController.popBackStack() },
                onFinish   = { navController.popBackStack() }
            )
        }
    }
    }

















































