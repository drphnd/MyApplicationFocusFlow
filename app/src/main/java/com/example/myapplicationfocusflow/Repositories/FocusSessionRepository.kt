package com.example.myapplicationfocusflow.Repositories

import com.example.myapplicationfocusflow.model.DataStoreManager
import com.example.myapplicationfocusflow.model.FocusSessionModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FocusSessionRepository(private val dataStoreManager: DataStoreManager) {

    fun getAllSessions(): Flow<List<FocusSessionModel>> {
        return dataStoreManager.getFocusSessions()
    }

    suspend fun insertSession(session: FocusSessionModel): FocusSessionModel {
        val currentList = dataStoreManager.getFocusSessions().first()
        val newId = dataStoreManager.getNextSessionId()
        val newSession = session.copy(sessionId = newId)

        dataStoreManager.saveFocusSessions(currentList + newSession)
        dataStoreManager.incrementSessionId()

        return newSession
    }

    suspend fun updateSession(session: FocusSessionModel) {
        val currentList = dataStoreManager.getFocusSessions().first()
        val updatedList = currentList.map {
            if (it.sessionId == session.sessionId) session else it
        }
        dataStoreManager.saveFocusSessions(updatedList)
    }

    suspend fun getSessionsByFocusId(focusId: Int): List<FocusSessionModel> {
        return dataStoreManager.getFocusSessions().first().filter { it.focusId == focusId }
    }

}
