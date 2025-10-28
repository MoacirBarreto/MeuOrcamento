package devandroid.moacir.meuorcamento.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository

/**
 * Factory para criar instâncias de ViewModels que dependem do MeuOrcamentoRepository.
 * Isso permite a injeção de dependência no ViewModel.
 */
class ViewModelFactory(private val repository: MeuOrcamentoRepository) : ViewModelProvider.Factory {

    /**
     * Cria uma nova instância do ViewModel solicitado.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica se a classe do ViewModel que está sendo pedida é a MainViewModel
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // Se for, cria e retorna uma instância dela, passando o repositório.
            // O cast com 'as T' é seguro por causa da verificação acima.
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        // Se for um ViewModel desconhecido, lança uma exceção.
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
