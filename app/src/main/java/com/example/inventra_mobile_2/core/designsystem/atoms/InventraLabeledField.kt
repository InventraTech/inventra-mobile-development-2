package com.example.inventra_mobile_2.core.designsystem.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventra_mobile_2.ui.theme.InventraPurple
import com.example.inventra_mobile_2.ui.theme.Montserrat

@Composable
fun InventraLabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    multiline: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        Text(
            text = label,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = InventraPurple
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            singleLine = !multiline,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun LabeledInventraFieldPreview() {
    InventraLabeledField(
        label = "Login",
        value = "",
        onValueChange = {}
    )
}