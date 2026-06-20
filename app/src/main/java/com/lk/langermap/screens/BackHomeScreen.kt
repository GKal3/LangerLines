package com.lk.langermap.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lk.langermap.R
import com.lk.langermap.ui.theme.*

@Preview
@Composable
fun BackHomeScreen(
    onBack: () -> Unit = {},
    onStartNewProject: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.teal_light))
    ) {

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 4.dp, top = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "Back",
                tint = colorResource(id = R.color.b)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {

            Icon(
                painter = painterResource(id = R.drawable.ic_complete),
                contentDescription = null,
                tint = colorResource(id = R.color.b),
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Session complete",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.b)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "The Langer's lines have been applied and saved. The image is ready for clinical use.",
                fontSize = 16.sp,
                fontFamily = robotoRegular,
                color = colorResource(id = R.color.b),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = onStartNewProject,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.w)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Start a new project",
                    fontSize = 18.sp,
                    fontFamily = robotoRegular,
                    color = colorResource(id = R.color.b)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    append("Thank you for using ")
                    withStyle(
                        style = SpanStyle(
                            fontFamily = montserratSemiBold,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Langer Map")
                    }
                },
                fontSize = 14.sp,
                fontFamily = robotoRegular,
                color = colorResource(id = R.color.b)
            )
        }
    }
}