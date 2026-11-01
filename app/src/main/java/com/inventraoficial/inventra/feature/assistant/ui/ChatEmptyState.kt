package com.inventraoficial.inventra.feature.assistant.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.Montserrat

/** Tela inicial: só aparece enquanto a conversa não começou. */
@Composable
fun ChatEmptyState(
    greeting: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_mascote),
            contentDescription = "Mascote do IAI",
            modifier = Modifier.size(104.dp).clip(CircleShape),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = greeting,
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            color = InventraInk,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Como posso ajudar?",
            fontFamily = Montserrat,
            fontSize = 15.sp,
            color = InventraMuted,
        )
    }
}
