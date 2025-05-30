package com.example.myapplicationfocusflow.Repositories

import com.example.myapplicationfocusflow.model.CategoryModel
import com.example.myapplicationfocusflow.model.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FocusCategoryRepository(private val dataStoreManager: DataStoreManager) {

    fun getAllCategories(): Flow<List<CategoryModel>> {
        return dataStoreManager.getCategories()
    }

    suspend fun insertCategory(category: CategoryModel) {
        val currentList = dataStoreManager.getCategories().first()
        val maxId = currentList.maxOfOrNull { it.category_ID } ?: 0
        val newCategory = category.copy(category_ID = maxId + 1)
        dataStoreManager.saveCategories(currentList + newCategory)
    }

    suspend fun deleteCategory(id: Int) {
        val currentList = dataStoreManager.getCategories().first()
        val updatedList = currentList.filter { it.category_ID != id }
        dataStoreManager.saveCategories(updatedList)
    }
}


