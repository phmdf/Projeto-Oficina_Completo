package com.example.oficina.controle.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.oficina.controle.auth.AppUsuario
import com.example.oficina.controle.auth.AuthRepository
import com.example.oficina.controle.auth.AuthViewModel
import com.example.oficina.controle.auth.AuthViewModelFactory
import com.example.oficina.controle.data.Repository
import com.example.oficina.controle.ui.auth.CompletarPerfilClienteScreen
import com.example.oficina.controle.ui.auth.LoginClienteScreen
import com.example.oficina.controle.ui.auth.LoginOficinaScreen
import com.example.oficina.controle.ui.client.AcompanharServicoScreen
import com.example.oficina.controle.ui.client.HistoricoClienteScreen
import com.example.oficina.controle.ui.client.MeusAgendamentosScreen
import com.example.oficina.controle.ui.client.NovoAgendamentoScreen
import com.example.oficina.controle.ui.client.SelecionarVeiculoScreen
import com.example.oficina.controle.ui.common.RoleSelectionScreen
import com.example.oficina.controle.ui.staff.AgendaScreen
import com.example.oficina.controle.ui.staff.ClientesScreen
import com.example.oficina.controle.ui.staff.DashboardScreen
import com.example.oficina.controle.ui.staff.HistoricoScreen
import com.example.oficina.controle.ui.staff.OrdensServicoScreen
import com.example.oficina.controle.viewmodel.AppViewModel
import kotlinx.coroutines.launch

private object Rotas {
    const val SELECAO_PERFIL = "selecao_perfil"
    const val LOGIN_OFICINA = "login_oficina"
    const val LOGIN_CLIENTE = "login_cliente"
    const val OFICINA_RAIZ = "oficina_raiz"

    const val COMPLETAR_PERFIL = "completar_perfil/{uid}/{email}"
    fun completarPerfil(uid: String, email: String) = "completar_perfil/$uid/${android.net.Uri.encode(email)}"

    const val CLIENTE_SELECIONAR_VEICULO = "cliente_selecionar_veiculo/{clienteId}"
    fun clienteSelecionarVeiculo(clienteId: Long) = "cliente_selecionar_veiculo/$clienteId"

    const val CLIENTE_RAIZ = "cliente_raiz/{veiculoId}"
    fun clienteRaiz(veiculoId: Long) = "cliente_raiz/$veiculoId"
}

