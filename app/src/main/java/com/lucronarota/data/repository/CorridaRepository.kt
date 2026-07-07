package com.lucronarota.data.repository

import com.lucronarota.data.local.dao.CorridaDao
import com.lucronarota.data.local.entity.CorridaEntity
import com.lucronarota.data.model.ClassificacaoCorrida
import com.lucronarota.data.model.Plataforma
import com.lucronarota.data.model.ResultadoCalculo
import kotlinx.coroutines.flow.Flow
import java.util.*

class CorridaRepository(private val corridaDao: CorridaDao) {

    fun getAllCorridas(): Flow<List<CorridaEntity>> = corridaDao.getAllCorridas()

    fun getCorridasDoDia(): Flow<List<CorridaEntity>> {
        val hoje = Calendar.getInstance()
        hoje.set(Calendar.HOUR_OF_DAY, 0)
        hoje.set(Calendar.MINUTE, 0)
        hoje.set(Calendar.SECOND, 0)
        hoje.set(Calendar.MILLISECOND, 0)
        val startOfDay = hoje.timeInMillis
        hoje.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = hoje.timeInMillis
        return corridaDao.getCorridasDoDia(startOfDay, endOfDay)
    }

    fun getCorridasDoDiaSync(startOfDay: Long, endOfDay: Long): List<CorridaEntity> {
        return runBlockingSafe { corridaDao.getCorridasDoDiaSync(startOfDay, endOfDay) }
    }

    suspend fun salvarCorrida(corrida: CorridaEntity): Long = corridaDao.insertCorrida(corrida)

    suspend fun getFaturamentoHoje(): Double {
        val hoje = Calendar.getInstance()
        hoje.set(Calendar.HOUR_OF_DAY, 0)
        hoje.set(Calendar.MINUTE, 0)
        hoje.set(Calendar.SECOND, 0)
        hoje.set(Calendar.MILLISECOND, 0)
        val startOfDay = hoje.timeInMillis
        hoje.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = hoje.timeInMillis
        return corridaDao.getFaturamentoTotal(startOfDay, endOfDay) ?: 0.0
    }

    suspend fun getResumoDia(
        startOfDay: Long,
        endOfDay: Long
    ): Triple<Double, Double, Double> {
        val faturamento = corridaDao.getFaturamentoTotal(startOfDay, endOfDay) ?: 0.0
        val rPorKm = corridaDao.getMediaRPorKm(startOfDay, endOfDay) ?: 0.0
        val lucro = corridaDao.getLucroTotal(startOfDay, endOfDay) ?: 0.0
        return Triple(faturamento, rPorKm, lucro)
    }

    fun calcularCorrida(
        valor: Double,
        distanciaKm: Double,
        tempoMin: Double,
        custoPorKm: Double,
        metaPorKm: Double,
        metaPorHora: Double
    ): ResultadoCalculo {
        val rPorKm = if (distanciaKm > 0) valor / distanciaKm else 0.0
        val rPorHora = if (tempoMin > 0) valor / (tempoMin / 60.0) else 0.0
        val custoTotal = distanciaKm * custoPorKm
        val lucro = valor - custoTotal
        val percentualLucro = if (custoTotal > 0) (lucro / custoTotal) * 100 else 0.0

        val classificacao = when {
            rPorKm >= metaPorKm && rPorHora >= metaPorHora -> ClassificacaoCorrida.VERDE
            rPorKm >= custoPorKm -> ClassificacaoCorrida.AMARELO
            else -> ClassificacaoCorrida.VERMELHO
        }

        val mensagem = when (classificacao) {
            ClassificacaoCorrida.VERDE -> "Corrida excelente! R$${"%.2f".format(rPorKm)}/km"
            ClassificacaoCorrida.AMARELO -> "Atencao! R$${"%.2f".format(rPorKm)}/km - Lucro de R$${"%.2f".format(lucro)}"
            ClassificacaoCorrida.VERMELHO -> "Evitar! R$${"%.2f".format(rPorKm)}/km - Abaixo do custo"
        }

        return ResultadoCalculo(
            rPorKm = rPorKm,
            rPorHora = rPorHora,
            lucro = lucro,
            classificacao = classificacao,
            mensagem = mensagem
        )
    }

    private fun <T> runBlockingSafe(block: suspend () -> T): T {
        return kotlinx.coroutines.runBlocking { block() }
    }
}

internal fun <T> runBlockingSafe(block: suspend () -> T): T {
    return kotlinx.coroutines.runBlocking { block() }
}
