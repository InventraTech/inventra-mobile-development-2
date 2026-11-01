package com.inventraoficial.inventra.feature.assistant.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.ui.theme.InventraDanger
import com.inventraoficial.inventra.ui.theme.InventraGold
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.InventraSuccess
import com.inventraoficial.inventra.ui.theme.Montserrat

/** Tabela ou gráfico que a IA montou com dados confirmados, desenhado embaixo da resposta em texto. */
@Composable
internal fun ArtefatoDaIa(
    artefato: Artefato,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .border(1.dp, InventraMuted.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (artefato.simulado) AvisoDeSimulacao()
        when (artefato) {
            is Artefato.Tabela -> TabelaDaIa(artefato)
            is Artefato.Grafico ->
                if (artefato.tipo == TipoDeGrafico.PIZZA) PizzaDaIa(artefato) else BarrasDaIa(artefato)
        }
        artefato.nota?.let { NotaDoArtefato(it) }
    }
}

@Composable
private fun NotaDoArtefato(texto: String) {
    Text(
        text = texto,
        fontFamily = Montserrat,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        color = InventraMuted,
    )
}

@Composable
private fun AvisoDeSimulacao() {
    Text(
        text = "Dados de demonstração",
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        color = InventraMuted,
        modifier =
            Modifier
                .background(InventraGold.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

// ---------- tabela ----------

@Composable
private fun TabelaDaIa(tabela: Artefato.Tabela) {
    var mostrarTodas by remember { mutableStateOf(false) }
    val larguras = remember(tabela) { larguraDasColunas(tabela) }
    val numericas = remember(tabela) { tabela.colunas.indices.map { colunaEhNumerica(tabela, it) } }
    val linhas = if (mostrarTodas) tabela.linhas else tabela.linhas.take(LINHAS_INICIAIS)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            LinhaDaTabela(
                celulas = tabela.colunas.map { it.titulo },
                larguras = larguras,
                numericas = numericas,
                fundo = InventraPurple.copy(alpha = 0.14f),
                cabecalho = true,
            )
            linhas.forEachIndexed { posicao, linha ->
                val fundo = if (posicao % 2 == 1) InventraPurple.copy(alpha = 0.06f) else Color.Transparent
                LinhaDaTabela(linha, larguras, numericas, fundo)
            }
        }
        if (tabela.linhas.size > LINHAS_INICIAIS) {
            TextButton(onClick = { mostrarTodas = !mostrarTodas }) {
                Text(
                    text = if (mostrarTodas) "Mostrar menos" else "Ver todas as ${tabela.linhas.size} linhas",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = InventraPurple,
                )
            }
        }
    }
}

@Composable
private fun LinhaDaTabela(
    celulas: List<String>,
    larguras: List<Dp>,
    numericas: List<Boolean>,
    fundo: Color,
    cabecalho: Boolean = false,
) {
    Row(modifier = Modifier.background(fundo)) {
        celulas.forEachIndexed { indice, texto ->
            Text(
                text = texto,
                modifier = Modifier.width(larguras[indice]).padding(horizontal = 6.dp, vertical = 6.dp),
                textAlign = if (numericas[indice]) TextAlign.Center else TextAlign.Start,
                fontFamily = Montserrat,
                fontWeight = if (cabecalho) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = InventraInk,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Largura de cada coluna pelo maior texto dela (cabeçalho incluído), sem passar do razoável no celular. */
private fun larguraDasColunas(tabela: Artefato.Tabela): List<Dp> =
    tabela.colunas.mapIndexed { indice, coluna ->
        val maior = maxOf(coluna.titulo.length, tabela.linhas.maxOfOrNull { it[indice].length } ?: 0)
        (maior.coerceIn(CARACTERES_MINIMOS, CARACTERES_MAXIMOS) * DP_POR_CARACTERE + DP_DE_FOLGA).dp
    }

// ---------- barras ----------

@Composable
private fun BarrasDaIa(grafico: Artefato.Grafico) {
    var mostrarTodas by remember { mutableStateOf(false) }
    val cores = paletaDoGrafico()
    val maximo = grafico.series.maxOf { serie -> serie.valores.maxOrNull() ?: 0.0 }.coerceAtLeast(MENOR_MAXIMO)
    val total = grafico.categorias.size
    val visiveis = if (mostrarTodas) total else minOf(total, BARRAS_INICIAIS)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Legenda(grafico.series.map { it.nome }, cores)
        for (indice in 0 until visiveis) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = grafico.categorias[indice],
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = InventraInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                grafico.series.forEachIndexed { posicao, serie ->
                    BarraDoValor(
                        valor = serie.valores[indice],
                        maximo = maximo,
                        cor = cores[posicao % cores.size],
                        texto = formatarValor(serie.valores[indice], grafico.unidade),
                    )
                }
            }
        }
        if (total > BARRAS_INICIAIS) {
            TextButton(onClick = { mostrarTodas = !mostrarTodas }) {
                Text(
                    text = if (mostrarTodas) "Mostrar menos" else "Ver todos os $total itens",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = InventraPurple,
                )
            }
        }
    }
}

@Composable
private fun BarraDoValor(
    valor: Double,
    maximo: Double,
    cor: Color,
    texto: String,
) {
    val fracao = (valor / maximo).coerceIn(0.0, 1.0).toFloat()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(10.dp)
                    .background(cor.copy(alpha = 0.15f), RoundedCornerShape(5.dp)),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(fracao)
                        .fillMaxHeight()
                        .background(cor, RoundedCornerShape(5.dp)),
            )
        }
        Text(
            text = texto,
            modifier = Modifier.width(LARGURA_DO_VALOR),
            textAlign = TextAlign.End,
            fontFamily = Montserrat,
            fontSize = 12.sp,
            color = InventraInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---------- pizza ----------

@Composable
private fun PizzaDaIa(grafico: Artefato.Grafico) {
    val cores = paletaDoGrafico()
    val fatias = remember(grafico) { agruparFatias(grafico.categorias, grafico.series.first().valores, FATIAS_MAXIMAS) }
    val total = fatias.sumOf { it.valor }
    if (total <= 0.0) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Canvas(
            modifier =
                Modifier
                    .size(TAMANHO_DA_PIZZA)
                    .semantics { contentDescription = descricaoDoArtefato(grafico) },
        ) {
            var inicio = ANGULO_INICIAL
            fatias.forEachIndexed { indice, fatia ->
                val varredura = (fatia.valor / total * VOLTA_COMPLETA).toFloat()
                drawArc(
                    color = cores[indice % cores.size],
                    startAngle = inicio,
                    sweepAngle = varredura,
                    useCenter = true,
                )
                inicio += varredura
            }
        }
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            fatias.forEachIndexed { indice, fatia ->
                val percentual = percentualLegivel(fatia.valor, total)
                LinhaDaLegenda(
                    cor = cores[indice % cores.size],
                    nome = fatia.nome,
                    valor = "${formatarValor(fatia.valor, grafico.unidade)} ($percentual)",
                )
            }
        }
    }
}

