package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpiryStatus
import com.example.model.FoodItem
import com.example.model.SmartRecipe
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusGreenBorder
import com.example.ui.theme.StatusGreenText
import com.example.ui.theme.StatusRedBg
import com.example.ui.theme.StatusRedBorder
import com.example.ui.theme.StatusRedText
import com.example.ui.theme.StatusYellowBg
import com.example.ui.theme.StatusYellowBorder
import com.example.ui.theme.StatusYellowText
import com.example.ui.theme.ZeroSobraBackground
import com.example.ui.theme.ZeroSobraGreenContainer
import com.example.ui.theme.ZeroSobraGreenDark
import com.example.ui.theme.ZeroSobraGreenLight
import com.example.ui.theme.ZeroSobraGreenPrimary
import com.example.ui.theme.ZeroSobraOutline
import com.example.ui.theme.ZeroSobraSurface
import com.example.ui.theme.ZeroSobraSurfaceVariant
import com.example.ui.theme.ZeroSobraTextPrimary
import com.example.ui.theme.ZeroSobraTextSecondary
import com.example.ui.theme.ZeroSobraWarmAmber
import com.example.ui.theme.ZeroSobraWarmContainer
import com.example.ui.theme.ZeroSobraWarmOrange
import com.example.util.RecipeGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZeroSobraScreen() {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Initial state with sample items in all 3 expiration states for immediate testing
    val foodList = remember {
        mutableStateListOf(
            FoodItem(name = "Tomates maduros", daysUntilExpiry = 0, iconEmoji = "🍅"),
            FoodItem(name = "Yogur natural", daysUntilExpiry = 1, iconEmoji = "🥣"),
            FoodItem(name = "Pan de molde", daysUntilExpiry = 2, iconEmoji = "🍞"),
            FoodItem(name = "Queso fresco", daysUntilExpiry = 3, iconEmoji = "🧀"),
            FoodItem(name = "Aguacates", daysUntilExpiry = 5, iconEmoji = "🥑"),
            FoodItem(name = "Manzanas", daysUntilExpiry = 7, iconEmoji = "🍎")
        )
    }

    // Modal state for registration
    var showRegisterSheet by remember { mutableStateOf(false) }

    // Filter state for "Mi Refrigeradora"
    var selectedFilter by remember { mutableStateOf("Todos") }

    // Recipe generation state
    var generatedRecipe by remember { mutableStateOf<SmartRecipe?>(null) }
    var isGeneratingRecipe by remember { mutableStateOf(false) }
    var recipeGenerationStep by remember { mutableStateOf("") }

    // Deletion confirmation dialog
    var itemToDelete by remember { mutableStateOf<FoodItem?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ZeroSobraBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Encabezado
            item {
                HeaderSection(
                    urgentCount = foodList.count { it.status == ExpiryStatus.RED },
                    soonCount = foodList.count { it.status == ExpiryStatus.YELLOW },
                    freshCount = foodList.count { it.status == ExpiryStatus.GREEN }
                )
            }

            // 2. Botón de Acción Principal: "📷 Registrar Alimento"
            item {
                MainActionButton(
                    onClick = { showRegisterSheet = true }
                )
            }

            // 3. Sección "Receta Inteligente"
            item {
                SmartRecipeSection(
                    recipe = generatedRecipe,
                    isLoading = isGeneratingRecipe,
                    loadingMessage = recipeGenerationStep,
                    urgentFoodsCount = foodList.count { it.status == ExpiryStatus.RED || it.status == ExpiryStatus.YELLOW },
                    onGenerateRecipe = {
                        coroutineScope.launch {
                            isGeneratingRecipe = true
                            recipeGenerationStep = "Escaneando alimentos en riesgo (rojo y amarillo)..."
                            delay(700)
                            recipeGenerationStep = "Buscando la mejor combinación antidesperdicio..."
                            delay(600)
                            generatedRecipe = RecipeGenerator.generateRecipeForFoods(foodList)
                            isGeneratingRecipe = false
                            snackbarHostState.showSnackbar("¡Receta inteligente generada con éxito! 🍲")
                        }
                    }
                )
            }

            // 4. Sección "Mi Refrigeradora"
            item {
                FridgeSectionHeader(
                    totalCount = foodList.size,
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it }
                )
            }

            // Filtrado de alimentos
            val filteredFoods = when (selectedFilter) {
                "¡Usar hoy!" -> foodList.filter { it.status == ExpiryStatus.RED }
                "Próximos" -> foodList.filter { it.status == ExpiryStatus.YELLOW }
                "Frescos" -> foodList.filter { it.status == ExpiryStatus.GREEN }
                else -> foodList
            }

            if (filteredFoods.isEmpty()) {
                item {
                    EmptyFridgeState(filter = selectedFilter)
                }
            } else {
                items(filteredFoods, key = { it.id }) { food ->
                    FoodCard(
                        food = food,
                        onConsume = {
                            foodList.remove(food)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("¡Genial! Salvaste ${food.name} del desperdicio 🎉")
                            }
                        },
                        onDelete = { itemToDelete = food }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Formulario Rápido
    if (showRegisterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showRegisterSheet = false },
            sheetState = sheetState,
            containerColor = ZeroSobraSurface,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            QuickRegistrationForm(
                onDismiss = { showRegisterSheet = false },
                onSave = { name, days, emoji ->
                    foodList.add(0, FoodItem(name = name, daysUntilExpiry = days, iconEmoji = emoji))
                    showRegisterSheet = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("¡$name guardado en tu refrigeradora! 🥑")
                    }
                }
            )
        }
    }

    // Confirmación de eliminación
    itemToDelete?.let { food ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(text = "¿Eliminar ${food.name}?") },
            text = { Text(text = "Si ya lo comiste, te recomendamos usar el botón verde '¡Salvar!' para sumar puntos contra el desperdicio.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        foodList.remove(food)
                        itemToDelete = null
                    }
                ) {
                    Text(text = "Eliminar", color = Color(0xFFC62828))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// Componentes UI de ZeroSobra
// -------------------------------------------------------------

@Composable
fun HeaderSection(urgentCount: Int, soonCount: Int, freshCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ZeroSobraSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, ZeroSobraOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ZeroSobra 🥑",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ZeroSobraGreenDark,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Gestión inteligente para no desperdiciar alimentos",
                        fontSize = 13.sp,
                        color = ZeroSobraTextSecondary,
                        lineHeight = 18.sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = ZeroSobraGreenContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🌱", fontSize = 22.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resumen de impacto tipo semáforo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZeroSobraBackground, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadgeMini(count = urgentCount, label = "Usar hoy", color = StatusRedText, bg = StatusRedBg)
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(ZeroSobraOutline))
                StatusBadgeMini(count = soonCount, label = "Próximos", color = StatusYellowText, bg = StatusYellowBg)
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(ZeroSobraOutline))
                StatusBadgeMini(count = freshCount, label = "Frescos", color = StatusGreenText, bg = StatusGreenBg)
            }
        }
    }
}

