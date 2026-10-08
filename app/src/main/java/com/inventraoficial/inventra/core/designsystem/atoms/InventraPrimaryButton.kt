package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        // Durante o loading o botao continua habilitado (para nao ficar cinza),
        // mas ignora cliques - evita disparar a mesma acao duas vezes.
        onClick = { if (!isLoading) onClick() },
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(percent = 25),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = InventraPurple,
                contentColor = Color.White,
                disabledContainerColor = InventraPurple.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.8f),
            ),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 24.dp),
    ) {
        // O texto fica invisivel (mas ocupando espaco) durante o loading,
        // para o botao nao mudar de tamanho quando o indicador aparece.
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.alpha(if (isLoading) 0f else 1f),
            )
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun InventraPrimaryButtonPreview() {
    InventraPrimaryButton(text = "Confirmar", onClick = {})
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraPrimaryButtonLoadingPreview() {
    InventraPrimaryButton(text = "Confirmar", onClick = {}, isLoading = true)
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraPrimaryButtonDisabledPreview() {
    InventraPrimaryButton(text = "Confirmar", onClick = {}, enabled = false)
}
