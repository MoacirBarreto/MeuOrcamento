package devandroid.moacir.meuorcamento.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import devandroid.moacir.meuorcamento.data.Converters // 1. Importe os conversores
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Representa um lançamento financeiro no banco de dados.
 *
 * @property tipo Usa o Enum 'TipoLancamento' para garantir consistência e segurança.
 * @property dataHora Usa 'LocalDateTime' para uma manipulação de data moderna e segura.
 *   O Room usará a classe 'Converters' para salvar este campo como Long no banco.
 */
@Entity(tableName = "Lancamentos")
@TypeConverters(Converters::class) // 2. Registre os conversores na entidade
data class Lancamento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val descricao: String,
    val valor: BigDecimal,
    val tipo: TipoLancamento,
    val categoriaId: Long,
    val dataHora: LocalDate
)

/**
 * Enum para representar os tipos de lançamento de forma segura.
 * Evita erros de digitação e torna o código mais legível.
 */
enum class TipoLancamento {
    RECEITA,
    DESPESA
}

