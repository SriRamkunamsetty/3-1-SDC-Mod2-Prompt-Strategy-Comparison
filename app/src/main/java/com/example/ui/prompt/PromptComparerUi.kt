package com.example.ui.prompt

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ComparisonRun
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    border: BorderStroke? = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.60f)
            ),
            border = border,
            elevation = elevation,
            onClick = onClick,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.60f)
            ),
            border = border,
            elevation = elevation,
            content = content
        )
    }
}

@Composable
fun CustomGlassTopBar(
    activeTab: Int,
    historyNotEmpty: Boolean,
    onClearHistory: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
        color = Color(0xFFF3EDF7),
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Compare,
                        contentDescription = null,
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(28.dp).padding(end = 8.dp)
                    )
                    Text(
                        text = "Prompt Strategy",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        color = Color(0xFF21005D),
                        letterSpacing = (-0.5).sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (activeTab == 2 && historyNotEmpty) {
                        IconButton(
                            onClick = onClearHistory,
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All History",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8DEF8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LLM",
                            color = Color(0xFF21005D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFEADDFF))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                var isVisible by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    while (true) {
                        kotlinx.coroutines.delay(800)
                        isVisible = !isVisible
                    }
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isVisible) Color(0xFF6750A4) else Color(0xFF6750A4).copy(alpha = 0.3f))
                )
                Text(
                    text = "API ACTIVE: GEMINI PIPELINE SECURE",
                    color = Color(0xFF21005D),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptComparerScreen(
    viewModel: PromptViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val selectedTemplate by viewModel.selectedTemplate.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val evaluationState by viewModel.evaluationState.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) }
    val tabs = listOf("Evaluator", "Analytics", "History")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CustomGlassTopBar(
                activeTab = activeTab,
                historyNotEmpty = history.isNotEmpty(),
                onClearHistory = { viewModel.clearHistory() }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = Color(0xFFF3EDF7)
            ) {
                tabs.forEachIndexed { index, label ->
                    val icon = when (index) {
                        0 -> if (activeTab == 0) Icons.Default.FlashOn else Icons.Outlined.FlashOn
                        1 -> if (activeTab == 1) Icons.Default.BarChart else Icons.Outlined.BarChart
                        else -> if (activeTab == 2) Icons.Default.History else Icons.Outlined.History
                    }
                    NavigationBarItem(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        label = { Text(label) },
                        icon = { Icon(icon, contentDescription = label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF21005D),
                            unselectedIconColor = Color(0xFF49454F),
                            indicatorColor = Color(0xFFE8DEF8),
                            selectedTextColor = Color(0xFF21005D),
                            unselectedTextColor = Color(0xFF49454F)
                        ),
                        modifier = Modifier.testTag("nav_tab_${label.lowercase(Locale.ROOT)}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFEF7FF),
                            Color(0xFFEADDFF),
                            Color(0xFFF3EDF7)
                        )
                    )
                )
        ) {
            when (activeTab) {
                0 -> EvaluatorTab(
                    viewModel = viewModel,
                    selectedTemplate = selectedTemplate,
                    inputText = inputText,
                    evaluationState = evaluationState
                )
                1 -> AnalyticsTab(
                    history = history
                )
                2 -> HistoryTab(
                    history = history,
                    onSelectRun = { run ->
                        // Load saved run into UI view state
                        val template = PromptTemplates.templates.find { it.name == run.taskName }
                            ?: PromptTemplates.templates.last()
                        viewModel.selectTemplate(template)
                        viewModel.setInputText(run.customInput)
                        viewModel.setCustomZeroShotPrompt(run.zeroShotPrompt)
                        viewModel.setCustomFewShotPrompt(run.fewShotPrompt)
                        viewModel.setCustomStructuredPrompt(run.structuredPrompt)
                        // Trigger success state with this historical run loaded
                        // so they can read and inspect
                        (viewModel.evaluationState as? MutableStateFlow)?.value = EvaluationState.Success(run)
                        activeTab = 0
                    },
                    onDeleteRun = { run -> viewModel.deleteRun(run) }
                )
            }
        }
    }
}

