package com.inventraoficial.inventra.feature.assistant.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatMessageList(
    mensagens: List<Mensagem>,
    carregando: Boolean,
    modifier: Modifier = Modifier,
    mensagemAnimandoId: String? = null,
    iniciouEmMillis: Long? = null,
    onDigitacaoTerminou: (String) -> Unit = {},
    onAcaoAprovada: (String) -> Unit = {},
    onAcaoRecusada: (String) -> Unit = {},
    onAcaoExecutar: (String) -> Unit = {},
) {
    val listState = rememberLazyListState()
    // Com a resposta já chegando aos poucos (passos, pensamento ou texto), ela mesma mostra que a IA trabalha.
    val respostaChegando = carregando && mensagens.lastOrNull()?.ehDoUsuario == false
    val mostrarIndicador = carregando && !respostaChegando
    // As bolinhas no fim da resposta mostram que a IA ainda está trabalhando, mesmo com o texto já aparecendo.
    val mostrarAtividade = carregando && respostaChegando
    val totalItens = mensagens.size + (if (mostrarIndicador) 1 else 0) + (if (mostrarAtividade) 1 else 0)
    val crescimentoDoTexto = remember { mutableIntStateOf(0) }
    val escopo = rememberCoroutineScope()
    val longeDoFim by remember { derivedStateOf { listState.canScrollForward } }

    LaunchedEffect(totalItens) {
        if (totalItens > 0) listState.animateScrollToItem(totalItens - 1)
    }

    // Enquanto a resposta "digita", a lista acompanha o texto, a não ser que o usuário tenha rolado para cima.
    LaunchedEffect(listState) {
        snapshotFlow { crescimentoDoTexto.intValue }.collect {
            // Espera um quadro: o conteúdo novo precisa estar medido para a rolagem chegar ao fim dele.
            withFrameNanos { }
            val info = listState.layoutInfo
            val ultimoVisivel = info.visibleItemsInfo.lastOrNull()?.index
            if (ultimoVisivel != null && ultimoVisivel == info.totalItemsCount - 1) {
                listState.scrollBy(SEGUIR_TEXTO_EM_PX)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(mensagens, key = { it.id }) { mensagem ->
                if (mensagem.ehDoUsuario) {
                    MensagemDoUsuario(texto = mensagem.conteudo)
                } else {
                    MensagemDaIa(
                        texto = mensagem.conteudo,
                        animar = mensagem.id == mensagemAnimandoId,
                        onTextoCresceu = { crescimentoDoTexto.intValue += 1 },
                        onDigitacaoTerminou = { onDigitacaoTerminou(mensagem.id) },
                        artefato = mensagem.artefato,
                        acoes = mensagem.acoes,
                        passos = mensagem.passos,
                        pensamento = mensagem.pensamento,
                        duracaoSegundos = mensagem.duracaoSegundos,
                        emAndamento = carregando && mensagem.id == mensagens.lastOrNull()?.id,
                        onAcaoAprovada = onAcaoAprovada,
                        onAcaoRecusada = onAcaoRecusada,
                        onAcaoExecutar = onAcaoExecutar,
                    )
                }
            }
            if (mostrarIndicador) {
                item(key = "digitando") {
                    IndicadorDePensando(iniciouEmMillis)
                }
            }
            if (mostrarAtividade) {
                item(key = "atividade") {
                    Row(
                        modifier = Modifier.padding(start = 36.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        PontinhosAnimados(InventraPurple)
                        TempoDecorrido(iniciouEmMillis)
                    }
                }
            }
        }
        if (longeDoFim) {
            BotaoIrParaOFim(
                trabalhando = carregando,
                onClick = {
                    escopo.launch {
                        if (totalItens > 0) listState.animateScrollToItem(totalItens - 1)
                        listState.scrollBy(SEGUIR_TEXTO_EM_PX)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun MensagemDoUsuario(texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Box(
            modifier =
                Modifier
                    .widthIn(max = 300.dp)
                    .background(InventraPurple.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            SelectionContainer {
                Text(
                    text = texto,
                    fontFamily = Montserrat,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = InventraInk,
                )
            }
        }
    }
}

/** Resposta da IA. Quando [animar], o texto aparece aos poucos, como se a IA estivesse digitando. */
@Composable
private fun MensagemDaIa(
    texto: String,
    animar: Boolean,
    onTextoCresceu: () -> Unit,
    onDigitacaoTerminou: () -> Unit,
    artefato: Artefato? = null,
    acoes: List<AcaoPendente> = emptyList(),
    passos: List<PassoIa> = emptyList(),
    pensamento: String = "",
    emAndamento: Boolean = false,
    duracaoSegundos: Int? = null,
    onAcaoAprovada: (String) -> Unit = {},
    onAcaoRecusada: (String) -> Unit = {},
    onAcaoExecutar: (String) -> Unit = {},
) {
    var visiveis by remember(texto) { mutableIntStateOf(if (animar) 0 else texto.length) }

    LaunchedEffect(texto, animar) {
        if (!animar) return@LaunchedEffect
        try {
            while (visiveis < texto.length) {
                delay(INTERVALO_DA_DIGITACAO_MS)
                visiveis = proximoLimiteDaDigitacao(texto, visiveis)
                onTextoCresceu()
            }
        } finally {
            // Também avisa se a mensagem saiu da tela no meio da digitação, para a lista não ficar esperando.
            onDigitacaoTerminou()
        }
    }

    // Tabela, gráfico e cartões entram quando o texto termina de "digitar"; a lista acompanha o crescimento.
    LaunchedEffect(animar) {
        if (!animar && (artefato != null || acoes.isNotEmpty())) {
            delay(ESPERA_DO_LAYOUT_MS)
            onTextoCresceu()
        }
    }

    // Resposta que chega aos poucos: a lista acompanha passos, pensamento e texto novos.
    LaunchedEffect(texto, pensamento, passos) {
        if (!animar) onTextoCresceu()
    }

    val corDeFundoDoCodigo = InventraPurple.copy(alpha = 0.10f)
    val textoVisivel = if (visiveis >= texto.length) texto else fecharMarcadoresAbertos(texto.substring(0, visiveis))
    val textoFormatado =
        remember(textoVisivel, corDeFundoDoCodigo) {
            markdownParaTexto(textoVisivel, corDeFundoDoCodigo)
        }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AvatarDaIa()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (passos.isNotEmpty()) PassosDaIa(passos, aberto = emAndamento)
            if (pensamento.isNotBlank()) PensamentoDaIa(pensamento, corDeFundoDoCodigo, aberto = emAndamento)
            if (textoFormatado.isNotEmpty()) {
                SelectionContainer {
                    Text(
                        text = textoFormatado,
                        fontFamily = Montserrat,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = InventraInk,
                    )
                }
            }
            if (!emAndamento && duracaoSegundos != null) {
                Text(
                    text = "Respondeu em ${formatarDuracao(duracaoSegundos.toLong())}",
                    fontFamily = Montserrat,
                    fontSize = 11.sp,
                    color = InventraMuted,
                )
            }
            if (!animar) {
                artefato?.let { ArtefatoDaIa(it) }
                acoes.forEach { acao ->
                    CartaoDeAcao(
                        acao = acao,
                        onAprovar = { onAcaoAprovada(acao.id) },
                        onRecusar = { onAcaoRecusada(acao.id) },
                        onExecutar = { onAcaoExecutar(acao.id) },
                    )
                }
            }
        }
    }
}

/**
 * O que a IA foi fazendo, em linhas: gira enquanto roda e ganha ✓ quando termina. Cada tool aparece
 * sob o passo em que rodou. Enquanto a resposta chega ([aberto]) os passos ficam abertos; depois
 * recolhem sozinhos, e a setinha abre ou fecha cada um. Um passo com comando mostra o comando ao abrir.
 */
@Composable
private fun PassosDaIa(
    passos: List<PassoIa>,
    aberto: Boolean,
) {
    val grupos = remember(passos) { agruparPassos(passos) }
    val escolhaDoUsuario = remember { mutableStateMapOf<String, Boolean>() }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        grupos.forEach { grupo ->
            val aberta = escolhaDoUsuario[grupo.passo.id] ?: aberto
            val temDetalhes = grupo.filhos.isNotEmpty()
            LinhaDoPasso(
                passo = grupo.passo,
                expansivel = temDetalhes,
                aberta = aberta,
                onClick = { escolhaDoUsuario[grupo.passo.id] = !aberta },
            )
            if (aberta) {
                Column(
                    modifier = Modifier.padding(start = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    grupo.filhos.forEach { filho -> PassoFilho(filho) }
                }
            }
        }
    }
}

@Composable
private fun PassoFilho(passo: PassoIa) {
    var comandoAberto by remember(passo.id) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinhaDoPasso(
            passo = passo,
            expansivel = passo.detalhe != null,
            aberta = comandoAberto,
            onClick = { comandoAberto = !comandoAberto },
        )
        if (comandoAberto && passo.detalhe != null) {
            SelectionContainer {
                Text(
                    text = passo.detalhe,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(InventraPurple.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = InventraInk,
                )
            }
        }
    }
}

@Composable
private fun LinhaDoPasso(
    passo: PassoIa,
    expansivel: Boolean,
    aberta: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .clickable(enabled = expansivel, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SetinhaDoFluxo(visivel = expansivel, aberta = aberta)
        Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
            if (passo.concluido) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = InventraPurple,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = InventraPurple,
                )
            }
        }
        Text(
            text = passo.texto,
            modifier = Modifier.weight(1f),
            fontFamily = Montserrat,
            fontSize = 13.sp,
            color = InventraMuted,
        )
    }
}

/** ▸ fechado, ▾ aberto, ao lado do texto; sem seta, deixa o mesmo espaço para os textos alinharem. */
@Composable
private fun SetinhaDoFluxo(
    visivel: Boolean,
    aberta: Boolean,
) {
    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
        if (visivel) {
            Icon(
                imageVector =
                    if (aberta) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (aberta) "Recolher" else "Expandir",
                modifier = Modifier.size(22.dp),
                tint = InventraMuted,
            )
        }
    }
}

/**
 * Raciocínio do modelo: menor, em itálico e com uma barra ao lado, para não se confundir com a resposta.
 * Aberto enquanto a resposta chega ([aberto]); depois recolhe numa linha com o início, e a setinha abre de novo.
 */
@Composable
private fun PensamentoDaIa(
    pensamento: String,
    corDeFundoDoCodigo: Color,
    aberto: Boolean,
) {
    var escolhaDoUsuario by remember { mutableStateOf<Boolean?>(null) }
    val aberta = escolhaDoUsuario ?: aberto
    val formatado = remember(pensamento, corDeFundoDoCodigo) { markdownParaTexto(pensamento, corDeFundoDoCodigo) }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .clickable { escolhaDoUsuario = !aberta },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SetinhaDoFluxo(visivel = true, aberta = aberta)
        Row(
            modifier = Modifier.weight(1f).height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(InventraPurple.copy(alpha = 0.35f), RoundedCornerShape(1.dp)),
            )
            if (aberta) {
                Text(
                    text = formatado,
                    modifier = Modifier.weight(1f),
                    fontFamily = Montserrat,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = InventraMuted,
                )
            } else {
                Text(
                    text = previaDoPensamento(pensamento),
                    modifier = Modifier.weight(1f),
                    fontFamily = Montserrat,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = InventraMuted,
                )
            }
        }
    }
}

