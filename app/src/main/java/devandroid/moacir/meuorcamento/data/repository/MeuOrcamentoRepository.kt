package devandroid.moacir.meuorcamento.data.repository

import devandroid.moacir.meuorcamento.data.dao.CategoriaDao
import devandroid.moacir.meuorcamento.data.dao.LancamentoDao
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.model.Lancamento
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

    // --- Operações de Categoria ---

    /**
     * Retorna um fluxo com todas as categorias, ordenadas por nome.
     * O Flow garante que a UI seja atualizada automaticamente quando os dados mudarem.
     */
    fun getTodasCategorias(): Flow<List<Categoria>> {
        return categoriaDao.buscarTodas()
    }

    /**
     * Insere uma nova categoria no banco de dados.
     * Esta é uma operação assíncrona.
     */
    suspend fun inserirCategoria(categoria: Categoria) {
        categoriaDao.inserir(categoria)
    }

    // --- Operações de Lançamento ---

    /**
     * Retorna um fluxo com todos os lançamentos, ordenados por data decrescente.
     */
    fun getTodosLancamentos(): Flow<List<Lancamento>> {
        return lancamentoDao.buscarTodos()
    }

    /**
     * Insere um novo lançamento no banco de dados.
     * Esta é uma operação assíncrona.
     */
    suspend fun inserirLancamento(lancamento: Lancamento) {
        lancamentoDao.inserir(lancamento)
    }
}
