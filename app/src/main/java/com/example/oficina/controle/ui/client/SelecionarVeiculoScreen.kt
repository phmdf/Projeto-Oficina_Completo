package com.example.oficina.controle.ui.client

import androidx.compose.foundation.clickable
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
import com.example.oficina.controle.viewmodel.AppViewModel

/**
 * Mostra os veículos vinculados ao cliente já autenticado (clienteId vem do login).
 * Se o cliente tiver só um veículo, o app pula essa tela automaticamente (ver AppNavGraph).
 */
@Composable
fun SelecionarVeiculoScreen(vm: AppViewModel, clienteId: Long, aoSelecionar: (Long) -> Unit) {
    val veiculos by vm.buscarVeiculosDoCliente(clienteId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Selecione seu veículo") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (veiculos.isEmpty()) {
                Text("Nenhum veículo vinculado ao seu cadastro ainda. Procure a oficina para registrar seu veículo.")
            } else {
                Text(
                    "Toque no seu veículo para continuar:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(veiculos) { veiculo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { aoSelecionar(veiculo.id) },
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text("${veiculo.marca} ${veiculo.modelo} (${veiculo.ano})", fontWeight = FontWeight.Bold)
                            Text("Placa: ${veiculo.placa}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
