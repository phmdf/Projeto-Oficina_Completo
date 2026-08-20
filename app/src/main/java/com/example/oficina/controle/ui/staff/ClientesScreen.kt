package com.example.oficina.controle.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.oficina.controle.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(vm: AppViewModel) {
    val clientes by vm.clientes.collectAsState()
    val veiculos by vm.veiculos.collectAsState()
    var aba by remember { mutableStateOf(0) }
    var mostrarDialogoCliente by remember { mutableStateOf(false) }
    var mostrarDialogoVeiculo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Clientes e Veículos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (aba == 0) mostrarDialogoCliente = true else mostrarDialogoVeiculo = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = aba) {
                Tab(selected = aba == 0, onClick = { aba = 0 }, text = { Text("Clientes") })
                Tab(selected = aba == 1, onClick = { aba = 1 }, text = { Text("Veículos") })
            }

            if (aba == 0) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(clientes) { cliente ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(cliente.nome, fontWeight = FontWeight.Bold)
                                Text(cliente.telefone, style = MaterialTheme.typography.bodySmall)
                                if (cliente.email.isNotBlank()) {
                                    Text(cliente.email, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(veiculos) { veiculo ->
                        val dono = clientes.find { it.id == veiculo.clienteId }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${veiculo.marca} ${veiculo.modelo} (${veiculo.ano})", fontWeight = FontWeight.Bold)
                                Text("Placa: ${veiculo.placa}", style = MaterialTheme.typography.bodySmall)
                                Text("Dono: ${dono?.nome ?: "?"}", style = MaterialTheme.typography.bodySmall)
                                Text("KM atual: ${veiculo.quilometragemAtual}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoCliente) {
        NovoClienteDialog(
            aoFechar = { mostrarDialogoCliente = false },
            aoSalvar = { nome, tel, email ->
                vm.salvarCliente(nome, tel, email)
                mostrarDialogoCliente = false
            }
        )
    }

    if (mostrarDialogoVeiculo) {
        NovoVeiculoDialog(
            clientes = clientes,
            aoFechar = { mostrarDialogoVeiculo = false },
            aoSalvar = { clienteId, placa, marca, modelo, ano, km ->
                vm.salvarVeiculo(clienteId, placa, marca, modelo, ano, km)
                mostrarDialogoVeiculo = false
            }
        )
    }
}

@Composable
fun NovoClienteDialog(
    aoFechar: () -> Unit,
    aoSalvar: (nome: String, telefone: String, email: String) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Novo Cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = telefone, onValueChange = { telefone = it }, label = { Text("Telefone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (opcional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (nome.isNotBlank()) aoSalvar(nome, telefone, email) }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar") } }
    )
}

@Composable
fun NovoVeiculoDialog(
    clientes: List<com.example.oficina.controle.data.Cliente>,
    aoFechar: () -> Unit,
    aoSalvar: (clienteId: Long, placa: String, marca: String, modelo: String, ano: Int, km: Int) -> Unit
) {
    var clienteSelecionado by remember { mutableStateOf(clientes.firstOrNull()) }
    var expandido by remember { mutableStateOf(false) }
    var placa by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }
    var km by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Novo Veículo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                    OutlinedTextField(
                        value = clienteSelecionado?.nome ?: "Nenhum cliente cadastrado",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dono do veículo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    androidx.compose.material3.DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                        clientes.forEach { c ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(c.nome) },
                                onClick = { clienteSelecionado = c; expandido = false }
                            )
                        }
                    }
                }
                OutlinedTextField(value = placa, onValueChange = { placa = it.uppercase() }, label = { Text("Placa") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = marca, onValueChange = { marca = it }, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = modelo, onValueChange = { modelo = it }, label = { Text("Modelo") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ano, onValueChange = { ano = it.filter { c -> c.isDigit() } }, label = { Text("Ano") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = km, onValueChange = { km = it.filter { c -> c.isDigit() } }, label = { Text("Quilometragem atual") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val c = clienteSelecionado ?: return@Button
                if (placa.isNotBlank()) {
                    aoSalvar(c.id, placa, marca, modelo, ano.toIntOrNull() ?: 0, km.toIntOrNull() ?: 0)
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar") } }
    )
}
