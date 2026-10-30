package com.inventraoficial.inventra.feature.auth.passwordreset

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.core.designsystem.organisms.InventraAuthHeader
import com.inventraoficial.inventra.feature.auth.AuthContentMaxWidth
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat

const val RESET_CODE_LENGTH = 6
const val MIN_PASSWORD_LENGTH = 8

private val emailPattern = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun String.looksLikeEmail(): Boolean = emailPattern.matches(this)

@Composable
fun PasswordResetLayout(
    subtitle: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(InventraBackground),
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_shape_yellow_top),
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .size(200.dp)
                    .offset(0.dp, -30.dp),
        )
        Column(
            modifier =
                modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = AuthContentMaxWidth)
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp, 64.dp, 24.dp, 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            InventraAuthHeader(appName = "Inventra", subtitle = subtitle)
            Text(
                text = description,
                fontFamily = Montserrat,
                fontSize = 14.sp,
                color = InventraMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            content()
        }
    }
}

@Composable
fun PasswordResetMessage(
    errorMessage: String?,
    infoMessage: String? = null,
) {
    Text(
        text = errorMessage ?: infoMessage.orEmpty(),
        color = if (errorMessage != null) InventraDanger else InventraPurple,
        minLines = 1,
        modifier = Modifier.fillMaxWidth(),
    )
}
