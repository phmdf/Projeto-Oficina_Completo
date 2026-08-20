package com.example.oficina.controle.ui.auth

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
import com.example.oficina.controle.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletarPerfilClienteScreen(
    vm: AuthViewModel,
    uid: String,
    emailConta: String,
    aoConcluir: (clienteId: Long) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Complete seu cadastro") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Só mais um passo antes de continuar:",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Seu nome completo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
            OutlinedTextField(
                value = telefone,
                onValueChange = { telefone = it },
                label = { Text("Telefone") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Button(
                onClick = {
                    if (nome.isNotBlank()) {
                        vm.criarClienteVinculado(uid, nome, telefone, emailConta, aoConcluir)
                    }
                },
                enabled = nome.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 20.dp)
            ) {
                Text("Continuar")
            }
        }
    }
}
