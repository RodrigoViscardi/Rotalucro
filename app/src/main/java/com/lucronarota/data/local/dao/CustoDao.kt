package com.lucronarota.data.local.dao

import androidx.room.*
import com.lucronarota.data.local.entity.CustoFixoEntity
import com.lucronarota.data.local.entity.CustoVariavelEntity
import com.lucronarota.data.local.entity.AbastecimentoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustoFixoDao {

    @Query("SELECT * FROM custos_fixos WHERE ativo = 1 ORDER BY nome")
    fun getAllAtivos(): Flow<List<CustoFixoEntity>>

    @Query("SELECT * FROM custos_fixos ORDER BY nome")
    fun getAll(): Flow<List<CustoFixoEntity>>

    @Query("SELECT SUM(valorMensal) FROM custos_fixos WHERE ativo = 1")
    suspend fun getTotalCustosFixosMensais(): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(custo: CustoFixoEntity): Long

    @Update
    suspend fun update(custo: CustoFixoEntity)

    @Delete
    suspend fun delete(custo: CustoFixoEntity)
}

@Dao
interface CustoVariavelDao {

    @Query("SELECT * FROM custos_variaveis ORDER BY data DESC")
    fun getAll(): Flow<List<CustoVariavelEntity>>

    @Query("SELECT * FROM custos_variaveis WHERE data >= :startOfMonth AND data < :endOfMonth")
    suspend fun getCustosDoMes(startOfMonth: Long, endOfMonth: Long): List<CustoVariavelEntity>

    @Query("SELECT SUM(valor) FROM custos_variaveis WHERE data >= :startOfMonth AND data < :endOfMonth")
    suspend fun getTotalCustosVariaveis(startOfMonth: Long, endOfMonth: Long): Double?

    @Query("SELECT * FROM custos_variaveis WHERE data >= :startOfDay AND data < :endOfDay")
    suspend fun getCustosDoDia(startOfDay: Long, endOfDay: Long): List<CustoVariavelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(custo: CustoVariavelEntity): Long

    @Delete
    suspend fun delete(custo: CustoVariavelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbastecimento(abastecimento: AbastecimentoEntity): Long
}

@Dao
interface AbastecimentoDao {

    @Query("SELECT * FROM abastecimentos ORDER BY data DESC")
    fun getAll(): Flow<List<AbastecimentoEntity>>

    @Query("SELECT * FROM abastecimentos ORDER BY data DESC LIMIT 2")
    suspend fun getUltimosDois(): List<AbastecimentoEntity>

    @Query("SELECT SUM(valor) FROM abastecimentos WHERE data >= :startOfMonth AND data < :endOfMonth")
    suspend fun getTotalAbastecimentosNoMes(startOfMonth: Long, endOfMonth: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(abastecimento: AbastecimentoEntity): Long

    @Delete
    suspend fun delete(abastecimento: AbastecimentoEntity)
}
