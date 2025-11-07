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
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = MeuOrcamentoRepository(database.lancamentoDao(), database.categoriaDao())
        MainViewModelFactory(repository)
    }

    private val lancamentoAdapter by lazy {
        LancamentoAdapter(
            context = this,
            onClick = { lancamentoComCategoria ->
                abrirBottomSheet(lancamentoComCategoria.lancamento)
            },
            onLongClick = { lancamentoComCategoria ->
                mostrarDialogoDeExclusao(lancamentoComCategoria.lancamento)
            }
        )
    }

    // Otimização: Formatador de moeda para ser reutilizado.
    // Locale("pt", "BR") garante o formato R$ 1.234,56
    private val formatadorDeMoeda: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    }

    // Propriedade para manter a lista de categorias sempre atualizada
    private var listaDeCategoriasAtual: List<Categoria> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarUI()
        observarDados()
    }

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
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = lancamentoAdapter
        }
    }

    private fun configurarFab() {
        binding.fabAdicionarLancamento.setOnClickListener {
            abrirBottomSheet()
        }
    }

    /**
     * Mostra um diálogo de confirmação para excluir um lançamento.
     */
    private fun mostrarDialogoDeExclusao(lancamento: Lancamento) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirmar_exclusao_titulo)
            .setMessage(getString(R.string.confirmar_exclusao_mensagem, lancamento.descricao))
            .setNegativeButton(R.string.cancelar) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.excluir) { _, _ ->
                mainViewModel.excluirLancamento(lancamento)
            }
            .show()
    }

    /**
     * Centraliza a observação dos fluxos de dados do ViewModel.
     * Otimização: Coleta múltiplos flows dentro de um único repeatOnLifecycle.
     */
    private fun observarDados() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Coleta o fluxo de lançamentos
                launch {
                    mainViewModel.todosLancamentos.collectLatest(lancamentoAdapter::submitList)
                }

                // Coleta o fluxo de categorias
                launch {
                    mainViewModel.todasCategorias.collect { categorias ->
                        listaDeCategoriasAtual = categorias
                    }
                }

                // Otimização: Novo coletor para os totais
                // Coleta os totais de receitas e despesas
                launch {
                    mainViewModel.totais.collect { totais ->
                        binding.tvTotalReceitas.text =
                            formatadorDeMoeda.format(totais.totalReceitas)
                        binding.tvTotalDespesas.text =
                            formatadorDeMoeda.format(totais.totalDespesas.abs())
                    }
                }

                // Otimização: Novo coletor para o saldo total
                // Coleta o saldo total
                launch {
                    mainViewModel.saldoTotal.collect { saldo ->
                        binding.tvSaldoValor.text = formatadorDeMoeda.format(saldo)
                    }
                }
            }
        }
    }

    /**
     * Abre o BottomSheet para adicionar ou editar um lançamento.
     */
    private fun abrirBottomSheet(lancamento: Lancamento? = null) {
        AddLancamentoBottomSheet(
            lancamentoParaEditar = lancamento,
            categorias = this.listaDeCategoriasAtual,
            onSave = mainViewModel::salvarLancamento
        ).show(supportFragmentManager, AddLancamentoBottomSheet.TAG)
    }
}
