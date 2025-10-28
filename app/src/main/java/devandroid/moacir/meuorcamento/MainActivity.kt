package devandroid.moacir.meuorcamento

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import devandroid.moacir.meuorcamento.ui.viewmodel.MainViewModel
import devandroid.moacir.meuorcamento.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // 1. Obter a instância do ViewModel
    // Usa a property delegate 'viewModels' do KTX para obter o ViewModel.
    // Fornecemos nossa ViewModelFactory para que o sistema saiba como criar o MainViewModel.
    private val mainViewModel: MainViewModel by viewModels {
        // Acessa o repositório criado na classe Application
        ViewModelFactory((application as MeuOrcamentoApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // O setContentView será ajustado depois para usar seu layout XML
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "ViewModel inicializado: $mainViewModel")

        // 2. Observar os dados do ViewModel
        // Lança uma coroutine que respeita o ciclo de vida da Activity
        lifecycleScope.launch {
            // 'collect' fica escutando as emissões do StateFlow
            mainViewModel.todosLancamentos.collect { lancamentos ->
                // Este bloco será executado sempre que a lista de lançamentos mudar.
                if (lancamentos.isNotEmpty()) {
                    Log.d("MainActivity", "Lista de lançamentos atualizada: $lancamentos")
                } else {
                    Log.d("MainActivity", "Ainda não há lançamentos no banco de dados.")
                }
            }
        }

        // 3. (OPCIONAL) Adicionar dados de teste para verificar se tudo funciona
        // Vamos adicionar uma categoria e um lançamento de teste na primeira vez que o app abrir.
        adicionarDadosDeTeste()
    }

    private fun adicionarDadosDeTeste() {
        // Adiciona uma categoria "Salário"
        mainViewModel.adicionarCategoria("Salário")

        // Adiciona um lançamento de teste
        // A lógica para obter o ID da categoria será mais robusta no futuro.
        // Por enquanto, vamos assumir que a primeira categoria tem id = 1.
        mainViewModel.adicionarLancamento(
            descricao = "Salário do mês",
            valor = java.math.BigDecimal("3000.00"),
            categoriaId = 1, // Assumindo que o ID da categoria "Salário" será 1
            natureza = devandroid.moacir.meuorcamento.data.model.Natureza.RECEITA
        )
        Log.d("MainActivity", "Dados de teste adicionados.")
    }
}
