package devandroid.moacir.meuorcamento.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devandroid.moacir.meuorcamento.data.model.LancamentoComCategoria
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
enum class TipoPeriodo {
    MES_CORRENTE, ANO_CORRENTE, PERSONALIZADO
}

@OptIn(ExperimentalCoroutinesApi::class)
class TotalizadoresViewModel(private val repository: MeuOrcamentoRepository) : ViewModel() {
    private val _tipoPeriodoSelecionado = MutableStateFlow(TipoPeriodo.MES_CORRENTE)
    val tipoPeriodoSelecionado: StateFlow<TipoPeriodo> = _tipoPeriodoSelecionado.asStateFlow()
    // Add these properties to your TotalizadoresViewModel class

    private val _periodoSelecionado = MutableStateFlow(LocalDate.now().withDayOfMonth(1) to LocalDate.now())
    val periodoSelecionado: StateFlow<Pair<LocalDate, LocalDate>> = _periodoSelecionado.asStateFlow()

    fun setTipoPeriodo(tipo: TipoPeriodo) {
        _tipoPeriodoSelecionado.value = tipo
    }
    // 2. StateFlow que reage a mudanças no período e busca os dados no banco.
    // flatMapLatest é perfeito para isso: cancela a busca antiga e inicia uma nova.
    val lancamentosDoPeriodo: StateFlow<List<LancamentoComCategoria>> =
        periodoSelecionado.flatMapLatest { (inicio, fim) ->
            repository.getLancamentosComCategoriaPorPeriodo(inicio, fim)
        }.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Função pública para a Activity atualizar o período.
     * Isso vai disparar automaticamente a atualização do 'lancamentosDoPeriodo'.
     */
    fun setPeriodo(dataInicio: LocalDate, dataFim: LocalDate) {
        viewModelScope.launch {
            // Use os nomes corretos dos parâmetros: dataInicio e dataFim
            _periodoSelecionado.emit(Pair(dataInicio, dataFim))
        }
    }

    // A função original ainda pode ser usada para os totais de mês/ano, se preferir.
    fun getLancamentosComCategoriaPorPeriodo(
        dataInicio: LocalDate,
        dataFim: LocalDate
    ) = repository.getLancamentosComCategoriaPorPeriodo(dataInicio, dataFim)

}
