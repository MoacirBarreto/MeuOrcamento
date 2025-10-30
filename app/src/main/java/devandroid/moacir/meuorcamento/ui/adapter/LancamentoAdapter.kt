package devandroid.moacir.meuorcamento.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import devandroid.moacir.meuorcamento.R
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.model.Natureza
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// 1. Adicione o callback no construtor do Adapter.
class LancamentoAdapter(
    private val onClick: (Lancamento) -> Unit,
    private val onLongClick: (Lancamento) -> Unit
) : ListAdapter<Lancamento, LancamentoAdapter.LancamentoViewHolder>(LancamentoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LancamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lancamento, parent, false)
        // CORREÇÃO: Passe ambas as funções para o construtor do ViewHolder.
        return LancamentoViewHolder(view, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: LancamentoViewHolder, position: Int) {
        val lancamento = getItem(position)
        holder.bind(lancamento)
    }

    // 3. ViewHolder modificado para aceitar e usar o callback
    class LancamentoViewHolder(
        itemView: View,
        private val onClick: (Lancamento) -> Unit,
        private val onLongClick: (Lancamento) -> Unit // Aceita o callback aqui
    ) : RecyclerView.ViewHolder(itemView) {

        // Encontra as Views no layout do item
        private val descricaoTextView: TextView = itemView.findViewById(R.id.textViewDescricao)
        private val valorTextView: TextView = itemView.findViewById(R.id.textViewValor)
        private val dataTextView: TextView = itemView.findViewById(R.id.textViewData)

        // Formatadores para moeda e data
        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(lancamento: Lancamento) {
            // Preenche os dados do lançamento nas Views
            descricaoTextView.text = lancamento.descricaoLancamento
            valorTextView.text = currencyFormatter.format(lancamento.valor)
            dataTextView.text = dateFormatter.format(lancamento.dataHora)

            // Muda a cor do valor com base na natureza (Receita ou Despesa)
            val color = when (lancamento.natureza) {
                Natureza.RECEITA -> ContextCompat.getColor(itemView.context, R.color.receita)
                Natureza.DESPESA -> ContextCompat.getColor(itemView.context, R.color.despesa)
            }
            valorTextView.setTextColor(color)

            // 4. Configura o listener de clique longo na view principal do item
            itemView.setOnClickListener {
                onClick(lancamento) // Chama o callback de clique simples
            }

            // Listener de clique longo (já existente)
            itemView.setOnLongClickListener {
                onLongClick(lancamento)
                true
            }
        }
    }

    /**
     * DiffCallback: O cérebro por trás do ListAdapter.
     * Ele diz ao adapter como calcular as diferenças entre a lista antiga e a nova,
     * permitindo animações eficientes e automáticas.
     */
    class LancamentoDiffCallback : DiffUtil.ItemCallback<Lancamento>() {
        override fun areItemsTheSame(oldItem: Lancamento, newItem: Lancamento): Boolean {
            // Os itens são os mesmos se o ID for igual.
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Lancamento, newItem: Lancamento): Boolean {
            // O conteúdo é o mesmo se o objeto inteiro for igual (data class faz isso por nós).
            return oldItem == newItem
        }
    }
}
