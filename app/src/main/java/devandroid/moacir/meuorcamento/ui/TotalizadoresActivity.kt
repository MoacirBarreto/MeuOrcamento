package devandroid.moacir.meuorcamento.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker
import devandroid.moacir.meuorcamento.R
import devandroid.moacir.meuorcamento.data.AppDatabase
import devandroid.moacir.meuorcamento.data.model.LancamentoComCategoria
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository
import devandroid.moacir.meuorcamento.databinding.ActivityTotalizadoresBinding
import devandroid.moacir.meuorcamento.ui.viewmodel.TipoPeriodo
import devandroid.moacir.meuorcamento.ui.viewmodel.TotalizadoresViewModel
import devandroid.moacir.meuorcamento.ui.viewmodel.TotalizadoresViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TotalizadoresActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTotalizadoresBinding
    private val viewModel: TotalizadoresViewModel by viewModels {
        TotalizadoresViewModelFactory(MeuOrcamentoRepository(AppDatabase.getDatabase(this).lancamentoDao(), AppDatabase.getDatabase(this).categoriaDao()))
    }
    private val formatadorDeMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val formatadorDeData = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTotalizadoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        configurarListeners()
        observarViewModel()

        if (savedInstanceState == null) {
            carregarDadosIniciais()
        }
    }

    private fun configurarToolbar() {
        binding.toolbarTot.setNavigationOnClickListener { finish() }
    }

    private fun configurarListeners() {
        binding.btnSelecionarPeriodo.setOnClickListener { mostrarSeletorDePeriodo() }

        binding.rgPeriodo.setOnCheckedChangeListener { _, checkedId ->
            val tipo = when (checkedId) {
                R.id.rb_ano_corrente -> TipoPeriodo.ANO_CORRENTE
                R.id.rb_personalizado -> TipoPeriodo.PERSONALIZADO
                else -> TipoPeriodo.MES_CORRENTE
            }
            viewModel.setTipoPeriodo(tipo)
        }
    }

    private fun observarViewModel() {
        // Observa a MUDANÇA DO TIPO de período (Mês, Ano, Período)
        lifecycleScope.launch {
            viewModel.tipoPeriodoSelecionado.collectLatest { tipo ->
                binding.btnSelecionarPeriodo.visibility = if (tipo == TipoPeriodo.PERSONALIZADO) View.VISIBLE else View.GONE

                // Define o RadioButton correto (útil na rotação da tela)
                val idRadio = when(tipo) {
                    TipoPeriodo.MES_CORRENTE -> R.id.rb_mes_corrente
                    TipoPeriodo.ANO_CORRENTE -> R.id.rb_ano_corrente
                    TipoPeriodo.PERSONALIZADO -> R.id.rb_personalizado
                }
                if (binding.rgPeriodo.checkedRadioButtonId != idRadio) {
                    binding.rgPeriodo.check(idRadio)
                }

                if (tipo != TipoPeriodo.PERSONALIZADO) {
                    val hoje = LocalDate.now()
                    val (inicio, fim) = when (tipo) {
                        TipoPeriodo.MES_CORRENTE -> hoje.withDayOfMonth(1) to hoje
                        else -> hoje.withDayOfYear(1) to hoje
                    }
                    viewModel.setPeriodo(inicio, fim)
                }
            }
        }

        // Observa os DADOS (lançamentos) que chegam do período selecionado
        lifecycleScope.launch {
            viewModel.lancamentosDoPeriodo.collectLatest { lancamentos ->
                atualizarUI(lancamentos)
            }
        }

        // Observa o TEXTO do período para atualizar a UI
        lifecycleScope.launch {
            viewModel.periodoSelecionado.collectLatest { (inicio, fim) ->
                val textoPeriodo = "${inicio.format(formatadorDeData)} - ${fim.format(formatadorDeData)}"
                binding.tvPeriodoSelecionado.text = textoPeriodo
            }
        }
    }

    private fun carregarDadosIniciais() {
        binding.rgPeriodo.check(R.id.rb_mes_corrente)
    }

    private fun mostrarSeletorDePeriodo() {
        val datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Selecione o período")
            .build()

        datePicker.addOnPositiveButtonClickListener { dateRange ->
            val dataInicio = Instant.ofEpochMilli(dateRange.first).atZone(ZoneId.of("UTC")).toLocalDate()
            val dataFim = Instant.ofEpochMilli(dateRange.second).atZone(ZoneId.of("UTC")).toLocalDate()
            viewModel.setPeriodo(dataInicio, dataFim)
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER_TAG")
    }

    private fun atualizarUI(lancamentos: List<LancamentoComCategoria>) {
        var receitas = BigDecimal.ZERO
        var despesas = BigDecimal.ZERO
        val despesasPorCategoria = mutableMapOf<String, BigDecimal>()

        lancamentos.forEach { item ->
            val valor = item.lancamento.valor
            if (valor > BigDecimal.ZERO) {
                receitas += valor
            } else {
                despesas += valor.abs()
                val nomeCategoria = item.categoria.nome
                val valorAtual = despesasPorCategoria.getOrDefault(nomeCategoria, BigDecimal.ZERO)
                despesasPorCategoria[nomeCategoria] = valorAtual + valor.abs()
            }
        }
        val saldo = receitas - despesas

        // A lógica de texto e cores que você já implementou
        binding.tvPeriodoReceitas.text = formatadorDeMoeda.format(receitas)
        binding.tvPeriodoDespesas.text = formatadorDeMoeda.format(despesas)
        binding.tvPeriodoSaldo.text = "Saldo: ${formatadorDeMoeda.format(saldo)}"

        if (saldo < BigDecimal.ZERO) {
            binding.tvPeriodoSaldo.setTextColor(ContextCompat.getColor(this, R.color.vermelho_despesa))
        } else {
            val typedValue = android.util.TypedValue()
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
            binding.tvPeriodoSaldo.setTextColor(typedValue.data)
        }

        // A atualização dos gráficos também fica aqui dentro
        atualizarGraficoDeBarras(receitas.toFloat(), despesas.toFloat())
        atualizarGraficoDePizza(despesasPorCategoria)
    }



    private fun animarConteudo(view: View, executarAposAnimacao: () -> Unit) {
        view.animate()
            .alpha(0f) // Esmaece para transparente
            .setDuration(150)
            .withEndAction {
                executarAposAnimacao() // Executa a atualização dos dados
                view.animate()
                    .alpha(1f) // Reaparece
                    .setDuration(150)
                    .start()
            }
            .start()
    }
    // Funções atualizarGraficoDeBarras e atualizarGraficoDePizza (sem alterações)
    private fun atualizarGraficoDeBarras(totalReceitas: Float, totalDespesas: Float) {
        val barChart = binding.barChartReceitasDespesas
        if (totalReceitas == 0f && totalDespesas == 0f) {
            barChart.clear()
            barChart.invalidate()
            return
        }
        val receitaEntry = BarEntry(0f, totalReceitas)
        val despesaEntry = BarEntry(1f, totalDespesas)

        val receitaDataSet = BarDataSet(listOf(receitaEntry), "Receita").apply {
            color = ContextCompat.getColor(this@TotalizadoresActivity, R.color.verde_receita)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }
        val despesaDataSet = BarDataSet(listOf(despesaEntry), "Despesa").apply {
            color = ContextCompat.getColor(this@TotalizadoresActivity, R.color.vermelho_despesa)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }
        val barData = BarData(receitaDataSet, despesaDataSet)

        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true
        barChart.legend.textSize = 12f
        barChart.xAxis.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun atualizarGraficoDePizza(despesasPorCategoria: Map<String, BigDecimal>) {
        val pieChart = binding.pieChartComposicaoDespesas
        if (despesasPorCategoria.isEmpty()) {
            pieChart.clear()
            pieChart.centerText = "Sem despesas no período"
            pieChart.invalidate()
            return
        }

        val entries = despesasPorCategoria.map { (categoria, valor) ->
            PieEntry(valor.toFloat(), categoria)
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.VORDIPLOM_COLORS.toList()
            valueFormatter = PercentFormatter(pieChart)
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.centerText = "Despesas"
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.animateY(1000)
        pieChart.invalidate()
    }
}
