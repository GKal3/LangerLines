package com.lk.langermap

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import android.net.Uri
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import com.lk.langermap.screens.EditState

class AppViewModel : ViewModel() {
    var selectedSex by mutableStateOf<String?>(null)
    var selectedRegion by mutableStateOf<Int?>(null)
    var selectedPov by mutableStateOf("front")
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var selectedDrawableResId by mutableIntStateOf(0)
    var selectedRegionName by mutableStateOf("")
    var overlayOffsetX by mutableFloatStateOf(0f)
    var overlayOffsetY by mutableFloatStateOf(0f)
    var overlayScale by mutableFloatStateOf(1f)
    var overlayRotation by mutableFloatStateOf(0f)
    var overlayOpacity by mutableStateOf(0.75f)
    var overlayColor by mutableStateOf(androidx.compose.ui.graphics.Color.Black)

    var editState: EditState = EditState()
}