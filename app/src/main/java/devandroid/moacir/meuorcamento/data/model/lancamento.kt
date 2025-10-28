
package devandroid.moacir.meuorcamento.data.model
import devandroid.moacir.meuorcamento.data.model.Natureza
import devandroid.moacir.meuorcamento.data.model.Categoria

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * Representa um único lançamento financeiro no banco de dados.
 *
 * @param id A chave primária única do lançamento.
 * @param valor O valor monetário do lançamento. Usamos BigDecimal para precisão.
 * @param dataHora O momento em que o lançamento foi registrado, em formato timestamp (Long).
 * @param natureza O tipo do lançamento (RECEITA ou DESPESA).
 * @param categoriaId A chave estrangeira que vincula este lançamento a uma Categoria.
 */
@Entity(
    tableName = "lancamentos",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.RESTRICT // Impede apagar uma categoria que está em uso
        )
    ]
)
data class Lancamento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val valor: BigDecimal,

    val dataHora: Long,

    val natureza: Natureza,

    val descricaoLancamento : String,

    // O Room sabe que este campo se refere à chave estrangeira definida acima.
    // O índice acelera as consultas por categoria.
    @ColumnInfo(index = true)
    val categoriaId: Long
)
