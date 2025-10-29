package devandroid.moacir.meuorcamento.ui.adapter

import android.graphics.Color
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
import kotlin.text.format

class LancamentoAdapter : ListAdapter<Lancamento, LancamentoAdapter.LancamentoViewHolder>(LancamentoDiffCallback()) {

    /**
     * ViewHolder: Contém as referências para as Views de cada item (item_lancamento.xml).
     * Isso evita chamadas repetitivas de findViewById(), otimizando a performance.
     */
    class LancamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descricaoTextView: TextView = itemView.findViewById(R.id.textViewDescricao)
        private val valorTextView: TextView = itemView.findViewById(R.id.textViewValor)
        private val dataTextView: TextView = itemView.findViewById(R.id.textViewData)

        // Formatação de moeda e data para reutilização
        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(lancamento: Lancamento) {
            descricaoTextView.text = lancamento.descricaoLancamento
            valorTextView.text = currencyFormatter.format(lancamento.valor.toDouble())

            // Muda a cor do valor com base na natureza (Receita ou Despesa)
            val color = when (lancamento.natureza) {
                Natureza.RECEITA -> ContextCompat.getColor(itemView.context, R.color.receita)
                Natureza.DESPESA -> ContextCompat.getColor(itemView.context, R.color.despesa)
            }
            valorTextView.setTextColor(color)

            // Formata e exibe a data
            dataTextView.text = dateFormatter.format(lancamento.dataHora)
        }
    }

    /**
     * Chamado pelo RecyclerView quando precisa criar um novo ViewHolder.
     * Ele "infla" (cria) o layout do item a partir do XML.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LancamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lancamento, parent, false)
        return LancamentoViewHolder(view)
    }

    /**
     * Chamado pelo RecyclerView para exibir os dados em uma posição específica.
     * Ele pega o objeto 'Lancamento' da posição e chama o método 'bind' do ViewHolder.
     */
    override fun onBindViewHolder(holder: LancamentoViewHolder, position: Int) {
        val lancamento = getItem(position)
        holder.bind(lancamento)
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
