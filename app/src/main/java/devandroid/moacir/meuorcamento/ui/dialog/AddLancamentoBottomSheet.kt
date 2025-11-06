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
import com.google.android.material.textfield.TextInputLayout
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

    // Views
    private lateinit var editTextDescricao: EditText
    private lateinit var editTextValor: EditText
    private lateinit var radioGroupTipo: RadioGroup
    private lateinit var radioButtonReceita: RadioButton
    private lateinit var radioButtonDespesa: RadioButton
    private lateinit var autoCompleteCategoria: AutoCompleteTextView
    private lateinit var textInputLayoutCategoria: TextInputLayout // Declaração correta
    private lateinit var buttonSalvar: Button
    private lateinit var editTextData: EditText

    // Variáveis de estado
    private var dataSelecionada: LocalDate = LocalDate.now()
    private var categoriaSelecionada: Categoria? = null

    // Constantes e Formatters

    companion object {
        const val TAG = "AddLancamentoBottomSheet"
        private const val ID_CATEGORIA_RECEITA = 1L // ID fixo para a categoria "Receita"
    }
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
        observarSelecaoDeTipo()

        if (lancamentoParaEditar != null) {
            preencherDadosParaEdicao(lancamentoParaEditar)
        } else {
            // Garante que o estado inicial para um novo lançamento esteja correto
            radioButtonDespesa.isChecked = true
            atualizarVisibilidadeCategoria(false)
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
        textInputLayoutCategoria = view.findViewById(R.id.layoutCategoria)
        buttonSalvar = view.findViewById(R.id.buttonSalvar)
        editTextData = view.findViewById(R.id.editTextDataHora)
    }

    private fun observarSelecaoDeTipo() {
        radioGroupTipo.setOnCheckedChangeListener { _, checkedId ->
            val isReceita = checkedId == R.id.radioButtonReceita
            atualizarVisibilidadeCategoria(isReceita)
        }
    }

    /**
     * Centraliza a lógica de UI para quando o tipo de lançamento muda.
     */
    private fun atualizarVisibilidadeCategoria(isReceita: Boolean) {
        if (isReceita) {
            textInputLayoutCategoria.visibility = View.GONE
            autoCompleteCategoria.text = null
            categoriaSelecionada = null

            if (editTextDescricao.text.isBlank() || editTextDescricao.text.toString() == "Despesa Padrão") {
                editTextDescricao.setText("Receita")
            }
        } else {
            textInputLayoutCategoria.visibility = View.VISIBLE
            if (editTextDescricao.text.toString() == "Receita") {
                editTextDescricao.setText("")
            }
        }
    }

    private fun setupCategorySelector() {
        // Filtra a categoria "Receita" da lista de opções para despesas
        val categoriasDeDespesa = categorias.filter { it.id != ID_CATEGORIA_RECEITA }
        val nomesDasCategorias = categoriasDeDespesa.map { it.nome }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nomesDasCategorias)
        autoCompleteCategoria.setAdapter(adapter)

        autoCompleteCategoria.setOnItemClickListener { parent, _, position, _ ->
            val nomeSelecionado = parent.getItemAtPosition(position) as String
            categoriaSelecionada = categoriasDeDespesa.find { it.nome == nomeSelecionado }
        }
    }

    private fun setupClickListeners() {
        editTextData.setOnClickListener { mostrarSeletorDeData() }
        buttonSalvar.setOnClickListener { handleSave() }
    }

    private fun preencherDadosParaEdicao(lancamento: Lancamento) {
        buttonSalvar.text = getString(R.string.action_update)
        editTextDescricao.setText(lancamento.descricao)
        editTextValor.setText(lancamento.valor.toPlainString().replace("-", ""))

        if (lancamento.tipo == TipoLancamento.RECEITA) {
            radioButtonReceita.isChecked = true
            atualizarVisibilidadeCategoria(true) // Esconde a categoria
        } else {
            radioButtonDespesa.isChecked = true
            categoriaSelecionada = categorias.find { it.id == lancamento.categoriaId }
            categoriaSelecionada?.let { autoCompleteCategoria.setText(it.nome, false) }
            atualizarVisibilidadeCategoria(false) // Mostra a categoria
        }

        dataSelecionada = lancamento.dataHora
        atualizarCampoData()
    }

    private fun handleSave() {
        val descricao = editTextDescricao.text.toString().trim()
        val valorStr = editTextValor.text.toString().trim().replace(",", ".")
        val valorAbsoluto = valorStr.toBigDecimalOrNull()?.abs()

        if (descricao.isBlank()) {
            Toast.makeText(context, "A descrição é obrigatória.", Toast.LENGTH_SHORT).show()
            return
        }
        if (valorAbsoluto == null || valorAbsoluto.compareTo(BigDecimal.ZERO) == 0) {
            Toast.makeText(context, "O valor deve ser maior que zero.", Toast.LENGTH_SHORT).show()
            return
        }

        val tipo = if (radioButtonReceita.isChecked) TipoLancamento.RECEITA else TipoLancamento.DESPESA

        val idCategoriaFinal: Long
        if (tipo == TipoLancamento.RECEITA) {
            idCategoriaFinal = ID_CATEGORIA_RECEITA
        } else {
            if (categoriaSelecionada == null) {
                Toast.makeText(context, "Selecione uma categoria para a despesa.", Toast.LENGTH_SHORT).show()
                return
            }
            idCategoriaFinal = categoriaSelecionada!!.id
        }

        val valorFinal = if (tipo == TipoLancamento.DESPESA) valorAbsoluto.negate() else valorAbsoluto

        val lancamentoSalvo = lancamentoParaEditar?.copy(
            descricao = descricao,
            valor = valorFinal,
            tipo = tipo,
            categoriaId = idCategoriaFinal,
            dataHora = dataSelecionada
        ) ?: Lancamento(
            descricao = descricao,
            valor = valorFinal,
            tipo = tipo,
            categoriaId = idCategoriaFinal,
            dataHora = dataSelecionada
        )

        onSave(lancamentoSalvo)
        dismiss()
    }

    private fun mostrarSeletorDeData() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                dataSelecionada = LocalDate.of(year, month + 1, dayOfMonth)
                atualizarCampoData()
            },
            dataSelecionada.year,
            dataSelecionada.monthValue - 1,
            dataSelecionada.dayOfMonth
        ).show()
    }

    private fun atualizarCampoData() {
        editTextData.setText(dataSelecionada.format(dateFormatter))
    }
}
