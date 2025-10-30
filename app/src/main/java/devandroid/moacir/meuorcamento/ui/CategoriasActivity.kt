package devandroid.moacir.meuorcamento.ui

import android.os.Bundle
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager // Import necessário
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import devandroid.moacir.meuorcamento.MeuOrcamentoApplication
import devandroid.moacir.meuorcamento.R
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.ui.adapter.CategoriaAdapter
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModel
import devandroid.moacir.meuorcamento.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class CategoriasActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory((application as MeuOrcamentoApplication).repository)
    }
    private lateinit var categoriaAdapter: CategoriaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarCategorias)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Gerenciar Categorias"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        observeCategorias()

        findViewById<FloatingActionButton>(R.id.fabAdicionarCategoria).setOnClickListener {
            mostrarDialogoDeCategoria(null)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        categoriaAdapter = CategoriaAdapter(
            onClick = { categoria -> mostrarDialogoDeCategoria(categoria) },
            onLongClick = { categoria -> mostrarDialogoDeExclusao(categoria) }
        )
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCategorias)
        recyclerView.adapter = categoriaAdapter
        // CORREÇÃO APLICADA AQUI:
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeCategorias() {
        lifecycleScope.launch {
            mainViewModel.todasCategorias.collect { categorias ->
                categoriaAdapter.submitList(categorias)
            }
        }
    }

    private fun mostrarDialogoDeCategoria(categoria: Categoria?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_text, null)
        val editText = dialogView.findViewById<EditText>(R.id.editText)
        val title = if (categoria == null) "Nova Categoria" else "Editar Categoria"
        if (categoria != null) {
            editText.setText(categoria.nome)
        }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = editText.text.toString()
                if (nome.isNotBlank()) {
                    if (categoria == null) {
                        mainViewModel.adicionarCategoria(nome)
                    } else {
                        mainViewModel.atualizarCategoria(categoria.copy(nome = nome))
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoDeExclusao(categoria: Categoria) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Categoria")
            .setMessage("Tem certeza que deseja excluir a categoria \"${categoria.nome}\"?\n\nAtenção: Isso pode causar problemas se houver lançamentos associados a ela.")
            .setPositiveButton("Excluir") { _, _ -> mainViewModel.deletarCategoria(categoria) }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
