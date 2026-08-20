package com.example.oficina.controle.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    fun observarTodos(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun buscarPorId(id: Long): Cliente?

    @Query("SELECT * FROM clientes WHERE firebaseUid = :uid LIMIT 1")
    suspend fun buscarPorFirebaseUid(uid: String): Cliente?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(cliente: Cliente): Long

    @Update
    suspend fun atualizar(cliente: Cliente)

    @Delete
    suspend fun deletar(cliente: Cliente)
}

@Dao
interface VeiculoDao {
    @Query("SELECT * FROM veiculos ORDER BY placa ASC")
    fun observarTodos(): Flow<List<Veiculo>>

    @Query("SELECT * FROM veiculos WHERE clienteId = :clienteId")
    fun observarPorCliente(clienteId: Long): Flow<List<Veiculo>>

    @Query("SELECT * FROM veiculos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Veiculo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(veiculo: Veiculo): Long

    @Update
    suspend fun atualizar(veiculo: Veiculo)

    @Delete
    suspend fun deletar(veiculo: Veiculo)
}

@Dao
interface AgendamentoDao {
    @Query("SELECT * FROM agendamentos ORDER BY dataHoraEpochMillis ASC")
    fun observarTodos(): Flow<List<Agendamento>>

    @Query("SELECT * FROM agendamentos WHERE veiculoId = :veiculoId ORDER BY dataHoraEpochMillis DESC")
    fun observarPorVeiculo(veiculoId: Long): Flow<List<Agendamento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(agendamento: Agendamento): Long

    @Update
    suspend fun atualizar(agendamento: Agendamento)

    @Delete
    suspend fun deletar(agendamento: Agendamento)
}

@Dao
interface OrdemServicoDao {
    @Query("SELECT * FROM ordens_servico ORDER BY dataAberturaEpochMillis DESC")
    fun observarTodas(): Flow<List<OrdemServico>>

    @Query("SELECT * FROM ordens_servico WHERE veiculoId = :veiculoId ORDER BY dataAberturaEpochMillis DESC")
    fun observarPorVeiculo(veiculoId: Long): Flow<List<OrdemServico>>

    @Query("SELECT * FROM ordens_servico WHERE status != 'CONCLUIDO' ORDER BY dataAberturaEpochMillis ASC")
    fun observarAtivas(): Flow<List<OrdemServico>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(ordem: OrdemServico): Long

    @Update
    suspend fun atualizar(ordem: OrdemServico)

    @Delete
    suspend fun deletar(ordem: OrdemServico)
}