/** Contador ao vivo de quanto a IA já está levando para responder. */
@Composable
private fun TempoDecorrido(desde: Long?) {
    if (desde == null) return
    var agora by remember(desde) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(desde) {
        while (true) {
            agora = System.currentTimeMillis()
            delay(UM_SEGUNDO_MS)
        }
    }
    Text(
        text = formatarDuracao((agora - desde) / UM_SEGUNDO_MS),
        fontFamily = Montserrat,
        fontSize = 12.sp,
        color = InventraMuted,
    )
}

private const val QUANTIDADE_DE_PONTINHOS = 3
private const val DEFASAGEM_DO_PONTINHO_MS = 160

/** Três bolinhas que pulsam: a IA ainda está trabalhando. */
@Composable
private fun PontinhosAnimados(cor: Color) {
    val transicao = rememberInfiniteTransition(label = "pontinhos")
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(QUANTIDADE_DE_PONTINHOS) { indice ->
            val alpha by transicao.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 500),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(indice * DEFASAGEM_DO_PONTINHO_MS),
                    ),
                label = "pontinho$indice",
            )
            Box(modifier = Modifier.size(7.dp).background(cor.copy(alpha = alpha), CircleShape))
        }
    }
}

/** Aparece quando o usuário rolou para cima e volta ao fim: bolinhas se a IA trabalha, senão a seta. */
@Composable
private fun BotaoIrParaOFim(
    trabalhando: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .shadow(4.dp, RoundedCornerShape(PILULA_PERCENTUAL))
                .background(InventraPurple, RoundedCornerShape(PILULA_PERCENTUAL))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (trabalhando) {
            PontinhosAnimados(Color.White)
        } else {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Ir para o fim da conversa",
                modifier = Modifier.size(22.dp),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun IndicadorDePensando(iniciouEmMillis: Long?) {
    val transicao = rememberInfiniteTransition(label = "pensando")
    val alpha by transicao.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 700), RepeatMode.Reverse),
        label = "pensandoAlpha",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AvatarDaIa()
        Text(
            text = "Pensando…",
            fontFamily = Montserrat,
            fontSize = 15.sp,
            color = InventraMuted.copy(alpha = alpha),
        )
        TempoDecorrido(iniciouEmMillis)
    }
}

@Composable
private fun AvatarDaIa() {
    Image(
        painter = painterResource(id = R.drawable.ic_mascote),
        contentDescription = "IAI",
        modifier = Modifier.size(28.dp).clip(CircleShape),
    )
}

private const val INTERVALO_DA_DIGITACAO_MS = 30L
private const val UM_SEGUNDO_MS = 1_000L
private const val ESPERA_DO_LAYOUT_MS = 60L
private const val SEGUIR_TEXTO_EM_PX = 10_000f
