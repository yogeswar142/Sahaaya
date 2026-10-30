package com.sahaaya.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.sahaaya.companion.core.ClinicalTextFormatter
import com.sahaaya.companion.core.ProtocolChunk
import com.sahaaya.companion.ui.i18n.AppLanguage
import com.sahaaya.companion.ui.i18n.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    chunks: List<ProtocolChunk>,
    language: AppLanguage,
    isDarkTheme: Boolean,
    onNavigateToAsk: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    // Categorization logic based on official protocol ID prefixes
    fun getCategoryForChunk(chunk: ProtocolChunk): String {
        val id = chunk.id.uppercase()
        return when {
            id.startsWith("CHD") -> "CHD"
            id.startsWith("IMM") -> "IMM"
            id.startsWith("ANC") || id.startsWith("PNC") -> "MATERNAL"
            id.startsWith("ESC") -> "ESC"
            id.startsWith("SCP") -> "SCP"
            else -> "OTHER"
        }
    }

    val filteredChunks = remember(chunks, searchQuery, selectedCategory) {
        val catFiltered = when (selectedCategory) {
            "ALL" -> chunks
            "CHD" -> chunks.filter { getCategoryForChunk(it) == "CHD" }
            "IMM" -> chunks.filter { getCategoryForChunk(it) == "IMM" }
            "MATERNAL" -> chunks.filter { getCategoryForChunk(it) == "MATERNAL" }
            "ESC" -> chunks.filter { getCategoryForChunk(it) == "ESC" }
            "SCP" -> chunks.filter { getCategoryForChunk(it) == "SCP" }
            else -> chunks
        }

        if (searchQuery.isBlank()) {
            catFiltered
        } else {
            val q = searchQuery.trim().lowercase()
            catFiltered.filter {
                it.id.lowercase().contains(q) ||
                it.title.lowercase().contains(q) ||
                it.text.lowercase().contains(q) ||
                it.sourceDocument.lowercase().contains(q)
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
        // 1. Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.guideHeader(language),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) TextPrimaryDark else SecondaryNavy,
                        fontSize = 19.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Strings.guideSubheader(chunks.size, language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isDarkTheme) TextSecondaryDark else Color(0xFF64748B),
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Official Protocol Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkTheme) SurfaceElevatedDark else Color(0xFFE0F2F1))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${filteredChunks.size} / ${chunks.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    Strings.searchProtocolsPlaceholder(language),
                    fontSize = 12.5.sp,
                    color = if (isDarkTheme) TextSecondaryDark else Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search Protocols",
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

        // 3. Category Filter Chips Row (Clean, Professional, No Emojis)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == "ALL",
                onClick = { selectedCategory = "ALL" },
                label = { Text("${Strings.categoryAll(language)} (${chunks.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isDarkTheme) PrimaryTealDarkTheme.copy(alpha = 0.2f) else Color(0xFFE0F2F1),
                    selectedLabelColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                )
            )
            FilterChip(
                selected = selectedCategory == "CHD",
                onClick = { selectedCategory = "CHD" },
                label = { Text(Strings.categoryChild(language), fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isDarkTheme) Color(0xFF16382E) else Color(0xFFE6F4EA),
                    selectedLabelColor = if (isDarkTheme) SuccessGreenDark else SuccessGreen
                )
            )
            FilterChip(
                selected = selectedCategory == "IMM",
                onClick = { selectedCategory = "IMM" },
                label = { Text(Strings.categoryImm(language), fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isDarkTheme) Color(0xFF172B4D) else Color(0xFFEBF3FB),
                    selectedLabelColor = if (isDarkTheme) SecondaryNavyDarkTheme else SecondaryNavy
                )
            )
            FilterChip(
                selected = selectedCategory == "MATERNAL",
                onClick = { selectedCategory = "MATERNAL" },
                label = { Text(Strings.categoryMaternal(language), fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isDarkTheme) Color(0xFF321A45) else Color(0xFFF3E8FF),
                    selectedLabelColor = Color(0xFF9333EA)
                )
            )
            FilterChip(
                selected = selectedCategory == "ESC",
                onClick = { selectedCategory = "ESC" },
                label = { Text(Strings.categoryEmergency(language), fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isDarkTheme) DangerRedDarkBg else DangerRedLight,
                    selectedLabelColor = if (isDarkTheme) DangerRedDark else DangerRed
                )
            )
            FilterChip(
                selected = selectedCategory == "SCP",
                onClick = { selectedCategory = "SCP" },
                label = { Text(Strings.categoryScope(language), fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isDarkTheme) WarningAmberDarkBg else WarningAmberLight,
                    selectedLabelColor = if (isDarkTheme) WarningAmberDark else WarningAmber
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Protocol Cards List
        if (filteredChunks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.noProtocolsMatch(language),
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
                items(filteredChunks, key = { it.id }) { chunk ->
                    ProtocolChunkCard(
                        chunk = chunk,
                        language = language,
                        isDarkTheme = isDarkTheme,
                        onConsult = { onNavigateToAsk(chunk.title) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProtocolChunkCard(
    chunk: ProtocolChunk,
    language: AppLanguage,
    isDarkTheme: Boolean,
    onConsult: () -> Unit
) {
    var isExpanded by remember(chunk.id) { mutableStateOf(false) }

    val formattedPoints = remember(chunk.id) {
        ClinicalTextFormatter.formatToPoints(chunk.text)
    }

    // Category styling
    val id = chunk.id.uppercase()
    val (categoryName, badgeColor) = when {
        id.startsWith("CHD") -> Strings.categoryChild(language) to (if (isDarkTheme) SuccessGreenDark else SuccessGreen)
        id.startsWith("IMM") -> Strings.categoryImm(language) to (if (isDarkTheme) SecondaryNavyDarkTheme else SecondaryNavy)
        id.startsWith("ANC") || id.startsWith("PNC") -> Strings.categoryMaternal(language) to Color(0xFF9333EA)
        id.startsWith("ESC") -> Strings.categoryEmergency(language) to (if (isDarkTheme) DangerRedDark else DangerRed)
        id.startsWith("SCP") -> Strings.categoryScope(language) to (if (isDarkTheme) WarningAmberDark else WarningAmber)
        else -> "MoHFW Protocol" to (if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isDarkTheme) Color(0xFF24364D) else Color(0xFFE2E8F0),
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) SurfaceDark else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Section ID Badge & Category Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDarkTheme) SurfaceElevatedDark else Color(0xFFF1F5F9))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = chunk.id,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                Text(
                    text = categoryName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) TextSecondaryDark else Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Protocol Title
            Text(
                text = chunk.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = if (isDarkTheme) TextPrimaryDark else Color(0xFF0F172A)
                )
            )

            // Official MoHFW Citation
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${chunk.sourceDocument}, p. ${chunk.pageNumber}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) TextSecondaryDark else Color(0xFF475569)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinical Protocol Bullet Points
            val displayPoints = if (isExpanded || formattedPoints.size <= 2) {
                formattedPoints
            } else {
                formattedPoints.take(2)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDarkTheme) Color(0xFF0D1B2A) else Color(0xFFF8FAFC))
                    .padding(10.dp)
            ) {
                Column {
                    displayPoints.forEach { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = if (isDarkTheme) TextPrimaryDark else Color(0xFF1E293B)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Expand / Collapse Toggle if more than 2 points
            if (formattedPoints.size > 2) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.heightIn(min = 32.dp)
                ) {
                    Text(
                        text = if (isExpanded) {
                            Strings.btnHideDetailedProtocol(language)
                        } else {
                            Strings.btnViewDetailedProtocol(formattedPoints.size, language)
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Query this protocol in Ask Sahaaya
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onConsult,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.heightIn(min = 36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                    )
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Strings.btnAskAboutThis(language),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