@Composable
fun EvaluatorTab(
    viewModel: PromptViewModel,
    selectedTemplate: PromptTemplate,
    inputText: String,
    evaluationState: EvaluationState
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val customZero by viewModel.customZeroShotPrompt.collectAsStateWithLifecycle()
    val customFew by viewModel.customFewShotPrompt.collectAsStateWithLifecycle()
    val customStruct by viewModel.customStructuredPrompt.collectAsStateWithLifecycle()

    var showPromptEditor by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Task / Scenario Selector Section
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select LLM Evaluation Task",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF21005D),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PromptTemplates.templates) { template ->
                        val isSelected = selectedTemplate.id == template.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectTemplate(template) },
                            label = { Text(template.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (template.id) {
                                        "email_triage" -> Icons.Default.Email
                                        "sql_gen" -> Icons.Default.Storage
                                        "data_extraction" -> Icons.Default.DataObject
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEADDFF),
                                selectedLabelColor = Color(0xFF21005D),
                                selectedLeadingIconColor = Color(0xFF21005D)
                            ),
                            modifier = Modifier.testTag("chip_${template.id}")
                        )
                    }
                }
                
                Text(
                    text = selectedTemplate.description,
                    fontSize = 13.sp,
                    color = Color(0xFF49454F),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Raw Input Text Field
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Source Task Input",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF21005D),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { 
                        viewModel.setInputText(it)
                        viewModel.syncCustomPrompts()
                    },
                    placeholder = { Text("Enter prompt input payload text here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("raw_input_text_field"),
                    textStyle = TextStyle(fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.3f),
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFF49454F).copy(alpha = 0.5f)
                    ),
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = { 
                                viewModel.setInputText("") 
                                viewModel.syncCustomPrompts()
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear input")
                            }
                        }
                    }
                )
            }
        }

        // Collapsible Custom Prompt Engineering Editor
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPromptEditor = !showPromptEditor }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Inspect Compiled Prompts",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                    Icon(
                        imageVector = if (showPromptEditor) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Prompts"
                    )
                }

                AnimatedVisibility(visible = showPromptEditor) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isCustomTemplate = selectedTemplate.id == "custom"
                        Text(
                            text = if (isCustomTemplate) "You can freely edit the prompt templates below!" else "These prompts are automatically generated based on the active template and input above:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        PromptInputBlock(
                            title = "Zero-Shot Prompt Blueprint",
                            value = customZero,
                            onValueChange = { if (isCustomTemplate) viewModel.setCustomZeroShotPrompt(it) },
                            readOnly = !isCustomTemplate,
                            tag = "prompt_editor_zero"
                        )

                        PromptInputBlock(
                            title = "Few-Shot Prompt Blueprint (With Examples)",
                            value = customFew,
                            onValueChange = { if (isCustomTemplate) viewModel.setCustomFewShotPrompt(it) },
                            readOnly = !isCustomTemplate,
                            tag = "prompt_editor_few"
                        )

                        PromptInputBlock(
                            title = "Structured Prompt Blueprint (Markdown/Roles)",
                            value = customStruct,
                            onValueChange = { if (isCustomTemplate) viewModel.setCustomStructuredPrompt(it) },
                            readOnly = !isCustomTemplate,
                            tag = "prompt_editor_structured"
                        )
                    }
                }
            }
        }

        // Compare Button
        Button(
            onClick = { viewModel.runComparison() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("compare_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "RUN PIPELINE COMPARISON",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        // Status Handler & Display Output Card
        when (evaluationState) {
            is EvaluationState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF6750A4).copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Set input and run evaluation comparison to see results",
                            fontSize = 13.sp,
                            color = Color(0xFF49454F).copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is EvaluationState.Loading -> {
                FrostedGlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF6750A4),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pipeline Progress",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF21005D)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = evaluationState.stage,
                            fontSize = 13.sp,
                            color = Color(0xFF49454F),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }
            }
            is EvaluationState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = evaluationState.message,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            is EvaluationState.Success -> {
                ComparisonResultDisplay(
                    run = evaluationState.run,
                    onRatingChanged = { zero, few, struct ->
                        viewModel.updateRatings(evaluationState.run, zero, few, struct)
                    }
                )
            }
        }
    }
}

