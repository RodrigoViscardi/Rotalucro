package com.lucronarota.data.repository

import com.lucronarota.data.local.dao.MetaDao
import com.lucronarota.data.local.entity.MetaEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

class MetaRepository(private val metaDao: MetaDao) {

    fun getAll(): Flow<List<MetaEntity>> = metaDao.getAll()

    suspend fun getMetaAtual(): MetaEntity? {
        val cal = Calendar.getInstance()
        val mes = cal.get(Calendar.MONTH) + 1
        val ano = cal.get(Calendar.YEAR)
        return metaDao.getMeta(mes, ano)
    }

    fun getMetaAtualFlow(): Flow<MetaEntity?> {
        val cal = Calendar.getInstance()
        val mes = cal.get(Calendar.MONTH) + 1
        val ano = cal.get(Calendar.YEAR)
        return metaDao.getMetaFlow(mes, ano)
    }

    suspend fun salvarMeta(meta: MetaEntity): Long = metaDao.insert(meta)

    suspend fun atualizarMeta(meta: MetaEntity) = metaDao.update(meta)

    suspend fun calcularMetaDiaria(lucroMensalDesejado: Double, diasUteis: Int = 22): Double {
        return if (diasUteis > 0) lucroMensalDesejado / diasUteis else 0.0
    }
}
