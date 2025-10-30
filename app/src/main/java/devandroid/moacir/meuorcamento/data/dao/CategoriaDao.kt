package devandroid.moacir.meuorcamento.data.dao

// Em CategoriaDao.kt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import devandroid.moacir.meuorcamento.data.model.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(categoria: Categoria)

    // GARANTIR QUE ESTA FUNÇÃO EXISTE E RETORNA UM FLOW
    @Query("SELECT * FROM categorias ORDER BY nome")
    fun buscarTodas(): Flow<List<Categoria>>

    // NOVO: Função para atualizar uma categoria
    @Update
    suspend fun update(categoria: Categoria)

    // NOVO: Função para deletar uma categoria
    @Delete
    suspend fun delete(categoria: Categoria)
}
