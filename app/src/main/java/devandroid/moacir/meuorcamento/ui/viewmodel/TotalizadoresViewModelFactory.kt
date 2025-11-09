package devandroid.moacir.meuorcamento.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository

class TotalizadoresViewModelFactory(private val repository: MeuOrcamentoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TotalizadoresViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TotalizadoresViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
