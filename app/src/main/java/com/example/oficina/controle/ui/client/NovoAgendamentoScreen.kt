package com.example.oficina.controle.ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oficina.controle.viewmodel.AppViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoAgendamentoScreen(vm: AppViewModel, veiculoId: Long, aoConcluir: () -> Unit) {
    var tipoServico by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var diasTexto by remember { mutableStateOf("3") }
    var confirmado by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agendar Revisão") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (confirmado) {
                Text(
                    "Solicitação enviada! A oficina irá confirmar seu horário em breve.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = aoConcluir, modifier = Modifier.fillMaxWidth()) {
                    Text("Voltar")
                }
            } else {
                Text("Conte-nos o que seu veículo precisa:", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = tipoServico,
                    onValueChange = { tipoServico = it },
                    label = { Text("Tipo de serviço (ex: Revisão 20.000km, troca de óleo)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = diasTexto,
                    onValueChange = { diasTexto = it.filter { c -> c.isDigit() } },
                    label = { Text("Em quantos dias você gostaria de trazer o carro?") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    label = { Text("Observações (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, diasTexto.toIntOrNull() ?: 1)
                        vm.criarAgendamento(
                            veiculoId = veiculoId,
                            dataHoraEpochMillis = cal.timeInMillis,
                            tipoServico = tipoServico.ifBlank { "Revisão geral" },
                            observacoes = observacoes
                        )
                        confirmado = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Solicitar Agendamento")
                }
            }
        }
    }
}
