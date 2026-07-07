package com.lucronarota.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lucronarota.data.model.ClassificacaoCorrida
import com.lucronarota.data.model.Plataforma
import com.lucronarota.data.model.StatusJornada
import com.lucronarota.data.model.TipoCombustivel

@Entity(tableName = "jornadas")
data class JornadaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kmInicial: Double,
    val kmFinal: Double? = null,
    val horarioInicio: Long = System.currentTimeMillis(),
    val horarioFim: Long? = null,
    val tempoParadoTotalMin: Long = 0,
    val status: StatusJornada = StatusJornada.PARADO,
    val faturamentoUber: Double = 0.0,
    val faturamento99: Double = 0.0,
    val faturamentoIndrive: Double = 0.0,
    val faturamentoOutros: Double = 0.0,
    val observacao: String = ""
)

@Entity(tableName = "corridas")
data class CorridaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jornadaId: Long = 0,
    val plataforma: Plataforma,
    val valor: Double,
    val distanciaKm: Double,
    val tempoMin: Double,
    val rPorKm: Double,
    val rPorHora: Double,
    val lucro: Double,
    val classificacao: ClassificacaoCorrida,
    val latitudeInicio: Double? = null,
    val longitudeInicio: Double? = null,
    val latitudeFim: Double? = null,
    val longitudeFim: Double? = null,
    val printPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custos_fixos")
data class CustoFixoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val valorMensal: Double,
    val periodicidade: String = "mensal",
    val proximoVencimento: Long? = null,
    val ativo: Boolean = true
)

@Entity(tableName = "custos_variaveis")
data class CustoVariavelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val valor: Double,
    val kmNoMomento: Double? = null,
    val data: Long = System.currentTimeMillis(),
    val tipo: String = "combustivel"
)

@Entity(tableName = "abastecimentos")
data class AbastecimentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val litros: Double,
    val valor: Double,
    val kmAtual: Double,
    val tipoCombustivel: TipoCombustivel = TipoCombustivel.GASOLINA,
    val posto: String = "",
    val data: Long = System.currentTimeMillis()
)

@Entity(tableName = "metas")
data class MetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mes: Int,
    val ano: Int,
    val valorMetaLucro: Double = 0.0,
    val valorMetaFaturamento: Double = 0.0,
    val metaDiariaAutomatica: Double = 0.0
)

@Entity(tableName = "pausas_jornada")
data class PausaJornadaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jornadaId: Long,
    val horarioInicio: Long,
    val horarioFim: Long? = null,
    val motivo: String = ""
)
