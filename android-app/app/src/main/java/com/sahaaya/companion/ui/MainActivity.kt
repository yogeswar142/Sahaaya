package com.sahaaya.companion.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahaaya.companion.core.*
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var pipeline: SahaayaPipeline
    private lateinit var visitEngine: VisitListEngine
    private var registerMembers: List<RegisterMember> = emptyList()
    private var testQuestions: List<Map<String, String>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load offline assets
        val chunksJson = assets.open("protocol_chunks.json").bufferedReader().use { it.readText() }
        val rulesJson = assets.open("danger_rules.json").bufferedReader().use { it.readText() }
        val registerCsv = assets.open("register.csv").bufferedReader().use { it.readText() }
        val questionsCsv = assets.open("test_questions.csv").bufferedReader().use { it.readText() }

        val chunks = DataLoader.parseProtocolChunks(chunksJson)
        val rules = DataLoader.parseDangerRules(rulesJson)
        registerMembers = DataLoader.parseRegister(registerCsv)
        testQuestions = DataLoader.parseCsv(questionsCsv)

        val dangerEngine = DangerRuleEngine(rules)
        val intentGate = IntentGate()
        val bm25Retriever = Bm25Retriever(chunks)
        pipeline = SahaayaPipeline(dangerEngine, intentGate, bm25Retriever)
        visitEngine = VisitListEngine()

        setContent {
            SahaayaTheme {
                MainAppScaffold()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainAppScaffold() {
        var selectedTab by remember { mutableStateOf(0) }
        var showBenchmarkDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Sahaaya Sahayak",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Offline ASHA & ANM Clinical Companion",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryTeal),
                    actions = {
                        IconButton(onClick = { showBenchmarkDialog = true }) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = "Benchmark",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = SurfaceWhite) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                        label = { Text("Ask Sahaaya", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryTeal, indicatorColor = Color(0xFFE0F2F1))
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        label = { Text("Today's Visits", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryTeal, indicatorColor = Color(0xFFE0F2F1))
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                if (selectedTab == 0) {
                    AskScreen(pipeline = pipeline)
                } else {
                    VisitsScreen(members = registerMembers, visitEngine = visitEngine)
                }

                if (showBenchmarkDialog) {
                    val apkFile = applicationInfo?.sourceDir?.let { File(it) }
                    BenchmarkScreen(
                        pipeline = pipeline,
                        testQuestions = testQuestions,
                        apkFile = apkFile,
                        onDismiss = { showBenchmarkDialog = false }
                    )
                }
            }
        }
    }
}
