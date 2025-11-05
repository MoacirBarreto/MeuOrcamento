package devandroid.moacir.meuorcamento.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Representa a relação Um-para-Um entre um Lançamento e sua Categoria.
 * O Room usará esta classe para unir os dados das duas tabelas.
 */
data class LancamentoComCategoria(
    // @Embedded diz ao Room para tratar os campos de Lancamento como se
    // estivessem diretamente nesta classe.
    @Embedded
    val lancamento: Lancamento,

    // @Relation define a relação.
    @Relation(
        parentColumn = "categoriaId", // A chave estrangeira na tabela Lancamento
        entityColumn = "id"           // A chave primária na tabela Categoria
    )
    val categoria: Categoria
)
