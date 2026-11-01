package com.inventraoficial.inventra.feature.assistant.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.inventraoficial.inventra.data.repository.AnexoParaEnvio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.UUID

/** Arquivos já copiados para o cache do app + o que ficou de fora (cada item é um texto para o usuário). */
data class AnexosPreparados(
    val anexos: List<AnexoParaEnvio>,
    val ignorados: List<String> = emptyList(),
)

/** Mesmo limite da IA por arquivo (ARQUIVOS_MAX_BYTES); evita subir o que ela vai recusar. */
internal const val TAMANHO_MAXIMO_ANEXO_BYTES = 10L * 1024 * 1024

/** A IA aceita 10 arquivos por envio (o app manda em lotes menores); este é o teto por escolha do usuário. */
internal const val MAXIMO_DE_ANEXOS_POR_ESCOLHA = 20

/** Tipos que o seletor de arquivos oferece (a IA ainda valida o conteúdo de cada um). */
internal val TIPOS_DE_ARQUIVO_ACEITOS =
    arrayOf(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "text/csv",
        "text/plain",
        "text/markdown",
        "image/png",
        "image/jpeg",
        "image/webp",
    )

private const val TAMANHO_DO_BUFFER = 16 * 1024
private const val PASTA_DE_ANEXOS = "anexos"
private val CARACTERES_PROIBIDOS_NO_NOME = Regex("[\\\\/:*?\"<>|]")

private enum class ResultadoCopia { OK, GRANDE, FALHOU }

/** Cada anexo vive numa pasta própria do cache (nomes iguais de pastas diferentes não colidem). */
internal fun novaPastaDoAnexo(context: Context): File =
    File(File(context.cacheDir, PASTA_DE_ANEXOS), UUID.randomUUID().toString()).apply { mkdirs() }

/** Onde a câmera vai gravar a foto (o FileProvider entrega essa pasta do cache à câmera). */
fun novoArquivoDeFoto(context: Context) = File(novaPastaDoAnexo(context), "foto_${System.currentTimeMillis()}.jpg")

fun uriDoArquivo(
    context: Context,
    arquivo: File,
): Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)

/** Foto recém-tirada; recusa a foto vazia ou grande demais (e apaga o arquivo). */
fun prepararFoto(arquivo: File): AnexosPreparados {
    val tamanho = arquivo.length()
    return when {
        tamanho <= 0L -> {
            arquivo.delete()
            AnexosPreparados(emptyList(), listOf("A foto não foi salva."))
        }
        tamanho > TAMANHO_MAXIMO_ANEXO_BYTES -> {
            arquivo.delete()
            AnexosPreparados(emptyList(), listOf("${arquivo.name}: passa de 10 MB."))
        }
        else -> AnexosPreparados(listOf(AnexoParaEnvio(arquivo, arquivo.name)))
    }
}

/** Copia os arquivos escolhidos (fotos ou documentos) para o cache, respeitando o limite de tamanho. */
suspend fun prepararUris(
    context: Context,
    uris: List<Uri>,
): AnexosPreparados =
    withContext(Dispatchers.IO) {
        val anexos = mutableListOf<AnexoParaEnvio>()
        val ignorados = mutableListOf<String>()
        uris.take(MAXIMO_DE_ANEXOS_POR_ESCOLHA).forEach { uri ->
            copiarParaCache(context, uri, nomeDoArquivo(context, uri), anexos, ignorados)
        }
        val demais = uris.size - MAXIMO_DE_ANEXOS_POR_ESCOLHA
        if (demais > 0) {
            ignorados += "Só aceito $MAXIMO_DE_ANEXOS_POR_ESCOLHA arquivos por vez; $demais ficaram de fora."
        }
        AnexosPreparados(anexos, ignorados)
    }

/** Tira barras e caracteres que o Android não aceita em nome de arquivo. */
internal fun nomeLocal(nome: String): String =
    nome
        .substringAfterLast('/')
        .replace(CARACTERES_PROIBIDOS_NO_NOME, "_")
        .ifBlank { "arquivo" }

internal fun consultarNome(
    context: Context,
    uri: Uri,
): String? =
    runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()?.takeIf { it.isNotBlank() }

private fun nomeDoArquivo(
    context: Context,
    uri: Uri,
): String {
    val base = consultarNome(context, uri) ?: uri.lastPathSegment?.substringAfterLast('/') ?: "arquivo"
    val nome = nomeLocal(base)
    if (nome.contains('.')) return nome
    val extensao =
        context.contentResolver
            .getType(uri)
            ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
    return if (extensao != null) "$nome.$extensao" else nome
}

/** Copia [uri] para o cache. O nome que a IA recebe é [nomeParaIa] (pode levar o caminho da pasta). */
@Suppress("SwallowedException")
internal fun copiarParaCache(
    context: Context,
    uri: Uri,
    nomeParaIa: String,
    anexos: MutableList<AnexoParaEnvio>,
    ignorados: MutableList<String>,
) {
    val destino = File(novaPastaDoAnexo(context), nomeLocal(nomeParaIa))
    val resultado =
        try {
            context.contentResolver.openInputStream(uri)?.use { entrada -> copiarComLimite(entrada, destino) }
                ?: ResultadoCopia.FALHOU
        } catch (e: IOException) {
            ResultadoCopia.FALHOU
        } catch (e: SecurityException) {
            ResultadoCopia.FALHOU
        }
    val nomeCurto = nomeParaIa.substringAfterLast('/')
    when (resultado) {
        ResultadoCopia.OK -> anexos += AnexoParaEnvio(destino, nomeParaIa)
        ResultadoCopia.GRANDE -> {
            destino.delete()
            ignorados += "$nomeCurto: passa de 10 MB."
        }
        ResultadoCopia.FALHOU -> {
            destino.delete()
            ignorados += "$nomeCurto: não consegui abrir o arquivo."
        }
    }
}

private fun copiarComLimite(
    entrada: InputStream,
    destino: File,
): ResultadoCopia {
    var total = 0L
    val buffer = ByteArray(TAMANHO_DO_BUFFER)
    destino.outputStream().use { saida ->
        while (true) {
            val lidos = entrada.read(buffer)
            if (lidos < 0) break
            total += lidos
            if (total > TAMANHO_MAXIMO_ANEXO_BYTES) return ResultadoCopia.GRANDE
            saida.write(buffer, 0, lidos)
        }
    }
    return ResultadoCopia.OK
}
