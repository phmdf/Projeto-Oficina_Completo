package com.example.oficina.controle.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.oficina.controle.data.OrdemServico
import com.example.oficina.controle.data.StatusOrdemServico
import com.example.oficina.controle.viewmodel.AppViewModel

@Composable
fun AcompanharServicoScreen(vm: AppViewModel, veiculoId: Long) {
    val ordens by vm.buscarOrdensDoVeiculo(veiculoId).collectAsState(initial = emptyList())
    val ativas = ordens.filter { it.status != StatusOrdemServico.CONCLUIDO }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Acompanhar Serviço") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (ativas.isEmpty()) {
                Text("Nenhum serviço em andamento no momento para este veículo.")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(ativas) { ordem ->
                    AcompanhamentoCard(ordem)
                }
            }
        }
    }
}

@Composable
fun AcompanhamentoCard(ordem: OrdemServico) {
    val progresso = when (ordem.status) {
        StatusOrdemServico.AGUARDANDO -> 0.15f
        StatusOrdemServico.AGUARDANDO_PECA -> 0.5f
        StatusOrdemServico.EM_ANDAMENTO -> 0.75f
        StatusOrdemServico.CONCLUIDO -> 1f
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(ordem.descricaoServico, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            if (ordem.mecanicoResponsavel.isNotBlank()) {
                Text("Mecânico responsável: ${ordem.mecanicoResponsavel}", style = MaterialTheme.typography.bodySmall)
            }

            LinearProgressIndicator(
                progress = { progresso },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            Text(
                textoStatusCliente(ordem.status),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun textoStatusCliente(status: StatusOrdemServico): String = when (status) {
    StatusOrdemServico.AGUARDANDO -> "Seu veículo está na fila para atendimento"
    StatusOrdemServico.AGUARDANDO_PECA -> "Aguardando chegada de peça"
    StatusOrdemServico.EM_ANDAMENTO -> "Nossos mecânicos estão trabalhando no seu carro"
    StatusOrdemServico.CONCLUIDO -> "Serviço concluído! Seu carro está pronto"
}
