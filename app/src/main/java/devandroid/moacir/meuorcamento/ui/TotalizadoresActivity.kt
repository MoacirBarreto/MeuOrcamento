package devandroid.moacir.meuorcamento.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import devandroid.moacir.meuorcamento.data.AppDatabase
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository
import devandroid.moacir.meuorcamento.databinding.ActivityTotalizadoresBinding
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModel
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class TotalizadoresActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTotalizadoresBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            MeuOrcamentoRepository(
                AppDatabase.getDatabase(this).lancamentoDao(),
                AppDatabase.getDatabase(this).categoriaDao()
            )
        )
    }

    // O formatador agora é 'lazy', inicializado apenas na primeira vez que for usado.
    private val formatadorDeMoeda: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTotalizadoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        configurarListeners()
        carregarDadosIniciais()
    }

    private fun configurarToolbar() {
        binding.toolbarTot.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarListeners() {
        binding.btnSelecionarPeriodo.setOnClickListener {
            mostrarSeletorDePeriodo()
        }
    }

    private fun carregarDadosIniciais() {
        val hoje = LocalDate.now()
        // Mês Corrente
        calcularEExibirTotais(hoje.withDayOfMonth(1), hoje, TipoTotal.MES)
        // Ano Corrente
        calcularEExibirTotais(hoje.withDayOfYear(1), hoje, TipoTotal.ANO)
    }

    private fun mostrarSeletorDePeriodo() {
        val datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Selecione o período")
            .setSelection(
                androidx.core.util.Pair(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds()
                )
            )
            .build()

        datePicker.addOnPositiveButtonClickListener { dateRange ->
            // Conversão de Long (UTC) para LocalDate
            val dataInicio = Instant.ofEpochMilli(dateRange.first)
                .atZone(ZoneId.of("UTC")) // Use UTC para consistência com o DatePicker
                .toLocalDate()
            val dataFim = Instant.ofEpochMilli(dateRange.second)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()

            calcularEExibirTotais(dataInicio, dataFim, TipoTotal.PERIODO)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER_TAG")
    }

    private fun calcularEExibirTotais(
        dataInicio: LocalDate,
        dataFim: LocalDate,
        tipo: TipoTotal
    ) {
        lifecycleScope.launch {


            viewModel.getLancamentosPorPeriodo(dataInicio, dataFim).collectLatest { lancamentos ->
                // Lógica de cálculo separada da lógica de exibição
                val totais = calcularTotais(lancamentos)
                exibirTotais(totais, tipo)
            }
        }
    }

    // Função que apenas calcula, melhorando a separação de responsabilidades
    private fun calcularTotais(lancamentos: List<Lancamento>): Totais {
        var receitas = BigDecimal.ZERO
        var despesas = BigDecimal.ZERO

        lancamentos.forEach { lancamento ->
            if (lancamento.valor > BigDecimal.ZERO) {
                receitas += lancamento.valor
            } else {
                despesas += lancamento.valor
            }
        }
        return Totais(receitas, despesas.abs())
    }

    // Função que apenas exibe os dados, deixando o código mais limpo
    private fun exibirTotais(totais: Totais, tipo: TipoTotal) {
        val (receitas, despesas) = totais
        val saldo = receitas.subtract(despesas)

        // AQUI ESTÁ A CORREÇÃO
        val formatar: (BigDecimal) -> String = { formatadorDeMoeda.format(it.toDouble()) }

        when (tipo) {
            TipoTotal.MES -> {
                binding.tvMesReceitas.text = "Receitas: ${formatar(receitas)}"
                binding.tvMesDespesas.text = "Despesas: ${formatar(despesas)}"
                binding.tvMesSaldo.text = "Saldo: ${formatar(saldo)}"
            }

            TipoTotal.ANO -> {
                binding.tvAnoReceitas.text = "Receitas: ${formatar(receitas)}"
                binding.tvAnoDespesas.text = "Despesas: ${formatar(despesas)}"
                binding.tvAnoSaldo.text = "Saldo: ${formatar(saldo)}"
            }

            TipoTotal.PERIODO -> {
                binding.tvPeriodoReceitas.text = "Receitas: ${formatar(receitas)}"
                binding.tvPeriodoDespesas.text = "Despesas: ${formatar(despesas)}"
                binding.tvPeriodoSaldo.text = "Saldo: ${formatar(saldo)}"
            }
        }
    }
}

// Data class para armazenar os totais calculados
private data class Totais(val receitas: BigDecimal, val despesas: BigDecimal)

// Enum para evitar o uso de "magic strings" ("mes", "ano", etc.)
private enum class TipoTotal {
    MES,
    ANO,
    PERIODO
}
