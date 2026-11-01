package com.inventraoficial.inventra.feature.assistant.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Transcrição ao vivo: o texto aparece enquanto o usuário fala e o volume da voz anima a bolinha.
 *
 * Usa o reconhecedor de voz do Android. Ele para sozinho nas pausas; aqui ele é reiniciado até o
 * usuário tocar em enviar, e cada trecho entendido vai sendo somado. Tudo roda na thread principal.
 */
class VozAoVivo(
    private val context: Context,
    private val aoMudarTexto: (String) -> Unit,
    private val aoMudarNivel: (Float) -> Unit,
    private val aoFalhar: () -> Unit,
) {
    private val principal = Handler(Looper.getMainLooper())
    private var reconhecedor: SpeechRecognizer? = null
    private var ativo = false
    private var encerrando = false
    private var acumulado = ""
    private var parcial = ""
    private var aoTerminar: ((String) -> Unit)? = null

    fun disponivel(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    /** Começa a ouvir. Lança exceção se o reconhecedor não puder ser criado. */
    fun iniciar() {
        cancelar()
        ativo = true
        encerrando = false
        acumulado = ""
        parcial = ""
        reconhecedor = SpeechRecognizer.createSpeechRecognizer(context).also { it.setRecognitionListener(ouvinte) }
        ouvir()
    }

    /** Para de ouvir e entrega tudo o que foi entendido (espera um instante pelo último trecho). */
    fun terminar(aoTerminar: (String) -> Unit) {
        if (!ativo) {
            aoTerminar("")
            return
        }
        encerrando = true
        this.aoTerminar = aoTerminar
        reconhecedor?.stopListening()
        principal.postDelayed({ entregar() }, ESPERA_PELO_ULTIMO_TRECHO_MS)
    }

    /** Descarta o que foi ouvido. */
    fun cancelar() {
        ativo = false
        encerrando = false
        aoTerminar = null
        principal.removeCallbacksAndMessages(null)
        reconhecedor?.destroy()
        reconhecedor = null
        aoMudarNivel(0f)
    }

    private fun ouvir() {
        val intencao =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, IDIOMA)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
        reconhecedor?.startListening(intencao)
    }

    private fun ouvirDeNovoEm(atrasoMs: Long) {
        principal.postDelayed({ if (ativo && !encerrando) ouvir() }, atrasoMs)
    }

    private fun entregar() {
        val destino = aoTerminar ?: return
        val texto = juntarTranscricao(acumulado, parcial)
        cancelar()
        destino(texto)
    }

    private fun melhorTexto(resultados: Bundle?): String =
        resultados?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()

    private val ouvinte =
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit

            override fun onBeginningOfSpeech() = Unit

            override fun onRmsChanged(rmsdB: Float) = aoMudarNivel(nivelDaVoz(rmsdB))

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() = aoMudarNivel(0f)

            override fun onEvent(
                eventType: Int,
                params: Bundle?,
            ) = Unit

            override fun onPartialResults(partialResults: Bundle?) {
                parcial = melhorTexto(partialResults)
                aoMudarTexto(juntarTranscricao(acumulado, parcial))
            }

            override fun onResults(results: Bundle?) {
                acumulado = juntarTranscricao(acumulado, melhorTexto(results))
                parcial = ""
                aoMudarTexto(acumulado)
                if (encerrando) entregar() else ouvirDeNovoEm(REINICIO_APOS_RESULTADO_MS)
            }

            override fun onError(error: Int) {
                when {
                    encerrando -> entregar()
                    // Silêncio ou fala não entendida: o usuário ainda não tocou em enviar, segue ouvindo.
                    error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        ouvirDeNovoEm(REINICIO_APOS_RESULTADO_MS)
                    error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        reconhecedor?.cancel()
                        ouvirDeNovoEm(REINICIO_QUANDO_OCUPADO_MS)
                    }
                    else -> aoFalhar()
                }
            }
        }

    private companion object {
        const val IDIOMA = "pt-BR"
        const val REINICIO_APOS_RESULTADO_MS = 150L
        const val REINICIO_QUANDO_OCUPADO_MS = 500L
        const val ESPERA_PELO_ULTIMO_TRECHO_MS = 1_500L
    }
}
