package com.lucronarota.service.calculo

import com.lucronarota.data.model.ClassificacaoCorrida
import com.lucronarota.data.model.ResultadoCalculo

class CalculoEngine {

    fun calcular(
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

        val classificacao = when {
            rPorKm >= metaPorKm && rPorHora >= metaPorHora -> ClassificacaoCorrida.VERDE
            rPorKm >= custoPorKm -> ClassificacaoCorrida.AMARELO
            else -> ClassificacaoCorrida.VERMELHO
        }

        val mensagem = when (classificacao) {
            ClassificacaoCorrida.VERDE ->
                "VERDE: R$${"%.2f".format(rPorKm)}/km | R$${"%.2f".format(rPorHora)}/h | Lucro: R$${"%.2f".format(lucro)}"
            ClassificacaoCorrida.AMARELO ->
                "AMARELO: R$${"%.2f".format(rPorKm)}/km | R$${"%.2f".format(rPorHora)}/h | Lucro: R$${"%.2f".format(lucro)}"
            ClassificacaoCorrida.VERMELHO ->
                "VERMELHO: R$${"%.2f".format(rPorKm)}/km | R$${"%.2f".format(rPorHora)}/h | Prejuizo: R$${"%.2f".format(lucro)}"
        }

        return ResultadoCalculo(
            rPorKm = rPorKm,
            rPorHora = rPorHora,
            lucro = lucro,
            classificacao = classificacao,
            mensagem = mensagem
        )
    }

    fun calcularCustoPorKm(
        custosFixosMensais: Double,
        custosVariaveisMes: Double,
        kmRodadosMes: Double
    ): Double {
        if (kmRodadosMes <= 0) return 0.0
        return (custosFixosMensais + custosVariaveisMes) / kmRodadosMes
    }
}
