package com.example.oficina.controle.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.oficina.controle.data.OrdemServico
import com.example.oficina.controle.data.StatusOrdemServico
import com.example.oficina.controle.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(vm: AppViewModel) {
    val veiculos by vm.veiculos.collectAsState()
    var veiculoSelecionado by remember { mutableStateOf(veiculos.firstOrNull()) }
    var expandido by remember { mutableStateOf(false) }

    LaunchedEffect(veiculos) {
        if (veiculoSelecionado == null) veiculoSelecionado = veiculos.firstOrNull()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Histórico de Manutenção") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                OutlinedTextField(
                    value = veiculoSelecionado?.let { "${it.marca} ${it.modelo} - ${it.placa}" } ?: "Nenhum veículo cadastrado",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Selecione o veículo") },
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

            val veiculoId = veiculoSelecionado?.id
            if (veiculoId != null) {
                val ordens by vm.buscarOrdensDoVeiculo(veiculoId).collectAsState(initial = emptyList())
                val concluidas = ordens.filter { it.status == StatusOrdemServico.CONCLUIDO }

                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (concluidas.isEmpty()) {
                        item { Text("Nenhum serviço concluído registrado para este veículo ainda.") }
                    }
                    items(concluidas.sortedByDescending { it.dataConclusaoEpochMillis }) { ordem ->
                        HistoricoCard(ordem)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoricoCard(ordem: OrdemServico) {
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(ordem.descricaoServico, fontWeight = FontWeight.Bold)
            ordem.dataConclusaoEpochMillis?.let {
                Text("Concluído em ${formato.format(Date(it))}", style = MaterialTheme.typography.bodySmall)
            }
            if (ordem.mecanicoResponsavel.isNotBlank()) {
                Text("Mecânico: ${ordem.mecanicoResponsavel}", style = MaterialTheme.typography.bodySmall)
            }
            Text("KM na época: ${ordem.quilometragem}", style = MaterialTheme.typography.bodySmall)
            if (ordem.valor > 0) {
                Text("Valor: R$ %.2f".format(ordem.valor), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