// ---------- peças comuns ----------

/** Só aparece quando há mais de uma série (por exemplo, quantidade atual e estoque mínimo). */
@Composable
private fun Legenda(
    nomes: List<String>,
    cores: List<Color>,
) {
    if (nomes.size < 2) return
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        nomes.forEachIndexed { indice, nome ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(modifier = Modifier.size(10.dp).background(cores[indice % cores.size], CircleShape))
                Text(text = nome, fontFamily = Montserrat, fontSize = 12.sp, color = InventraMuted)
            }
        }
    }
}

@Composable
private fun LinhaDaLegenda(
    cor: Color,
    nome: String,
    valor: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(10.dp).background(cor, CircleShape))
        Text(
            text = nome,
            modifier = Modifier.weight(1f),
            fontFamily = Montserrat,
            fontSize = 13.sp,
            color = InventraInk,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(text = valor, fontFamily = Montserrat, fontSize = 13.sp, color = InventraMuted)
    }
}

@Composable
private fun paletaDoGrafico(): List<Color> =
    listOf(
        InventraPurple,
        InventraGold,
        InventraSuccess,
        InventraDanger,
        COR_AZUL,
        COR_MARROM,
    )

@Suppress("MagicNumber")
private val COR_AZUL = Color(0xFF2E86AB)

@Suppress("MagicNumber")
private val COR_MARROM = Color(0xFF8D6E63)

private const val LINHAS_INICIAIS = 8
private const val BARRAS_INICIAIS = 8
private const val FATIAS_MAXIMAS = 6
private const val CARACTERES_MINIMOS = 5
private const val CARACTERES_MAXIMOS = 24
private const val DP_POR_CARACTERE = 7
private const val DP_DE_FOLGA = 20
private const val MENOR_MAXIMO = 1e-9
private const val ANGULO_INICIAL = -90f
private const val VOLTA_COMPLETA = 360.0
private val LARGURA_DO_VALOR = 88.dp
private val TAMANHO_DA_PIZZA = 150.dp

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun ArtefatoBarrasPreview() {
    ArtefatoDaIa(
        Artefato.Grafico(
            tipo = TipoDeGrafico.BARRAS,
            categorias = listOf("Arroz", "Feijão", "Açúcar"),
            series =
                listOf(
                    SerieDoGrafico("Quantidade atual", listOf(12.0, 3.5, 8.0)),
                    SerieDoGrafico("Estoque mínimo", listOf(10.0, 6.0, 5.0)),
                ),
            unidade = "kg",
            simulado = true,
        ),
    )
}
