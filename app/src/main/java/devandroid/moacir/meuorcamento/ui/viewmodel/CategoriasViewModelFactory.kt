package devandroid.moacir.meuorcamento.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository

/**
 * Factory para criar instâncias de CategoriasViewModel com o repositório como dependência.
 */
class CategoriasViewModelFactory(private val repository: MeuOrcamentoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoriasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
