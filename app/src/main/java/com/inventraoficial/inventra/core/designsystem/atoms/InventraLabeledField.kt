package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraLabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    multiline: Boolean = false,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Unspecified,
    placeholder: String,
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val hidesText = isPassword && !isPasswordVisible
    val fieldKeyboardType =
        if (isPassword && keyboardType == KeyboardType.Unspecified) KeyboardType.Password else keyboardType
    val visibilityIcon = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            fontFamily = Montserrat,
            fontSize = 18.sp,
            color = InventraPurple,
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, fontSize = 13.sp) },
            readOnly = readOnly,
            singleLine = !multiline,
            keyboardOptions = KeyboardOptions(keyboardType = fieldKeyboardType),
            visualTransformation = if (hidesText) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon =
                if (isPassword) {
                    {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = visibilityIcon,
                                contentDescription = if (isPasswordVisible) "Esconder senha" else "Mostrar senha",
                                tint = InventraPurple,
                            )
                        }
                    }
                } else {
                    null
                },
            shape = RoundedCornerShape(8.dp),
            colors =
                TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = TextFieldDefaults.MinHeight)
                    .border(1.dp, InventraPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
        )
    }
}

@Preview
@Composable
private fun InventraLabeledFieldPreview() {
    InventraLabeledField(
        label = "Login",
        value = "",
        onValueChange = {},
        placeholder = "insira seu Login",
    )
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InventraLabeledFieldPasswordPreview() {
    InventraLabeledField(
        label = "Senha",
        value = "inventra123",
        onValueChange = {},
        isPassword = true,
        placeholder = "insira sua senha",
    )
}
