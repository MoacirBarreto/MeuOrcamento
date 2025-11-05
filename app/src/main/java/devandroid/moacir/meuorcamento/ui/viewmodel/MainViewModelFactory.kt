package devandroid.moacir.meuorcamento.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository

/**
 * Factory para criar instâncias de MainViewModel com dependências (MeuOrcamentoRepository).
 * Isso permite que o ViewModel seja criado com os parâmetros necessários.
 */
class MainViewModelFactory(
    private val repository: MeuOrcamentoRepository

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida: ${modelClass.name}")
    }
}
