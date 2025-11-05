package devandroid.moacir.meuorcamento.data.repository

import devandroid.moacir.meuorcamento.data.dao.CategoriaDao
import devandroid.moacir.meuorcamento.data.dao.LancamentoDao
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.model.LancamentoComCategoria
import kotlinx.coroutines.flow.Flow

/**
 * Repositório que gerencia as operações de dados para Lançamentos e Categorias.
 * Ele abstrai a fonte de dados (neste caso, o Room) do resto do aplicativo.
 *
 * @param lancamentoDao O DAO para acessar os dados dos lançamentos.
 * @param categoriaDao O DAO para acessar os dados das categorias.
 */
class MeuOrcamentoRepository(
    private val lancamentoDao: LancamentoDao,
    private val categoriaDao: CategoriaDao
) {
    // Altere o tipo de retorno aqui
    val todosLancamentos: Flow<List<LancamentoComCategoria>> =
        lancamentoDao.getTodosLancamentosComCategoria()

    // --- Operações de Categoria ---

    fun getTodasCategorias(): Flow<List<Categoria>> {
        return categoriaDao.buscarTodas()
    }

    suspend fun inserirCategoria(categoria: Categoria) {
        categoriaDao.inserir(categoria)
    }

    suspend fun updateCategoria(categoria: List<Categoria>) {
        categoriaDao.update(categoria)
    }

    suspend fun deletarCategoria(categoria: Categoria) {
        categoriaDao.delete(categoria)
    }

    // --- Operações de Lançamento ---

    fun getTodosLancamentosSimples(): Flow<List<Lancamento>> {
        return lancamentoDao.buscarTodos()
    }

    suspend fun inserirLancamento(lancamento: Lancamento) {
        lancamentoDao.inserir(lancamento)
    }

    suspend fun deletarLancamento(lancamento: Lancamento) {
        lancamentoDao.delete(lancamento)
    }

    suspend fun updateLancamento(lancamento: Lancamento) {
        lancamentoDao.update(lancamento)
    }

    suspend fun contarCategorias(): Int {
        return categoriaDao.contar()
    }

    // Este método já deve retornar o Flow com o tipo combinado
    fun getLancamentosComCategoria(): Flow<List<LancamentoComCategoria>> {
        return lancamentoDao.getLancamentosComCategoria()
    }
    suspend fun insert(lancamento: Lancamento) {
        lancamentoDao.insert(lancamento)
    }

    suspend fun update(lancamento: Lancamento) {
        lancamentoDao.update(lancamento)
    }
    suspend fun salvarCategorias(categorias: List<Categoria>) {
        categoriaDao.inserirOuAtualizarCategorias(categorias)
    }
    suspend fun atualizarCategorias(categorias: List<Categoria>) {
        categoriaDao.inserirOuAtualizarCategorias(categorias)
    }
}
