package com.inventraoficial.inventra.data.local

import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.remote.dto.auth.KitchenSummary
import com.inventraoficial.inventra.data.remote.dto.auth.ProfileSummary
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionUserTest {
    private val userSemCozinha =
        UserResponse(
            id = "00000000-0000-0000-0000-000000000001",
            name = "Felipe",
            email = "felipe@inventra.com",
            profile = ProfileSummary(id = 2, accessType = AccessType.ESTOQUISTA),
            active = true,
            createdAt = "2026-10-09T00:00:00Z",
        )

    @Test
    fun `converte o usuario da API para o modelo da sessao`() {
        val sessionUser = userSemCozinha.toSessionUser()

        assertEquals("00000000-0000-0000-0000-000000000001", sessionUser.id)
        assertEquals("Felipe", sessionUser.name)
        assertEquals("felipe@inventra.com", sessionUser.email)
        assertEquals(AccessType.ESTOQUISTA, sessionUser.accessType)
    }

    @Test
    fun `usuario sem cozinha fica com kitchenId e kitchenName nulos`() {
        val sessionUser = userSemCozinha.toSessionUser()

        assertNull(sessionUser.kitchenId)
        assertNull(sessionUser.kitchenName)
    }

    @Test
    fun `usuario com cozinha leva o id e o nome dela`() {
        val comCozinha = userSemCozinha.copy(kitchen = KitchenSummary(id = 7, name = "Sabor Filial Sul"))

        val sessionUser = comCozinha.toSessionUser()

        assertEquals(7, sessionUser.kitchenId)
        assertEquals("Sabor Filial Sul", sessionUser.kitchenName)
    }

    @Test
    fun `sobrevive a ida e volta em JSON, que e como vai ser guardado no DataStore`() {
        val original = userSemCozinha.copy(kitchen = KitchenSummary(id = 7, name = "Sabor Filial Sul")).toSessionUser()

        val texto = Json.encodeToString(SessionUser.serializer(), original)
        val lido = Json.decodeFromString(SessionUser.serializer(), texto)

        assertEquals(original, lido)
    }

    @Test
    fun `so supervisor sem cozinha precisa criar cozinha`() {
        val supervisor = userSemCozinha.copy(profile = ProfileSummary(id = 1, accessType = AccessType.SUPERVISOR))
        val cozinha = KitchenSummary(id = 7, name = "Sabor Filial Sul")

        assertTrue(supervisor.toSessionUser().mustCreateKitchen)
        assertFalse(supervisor.copy(kitchen = cozinha).toSessionUser().mustCreateKitchen)
        assertFalse(userSemCozinha.toSessionUser().mustCreateKitchen)
    }
}
