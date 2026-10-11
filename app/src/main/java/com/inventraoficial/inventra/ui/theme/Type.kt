package com.inventraoficial.inventra.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.R

@OptIn(ExperimentalTextApi::class)
val Montserrat =
    FontFamily(
        Font(
            R.font.montserrat_variable,
            weight = FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        ),
        Font(
            R.font.montserrat_variable,
            weight = FontWeight.Medium,
            variationSettings = FontVariation.Settings(FontVariation.weight(500)),
        ),
        Font(
            R.font.montserrat_variable,
            weight = FontWeight.Bold,
            variationSettings = FontVariation.Settings(FontVariation.weight(700)),
        ),
        Font(
            R.font.montserrat_italic_variable,
            weight = FontWeight.Normal,
            style = FontStyle.Italic,
            variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        ),
    )

val Typography =
    Typography(
        bodyLarge =
            TextStyle(
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
    )
