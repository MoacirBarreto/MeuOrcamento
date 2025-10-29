package devandroid.moacir.meuorcamento.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import devandroid.moacir.meuorcamento.R
import devandroid.moacir.meuorcamento.data.model.Natureza
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModel

class AddLancamentoBottomSheet(
    private val viewModel: MainViewModel
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout que criamos
        return inflater.inflate(R.layout.dialog_add_lancamento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTextDescricao = view.findViewById<TextInputEditText>(R.id.editTextDescricao)
        val editTextValor = view.findViewById<TextInputEditText>(R.id.editTextValor)
        val chipGroupNatureza = view.findViewById<ChipGroup>(R.id.chipGroupNatureza)
        val buttonSalvar = view.findViewById<Button>(R.id.buttonSalvar)

        // Define a Receita como padrão
        chipGroupNatureza.check(R.id.chipReceita)

        buttonSalvar.setOnClickListener {
            val descricao = editTextDescricao.text.toString()
            val valorStr = editTextValor.text.toString()
            val naturezaSelecionadaId = chipGroupNatureza.checkedChipId

            // Validação simples
            if (descricao.isBlank() || valorStr.isBlank() || naturezaSelecionadaId == View.NO_ID) {
                Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valor = valorStr.toBigDecimalOrNull()
            if (valor == null) {
                Toast.makeText(context, "Valor inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val natureza = if (naturezaSelecionadaId == R.id.chipReceita) {
                Natureza.RECEITA
            } else {
                Natureza.DESPESA
            }

            // ATENÇÃO: Por enquanto, vamos usar uma categoria fixa (ID 1).
            // No futuro, você pode adicionar um Spinner para o usuário escolher a categoria.
            viewModel.adicionarLancamento(
                descricao = descricao,
                valor = valor,
                categoriaId = 1, // Categoria de exemplo
                natureza = natureza
            )

            // Fecha o BottomSheet após salvar
            dismiss()
        }
    }
}
