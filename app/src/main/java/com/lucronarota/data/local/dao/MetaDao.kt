package com.lucronarota.data.local.dao

import androidx.room.*
import com.lucronarota.data.local.entity.MetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaDao {

    @Query("SELECT * FROM metas ORDER BY ano DESC, mes DESC")
    fun getAll(): Flow<List<MetaEntity>>

    @Query("SELECT * FROM metas WHERE mes = :mes AND ano = :ano LIMIT 1")
    suspend fun getMeta(mes: Int, ano: Int): MetaEntity?

    @Query("SELECT * FROM metas WHERE mes = :mes AND ano = :ano LIMIT 1")
    fun getMetaFlow(mes: Int, ano: Int): Flow<MetaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meta: MetaEntity): Long

    @Update
    suspend fun update(meta: MetaEntity)

    @Query("SELECT * FROM metas ORDER BY ano DESC, mes DESC LIMIT 1")
    suspend fun getUltimaMeta(): MetaEntity?
}
