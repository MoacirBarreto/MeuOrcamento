package devandroid.moacir.meuorcamento.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import devandroid.moacir.meuorcamento.R
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.model.Natureza
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// 1. Modifique o construtor para aceitar um Lançamento opcional (nullable)
class AddLancamentoBottomSheet(
    private val viewModel: MainViewModel,
    private val lancamentoParaEditar: Lancamento? = null // Se for null, é modo de criação
) : BottomSheetDialogFragment() {

    private var dataSelecionada: Long = System.currentTimeMillis()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_lancamento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Encontra as views
        val tituloTextView = view.findViewById<TextView>(R.id.tituloDialog) // Supondo que você adicione um ID ao título
        val editTextDescricao = view.findViewById<TextInputEditText>(R.id.editTextDescricao)
        val editTextValor = view.findViewById<TextInputEditText>(R.id.editTextValor)
        val editTextData = view.findViewById<TextInputEditText>(R.id.editTextData)
        val chipGroupNatureza = view.findViewById<ChipGroup>(R.id.chipGroupNatureza)
        val buttonSalvar = view.findViewById<Button>(R.id.buttonSalvar)

        // 2. Verifica se está em modo de edição e pré-preenche os campos
        if (lancamentoParaEditar != null) {
            tituloTextView.text = "Editar Lançamento"
            buttonSalvar.text = "Atualizar Lançamento"
            editTextDescricao.setText(lancamentoParaEditar.descricaoLancamento)
            editTextValor.setText(lancamentoParaEditar.valor.toPlainString()) // toPlainString() evita notação científica
            dataSelecionada = lancamentoParaEditar.dataHora
            if (lancamentoParaEditar.natureza == Natureza.RECEITA) {
                chipGroupNatureza.check(R.id.chipReceita)
            } else {
                chipGroupNatureza.check(R.id.chipDespesa)
            }
        } else {
            // Modo de criação: Define um valor padrão
            chipGroupNatureza.check(R.id.chipReceita)
        }

        editTextData.setText(dateFormatter.format(dataSelecionada))
        editTextData.setOnClickListener { showDatePicker() }

        buttonSalvar.setOnClickListener {
            // A lógica de validação continua a mesma
            val descricao = editTextDescricao.text.toString()
            val valorStr = editTextValor.text.toString().replace(',', '.')
            val naturezaSelecionadaId = chipGroupNatureza.checkedChipId

            if (descricao.isBlank() || valorStr.isBlank() || naturezaSelecionadaId == View.NO_ID) {
                Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valor = valorStr.toBigDecimalOrNull()
            if (valor == null) {
                Toast.makeText(context, "Valor inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val natureza = if (naturezaSelecionadaId == R.id.chipReceita) Natureza.RECEITA else Natureza.DESPESA

            // 3. Lógica para decidir se vai ATUALIZAR ou ADICIONAR
            if (lancamentoParaEditar != null) {
                // Modo Edição: cria um novo objeto com o ID original e chama o ViewModel para atualizar
                val lancamentoAtualizado = lancamentoParaEditar.copy(
                    descricaoLancamento = descricao,
                    valor = valor,
                    dataHora = dataSelecionada,
                    natureza = natureza
                    // categoriaId permanece a mesma do lançamento original
                )
                viewModel.atualizarLancamento(lancamentoAtualizado)
            } else {
                // Modo Criação: chama o ViewModel para adicionar um novo
                viewModel.adicionarLancamento(
                    descricao = descricao,
                    valor = valor,
                    categoriaId = 1, // Categoria de exemplo
                    natureza = natureza,
                    data = dataSelecionada
                )
            }
            dismiss() // Fecha o BottomSheet
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecione a data")
            .setSelection(dataSelecionada)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val timeZone = TimeZone.getDefault()
            val offset = timeZone.getOffset(selection)
            dataSelecionada = selection + offset
            view?.findViewById<TextInputEditText>(R.id.editTextData)?.setText(dateFormatter.format(dataSelecionada))
        }

        datePicker.show(childFragmentManager, "DatePicker")
    }
}
