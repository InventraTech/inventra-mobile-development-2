package com.inventraoficial.inventra.feature.assistant.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.inventraoficial.inventra.data.repository.AnexoParaEnvio
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "ChatComposer"
private const val UM_SEGUNDO_MS = 1_000L
private const val SEGUNDOS_POR_MINUTO = 60
private const val ALPHA_DESABILITADO = 0.38f
private const val NIVEL_A_CADA_MS = 80L
private const val QUANTIDADE_DE_BARRAS = 5
private const val INTERVALO_DAS_BARRAS_MS = 70L
private const val LARGURA_DA_BARRA_DP = 3
private const val ALTURA_MINIMA_DA_BARRA_DP = 4f
private const val ALTURA_MAXIMA_DA_BARRA_DP = 24f
private const val VARIACAO_DA_BARRA_DP = ALTURA_MAXIMA_DA_BARRA_DP - ALTURA_MINIMA_DA_BARRA_DP

/**
 * Campo de mensagem no estilo dos assistentes de IA: "+" para anexos, texto, e à direita
 * microfone (campo vazio) ou enviar (com texto ou arquivos). Os arquivos escolhidos aparecem como
 * chips em cima do campo e só vão quando o usuário envia. Embaixo, o aviso de que a IA pode errar.
 */
@Suppress("TooGenericExceptionCaught")
@Composable
fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAudioGravado: (File) -> Unit,
    carregando: Boolean,
    modifier: Modifier = Modifier,
    onParar: () -> Unit = {},
    onAnexosEscolhidos: (List<AnexoParaEnvio>, List<String>) -> Unit = { _, _ -> },
    anexosPendentes: List<AnexoParaEnvio> = emptyList(),
    onAnexoRemovido: (AnexoParaEnvio) -> Unit = {},
) {
    val context = LocalContext.current
    val recorder = remember { AudioRecorder(context.applicationContext) }
    var gravando by remember { mutableStateOf(false) }
    var segundos by remember { mutableIntStateOf(0) }
    var menuDeAnexosAberto by remember { mutableStateOf(false) }
    // Voz ao vivo: o texto aparece enquanto se fala. Sem reconhecedor no aparelho, grava o áudio e a IA transcreve.
    var vozAoVivoAtiva by remember { mutableStateOf(false) }
    var textoAoVivo by remember { mutableStateOf("") }
    var nivelDaVozAtual by remember { mutableFloatStateOf(0f) }
    val onValueChangeAtual by rememberUpdatedState(onValueChange)
    val onSendAtual by rememberUpdatedState(onSend)

    fun encerrarVoz() {
        gravando = false
        vozAoVivoAtiva = false
        textoAoVivo = ""
        nivelDaVozAtual = 0f
    }

    val vozAoVivo =
        remember {
            VozAoVivo(
                context = context.applicationContext,
                aoMudarTexto = { textoAoVivo = it },
                aoMudarNivel = { nivelDaVozAtual = it },
                aoFalhar = {
                    encerrarVoz()
                    Toast.makeText(context, "Não consegui ouvir. Tente de novo.", Toast.LENGTH_SHORT).show()
                },
            )
        }

    DisposableEffect(Unit) {
        onDispose {
            recorder.cancelar()
            vozAoVivo.cancelar()
        }
    }

    // Sem reconhecedor ao vivo, a bolinha acompanha o volume que o gravador mede.
    LaunchedEffect(gravando, vozAoVivoAtiva) {
        while (gravando && !vozAoVivoAtiva) {
            nivelDaVozAtual = recorder.nivel()
            delay(NIVEL_A_CADA_MS)
        }
    }

    LaunchedEffect(gravando) {
        segundos = 0
        while (gravando) {
            delay(UM_SEGUNDO_MS)
            segundos++
        }
    }

    fun iniciarGravacao() {
        try {
            textoAoVivo = ""
            if (vozAoVivo.disponivel()) {
                vozAoVivo.iniciar()
                vozAoVivoAtiva = true
            } else {
                recorder.iniciar()
            }
            gravando = true
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao iniciar a gravação", e)
            encerrarVoz()
            Toast.makeText(context, "Não foi possível gravar o áudio.", Toast.LENGTH_SHORT).show()
        }
    }

    val permissaoDoMicrofone =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
            if (concedida) {
                iniciarGravacao()
            } else {
                Toast.makeText(context, "Permita o microfone para conversar por áudio.", Toast.LENGTH_SHORT).show()
            }
        }

    val anexos = rememberSeletoresDeAnexos(onAnexosEscolhidos)

    fun aoTocarNoMicrofone() {
        val temPermissao =
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        if (temPermissao) {
            iniciarGravacao()
        } else {
            permissaoDoMicrofone.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(InventraPurple.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            if (anexosPendentes.isNotEmpty() && !gravando) {
                ListaDeAnexos(anexos = anexosPendentes, onRemover = onAnexoRemovido)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = if (gravando) Alignment.CenterVertically else Alignment.Bottom,
            ) {
                if (gravando) {
                    BarraDeGravacao(
                        segundos = segundos,
                        nivel = nivelDaVozAtual,
                        textoAoVivo = textoAoVivo,
                        onCancelar = {
                            if (vozAoVivoAtiva) vozAoVivo.cancelar() else recorder.cancelar()
                            encerrarVoz()
                        },
                        onEnviar = {
                            if (vozAoVivoAtiva) {
                                vozAoVivo.terminar { texto ->
                                    encerrarVoz()
                                    if (texto.isBlank()) {
                                        Toast
                                            .makeText(context, "Não ouvi nada. Tente de novo.", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        onValueChangeAtual(texto)
                                        onSendAtual()
                                    }
                                }
                            } else {
                                val arquivo = recorder.parar()
                                encerrarVoz()
                                if (arquivo != null) onAudioGravado(arquivo)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Box {
                        IconButton(onClick = { menuDeAnexosAberto = true }, enabled = !carregando) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adicionar anexo",
                                tint = InventraInk.copy(alpha = if (carregando) ALPHA_DESABILITADO else 1f),
                            )
                        }
                        DropdownMenu(
                            expanded = menuDeAnexosAberto,
                            onDismissRequest = { menuDeAnexosAberto = false },
                        ) {
                            ItemDeAnexo(Icons.Default.CameraAlt, "Tirar foto") {
                                menuDeAnexosAberto = false
                                anexos.tirarFoto()
                            }
                            ItemDeAnexo(Icons.Default.PhotoLibrary, "Fotos") {
                                menuDeAnexosAberto = false
                                anexos.escolherFotos()
                            }
                            ItemDeAnexo(Icons.Default.AttachFile, "Arquivos") {
                                menuDeAnexosAberto = false
                                anexos.escolherArquivos()
                            }
                            ItemDeAnexo(Icons.Default.FolderOpen, "Pasta") {
                                menuDeAnexosAberto = false
                                anexos.escolherPasta()
                            }
                        }
                    }
                    CampoDeTexto(
                        value = value,
                        onValueChange = onValueChange,
                        dica = if (anexosPendentes.isEmpty()) "Pergunte ao IAI" else "Pergunte sobre o arquivo",
                        modifier = Modifier.weight(1f),
                    )
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        if (carregando) {
                            FilledIconButton(
                                onClick = onParar,
                                modifier = Modifier.size(40.dp),
                                colors =
                                    IconButtonDefaults.filledIconButtonColors(
                                        containerColor = InventraPurple,
                                        contentColor = InventraBackground,
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Parar resposta",
                                )
                            }
                        } else if (value.isNotBlank() || anexosPendentes.isNotEmpty()) {
                            FilledIconButton(
                                onClick = onSend,
                                enabled = !carregando,
                                modifier = Modifier.size(40.dp),
                                colors =
                                    IconButtonDefaults.filledIconButtonColors(
                                        containerColor = InventraPurple,
                                        contentColor = InventraBackground,
                                        disabledContainerColor = InventraPurple.copy(alpha = 0.3f),
                                        disabledContentColor = InventraBackground,
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Enviar",
                                )
                            }
                        } else {
                            IconButton(onClick = ::aoTocarNoMicrofone, enabled = !carregando) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Conversar por áudio",
                                    tint = InventraInk.copy(alpha = if (carregando) ALPHA_DESABILITADO else 1f),
                                )
                            }
                        }
                    }
                }
            }
        }
        Text(
            text = "O IAI é uma IA e pode cometer erros. Confira informações importantes.",
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            textAlign = TextAlign.Center,
            fontFamily = Montserrat,
            fontSize = 8.sp,
            maxLines = 1,
            color = InventraMuted,
        )
    }
}

