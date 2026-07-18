package com.example.data.repository

import com.example.data.local.ComparisonDao
import com.example.data.model.ComparisonRun
import kotlinx.coroutines.flow.Flow

class ComparisonRepository(private val comparisonDao: ComparisonDao) {
    val allRuns: Flow<List<ComparisonRun>> = comparisonDao.getAllRuns()

    suspend fun getRunById(id: Int): ComparisonRun? {
        return comparisonDao.getRunById(id)
    }

    suspend fun insertRun(run: ComparisonRun): Long {
        return comparisonDao.insertRun(run)
    }

    suspend fun updateRun(run: ComparisonRun) {
        comparisonDao.updateRun(run)
    }

    suspend fun deleteRun(run: ComparisonRun) {
        comparisonDao.deleteRun(run)
    }

    suspend fun clearAll() {
        comparisonDao.clearAll()
    }
}
