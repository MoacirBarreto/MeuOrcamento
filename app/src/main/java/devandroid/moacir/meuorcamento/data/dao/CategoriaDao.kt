package devandroid.moacir.meuorcamento.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import devandroid.moacir.meuorcamento.data.model.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    /**
     * Insere uma nova categoria no banco de dados.
     * Se uma categoria com o mesmo ID já existir, ela será substituída.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(categoria: Categoria)

    /**
     * Busca todas as categorias do banco de dados, ordenadas pelo nome.
     * Retorna um Flow, que emite a lista automaticamente sempre que os dados mudam.
     */
    @Query("SELECT * FROM categorias ORDER BY nome ASC")
    fun buscarTodas(): Flow<List<Categoria>>

    /**
     * Busca uma única categoria pelo seu ID.
     */
    @Query("SELECT * FROM categorias WHERE id = :categoriaId")
    suspend fun buscarPorId(categoriaId: Long): Categoria?
}