@Composable
private fun CampoDeTexto(
    value: String,
    onValueChange: (String) -> Unit,
    dica: String,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(fontFamily = Montserrat, fontSize = 15.sp, color = InventraInk),
        cursorBrush = SolidColor(InventraPurple),
        maxLines = 5,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = modifier.padding(horizontal = 4.dp, vertical = 13.dp),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = dica,
                        fontFamily = Montserrat,
                        fontSize = 15.sp,
                        color = InventraMuted,
                    )
                }
                innerTextField()
            }
        },
    )
}

/** Arquivos escolhidos que esperam a mensagem: um chip por arquivo, com "x" para tirar. */
@Composable
private fun ListaDeAnexos(
    anexos: List<AnexoParaEnvio>,
    onRemover: (AnexoParaEnvio) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        anexos.forEach { anexo ->
            ChipDeAnexo(nome = anexo.nome.substringAfterLast('/'), onRemover = { onRemover(anexo) })
        }
    }
}

@Composable
private fun ChipDeAnexo(
    nome: String,
    onRemover: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .widthIn(max = 220.dp)
                .background(InventraBackground, RoundedCornerShape(16.dp))
                .padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = InventraPurple,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = nome,
            modifier = Modifier.weight(1f, fill = false).padding(horizontal = 6.dp),
            fontFamily = Montserrat,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = InventraInk,
        )
        IconButton(onClick = onRemover, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remover $nome",
                tint = InventraMuted,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun BarraDeGravacao(
    segundos: Int,
    nivel: Float,
    textoAoVivo: String,
    onCancelar: () -> Unit,
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCancelar) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancelar gravação",
                tint = InventraInk,
            )
        }
        Text(
            text = if (textoAoVivo.isBlank()) "Ouvindo… ${formatarTempo(segundos)}" else fimDaTranscricao(textoAoVivo),
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            fontFamily = Montserrat,
            fontSize = 15.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = if (textoAoVivo.isBlank()) InventraMuted else InventraInk,
        )
        // O indicador de volume fica só num canto, pequeno, ao lado do botão de enviar.
        OndasDaVoz(nivel = nivel, modifier = Modifier.padding(horizontal = 8.dp))
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            FilledIconButton(
                onClick = onEnviar,
                modifier = Modifier.size(40.dp),
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = InventraPurple,
                        contentColor = InventraBackground,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Enviar",
                )
            }
        }
    }
}

