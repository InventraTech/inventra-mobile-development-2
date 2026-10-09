package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.BorderStroke
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
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.Montserrat

/** Botao para acoes destrutivas ou irreversiveis (sair da conta, excluir...). */
@Composable
fun InventraDangerButton(
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
                containerColor = Color.White,
                contentColor = InventraDanger,
            ),
        border = BorderStroke(1.dp, InventraDanger),
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

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraDangerButtonPreview() {
    InventraDangerButton(text = "Sair da conta", onClick = {})
}
