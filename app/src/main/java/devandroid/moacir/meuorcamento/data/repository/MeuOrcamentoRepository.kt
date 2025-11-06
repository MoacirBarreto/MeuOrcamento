package devandroid.moacir.meuorcamento.data.repository

import devandroid.moacir.meuorcamento.data.dao.CategoriaDao
import devandroid.moacir.meuorcamento.data.dao.LancamentoDao
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.model.LancamentoComCategoria
import kotlinx.coroutines.flow.Flow

class MeuOrcamentoRepository(
    private val lancamentoDao: LancamentoDao,
    private val categoriaDao: CategoriaDao
) {
    // --- Operações de Lançamento ---

    val todosLancamentos: Flow<List<LancamentoComCategoria>> = lancamentoDao.LancamentosMaisCategoria()

    // Função corrigida
    fun getTodosLancamentosSimples(): Flow<List<LancamentoComCategoria>> {
        return lancamentoDao.LancamentosMaisCategoria()
    }

    fun LancamentosMaisCategoria(): Flow<List<LancamentoComCategoria>> {
        return lancamentoDao.LancamentosMaisCategoria()
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

    suspend fun contarCategorias(): Int {
        return categoriaDao.contar()
    }

    suspend fun salvarCategorias(categorias: List<Categoria>) {
        categoriaDao.inserirOuAtualizarCategorias(categorias)
    }

    suspend fun atualizarCategorias(categorias: List<Categoria>) {
        // Esta função parece duplicada, você pode querer usar salvarCategorias
        categoriaDao.inserirOuAtualizarCategorias(categorias)
    }
}
