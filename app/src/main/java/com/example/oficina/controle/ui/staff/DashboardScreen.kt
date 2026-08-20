package com.example.oficina.controle.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.oficina.controle.data.StatusOrdemServico
import com.example.oficina.controle.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: AppViewModel, aoSair: () -> Unit = {}) {
    val agendamentos by vm.agendamentos.collectAsState()
    val ordensAtivas by vm.ordensAtivas.collectAsState()
    val veiculos by vm.veiculos.collectAsState()

    val hoje = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
    val agendamentosHoje = agendamentos.filter {
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(it.dataHoraEpochMillis)) == hoje
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel da Oficina") },
                actions = {
                    IconButton(onClick = aoSair) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResumoCard(
                        titulo = "Agendamentos hoje",
                        valor = agendamentosHoje.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    ResumoCard(
                        titulo = "Ordens ativas",
                        valor = ordensAtivas.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Text(
                    "Ordens de serviço em andamento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (ordensAtivas.isEmpty()) {
                item { Text("Nenhuma ordem em aberto no momento.") }
            }
            items(ordensAtivas) { ordem ->
                val veiculo = veiculos.find { it.id == ordem.veiculoId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "${veiculo?.marca ?: ""} ${veiculo?.modelo ?: ""} - ${veiculo?.placa ?: "?"}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(ordem.descricaoServico)
                        Text(
                            "Status: ${statusLegivel(ordem.status)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResumoCard(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(titulo, style = MaterialTheme.typography.bodySmall)
        }
    }
}

fun statusLegivel(status: StatusOrdemServico): String = when (status) {
    StatusOrdemServico.AGUARDANDO -> "Aguardando"
    StatusOrdemServico.EM_ANDAMENTO -> "Em andamento"
    StatusOrdemServico.AGUARDANDO_PECA -> "Aguardando peça"
    StatusOrdemServico.CONCLUIDO -> "Concluído"
}
