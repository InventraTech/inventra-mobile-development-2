package com.inventraoficial.inventra.feature.assistant.ui

import com.inventraoficial.inventra.data.remote.api.AcaoPendenteDto
import com.inventraoficial.inventra.data.remote.api.EventoIa
import kotlinx.serialization.json.JsonElement

/**
 * Junta os eventos de POST /chat/stream numa resposta que cresce. Cada tipo de evento alimenta uma
 * parte diferente da bolha (passos, pensamento, texto...), e a tela as desenha de formas distintas.
 * Lógica pura, sem Android: testada direto.
 */
internal class RespostaEmStream {
    private var passos: List<PassoIa> = emptyList()
    private var pensamento: String = ""
    private var ultimoPensador: String? = null
    private var texto: String = ""
    private var artefatos: List<JsonElement> = emptyList()
    private var acoes: List<AcaoPendenteDto> = emptyList()

    var terminou: Boolean = false
        private set
    var respostaSegura: Boolean = false
        private set

    /** Há algo para mostrar? Antes do primeiro evento a tela segue no "Pensando…". */
    var temConteudo: Boolean = false
        private set

    /** Aplica o evento; devolve false para os que não mudam a bolha (fim e erro são do chamador). */
    fun aplicar(evento: EventoIa): Boolean {
        when (evento) {
            is EventoIa.Etapa -> aplicarEtapa(evento)

            is EventoIa.Pensamento -> {
                // Especialistas em paralelo pensam ao mesmo tempo: um parágrafo novo quando muda quem fala.
                if (pensamento.isNotEmpty() && ultimoPensador != evento.id) pensamento += "\n\n"
                ultimoPensador = evento.id
                pensamento += evento.delta
            }

            is EventoIa.ArtefatoPronto -> artefatos = artefatos + evento.dados

            is EventoIa.AcaoProposta -> acoes = acoes + evento.acao

            is EventoIa.Texto -> texto += evento.delta

            is EventoIa.Fim -> {
                terminou = true
                respostaSegura = evento.respostaSegura
                return false
            }

            is EventoIa.Erro, is EventoIa.RespostaCompleta -> return false
        }
        temConteudo = true
        return true
    }

    private fun aplicarEtapa(evento: EventoIa.Etapa) {
        val atual = passos.firstOrNull { it.id == evento.id }
        passos =
            if (atual == null) {
                passos + PassoIa(evento.id, evento.texto.orEmpty(), evento.concluida, evento.pai, evento.detalhe)
            } else {
                passos.map {
                    if (it.id == evento.id) {
                        it.copy(
                            texto = evento.texto ?: it.texto,
                            concluido = evento.concluida,
                            pai = evento.pai ?: it.pai,
                            detalhe = evento.detalhe ?: it.detalhe,
                        )
                    } else {
                        it
                    }
                }
            }
    }

    /** Quando a conexão cai no meio, os passos que ainda giravam deixam de girar. */
    fun encerrarPassosPendentes() {
        passos = passos.map { it.copy(concluido = true) }
    }

    fun paraMensagem(id: String): Mensagem {
        val cartoes = acoes.map { it.paraAcao() }
        return Mensagem(
            id = id,
            conteudo = if (cartoes.isEmpty()) texto else textoComCartoes(texto, cartoes.size),
            ehDoUsuario = false,
            artefato = lerArtefatos(artefatos).firstOrNull(),
            acoes = cartoes,
            passos = passos,
            pensamento = pensamento,
        )
    }
}

/** Os cartões trazem os detalhes das propostas; o texto fica com a frase de resumo e o que sobrar. */
internal fun textoComCartoes(
    texto: String,
    quantidade: Int,
): String {
    val resto = limparTextoDasPropostas(texto)
    val resumo = resumoDasPropostas(quantidade)
    return if (resto.isBlank()) resumo else "$resumo\n\n$resto"
}
