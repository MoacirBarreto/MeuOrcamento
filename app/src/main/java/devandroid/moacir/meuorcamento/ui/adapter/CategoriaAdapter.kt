package devandroid.moacir.meuorcamento.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import devandroid.moacir.meuorcamento.R
import devandroid.moacir.meuorcamento.data.model.Categoria

class CategoriaAdapter(
    private val onClick: (Categoria) -> Unit,
    private val onLongClick: (Categoria) -> Unit
) : ListAdapter<Categoria, CategoriaAdapter.CategoriaViewHolder>(CategoriaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return CategoriaViewHolder(view, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoriaViewHolder(
        itemView: View,
        private val onClick: (Categoria) -> Unit,
        private val onLongClick: (Categoria) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nomeTextView: TextView = itemView.findViewById(R.id.textViewNomeCategoria)

        fun bind(categoria: Categoria) {
            nomeTextView.text = categoria.nome

            itemView.setOnClickListener { onClick(categoria) }
            itemView.setOnLongClickListener {
                onLongClick(categoria)
                true
            }
        }
    }

    class CategoriaDiffCallback : DiffUtil.ItemCallback<Categoria>() {
        override fun areItemsTheSame(oldItem: Categoria, newItem: Categoria) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Categoria, newItem: Categoria) = oldItem == newItem
    }
}
