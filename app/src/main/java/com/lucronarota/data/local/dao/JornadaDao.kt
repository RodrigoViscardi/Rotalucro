package com.lucronarota.data.local.dao

import androidx.room.*
import com.lucronarota.data.local.entity.JornadaEntity
import com.lucronarota.data.local.entity.PausaJornadaEntity
import com.lucronarota.data.model.StatusJornada
import kotlinx.coroutines.flow.Flow

@Dao
interface JornadaDao {

    @Query("SELECT * FROM jornadas ORDER BY horarioInicio DESC")
    fun getAllJornadas(): Flow<List<JornadaEntity>>

    @Query("SELECT * FROM jornadas WHERE id = :id")
    suspend fun getJornadaById(id: Long): JornadaEntity?

    @Query("SELECT * FROM jornadas WHERE status = :status ORDER BY horarioInicio DESC LIMIT 1")
    suspend fun getJornadaByStatus(status: StatusJornada): JornadaEntity?

    @Query("SELECT * FROM jornadas WHERE status = :status ORDER BY horarioInicio DESC LIMIT 1")
    fun getJornadaByStatusFlow(status: StatusJornada): Flow<JornadaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJornada(jornada: JornadaEntity): Long

    @Update
    suspend fun updateJornada(jornada: JornadaEntity)

    @Delete
    suspend fun deleteJornada(jornada: JornadaEntity)

    @Query("SELECT * FROM jornadas WHERE horarioInicio >= :startOfDay AND horarioInicio < :endOfDay")
    suspend fun getJornadasDoDia(startOfDay: Long, endOfDay: Long): List<JornadaEntity>

    @Query("SELECT SUM(kmFinal - kmInicial) FROM jornadas WHERE status = 'FINALIZADO' AND horarioInicio >= :startOfMonth AND horarioInicio < :endOfMonth")
    suspend fun getKmTotalNoMes(startOfMonth: Long, endOfMonth: Long): Double?

    // Pausas
    @Insert
    suspend fun insertPausa(pausa: PausaJornadaEntity): Long

    @Update
    suspend fun updatePausa(pausa: PausaJornadaEntity)

    @Query("SELECT * FROM pausas_jornada WHERE jornadaId = :jornadaId AND horarioFim IS NULL LIMIT 1")
    suspend fun getPausaAtiva(jornadaId: Long): PausaJornadaEntity?

    @Query("SELECT SUM(COALESCE(horarioFim, 0) - horarioInicio) FROM pausas_jornada WHERE jornadaId = :jornadaId AND horarioFim IS NOT NULL")
    suspend fun getTempoParadoTotal(jornadaId: Long): Long?

    @Query("SELECT * FROM pausas_jornada WHERE jornadaId = :jornadaId ORDER BY horarioInicio")
    suspend fun getPausasDaJornada(jornadaId: Long): List<PausaJornadaEntity>
}
