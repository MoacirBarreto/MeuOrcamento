package devandroid.moacir.meuorcamento.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel dedicado para a tela de gerenciamento de categorias.
 */
class CategoriasViewModel(private val repository: MeuOrcamentoRepository) : ViewModel() {

    // Expõe o Flow de categorias do repositório como um StateFlow.
    // A UI pode coletar dados deste Flow de forma segura.
    val categorias: StateFlow<List<Categoria>> = repository.getTodasCategorias()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Recebe a lista de categorias modificadas da UI e pede ao repositório
     * para persisti-las no banco de dados.
     */
    fun atualizarListaDeCategorias(categorias: List<Categoria>) {
        viewModelScope.launch {
            repository.atualizarCategorias(categorias)
        }
    }
}
