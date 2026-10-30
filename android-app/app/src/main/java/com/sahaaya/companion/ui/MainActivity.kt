package com.sahaaya.companion.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahaaya.companion.R
import com.sahaaya.companion.core.*
import com.sahaaya.companion.ui.i18n.AppLanguage
import com.sahaaya.companion.ui.i18n.Strings
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var pipeline: SahaayaPipeline
    private lateinit var visitEngine: VisitListEngine
    private var protocolChunks: List<ProtocolChunk> = emptyList()
    private var registerMembers: List<RegisterMember> = emptyList()
    private var householdMembers: List<HouseholdMember> = emptyList()
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
        protocolChunks = chunks
        registerMembers = DataLoader.parseRegister(registerCsv)
        testQuestions = DataLoader.parseCsv(questionsCsv)

        // Load complete household register for rich search & overview
        val householdCsv = try {
            assets.open("household_register.csv").bufferedReader().use { it.readText() }
        } catch (_: Exception) { "" }
        if (householdCsv.isNotBlank()) {
            householdMembers = DataLoader.parseHouseholdRegister(householdCsv)
        }

        val dangerEngine = DangerRuleEngine(rules)
        val intentGate = IntentGate()
        val bm25Retriever = Bm25Retriever(chunks)
        pipeline = SahaayaPipeline(dangerEngine, intentGate, bm25Retriever)
        visitEngine = VisitListEngine()

        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            var currentLanguage by rememberSaveable { mutableStateOf(AppLanguage.ENGLISH) }

            SahaayaTheme(darkTheme = isDarkTheme) {
                MainAppScaffold(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    language = currentLanguage,
                    onLanguageChange = { currentLanguage = it }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainAppScaffold(
        isDarkTheme: Boolean,
        onToggleTheme: () -> Unit,
        language: AppLanguage,
        onLanguageChange: (AppLanguage) -> Unit
    ) {
        var showPortalWelcome by rememberSaveable { mutableStateOf(false) }
        var selectedTab by rememberSaveable { mutableStateOf(0) }
        var showBenchmarkDialog by remember { mutableStateOf(false) }
        var showLanguageMenu by remember { mutableStateOf(false) }
        var askInitialQuery by remember { mutableStateOf<String?>(null) }
        var visitsFilter by remember { mutableStateOf("ALL") }
        var visitsSearchQuery by remember { mutableStateOf("") }
        var referralTarget by remember { mutableStateOf<Triple<String, String, String>?>(null) }

        // Choose logo based on mode: Dark mode uses glowing squircle, Light mode uses bright symbol
        val logoRes = if (isDarkTheme) {
            R.drawable.ic_sahaaya_logo
        } else {
            R.drawable.ic_sahaaya_symbol_bright
        }

        if (showPortalWelcome) {
            PortalWelcomeScreen(
                language = language,
                isDarkTheme = isDarkTheme,
                onEnterPortal = { showPortalWelcome = false }
            )
            return
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDarkTheme) Color(0xFF041930) else Color.White)
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = logoRes),
                                    contentDescription = "Sahaaya Logo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    Strings.appTitle(language),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "Frontline Clinical Companion",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDarkTheme) SurfaceDark else PrimaryTeal
                    ),
                    actions = {
                        // 1. Language Switcher Menu
                        Box {
                            IconButton(
                                onClick = { showLanguageMenu = true },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    Icons.Default.Translate,
                                    contentDescription = "Switch Language",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showLanguageMenu,
                                onDismissRequest = { showLanguageMenu = false },
                                modifier = Modifier.background(
                                    if (isDarkTheme) SurfaceElevatedDark else Color.White
                                )
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${lang.nativeName} (${lang.displayName})",
                                                    fontWeight = if (lang == language) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isDarkTheme) TextPrimaryDark else Color.Black
                                                )
                                                if (lang == language) {
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            onLanguageChange(lang)
                                            showLanguageMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // 2. Info & Benchmark Dialog
                        IconButton(
                            onClick = { showBenchmarkDialog = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Benchmark & Info",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 3. Dark / Bright Theme Toggle
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkTheme) "Bright Mode" else "Dark Mode",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 4. Portal Open Screen Button (Door icon)
                        IconButton(
                            onClick = { showPortalWelcome = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Portal Open Screen",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = if (isDarkTheme) SurfaceDark else SurfaceWhite
                ) {
                    // Tab 0: Home
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = {
                            Text(
                                Strings.tabHome(language),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            selectedTextColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            indicatorColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1)
                        )
                    )

                    // Tab 1: Today's Visits
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        label = {
                            Text(
                                Strings.tabVisits(language),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            selectedTextColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            indicatorColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1)
                        )
                    )

                    // Tab 2: Ask Sahaaya
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                        label = {
                            Text(
                                Strings.tabAsk(language),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            selectedTextColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            indicatorColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1)
                        )
                    )

                    // Tab 3: Protocols
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                        label = {
                            Text(
                                Strings.tabGuide(language),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            selectedTextColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                            indicatorColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1)
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        householdMembers = householdMembers,
                        registerMembers = registerMembers,
                        language = language,
                        isDarkTheme = isDarkTheme,
                        onNavigateToAsk = { query ->
                            askInitialQuery = query
                            selectedTab = 2
                        },
                        onNavigateToVisits = { filter, search ->
                            visitsFilter = filter
                            visitsSearchQuery = search
                            selectedTab = 1
                        },
                        onOpenReferral = { name, famId, reason ->
                            referralTarget = Triple(name, famId, reason)
                        }
                    )
                    1 -> VisitsScreen(
                        members = registerMembers,
                        visitEngine = visitEngine,
                        language = language,
                        isDarkTheme = isDarkTheme,
                        onNavigateToAsk = { query ->
                            askInitialQuery = query
                            selectedTab = 2
                        },
                        initialFilter = visitsFilter,
                        initialSearch = visitsSearchQuery
                    )
                    2 -> AskScreen(
                        pipeline = pipeline,
                        language = language,
                        isDarkTheme = isDarkTheme,
                        initialQuery = askInitialQuery
                    )
                    3 -> GuideScreen(
                        chunks = protocolChunks,
                        language = language,
                        isDarkTheme = isDarkTheme,
                        onNavigateToAsk = { query ->
                            askInitialQuery = query
                            selectedTab = 2
                        }
                    )
                }

                if (showBenchmarkDialog) {
                    val apkFile = applicationInfo?.sourceDir?.let { File(it) }
                    BenchmarkScreen(
                        pipeline = pipeline,
                        testQuestions = testQuestions,
                        apkFile = apkFile,
                        isDarkTheme = isDarkTheme,
                        language = language,
                        onDismiss = { showBenchmarkDialog = false }
                    )
                }

                referralTarget?.let { (patientName, familyId, reason) ->
                    ReferralSlipDialog(
                        isOpen = true,
                        onDismiss = { referralTarget = null },
                        patientName = "$patientName ($familyId)",
                        dangerSign = reason,
                        category = "EMERGENCY REFERRAL",
                        citation = "MoHFW Clinical Protocol",
                        language = language,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}
