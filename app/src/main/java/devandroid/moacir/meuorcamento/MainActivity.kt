package devandroid.moacir.meuorcamento

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import devandroid.moacir.meuorcamento.data.model.Natureza
import devandroid.moacir.meuorcamento.ui.adapter.LancamentoAdapter
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModel
import devandroid.moacir.meuorcamento.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import com.google.android.material.floatingactionbutton.FloatingActionButton
import devandroid.moacir.meuorcamento.ui.dialog.AddLancamentoBottomSheet

class MainActivity : AppCompatActivity() {

    // 1. Obter a instância do ViewModel (como antes)
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory((application as MeuOrcamentoApplication).repository)
    }

    // 2. Declarar o Adapter que criamos
    private lateinit var lancamentoAdapter: LancamentoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fab: FloatingActionButton = findViewById(R.id.fabAdicionarLancamento)
        fab.setOnClickListener {
            // Cria uma instância do nosso BottomSheet e o exibe
            val bottomSheet = AddLancamentoBottomSheet(mainViewModel)
            bottomSheet.show(supportFragmentManager, "AddLancamentoBottomSheet")
        }

        setupRecyclerView()
        observeLancamentos()
        adicionarDadosDeTesteSeNecessario()


    }

    private fun setupRecyclerView() {
        // Encontra o RecyclerView no layout
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewLancamentos)

        // Instancia o nosso adapter
        lancamentoAdapter = LancamentoAdapter()

        // Define o adapter e o layout manager para o RecyclerView
        recyclerView.adapter = lancamentoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeLancamentos() {
        // Lança uma coroutine que respeita o ciclo de vida da Activity
        lifecycleScope.launch {
            // 'collect' fica escutando as emissões do StateFlow
            mainViewModel.todosLancamentos.collect { lancamentos ->
                // Quando uma nova lista de lançamentos chegar, submeta-a ao adapter.
                // O ListAdapter cuidará de calcular as diferenças e animar a lista.
                lancamentoAdapter.submitList(lancamentos)
                Log.d("MainActivity", "Adapter atualizado com ${lancamentos.size} itens.")
            }
        }
    }

    // Código para popular o banco com dados de exemplo.
    // Alterado para adicionar dados apenas se a lista estiver vazia.
    private fun adicionarDadosDeTesteSeNecessario() {
        lifecycleScope.launch {
            mainViewModel.todosLancamentos.collect { lancamentos ->
                if (lancamentos.isEmpty()) {
                    Log.d("MainActivity", "Banco de dados vazio. Adicionando dados de teste.")
                    // Adiciona uma categoria "Salário"
                    mainViewModel.adicionarCategoria("Salário")
                    mainViewModel.adicionarCategoria("Alimentação")

                    // Adiciona um lançamento de teste
                    mainViewModel.adicionarLancamento(
                        descricao = "Salário de Outubro",
                        valor = 3000.0.toBigDecimal(),
                        categoriaId = 1, // Assumindo que o ID da categoria "Salário" será 1
                        natureza = Natureza.RECEITA
                    )

                    mainViewModel.adicionarLancamento(
                        descricao = "Compras no mercado",
                        valor = 250.55.toBigDecimal(),
                        categoriaId = 2, // Assumindo que o ID da categoria "Alimentação" será 2
                        natureza = Natureza.DESPESA
                    )
                }
                // Usamos 'return@collect' implicitamente para parar de coletar após a verificação,
                // mas a coroutine principal (observeLancamentos) continuará funcionando.
            }
        }
    }
}
