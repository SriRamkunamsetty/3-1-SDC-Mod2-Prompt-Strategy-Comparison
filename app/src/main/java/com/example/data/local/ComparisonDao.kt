package com.example.data.local

import androidx.room.*
import com.example.data.model.ComparisonRun
import kotlinx.coroutines.flow.Flow

@Dao
interface ComparisonDao {
    @Query("SELECT * FROM comparison_runs ORDER BY timestamp DESC")
    fun getAllRuns(): Flow<List<ComparisonRun>>

    @Query("SELECT * FROM comparison_runs WHERE id = :id")
    suspend fun getRunById(id: Int): ComparisonRun?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: ComparisonRun): Long

    @Update
    suspend fun updateRun(run: ComparisonRun)

    @Delete
    suspend fun deleteRun(run: ComparisonRun)

    @Query("DELETE FROM comparison_runs")
    suspend fun clearAll()
}
