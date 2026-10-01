package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CommentEntity
import com.example.data.model.DocumentEntity
import com.example.data.model.MailDao
import com.example.data.model.MailMessageEntity
import com.example.data.model.ProductKeyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DocumentEntity::class, ProductKeyEntity::class, CommentEntity::class, MailMessageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ArcDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun productKeyDao(): ProductKeyDao
    abstract fun commentDao(): CommentDao
    abstract fun mailDao(): MailDao

    companion object {
        @Volatile
        private var INSTANCE: ArcDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ArcDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArcDatabase::class.java,
                    "arc_suite_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(ArcDatabaseCallback(context, scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class ArcDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database, context)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: ArcDatabase, context: Context) {
    val keyDao = database.productKeyDao()

    // 1. Populate the 100 authentic product keys from Arc Suite.txt asset
    // NOTE: All keys are unactivated upon fresh installation. The user must provide their key and custom email.
    val keysList = mutableListOf<ProductKeyEntity>()
    try {
        context.assets.open("Arc Suite.txt").bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val regex = """^(\d+)\.\s+(ARCS-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4})\s+\|\s+Tier:\s+(.*)$""".toRegex()
                val match = regex.find(line.trim())
                if (match != null) {
                    val idx = match.groupValues[1].toInt()
                    val key = match.groupValues[2]
                    val tier = match.groupValues[3]
                    keysList.add(
                        ProductKeyEntity(
                            keyIndex = idx,
                            productKey = key,
                            tier = tier,
                            isActivated = false,
                            activatedOwner = "",
                            activatedDate = 0L,
                            deviceSeatsMax = if (tier.contains("Unlimited")) 999 else if (tier.contains("25")) 25 else if (tier.contains("10")) 10 else if (tier.contains("Personal") || tier.contains("1 Seat")) 1 else 5,
                            deviceSeatsUsed = 0
                        )
                    )
                }
            }
        }
    } catch (_: Exception) {}

    if (keysList.isNotEmpty()) {
        keyDao.insertKeys(keysList)
    } else {
        // Fallback generator for 5 Arc 360 tiers
        val tiers = listOf(
            "Arc 360 Personal (1 Seat • 500 GB Cloud)",
            "Arc 360 Enterprise Ultra (Unlimited Seats • 8 TB Cloud)",
            "Arc 360 Pro Creative Cloud (10 Seats • 2 TB Cloud)",
            "Arc 360 Business Suite (25 Seats • 4 TB Cloud)",
            "Arc 360 Ultimate Studio (5 Seats • 1 TB Cloud)"
        )
        val fallbackList = mutableListOf<ProductKeyEntity>()
        for (i in 1..100) {
            val tier = tiers[(i - 1) % tiers.size]
            val key = when (i % 5) {
                1 -> "ARCS-HBTR-QJGF-5CBF-PQBN"
                2 -> "ARCS-4Q6T-AL5X-TKPX-GF2G"
                3 -> "ARCS-YYSC-7H2F-UZNE-CQUF"
                4 -> "ARCS-QG2T-7ZLZ-YPTE-LRL7"
                else -> "ARCS-2TQW-DQCW-3TEP-WP93"
            }
            fallbackList.add(
                ProductKeyEntity(
                    keyIndex = i,
                    productKey = key,
                    tier = tier,
                    isActivated = false,
                    activatedOwner = "",
                    deviceSeatsMax = if (tier.contains("Unlimited")) 999 else if (tier.contains("25")) 25 else if (tier.contains("10")) 10 else if (tier.contains("Personal")) 1 else 5,
                    deviceSeatsUsed = 0
                )
            )
        }
        keyDao.insertKeys(fallbackList)
    }

    // 2. Sample documents removed: User starts with a clean slate as requested!
    // No pre-seeded sample documents.
}
