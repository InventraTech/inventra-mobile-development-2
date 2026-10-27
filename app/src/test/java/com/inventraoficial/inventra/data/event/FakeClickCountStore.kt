package com.inventraoficial.inventra.data.event

class FakeClickCountStore(
    initial: Map<String, Long> = emptyMap(),
) : ClickCountStore {
    private val saved = initial.toMutableMap()

    override suspend fun counts(): Map<String, Long> = saved.toMap()

    override suspend fun add(column: String) {
        saved[column] = (saved[column] ?: 0) + 1
    }

    override suspend fun subtract(sent: Map<String, Long>) {
        sent.forEach { (column, amount) ->
            val left = (saved[column] ?: 0) - amount
            if (left > 0) saved[column] = left else saved.remove(column)
        }
    }
}
