package com.inventraoficial.inventra.data.remote.dto.prelist

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class PreListResponseTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `listagem le o cabecalho mesmo com items nulo`() {
        val body = """[{"id":3,"listDate":"2026-10-27","sourceId":null,"kitchenId":1,"items":null}]"""

        val preLists = json.decodeFromString<List<PreListSummaryResponse>>(body)

        assertEquals(listOf(PreListSummaryResponse(id = 3, listDate = "2026-10-27")), preLists)
    }

    @Test
    fun `detalhe le os itens com produto e fornecedor`() {
        val body =
            """
            {"id":3,"listDate":"2026-10-27","sourceId":null,"kitchenId":1,"items":[
              {"id":9,"product":{"id":5,"name":"Leite Integral","photoUrl":null},
               "supplier":{"id":2,"legalName":"Italac"},"quantity":12.000}
            ]}
            """.trimIndent()

        val item = json.decodeFromString<PreListResponse>(body).items.single()

        assertEquals("Leite Integral", item.product.name)
        assertEquals(12.0, item.quantity, 0.0)
    }
}
