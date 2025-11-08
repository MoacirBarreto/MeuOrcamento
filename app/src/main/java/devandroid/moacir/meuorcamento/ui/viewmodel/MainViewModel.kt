package devandroid.moacir.meuorcamento.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.model.TipoLancamento
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Classe de dados para agrupar os totais de forma coesa.
 */
data class Totais(
    val totalReceitas: BigDecimal = BigDecimal.ZERO,
    val totalDespesas: BigDecimal = BigDecimal.ZERO
)

/**
 * ViewModel principal da aplicação.
 * Responsável por preparar e gerenciar os dados para a MainActivity.
 */
class MainViewModel(private val repository: MeuOrcamentoRepository) : ViewModel() {

    // CORREÇÃO: A propriedade foi tornada pública removendo o 'private'.
    // Agora a MainActivity pode observar este StateFlow.
    val todosLancamentos = repository.todosLancamentos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    // Otimização: A mesma abordagem para categorias, garantindo consistência.
    val todasCategorias: StateFlow<List<Categoria>> = repository.getTodasCategorias().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    // Flow que calcula os totais de receitas e despesas a partir dos lançamentos.
    // A transformação (map) só ocorre quando `todosLancamentos` emite um novo valor.
    val totais: StateFlow<Totais> = todosLancamentos.map { lancamentos ->
        // Otimização: Usar partition para dividir a lista em duas de uma só vez.
        val (receitas, despesas) = lancamentos.partition { it.lancamento.tipo == TipoLancamento.RECEITA }

        val totalReceitas = receitas.sumOf { it.lancamento.valor }
        val totalDespesas = despesas.sumOf { it.lancamento.valor }

        Totais(totalReceitas, totalDespesas)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = Totais() // Valor inicial seguro
    )

    // Flow que calcula o saldo total. Deriva diretamente dos totais para ser mais eficiente.
    val saldoTotal: StateFlow<BigDecimal> = totais.map { it.totalReceitas + it.totalDespesas }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = BigDecimal.ZERO
        )


    fun getLancamentosPorPeriodo(dataInicio: LocalDate, dataFim: LocalDate): Flow<List<Lancamento>> {
        return repository.getLancamentosEntreDatas(dataInicio, dataFim)
    }
    /**
     * Salva um lançamento (novo ou editado).
     * Otimização: Especifica o Dispatchers.IO para operações de escrita no banco de dados.
     */
    fun salvarLancamento(lancamento: Lancamento) = viewModelScope.launch(Dispatchers.IO) {
        repository.inserirLancamento(lancamento)
    }

    /**
     * Exclui um lançamento.
     * Otimização: Especifica o Dispatchers.IO.
     */
    fun excluirLancamento(lancamento: Lancamento) = viewModelScope.launch(Dispatchers.IO) {
        repository.deletarLancamento(lancamento)
    }

    /**
     * Atualiza os nomes das categorias.
     * Otimização: Especifica o Dispatchers.IO.
     */
    fun updateCategorias(categorias: List<Categoria>) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCategorias(categorias)
    }
}

