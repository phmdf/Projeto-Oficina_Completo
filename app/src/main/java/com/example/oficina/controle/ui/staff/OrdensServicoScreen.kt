package com.example.oficina.controle.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.oficina.controle.data.OrdemServico
import com.example.oficina.controle.data.StatusOrdemServico
import com.example.oficina.controle.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdensServicoScreen(vm: AppViewModel) {
    val ordens by vm.ordensTodas.collectAsState()
    val veiculos by vm.veiculos.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ordens de Serviço") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(ordens) { ordem ->
                val veiculo = veiculos.find { it.id == ordem.veiculoId }
                OrdemServicoCard(
                    ordem = ordem,
                    nomeVeiculo = "${veiculo?.marca ?: ""} ${veiculo?.modelo ?: ""} - ${veiculo?.placa ?: "?"}",
                    aoMudarStatus = { novoStatus -> vm.atualizarStatusOrdem(ordem, novoStatus) },
                    aoDefinirValor = { valor -> vm.atualizarValorOrdem(ordem, valor) }
                )
            }
        }
    }
}

@Composable
fun OrdemServicoCard(
    ordem: OrdemServico,
    nomeVeiculo: String,
    aoMudarStatus: (StatusOrdemServico) -> Unit,
    aoDefinirValor: (Double) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    var valorTexto by remember { mutableStateOf(if (ordem.valor > 0) ordem.valor.toString() else "") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(nomeVeiculo, fontWeight = FontWeight.Bold)
            Text(ordem.descricaoServico)
            if (ordem.mecanicoResponsavel.isNotBlank()) {
                Text("Mecânico: ${ordem.mecanicoResponsavel}", style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    TextButton(onClick = { expandido = true }) {
                        Text("Status: ${statusLegivel(ordem.status)}")
                    }
                    DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                        StatusOrdemServico.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(statusLegivel(status)) },
                                onClick = { aoMudarStatus(status); expandido = false }
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = { valorTexto = it },
                    label = { Text("Valor (R$)") },
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { aoDefinirValor(valorTexto.replace(",", ".").toDoubleOrNull() ?: 0.0) }) {
                    Text("Salvar")
                }
            }
        }
    }
}
