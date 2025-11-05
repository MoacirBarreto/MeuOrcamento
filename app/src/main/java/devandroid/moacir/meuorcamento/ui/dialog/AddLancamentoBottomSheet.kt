package devandroid.moacir.meuorcamento.ui.dialog

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import devandroid.moacir.meuorcamento.R
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.model.TipoLancamento
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddLancamentoBottomSheet(
    private val lancamentoParaEditar: Lancamento? = null,
    private val categorias: List<Categoria>,
    private val onSave: (Lancamento) -> Unit
) : BottomSheetDialogFragment() {

    // Views do Layout
    private lateinit var editTextDescricao: EditText
    private lateinit var editTextValor: EditText
    private lateinit var radioGroupTipo: RadioGroup
    private lateinit var radioButtonReceita: RadioButton
    private lateinit var radioButtonDespesa: RadioButton
    private lateinit var autoCompleteCategoria: AutoCompleteTextView
    private lateinit var buttonSalvar: Button
    private lateinit var editTextData: EditText // Nome atualizado para clareza

    // Variável de estado para a data, usando apenas LocalDate
    private var dataSelecionada: LocalDate = LocalDate.now()
    private var categoriaSelecionada: Categoria? = null

    // Formatter para ser usado em múltiplos locais
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_lancamento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializarViews(view)
        setupCategorySelector()
        setupClickListeners()

        if (lancamentoParaEditar != null) {
            preencherDadosParaEdicao(lancamentoParaEditar)
        } else {
            // Para um novo lançamento, define a data atual e atualiza o campo de texto
            dataSelecionada = LocalDate.now()
            atualizarCampoData()
        }
    }

    private fun inicializarViews(view: View) {
        editTextDescricao = view.findViewById(R.id.editTextDescricao)
        editTextValor = view.findViewById(R.id.editTextValor)
        radioGroupTipo = view.findViewById(R.id.radioGroupTipo)
        radioButtonReceita = view.findViewById(R.id.radioButtonReceita)
        radioButtonDespesa = view.findViewById(R.id.radioButtonDespesa)
        autoCompleteCategoria = view.findViewById(R.id.autoCompleteCategoria)
        buttonSalvar = view.findViewById(R.id.buttonSalvar)
        // O ID no XML ainda é editTextDataHora, mas a variável é editTextData
        editTextData = view.findViewById(R.id.editTextDataHora)
    }

    private fun setupCategorySelector() {
        val nomesDasCategorias = categorias.map { it.nome }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nomesDasCategorias)
        autoCompleteCategoria.setAdapter(adapter)

        autoCompleteCategoria.setOnItemClickListener { parent, _, position, _ ->
            val nomeSelecionado = parent.getItemAtPosition(position) as String
            categoriaSelecionada = categorias.find { it.nome == nomeSelecionado }
        }
    }

    private fun setupClickListeners() {
        // O listener agora chama a função de data simplificada
        editTextData.setOnClickListener { mostrarSeletorDeData() }
        buttonSalvar.setOnClickListener { handleSave() }
    }

    /**
     * Preenche os campos do formulário para edição.
     */
    private fun preencherDadosParaEdicao(lancamento: Lancamento) {
        buttonSalvar.text = getString(R.string.action_update)
        editTextDescricao.setText(lancamento.descricao)
        editTextValor.setText(lancamento.valor.toPlainString())

        if (lancamento.tipo == TipoLancamento.RECEITA) {
            radioButtonReceita.isChecked = true
        } else {
            radioButtonDespesa.isChecked = true
        }
        categoriaSelecionada = categorias.find { it.id == lancamento.categoriaId }
        categoriaSelecionada?.let {
            autoCompleteCategoria.setText(it.nome, false)
        }

        // Define e atualiza a data a partir do objeto de edição
        dataSelecionada = lancamento.dataHora
        atualizarCampoData()
    }

    /**
     * Valida os campos e chama a função de callback onSave.
     */
    private fun handleSave() {
        val descricao = editTextDescricao.text.toString().trim()
        val valorStr = editTextValor.text.toString().trim().replace(",", ".")
        // Garante que o valor seja positivo, conforme a lógica do enum
        val valorAbsoluto = valorStr.toBigDecimalOrNull()?.abs()

        if (descricao.isBlank() || valorAbsoluto == null) {
            Toast.makeText(context, "Descrição e Valor são obrigatórios.", Toast.LENGTH_SHORT).show()
            return
        }

        if (valorAbsoluto.compareTo(BigDecimal.ZERO) == 0) {
            Toast.makeText(context, "O valor não pode ser zero.", Toast.LENGTH_SHORT).show()
            return
        }

        val tipo = when (radioGroupTipo.checkedRadioButtonId) {
            R.id.radioButtonReceita -> TipoLancamento.RECEITA
            R.id.radioButtonDespesa -> TipoLancamento.DESPESA
            else -> {
                Toast.makeText(context, "Selecione um tipo (Receita ou Despesa).", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (categoriaSelecionada == null) {
            Toast.makeText(context, "Selecione uma categoria válida.", Toast.LENGTH_SHORT).show()
            return
        }

        // Aplica o sinal negativo para despesas
        val valorFinal = if (tipo == TipoLancamento.DESPESA) valorAbsoluto.negate() else valorAbsoluto

        val lancamentoSalvo = lancamentoParaEditar?.copy(
            descricao = descricao,
            valor = valorFinal,
            tipo = tipo,
            categoriaId = categoriaSelecionada!!.id,
            dataHora = dataSelecionada // Salva o LocalDate
        ) ?: Lancamento(
            descricao = descricao,
            valor = valorFinal,
            tipo = tipo,
            categoriaId = categoriaSelecionada!!.id,
            dataHora = dataSelecionada // Salva o LocalDate
        )

        onSave(lancamentoSalvo)
        dismiss()
    }

    // --- Funções Auxiliares para Data (Simplificadas) ---

    private fun mostrarSeletorDeData() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Atualiza a data selecionada com a escolha do usuário
                dataSelecionada = LocalDate.of(year, month + 1, dayOfMonth)
                // Atualiza o campo de texto para refletir a nova data
                atualizarCampoData()
            },
            dataSelecionada.year,
            dataSelecionada.monthValue - 1, // Calendar usa mês 0-11
            dataSelecionada.dayOfMonth
        ).show()
    }

    private fun atualizarCampoData() {
        // Usa o formatter definido no topo da classe
        editTextData.setText(dataSelecionada.format(dateFormatter))
    }
}
