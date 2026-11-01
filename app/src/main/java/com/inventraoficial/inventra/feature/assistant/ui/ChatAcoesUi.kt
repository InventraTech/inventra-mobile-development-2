package com.inventraoficial.inventra.feature.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

/**
 * Cartão de confirmação de uma ação que a IA propôs (dar baixa, criar requisição...).
 * Nada é alterado no Inventra até o usuário tocar em "Aprovar".
 */
@Composable
internal fun CartaoDeAcao(
    acao: AcaoPendente,
    onAprovar: () -> Unit,
    onRecusar: () -> Unit,
    onExecutar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val expirada = acao.expirou(System.currentTimeMillis())
    val visivel = if (expirada) acao.copy(estado = EstadoDaAcao.EXPIRADA, detalhe = TEXTO_ACAO_EXPIRADA) else acao

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(InventraBackground, FORMA_DO_CARTAO)
                .border(1.dp, InventraPurple.copy(alpha = 0.25f), FORMA_DO_CARTAO)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CabecalhoDoCartao(visivel)
        Divisor()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            visivel.linhas.forEach { LinhaDoCartao(it) }
        }
        visivel.mudanca?.let { MudancaDoCartao(it) }
        if (visivel.justificativa.isNotBlank()) {
            Text(
                text = "Motivo: ${visivel.justificativa}",
                fontFamily = Montserrat,
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = InventraMuted,
            )
        }
        RodapeDoCartao(visivel, onAprovar, onRecusar, onExecutar)
    }
}

/** O saldo do lote antes e depois, lado a lado, para o usuário ver o efeito antes de aprovar. */
@Composable
private fun MudancaDoCartao(mudanca: MudancaDeSaldo) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(InventraPurple.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ValorDoSaldo(rotulo = "Antes", valor = mudanca.antes, destaque = false)
        Text(text = "→", fontSize = 20.sp, color = InventraPurple)
        ValorDoSaldo(rotulo = "Depois", valor = mudanca.depois, destaque = true)
    }
}

@Composable
private fun ValorDoSaldo(
    rotulo: String,
    valor: String,
    destaque: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = rotulo, fontFamily = Montserrat, fontSize = 11.sp, color = InventraMuted)
        Text(
            text = valor,
            fontFamily = Montserrat,
            fontWeight = if (destaque) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            color = if (destaque) InventraPurple else InventraInk,
        )
    }
}

@Composable
private fun CabecalhoDoCartao(acao: AcaoPendente) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(InventraPurple.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = InventraPurple,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = acao.titulo,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = InventraInk,
            )
            seloDoEstado(acao.estado)?.let { SeloDoCartao(it) }
        }
    }
}

private fun seloDoEstado(estado: EstadoDaAcao): String? =
    when (estado) {
        EstadoDaAcao.AGUARDANDO, EstadoDaAcao.APROVANDO, EstadoDaAcao.RECUSANDO -> "Aguardando sua aprovação"
        EstadoDaAcao.APROVADA, EstadoDaAcao.EXECUTANDO -> "Aprovada, falta concluir"
        else -> null
    }

@Composable
private fun SeloDoCartao(texto: String) {
    Text(
        text = texto,
        fontFamily = Montserrat,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = InventraPurple,
        modifier =
            Modifier
                .background(InventraPurple.copy(alpha = 0.12f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

@Composable
private fun Divisor() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(InventraMuted.copy(alpha = 0.2f)),
    )
}

@Composable
private fun LinhaDoCartao(linha: LinhaDaAcao) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = linha.rotulo,
            fontFamily = Montserrat,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = InventraMuted,
            modifier = Modifier.weight(PESO_DO_ROTULO),
        )
        Text(
            text = linha.valor,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = InventraInk,
            modifier = Modifier.weight(PESO_DO_VALOR),
        )
    }
}

@Composable
private fun RodapeDoCartao(
    acao: AcaoPendente,
    onAprovar: () -> Unit,
    onRecusar: () -> Unit,
    onExecutar: () -> Unit,
) {
    when (acao.estado) {
        EstadoDaAcao.AGUARDANDO, EstadoDaAcao.APROVANDO, EstadoDaAcao.RECUSANDO ->
            BotoesDeDecisao(acao, onAprovar, onRecusar)
        EstadoDaAcao.APROVADA, EstadoDaAcao.EXECUTANDO -> BotaoDeExecucao(acao, onExecutar)
        else -> ResultadoDaAcao(acao)
    }
}

