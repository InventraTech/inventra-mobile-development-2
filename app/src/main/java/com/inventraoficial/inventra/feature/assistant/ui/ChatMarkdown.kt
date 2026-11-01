package com.inventraoficial.inventra.feature.assistant.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/** Converte o markdown da IA em texto estilizado para o balão do chat. */
fun markdownParaTexto(
    markdown: String,
    corDeFundoDoCodigo: Color = Color.Unspecified,
): AnnotatedString =
    buildAnnotatedString {
        interpretarMarkdown(markdown).forEach { trecho ->
            val estilo =
                SpanStyle(
                    fontWeight = if (trecho.negrito) FontWeight.Bold else null,
                    fontStyle = if (trecho.italico) FontStyle.Italic else null,
                    fontFamily = if (trecho.codigo) FontFamily.Monospace else null,
                    background = if (trecho.codigo) corDeFundoDoCodigo else Color.Unspecified,
                )
            withStyle(estilo) { append(trecho.texto) }
        }
    }
