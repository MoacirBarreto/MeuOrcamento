package devandroid.moacir.meuorcamento

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.appbar.MaterialToolbar
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.model.Natureza
import devandroid.moacir.meuorcamento.ui.CategoriasActivity
import devandroid.moacir.meuorcamento.ui.adapter.LancamentoAdapter
import devandroid.moacir.meuorcamento.ui.dialog.AddLancamentoBottomSheet
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModel
import devandroid.moacir.meuorcamento.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_manage_categories -> {
                startActivity(Intent(this, CategoriasActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // 1. Declaração das propriedades da classe
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory((application as MeuOrcamentoApplication).repository)
    }
    private lateinit var lancamentoAdapter: LancamentoAdapter

    // 2. Método principal do ciclo de vida da Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configura o botão flutuante para abrir o BottomSheet
        val fab: FloatingActionButton = findViewById(R.id.fabAdicionarLancamento)
        fab.setOnClickListener {
            val bottomSheet = AddLancamentoBottomSheet(mainViewModel)
            bottomSheet.show(supportFragmentManager, "AddLancamentoBottomSheet")
        }

        // Chama os métodos de configuração da UI
        setupRecyclerView()
        observeLancamentos()
        adicionarDadosDeTesteSeNecessario()
    }

    // 3. Métodos privados de configuração e lógica da UI
    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewLancamentos)

        // Atualize a criação do adapter para passar os dois callbacks
        lancamentoAdapter = LancamentoAdapter(
            onClick = { lancamento ->
                // Ação para o clique simples: abrir o diálogo de edição
                // Vamos criar uma função para isso em breve
                abrirDialogoDeEdicao(lancamento)
            },
            onLongClick = { lancamento ->
                // Ação para o clique longo: mostrar diálogo de exclusão
                mostrarDialogoDeConfirmacao(lancamento)
            }
        )

        recyclerView.adapter = lancamentoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeLancamentos() {
        lifecycleScope.launch {
            mainViewModel.todosLancamentos.collect { lancamentos ->
                // Submete a nova lista ao adapter sempre que os dados mudarem
                lancamentoAdapter.submitList(lancamentos)
                Log.d("MainActivity", "Adapter atualizado com ${lancamentos.size} itens.")
            }
        }
    }

    private fun mostrarDialogoDeConfirmacao(lancamento: Lancamento) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza de que deseja excluir o lançamento \"${lancamento.descricaoLancamento}\"?")
            .setPositiveButton("Excluir") { dialog, _ ->
                // Se o usuário confirmar, chama o ViewModel para deletar
                mainViewModel.deletarLancamento(lancamento)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun adicionarDadosDeTesteSeNecessario() {
        lifecycleScope.launch {
            // Acessa o Flow apenas uma vez para verificar se está vazio
            mainViewModel.todosLancamentos.collect { lancamentos ->
                if (lancamentos.isEmpty()) {
                    Log.d("MainActivity", "Banco de dados vazio. Adicionando dados de teste.")

                    mainViewModel.adicionarCategoria("Salário")
                    mainViewModel.adicionarCategoria("Alimentação")

                    mainViewModel.adicionarLancamento(
                        descricao = "Salário de Outubro",
                        valor = 3000.0.toBigDecimal(),
                        categoriaId = 1,
                        natureza = Natureza.RECEITA
                    )
                    mainViewModel.adicionarLancamento(
                        descricao = "Compras no mercado",
                        valor = 250.55.toBigDecimal(),
                        categoriaId = 2,
                        natureza = Natureza.DESPESA
                    )
                }
                // O 'return' implícito aqui faz com que o collect pare após a primeira emissão,
                // evitando que os dados de teste sejam adicionados repetidamente.
                // A corrotina de 'observeLancamentos' continua funcionando normalmente.
            }
        }
    }

    private fun abrirDialogoDeEdicao(lancamento: Lancamento) {
        // Cria uma instância do BottomSheet passando o ViewModel E o lançamento para editar
        val bottomSheet = AddLancamentoBottomSheet(mainViewModel, lancamento)
        bottomSheet.show(supportFragmentManager, "EditLancamentoBottomSheet")
    }
}
