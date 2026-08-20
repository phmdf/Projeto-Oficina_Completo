package com.example.oficina.controle.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class StatusAgendamento { AGENDADO, CONFIRMADO, CANCELADO, CONCLUIDO }

enum class StatusOrdemServico { AGUARDANDO, EM_ANDAMENTO, AGUARDANDO_PECA, CONCLUIDO }

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val telefone: String,
    val email: String = "",
    /** UID do Firebase Authentication vinculado a este cliente (preenchido no primeiro login). */
    val firebaseUid: String = ""
)

@Entity(
    tableName = "veiculos",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clienteId")]
)
data class Veiculo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clienteId: Long,
    val placa: String,
    val marca: String,
    val modelo: String,
    val ano: Int,
    val quilometragemAtual: Int = 0
)

@Entity(
    tableName = "agendamentos",
    foreignKeys = [
        ForeignKey(
            entity = Veiculo::class,
            parentColumns = ["id"],
            childColumns = ["veiculoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("veiculoId")]
)
data class Agendamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val veiculoId: Long,
    val dataHoraEpochMillis: Long,
    val tipoServico: String,
    val observacoes: String = "",
    val status: StatusAgendamento = StatusAgendamento.AGENDADO
)

@Entity(
    tableName = "ordens_servico",
    foreignKeys = [
        ForeignKey(
            entity = Veiculo::class,
            parentColumns = ["id"],
            childColumns = ["veiculoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("veiculoId")]
)
data class OrdemServico(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val veiculoId: Long,
    val agendamentoId: Long? = null,
    val dataAberturaEpochMillis: Long,
    val dataConclusaoEpochMillis: Long? = null,
    val descricaoServico: String,
    val mecanicoResponsavel: String = "",
    val quilometragem: Int = 0,
    val valor: Double = 0.0,
    val status: StatusOrdemServico = StatusOrdemServico.AGUARDANDO,
    val observacoesInternas: String = ""
)
