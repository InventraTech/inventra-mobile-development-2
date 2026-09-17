package com.inventraoficial.inventra.core.designsystem.atoms

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    placeholder: String,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            fontFamily = Montserrat,
//            fontWeight = FontWeight.,
            fontSize = 18.sp,
            color = InventraPurple,
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, fontSize = 13.sp) },
            readOnly = readOnly,
            singleLine = !multiline,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
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
                    .height(54.dp)
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