@Composable
fun AppNavGraph(vm: AppViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    val authRepo = remember { AuthRepository(context) }
    val authVm: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AuthViewModelFactory(authRepo, vmRepositorio(vm))
    )
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = Rotas.SELECAO_PERFIL) {
        composable(Rotas.SELECAO_PERFIL) {
            RoleSelectionScreen(
                aoEscolherOficina = { navController.navigate(Rotas.LOGIN_OFICINA) },
                aoEscolherCliente = { navController.navigate(Rotas.LOGIN_CLIENTE) }
            )
        }

        composable(Rotas.LOGIN_OFICINA) {
            LoginOficinaScreen(
                vm = authVm,
                aoEntrar = {
                    navController.navigate(Rotas.OFICINA_RAIZ) {
                        popUpTo(Rotas.SELECAO_PERFIL) { inclusive = false }
                    }
                },
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable(Rotas.LOGIN_CLIENTE) {
            LoginClienteScreen(
                vm = authVm,
                aoLogar = { usuario ->
                    scope.launch {
                        val cliente = authVm.buscarClienteVinculado(usuario.uid)
                        if (cliente != null) {
                            navController.navigate(Rotas.clienteSelecionarVeiculo(cliente.id)) {
                                popUpTo(Rotas.SELECAO_PERFIL) { inclusive = false }
                            }
                        } else {
                            navController.navigate(Rotas.completarPerfil(usuario.uid, usuario.email ?: "")) {
                                popUpTo(Rotas.SELECAO_PERFIL) { inclusive = false }
                            }
                        }
                    }
                },
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable(
            route = Rotas.COMPLETAR_PERFIL,
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val email = android.net.Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            CompletarPerfilClienteScreen(
                vm = authVm,
                uid = uid,
                emailConta = email,
                aoConcluir = { clienteId ->
                    navController.navigate(Rotas.clienteSelecionarVeiculo(clienteId)) {
                        popUpTo(Rotas.SELECAO_PERFIL) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Rotas.CLIENTE_SELECIONAR_VEICULO,
            arguments = listOf(navArgument("clienteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getLong("clienteId") ?: 0L
            val veiculos by vm.buscarVeiculosDoCliente(clienteId).collectAsState(initial = emptyList())

            // Se o cliente tem exatamente um veículo, pula direto pra ele.
            LaunchedEffect(veiculos) {
                if (veiculos.size == 1) {
                    navController.navigate(Rotas.clienteRaiz(veiculos.first().id)) {
                        popUpTo(Rotas.SELECAO_PERFIL) { inclusive = false }
                    }
                }
            }

            SelecionarVeiculoScreen(
                vm = vm,
                clienteId = clienteId,
                aoSelecionar = { veiculoId ->
                    navController.navigate(Rotas.clienteRaiz(veiculoId)) {
                        popUpTo(Rotas.SELECAO_PERFIL) { inclusive = false }
                    }
                }
            )
        }

        composable(Rotas.OFICINA_RAIZ) {
            OficinaShell(
                vm = vm,
                aoSair = {
                    authVm.sair()
                    navController.navigate(Rotas.SELECAO_PERFIL) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Rotas.CLIENTE_RAIZ,
            arguments = listOf(navArgument("veiculoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val veiculoId = backStackEntry.arguments?.getLong("veiculoId") ?: 0L
            ClienteShell(
                vm = vm,
                veiculoId = veiculoId,
                aoSair = {
                    authVm.sair()
                    navController.navigate(Rotas.SELECAO_PERFIL) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

/** Pequeno helper para expor o Repository interno do AppViewModel ao AuthViewModel. */
private fun vmRepositorio(vm: AppViewModel): Repository = vm.repositorioPublico

private sealed class AbaOficina(val rota: String, val titulo: String, val icone: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : AbaOficina("dashboard", "Início", Icons.Filled.Home)
    object Agenda : AbaOficina("agenda", "Agenda", Icons.Filled.CalendarMonth)
    object Ordens : AbaOficina("ordens", "Ordens", Icons.Filled.Build)
    object Clientes : AbaOficina("clientes", "Clientes", Icons.Filled.Group)
    object Historico : AbaOficina("historico", "Histórico", Icons.Filled.History)
}

@Composable
private fun OficinaShell(vm: AppViewModel, aoSair: () -> Unit) {
    val abas = listOf(AbaOficina.Dashboard, AbaOficina.Agenda, AbaOficina.Ordens, AbaOficina.Clientes, AbaOficina.Historico)
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val rotaAtual = backStackEntry?.destination
                abas.forEach { aba ->
                    NavigationBarItem(
                        selected = rotaAtual?.hierarchy?.any { it.route == aba.rota } == true,
                        onClick = {
                            navController.navigate(aba.rota) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(aba.icone, contentDescription = aba.titulo) },
                        label = { Text(aba.titulo) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AbaOficina.Dashboard.rota,
            modifier = Modifier.padding(padding)
        ) {
            composable(AbaOficina.Dashboard.rota) { DashboardScreen(vm, aoSair = aoSair) }
            composable(AbaOficina.Agenda.rota) { AgendaScreen(vm) }
            composable(AbaOficina.Ordens.rota) { OrdensServicoScreen(vm) }
            composable(AbaOficina.Clientes.rota) { ClientesScreen(vm) }
            composable(AbaOficina.Historico.rota) { HistoricoScreen(vm) }
        }
    }
}

private sealed class AbaCliente(val rota: String, val titulo: String, val icone: androidx.compose.ui.graphics.vector.ImageVector) {
    object Agendar : AbaCliente("agendar", "Agendar", Icons.Filled.CalendarMonth)
    object Meus : AbaCliente("meus_agendamentos", "Agendamentos", Icons.Filled.ListAlt)
    object Acompanhar : AbaCliente("acompanhar", "Acompanhar", Icons.Filled.Build)
    object Historico : AbaCliente("historico_cliente", "Histórico", Icons.Filled.History)
}

@Composable
private fun ClienteShell(vm: AppViewModel, veiculoId: Long, aoSair: () -> Unit) {
    val abas = listOf(AbaCliente.Agendar, AbaCliente.Meus, AbaCliente.Acompanhar, AbaCliente.Historico)
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val rotaAtual = backStackEntry?.destination
                abas.forEach { aba ->
                    NavigationBarItem(
                        selected = rotaAtual?.hierarchy?.any { it.route == aba.rota } == true,
                        onClick = {
                            navController.navigate(aba.rota) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(aba.icone, contentDescription = aba.titulo) },
                        label = { Text(aba.titulo) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AbaCliente.Meus.rota,
            modifier = Modifier.padding(padding)
        ) {
            composable(AbaCliente.Agendar.rota) {
                NovoAgendamentoScreen(vm, veiculoId) {
                    navController.navigate(AbaCliente.Meus.rota) {
                        popUpTo(AbaCliente.Meus.rota) { inclusive = true }
                    }
                }
            }
            composable(AbaCliente.Meus.rota) { MeusAgendamentosScreen(vm, veiculoId, aoSair = aoSair) }
            composable(AbaCliente.Acompanhar.rota) { AcompanharServicoScreen(vm, veiculoId) }
            composable(AbaCliente.Historico.rota) { HistoricoClienteScreen(vm, veiculoId) }
        }
    }
}
