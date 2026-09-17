package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(percent = 25),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = InventraPurple,
                contentColor = Color.White,
            ),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 24.dp),
    ) {
        Text(
            text = text,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}

@Preview
@Composable
private fun InventraPrimaryButtonPreview() {
    InventraPrimaryButton(text = "Confirmar", onClick = {})
}
