package devandroid.moacir.meuorcamento

import android.app.Application
import devandroid.moacir.meuorcamento.data.AppDatabase
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository

/**
 * Classe Application personalizada para inicializar e fornecer
 * instâncias únicas do banco de dados e do repositório.
 */
class MeuOrcamentoApplication : Application() {

    // A inicialização 'lazy' garante que o banco de dados e o repositório
    // só sejam criados quando forem realmente necessários pela primeira vez.

    // Cria a instância do banco de dados de forma preguiçosa (lazy).
    private val database by lazy { AppDatabase.getDatabase(this) }

    // Cria a instância do repositório, usando os DAOs do banco de dados.
    val repository by lazy {
        MeuOrcamentoRepository(database.lancamentoDao(), database.categoriaDao())
    }
}
