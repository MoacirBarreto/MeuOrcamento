package devandroid.moacir.meuorcamento.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.model.Natureza
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

class MainViewModel(private val repository: MeuOrcamentoRepository) : ViewModel() {

    // Expõe um fluxo (Flow) de lançamentos que a UI pode observar.
    // O `stateIn` converte um Flow frio em um Flow quente (StateFlow),
    // que mantém o último valor emitido e sobrevive a mudanças de configuração.
    val todosLancamentos: StateFlow<List<Lancamento>> = repository.getTodosLancamentos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Começa a coletar quando a UI observa e para 5s depois
            initialValue = emptyList() // Valor inicial enquanto os dados não chegam
        )

    val todasCategorias: StateFlow<List<Categoria>> = repository.getTodasCategorias()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    /**
     * Função para adicionar um novo lançamento.
     * Ela é chamada a partir da UI (ex: clique de um botão).
     */
    fun adicionarLancamento(
        descricao: String,
        valor: BigDecimal,
        categoriaId: Int,
        natureza: Natureza,
        data: Long = System.currentTimeMillis() // Novo parâmetro com valor padrão
    ) {
        viewModelScope.launch {
            val novoLancamento = Lancamento(
                valor = valor,
                dataHora = data, // Usa o parâmetro de data
                natureza = natureza,
                descricaoLancamento = descricao,
                categoriaId = categoriaId.toLong() // <<< CORREÇÃO APLICADA AQUI
            )
            repository.inserirLancamento(novoLancamento)
        }
    }


    /**
     * Função para adicionar uma nova categoria.
     */
    fun adicionarCategoria(nome: String) {
        viewModelScope.launch {
            val novaCategoria = Categoria(nome = nome)
            repository.inserirCategoria(novaCategoria)
        }
    }

    // NOVO: Função para atualizar uma categoria
    fun atualizarCategoria(categoria: Categoria) {
        viewModelScope.launch {
            repository.updateCategoria(categoria)
        }
    }

    // NOVO: Função para deletar uma categoria
    fun deletarCategoria(categoria: Categoria) {
        viewModelScope.launch {
            repository.deletarCategoria(categoria)
        }
    }


    fun deletarLancamento(lancamento: Lancamento) {
        viewModelScope.launch {
            repository.deletarLancamento(lancamento)
        }
    }

    fun atualizarLancamento(lancamento: Lancamento) {
        viewModelScope.launch {
            repository.updateLancamento(lancamento)
        }
    }

}
