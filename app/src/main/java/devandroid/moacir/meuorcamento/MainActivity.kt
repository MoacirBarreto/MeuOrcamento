package devandroid.moacir.meuorcamento

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import devandroid.moacir.meuorcamento.data.AppDatabase
import devandroid.moacir.meuorcamento.data.model.Categoria
import devandroid.moacir.meuorcamento.data.model.LancamentoComCategoria
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.repository.MeuOrcamentoRepository
import devandroid.moacir.meuorcamento.databinding.ActivityMainBinding
import devandroid.moacir.meuorcamento.ui.CategoriasActivity
import devandroid.moacir.meuorcamento.ui.adapter.LancamentoAdapter
import devandroid.moacir.meuorcamento.ui.dialog.AddLancamentoBottomSheet
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModel
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MeuOrcamentoRepository(database.lancamentoDao(), database.categoriaDao())
        MainViewModelFactory(repository)
    }

    private val adapter by lazy {
        LancamentoAdapter(
            context = this, // Passa o contexto para o adapter usar nas cores
            onClick = { lancamentoComCategoria ->
                // ✅ CORREÇÃO: Acessa a propriedade 'lancamento' dentro do objeto para edição
                abrirBottomSheet(lancamentoComCategoria.lancamento)
            },
            onLongClick = { /* Ação de clique longo pode ser implementada aqui */ }
        )
    }

    // Propriedade para manter a lista de categorias sempre atualizada,
    // garantindo que o BottomSheet sempre receba os dados mais recentes.
    private var listaDeCategoriasAtual: List<Categoria> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ CORREÇÃO: A inicialização do binding DEVE vir primeiro.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurações iniciais da UI (agora que o 'binding' é seguro de usar)
        setSupportActionBar(binding.toolbar)
        configurarRecyclerView()
        configurarFab()
        observarDados()
    }

    // --- Configuração do Menu da Toolbar ---

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_categories -> {
                startActivity(Intent(this, CategoriasActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --- Configurações da UI ---

    private fun configurarRecyclerView() {
        binding.recyclerViewLancamentos.layoutManager = LinearLayoutManager(this)
        // ✅ CORREÇÃO: Usa o adapter inicializado via 'lazy'
        binding.recyclerViewLancamentos.adapter = adapter
    }

    private fun configurarFab() {
        binding.fabAdicionarLancamento.setOnClickListener {
            // Ao adicionar um novo lançamento, não passamos um 'lancamento',
            // e a lista de categorias já está disponível na propriedade da Activity.
            abrirBottomSheet()
        }
    }

    /**
     * Centraliza a observação de todos os fluxos de dados do ViewModel.
     * Garante que a coleta só aconteça quando a UI estiver visível (STARTED).
     */
    private fun observarDados() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observa o fluxo de lançamentos com categoria e atualiza o adapter.
                // 'collectLatest' cancela o processamento anterior se um novo dado chegar.
                launch {
                    mainViewModel.todosLancamentos.collectLatest { lancamentos ->
                        adapter.submitList(lancamentos)
                    }
                }

                // Observa o fluxo de categorias e atualiza a propriedade da Activity.
                // Esta é a chave para manter a lista sempre sincronizada para o BottomSheet.
                launch {
                    mainViewModel.todasCategorias.collect { categorias ->
                        listaDeCategoriasAtual = categorias
                    }
                }
            }
        }
    }

    /**
     * Abre o BottomSheet para adicionar ou editar um lançamento.
     * Usa a `listaDeCategoriasAtual`, que está sempre sincronizada com o BD.
     */
    private fun abrirBottomSheet(lancamento: Lancamento? = null) {
        val bottomSheet = AddLancamentoBottomSheet(
            lancamentoParaEditar = lancamento,
            categorias = this.listaDeCategoriasAtual, // Sempre usa a lista mais recente
            onSave = { lancamentoProcessado ->
                mainViewModel.salvarLancamento(lancamentoProcessado)
            }
        )
        bottomSheet.show(supportFragmentManager, "AddLancamentoBottomSheet")
    }
}
