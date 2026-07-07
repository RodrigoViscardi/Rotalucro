package com.lucronarota.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lucronarota.data.local.dao.*
import com.lucronarota.data.local.entity.*

@Database(
    entities = [
        JornadaEntity::class,
        CorridaEntity::class,
        CustoFixoEntity::class,
        CustoVariavelEntity::class,
        AbastecimentoEntity::class,
        MetaEntity::class,
        PausaJornadaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jornadaDao(): JornadaDao
    abstract fun corridaDao(): CorridaDao
    abstract fun custoFixoDao(): CustoFixoDao
    abstract fun custoVariavelDao(): CustoVariavelDao
    abstract fun abastecimentoDao(): AbastecimentoDao
    abstract fun metaDao(): MetaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lucro_na_rota_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
