package com.inventraoficial.inventra.feature.assistant.ui

import com.inventraoficial.inventra.data.repository.AnexoParaEnvio

data class Mensagem(
    val id: String,
    val conteudo: String,
    val ehDoUsuario: Boolean,
    /** Tabela ou gráfico que a IA montou junto com a resposta. */
    val artefato: Artefato? = null,
    /** Ações que a IA propôs nesta resposta e esperam o usuário aprovar ou recusar. */
    val acoes: List<AcaoPendente> = emptyList(),
    /** O que a IA foi fazendo enquanto respondia (consultar o estoque, conferir a resposta...). */
    val passos: List<PassoIa> = emptyList(),
    /** Resumo do raciocínio do modelo, acumulado enquanto chega. Vazio quando o modelo não expõe. */
    val pensamento: String = "",
    /** Quanto a IA levou para responder, em segundos (só em respostas vistas ao vivo). */
    val duracaoSegundos: Int? = null,
)

/** Um passo do trabalho da IA: gira enquanto roda e vira ✓ quando termina. */
data class PassoIa(
    val id: String,
    val texto: String,
    val concluido: Boolean,
    /** Passo em que este roda (a tool dentro do nó da IA); null nos passos de primeiro nível. */
    val pai: String? = null,
    /** O comando que o passo executou, para quem quiser ver (ex.: planejar_baixa(...)). */
    val detalhe: String? = null,
)

/** Uma conversa anterior listada no menu lateral. */
data class ConversaResumo(
    val id: String,
    val titulo: String,
    /** Quando a conversa começou (epoch em milissegundos); 0 quando a IA não informou. */
    val iniciadaEmMillis: Long = 0L,
)

data class ChatUiState(
    val greeting: String = "Olá!",
    val inputValue: String = "",
    val mensagens: List<Mensagem> = emptyList(),
    val conversas: List<ConversaResumo> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null,
    val sessionId: String = "sessao_${System.currentTimeMillis()}",
    /** Quantas respostas da IA a conversa atual já teve. */
    val respostasDaIa: Int = 0,
    /** Quando a pergunta em andamento foi enviada (epoch ms); alimenta o contador de tempo. Null = nada em andamento. */
    val iniciouEmMillis: Long? = null,
    /** Resposta da IA que ainda está sendo "digitada" na tela. */
    val mensagemAnimandoId: String? = null,
    val historicoCarregando: Boolean = false,
    /** Conversa do histórico aberta em modo leitura (null = conversa ao vivo). */
    val conversaVisualizadaId: String? = null,
    val abrindoConversaId: String? = null,
    /** A conversa ao vivo guardada enquanto uma conversa do histórico está aberta. */
    val conversaEmAndamento: ChatUiState? = null,
    /** Aviso curto para o usuário (ex.: falha ao abrir uma conversa); a tela mostra e limpa. */
    val aviso: String? = null,
    /** Arquivos escolhidos no "+" que esperam a mensagem do usuário; só sobem quando ele envia. */
    val anexosPendentes: List<AnexoParaEnvio> = emptyList(),
)
