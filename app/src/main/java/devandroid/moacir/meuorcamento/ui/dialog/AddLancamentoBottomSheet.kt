package devandroid.moacir.meuorcamento.ui.dialog

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import devandroid.moacir.meuorcamento.R
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.model.TipoLancamento
import devandroid.moacir.meuorcamento.databinding.BottomSheetAddLancamentoBinding
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddLancamentoBottomSheet(
    private val lancamentoParaEditar: Lancamento? = null,
    private val categorias: List<Categoria>,
    private val onSave: (Lancamento) -> Unit
) : BottomSheetDialogFragment() { // O construtor da superclasse é chamado aqui

    override fun getTheme(): Int {
        return R.style.Theme_MeuOrcamento_BottomSheetDialog
    }

    // Bloco init para aplicar o estilo que corrige o problema do teclado
    init {
        setStyle(STYLE_NORMAL, R.style.App_BottomSheet_Modal)
    }

    private var _binding: BottomSheetAddLancamentoBinding? = null
    private val binding get() = _binding!!

    private var dataSelecionada: LocalDate = LocalDate.now()
    private var categoriaSelecionada: Categoria? = null

    private val dateFormatter: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    private val categoriasDeDespesa by lazy { categorias.filter { it.id != ID_CATEGORIA_RECEITA } }

    companion object {
        const val TAG = "AddLancamentoBottomSheet"
        private const val ID_CATEGORIA_RECEITA = 1L // ID fixo para a categoria "Receita"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddLancamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // A linha obsoleta foi removida daqui, pois a correção agora é via estilo.
        configurarSeletores()
        configurarListeners()
        preencherDadosIniciais()
    }

    private fun configurarSeletores() {
        val nomesDasCategorias = categoriasDeDespesa.map { it.nome }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            nomesDasCategorias
        )
        binding.autoCompleteCategoria.setAdapter(adapter)
    }

    private fun configurarListeners() {
        binding.radioGroupTipo.setOnCheckedChangeListener { _, checkedId ->
            val isReceita = checkedId == R.id.radioButtonReceita
            atualizarVisibilidadeCategoria(isReceita)
        }

        binding.autoCompleteCategoria.setOnItemClickListener { parent, _, position, _ ->
            val nomeSelecionado = parent.getItemAtPosition(position) as String
            categoriaSelecionada = categoriasDeDespesa.find { it.nome == nomeSelecionado }

            // *** LÓGICA ADICIONADA ***
            // Copia o nome da categoria para a descrição apenas se o campo estiver vazio.
            // Isso evita apagar uma descrição personalizada que o usuário já digitou.
            if (binding.editTextDescricao.text.isNullOrBlank()) {
                binding.editTextDescricao.setText(nomeSelecionado)
            }
        }

        binding.editTextDataHora.setOnClickListener { mostrarSeletorDeData() }
        binding.buttonSalvar.setOnClickListener { salvarLancamento() }
    }

    private fun preencherDadosIniciais() {
        lancamentoParaEditar?.let { lancamento ->
            // Modo Edição
            binding.buttonSalvar.text = getString(R.string.action_update)
            binding.editTextDescricao.setText(lancamento.descricao)
            binding.editTextValor.setText(lancamento.valor.abs().toPlainString())
            dataSelecionada = lancamento.dataHora

            if (lancamento.tipo == TipoLancamento.RECEITA) {
                binding.radioButtonReceita.isChecked = true
            } else {
                binding.radioButtonDespesa.isChecked = true
                categoriaSelecionada = categoriasDeDespesa.find { it.id == lancamento.categoriaId }
                categoriaSelecionada?.let { binding.autoCompleteCategoria.setText(it.nome, false) }
            }
        } ?: run {
            // Modo Novo Lançamento
            binding.radioButtonDespesa.isChecked = true
        }
        atualizarCampoData()
    }

    private fun atualizarVisibilidadeCategoria(isReceita: Boolean) {
        binding.layoutCategoria.visibility = if (isReceita) View.GONE else View.VISIBLE

        // Limpa a descrição sempre que o tipo é trocado
        binding.editTextDescricao.text?.clear()

        if (isReceita) {
            // Se for receita, esconde o campo de categoria e define a descrição padrão
            binding.autoCompleteCategoria.setText("", false)
            categoriaSelecionada = null
            binding.editTextDescricao.setText(getString(R.string.default_receita_description))
        }
        // Se for despesa (o 'else'), a descrição ficará vazia, pronta para ser preenchida pela seleção de categoria.
    }

    private fun salvarLancamento() {
        val descricao = binding.editTextDescricao.text.toString().trim()
        val valorStr = binding.editTextValor.text.toString().trim().replace(",", ".")
        val valorAbsoluto = valorStr.toBigDecimalOrNull()?.abs()
        val tipo =
            if (binding.radioButtonReceita.isChecked) TipoLancamento.RECEITA else TipoLancamento.DESPESA

        if (!validarInputs(descricao, valorAbsoluto, tipo)) return

        val idCategoriaFinal = if (tipo == TipoLancamento.RECEITA) {
            ID_CATEGORIA_RECEITA
        } else {
            categoriaSelecionada!!.id
        }

        val valorFinal = if (tipo == TipoLancamento.DESPESA) {
            valorAbsoluto!!.negate()
        } else {
            valorAbsoluto!!
        }

        val lancamentoSalvo = lancamentoParaEditar?.apply {
            // Modo Edição: Atualiza o objeto existente
            this.descricao = descricao
            this.valor = valorFinal
            this.tipo = tipo
            this.categoriaId = idCategoriaFinal
            this.dataHora = dataSelecionada
        } ?:
        // Modo Novo Lançamento: Cria um novo objeto
        Lancamento(
            descricao = descricao,
            valor = valorFinal,
            tipo = tipo,
            categoriaId = idCategoriaFinal,
            dataHora = dataSelecionada
        )

        onSave(lancamentoSalvo)
        dismiss()
    }

    private fun validarInputs(
        descricao: String,
        valor: BigDecimal?,
        tipo: TipoLancamento
    ): Boolean {
        binding.layoutDescricao.error = if (descricao.isBlank()) {
            getString(R.string.add_lancamento_error_descricao_vazia)
        } else {
            null
        }
        binding.layoutValor.error = if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
            getString(R.string.add_lancamento_error_valor_invalido)
        } else {
            null
        }

        binding.layoutCategoria.error =
            if (tipo == TipoLancamento.DESPESA && categoriaSelecionada == null) {
                getString(R.string.add_lancamento_error_categoria_vazia)
            } else {
                null
            }

        return binding.layoutDescricao.error == null &&
                binding.layoutValor.error == null &&
                binding.layoutCategoria.error == null
    }

    private fun mostrarSeletorDeData() {
        val dataAtual = dataSelecionada
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                dataSelecionada = LocalDate.of(year, month + 1, dayOfMonth)
                atualizarCampoData()
            },
            dataAtual.year,
            dataAtual.monthValue - 1,
            dataAtual.dayOfMonth
        ).show()
    }

    private fun atualizarCampoData() {
        binding.editTextDataHora.setText(dataSelecionada.format(dateFormatter))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
