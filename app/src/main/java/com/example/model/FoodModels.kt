package com.example.model

import java.util.UUID

enum class FoodCategory(val id: String, val displayName: String, val iconEmoji: String) {
    FRUTAS("frutas", "Frutas", "🍎"),
    VERDURAS("verduras", "Verduras", "🥦"),
    LACTEOS("lacteos", "Lácteos", "🥛"),
    DERIVADOS_LACTEOS("derivados_lacteos", "Derivados lácteos", "🧀"),
    CARNES("carnes", "Carnes", "🥩"),
    HUEVOS("huevos", "Huevos", "🥚"),
    PANADERIA("panaderia", "Panadería", "🍞"),
    GRANOS_CEREALES("granos_cereales", "Granos y Cereales", "🌾"),
    ENLATADOS_CONSERVAS("enlatados_conservas", "Enlatados y Conservas", "🥫"),
    SNACKS_PROCESADOS("snacks_procesados", "Snacks y Procesados", "🍪"),
    OTROS("otros", "Otros", "📦")
}

enum class ExpiryStatus(val label: String, val badgeColorType: String) {
    RED("¡Usar hoy!", "red"),
    YELLOW("Próximo a vencer", "yellow"),
    GREEN("Fresco", "green")
}

data class FoodItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val daysUntilExpiry: Int,
    val category: FoodCategory = FoodCategory.OTROS,
    val iconEmoji: String = category.iconEmoji,
    val addedDateText: String = "Hoy"
) {
    val status: ExpiryStatus
        get() = when {
            daysUntilExpiry <= 1 -> ExpiryStatus.RED
            daysUntilExpiry in 2..3 -> ExpiryStatus.YELLOW
            else -> ExpiryStatus.GREEN
        }

    val expiryDisplayText: String
        get() = when {
            daysUntilExpiry < 0 -> "Vencido hace ${-daysUntilExpiry} d"
            daysUntilExpiry == 0 -> "¡Vence hoy!"
            daysUntilExpiry == 1 -> "Vence mañana"
            else -> "Vence en $daysUntilExpiry días"
        }
}

data class SmartRecipe(
    val title: String,
    val prepTime: String,
    val difficulty: String,
    val rescuedIngredients: List<String>,
    val steps: List<String>,
    val ecoTip: String
)
