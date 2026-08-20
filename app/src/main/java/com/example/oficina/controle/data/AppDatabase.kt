package com.example.oficina.controle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromStatusAgendamento(status: StatusAgendamento): String = status.name

    @TypeConverter
    fun toStatusAgendamento(value: String): StatusAgendamento = StatusAgendamento.valueOf(value)

    @TypeConverter
    fun fromStatusOrdemServico(status: StatusOrdemServico): String = status.name

    @TypeConverter
    fun toStatusOrdemServico(value: String): StatusOrdemServico = StatusOrdemServico.valueOf(value)
}

@Database(
    entities = [Cliente::class, Veiculo::class, Agendamento::class, OrdemServico::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
    abstract fun veiculoDao(): VeiculoDao
    abstract fun agendamentoDao(): AgendamentoDao
    abstract fun ordemServicoDao(): OrdemServicoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oficina_db"
                )
                    // App em fase inicial: sem migrações formais ainda, recria o banco se o schema mudar.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
