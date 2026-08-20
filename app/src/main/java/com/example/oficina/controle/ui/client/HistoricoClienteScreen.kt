package com.example.oficina.controle.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoricoClienteScreen(vm: AppViewModel, veiculoId: Long) {
    val ordens by vm.buscarOrdensDoVeiculo(veiculoId).collectAsState(initial = emptyList())
    val concluidas = ordens.filter { it.status == StatusOrdemServico.CONCLUIDO }
        .sortedByDescending { it.dataConclusaoEpochMillis }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Histórico de Manutenção") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (concluidas.isEmpty()) {
                Text("Nenhum serviço concluído registrado ainda para este veículo.")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(concluidas) { ordem -> HistoricoClienteCard(ordem) }
            }
        }
    }
}

@Composable
private fun HistoricoClienteCard(ordem: OrdemServico) {
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(ordem.descricaoServico, fontWeight = FontWeight.Bold)
            ordem.dataConclusaoEpochMillis?.let {
                Text("Concluído em ${formato.format(Date(it))}", style = MaterialTheme.typography.bodySmall)
            }
            Text("Quilometragem: ${ordem.quilometragem} km", style = MaterialTheme.typography.bodySmall)
            if (ordem.valor > 0) {
                Text("Valor: R$ %.2f".format(ordem.valor), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
