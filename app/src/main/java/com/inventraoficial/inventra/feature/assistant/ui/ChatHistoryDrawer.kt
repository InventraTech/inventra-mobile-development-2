package com.inventraoficial.inventra.feature.assistant.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventraoficial.inventra.R
import com.inventraoficial.inventra.ui.theme.InventraBackground
import com.inventraoficial.inventra.ui.theme.InventraInk
import com.inventraoficial.inventra.ui.theme.InventraMuted
import com.inventraoficial.inventra.ui.theme.InventraPurple
import com.inventraoficial.inventra.ui.theme.Montserrat
import java.time.LocalDate
import java.time.ZoneId

/** Menu lateral: nova conversa e o histórico, agrupado por data (Hoje, Ontem, Últimos 7 dias...). */
@Composable
fun ChatHistoryDrawer(
    conversas: List<ConversaResumo>,
    onNovaConversaClick: () -> Unit,
    onConversaClick: (ConversaResumo) -> Unit,
    modifier: Modifier = Modifier,
    carregando: Boolean = false,
    conversaSelecionadaId: String? = null,
) {
    val grupos = remember(conversas) { agruparPorData(conversas, LocalDate.now(), ZoneId.systemDefault()) }

    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = InventraBackground,
        drawerContentColor = InventraInk,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mascote),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(CircleShape),
            )
            Text(
                text = "IAI",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = InventraInk,
            )
        }

        NavigationDrawerItem(
            label = { RotuloDoItem("Nova conversa") },
            icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
            selected = false,
            onClick = onNovaConversaClick,
            colors = coresDoItem(),
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp)) {
            if (conversas.isEmpty()) {
                Text(
                    text = if (carregando) "Carregando conversas…" else "Suas conversas anteriores aparecerão aqui.",
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                    fontFamily = Montserrat,
                    fontSize = 13.sp,
                    color = InventraMuted,
                )
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 12.dp)) {
                    grupos.forEach { grupo ->
                        item(key = "grupo_${grupo.titulo}") {
                            Text(
                                text = grupo.titulo,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp),
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = InventraMuted,
                            )
                        }
                        items(grupo.conversas, key = { it.id }) { conversa ->
                            NavigationDrawerItem(
                                label = { RotuloDoItem(conversa.titulo) },
                                selected = conversa.id == conversaSelecionadaId,
                                onClick = { onConversaClick(conversa) },
                                colors = coresDoItem(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RotuloDoItem(texto: String) {
    Text(
        text = texto,
        fontFamily = Montserrat,
        fontSize = 15.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun coresDoItem(): NavigationDrawerItemColors =
    NavigationDrawerItemDefaults.colors(
        unselectedContainerColor = Color.Transparent,
        unselectedIconColor = InventraInk,
        unselectedTextColor = InventraInk,
        selectedContainerColor = InventraPurple.copy(alpha = 0.12f),
        selectedIconColor = InventraInk,
        selectedTextColor = InventraInk,
    )
