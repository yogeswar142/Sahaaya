package com.sahaaya.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahaaya.companion.core.PrioritizedVisit
import com.sahaaya.companion.core.PriorityGroup
import com.sahaaya.companion.core.RegisterMember
import com.sahaaya.companion.core.VisitListEngine
import java.time.LocalDate

@Composable
fun VisitsScreen(
    members: List<RegisterMember>,
    visitEngine: VisitListEngine,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.parse("2026-10-18") }
    val prioritizedVisits = remember(members) {
        visitEngine.prioritizeVisits(members, today)
    }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val dangerCount = prioritizedVisits.count { it.priorityGroup == PriorityGroup.DANGER }
    val overdueCount = prioritizedVisits.count { it.priorityGroup == PriorityGroup.OVERDUE }
    val upcomingCount = prioritizedVisits.count { it.priorityGroup == PriorityGroup.UPCOMING }

    val filteredList = when (selectedFilter) {
        "DANGER" -> prioritizedVisits.filter { it.priorityGroup == PriorityGroup.DANGER }
        "OVERDUE" -> prioritizedVisits.filter { it.priorityGroup == PriorityGroup.OVERDUE }
        "UPCOMING" -> prioritizedVisits.filter { it.priorityGroup == PriorityGroup.UPCOMING }
        else -> prioritizedVisits
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        Text(
            text = "Today's Prioritised Visits",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy
            )
        )
        Text(
            text = "Date: 18 Oct 2026 | Register: ${members.size} Beneficiaries",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Summary Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCounterBadge(
                title = "DANGER",
                count = dangerCount,
                bgColor = DangerRedLight,
                textColor = DangerRed,
                modifier = Modifier.weight(1f),
                onClick = { selectedFilter = "DANGER" }
            )
            SummaryCounterBadge(
                title = "OVERDUE",
                count = overdueCount,
                bgColor = WarningAmberLight,
                textColor = WarningAmber,
                modifier = Modifier.weight(1f),
                onClick = { selectedFilter = "OVERDUE" }
            )
            SummaryCounterBadge(
                title = "UPCOMING",
                count = upcomingCount,
                bgColor = Color(0xFFE3F2FD),
                textColor = Color(0xFF1976D2),
                modifier = Modifier.weight(1f),
                onClick = { selectedFilter = "UPCOMING" }
            )
        }

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${prioritizedVisits.size})", fontSize = 12.sp) }
            )
            FilterChip(
                selected = selectedFilter == "DANGER",
                onClick = { selectedFilter = "DANGER" },
                label = { Text("Danger ($dangerCount)", fontSize = 12.sp) }
            )
            FilterChip(
                selected = selectedFilter == "OVERDUE",
                onClick = { selectedFilter = "OVERDUE" },
                label = { Text("Overdue ($overdueCount)", fontSize = 12.sp) }
            )
            FilterChip(
                selected = selectedFilter == "UPCOMING",
                onClick = { selectedFilter = "UPCOMING" },
                label = { Text("Upcoming ($upcomingCount)", fontSize = 12.sp) }
            )
        }

        // Visits list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList) { visit ->
                VisitItemCard(visit)
            }
        }
    }
}

@Composable
fun SummaryCounterBadge(
    title: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun VisitItemCard(visit: PrioritizedVisit) {
    val (badgeBg, badgeText) = when (visit.priorityGroup) {
        PriorityGroup.DANGER -> Pair(DangerRed, Color.White)
        PriorityGroup.OVERDUE -> Pair(WarningAmber, Color.White)
        PriorityGroup.UPCOMING -> Pair(Color(0xFF1976D2), Color.White)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = visit.member.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecondaryNavy
                        )
                    )
                    Text(
                        text = "${visit.member.memberType.replace('_', ' ').uppercase()} | ${visit.member.familyId}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = visit.urgencyBadge,
                        color = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actionable one-line reason
            Row(verticalAlignment = Alignment.Top) {
                val icon = if (visit.priorityGroup == PriorityGroup.DANGER) Icons.Default.Warning else Icons.Default.NotificationsActive
                val iconTint = if (visit.priorityGroup == PriorityGroup.DANGER) DangerRed else WarningAmber
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = visit.reason,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (visit.priorityGroup == PriorityGroup.DANGER) FontWeight.Bold else FontWeight.Normal,
                        color = if (visit.priorityGroup == PriorityGroup.DANGER) DangerRed else Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Due: ${visit.member.dueDate}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                )
                Text(
                    text = "Last Visit: ${visit.member.lastVisitDate}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                )
            }
        }
    }
}
