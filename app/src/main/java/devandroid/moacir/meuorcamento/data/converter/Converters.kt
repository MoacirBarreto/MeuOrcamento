package devandroid.moacir.meuorcamento.data

import androidx.room.TypeConverter
import devandroid.moacir.meuorcamento.data.model.TipoLancamento
import java.math.BigDecimal
import java.time.LocalDate // ✅ Importe LocalDate

class Converters {

    // --- Conversores para BigDecimal <-> String ---
    @TypeConverter
    fun fromString(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun bigDecimalToString(bigDecimal: BigDecimal?): String? {
        return bigDecimal?.toPlainString()
    }

    // --- ✅ Conversores para LocalDate <-> Long (Epoch Day) ---
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? {
        // Converte o número de dias desde 1970-01-01 para um objeto LocalDate
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? {
        // Converte o objeto LocalDate para um número de dias desde 1970-01-01
        return date?.toEpochDay()
    }

    // --- Conversores para TipoLancamento (Enum) <-> String ---
    @TypeConverter
    fun fromStringToTipoLancamento(value: String?): TipoLancamento? {
        return value?.let { enumValueOf<TipoLancamento>(it) }
    }

    @TypeConverter
    fun tipoLancamentoToString(tipo: TipoLancamento?): String? {
        return tipo?.name
    }
}
