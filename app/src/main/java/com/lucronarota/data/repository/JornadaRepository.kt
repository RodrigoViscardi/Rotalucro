package com.lucronarota.data.repository

import com.lucronarota.data.local.dao.JornadaDao
import com.lucronarota.data.local.entity.JornadaEntity
import com.lucronarota.data.local.entity.PausaJornadaEntity
import com.lucronarota.data.model.ResumoJornada
import com.lucronarota.data.model.StatusJornada
import kotlinx.coroutines.flow.Flow
import java.util.*

class JornadaRepository(private val jornadaDao: JornadaDao) {

    fun getAllJornadas(): Flow<List<JornadaEntity>> = jornadaDao.getAllJornadas()

    suspend fun getJornadaById(id: Long): JornadaEntity? = jornadaDao.getJornadaById(id)

    suspend fun getJornadaAtiva(): JornadaEntity? =
        jornadaDao.getJornadaByStatus(StatusJornada.RODANDO)

    fun getJornadaAtivaFlow(): Flow<JornadaEntity?> =
        jornadaDao.getJornadaByStatusFlow(StatusJornada.RODANDO)

    suspend fun iniciarJornada(kmInicial: Double): Long {
        val jornada = JornadaEntity(
            kmInicial = kmInicial,
            horarioInicio = System.currentTimeMillis(),
            status = StatusJornada.RODANDO
        )
        return jornadaDao.insertJornada(jornada)
    }

    suspend fun pausarJornada(jornadaId: Long) {
        val pausa = PausaJornadaEntity(
            jornadaId = jornadaId,
            horarioInicio = System.currentTimeMillis()
        )
        jornadaDao.insertPausa(pausa)
        val jornada = jornadaDao.getJornadaById(jornadaId) ?: return
        jornadaDao.updateJornada(jornada.copy(status = StatusJornada.PAUSADO))
    }

    suspend fun retomarJornada(jornadaId: Long) {
        val pausaAtiva = jornadaDao.getPausaAtiva(jornadaId)
        if (pausaAtiva != null) {
            jornadaDao.updatePausa(pausaAtiva.copy(horarioFim = System.currentTimeMillis()))
        }
        val jornada = jornadaDao.getJornadaById(jornadaId) ?: return
        val tempoParado = calcularTempoParadoTotal(jornadaId)
        jornadaDao.updateJornada(
            jornada.copy(
                status = StatusJornada.RODANDO,
                tempoParadoTotalMin = tempoParado
            )
        )
    }

    suspend fun finalizarJornada(
        jornadaId: Long,
        kmFinal: Double,
        faturamentoUber: Double = 0.0,
        faturamento99: Double = 0.0,
        faturamentoIndrive: Double = 0.0,
        faturamentoOutros: Double = 0.0,
        observacao: String = ""
    ) {
        val jornada = jornadaDao.getJornadaById(jornadaId) ?: return
        val tempoParado = calcularTempoParadoTotal(jornadaId)

        jornadaDao.updateJornada(
            jornada.copy(
                kmFinal = kmFinal,
                horarioFim = System.currentTimeMillis(),
                status = StatusJornada.FINALIZADO,
                tempoParadoTotalMin = tempoParado,
                faturamentoUber = faturamentoUber,
                faturamento99 = faturamento99,
                faturamentoIndrive = faturamentoIndrive,
                faturamentoOutros = faturamentoOutros,
                observacao = observacao
            )
        )
    }

    suspend fun getResumoJornada(jornadaId: Long): ResumoJornada {
        val jornada = jornadaDao.getJornadaById(jornadaId) ?: return ResumoJornada()
        val kmFinal = jornada.kmFinal ?: return ResumoJornada()
        val horaFim = jornada.horarioFim ?: System.currentTimeMillis()

        val kmRodados = kmFinal - jornada.kmInicial
        val tempoTotalMin = (horaFim - jornada.horarioInicio) / 60000
        val tempoLiquidoMin = tempoTotalMin - jornada.tempoParadoTotalMin
        val faturamento = jornada.faturamentoUber + jornada.faturamento99 +
                jornada.faturamentoIndrive + jornada.faturamentoOutros
        val rPorKm = if (kmRodados > 0) faturamento / kmRodados else 0.0
        val rPorHora = if (tempoLiquidoMin > 0) faturamento / (tempoLiquidoMin / 60.0) else 0.0

        return ResumoJornada(
            kmRodados = kmRodados,
            tempoLiquidoMin = tempoLiquidoMin,
            tempoParadoMin = jornada.tempoParadoTotalMin,
            faturamento = faturamento,
            rPorKm = rPorKm,
            rPorHora = rPorHora
        )
    }

    private suspend fun calcularTempoParadoTotal(jornadaId: Long): Long {
        val totalParado = jornadaDao.getTempoParadoTotal(jornadaId) ?: 0L
        return totalParado / 60000 // converte ms para minutos
    }

    suspend fun getKmTotalNoMes(mes: Int, ano: Int): Double {
        val cal = Calendar.getInstance()
        cal.set(ano, mes - 1, 1, 0, 0, 0)
        val startOfMonth = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val endOfMonth = cal.timeInMillis
        return jornadaDao.getKmTotalNoMes(startOfMonth, endOfMonth) ?: 0.0
    }
}
