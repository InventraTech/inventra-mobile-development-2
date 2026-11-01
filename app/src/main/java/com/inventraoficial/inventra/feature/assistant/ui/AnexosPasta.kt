package com.inventraoficial.inventra.feature.assistant.ui

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.inventraoficial.inventra.data.repository.AnexoParaEnvio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val EXTENSOES_ACEITAS =
    setOf("pdf", "docx", "xlsx", "pptx", "csv", "txt", "md", "png", "jpg", "jpeg", "webp")
private const val PROFUNDIDADE_MAXIMA_DA_PASTA = 4

private fun extensaoAceita(nome: String): Boolean = nome.substringAfterLast('.', "").lowercase() in EXTENSOES_ACEITAS

/** Lê a árvore de uma pasta escolhida pelo usuário e separa os arquivos de tipos aceitos dos demais. */
private class LeitorDePasta(
    private val context: Context,
    private val arvore: Uri,
) {
    /** Arquivos aceitos, cada um com o caminho relativo ("Cardapios/verao/prato.pdf"). */
    val arquivos = mutableListOf<Pair<Uri, String>>()
    var tipoNaoSuportado = 0
    var passouDoLimite = false

    fun percorrer(
        documentoId: String,
        caminho: String,
        nivel: Int,
    ) {
        for ((id, nome, mime) in filhosDe(documentoId).sortedBy { it.second.lowercase() }) {
            val nomeComCaminho = "$caminho/${nomeLocal(nome)}"
            when {
                mime == DocumentsContract.Document.MIME_TYPE_DIR ->
                    if (nivel < PROFUNDIDADE_MAXIMA_DA_PASTA) percorrer(id, nomeComCaminho, nivel + 1)
                !extensaoAceita(nome) -> tipoNaoSuportado++
                arquivos.size >= MAXIMO_DE_ANEXOS_POR_ESCOLHA -> passouDoLimite = true
                else -> arquivos += DocumentsContract.buildDocumentUriUsingTree(arvore, id) to nomeComCaminho
            }
        }
    }

    /** Filhos diretos do documento: (id, nome, tipo). */
    private fun filhosDe(documentoId: String): List<Triple<String, String, String>> {
        val filhos = DocumentsContract.buildChildDocumentsUriUsingTree(arvore, documentoId)
        val colunas =
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
            )
        val itens = mutableListOf<Triple<String, String, String>>()
        runCatching {
            context.contentResolver.query(filhos, colunas, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    itens +=
                        Triple(
                            cursor.getString(0).orEmpty(),
                            cursor.getString(1).orEmpty(),
                            cursor.getString(2).orEmpty(),
                        )
                }
            }
        }
        return itens
    }
}

/**
 * Percorre a pasta escolhida (até [PROFUNDIDADE_MAXIMA_DA_PASTA] níveis) e copia os arquivos de tipos
 * aceitos. O nome enviado à IA leva o caminho ("Cardapios/verao/prato.pdf"); ela guarda isso como a pasta.
 */
suspend fun prepararPasta(
    context: Context,
    arvore: Uri,
): AnexosPreparados =
    withContext(Dispatchers.IO) {
        val raizId = DocumentsContract.getTreeDocumentId(arvore)
        val nomeDaRaiz =
            consultarNome(context, DocumentsContract.buildDocumentUriUsingTree(arvore, raizId))
                ?.let(::nomeLocal) ?: "pasta"
        val leitor = LeitorDePasta(context, arvore)
        leitor.percorrer(raizId, nomeDaRaiz, 1)

        val anexos = mutableListOf<AnexoParaEnvio>()
        val ignorados = mutableListOf<String>()
        leitor.arquivos.forEach { (uri, nomeComCaminho) ->
            copiarParaCache(context, uri, nomeComCaminho, anexos, ignorados)
        }
        if (leitor.tipoNaoSuportado > 0) {
            ignorados += "${leitor.tipoNaoSuportado} arquivo(s) da pasta de tipo não suportado foram ignorados."
        }
        if (leitor.passouDoLimite) {
            ignorados += "A pasta tem mais de $MAXIMO_DE_ANEXOS_POR_ESCOLHA arquivos; enviei só os primeiros."
        }
        if (leitor.arquivos.isEmpty() && leitor.tipoNaoSuportado == 0) ignorados += "A pasta está vazia."
        AnexosPreparados(anexos, ignorados)
    }
