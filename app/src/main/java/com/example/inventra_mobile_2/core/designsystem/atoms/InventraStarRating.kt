package com.example.inventra_mobile_2.core.designsystem.atoms

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.ui.theme.InventraGold

@Composable
fun InventraStarRating(
    rating: Int,
    modifier: Modifier = Modifier,
    maxStars: Int = 5
) {
    Row(modifier = modifier) {
        repeat(maxStars) { index ->
            Text(
                text = if (index < rating) "★" else "☆",
                color = InventraGold,
                fontSize = 16.sp
            )
        }
    }
}

@Preview
@Composable
private fun InventraStarRatingPreview() {
    InventraStarRating(rating = 4)
}