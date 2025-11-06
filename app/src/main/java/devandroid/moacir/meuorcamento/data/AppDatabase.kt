package devandroid.moacir.meuorcamento.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import devandroid.moacir.meuorcamento.data.dao.CategoriaDao
import devandroid.moacir.meuorcamento.data.dao.LancamentoDao
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.model.Lancamento
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Lancamento::class, Categoria::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lancamentoDao(): LancamentoDao
    abstract fun categoriaDao(): CategoriaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // O synchronized garante que apenas uma thread possa executar este bloco por vez,
            // evitando a criação de múltiplas instâncias do banco.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meu_orcamento_db"
                )
                    // Adiciona o nosso callback para popular os dados na criação.
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback para popular o banco de dados na primeira vez que ele é criado.
     */
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d("AppDatabase", "onCreate do banco de dados foi chamado. Inserindo dados iniciais...")
            INSTANCE?.let { database ->
                // Usar um escopo de coroutine global é aceitável aqui, pois esta é uma
                // operação única de inicialização do aplicativo.
                CoroutineScope(Dispatchers.IO).launch {
                    val categoriaDao = database.categoriaDao()

                    // Limpa dados antigos para garantir um estado inicial limpo
                    categoriaDao.deleteAll()

                    val categoriasIniciais = listOf(
                        // Categoria não editável para receitas
                        Categoria(id = 1, nome = "Receita"),
                        // Categorias de despesa editáveis
                        Categoria(id = 2, nome = "Alimentação"),
                        Categoria(id = 3, nome = "Casa"),
                        Categoria(id = 4, nome = "Lazer"),
                        Categoria(id = 5, nome = "Transporte"),
                        Categoria(id = 6, nome = "Outros")
                    )

                    categoriaDao.inserirOuAtualizarCategorias(categoriasIniciais)
                    Log.d("AppDatabase", "Categorias iniciais inseridas com sucesso.")
                }
            }
        }
    }
}
