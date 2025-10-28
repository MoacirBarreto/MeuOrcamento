package devandroid.moacir.meuorcamento.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa uma categoria de lançamento (ex: Moradia, Alimentação, Salário).
 *
 * @param id A chave primária única da categoria.
 * @param nome O nome da categoria a ser exibido para o usuário.
 */
@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String
)
