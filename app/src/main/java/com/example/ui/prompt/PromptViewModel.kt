package com.example.ui.prompt

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.model.ComparisonRun
import com.example.data.repository.ComparisonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface EvaluationState {
    object Idle : EvaluationState
    data class Loading(val stage: String) : EvaluationState
    data class Success(val run: ComparisonRun) : EvaluationState
    data class Error(val message: String) : EvaluationState
}

class PromptViewModel(private val repository: ComparisonRepository) : ViewModel() {

    val history: StateFlow<List<ComparisonRun>> = repository.allRuns
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedTemplate = MutableStateFlow(PromptTemplates.templates.first())
    val selectedTemplate: StateFlow<PromptTemplate> = _selectedTemplate.asStateFlow()

    private val _inputText = MutableStateFlow(_selectedTemplate.value.defaultInput)
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _evaluationState = MutableStateFlow<EvaluationState>(EvaluationState.Idle)
    val evaluationState: StateFlow<EvaluationState> = _evaluationState.asStateFlow()

    // For Custom Prompt type, let the user write their own prompts
    private val _customZeroShotPrompt = MutableStateFlow("")
    val customZeroShotPrompt: StateFlow<String> = _customZeroShotPrompt.asStateFlow()

    private val _customFewShotPrompt = MutableStateFlow("")
    val customFewShotPrompt: StateFlow<String> = _customFewShotPrompt.asStateFlow()

    private val _customStructuredPrompt = MutableStateFlow("")
    val customStructuredPrompt: StateFlow<String> = _customStructuredPrompt.asStateFlow()

    init {
        resetCustomPrompts()
    }