/**
 * Indicador de voz pequeno: poucas barras finas que sobem com a voz e descem no silêncio, como o
 * "equalizador" dos assistentes de voz. A cada instante entra uma barra nova pela direita.
 */
@Composable
private fun OndasDaVoz(
    nivel: Float,
    modifier: Modifier = Modifier,
) {
    val nivelAtual by rememberUpdatedState(nivel)
    val barras = remember { mutableStateListOf<Float>().apply { repeat(QUANTIDADE_DE_BARRAS) { add(0f) } } }

    LaunchedEffect(Unit) {
        while (true) {
            delay(INTERVALO_DAS_BARRAS_MS)
            barras.removeAt(0)
            barras.add(nivelAtual)
        }
    }

    Row(
        modifier = modifier.height(ALTURA_MAXIMA_DA_BARRA_DP.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        barras.forEach { valor ->
            Box(
                modifier =
                    Modifier
                        .width(LARGURA_DA_BARRA_DP.dp)
                        .height((ALTURA_MINIMA_DA_BARRA_DP + VARIACAO_DA_BARRA_DP * valor).dp)
                        .background(InventraPurple, RoundedCornerShape(PILULA_PERCENTUAL)),
            )
        }
    }
}

@Composable
private fun ItemDeAnexo(
    icone: ImageVector,
    rotulo: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(rotulo, fontFamily = Montserrat, fontSize = 14.sp) },
        onClick = onClick,
        leadingIcon = { Icon(imageVector = icone, contentDescription = null) },
    )
}

/** Ações do menu "+": cada uma abre o seletor do Android e devolve os arquivos já copiados para o cache. */
private class SeletoresDeAnexos(
    val tirarFoto: () -> Unit,
    val escolherFotos: () -> Unit,
    val escolherArquivos: () -> Unit,
    val escolherPasta: () -> Unit,
)

@Suppress("TooGenericExceptionCaught")
@Composable
private fun rememberSeletoresDeAnexos(onEscolhidos: (List<AnexoParaEnvio>, List<String>) -> Unit): SeletoresDeAnexos {
    val context = LocalContext.current.applicationContext
    val escopo = rememberCoroutineScope()

    fun entregar(preparados: AnexosPreparados) = onEscolhidos(preparados.anexos, preparados.ignorados)

    val fotos =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) escopo.launch { entregar(prepararUris(context, uris)) }
        }
    val arquivos =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isNotEmpty()) escopo.launch { entregar(prepararUris(context, uris)) }
        }
    val pasta =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { arvore ->
            if (arvore != null) escopo.launch { entregar(prepararPasta(context, arvore)) }
        }

    // O caminho da foto sobrevive se a tela for recriada enquanto a câmera está aberta.
    var fotoPendente by rememberSaveable { mutableStateOf<String?>(null) }
    val camera =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tirou ->
            val arquivo = fotoPendente?.let { File(it) }
            fotoPendente = null
            if (arquivo != null) {
                if (tirou) entregar(prepararFoto(arquivo)) else arquivo.delete()
            }
        }

    fun abrirCamera() {
        try {
            val arquivo = novoArquivoDeFoto(context)
            fotoPendente = arquivo.path
            camera.launch(uriDoArquivo(context, arquivo))
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao abrir a câmera", e)
            Toast.makeText(context, "Não foi possível abrir a câmera.", Toast.LENGTH_SHORT).show()
        }
    }

    val permissaoDaCamera =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
            if (concedida) {
                abrirCamera()
            } else {
                Toast.makeText(context, "Permita a câmera para tirar fotos.", Toast.LENGTH_SHORT).show()
            }
        }

    return SeletoresDeAnexos(
        tirarFoto = {
            val temPermissao =
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
            if (temPermissao) abrirCamera() else permissaoDaCamera.launch(Manifest.permission.CAMERA)
        },
        escolherFotos = { fotos.launch("image/*") },
        escolherArquivos = { arquivos.launch(TIPOS_DE_ARQUIVO_ACEITOS) },
        escolherPasta = { pasta.launch(null) },
    )
}

private fun formatarTempo(segundos: Int): String {
    val minutos = segundos / SEGUNDOS_POR_MINUTO
    val resto = (segundos % SEGUNDOS_POR_MINUTO).toString().padStart(2, '0')
    return "$minutos:$resto"
}
