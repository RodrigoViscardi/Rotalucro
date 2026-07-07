package com.lucronarota.data.local.dao

import androidx.room.*
import com.lucronarota.data.local.entity.CorridaEntity
import com.lucronarota.data.model.ClassificacaoCorrida
import com.lucronarota.data.model.Plataforma
import kotlinx.coroutines.flow.Flow

@Dao
interface CorridaDao {

    @Query("SELECT * FROM corridas ORDER BY timestamp DESC")
    fun getAllCorridas(): Flow<List<CorridaEntity>>

    @Query("SELECT * FROM corridas WHERE jornadaId = :jornadaId ORDER BY timestamp")
    suspend fun getCorridasDaJornada(jornadaId: Long): List<CorridaEntity>

    @Query("SELECT * FROM corridas WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    fun getCorridasDoDia(startOfDay: Long, endOfDay: Long): Flow<List<CorridaEntity>>

    @Query("SELECT * FROM corridas WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    suspend fun getCorridasDoDiaSync(startOfDay: Long, endOfDay: Long): List<CorridaEntity>

    @Query("SELECT * FROM corridas WHERE classificacao = :classificacao ORDER BY timestamp DESC LIMIT 20")
    fun getCorridasPorClassificacao(classificacao: ClassificacaoCorrida): Flow<List<CorridaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrida(corrida: CorridaEntity): Long

    @Insert
    suspend fun insertAll(corridas: List<CorridaEntity>)

    @Query("SELECT SUM(valor) FROM corridas WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    suspend fun getFaturamentoTotal(startOfDay: Long, endOfDay: Long): Double?

    @Query("SELECT AVG(rPorKm) FROM corridas WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    suspend fun getMediaRPorKm(startOfDay: Long, endOfDay: Long): Double?

    @Query("SELECT AVG(rPorHora) FROM corridas WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    suspend fun getMediaRPorHora(startOfDay: Long, endOfDay: Long): Double?

    @Query("SELECT SUM(lucro) FROM corridas WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    suspend fun getLucroTotal(startOfDay: Long, endOfDay: Long): Double?

    @Query("SELECT plataforma, COUNT(*) as count, SUM(valor) as total FROM corridas WHERE timestamp >= :startOfDay AND timestamp < :endOfDay GROUP BY plataforma")
    suspend fun getFaturamentoPorPlataforma(startOfDay: Long, endOfDay: Long): List<PlataformaFaturamento>

    @Query("SELECT COUNT(*) FROM corridas WHERE latitudeInicio IS NOT NULL AND timestamp >= :startOfDay AND timestamp < :endOfDay")
    suspend fun countCorridasComLocalizacao(startOfDay: Long, endOfDay: Long): Int

    data class PlataformaFaturamento(
        val plataforma: Plataforma,
        val count: Int,
        val total: Double
    )
}
