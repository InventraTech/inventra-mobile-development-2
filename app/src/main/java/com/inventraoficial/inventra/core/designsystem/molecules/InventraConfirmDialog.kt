package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

/**
 * Modal de confirmacao. Com [isDestructive], o botao de confirmar fica vermelho,
 * para acoes que nao podem ser desfeitas.
 */
@Composable
fun InventraConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = "Cancelar",
    isDestructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = Color.White,
        title = {
            Text(
                text = title,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = InventraPurple,
            )
        },
        text = {
            Text(
                text = message,
                fontFamily = Montserrat,
                fontSize = 15.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    color = if (isDestructive) InventraDanger else InventraPurple,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    color = InventraPurple,
                )
            }
        },
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraConfirmDialogPreview() {
    InventraConfirmDialog(
        title = "Sair da conta?",
        message = "Você precisará entrar com seu e-mail e senha novamente.",
        confirmText = "Sair",
        onConfirm = {},
        onDismiss = {},
        isDestructive = true,
    )
}
