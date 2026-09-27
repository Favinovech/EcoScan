package pe.ecoscan.app.data.local.converter

import androidx.room.TypeConverter
import pe.ecoscan.app.domain.model.WasteCategory

class WasteCategoryConverter {

    @TypeConverter
    fun fromWasteCategory(category: WasteCategory): String = category.name

    @TypeConverter
    fun toWasteCategory(value: String): WasteCategory = WasteCategory.valueOf(value)
}