@Composable
fun StatusBadgeMini(count: Int, label: String, color: Color, bg: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = bg,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ZeroSobraTextPrimary
        )
    }
}

@Composable
fun MainActionButton(onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = ZeroSobraGreenPrimary,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Cámara",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "📷 Registrar Alimento",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        }
    }
}

@Composable
fun QuickRegistrationForm(
    onDismiss: () -> Unit,
    onSave: (name: String, days: Int, emoji: String) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var selectedDays by remember { mutableIntStateOf(3) }
    var selectedEmoji by remember { mutableStateOf("🥑") }
    var isCameraSimulated by remember { mutableStateOf(false) }
    var cameraFlashState by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val presetFoods = listOf(
        Pair("Aguacate", "🥑"),
        Pair("Tomates", "🍅"),
        Pair("Pan", "🍞"),
        Pair("Yogur", "🥣"),
        Pair("Queso", "🧀"),
        Pair("Leche", "🥛"),
        Pair("Manzana", "🍎"),
        Pair("Zanahorias", "🥕")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registrar Alimento",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ZeroSobraGreenDark
            )
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Simulación de Cámara / Detección Visual
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    cameraFlashState = true
                    isCameraSimulated = true
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (cameraFlashState) ZeroSobraWarmContainer else ZeroSobraSurfaceVariant
            ),
            border = BorderStroke(1.dp, if (isCameraSimulated) ZeroSobraGreenLight else ZeroSobraOutline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isCameraSimulated) ZeroSobraGreenContainer else Color.White,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = if (isCameraSimulated) selectedEmoji else "📸", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCameraSimulated) "¡Foto capturada! Alimento detectado ✨" else "Simular captura con cámara 📷",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (isCameraSimulated) ZeroSobraGreenDark else ZeroSobraTextPrimary
                    )
                    Text(
                        text = if (isCameraSimulated) "Puedes editar el nombre y confirmar la fecha" else "Toca aquí para simular que tomas una foto",
                        fontSize = 12.sp,
                        color = ZeroSobraTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sugerencias rápidas de alimentos
        Text(
            text = "O elige rápidamente:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ZeroSobraTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presetFoods) { item ->
                val isSelected = foodName.equals(item.first, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) ZeroSobraGreenContainer else ZeroSobraBackground,
                    border = BorderStroke(1.dp, if (isSelected) ZeroSobraGreenPrimary else ZeroSobraOutline),
                    modifier = Modifier.clickable {
                        foodName = item.first
                        selectedEmoji = item.second
                        isCameraSimulated = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.second, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.first,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) ZeroSobraGreenDark else ZeroSobraTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Campo para nombre del alimento
        OutlinedTextField(
            value = foodName,
            onValueChange = { foodName = it },
            label = { Text("Nombre del alimento") },
            placeholder = { Text("Ej. Leche entera, Tomates, Pan...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZeroSobraGreenPrimary,
                focusedLabelColor = ZeroSobraGreenPrimary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Seleccionador de fecha de caducidad
        Text(
            text = "¿Cuándo vence o caduca?",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ZeroSobraTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        val daysOptions = listOf(
            Triple(0, "Hoy", "🔴 ¡Usar hoy!"),
            Triple(1, "Mañana", "🔴 Urgente"),
            Triple(2, "2 días", "🟡 Próximo"),
            Triple(3, "3 días", "🟡 Próximo"),
            Triple(5, "5 días", "🟢 Fresco"),
            Triple(7, "7 días", "🟢 Fresco")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            daysOptions.take(4).forEach { option ->
                val isSelected = selectedDays == option.first
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedDays = option.first },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) ZeroSobraGreenPrimary else ZeroSobraBackground,
                    border = BorderStroke(1.dp, if (isSelected) ZeroSobraGreenPrimary else ZeroSobraOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = option.second,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else ZeroSobraTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            daysOptions.drop(4).forEach { option ->
                val isSelected = selectedDays == option.first
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedDays = option.first },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) ZeroSobraGreenPrimary else ZeroSobraBackground,
                    border = BorderStroke(1.dp, if (isSelected) ZeroSobraGreenPrimary else ZeroSobraOutline)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = option.second,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else ZeroSobraTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Slider interactivo para días exactos
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Caducidad en: ",
                fontSize = 13.sp,
                color = ZeroSobraTextSecondary
            )
            Text(
                text = when (selectedDays) {
                    0 -> "¡Hoy mismo! (0 días)"
                    1 -> "Mañana (1 día)"
                    else -> "$selectedDays días"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    selectedDays <= 1 -> StatusRedText
                    selectedDays in 2..3 -> StatusYellowText
                    else -> StatusGreenText
                }
            )
        }

        Slider(
            value = selectedDays.toFloat(),
            onValueChange = { selectedDays = it.toInt() },
            valueRange = 0f..14f,
            steps = 13,
            colors = SliderDefaults.colors(
                thumbColor = ZeroSobraGreenPrimary,
                activeTrackColor = ZeroSobraGreenPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Botón "Guardar en mi Refrigeradora"
        Button(
            onClick = {
                val finalName = foodName.trim().ifEmpty { "Alimento saludable" }
                onSave(finalName, selectedDays, selectedEmoji)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ZeroSobraGreenPrimary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "🧊 Guardar en mi Refrigeradora",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// -------------------------------------------------------------
// Sección Receta Inteligente con IA
// -------------------------------------------------------------

@Composable
fun SmartRecipeSection(
    recipe: SmartRecipe?,
    isLoading: Boolean,
    loadingMessage: String,
    urgentFoodsCount: Int,
    onGenerateRecipe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ZeroSobraSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, if (recipe != null) ZeroSobraWarmOrange.copy(alpha = 0.4f) else ZeroSobraOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ZeroSobraWarmContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "✨", fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Receta Inteligente",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZeroSobraTextPrimary
                        )
                        Text(
                            text = "Aprovecha los alimentos en rojo y amarillo",
                            fontSize = 12.sp,
                            color = ZeroSobraTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón destacado: "✨ Generar Receta con IA"
            ElevatedButton(
                onClick = onGenerateRecipe,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = ZeroSobraWarmOrange,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 3.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pensando receta...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "IA",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (recipe == null) "✨ Generar Receta con IA" else "✨ Regenerar Otra Receta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // Indicador de carga de IA
            if (isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ZeroSobraBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🤖", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = loadingMessage,
                            fontSize = 12.sp,
                            color = ZeroSobraTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Área donde se muestra la receta sugerida
            if (recipe != null && !isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                RecipeDisplayCard(recipe = recipe)
            } else if (recipe == null && !isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ZeroSobraBackground,
                    border = BorderStroke(1.dp, ZeroSobraOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💡 Tienes $urgentFoodsCount alimento(s) en alerta.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (urgentFoodsCount > 0) ZeroSobraWarmOrange else ZeroSobraGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Presiona el botón de arriba para que la IA cree un plato delicioso evitando que terminen en la basura.",
                            fontSize = 12.sp,
                            color = ZeroSobraTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeDisplayCard(recipe: SmartRecipe) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZeroSobraWarmContainer.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, ZeroSobraWarmAmber.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = recipe.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZeroSobraTextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Etiquetas de tiempo y dificultad
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Text(
                        text = "⏱️ ${recipe.prepTime}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ZeroSobraTextPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Text(
                        text = "⚡ ${recipe.difficulty}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ZeroSobraGreenDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ingredientes rescatados:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ZeroSobraTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                recipe.rescuedIngredients.forEach { ingredient ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, ZeroSobraOutline)
                    ) {
                        Text(
                            text = ingredient,
                            fontSize = 12.sp,
                            color = ZeroSobraTextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Pasos sencillos:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ZeroSobraTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            recipe.steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ZeroSobraGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step,
                        fontSize = 13.sp,
                        color = ZeroSobraTextPrimary,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // EcoTip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, ZeroSobraGreenLight.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = recipe.ecoTip,
                    fontSize = 12.sp,
                    color = ZeroSobraGreenDark,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Sección "Mi Refrigeradora"
// -------------------------------------------------------------

@Composable
fun FridgeSectionHeader(
    totalCount: Int,
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Kitchen,
                    contentDescription = "Refrigeradora",
                    tint = ZeroSobraGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mi Refrigeradora",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZeroSobraTextPrimary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ZeroSobraGreenContainer
            ) {
                Text(
                    text = "$totalCount alimentos",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZeroSobraGreenDark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filtros rápidos
        val filters = listOf("Todos", "¡Usar hoy!", "Próximos", "Frescos")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterChange(filter) },
                    label = {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ZeroSobraGreenPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = ZeroSobraSurface,
                        labelColor = ZeroSobraTextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) ZeroSobraGreenPrimary else ZeroSobraOutline,
                        selectedBorderColor = ZeroSobraGreenPrimary,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun FoodCard(
    food: FoodItem,
    onConsume: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZeroSobraSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            when (food.status) {
                ExpiryStatus.RED -> StatusRedBorder
                ExpiryStatus.YELLOW -> StatusYellowBorder
                ExpiryStatus.GREEN -> StatusGreenBorder
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto o icono con fondo temático
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = when (food.status) {
                    ExpiryStatus.RED -> StatusRedBg
                    ExpiryStatus.YELLOW -> StatusYellowBg
                    ExpiryStatus.GREEN -> StatusGreenBg
                },
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = food.iconEmoji, fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Información del alimento
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZeroSobraTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = food.expiryDisplayText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (food.status) {
                        ExpiryStatus.RED -> StatusRedText
                        ExpiryStatus.YELLOW -> StatusYellowText
                        ExpiryStatus.GREEN -> ZeroSobraTextSecondary
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Etiqueta visual de color solicitada:
                // Verde: Fresco | Amarillo: Próximo a vencer (2-3 días) | Rojo: ¡Usar hoy!
                ExpiryTag(status = food.status)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Acciones: Consumir / Salvar y Eliminar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = ZeroSobraGreenContainer,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { onConsume() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Consumido / Salvar",
                            tint = ZeroSobraGreenDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpiryTag(status: ExpiryStatus) {
    val (label, textColor, bgColor, borderColor) = when (status) {
        ExpiryStatus.GREEN -> Quadruple(
            "🟢 Fresco",
            StatusGreenText,
            StatusGreenBg,
            StatusGreenBorder
        )
        ExpiryStatus.YELLOW -> Quadruple(
            "🟡 Próximo a vencer (2-3 días)",
            StatusYellowText,
            StatusYellowBg,
            StatusYellowBorder
        )
        ExpiryStatus.RED -> Quadruple(
            "🔴 ¡Usar hoy!",
            StatusRedText,
            StatusRedBg,
            StatusRedBorder
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun EmptyFridgeState(filter: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZeroSobraSurface),
        border = BorderStroke(1.dp, ZeroSobraOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🥗", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (filter == "Todos") "¡Tu refrigeradora está vacía!" else "No hay alimentos en estado '$filter'",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ZeroSobraTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Presiona '📷 Registrar Alimento' para agregar lo que tienes en casa y evitar que se eche a perder.",
                fontSize = 12.sp,
                color = ZeroSobraTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
