package devandroid.moacir.meuorcamento.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.model.LancamentoComCategoria
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// A injeção de dependência é feita aqui pelo construtor.
class MainViewModel(private val repository: MeuOrcamentoRepository) : ViewModel() {
    val todosLancamentos: Flow<List<LancamentoComCategoria>> = repository.getLancamentosComCategoria()
    // Expõe as categorias como um StateFlow (quente), que é a prática recomendada.
    val todasCategorias: StateFlow<List<Categoria>> = repository.getTodasCategorias()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Inicia 5s após a UI parar de observar
            initialValue = emptyList() // Valor inicial enquanto os dados carregam
        )

    /**
     * Função única para atualizar a lista de categorias.
     * Renomeada para ser mais clara e evitar ambiguidade.
     */
    fun atualizarListaDeCategorias(categorias: List<Categoria>) {
        viewModelScope.launch { // Executa a operação de banco de dados em segundo plano
            repository.atualizarCategorias(categorias)
        }
    }

    // --- Lançamentos ---

    // Expõe os lançamentos como um StateFlow

     fun adicionarLancamento(lancamento: Lancamento) {
        viewModelScope.launch {
            repository.inserirLancamento(lancamento)
        }
    }

    fun atualizarLancamento(lancamento: Lancamento) {
        viewModelScope.launch {
            repository.updateLancamento(lancamento)
        }
    }

    fun deletarLancamento(lancamento: Lancamento) {
        viewModelScope.launch {
            repository.deletarLancamento(lancamento)
        }
    }
    fun salvarLancamento(lancamento: Lancamento) {
        viewModelScope.launch {
            // Se o id for 0, é um novo lançamento. Caso contrário, é uma atualização.
            if (lancamento.id == 0L) {
                repository.insert(lancamento)
            } else {
                repository.update(lancamento)
            }
        }
    }

}
