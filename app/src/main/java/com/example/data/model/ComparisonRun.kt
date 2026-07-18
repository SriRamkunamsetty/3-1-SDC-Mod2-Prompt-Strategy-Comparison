package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comparison_runs")
data class ComparisonRun(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskName: String,
    val customInput: String,
    val timestamp: Long = System.currentTimeMillis(),
    
    // Zero-Shot details
    val zeroShotPrompt: String,
    val zeroShotResponse: String,
    val zeroShotRating: Float = 0f,
    
    // Few-Shot details
    val fewShotPrompt: String,
    val fewShotResponse: String,
    val fewShotRating: Float = 0f,
    
    // Structured details
    val structuredPrompt: String,
    val structuredResponse: String,
    val structuredRating: Float = 0f,
    
    // AI Judge Critique & Scores
    val aiCritique: String = "",
    val aiScoreZero: Float = 0f,
    val aiScoreFew: Float = 0f,
    val aiScoreStructured: Float = 0f
)
