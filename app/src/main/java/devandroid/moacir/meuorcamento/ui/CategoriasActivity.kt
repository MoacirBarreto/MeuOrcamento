package devandroid.moacir.meuorcamento.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import devandroid.moacir.meuorcamento.data.AppDatabase
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository
import devandroid.moacir.meuorcamento.databinding.ActivityCategoriasBinding
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModel
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch

class CategoriasActivity : AppCompatActivity() {

    // 1. Inicialização correta do ViewBinding
    private lateinit var binding: ActivityCategoriasBinding

    // 2. Inicialização do ViewModel usando a factory
    private val mainViewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MeuOrcamentoRepository(database.lancamentoDao(), database.categoriaDao())
        MainViewModelFactory(repository)
    }

    // 3. Mapa para associar os IDs das categorias aos seus EditTexts
    private val editTextsMap: MutableMap<Long, TextInputEditText> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Infla o layout usando o binding
        binding = ActivityCategoriasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("CategoriasActivity", "onCreate: Atividade criada.")

        // Associa os IDs aos campos de texto do layout
        inicializarMapaDeViews()

        // Inicia a observação dos dados do ViewModel
        observarCategorias()

        // Configura o listener do botão de salvar
        configurarBotaoSalvar()
    }

    private fun inicializarMapaDeViews() {
        // Acessa as views através do objeto 'binding'
        editTextsMap[2L] = binding.editTextCategoria2
        editTextsMap[3L] = binding.editTextCategoria3
        editTextsMap[4L] = binding.editTextCategoria4
        editTextsMap[5L] = binding.editTextCategoria5
        editTextsMap[6L] = binding.editTextCategoria6
        Log.d("CategoriasActivity", "inicializarMapaDeViews: Mapa de EditTexts foi preenchido.")
    }

    private fun observarCategorias() {
        lifecycleScope.launch {
            // Garante que a coleta do Flow ocorra apenas quando a Activity está ativa
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d("CategoriasActivity", "observarCategorias: Iniciando a coleta do Flow de categorias.")
                mainViewModel.todasCategorias.collect { categorias ->
                    Log.d("CategoriasActivity", "observarCategorias: Novas categorias recebidas do Flow. Total: ${categorias.size}")
                    if (categorias.isNotEmpty()) {
                        preencherCampos(categorias)
                    } else {
                        Log.w("CategoriasActivity", "observarCategorias: A lista de categorias recebida está vazia.")
                    }
                }
            }
        }
    }

    private fun preencherCampos(categorias: List<Categoria>) {
        Log.d("CategoriasActivity", "preencherCampos: Tentando preencher os campos de texto.")
        editTextsMap.forEach { (id, editText) ->
            val categoriaEncontrada = categorias.find { it.id == id }
            if (categoriaEncontrada != null) {
                // Apenas atualiza o texto se for diferente, para evitar loops
                if (editText.text.toString() != categoriaEncontrada.nome) {
                    editText.setText(categoriaEncontrada.nome)
                    Log.i("CategoriasActivity", "preencherCampos: Campo para ID $id preenchido com '${categoriaEncontrada.nome}'.")
                }
            } else {
                Log.w("CategoriasActivity", "preencherCampos: Não foi encontrada categoria para o ID $id no banco de dados.")
            }
        }
    }

    private fun configurarBotaoSalvar() {
        binding.buttonSalvarCategorias.setOnClickListener {
            salvarAlteracoes()
        }
    }

    private fun salvarAlteracoes() {
        lifecycleScope.launch {
            val categoriasParaAtualizar = mutableListOf<Categoria>()

            for ((id, editText) in editTextsMap) {
                val novoNome = editText.text.toString().trim()
                if (novoNome.isBlank()) {
                    Toast.makeText(this@CategoriasActivity, "O nome da categoria não pode estar vazio.", Toast.LENGTH_SHORT).show()
                    return@launch // Aborta o salvamento
                }
                categoriasParaAtualizar.add(Categoria(id = id, nome = novoNome))
            }

            mainViewModel.atualizarCategorias(categoriasParaAtualizar)

            Toast.makeText(this@CategoriasActivity, "Categorias atualizadas com sucesso!", Toast.LENGTH_SHORT).show()
            finish() // Fecha a activity após salvar
        }
    }
}
