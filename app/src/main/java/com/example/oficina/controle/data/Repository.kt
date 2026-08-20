package com.example.oficina.controle.data

import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {

    // Clientes
    fun observarClientes(): Flow<List<Cliente>> = db.clienteDao().observarTodos()
    suspend fun salvarCliente(cliente: Cliente): Long = db.clienteDao().inserir(cliente)
    suspend fun atualizarCliente(cliente: Cliente) = db.clienteDao().atualizar(cliente)
    suspend fun deletarCliente(cliente: Cliente) = db.clienteDao().deletar(cliente)
    suspend fun buscarCliente(id: Long): Cliente? = db.clienteDao().buscarPorId(id)
    suspend fun buscarClientePorFirebaseUid(uid: String): Cliente? = db.clienteDao().buscarPorFirebaseUid(uid)

    // Veiculos
    fun observarVeiculos(): Flow<List<Veiculo>> = db.veiculoDao().observarTodos()
    fun observarVeiculosPorCliente(clienteId: Long): Flow<List<Veiculo>> =
        db.veiculoDao().observarPorCliente(clienteId)
    suspend fun salvarVeiculo(veiculo: Veiculo): Long = db.veiculoDao().inserir(veiculo)
    suspend fun atualizarVeiculo(veiculo: Veiculo) = db.veiculoDao().atualizar(veiculo)
    suspend fun deletarVeiculo(veiculo: Veiculo) = db.veiculoDao().deletar(veiculo)
    suspend fun buscarVeiculo(id: Long): Veiculo? = db.veiculoDao().buscarPorId(id)

    // Agendamentos
    fun observarAgendamentos(): Flow<List<Agendamento>> = db.agendamentoDao().observarTodos()
    fun observarAgendamentosPorVeiculo(veiculoId: Long): Flow<List<Agendamento>> =
        db.agendamentoDao().observarPorVeiculo(veiculoId)
    suspend fun salvarAgendamento(agendamento: Agendamento): Long =
        db.agendamentoDao().inserir(agendamento)
    suspend fun atualizarAgendamento(agendamento: Agendamento) =
        db.agendamentoDao().atualizar(agendamento)
    suspend fun deletarAgendamento(agendamento: Agendamento) =
        db.agendamentoDao().deletar(agendamento)

    // Ordens de servico
    fun observarOrdens(): Flow<List<OrdemServico>> = db.ordemServicoDao().observarTodas()
    fun observarOrdensAtivas(): Flow<List<OrdemServico>> = db.ordemServicoDao().observarAtivas()
    fun observarOrdensPorVeiculo(veiculoId: Long): Flow<List<OrdemServico>> =
        db.ordemServicoDao().observarPorVeiculo(veiculoId)
    suspend fun salvarOrdem(ordem: OrdemServico): Long = db.ordemServicoDao().inserir(ordem)
    suspend fun atualizarOrdem(ordem: OrdemServico) = db.ordemServicoDao().atualizar(ordem)
    suspend fun deletarOrdem(ordem: OrdemServico) = db.ordemServicoDao().deletar(ordem)
}
