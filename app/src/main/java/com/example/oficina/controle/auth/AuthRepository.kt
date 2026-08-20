package com.example.oficina.controle.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class ResultadoAuth {
    data class Sucesso(val usuario: FirebaseUser) : ResultadoAuth()
    data class SucessoMock(val uid: String, val email: String) : ResultadoAuth()
    data class Erro(val mensagem: String) : ResultadoAuth()
}

/**
 * Interface para representar um usuário (Real ou Mock)
 */
interface AppUsuario {
    val uid: String
    val email: String?
}

class MockFirebaseUser(override val uid: String, override val email: String?) : AppUsuario

/**
 * Lista de e-mails autorizados a acessar o painel da oficina.
 *
 * Em uma versão de produção isso deveria vir de um servidor / Firestore com regras de
 * segurança (ou Custom Claims do Firebase Auth), nunca hardcoded no app — qualquer pessoa
 * pode descompilar o APK e ler esta lista. Para uso interno controlado já é uma melhoria
 * grande em relação a não ter login nenhum, mas troque por uma validação server-side antes
 * de publicar o app.
 */
val EMAILS_AUTORIZADOS_OFICINA = setOf(
    "oficina@exemplo.com",
    "phmdf@hotmail.com"
    // adicione aqui os e-mails da equipe
)

class AuthRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_mock_prefs", Context.MODE_PRIVATE)
    private val _mockUserFlow = MutableStateFlow<MockFirebaseUser?>(null)

    init {
        val savedUid = prefs.getString("user_uid", null)
        val savedEmail = prefs.getString("user_email", null)
        if (savedUid != null) {
            _mockUserFlow.value = MockFirebaseUser(savedUid, savedEmail)
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    }

    val useMock: Boolean = true // Alterne para false para usar Firebase Real

    val usuarioAtualMock: MockFirebaseUser? get() = _mockUserFlow.value

    val usuarioAtual: FirebaseUser?
        get() = if (useMock) null else auth?.currentUser

    /** Emite o usuário logado sempre que o estado de autenticação mudar. */
    fun observarUsuario(): Flow<FirebaseUser?> = callbackFlow {
        if (useMock) {
            // Em modo mock, não usamos o Flow do Firebase diretamente na assinatura que espera FirebaseUser.
            // O ViewModel precisará lidar com isso ou manteremos simplificado.
            trySend(null)
            awaitClose {}
            return@callbackFlow
        }
        val a = auth
        if (a == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        a.addAuthStateListener(listener)
        awaitClose { a.removeAuthStateListener(listener) }
    }

    fun observarUsuarioMock(): Flow<MockFirebaseUser?> = _mockUserFlow

    suspend fun entrar(email: String, senha: String): ResultadoAuth {
        if (useMock) {
            val uid = "mock_uid_${email.hashCode()}"
            salvarSessaoMock(uid, email)
            return ResultadoAuth.SucessoMock(uid, email)
        }
        val a = auth ?: return ResultadoAuth.Erro("Firebase não configurado.")
        return try {
            val resultado = a.signInWithEmailAndPassword(email, senha).await()
            val usuario = resultado.user ?: return ResultadoAuth.Erro("Não foi possível entrar.")
            ResultadoAuth.Sucesso(usuario)
        } catch (e: Exception) {
            ResultadoAuth.Erro(mapearErro(e))
        }
    }

    suspend fun cadastrar(email: String, senha: String): ResultadoAuth {
        if (useMock) {
            val uid = "mock_uid_${email.hashCode()}"
            salvarSessaoMock(uid, email)
            return ResultadoAuth.SucessoMock(uid, email)
        }
        val a = auth ?: return ResultadoAuth.Erro("Firebase não configurado.")
        return try {
            val resultado = a.createUserWithEmailAndPassword(email, senha).await()
            val usuario = resultado.user ?: return ResultadoAuth.Erro("Não foi possível criar a conta.")
            ResultadoAuth.Sucesso(usuario)
        } catch (e: Exception) {
            ResultadoAuth.Erro(mapearErro(e))
        }
    }

    private fun salvarSessaoMock(uid: String, email: String) {
        prefs.edit().putString("user_uid", uid).putString("user_email", email).apply()
        _mockUserFlow.value = MockFirebaseUser(uid, email)
    }

    fun sair() {
        if (useMock) {
            prefs.edit().clear().apply()
            _mockUserFlow.value = null
        } else {
            auth?.signOut()
        }
    }

    fun emailAutorizadoParaOficina(email: String?): Boolean {
        if (email == null) return false
        return EMAILS_AUTORIZADOS_OFICINA.contains(email.trim().lowercase())
    }

    private fun mapearErro(e: Exception): String {
        return when {
            e.message?.contains("badly formatted", ignoreCase = true) == true -> "E-mail inválido."
            e.message?.contains("password is invalid", ignoreCase = true) == true -> "Senha incorreta."
            e.message?.contains("no user record", ignoreCase = true) == true -> "Não existe conta com esse e-mail."
            e.message?.contains("already in use", ignoreCase = true) == true -> "Já existe uma conta com esse e-mail."
            e.message?.contains("at least 6 characters", ignoreCase = true) == true -> "A senha precisa ter no mínimo 6 caracteres."
            else -> e.message ?: "Ocorreu um erro. Tente novamente."
        }
    }
}
