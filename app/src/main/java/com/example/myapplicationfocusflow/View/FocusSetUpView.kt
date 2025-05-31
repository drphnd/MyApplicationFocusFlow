package com.example.myapplicationfocusflow.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplicationfocusflow.model.FocusModel
import com.example.myapplicationfocusflow.ViewModel.AmbientSoundViewModel
import com.example.myapplicationfocusflow.ViewModel.FocusCategoryViewModel
import com.example.myapplicationfocusflow.ViewModel.FocusModeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSetUpView(
    focusViewModel: FocusModeViewModel,
    categoryViewModel: FocusCategoryViewModel,
    ambientSoundViewModel: AmbientSoundViewModel,
    onSaveClick: (FocusModel) -> Unit,
    onBackClick: () -> Unit
) {
    var focusName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Study") }
    var focusGoals by remember { mutableStateOf("") }
    var focusDuration by remember { mutableStateOf("") }
    var restDuration by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    // ✅ State untuk dialog add category
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var isAddingCategory by remember { mutableStateOf(false) }
    var addCategoryError by remember { mutableStateOf<String?>(null) }

    val categories by categoryViewModel.categories.collectAsState()
    val scope = rememberCoroutineScope()
    val viewModelError by focusViewModel.error.collectAsState()

    LaunchedEffect(focusName, focusDuration, restDuration) {
        if (saveError != null) saveError = null
        if (viewModelError != null) focusViewModel.clearError()
    }

    // ✅ Add Category Dialog
    if (showAddCategoryDialog) {
        Dialog(onDismissRequest = {
            showAddCategoryDialog = false
            newCategoryName = ""
            addCategoryError = null
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Add New Category",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = {
                            newCategoryName = it
                            if (addCategoryError != null) addCategoryError = null
                        },
                        placeholder = { Text("Enter category name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            errorBorderColor = Color.Red
                        ),
                        isError = addCategoryError != null
                    )

                    // Error message
                    if (addCategoryError != null) {
                        Text(
                            text = addCategoryError!!,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = {
                                showAddCategoryDialog = false
                                newCategoryName = ""
                                addCategoryError = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                        ) {
                            Text("Cancel")
                        }

                        // Add Button
                        Button(
                            onClick = {
                                scope.launch {
                                    val categoryName = newCategoryName.trim()

                                    when {
                                        categoryName.isEmpty() -> {
                                            addCategoryError = "Category name cannot be empty"
                                        }
                                        categoryName.length < 2 -> {
                                            addCategoryError = "Category name must be at least 2 characters"
                                        }
                                        categoryName.length > 30 -> {
                                            addCategoryError = "Category name must be less than 30 characters"
                                        }
                                        categories.any { it.name.equals(categoryName, ignoreCase = true) } -> {
                                            addCategoryError = "Category already exists"
                                        }
                                        else -> {
                                            try {
                                                isAddingCategory = true
                                                categoryViewModel.addCategory(categoryName)

                                                // Set kategori baru sebagai selected
                                                selectedCategory = categoryName

                                                // Close dialog
                                                showAddCategoryDialog = false
                                                newCategoryName = ""
                                                addCategoryError = null
                                            } catch (e: Exception) {
                                                addCategoryError = "Failed to add category: ${e.message}"
                                            } finally {
                                                isAddingCategory = false
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            enabled = !isAddingCategory && newCategoryName.isNotBlank()
                        ) {
                            if (isAddingCategory) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                    Text("Adding...")
                                }
                            } else {
                                Text("Add")
                            }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title
            Text(
                text = "FOCUS\nFLOW",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Focus Name
            Text(
                text = "Focus Name",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = focusName,
                onValueChange = { focusName = it },
                placeholder = { Text("Enter Focus Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )

            // Category
            Text(
                text = "Category Focus",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    // ✅ Tampilkan semua kategori yang ada
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category.name
                                showCategoryDropdown = false
                            }
                        )
                    }

                    // ✅ Divider sebelum Add Category
                    if (categories.isNotEmpty()) {
                        HorizontalDivider()
                    }

                    // ✅ Add Category option
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "➕ Add Category",
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Blue
                                )
                            }
                        },
                        onClick = {
                            showCategoryDropdown = false
                            showAddCategoryDialog = true
                        }
                    )
                }
            }

            // Focus Goals
            Text(
                text = "Focus Goals",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = focusGoals,
                onValueChange = { focusGoals = it },
                placeholder = { Text("Enter Focus Goals") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )

            // Duration Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus Duration (minutes)",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    OutlinedTextField(
                        value = focusDuration,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                focusDuration = newValue
                            }
                        },
                        placeholder = { Text("25") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = focusDuration.toIntOrNull()?.let { it <= 0 || it > 300 } == true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Gray,
                            errorBorderColor = Color.Red,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        )
                    )
                    if (focusDuration.toIntOrNull()?.let { it <= 0 || it > 300 } == true) {
                        Text(
                            text = "Duration must be 1-300 minutes",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rest Duration (minutes)",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    OutlinedTextField(
                        value = restDuration,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                restDuration = newValue
                            }
                        },
                        placeholder = { Text("5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = restDuration.toIntOrNull()?.let { it <= 0 || it > 60 } == true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Gray,
                            errorBorderColor = Color.Red,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        )
                    )
                    if (restDuration.toIntOrNull()?.let { it <= 0 || it > 60 } == true) {
                        Text(
                            text = "Rest must be 1-60 minutes",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Error message display
            if (saveError != null || viewModelError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = saveError ?: viewModelError ?: "",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    scope.launch {
                        val focusDurationInt = focusDuration.toIntOrNull()
                        val restDurationInt = restDuration.toIntOrNull()

                        when {
                            focusName.isBlank() -> {
                                saveError = "Focus name cannot be empty"
                                return@launch
                            }
                            focusDurationInt == null || focusDurationInt <= 0 -> {
                                saveError = "Please enter a valid focus duration"
                                return@launch
                            }
                            focusDurationInt > 300 -> {
                                saveError = "Focus duration cannot exceed 300 minutes"
                                return@launch
                            }
                            restDurationInt == null || restDurationInt <= 0 -> {
                                saveError = "Please enter a valid rest duration"
                                return@launch
                            }
                            restDurationInt > 60 -> {
                                saveError = "Rest duration cannot exceed 60 minutes"
                                return@launch
                            }
                            else -> {
                                try {
                                    isSaving = true
                                    saveError = null

                                    val focusModel = FocusModel(
                                        title = focusName.trim(),
                                        category = selectedCategory,
                                        goals = focusGoals.trim(),
                                        focusDuration = focusDurationInt,
                                        restDuration = restDurationInt,
                                        ambientSoundId = null,
                                        createdAt = System.currentTimeMillis()
                                    )

                                    val result = focusViewModel.saveFocusModel(focusModel)

                                    result.fold(
                                        onSuccess = { savedModel ->
                                            onSaveClick(savedModel)
                                        },
                                        onFailure = { error ->
                                            saveError = "Failed to save: ${error.message}"
                                        }
                                    )

                                } catch (e: Exception) {
                                    saveError = "Unexpected error: ${e.message}"
                                } finally {
                                    isSaving = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(25.dp),
                enabled = !isSaving && focusName.isNotBlank() &&
                        focusDuration.isNotBlank() && restDuration.isNotBlank() &&
                        focusDuration.toIntOrNull()?.let { it > 0 && it <= 300 } == true &&
                        restDuration.toIntOrNull()?.let { it > 0 && it <= 60 } == true
            ) {
                if (isSaving) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "SAVING...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = "SAVE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}