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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.oficina.controle.data.Agendamento
import com.example.oficina.controle.data.StatusAgendamento
import com.example.oficina.controle.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(vm: AppViewModel) {
    val agendamentos by vm.agendamentos.collectAsState()
    val veiculos by vm.veiculos.collectAsState()
    var mostrarDialogoNovo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agenda de Revisões") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoNovo = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Novo agendamento")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(agendamentos.sortedBy { it.dataHoraEpochMillis }) { ag ->
                val veiculo = veiculos.find { it.id == ag.veiculoId }
                AgendamentoCard(
                    agendamento = ag,
                    nomeVeiculo = "${veiculo?.marca ?: ""} ${veiculo?.modelo ?: ""} - ${veiculo?.placa ?: "?"}",
                    onConfirmar = { vm.abrirOrdemServicoDeAgendamento(ag) },
                    onCancelar = { vm.atualizarStatusAgendamento(ag, StatusAgendamento.CANCELADO) }
                )
            }
        }
    }

    if (mostrarDialogoNovo) {
        NovoAgendamentoDialog(
            veiculos = veiculos,
            aoFechar = { mostrarDialogoNovo = false },
            aoSalvar = { veiculoId, dataMillis, tipo, obs ->
                vm.criarAgendamento(veiculoId, dataMillis, tipo, obs)
                mostrarDialogoNovo = false
            }
        )
    }
}

@Composable
fun AgendamentoCard(
    agendamento: Agendamento,
    nomeVeiculo: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val formato = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(nomeVeiculo, fontWeight = FontWeight.Bold)
            Text(agendamento.tipoServico)
            Text(formato.format(Date(agendamento.dataHoraEpochMillis)), style = MaterialTheme.typography.bodySmall)
            if (agendamento.observacoes.isNotBlank()) {
                Text("Obs: ${agendamento.observacoes}", style = MaterialTheme.typography.bodySmall)
            }
            Text("Status: ${agendamento.status.name}", style = MaterialTheme.typography.bodySmall)

            if (agendamento.status == StatusAgendamento.AGENDADO) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onConfirmar) { Text("Confirmar e abrir OS") }
                    TextButton(onClick = onCancelar) { Text("Cancelar") }
                }
            }
        }
    }
}

@Composable
fun NovoAgendamentoDialog(
    veiculos: List<com.example.oficina.controle.data.Veiculo>,
    aoFechar: () -> Unit,
    aoSalvar: (veiculoId: Long, dataMillis: Long, tipo: String, obs: String) -> Unit
) {
    var veiculoSelecionado by remember { mutableStateOf(veiculos.firstOrNull()) }
    var expandido by remember { mutableStateOf(false) }
    var tipoServico by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var dias by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Novo Agendamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                    OutlinedTextField(
                        value = veiculoSelecionado?.let { "${it.marca} ${it.modelo} - ${it.placa}" } ?: "Nenhum veículo cadastrado",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Veículo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                        veiculos.forEach { v ->
                            DropdownMenuItem(
                                text = { Text("${v.marca} ${v.modelo} - ${v.placa}") },
                                onClick = { veiculoSelecionado = v; expandido = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = tipoServico,
                    onValueChange = { tipoServico = it },
                    label = { Text("Tipo de serviço (ex: Revisão 10.000km)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dias,
                    onValueChange = { dias = it.filter { c -> c.isDigit() } },
                    label = { Text("Dias a partir de hoje") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val v = veiculoSelecionado ?: return@Button
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, dias.toIntOrNull() ?: 0)
                aoSalvar(v.id, cal.timeInMillis, tipoServico.ifBlank { "Revisão geral" }, observacoes)
            }) { Text("Agendar") }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) { Text("Cancelar") }
        }
    )
}
