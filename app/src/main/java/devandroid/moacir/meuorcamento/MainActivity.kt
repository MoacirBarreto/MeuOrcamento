package devandroid.moacir.meuorcamento

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.dismiss
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import devandroid.moacir.meuorcamento.data.AppDatabase
import devandroid.moacir.meuorcamento.data.model.Categoria
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

    private val lancamentoAdapter by lazy {
        LancamentoAdapter(
            context = this,
            onClick = { lancamentoComCategoria ->
                // Acessa a propriedade 'lancamento' para passar ao BottomSheet para edição
                abrirBottomSheet(lancamentoComCategoria.lancamento)
            },
            onLongClick = { lancamentoComCategoria ->
                // Extrai o objeto 'lancamento' para o diálogo de exclusão
                mostrarDialogoDeExclusao(lancamentoComCategoria.lancamento)
            }
        )
    }

    // Propriedade para manter a lista de categorias sempre atualizada,
    // garantindo que o BottomSheet sempre tenha os dados mais recentes.
    private var listaDeCategoriasAtual: List<Categoria> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarUI()
        observarDados()
    }

    /**
     * Otimização: Agrupa todas as configurações de UI em um único método para maior clareza.
     */
    private fun configurarUI() {
        setSupportActionBar(binding.toolbar)
        configurarRecyclerView()
        configurarFab()
    }

    // --- Configuração do Menu da Toolbar ---

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Otimização: Usar 'return when' deixa o código mais conciso.
        return when (item.itemId) {
            R.id.action_edit_categorias -> {
                startActivity(Intent(this, CategoriasActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --- Configurações dos Componentes da UI ---

    private fun configurarRecyclerView() {
        binding.recyclerViewLancamentos.apply {
            // Otimização: 'apply' é usado para configurar múltiplas propriedades do mesmo objeto.
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = lancamentoAdapter // Usa o adapter inicializado via 'lazy'
        }
    }

    private fun configurarFab() {
        binding.fabAdicionarLancamento.setOnClickListener {
            // Ao adicionar um novo lançamento, não passamos um 'lancamento',
            // e a lista de categorias já está disponível na propriedade da Activity.
            abrirBottomSheet()
        }
    }

    /**
     * Otimização: Nome do método mais descritivo.
     * Mostra um diálogo de confirmação antes de excluir um lançamento.
     */
    private fun mostrarDialogoDeExclusao(lancamento: Lancamento) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirmar_exclusao_titulo)
            .setMessage(getString(R.string.confirmar_exclusao_mensagem, lancamento.descricao))
            .setNegativeButton(R.string.cancelar) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.excluir) { dialog, _ ->
                mainViewModel.excluirLancamento(lancamento)
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Centraliza a observação dos fluxos de dados do ViewModel.
     * Garante que a coleta só aconteça quando a UI estiver visível (STARTED).
     */
    private fun observarDados() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observa o fluxo de lançamentos com categoria e atualiza o adapter.
                launch {
                    mainViewModel.todosLancamentos.collectLatest(lancamentoAdapter::submitList)
                }

                // Observa o fluxo de categorias e atualiza a propriedade local.
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
        AddLancamentoBottomSheet(
            lancamentoParaEditar = lancamento,
            categorias = this.listaDeCategoriasAtual, // Sempre usa a lista mais recente
            onSave = mainViewModel::salvarLancamento // Otimização: Referência de função
        ).show(supportFragmentManager, AddLancamentoBottomSheet.TAG)
    }
}
