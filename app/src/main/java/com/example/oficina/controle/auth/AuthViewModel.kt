package com.example.oficina.controle.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.oficina.controle.data.Cliente
import com.example.oficina.controle.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepo: AuthRepository,
    private val dataRepo: Repository
) : ViewModel() {

    val usuario: StateFlow<AppUsuario?> = authRepo.observarUsuario()
        .combine(authRepo.observarUsuarioMock()) { firebaseUser, mockUser ->
            if (authRepo.useMock) mockUser else {
                firebaseUser?.let { MockFirebaseUser(it.uid, it.email) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 
            if (authRepo.useMock) authRepo.usuarioAtualMock else authRepo.usuarioAtual?.let { MockFirebaseUser(it.uid, it.email) }
        )

    private val _carregando = MutableStateFlow(false)
    val carregando: StateFlow<Boolean> = _carregando

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    fun entrarOficina(email: String, senha: String, aoSucesso: () -> Unit) {
        if (!authRepo.emailAutorizadoParaOficina(email)) {
            _erro.value = "Este e-mail não tem acesso ao painel da oficina."
            return
        }
        _carregando.value = true
        viewModelScope.launch {
            when (val resultado = authRepo.entrar(email, senha)) {
                is ResultadoAuth.Sucesso -> {
                    _erro.value = null
                    aoSucesso()
                }
                is ResultadoAuth.SucessoMock -> {
                    _erro.value = null
                    aoSucesso()
                }
                is ResultadoAuth.Erro -> _erro.value = resultado.mensagem
            }
            _carregando.value = false
        }
    }

    fun entrarCliente(email: String, senha: String, aoSucesso: (AppUsuario) -> Unit) {
        _carregando.value = true
        viewModelScope.launch {
            when (val resultado = authRepo.entrar(email, senha)) {
                is ResultadoAuth.Sucesso -> {
                    _erro.value = null
                    aoSucesso(MockFirebaseUser(resultado.usuario.uid, resultado.usuario.email))
                }
                is ResultadoAuth.SucessoMock -> {
                    _erro.value = null
                    aoSucesso(MockFirebaseUser(resultado.uid, resultado.email))
                }
                is ResultadoAuth.Erro -> _erro.value = resultado.mensagem
            }
            _carregando.value = false
        }
    }

    fun cadastrarCliente(email: String, senha: String, aoSucesso: (AppUsuario) -> Unit) {
        _carregando.value = true
        viewModelScope.launch {
            when (val resultado = authRepo.cadastrar(email, senha)) {
                is ResultadoAuth.Sucesso -> {
                    _erro.value = null
                    aoSucesso(MockFirebaseUser(resultado.usuario.uid, resultado.usuario.email))
                }
                is ResultadoAuth.SucessoMock -> {
                    _erro.value = null
                    aoSucesso(MockFirebaseUser(resultado.uid, resultado.email))
                }
                is ResultadoAuth.Erro -> _erro.value = resultado.mensagem
            }
            _carregando.value = false
        }
    }

    suspend fun buscarClienteVinculado(uid: String): Cliente? = dataRepo.buscarClientePorFirebaseUid(uid)

    fun criarClienteVinculado(uid: String, nome: String, telefone: String, email: String, aoConcluir: (Long) -> Unit) {
        viewModelScope.launch {
            val id = dataRepo.salvarCliente(
                Cliente(nome = nome, telefone = telefone, email = email, firebaseUid = uid)
            )
            aoConcluir(id)
        }
    }

    fun sair() {
        authRepo.sair()
    }

    fun limparErro() {
        _erro.value = null
    }
}

class AuthViewModelFactory(
    private val authRepo: AuthRepository,
    private val dataRepo: Repository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepo, dataRepo) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
}
