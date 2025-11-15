package devandroid.moacir.meuorcamento.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import devandroid.moacir.meuorcamento.data.model.Lancamento
import devandroid.moacir.meuorcamento.data.model.LancamentoComCategoria
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


@Dao
interface LancamentoDao {

    // Dentro da interface LancamentoDao

    @Query(
        """
    SELECT * FROM lancamentos
    INNER JOIN categorias ON lancamentos.categoriaId = categorias.id
    WHERE dataHora BETWEEN :dataInicio AND :dataFim
    ORDER BY dataHora DESC
"""
    )
    fun getLancamentosComCategoriaPorPeriodo(
        dataInicio: LocalDate,
        dataFim: LocalDate
    ): Flow<List<LancamentoComCategoria>>

    @Query("SELECT * FROM lancamentos WHERE dataHora BETWEEN :dataInicio AND :dataFim ORDER BY dataHora DESC")
    fun getLancamentosEntreDatas(dataInicio: LocalDate, dataFim: LocalDate): Flow<List<Lancamento>>

    /**
     * Insere um novo lançamento no banco de dados.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(lancamento: Lancamento)

    @Delete
    suspend fun delete(lancamento: Lancamento)

    @Update
    suspend fun update(lancamento: Lancamento)

    /**
     * Busca todos os lançamentos, ordenados pela data (os mais recentes primeiro).
     * Retorna um Flow para atualizações automáticas na UI.
     */
    @Transaction // Essencial para queries com @Relation
    @Query("SELECT * FROM lancamentos ORDER BY dataHora DESC")
    fun LancamentosMaisCategoria(): Flow<List<LancamentoComCategoria>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lancamento: Lancamento)


}
