package com.example.oficina.controle.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Card
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
import com.example.oficina.controle.data.StatusAgendamento
import com.example.oficina.controle.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeusAgendamentosScreen(vm: AppViewModel, veiculoId: Long, aoSair: () -> Unit = {}) {
    val agendamentos by vm.buscarAgendamentosDoVeiculo(veiculoId).collectAsState(initial = emptyList())
    val formato = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Agendamentos") },
                actions = {
                    IconButton(onClick = aoSair) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (agendamentos.isEmpty()) {
                Text("Você ainda não tem agendamentos. Toque em \"Agendar Revisão\" para solicitar um horário.")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(agendamentos.sortedByDescending { it.dataHoraEpochMillis }) { ag ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(ag.tipoServico, fontWeight = FontWeight.Bold)
                            Text(formato.format(Date(ag.dataHoraEpochMillis)), style = MaterialTheme.typography.bodySmall)
                            if (ag.observacoes.isNotBlank()) {
                                Text("Obs: ${ag.observacoes}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                "Status: ${statusAgendamentoLegivel(ag.status)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

fun statusAgendamentoLegivel(status: StatusAgendamento): String = when (status) {
    StatusAgendamento.AGENDADO -> "Aguardando confirmação da oficina"
    StatusAgendamento.CONFIRMADO -> "Confirmado"
    StatusAgendamento.CANCELADO -> "Cancelado"
    StatusAgendamento.CONCLUIDO -> "Concluído"
}
