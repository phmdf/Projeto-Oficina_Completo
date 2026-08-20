package com.example.oficina.controle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oficina.controle.data.Agendamento
import com.example.oficina.controle.data.Cliente
import com.example.oficina.controle.data.OrdemServico
import com.example.oficina.controle.data.Repository
import com.example.oficina.controle.data.StatusAgendamento
import com.example.oficina.controle.data.StatusOrdemServico
import com.example.oficina.controle.data.Veiculo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(private val repo: Repository) : ViewModel() {

    /** Exposto para permitir que o AuthViewModel compartilhe o mesmo Repository/banco local. */
    val repositorioPublico: Repository get() = repo

    val clientes: StateFlow<List<Cliente>> = repo.observarClientes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val veiculos: StateFlow<List<Veiculo>> = repo.observarVeiculos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val agendamentos: StateFlow<List<Agendamento>> = repo.observarAgendamentos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ordensAtivas: StateFlow<List<OrdemServico>> = repo.observarOrdensAtivas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ordensTodas: StateFlow<List<OrdemServico>> = repo.observarOrdens()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun salvarCliente(nome: String, telefone: String, email: String, aoConcluir: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.salvarCliente(Cliente(nome = nome, telefone = telefone, email = email))
            aoConcluir(id)
        }
    }

    fun deletarCliente(cliente: Cliente) {
        viewModelScope.launch { repo.deletarCliente(cliente) }
    }

    fun salvarVeiculo(
        clienteId: Long,
        placa: String,
        marca: String,
        modelo: String,
        ano: Int,
        km: Int,
        aoConcluir: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = repo.salvarVeiculo(
                Veiculo(
                    clienteId = clienteId,
                    placa = placa,
                    marca = marca,
                    modelo = modelo,
                    ano = ano,
                    quilometragemAtual = km
                )
            )
            aoConcluir(id)
        }
    }

    fun deletarVeiculo(veiculo: Veiculo) {
        viewModelScope.launch { repo.deletarVeiculo(veiculo) }
    }

    fun criarAgendamento(
        veiculoId: Long,
        dataHoraEpochMillis: Long,
        tipoServico: String,
        observacoes: String
    ) {
        viewModelScope.launch {
            repo.salvarAgendamento(
                Agendamento(
                    veiculoId = veiculoId,
                    dataHoraEpochMillis = dataHoraEpochMillis,
                    tipoServico = tipoServico,
                    observacoes = observacoes,
                    status = StatusAgendamento.AGENDADO
                )
            )
        }
    }

    fun atualizarStatusAgendamento(agendamento: Agendamento, status: StatusAgendamento) {
        viewModelScope.launch {
            repo.atualizarAgendamento(agendamento.copy(status = status))
        }
    }

    /** Ao confirmar um agendamento, a oficina abre a ordem de serviço correspondente. */
    fun abrirOrdemServicoDeAgendamento(agendamento: Agendamento) {
        viewModelScope.launch {
            repo.atualizarAgendamento(agendamento.copy(status = StatusAgendamento.CONFIRMADO))
            repo.salvarOrdem(
                OrdemServico(
                    veiculoId = agendamento.veiculoId,
                    agendamentoId = agendamento.id,
                    dataAberturaEpochMillis = System.currentTimeMillis(),
                    descricaoServico = agendamento.tipoServico,
                    status = StatusOrdemServico.AGUARDANDO
                )
            )
        }
    }

    fun criarOrdemServicoAvulsa(
        veiculoId: Long,
        descricao: String,
        mecanico: String,
        km: Int
    ) {
        viewModelScope.launch {
            repo.salvarOrdem(
                OrdemServico(
                    veiculoId = veiculoId,
                    dataAberturaEpochMillis = System.currentTimeMillis(),
                    descricaoServico = descricao,
                    mecanicoResponsavel = mecanico,
                    quilometragem = km,
                    status = StatusOrdemServico.AGUARDANDO
                )
            )
        }
    }

    fun atualizarStatusOrdem(ordem: OrdemServico, status: StatusOrdemServico) {
        viewModelScope.launch {
            val concluida = status == StatusOrdemServico.CONCLUIDO
            repo.atualizarOrdem(
                ordem.copy(
                    status = status,
                    dataConclusaoEpochMillis = if (concluida) System.currentTimeMillis() else ordem.dataConclusaoEpochMillis
                )
            )
        }
    }

    fun atualizarValorOrdem(ordem: OrdemServico, valor: Double) {
        viewModelScope.launch {
            repo.atualizarOrdem(ordem.copy(valor = valor))
        }
    }

    fun buscarVeiculosDoCliente(clienteId: Long) = repo.observarVeiculosPorCliente(clienteId)
    fun buscarOrdensDoVeiculo(veiculoId: Long) = repo.observarOrdensPorVeiculo(veiculoId)
    fun buscarAgendamentosDoVeiculo(veiculoId: Long) = repo.observarAgendamentosPorVeiculo(veiculoId)
}
