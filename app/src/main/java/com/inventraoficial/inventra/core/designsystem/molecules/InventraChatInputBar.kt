package com.inventraoficial.inventra.core.designsystem.molecules

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

@Composable
fun InventraChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontFamily = Montserrat, fontSize = 15.sp),
            cursorBrush = SolidColor(InventraPurple),
            modifier =
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.5.dp, InventraPurple, RoundedCornerShape(12.dp)),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            "Digite sua pergunta...",
                            fontFamily = Montserrat,
                            fontSize = 15.sp,
                            color = InventraPurple.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                }
            },
        )
        Text(
            text = "➤",
            color = InventraGold,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.clickable(onClick = onSend),
        )
    }
}

@Preview
@Composable
private fun InventraChatInputBarPreview() {
    InventraChatInputBar(value = "", onValueChange = {}, onSend = {})
}
