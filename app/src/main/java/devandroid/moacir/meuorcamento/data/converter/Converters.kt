package devandroid.moacir.meuorcamento.data

import androidx.room.TypeConverter
import devandroid.moacir.meuorcamento.data.model.Natureza
import java.math.BigDecimal

class Converters {
    /**
     * Converte uma String (armazenada no banco) de volta para BigDecimal.
     */
    @TypeConverter
    fun fromString(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    /**
     * Converte um BigDecimal para String para poder ser salvo no banco.
     */
    @TypeConverter
    fun bigDecimalToString(bigDecimal: BigDecimal?): String? {
        return bigDecimal?.toPlainString()
    }

    /**
     * Converte uma String (armazenada no banco, ex: "RECEITA") de volta para o Enum Natureza.
     */
    @TypeConverter
    fun toNatureza(value: String) = enumValueOf<Natureza>(value)

    /**
     * Converte o Enum Natureza para uma String para poder ser salvo no banco.
     */
    @TypeConverter
    fun fromNatureza(value: Natureza) = value.name
}