@Composable
fun PromptInputBlock(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean,
    tag: String
) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6750A4),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 120.dp)
                .testTag(tag),
            textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.5f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.3f),
                focusedBorderColor = Color(0xFF6750A4),
                unfocusedBorderColor = Color(0xFF49454F).copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun ComparisonResultDisplay(
    run: ComparisonRun,
    onRatingChanged: (Float, Float, Float) -> Unit
) {
    var activeSubTab by remember { mutableStateOf(0) }
    val tabs = listOf("Zero-Shot", "Few-Shot", "Structured", "AI Judge")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "EVALUATION PIPELINE OUTPUTS",
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
            color = Color(0xFF21005D),
            modifier = Modifier.padding(top = 8.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = activeSubTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            containerColor = Color(0xFFF3EDF7)
        ) {
            tabs.forEachIndexed { index, label ->
                val badgeColor = when (index) {
                    0 -> Color(0xFF607D8B) // Slate
                    1 -> Color(0xFF2196F3) // Blue
                    2 -> Color(0xFF4CAF50) // Emerald
                    else -> Color(0xFF6750A4) // Violet
                }
                
                val score = when (index) {
                    0 -> run.aiScoreZero.toInt()
                    1 -> run.aiScoreFew.toInt()
                    2 -> run.aiScoreStructured.toInt()
                    else -> null
                }

                Tab(
                    selected = activeSubTab == index,
                    onClick = { activeSubTab = index },
                    modifier = Modifier.testTag("result_tab_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (activeSubTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeSubTab == index) Color(0xFF21005D) else Color(0xFF49454F)
                        )
                        if (score != null) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = score.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
        ) {
            when (activeSubTab) {
                0 -> OutputStrategyPane(
                    strategyTitle = "Zero-Shot Prompting",
                    score = run.aiScoreZero,
                    prompt = run.zeroShotPrompt,
                    response = run.zeroShotResponse,
                    userRating = run.zeroShotRating,
                    onRatingChanged = { onRatingChanged(it, run.fewShotRating, run.structuredRating) },
                    accentColor = Color(0xFF607D8B)
                )
                1 -> OutputStrategyPane(
                    strategyTitle = "Few-Shot Prompting",
                    score = run.aiScoreFew,
                    prompt = run.fewShotPrompt,
                    response = run.fewShotResponse,
                    userRating = run.fewShotRating,
                    onRatingChanged = { onRatingChanged(run.zeroShotRating, it, run.structuredRating) },
                    accentColor = Color(0xFF2196F3)
                )
                2 -> OutputStrategyPane(
                    strategyTitle = "Structured Prompting",
                    score = run.aiScoreStructured,
                    prompt = run.structuredPrompt,
                    response = run.structuredResponse,
                    userRating = run.structuredRating,
                    onRatingChanged = { onRatingChanged(run.zeroShotRating, run.fewShotRating, it) },
                    accentColor = Color(0xFF4CAF50)
                )
                3 -> JudgeReportPane(
                    run = run
                )
            }
        }
    }
}

@Composable
fun OutputStrategyPane(
    strategyTitle: String,
    score: Float,
    prompt: String,
    response: String,
    userRating: Float,
    onRatingChanged: (Float) -> Unit,
    accentColor: Color
) {
    val clipboardManager = LocalClipboardManager.current
    var showPromptUsed by remember { mutableStateOf(false) }

    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Strategy Score Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = strategyTitle,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = accentColor
                    )
                    Text(
                        text = "AI Quality Grade Score",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = score.toInt().toString(),
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // User rating stars selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Quality Assessment (User Rating)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..5) {
                        val isFilled = i <= userRating
                        IconButton(
                            onClick = { onRatingChanged(i.toFloat()) },
                            modifier = Modifier.size(36.dp).testTag("rate_star_${strategyTitle.split(" ")[0].lowercase()}_$i")
                        ) {
                            Icon(
                                imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Rate $i Stars",
                                tint = if (isFilled) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggle Prompt Used
            OutlinedButton(
                onClick = { showPromptUsed = !showPromptUsed },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showPromptUsed) "Hide Actual Prompt Sent" else "View Actual Prompt Sent",
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = if (showPromptUsed) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = showPromptUsed) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROMPT SENT:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        IconButton(
                            onClick = { clipboardManager.setText(AnnotatedString(prompt)) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Prompt", modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    SelectionContainer {
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // LLM Response Received Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RESPONSE RECEIVED:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(response)) },
                        modifier = Modifier.size(32.dp).testTag("copy_response_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Response", modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                SelectionContainer {
                    Text(
                        text = response,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.testTag("strategy_response_text")
                    )
                }
            }
        }
    }
}

@Composable
fun JudgeReportPane(run: ComparisonRun) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Heading with Judge Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Judge Analytical Critique",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF21005D)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Progress comparison
            Text(
                text = "GRADE PERFORMANCE SCORE BOARD",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = Color(0xFF49454F)
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ScoreIndicatorRow(
                    label = "Zero-Shot Prompting",
                    score = run.aiScoreZero,
                    color = Color(0xFF607D8B)
                )
                ScoreIndicatorRow(
                    label = "Few-Shot Prompting",
                    score = run.aiScoreFew,
                    color = Color(0xFF2196F3)
                )
                ScoreIndicatorRow(
                    label = "Structured Prompting",
                    score = run.aiScoreStructured,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEADDFF))
            Spacer(modifier = Modifier.height(12.dp))

            // Analytical Report text
            Text(
                text = "CRITIQUE REPORT:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            MarkdownText(
                text = run.aiCritique,
                modifier = Modifier.testTag("judge_critique_text")
            )
        }
    }
}

