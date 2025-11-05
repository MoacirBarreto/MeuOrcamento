package devandroid.moacir.meuorcamento.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import devandroid.moacir.meuorcamento.R
import devandroid.moacir.meuorcamento.data.AppDatabase
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository
import devandroid.moacir.meuorcamento.ui.viewmodel.CategoriasViewModel
import devandroid.moacir.meuorcamento.ui.viewmodel.CategoriasViewModelFactory
import kotlinx.coroutines.launch

class CategoriasActivity : AppCompatActivity() {

    // ✅ CORREÇÃO: Agora usa o ViewModel e a Factory corretos.
    private val viewModel: CategoriasViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MeuOrcamentoRepository(database.lancamentoDao(), database.categoriaDao())
        CategoriasViewModelFactory(repository)
    }

    // Lista para guardar a referência dos campos de texto da UI.
    private lateinit var editTexts: List<TextInputEditText>

    // Lista para armazenar o estado original das categorias carregadas do banco.
    private lateinit var categoriasAtuais: List<Categoria>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Editar Categorias"

        inicializarViews()
        observarCategorias()
        configurarBotaoSalvar()
    }

    private fun inicializarViews() {
        editTexts = listOf(
            findViewById(R.id.editTextCategoria1),
            findViewById(R.id.editTextCategoria2),
            findViewById(R.id.editTextCategoria3),
            findViewById(R.id.editTextCategoria4),
            findViewById(R.id.editTextCategoria5)
        )
    }

    private fun observarCategorias() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // ✅ CORREÇÃO: Observa a propriedade 'categorias' do novo ViewModel.
                viewModel.categorias.collect { categorias ->
                    if (categorias.isNotEmpty()) {
                        categoriasAtuais = categorias
                        preencherCampos(categorias)
                    }
                }
            }
        }
    }

    private fun preencherCampos(categorias: List<Categoria>) {
        categorias.forEachIndexed { index, categoria ->
            if (index < editTexts.size) {
                editTexts[index].setText(categoria.nome)
            }
        }
    }

    private fun configurarBotaoSalvar() {
        val btnSalvar: Button = findViewById(R.id.buttonSalvarCategorias)
        btnSalvar.setOnClickListener {
            salvarAlteracoes()
        }
    }

    private fun salvarAlteracoes() {
        if (!::categoriasAtuais.isInitialized) {
            Toast.makeText(this, "Aguarde o carregamento das categorias.", Toast.LENGTH_SHORT).show()
            return
        }

        val nomesAtualizados = editTexts.map { it.text.toString().trim() }

        if (nomesAtualizados.any { it.isBlank() }) {
            Toast.makeText(this, "Nenhum nome de categoria pode ficar em branco.", Toast.LENGTH_SHORT).show()
            return
        }

        val categoriasParaSalvar = categoriasAtuais.mapIndexed { index, categoriaOriginal ->
            categoriaOriginal.copy(nome = nomesAtualizados[index])
        }

        // ✅ CORREÇÃO: A chamada agora é feita no CategoriasViewModel, que sabe como salvar.
        viewModel.atualizarListaDeCategorias(categoriasParaSalvar)

        Toast.makeText(this, "Categorias salvas com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
