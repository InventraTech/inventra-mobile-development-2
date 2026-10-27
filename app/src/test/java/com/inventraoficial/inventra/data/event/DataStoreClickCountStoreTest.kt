package com.inventraoficial.inventra.data.event

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DataStoreClickCountStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun TestScope.store() =
        DataStoreClickCountStore(
            PreferenceDataStoreFactory.create(scope = backgroundScope) {
                File(folder.root, "ui_clicks.preferences_pb")
            },
        )

    @Test
    fun `add soma um na coluna do elemento`() =
        runTest {
            val store = store()

            store.add("nav_mb_global_home")
            store.add("nav_mb_global_home")
            store.add("nav_mb_global_stock")

            assertEquals(mapOf("nav_mb_global_home" to 2L, "nav_mb_global_stock" to 1L), store.counts())
        }

    @Test
    fun `subtract desconta o que foi enviado e remove o que zerou`() =
        runTest {
            val store = store()
            repeat(3) { store.add("nav_mb_global_home") }
            store.add("nav_mb_global_stock")

            store.subtract(mapOf("nav_mb_global_home" to 2L, "nav_mb_global_stock" to 1L))

            assertEquals(mapOf("nav_mb_global_home" to 1L), store.counts())
        }

    @Test
    fun `subtract de tudo deixa o buffer vazio`() =
        runTest {
            val store = store()
            store.add("nav_mb_global_home")

            store.subtract(mapOf("nav_mb_global_home" to 1L))

            assertTrue(store.counts().isEmpty())
        }
}
