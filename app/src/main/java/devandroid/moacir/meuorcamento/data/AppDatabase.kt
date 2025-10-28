package devandroid.moacir.meuorcamento.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import devandroid.moacir.meuorcamento.data.dao.CategoriaDao
import devandroid.moacir.meuorcamento.data.dao.LancamentoDao
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.model.Lancamento

@Database(
    entities = [
        Lancamento::class,
        Categoria::class
    ],
    version = 1,
    exportSchema = false // Simplifica o processo de build para este projeto
)
@TypeConverters(Converters::class)

abstract class AppDatabase : RoomDatabase() {

    // Declaração abstrata dos DAOs para que o Room possa implementá-los
    abstract fun lancamentoDao(): LancamentoDao
    abstract fun categoriaDao(): CategoriaDao

    // Companion object para criar a instância única (Singleton) do banco de dados
    companion object {
        // A anotação @Volatile garante que a variável INSTANCE seja sempre atualizada
        // para todas as threads, prevenindo inconsistências.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Retorna a instância se ela já existir.
            // Se não, cria o banco de dados dentro de um bloco 'synchronized'
            // para garantir que apenas uma thread possa criar a instância.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meu_orcamento_db" // Nome do arquivo do banco de dados
                ).build()
                INSTANCE = instance
                // retorna a instância recém-criada
                instance
            }
        }
    }
}
