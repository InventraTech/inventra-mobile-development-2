package com.inventraoficial.inventra.feature.assistant.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inventraoficial.inventra.core.designsystem.organisms.InventraScreenContent
import com.inventraoficial.inventra.data.repository.AnexoParaEnvio
import com.inventraoficial.inventra.ui.navigation.Navigator
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ChatScreen(
    greeting: String,
    inputValue: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onAudioGravado: (File) -> Unit,
    onNovaConversaClick: () -> Unit,
    onInicioClick: () -> Unit,
    modifier: Modifier = Modifier,
    mensagens: List<Mensagem> = emptyList(),
    conversas: List<ConversaResumo> = emptyList(),
    carregando: Boolean = false,
    iniciouEmMillis: Long? = null,
    onParar: () -> Unit = {},
    historicoCarregando: Boolean = false,
    mensagemAnimandoId: String? = null,
    conversaVisualizadaId: String? = null,
    temConversaEmAndamento: Boolean = false,
    aviso: String? = null,
    onConversaClick: (ConversaResumo) -> Unit = {},
    onMenuAberto: () -> Unit = {},
    onDigitacaoTerminou: (String) -> Unit = {},
    onVoltarParaConversaAtual: () -> Unit = {},
    onAvisoMostrado: () -> Unit = {},
    onAnexosEscolhidos: (List<AnexoParaEnvio>, List<String>) -> Unit = { _, _ -> },
    anexosPendentes: List<AnexoParaEnvio> = emptyList(),
    onAnexoRemovido: (AnexoParaEnvio) -> Unit = {},
    onAcaoAprovada: (String) -> Unit = {},
    onAcaoRecusada: (String) -> Unit = {},
    onAcaoExecutar: (String) -> Unit = {},
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    LaunchedEffect(aviso) {
        if (aviso != null) {
            Toast.makeText(context, aviso, Toast.LENGTH_SHORT).show()
            onAvisoMostrado()
        }
    }

    // Abrir uma conversa do histórico troca o conteúdo da tela; o menu fecha quando ela já abriu.
    LaunchedEffect(conversaVisualizadaId) {
        if (conversaVisualizadaId != null) drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        modifier = modifier,
        drawerContent = {
            ChatHistoryDrawer(
                conversas = conversas,
                carregando = historicoCarregando,
                conversaSelecionadaId = conversaVisualizadaId,
                onNovaConversaClick = {
                    scope.launch { drawerState.close() }
                    onNovaConversaClick()
                },
                onConversaClick = onConversaClick,
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(InventraBackground)
                    .imePadding(),
        ) {
            ChatTopBar(
                onMenuClick = {
                    onMenuAberto()
                    scope.launch { drawerState.open() }
                },
                onInicioClick = onInicioClick,
            )
            InventraScreenContent {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                ) {
                    if (mensagens.isEmpty()) {
                        ChatEmptyState(greeting = greeting)
                    } else {
                        ChatMessageList(
                            mensagens = mensagens,
                            carregando = carregando,
                            iniciouEmMillis = iniciouEmMillis,
                            mensagemAnimandoId = mensagemAnimandoId,
                            onDigitacaoTerminou = onDigitacaoTerminou,
                            onAcaoAprovada = onAcaoAprovada,
                            onAcaoRecusada = onAcaoRecusada,
                            onAcaoExecutar = onAcaoExecutar,
                        )
                    }
                }
                if (conversaVisualizadaId != null) {
                    BarraDeConversaEncerrada(
                        temConversaEmAndamento = temConversaEmAndamento,
                        onVoltarParaConversaAtual = onVoltarParaConversaAtual,
                        onNovaConversaClick = onNovaConversaClick,
                        modifier = Modifier.navigationBarsPadding(),
                    )
                } else {
                    ChatComposer(
                        value = inputValue,
                        onValueChange = onInputChange,
                        onSend = onSend,
                        onAudioGravado = onAudioGravado,
                        carregando = carregando,
                        onParar = onParar,
                        modifier = Modifier.navigationBarsPadding(),
                        onAnexosEscolhidos = onAnexosEscolhidos,
                        anexosPendentes = anexosPendentes,
                        onAnexoRemovido = onAnexoRemovido,
                    )
                }
            }
        }
    }
}

/** No lugar do campo de mensagem quando o usuário está lendo uma conversa do histórico. */
@Composable
private fun BarraDeConversaEncerrada(
    temConversaEmAndamento: Boolean,
    onVoltarParaConversaAtual: () -> Unit,
    onNovaConversaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(InventraPurple.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "Conversa encerrada — somente leitura",
            textAlign = TextAlign.Center,
            fontFamily = Montserrat,
            fontSize = 12.sp,
            color = InventraMuted,
        )
        TextButton(onClick = if (temConversaEmAndamento) onVoltarParaConversaAtual else onNovaConversaClick) {
            Text(
                text = if (temConversaEmAndamento) "Voltar à conversa atual" else "Iniciar nova conversa",
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = InventraPurple,
            )
        }
    }
}

@Composable
fun ChatRoute(
    navigator: Navigator,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    ChatScreen(
        greeting = uiState.greeting,
        inputValue = uiState.inputValue,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::onSend,
        onAudioGravado = viewModel::onAudioGravado,
        onNovaConversaClick = viewModel::onNovaSessao,
        onInicioClick = { viewModel.onVoltarAoInicio { navigator.back() } },
        modifier = modifier,
        mensagens = uiState.mensagens,
        conversas = uiState.conversas,
        carregando = uiState.carregando,
        iniciouEmMillis = uiState.iniciouEmMillis,
        onParar = viewModel::onPararResposta,
        historicoCarregando = uiState.historicoCarregando,
        mensagemAnimandoId = uiState.mensagemAnimandoId,
        conversaVisualizadaId = uiState.conversaVisualizadaId,
        temConversaEmAndamento = uiState.conversaEmAndamento?.mensagens?.isNotEmpty() == true,
        aviso = uiState.aviso,
        onConversaClick = viewModel::onConversaClick,
        onMenuAberto = viewModel::carregarHistorico,
        onDigitacaoTerminou = viewModel::onDigitacaoTerminou,
        onVoltarParaConversaAtual = viewModel::onVoltarParaConversaAtual,
        onAvisoMostrado = viewModel::onAvisoMostrado,
        onAnexosEscolhidos = viewModel::onAnexosEscolhidos,
        anexosPendentes = uiState.anexosPendentes,
        onAnexoRemovido = viewModel::onAnexoRemovido,
        onAcaoAprovada = viewModel::onAcaoAprovada,
        onAcaoRecusada = viewModel::onAcaoRecusada,
        onAcaoExecutar = viewModel::onAcaoExecutar,
    )
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun ChatScreenVaziaPreview() {
    ChatScreen(
        greeting = "Olá!",
        inputValue = "",
        onInputChange = {},
        onSend = {},
        onAudioGravado = {},
        onNovaConversaClick = {},
        onInicioClick = {},
    )
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun ChatScreenComConversaPreview() {
    ChatScreen(
        greeting = "Olá!",
        inputValue = "",
        onInputChange = {},
        onSend = {},
        onAudioGravado = {},
        onNovaConversaClick = {},
        onInicioClick = {},
        mensagens =
            listOf(
                Mensagem(id = "1", conteudo = "Quais produtos vencem essa semana?", ehDoUsuario = true),
                Mensagem(
                    id = "2",
                    conteudo =
                        "Você tem 3 lotes vencendo nos próximos 7 dias: " +
                            "leite integral, iogurte natural e queijo minas.",
                    ehDoUsuario = false,
                ),
            ),
    )
}
