package com.example.myapplicationfocusflow.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationfocusflow.model.CategoryModel
import com.example.myapplicationfocusflow.Repositories.FocusCategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FocusCategoryViewModel(private val repository: FocusCategoryRepository) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategoryModel>>(emptyList())
    val categories: StateFlow<List<CategoryModel>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categoryList ->
                _categories.value = categoryList
            }
        }
    }

    fun addCategory(categoryName: String) {
        viewModelScope.launch {
            val newCategory = CategoryModel(name = categoryName)
            repository.insertCategory(newCategory)
        }
    }
}