@Composable
fun ScoreIndicatorRow(
    label: String,
    score: Float,
    color: Color
) {
    val animatedProgress by animateFloatAsState(targetValue = score / 100f, label = "scoreProgress")

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${score.toInt()}/100",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val lines = text.split("\n")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("###") -> {
                    Text(
                        text = trimmed.replace("###", "").trim(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("####") -> {
                    Text(
                        text = trimmed.replace("####", "").trim(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 1.dp)
                    )
                }
                trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                    val bulletContent = trimmed.substring(1).trim()
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = parseBold(bulletContent),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
                trimmed.isNotBlank() -> {
                    Text(
                        text = parseBold(trimmed),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

fun parseBold(input: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var index = 0
    while (index < input.length) {
        val nextBold = input.indexOf("**", index)
        if (nextBold == -1) {
            builder.append(input.substring(index))
            break
        }
        // append plain text
        builder.append(input.substring(index, nextBold))
        val endBold = input.indexOf("**", nextBold + 2)
        if (endBold == -1) {
            builder.append(input.substring(nextBold))
            break
        }
        // append bold text
        builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
        builder.append(input.substring(nextBold + 2, endBold))
        builder.pop()
        index = endBold + 2
    }
    return builder.toAnnotatedString()
}

@Composable
fun AnalyticsTab(
    history: List<ComparisonRun>
) {
    if (history.isEmpty()) {
        EmptyStateView(
            message = "No evaluation history found. Complete a comparison run to view charts."
        )
        return
    }

    // Dynamic math computations
    val totalRuns = history.size
    
    val avgZero = history.map { it.aiScoreZero }.average().toFloat()
    val avgFew = history.map { it.aiScoreFew }.average().toFloat()
    val avgStruct = history.map { it.aiScoreStructured }.average().toFloat()

    val userAvgZero = history.filter { it.zeroShotRating > 0f }.map { it.zeroShotRating }.average().toFloat()
    val userAvgFew = history.filter { it.fewShotRating > 0f }.map { it.fewShotRating }.average().toFloat()
    val userAvgStruct = history.filter { it.structuredRating > 0f }.map { it.structuredRating }.average().toFloat()

    val bestStrategy = when {
        avgStruct >= avgFew && avgStruct >= avgZero -> "Structured Prompting"
        avgFew >= avgZero -> "Few-Shot Prompting"
        else -> "Zero-Shot Prompting"
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "PROMPT STRATEGY METRICS",
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // Stat overview row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FrostedGlassCard(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = totalRuns.toString(),
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        color = Color(0xFF6750A4)
                    )
                    Text(
                        text = "Runs Evaluated",
                        fontSize = 11.sp,
                        color = Color(0xFF49454F),
                        textAlign = TextAlign.Center
                    )
                }
            }

            FrostedGlassCard(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Best: Structured",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF49454F),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Custom drawn Chart comparison Canvas
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Average AI Score Comparison",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF21005D),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Visual Custom Drawn Canvas Bar Chart!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        val barSpacing = width / 4f
                        val barWidth = 48.dp.toPx()
                        
                        val maxScore = 100f
                        
                        // Draw grid lines
                        for (i in 1..4) {
                            val y = height - (height * (i * 0.25f))
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.2f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Draw Zero-Shot bar
                        val zeroBarHeight = (avgZero / maxScore) * height
                        drawRoundRect(
                            color = Color(0xFF607D8B),
                            topLeft = Offset(barSpacing - (barWidth / 2f), height - zeroBarHeight),
                            size = Size(barWidth, zeroBarHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Draw Few-Shot bar
                        val fewBarHeight = (avgFew / maxScore) * height
                        drawRoundRect(
                            color = Color(0xFF2196F3),
                            topLeft = Offset(barSpacing * 2f - (barWidth / 2f), height - fewBarHeight),
                            size = Size(barWidth, fewBarHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Draw Structured bar
                        val structBarHeight = (avgStruct / maxScore) * height
                        drawRoundRect(
                            color = Color(0xFF4CAF50),
                            topLeft = Offset(barSpacing * 3f - (barWidth / 2f), height - structBarHeight),
                            size = Size(barWidth, structBarHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }

                // Bar Labels Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Zero-Shot", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                        Text(text = "${avgZero.toInt()}/100", fontSize = 12.sp, color = Color(0xFF607D8B), fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Few-Shot", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                        Text(text = "${avgFew.toInt()}/100", fontSize = 12.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Structured", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                        Text(text = "${avgStruct.toInt()}/100", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Custom User Ratings comparison
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Average User Rating Assessment (1-5 ⭐)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF21005D),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                UserRatingBar(label = "Zero-Shot", rating = userAvgZero, color = Color(0xFF607D8B))
                Spacer(modifier = Modifier.height(10.dp))
                UserRatingBar(label = "Few-Shot", rating = userAvgFew, color = Color(0xFF2196F3))
                Spacer(modifier = Modifier.height(10.dp))
                UserRatingBar(label = "Structured", rating = userAvgStruct, color = Color(0xFF4CAF50))
            }
        }

        // Educational Insights Card
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF6750A4)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Prompt Engineering Insights",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF6750A4)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Based on evaluations, **Structured Prompts** consistently reduce hallucinations and yield higher scores by providing explicit roles and clear constraints.\n\n**Few-Shot Prompts** excel at aligning responses with specific stylistic expectations by demonstrating correct patterns beforehand.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF49454F)
                )
            }
        }
    }
}

@Composable
fun UserRatingBar(
    label: String,
    rating: Float,
    color: Color
) {
    val displayRating = if (rating.isNaN()) 0.0f else rating
    val formattedRating = String.format(Locale.ROOT, "%.1f", displayRating)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(80.dp)
        )
        Row(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            for (i in 1..5) {
                val isFilled = i <= displayRating
                Icon(
                    imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (isFilled) Color(0xFFFFB300) else Color.Gray.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = "$formattedRating/5.0",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun HistoryTab(
    history: List<ComparisonRun>,
    onSelectRun: (ComparisonRun) -> Unit,
    onDeleteRun: (ComparisonRun) -> Unit
) {
    if (history.isEmpty()) {
        EmptyStateView(
            message = "No comparison runs recorded yet. Start by executing an evaluation on the Evaluator screen."
        )
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "HISTORICAL RUN RECORDS",
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
            color = Color(0xFF21005D)
        )

        history.forEach { run ->
            HistoryRunCard(
                run = run,
                onClick = { onSelectRun(run) },
                onDelete = { onDeleteRun(run) }
            )
        }
    }
}

@Composable
fun HistoryRunCard(
    run: ComparisonRun,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedDate = remember(run.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        sdf.format(Date(run.timestamp))
    }

    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_run_card_${run.id}"),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = run.taskName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF21005D)
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = Color(0xFF49454F)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("delete_run_${run.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete record",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            // Compact score summary tags row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreTag(label = "Z: ${run.aiScoreZero.toInt()}", color = Color(0xFF607D8B))
                ScoreTag(label = "F: ${run.aiScoreFew.toInt()}", color = Color(0xFF2196F3))
                ScoreTag(label = "S: ${run.aiScoreStructured.toInt()}", color = Color(0xFF4CAF50))
            }

            // Preview snippet of the source input
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = run.customInput,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF49454F)
            )
        }
    }
}

@Composable
fun ScoreTag(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun EmptyStateView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Compare,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
