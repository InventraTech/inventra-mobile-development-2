package com.inventraoficial.inventra.feature.assistant.ui

import kotlin.math.sqrt

// Regras da entrada por voz que não dependem de Android: ficam aqui para serem testadas direto.

private const val RMS_MINIMO_DB = -2f
private const val FAIXA_DO_RMS_DB = 12f
private const val AMPLITUDE_MAXIMA = 32_767f
private const val TAMANHO_DO_TRECHO_VISIVEL = 90

/** O reconhecedor de voz informa o volume em dB (de uns -2 a uns 10); vira 0 (silêncio) a 1 (alto). */
internal fun nivelDaVoz(rmsDb: Float): Float = ((rmsDb - RMS_MINIMO_DB) / FAIXA_DO_RMS_DB).coerceIn(0f, 1f)

/** O gravador de arquivo informa a amplitude de 0 a 32767; a raiz deixa a voz baixa mais visível. */
internal fun nivelDoMicrofone(amplitudeMaxima: Int): Float = sqrt((amplitudeMaxima / AMPLITUDE_MAXIMA).coerceIn(0f, 1f))

/** Junta o que já foi entendido com o trecho novo, com um espaço só entre eles. */
internal fun juntarTranscricao(
    acumulado: String,
    novo: String,
): String = listOf(acumulado.trim(), novo.trim()).filter { it.isNotEmpty() }.joinToString(" ")

/** Só o fim da fala cabe na barra: o texto longo mostra "…" e as últimas palavras. */
internal fun fimDaTranscricao(
    texto: String,
    maximo: Int = TAMANHO_DO_TRECHO_VISIVEL,
): String = if (texto.length <= maximo) texto else "…" + texto.takeLast(maximo).trimStart()
