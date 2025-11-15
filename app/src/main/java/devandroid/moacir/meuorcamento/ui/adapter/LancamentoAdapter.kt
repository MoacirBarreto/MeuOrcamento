package devandroid.moacir.meuorcamento.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import devandroid.moacir.meuorcamento.R
import devandroid.moacir.meuorcamento.data.model.LancamentoComCategoria
import devandroid.moacir.meuorcamento.data.model.TipoLancamento
import devandroid.moacir.meuorcamento.databinding.ItemLancamentoBinding
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

// Lembre-se de passar o Context no construtor do adapter
class LancamentoAdapter(
    private val context: Context,
    private val onClick: (LancamentoComCategoria) -> Unit,
    private val onLongClick: (LancamentoComCategoria) -> Unit
) : ListAdapter<LancamentoComCategoria, LancamentoAdapter.LancamentoViewHolder>(LancamentoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LancamentoViewHolder {
        val binding = ItemLancamentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LancamentoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LancamentoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // O ViewHolder agora precisa do Context para resolver as cores
    inner class LancamentoViewHolder(private val binding: ItemLancamentoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lancamentoComCategoria: LancamentoComCategoria) {
            // Acesse o objeto 'lancamento' dentro de 'lancamentoComCategoria'
            val lancamento = lancamentoComCategoria.lancamento

            binding.textViewDescricao.text = lancamento.descricao

            // --- LÓGICA DE CORES AQUI ---
            val cor: Int
            if (lancamento.tipo == TipoLancamento.RECEITA) {
                // Define a cor verde para receitas
                cor = ContextCompat.getColor(context, R.color.app_verde_receita)
            } else {
                // Define a cor vermelha para despesas
                cor = ContextCompat.getColor(context, R.color.app_vermelho_despesa)
            }
            // Aplica a cor ao TextView do valor
            binding.textViewValor.setTextColor(cor)


            // Formatação do valor para moeda local (BRL)
            val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            binding.textViewValor.text = formatadorMoeda.format(lancamento.valor)

            // Formatação da data
            val formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            binding.textViewData.text = runCatching {
                formatadorData.format(lancamento.dataHora)
            }.getOrDefault("Data inválida")

            // Você também pode querer exibir o nome da categoria, se desejar:
            binding.textViewCategoria.text = lancamentoComCategoria.categoria.nome
            // --- ✅ IMPLEMENTAÇÃO DOS CLIQUES ---
            // Configura o clique normal no item
            binding.root.setOnClickListener {
                onClick(lancamentoComCategoria)
            }

            // Configura o clique longo no item
            binding.root.setOnLongClickListener {
                onLongClick(lancamentoComCategoria)
                true // Retorna 'true' para indicar que o evento foi consumido e não deve propagar.
            }
        }
    }


    // Classe auxiliar para calcular a diferença entre listas e otimizar o RecyclerView
    class LancamentoDiffCallback : DiffUtil.ItemCallback<LancamentoComCategoria>() {
        override fun areItemsTheSame(oldItem: LancamentoComCategoria, newItem: LancamentoComCategoria): Boolean {
            return oldItem.lancamento.id == newItem.lancamento.id
        }

        override fun areContentsTheSame(oldItem: LancamentoComCategoria, newItem: LancamentoComCategoria): Boolean {
            return oldItem == newItem
        }
    }
}
