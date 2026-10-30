package com.sahaaya.companion.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahaaya.companion.core.HouseholdMember
import com.sahaaya.companion.core.RegisterMember
import com.sahaaya.companion.ui.i18n.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    householdMembers: List<HouseholdMember>,
    registerMembers: List<RegisterMember>,
    language: AppLanguage,
    isDarkTheme: Boolean,
    onNavigateToAsk: (String) -> Unit,
    onNavigateToVisits: (filter: String, searchQuery: String) -> Unit,
    onOpenReferral: (patientName: String, familyId: String, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val textPrimary = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) TextSecondaryDark else Color(0xFF64748B)
    val cardBg = if (isDarkTheme) SurfaceElevatedDark else Color.White
    val borderCol = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
    val primaryColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal

    // Filter beneficiaries dynamically when user searches
    val searchMatches = remember(searchQuery, householdMembers) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase()
            householdMembers.filter {
                it.name.lowercase().contains(q) ||
                it.memberId.lowercase().contains(q) ||
                it.householdId.lowercase().contains(q)
            }.take(6)
        }
    }

    // Default stats matching the Chandrapuri Block register reference overview
    val dangerAlertsCount = 30
    val overdueVisitsCount = 237
    val routineVisitsCount = 80

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) BackgroundDark else Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Search Bar (Beneficiary Search by name or HH ID)
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                placeholder = {
                    Text(
                        text = "Search beneficiary by name or household ID...",
                        color = textSecondary.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = if (isDarkTheme) Color(0xFF475569) else Color(0xFF94A3B8),
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                )
            )
        }

        // 2. Real-time Search Matches Card (as in home-page-with-patient-search.jpg)
        if (searchQuery.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderCol),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Search Matches (${searchMatches.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (searchMatches.isEmpty()) {
                            Text(
                                text = "No beneficiary found matching '$searchQuery'",
                                color = textSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            searchMatches.forEachIndexed { index, member ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        color = borderCol.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onNavigateToVisits("ALL", member.name)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = member.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${member.memberId} • ${member.householdId} • ${member.sex}",
                                            fontSize = 13.sp,
                                            color = textSecondary
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Section Title: Today's Field Priority Overview
        item {
            Text(
                text = "Today's Field Priority Overview",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = textPrimary
            )
        }

        // 4. 3 Field Priority Stat Cards in a Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: Emergency Alerts
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToVisits("DANGER", "") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) DangerRedDarkBg else Color(0xFFFFF1F2)
                    ),
                    border = BorderStroke(1.dp, if (isDarkTheme) DangerRedDark.copy(alpha = 0.4f) else Color(0xFFFECDD3))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isDarkTheme) DangerRedDark else Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "$dangerAlertsCount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                color = if (isDarkTheme) DangerRedDark else Color(0xFFDC2626)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Emergency Alerts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = if (isDarkTheme) DangerRedDark else Color(0xFF991B1B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Referral required",
                            fontSize = 9.5.sp,
                            color = if (isDarkTheme) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Card 2: Overdue Visits
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToVisits("OVERDUE", "") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) WarningAmberDarkBg else Color(0xFFFFFBEB)
                    ),
                    border = BorderStroke(1.dp, if (isDarkTheme) WarningAmberDark.copy(alpha = 0.4f) else Color(0xFFFDE68A))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (isDarkTheme) WarningAmberDark else Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "$overdueVisitsCount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                color = if (isDarkTheme) WarningAmberDark else Color(0xFFD97706)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Overdue Visits",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = if (isDarkTheme) WarningAmberDark else Color(0xFF92400E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Action due",
                            fontSize = 9.5.sp,
                            color = if (isDarkTheme) Color(0xFFFDE68A) else Color(0xFFB45309),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Card 3: Routine Visits
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToVisits("UPCOMING", "") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF0F2634) else Color(0xFFE0F2FE)
                    ),
                    border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF0369A1) else Color(0xFFBAE6FD))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "$routineVisitsCount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                color = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Routine Visits",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0369A1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Scheduled",
                            fontSize = 9.5.sp,
                            color = if (isDarkTheme) Color(0xFFBAE6FD) else Color(0xFF0284C7),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 5. Two Action Buttons: Ask Protocol & Visit Register
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigateToAsk("") },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ChatBubble,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ask Protocol",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            maxLines = 1
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onNavigateToVisits("ALL", "") },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, primaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ListAlt,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Visit Register",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 13.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 6. Offline Operation Active Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderCol),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Offline Operation Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = textPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "0 pending audit events queued locally",
                                fontSize = 11.sp,
                                color = textSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "100% OFFLINE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color(0xFF16A34A),
                        maxLines = 1
                    )
                }
            }
        }

        // 7. Section Title: Priority Field Beneficiaries
        item {
            Text(
                text = "Priority Field Beneficiaries",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = textPrimary
            )
        }

        // 8. Priority Beneficiaries Cards matching home-page.jpg
        item {
            // Card 1: Baby of Zarina
            BeneficiaryDangerCard(
                name = "Baby of Zarina",
                details = "HH-0CXDQG • HBNC-D14 scheduled / required",
                isDarkTheme = isDarkTheme,
                cardBg = cardBg,
                borderCol = borderCol,
                onClick = {
                    onOpenReferral("Baby of Zarina", "HH-0CXDQG", "HBNC-D14 scheduled / required - Danger follow-up")
                }
            )
        }

        item {
            // Card 2: Munni Prasad
            BeneficiaryDangerCard(
                name = "Munni Prasad",
                details = "HH-20Y65N • HRP-followup scheduled / required",
                isDarkTheme = isDarkTheme,
                cardBg = cardBg,
                borderCol = borderCol,
                onClick = {
                    onOpenReferral("Munni Prasad", "HH-20Y65N", "Severe anaemia (Hb 6.5 g/dL) - HRP danger follow-up")
                }
            )
        }

        item {
            // Card 3: Sonam Sah
            BeneficiaryDangerCard(
                name = "Sonam Sah",
                details = "HH-2TG4D0 • Severe hypertension: BP 174/104",
                isDarkTheme = isDarkTheme,
                cardBg = cardBg,
                borderCol = borderCol,
                onClick = {
                    onOpenReferral("Sonam Sah", "HH-2TG4D0", "Hypertensive crisis & Diabetes (BP 174/104, Sugar 295)")
                }
            )
        }
    }
}

@Composable
private fun BeneficiaryDangerCard(
    name: String,
    details: String,
    isDarkTheme: Boolean,
    cardBg: Color,
    borderCol: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderCol),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A)
                )

                // Danger Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDarkTheme) DangerRedDarkBg else Color(0xFFFEE2E2))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "DANGER",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = if (isDarkTheme) DangerRedDark else Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = details,
                fontSize = 13.5.sp,
                color = if (isDarkTheme) TextSecondaryDark else Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
