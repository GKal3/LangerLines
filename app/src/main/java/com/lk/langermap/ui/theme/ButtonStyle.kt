package com.lk.langermap.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.lk.langermap.R

@Composable
fun regionButtonColors(isSelected: Boolean) = ButtonDefaults.buttonColors(
    containerColor = if (isSelected)
        colorResource(id = R.color.lav_light)      // selezionato
    else
        colorResource(id = R.color.lav_light_trasl) // default
)
val regionButtonShape = RoundedCornerShape(16.dp)

@Composable
fun nextButtonColors() = ButtonDefaults.buttonColors(
    containerColor = colorResource(id = R.color.teal),
    disabledContainerColor = colorResource(id = R.color.teal_deact),
    contentColor = colorResource(id = R.color.b),
    disabledContentColor = colorResource(id = R.color.b_trasl)
)

val nextButtonShape = RoundedCornerShape(16.dp)