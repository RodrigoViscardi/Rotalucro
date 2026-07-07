package com.lucronarota.data.model

enum class StatusJornada {
    PARADO, RODANDO, PAUSADO, FINALIZADO
}

enum class ClassificacaoCorrida {
    VERDE, AMARELO, VERMELHO
}

enum class TipoCusto {
    FIXO, VARIAVEL
}

enum class TipoCombustivel {
    GASOLINA, ETANOL, DIESEL, GNV, FLEX
}

enum class Plataforma {
    UBER, APP99, INDRIVE, IFOOD, OUTROS
}

data class ResumoJornada(
    val kmRodados: Double = 0.0,
    val tempoLiquidoMin: Long = 0,
    val tempoParadoMin: Long = 0,
    val faturamento: Double = 0.0,
    val custos: Double = 0.0,
    val lucroLiquido: Double = 0.0,
    val rPorKm: Double = 0.0,
    val rPorHora: Double = 0.0
)

data class ResultadoCalculo(
    val rPorKm: Double,
    val rPorHora: Double,
    val lucro: Double,
    val classificacao: ClassificacaoCorrida,
    val mensagem: String
)

data class EstatisticaRegiao(
    val nomeBairro: String,
    val corridas: Int,
    val rPorKmMedio: Double,
    val rPorHoraMedio: Double,
    val lucroTotal: Double,
    val latCenter: Double,
    val lngCenter: Double
)
