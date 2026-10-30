package com.sahaaya.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahaaya.companion.core.PrioritizedVisit
import com.sahaaya.companion.core.PriorityGroup
import com.sahaaya.companion.core.RegisterMember
import com.sahaaya.companion.core.VisitListEngine
import com.sahaaya.companion.ui.i18n.AppLanguage
import com.sahaaya.companion.ui.i18n.Strings
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitsScreen(
    members: List<RegisterMember>,
    visitEngine: VisitListEngine,
    language: AppLanguage,
    isDarkTheme: Boolean,
    onNavigateToAsk: (String) -> Unit = {},
    initialFilter: String = "ALL",
    initialSearch: String = "",
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.parse("2026-10-18") }
    val prioritizedVisits = remember(members) {
        visitEngine.prioritizeVisits(members, today)
    }

    var selectedFilter by remember(initialFilter) { mutableStateOf(initialFilter) }
    var searchQuery by remember(initialSearch) { mutableStateOf(initialSearch) }
    val completedVisits = remember { mutableStateListOf<String>() }
    val scheduled48hList = remember { mutableStateListOf<String>() }
    var showDailySummary by remember { mutableStateOf(false) }
    var selectedDangerReferral by remember { mutableStateOf<PrioritizedVisit?>(null) }

    val dangerCount = prioritizedVisits.count { it.priorityGroup == PriorityGroup.DANGER }
    val overdueCount = prioritizedVisits.count { it.priorityGroup == PriorityGroup.OVERDUE }
    val upcomingCount = prioritizedVisits.count { it.priorityGroup == PriorityGroup.UPCOMING }

    // Filter by category and search
    val filteredList = remember(selectedFilter, searchQuery, prioritizedVisits) {
        val categoryFiltered = when (selectedFilter) {
            "DANGER" -> prioritizedVisits.filter { it.priorityGroup == PriorityGroup.DANGER }
            "OVERDUE" -> prioritizedVisits.filter { it.priorityGroup == PriorityGroup.OVERDUE }
            "UPCOMING" -> prioritizedVisits.filter { it.priorityGroup == PriorityGroup.UPCOMING }
            else -> prioritizedVisits
        }

        if (searchQuery.isBlank()) {
            categoryFiltered
        } else {
            val q = searchQuery.trim().lowercase()
            categoryFiltered.filter {
                it.member.name.lowercase().contains(q) ||
                it.member.familyId.lowercase().contains(q) ||
                it.reason.lowercase().contains(q) ||
                (it.member.dangerFlag.isNotBlank() &&
                 !it.member.dangerFlag.equals("none", ignoreCase = true) &&
                 it.member.dangerFlag.lowercase().contains(q))
            }
        }
    }

    val bgModifier = if (isDarkTheme) {
        Modifier.background(BackgroundDark)
    } else {
        Modifier.background(BackgroundLight)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(bgModifier)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // 1. Header with Title, Beneficiary Count, and 1-Tap Daily Report Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.visitsHeader(language),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) TextPrimaryDark else SecondaryNavy,
                        fontSize = 19.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Strings.visitsSubheader(filteredList.size, prioritizedVisits.size, language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isDarkTheme) TextSecondaryDark else Color(0xFF64748B),
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 1-Tap End-of-Day PHC Summary Report Button
            FilledTonalButton(
                onClick = { showDailySummary = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.heightIn(min = 36.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1),
                    contentColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                )
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Strings.endOfDayReportButton(language),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. High-Visibility Triage Stat Dashboard (3 Tappable Action Cards with Equal Height & Precise Alignment)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1: Danger / High Alert
            TriageStatCard(
                title = Strings.statDangerTitle(language),
                count = dangerCount,
                subtitle = Strings.statDangerSub(language),
                containerColor = if (isDarkTheme) DangerRedDarkBg else DangerRedLight,
                accentColor = if (isDarkTheme) DangerRedDark else DangerRed,
                isSelected = selectedFilter == "DANGER",
                icon = Icons.Default.Warning,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedFilter = if (selectedFilter == "DANGER") "ALL" else "DANGER"
                }
            )

            // Card 2: Overdue Visits
            TriageStatCard(
                title = Strings.statOverdueTitle(language),
                count = overdueCount,
                subtitle = Strings.statOverdueSub(language),
                containerColor = if (isDarkTheme) WarningAmberDarkBg else WarningAmberLight,
                accentColor = if (isDarkTheme) WarningAmberDark else WarningAmber,
                isSelected = selectedFilter == "OVERDUE",
                icon = Icons.Default.NotificationsActive,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedFilter = if (selectedFilter == "OVERDUE") "ALL" else "OVERDUE"
                }
            )

            // Card 3: Routine Visits
            TriageStatCard(
                title = Strings.statRoutineTitle(language),
                count = upcomingCount,
                subtitle = Strings.statRoutineSub(language),
                containerColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1),
                accentColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                isSelected = selectedFilter == "UPCOMING",
                icon = Icons.Default.DateRange,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedFilter = if (selectedFilter == "UPCOMING") "ALL" else "UPCOMING"
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    Strings.searchHouseholdPlaceholder(language),
                    fontSize = 12.5.sp,
                    color = if (isDarkTheme) TextSecondaryDark else Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDarkTheme) SurfaceDark else Color.White,
                unfocusedContainerColor = if (isDarkTheme) SurfaceDark else Color.White,
                focusedBorderColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                unfocusedBorderColor = if (isDarkTheme) Color(0xFF24364D) else Color(0xFFCBD5E1)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Quick Filter Chips (Horizontally Scrollable so chips never wrap vertically)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("${Strings.filterAll(language)} (${prioritizedVisits.size})", fontSize = 12.sp) }
            )
            FilterChip(
                selected = selectedFilter == "DANGER",
                onClick = { selectedFilter = "DANGER" },
                label = { Text("🚨 ${Strings.filterDanger(language)} ($dangerCount)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isDarkTheme) DangerRedDarkBg else DangerRedLight,
                    selectedLabelColor = if (isDarkTheme) DangerRedDark else DangerRed
                )
            )
            FilterChip(
                selected = selectedFilter == "OVERDUE",
                onClick = { selectedFilter = "OVERDUE" },
                label = { Text("⚠️ ${Strings.filterOverdue(language)} ($overdueCount)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isDarkTheme) WarningAmberDarkBg else WarningAmberLight,
                    selectedLabelColor = if (isDarkTheme) WarningAmberDark else WarningAmber
                )
            )
            FilterChip(
                selected = selectedFilter == "UPCOMING",
                onClick = { selectedFilter = "UPCOMING" },
                label = { Text("📅 ${Strings.filterRoutine(language)} ($upcomingCount)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1),
                    selectedLabelColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Beneficiary Cards List (Starts directly below filter chips with 0 artificial spacing)
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.noVisitsFound(language),
                    color = if (isDarkTheme) TextSecondaryDark else Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 2.dp, bottom = 28.dp)
            ) {
                items(filteredList, key = { it.member.memberId }) { visit ->
                    val isDone = completedVisits.contains(visit.member.memberId)
                    val is48h = scheduled48hList.contains(visit.member.memberId)
                    BeneficiaryVisitCard(
                        visit = visit,
                        language = language,
                        isDone = isDone,
                        is48hScheduled = is48h,
                        isDarkTheme = isDarkTheme,
                        onToggleDone = {
                            if (isDone) {
                                completedVisits.remove(visit.member.memberId)
                            } else {
                                completedVisits.add(visit.member.memberId)
                            }
                        },
                        onToggle48h = {
                            if (is48h) {
                                scheduled48hList.remove(visit.member.memberId)
                            } else {
                                scheduled48hList.add(visit.member.memberId)
                            }
                        },
                        onConsultProtocol = { query ->
                            onNavigateToAsk(query)
                        },
                        onOpenReferral = { dangerVisit ->
                            selectedDangerReferral = dangerVisit
                        }
                    )
                }
            }
        }
    }

    // 1-Tap Daily Summary Dialog
    DailySummaryDialog(
        isOpen = showDailySummary,
        onDismiss = { showDailySummary = false },
        totalHouseholds = members.size,
        completedVisits = completedVisits.toList(),
        dangerList = prioritizedVisits.filter { it.priorityGroup == PriorityGroup.DANGER },
        overdueCount = overdueCount,
        language = language,
        isDarkTheme = isDarkTheme
    )

    // Emergency Referral Slip Dialog for selected danger visit
    selectedDangerReferral?.let { dangerVisit ->
        ReferralSlipDialog(
            isOpen = true,
            onDismiss = { selectedDangerReferral = null },
            patientName = "${dangerVisit.member.name} (${dangerVisit.member.familyId})",
            dangerSign = dangerVisit.member.dangerFlag,
            category = dangerVisit.member.memberType,
            citation = "MoHFW Protocol • Induction Module for ASHAs / MCP Card",
            language = language,
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
fun TriageStatCard(
    title: String,
    count: Int,
    subtitle: String,
    containerColor: Color,
    accentColor: Color,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, accentColor, RoundedCornerShape(12.dp))
                else Modifier.border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Top Row: Icon badge + Bold number count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "$count",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            // Bottom Column: Title + Subtitle
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BeneficiaryVisitCard(
    visit: PrioritizedVisit,
    language: AppLanguage,
    isDone: Boolean,
    is48hScheduled: Boolean = false,
    isDarkTheme: Boolean,
    onToggleDone: () -> Unit,
    onToggle48h: () -> Unit = {},
    onConsultProtocol: (String) -> Unit,
    onOpenReferral: (PrioritizedVisit) -> Unit = {}
) {
    val member = visit.member
    val isDanger = visit.priorityGroup == PriorityGroup.DANGER
    val isOverdue = visit.priorityGroup == PriorityGroup.OVERDUE

    // Theme-aware colors
    val cardBg = when {
        isDone -> if (isDarkTheme) Color(0xFF0D281E) else Color(0xFFF0FDF4)
        isDanger -> if (isDarkTheme) Color(0xFF2C1318) else Color(0xFFFFF5F5)
        isOverdue -> if (isDarkTheme) Color(0xFF2B210F) else Color(0xFFFFFDF5)
        else -> if (isDarkTheme) SurfaceDark else Color.White
    }

    val borderColor = when {
        isDone -> if (isDarkTheme) SuccessGreenDark else SuccessGreen
        isDanger -> if (isDarkTheme) DangerRedDark else DangerRed
        isOverdue -> if (isDarkTheme) WarningAmberDark else WarningAmber
        else -> if (isDarkTheme) Color(0xFF24364D) else Color(0xFFE2E8F0)
    }

    val isPregnant = member.memberType.contains("PREGNANT", ignoreCase = true)
    val isNewborn = member.memberType.contains("NEWBORN", ignoreCase = true)

    val typeLabel = when {
        isPregnant -> Strings.memberPregnant(language)
        isNewborn -> Strings.memberNewborn(language)
        else -> Strings.memberChild(language)
    }

    val typeIcon = when {
        isPregnant -> Icons.Default.PregnantWoman
        isNewborn -> Icons.Default.ChildCare
        else -> Icons.Default.Vaccines
    }

    val typeBadgeColor = when {
        isPregnant -> Color(0xFF9333EA)
        isNewborn -> Color(0xFF0284C7)
        else -> Color(0xFF059669)
    }

    val hasDangerFlag = member.dangerFlag.isNotBlank() && !member.dangerFlag.equals("none", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(if (isDanger || isDone) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDanger) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Category Badge + Household ID + Priority Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(typeBadgeColor.copy(alpha = 0.15f))
                            .padding(5.dp)
                    ) {
                        Icon(
                            typeIcon,
                            contentDescription = null,
                            tint = typeBadgeColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = typeLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = typeBadgeColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Household ID Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDarkTheme) Color(0xFF1E2E42) else Color(0xFFF1F5F9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = member.familyId,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) TextPrimaryDark else Color(0xFF334155)
                        )
                    }

                    // Priority Pill
                    val priorityPillText = when (visit.priorityGroup) {
                        PriorityGroup.DANGER -> "🚨 ${Strings.statDangerTitle(language)}"
                        PriorityGroup.OVERDUE -> "⚠️ ${Strings.statOverdueTitle(language)}"
                        PriorityGroup.UPCOMING -> "📅 ${Strings.statRoutineTitle(language)}"
                    }
                    val priorityPillColor = when (visit.priorityGroup) {
                        PriorityGroup.DANGER -> if (isDarkTheme) DangerRedDark else DangerRed
                        PriorityGroup.OVERDUE -> if (isDarkTheme) WarningAmberDark else WarningAmber
                        PriorityGroup.UPCOMING -> if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                    }
                    Text(
                        text = priorityPillText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = priorityPillColor,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Beneficiary Name
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A),
                    fontSize = 16.sp
                )
            )

            // Dynamic 48-Hour Re-Visit Indicator Banner
            if (is48hScheduled) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDarkTheme) Color(0xFF3B270A) else Color(0xFFFEF3C7))
                        .border(1.dp, if (isDarkTheme) WarningAmberDark else Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.EventRepeat,
                            contentDescription = null,
                            tint = if (isDarkTheme) WarningAmberDark else Color(0xFFB45309),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.scheduled48hBadge(language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) WarningAmberDark else Color(0xFF92400E)
                        )
                    }
                }
            }

            // DANGER ALERT BANNER (If previous visit recorded a danger sign)
            if (hasDangerFlag) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) DangerRedDarkBg else DangerRedLight
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = if (isDarkTheme) DangerRedDark else DangerRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Strings.dangerFlagAlert(member.dangerFlag, language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = if (isDarkTheme) DangerRedDark else DangerRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onOpenReferral(visit) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isDarkTheme) DangerRedDark else DangerRed
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isDarkTheme) DangerRedDark else DangerRed
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.heightIn(min = 32.dp)
                            ) {
                                Text("Slip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { onConsultProtocol(member.dangerFlag) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkTheme) DangerRedDark else DangerRed
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.heightIn(min = 32.dp)
                            ) {
                                Text(Strings.btnReferNow(language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clinical Reason Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkTheme) SurfaceElevatedDark else Color(0xFFF8FAFC))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = visit.reason,
                    fontSize = 12.sp,
                    color = if (isDarkTheme) TextPrimaryDark else Color(0xFF334155),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1: Mark Visited (Interactive Checklist)
                OutlinedButton(
                    onClick = onToggleDone,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 38.dp),
                    contentPadding = PaddingValues(horizontal = 3.dp, vertical = 3.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDone) {
                            if (isDarkTheme) SuccessGreenDarkBg else SuccessGreenLight
                        } else Color.Transparent,
                        contentColor = if (isDone) {
                            if (isDarkTheme) SuccessGreenDark else SuccessGreen
                        } else {
                            if (isDarkTheme) TextPrimaryDark else Color.DarkGray
                        }
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        if (isDone) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isDone) Strings.btnDoneChecked(language) else Strings.btnMarkDone(language),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 12.sp
                    )
                }

                // Button 2: 48h Dynamic Re-Visit Stamping
                OutlinedButton(
                    onClick = onToggle48h,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 38.dp),
                    contentPadding = PaddingValues(horizontal = 3.dp, vertical = 3.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (is48hScheduled) {
                            if (isDarkTheme) Color(0xFF3B270A) else Color(0xFFFEF3C7)
                        } else Color.Transparent,
                        contentColor = if (is48hScheduled) {
                            if (isDarkTheme) WarningAmberDark else Color(0xFFB45309)
                        } else {
                            if (isDarkTheme) TextPrimaryDark else Color.DarkGray
                        }
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        if (is48hScheduled) Icons.Default.EventRepeat else Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = Strings.btnSchedule48h(language),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 12.sp
                    )
                }

                // Button 3: Protocol Check
                Button(
                    onClick = { onConsultProtocol(visit.reason) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 38.dp),
                    contentPadding = PaddingValues(horizontal = 3.dp, vertical = 3.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                        contentColor = if (isDarkTheme) Color(0xFF022C28) else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = Strings.btnViewProtocol(language),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}