@Composable
private fun BotoesDeDecisao(
    acao: AcaoPendente,
    onAprovar: () -> Unit,
    onRecusar: () -> Unit,
) {
    val livre = acao.estado == EstadoDaAcao.AGUARDANDO
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        acao.detalhe?.let { TextoDeErro(it) }
        acao.expiraEmMillis?.let {
            Text(
                text = "Vale até ${prazoLegivel(it)}",
                fontFamily = Montserrat,
                fontSize = 12.sp,
                color = InventraMuted,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onAprovar,
                enabled = livre,
                shape = FORMA_DO_BOTAO,
                modifier = Modifier.weight(1f).height(ALTURA_DO_BOTAO),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = InventraPurple,
                        contentColor = InventraBackground,
                    ),
            ) {
                if (acao.estado == EstadoDaAcao.APROVANDO) {
                    Carregando(InventraBackground)
                } else {
                    Text(text = "Aprovar", fontFamily = Montserrat, fontSize = 14.sp)
                }
            }
            OutlinedButton(
                onClick = onRecusar,
                enabled = livre,
                shape = FORMA_DO_BOTAO,
                modifier = Modifier.weight(1f).height(ALTURA_DO_BOTAO),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = InventraInk),
            ) {
                if (acao.estado == EstadoDaAcao.RECUSANDO) {
                    Carregando(InventraInk)
                } else {
                    Text(text = "Recusar", fontFamily = Montserrat, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun BotaoDeExecucao(
    acao: AcaoPendente,
    onExecutar: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        acao.detalhe?.let {
            Text(
                text = it,
                fontFamily = Montserrat,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = InventraMuted,
            )
        }
        Button(
            onClick = onExecutar,
            enabled = acao.estado == EstadoDaAcao.APROVADA,
            shape = FORMA_DO_BOTAO,
            modifier = Modifier.fillMaxWidth().height(ALTURA_DO_BOTAO),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = InventraPurple,
                    contentColor = InventraBackground,
                ),
        ) {
            if (acao.estado == EstadoDaAcao.EXECUTANDO) {
                Carregando(InventraBackground)
            } else {
                Text(text = "Concluir execução", fontFamily = Montserrat, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ResultadoDaAcao(acao: AcaoPendente) {
    val (icone, cor, titulo) = aparenciaDoResultado(acao.estado)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(imageVector = icone, contentDescription = null, tint = cor, modifier = Modifier.size(20.dp))
            Text(
                text = titulo,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = cor,
            )
        }
        acao.detalhe?.let {
            Text(
                text = it,
                fontFamily = Montserrat,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = InventraInk,
            )
        }
        if (acao.simulado) {
            Text(
                text = "Dados de demonstração: nada foi gravado de verdade.",
                fontFamily = Montserrat,
                fontSize = 12.sp,
                color = InventraMuted,
            )
        }
    }
}

private data class AparenciaDoResultado(
    val icone: ImageVector,
    val cor: Color,
    val titulo: String,
)

@Composable
private fun aparenciaDoResultado(estado: EstadoDaAcao): AparenciaDoResultado =
    when (estado) {
        EstadoDaAcao.CONCLUIDA -> AparenciaDoResultado(Icons.Filled.CheckCircle, InventraSuccess, "Concluída")
        EstadoDaAcao.RECUSADA -> AparenciaDoResultado(Icons.Filled.Cancel, InventraMuted, "Recusada, nada foi alterado")
        EstadoDaAcao.EXPIRADA -> AparenciaDoResultado(Icons.Filled.Schedule, InventraMuted, "Expirada")
        EstadoDaAcao.INCERTA -> AparenciaDoResultado(Icons.Filled.Warning, InventraGold, "Resultado incerto")
        else -> AparenciaDoResultado(Icons.Filled.Error, InventraDanger, "Não foi possível concluir")
    }

@Composable
private fun TextoDeErro(texto: String) {
    Text(
        text = texto,
        fontFamily = Montserrat,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = InventraDanger,
    )
}

@Composable
private fun Carregando(cor: Color) {
    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = cor)
}

private val FORMA_DO_CARTAO = RoundedCornerShape(20.dp)
private val FORMA_DO_BOTAO = RoundedCornerShape(12.dp)
private val ALTURA_DO_BOTAO = 48.dp
private const val PESO_DO_ROTULO = 0.38f
private const val PESO_DO_VALOR = 0.62f

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun CartaoDeAcaoPreview() {
    CartaoDeAcao(
        acao =
            AcaoPendente(
                id = "1",
                propostaHash = "x",
                titulo = "Registrar saída de estoque",
                linhas =
                    listOf(
                        LinhaDaAcao("Lote", "Arroz, lote L-10 (nº 1)"),
                        LinhaDaAcao("Quantidade", "10 kg"),
                    ),
                justificativa = "Consumo informado pelo usuário",
                expiraEmMillis = null,
            ),
        onAprovar = {},
        onRecusar = {},
        onExecutar = {},
    )
}
