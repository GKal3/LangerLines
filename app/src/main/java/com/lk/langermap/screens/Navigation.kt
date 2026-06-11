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
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalContext
import com.lk.langermap.AppViewModel
import androidx.core.net.toUri

@Composable
fun Navigation(viewModel: AppViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {

        // HOME
        composable(
            "home",
            exitTransition = {
                if (targetState.destination.route == "sex") {
                    slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(500))
                } else null
            },
            popEnterTransition = {
                if (initialState.destination.route == "sex") {
                    slideInVertically(initialOffsetY = { -it }, animationSpec = tween(500))
                } else null
            }
        ) {
            HomeScreen(onNavigateToRegion = { navController.navigate("sex") })
        }

        // SEX
        composable(
            "sex",
            enterTransition = {
                if (initialState.destination.route == "home") {
                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(500))
                } else null
            },
            popExitTransition = {
                if (targetState.destination.route == "home") {
                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(500))
                } else null
            }
        ) {
            SexScreen(
                initialSex = viewModel.selectedSex,
                onNavigateToRegion = { sex ->
                    viewModel.selectedSex = sex
                    navController.navigate("region/$sex")
                }
            )
        }

        // SELEZIONE REGIONE
        composable(
            route = "region/{sex}",
            arguments = listOf(navArgument("sex") { type = NavType.StringType })
        ) { backStackEntry ->
            val sex = backStackEntry.arguments?.getString("sex") ?: ""
            RegionScreen(
                sex = sex,
                initialRegion = viewModel.selectedRegion,
                initialPov = viewModel.selectedPov,
                onNavigateToUpload = { drawableResId, region, selectedRegion, selectedPov ->
                    viewModel.selectedRegion = selectedRegion
                    viewModel.selectedPov = selectedPov
                    navController.navigate("upload/$drawableResId/$region")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // UPLOAD
        composable(
            route = "upload/{regionDrawableResId}/{region}",
            arguments = listOf(
                navArgument("regionDrawableResId") { type = NavType.IntType },
                navArgument("region") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val drawableResId = backStackEntry.arguments?.getInt("regionDrawableResId") ?: 0
            val region = backStackEntry.arguments?.getString("region") ?: ""
            viewModel.selectedDrawableResId = drawableResId
            viewModel.selectedRegionName = region
            UploadScreen(
                regionDrawableResId = drawableResId,
                regionName = region,
                initialImageUri = viewModel.selectedImageUri,
                onImageSelected = { uri: Uri? -> viewModel.selectedImageUri = uri },
                onNavigateToSettings = { photoUriString ->
                    val encoded = URLEncoder.encode(photoUriString, "UTF-8")
                    navController.navigate("edit/$encoded/$drawableResId")
                },
                onNavigateToCamera = {
                    navController.navigate("camera/$drawableResId")
                },
                onNavigateToOverlay = { photoUriString ->
                    val encoded = URLEncoder.encode(photoUriString, "UTF-8")
                    navController.navigate("overlay/$encoded/$drawableResId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // FOTOCAMERA
        composable(
            route = "camera/{regionDrawableResId}",
            arguments = listOf(navArgument("regionDrawableResId") { type = NavType.IntType })
        ) { backStackEntry ->
            val drawableResId = backStackEntry.arguments?.getInt("regionDrawableResId") ?: 0
            CameraScreen(
                regionDrawableResId = drawableResId,
                onNavigateToOverlay = { photoUri, resId ->
                    viewModel.selectedImageUri = photoUri.toUri()
                    navController.navigate("upload/${viewModel.selectedDrawableResId}/${viewModel.selectedRegionName}")
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
            val context       = LocalContext.current
            val encodedUri    = backStackEntry.arguments?.getString("photoUri") ?: ""
            val photoUri      = URLDecoder.decode(encodedUri, "UTF-8").toUri()
            val drawableResId = backStackEntry.arguments?.getInt("regionDrawableResId") ?: 0

            EditPhotoScreen(
                photoUri = photoUri,
                onBack   = { navController.popBackStack() },
                onApply  = { editedBitmap ->
                    val savedUri = BitmapUtils.saveBitmapToCache(editedBitmap, context)
                    val encoded  = URLEncoder.encode(savedUri.toString(), "UTF-8")
                    navController.navigate("overlay/$encoded/$drawableResId") {
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
                initialOffsetX    = viewModel.overlayOffsetX,
                initialOffsetY    = viewModel.overlayOffsetY,
                initialScale      = viewModel.overlayScale,
                initialRotation   = viewModel.overlayRotation,
                initialOpacity    = viewModel.overlayOpacity,
                initialColor      = viewModel.overlayColor,
                onStateChanged    = { ox, oy, sc, rot, op, col ->
                    viewModel.overlayOffsetX  = ox
                    viewModel.overlayOffsetY  = oy
                    viewModel.overlayScale    = sc
                    viewModel.overlayRotation = rot
                    viewModel.overlayOpacity  = op
                    viewModel.overlayColor    = col
                },
                onBack   = { navController.popBackStack() },
                onFinish = { composedUri ->
                    val encoded = URLEncoder.encode(composedUri, "UTF-8")
                    navController.navigate("output/$encoded")
                }
            )
        }

        // OUTPUT
        composable(
            route = "output/{photoUri}",
            arguments = listOf(navArgument("photoUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("photoUri") ?: ""
            val photoUri   = URLDecoder.decode(encodedUri, "UTF-8")
            OutputScreen(
                photoUri = photoUri,
                onBack   = { navController.popBackStack() },
                onNavigateToBackHome = {
                    navController.navigate("backhome") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }

        // BACKHOME
        composable("backhome") {
            BackHomeScreen(
                onStartNewProject = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
