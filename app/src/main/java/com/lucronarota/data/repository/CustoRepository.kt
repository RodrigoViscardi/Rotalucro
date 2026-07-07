package com.lucronarota.data.repository

import com.lucronarota.data.local.dao.AbastecimentoDao
import com.lucronarota.data.local.dao.CustoFixoDao
import com.lucronarota.data.local.dao.CustoVariavelDao
import com.lucronarota.data.local.entity.AbastecimentoEntity
import com.lucronarota.data.local.entity.CustoFixoEntity
import com.lucronarota.data.local.entity.CustoVariavelEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

class CustoRepository(
    private val custoFixoDao: CustoFixoDao,
    private val custoVariavelDao: CustoVariavelDao,
    private val abastecimentoDao: AbastecimentoDao
) {

    // Custos Fixos
    fun getCustosFixos(): Flow<List<CustoFixoEntity>> = custoFixoDao.getAll()

    suspend fun getTotalCustosFixosMensais(): Double =
        custoFixoDao.getTotalCustosFixosMensais() ?: 0.0

    suspend fun salvarCustoFixo(custo: CustoFixoEntity): Long = custoFixoDao.insert(custo)

    suspend fun atualizarCustoFixo(custo: CustoFixoEntity) = custoFixoDao.update(custo)

    suspend fun deletarCustoFixo(custo: CustoFixoEntity) = custoFixoDao.delete(custo)

    // Custos Variáveis
    fun getCustosVariaveis(): Flow<List<CustoVariavelEntity>> = custoVariavelDao.getAll()

    suspend fun getCustosVariaveisDoMes(mes: Int, ano: Int): Double {
        val cal = Calendar.getInstance()
        cal.set(ano, mes - 1, 1, 0, 0, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return custoVariavelDao.getTotalCustosVariaveis(start, end) ?: 0.0
    }

    suspend fun salvarCustoVariavel(custo: CustoVariavelEntity): Long =
        custoVariavelDao.insert(custo)

    suspend fun deletarCustoVariavel(custo: CustoVariavelEntity) =
        custoVariavelDao.delete(custo)

    // Abastecimentos
    fun getAbastecimentos(): Flow<List<AbastecimentoEntity>> = abastecimentoDao.getAll()

    suspend fun getMediaKmL(): Double {
        val ultimos = abastecimentoDao.getUltimosDois()
        if (ultimos.size < 2) return 0.0
        val kmDiferenca = ultimos[0].kmAtual - ultimos[1].kmAtual
        val litros = ultimos[0].litros
        return if (litros > 0 && kmDiferenca > 0) kmDiferenca / litros else 0.0
    }

    suspend fun salvarAbastecimento(abastecimento: AbastecimentoEntity): Long =
        abastecimentoDao.insert(abastecimento)

    suspend fun deletarAbastecimento(abastecimento: AbastecimentoEntity) =
        abastecimentoDao.delete(abastecimento)

    // Cálculo do Custo por KM
    suspend fun calcularCustoPorKm(mes: Int, ano: Int, kmRodados: Double): Double {
        if (kmRodados <= 0) return 0.0
        val custosFixos = getTotalCustosFixosMensais()
        val custosVariaveis = getCustosVariaveisDoMes(mes, ano)
        val combustivel = calcularCustoCombustivel(mes, ano)
        val total = custosFixos + custosVariaveis + combustivel
        return total / kmRodados
    }

    private suspend fun calcularCustoCombustivel(mes: Int, ano: Int): Double {
        val cal = Calendar.getInstance()
        cal.set(ano, mes - 1, 1, 0, 0, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return abastecimentoDao.getTotalAbastecimentosNoMes(start, end) ?: 0.0
    }
}
