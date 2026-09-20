package com.example.util

import com.example.model.FoodItem
import com.example.model.ExpiryStatus
import com.example.model.SmartRecipe

object RecipeGenerator {

    fun generateRecipeForFoods(foods: List<FoodItem>): SmartRecipe {
        val urgentFoods = foods.filter { it.status == ExpiryStatus.RED || it.status == ExpiryStatus.YELLOW }
        val foodNames = if (urgentFoods.isNotEmpty()) {
            urgentFoods.map { "${it.iconEmoji} ${it.name}" }
        } else {
            foods.take(3).map { "${it.iconEmoji} ${it.name}" }
        }

        val primaryFood = urgentFoods.firstOrNull()?.name?.lowercase() ?: "vegetales"

        return when {
            primaryFood.contains("pan") || primaryFood.contains("queso") -> {
                SmartRecipe(
                    title = "Tostadas Doradas ZeroSobra Gratinadas 🧀🍞",
                    prepTime = "10 min",
                    difficulty = "Muy Fácil",
                    rescuedIngredients = foodNames.ifEmpty { listOf("🍞 Pan", "🧀 Queso") },
                    steps = listOf(
                        "Corta el pan en rebanadas gruesas y colócalas en una sartén a fuego medio.",
                        "Agrega una pizca de aceite de oliva o mantequilla para dorar ambos lados.",
                        "Coloca el queso y los ingredientes restantes encima; tapa la sartén por 2 minutos para que se funda.",
                        "Sirve de inmediato caliente. ¡Crujiente y sin desperdiciar nada!"
                    ),
                    ecoTip = "💡 Tip de conservación: Si el pan está empezando a ponerse duro, humedécelo apenas con unas gotas de agua antes de tostarlo y recuperará su textura suave por dentro."
                )
            }
            primaryFood.contains("tomate") || primaryFood.contains("aguacate") || primaryFood.contains("ensalada") -> {
                SmartRecipe(
                    title = "Bruschetta Rústica & Guacamole Express 🥑🍅",
                    prepTime = "8 min",
                    difficulty = "Fácil",
                    rescuedIngredients = foodNames.ifEmpty { listOf("🥑 Aguacate", "🍅 Tomate") },
                    steps = listOf(
                        "Pica los tomates y el aguacate en cubitos pequeños en un tazón.",
                        "Sazona con limón, una pizca de sal, pimienta y unas gotas de aceite.",
                        "Tuesta rebanadas de pan o galletas saladas.",
                        "Monta la mezcla fresca encima y disfruta de un snack nutritivo y sustentable."
                    ),
                    ecoTip = "💡 Tip de conservación: Guarda los tomates a temperatura ambiente lejos de la luz solar directa; el frío del refrigerador apaga su sabor y aroma natural."
                )
            }
            primaryFood.contains("yogur") || primaryFood.contains("manzana") || primaryFood.contains("fruta") || primaryFood.contains("leche") -> {
                SmartRecipe(
                    title = "Bowl Energético Antidesperdicio con Frutas y Yogur 🥣🍎",
                    prepTime = "5 min",
                    difficulty = "Rápido",
                    rescuedIngredients = foodNames.ifEmpty { listOf("🥣 Yogur", "🍎 Frutas") },
                    steps = listOf(
                        "Sirve el yogur en tu tazón favorito.",
                        "Lava y pica las frutas que están maduras en rodajas finas.",
                        "Decora por encima agregando avena, canela o frutos secos si tienes a mano.",
                        "Mezcla y disfruta de un desayuno o merienda escolar lleno de energía."
                    ),
                    ecoTip = "💡 Tip de conservación: Las frutas muy maduras son ideales para licuados o batidos; si no las vas a comer hoy, córtalas y congélalas en bolsitas herméticas."
                )
            }
            else -> {
                SmartRecipe(
                    title = "Salteado Sorpresa 'Salva-Nevera' 🍳🌿",
                    prepTime = "15 min",
                    difficulty = "Fácil",
                    rescuedIngredients = foodNames.ifEmpty { listOf("🥗 Ingredientes frescos") },
                    steps = listOf(
                        "Pica finamente todos los ingredientes a punto de caducar.",
                        "Calienta un chorrito de aceite en una sartén antiadherente.",
                        "Saltea primero los vegetales más firmes y luego añade los suaves durante 5 a 7 minutos.",
                        "Opcional: agrega 1 o 2 huevos batidos o arroz del día anterior y revuelve.",
                        "Sazona con salsa de soja o tus especias favoritas y sirve caliente."
                    ),
                    ecoTip = "💡 Sabías que: Reducir el desperdicio de alimentos es una de las 3 acciones individuales más efectivas para frenar el cambio climático."
                )
            }
        }
    }
}
