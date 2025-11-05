package devandroid.moacir.meuorcamento.data

import android.content.Context
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

@Database(entities = [Lancamento::class, Categoria::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lancamentoDao(): LancamentoDao
    abstract fun categoriaDao(): CategoriaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meu_orcamento_database"
                )
                    .addCallback(DatabaseCallback(context)) // <-- 1. ADICIONE O CALLBACK AQUI
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // 2. CRIE A CLASSE PRIVADA DO CALLBACK AQUI DENTRO
    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                // Usando Dispatchers.IO, ideal para operações de banco
                CoroutineScope(Dispatchers.IO).launch {
                    popularBanco(database.categoriaDao())
                }
            }
        }

        suspend fun popularBanco(categoriaDao: CategoriaDao) {
            // Insere as categorias padrão
            val categoriasIniciais = listOf(
                Categoria(id = 1, nome = "Alimentação"),
                Categoria(id = 2, nome = "Casa"),
                Categoria(id = 3, nome = "Lazer"),
                Categoria(id = 4, nome = "Transporte"),
                Categoria(id = 5, nome = "Outros")
            )
            categoriaDao.inserirOuAtualizarCategorias(categoriasIniciais)
        }
    }
}