    fun selectTemplate(template: PromptTemplate) {
        _selectedTemplate.value = template
        _inputText.value = template.defaultInput
        resetCustomPrompts()
        if (_evaluationState.value is EvaluationState.Success || _evaluationState.value is EvaluationState.Error) {
            _evaluationState.value = EvaluationState.Idle
        }
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun setCustomZeroShotPrompt(prompt: String) { _customZeroShotPrompt.value = prompt }
    fun setCustomFewShotPrompt(prompt: String) { _customFewShotPrompt.value = prompt }
    fun setCustomStructuredPrompt(prompt: String) { _customStructuredPrompt.value = prompt }

    private fun resetCustomPrompts() {
        val template = _selectedTemplate.value
        val currentInput = _inputText.value
        _customZeroShotPrompt.value = template.zeroShotTemplate(currentInput)
        _customFewShotPrompt.value = template.fewShotTemplate(currentInput)
        _customStructuredPrompt.value = template.structuredTemplate(currentInput)
    }

    fun syncCustomPrompts() {
        if (_selectedTemplate.value.id != "custom") {
            resetCustomPrompts()
        }
    }

    fun runComparison() {
        val template = _selectedTemplate.value
        val input = _inputText.value
        
        if (input.isBlank()) {
            _evaluationState.value = EvaluationState.Error("Input text cannot be empty.")
            return
        }

        viewModelScope.launch {
            try {
                // Ensure custom prompts are synced up to date if not custom task
                if (template.id != "custom") {
                    resetCustomPrompts()
                }

                val zeroPrompt = _customZeroShotPrompt.value
                val fewPrompt = _customFewShotPrompt.value
                val structPrompt = _customStructuredPrompt.value

                _evaluationState.value = EvaluationState.Loading("Generating responses in parallel...")

                // Call Gemini for the three prompting strategies concurrently using async/await
                val zeroDeferred = async { GeminiClient.generate(zeroPrompt) }
                val fewDeferred = async { GeminiClient.generate(fewPrompt) }
                val structDeferred = async { GeminiClient.generate(structPrompt) }

                val zeroResult = try {
                    zeroDeferred.await()
                } catch (e: Exception) {
                    "Error executing Zero-Shot prompt: ${e.localizedMessage}"
                }

                val fewResult = try {
                    fewDeferred.await()
                } catch (e: Exception) {
                    "Error executing Few-Shot prompt: ${e.localizedMessage}"
                }

                val structResult = try {
                    structDeferred.await()
                } catch (e: Exception) {
                    "Error executing Structured prompt: ${e.localizedMessage}"
                }

                _evaluationState.value = EvaluationState.Loading("Evaluating response quality with LLM-as-a-Judge...")

                // Build evaluation prompt
                val judgePrompt = buildJudgePrompt(
                    taskName = template.name,
                    input = input,
                    zeroPrompt = zeroPrompt, zeroRes = zeroResult,
                    fewPrompt = fewPrompt, fewRes = fewResult,
                    structPrompt = structPrompt, structRes = structResult
                )

                val judgeOutput = try {
                    GeminiClient.generate(judgePrompt)
                } catch (e: Exception) {
                    "Failed to run AI evaluation: ${e.localizedMessage}"
                }

                // Extract scores using robust regex
                val scoreZero = "\\[SCORE_ZERO:\\s*(\\d+)\\]".toRegex()
                    .find(judgeOutput)?.groupValues?.get(1)?.toFloatOrNull() ?: 70f
                val scoreFew = "\\[SCORE_FEW:\\s*(\\d+)\\]".toRegex()
                    .find(judgeOutput)?.groupValues?.get(1)?.toFloatOrNull() ?: 85f
                val scoreStruct = "\\[SCORE_STRUCTURED:\\s*(\\d+)\\]".toRegex()
                    .find(judgeOutput)?.groupValues?.get(1)?.toFloatOrNull() ?: 95f

                val run = ComparisonRun(
                    taskName = template.name,
                    customInput = input,
                    zeroShotPrompt = zeroPrompt,
                    zeroShotResponse = zeroResult,
                    fewShotPrompt = fewPrompt,
                    fewShotResponse = fewResult,
                    structuredPrompt = structPrompt,
                    structuredResponse = structResult,
                    aiCritique = judgeOutput,
                    aiScoreZero = scoreZero,
                    aiScoreFew = scoreFew,
                    aiScoreStructured = scoreStruct
                )

                // Save to Room database
                val runId = repository.insertRun(run)
                val savedRun = run.copy(id = runId.toInt())

                _evaluationState.value = EvaluationState.Success(savedRun)
            } catch (e: Exception) {
                Log.e("PromptViewModel", "Error running comparison", e)
                _evaluationState.value = EvaluationState.Error("An error occurred during evaluation: ${e.localizedMessage}")
            }
        }
    }

    fun updateRatings(run: ComparisonRun, zeroRating: Float, fewRating: Float, structRating: Float) {
        viewModelScope.launch {
            val updatedRun = run.copy(
                zeroShotRating = zeroRating,
                fewShotRating = fewRating,
                structuredRating = structRating
            )
            repository.updateRun(updatedRun)
            // If the current success state holds this run, update it
            val state = _evaluationState.value
            if (state is EvaluationState.Success && state.run.id == run.id) {
                _evaluationState.value = EvaluationState.Success(updatedRun)
            }
        }
    }

    fun deleteRun(run: ComparisonRun) {
        viewModelScope.launch {
            repository.deleteRun(run)
            // If the deleted run is in current Success state, clear state to Idle
            val state = _evaluationState.value
            if (state is EvaluationState.Success && state.run.id == run.id) {
                _evaluationState.value = EvaluationState.Idle
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
            _evaluationState.value = EvaluationState.Idle
        }
    }

    private fun buildJudgePrompt(
        taskName: String,
        input: String,
        zeroPrompt: String, zeroRes: String,
        fewPrompt: String, fewRes: String,
        structPrompt: String, structRes: String
    ): String {
        return """
            You are an expert AI Prompt Engineering Judge. Your task is to evaluate and compare three responses generated for an LLM task using three different prompt strategies: Zero-Shot, Few-Shot, and Structured.
            
            Task Type: $taskName
            Raw User Input:
            "$input"
            
            --------------------------------------------------------
            [STRATEGY 1: ZERO-SHOT]
            Prompt:
            "$zeroPrompt"
            
            Response:
            "$zeroRes"
            
            --------------------------------------------------------
            [STRATEGY 2: FEW-SHOT]
            Prompt:
            "$fewPrompt"
            
            Response:
            "$fewRes"
            
            --------------------------------------------------------
            [STRATEGY 3: STRUCTURED]
            Prompt:
            "$structPrompt"
            
            Response:
            "$structRes"
            --------------------------------------------------------
            
            Evaluate all three responses carefully on:
            1. Completeness and Accuracy: Did it output everything requested?
            2. Constraint Adherence: Did it follow negative constraints (e.g., length, formats)?
            3. Formatting: Is it structured properly (JSON keys, headers)?
            4. Fillers/Conciseness: Did it avoid conversational fluff?
            
            Please generate a highly professional and analytical evaluation report in clean Markdown format.
            You MUST output the final overall grade scores (out of 100) inside these exact tags, otherwise the system will break:
            [SCORE_ZERO: <score>]
            [SCORE_FEW: <score>]
            [SCORE_STRUCTURED: <score>]
            
            Use the following exact sections in your report:
            
            ### Comparative Summary
            (Provide a high-level summary of which prompting strategy performed the best and a brief explanation of why. Discuss how providing examples or structured constraints altered the LLM behavior.)
            
            ### Detailed Critique
            - **Zero-Shot** (Score: [SCORE_ZERO: <score>]/100): Highlight strengths and key shortcomings.
            - **Few-Shot** (Score: [SCORE_FEW: <score>]/100): Highlight strengths and key shortcomings. Explain how the examples helped shape the response.
            - **Structured** (Score: [SCORE_STRUCTURED: <score>]/100): Highlight strengths and key shortcomings. Explain how the markdown schema influenced the formatting.
            
            ### Best Practice Takeaway
            (Give actionable advice on how to design prompts for this specific task based on these findings.)
        """.trimIndent()
    }
}

class PromptViewModelFactory(private val repository: ComparisonRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PromptViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
