package com.inventraoficial.inventra.feature.assistant.ui

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Grava o microfone em um arquivo .m4a (AAC) dentro do cache do app. */
class AudioRecorder(
    private val context: Context,
) {
    private var recorder: MediaRecorder? = null
    private var arquivo: File? = null

    /** Começa a gravar. Lança exceção se o microfone não puder ser aberto. */
    @Suppress("TooGenericExceptionCaught")
    fun iniciar() {
        cancelar()
        val destino = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
        val novo = criarRecorder()
        try {
            novo.setAudioSource(MediaRecorder.AudioSource.MIC)
            novo.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            novo.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            novo.setAudioSamplingRate(SAMPLE_RATE_HZ)
            novo.setAudioEncodingBitRate(BIT_RATE)
            novo.setOutputFile(destino.absolutePath)
            novo.prepare()
            novo.start()
        } catch (e: Exception) {
            runCatching { novo.release() }
            destino.delete()
            throw e
        }
        recorder = novo
        arquivo = destino
    }

    /** Para a gravação e devolve o arquivo, ou null se não gravou nada aproveitável. */
    fun parar(): File? {
        val atual = recorder ?: return null
        val destino = arquivo
        recorder = null
        arquivo = null

        val gravouComSucesso =
            try {
                atual.stop()
                true
            } catch (e: RuntimeException) {
                // stop() falha quando a gravação foi curta demais para gerar áudio.
                false
            } finally {
                atual.release()
            }

        if (!gravouComSucesso) {
            destino?.delete()
            return null
        }
        return destino?.takeIf { it.exists() && it.length() > 0 }
    }

    /** Volume de agora, de 0 a 1 (maior amplitude desde a última leitura). */
    fun nivel(): Float {
        val atual = recorder ?: return 0f
        return runCatching { nivelDoMicrofone(atual.maxAmplitude) }.getOrDefault(0f)
    }

    /** Descarta a gravação em andamento, se houver. */
    fun cancelar() {
        val atual = recorder
        val destino = arquivo
        recorder = null
        arquivo = null
        if (atual != null) {
            runCatching { atual.stop() }
            atual.release()
        }
        destino?.delete()
    }

    @Suppress("DEPRECATION")
    private fun criarRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

    private companion object {
        const val SAMPLE_RATE_HZ = 44_100
        const val BIT_RATE = 128_000
    }
}
