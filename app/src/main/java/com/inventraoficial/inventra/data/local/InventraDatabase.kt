package com.inventraoficial.inventra.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class InventraDatabase(
    context: Context,
) : SQLiteOpenHelper(context, NAME, null, VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $STOCK_TABLE (
                $KITCHEN_ID INTEGER NOT NULL,
                $PRODUCT_ID INTEGER NOT NULL,
                $POSITION INTEGER NOT NULL,
                $PAYLOAD TEXT NOT NULL,
                $SAVED_AT INTEGER NOT NULL,
                PRIMARY KEY ($KITCHEN_ID, $PRODUCT_ID)
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        db.execSQL("DROP TABLE IF EXISTS $STOCK_TABLE")
        onCreate(db)
    }

    companion object {
        private const val NAME = "inventra.db"
        private const val VERSION = 1
        const val STOCK_TABLE = "kitchen_stock_cache"
        const val KITCHEN_ID = "kitchen_id"
        const val PRODUCT_ID = "product_id"
        const val POSITION = "position"
        const val PAYLOAD = "payload"
        const val SAVED_AT = "saved_at"
    }
}
