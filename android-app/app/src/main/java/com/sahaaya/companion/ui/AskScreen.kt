package com.sahaaya.companion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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
import com.sahaaya.companion.core.PipelineOutput
import com.sahaaya.companion.core.SahaayaPipeline

@Composable
fun AskScreen(
    pipeline: SahaayaPipeline,
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf("") }
    var currentOutput by remember { mutableStateOf<PipelineOutput?>(null) }

    val quickPrompts = listOf(
        "Vaginal bleeding pregnant",
        "Baby ribs sinking fast breathing",
        "How much ORS for diarrhoea",
        "What vaccines at birth",
        "Amoxicillin syrup dose 1 yr"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Ask Sahaaya Protocol Guide",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy
            )
        )
        Text(
            text = "100% Offline | MoHFW & NHM Protocols | Safety-First Escalation",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Query input field
        OutlinedTextField(
            value = queryText,
            onValueChange = { queryText = it },
            placeholder = { Text("Enter symptom, sign or question (English / Hinglish / Telugu)...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (queryText.isNotBlank()) {
                            currentOutput = pipeline.process(queryText.trim())
                        }
                    }
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryTeal)
                }
            }
        )

        // Ask Button (Big buttons, short text)
        Button(
            onClick = {
                if (queryText.isNotBlank()) {
                    currentOutput = pipeline.process(queryText.trim())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "SEARCH PROTOCOL",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Quick symptom test chips
        Text(
            text = "Quick Sample Queries:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.take(2).forEach { prompt ->
                SuggestionChip(
                    onClick = {
                        queryText = prompt
                        currentOutput = pipeline.process(prompt)
                    },
                    label = { Text(prompt, fontSize = 12.sp, maxLines = 1) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.drop(2).take(2).forEach { prompt ->
                SuggestionChip(
                    onClick = {
                        queryText = prompt
                        currentOutput = pipeline.process(prompt)
                    },
                    label = { Text(prompt, fontSize = 12.sp, maxLines = 1) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Output Display Area
        currentOutput?.let { output ->
            when (output) {
                is PipelineOutput.DangerEscalation -> {
                    // Bright Red Card for Emergency Escalation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DangerRedLight),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Danger",
                                    tint = DangerRed,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DangerRed)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "EMERGENCY: ${output.actionText}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Danger Sign Detected: ${output.rule.dangerSign}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed
                                )
                            )
                            Text(
                                text = "Category: ${output.rule.category.uppercase()} EMERGENCY",
                                style = MaterialTheme.typography.labelMedium.copy(color = Color.DarkGray)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Immediate Action: Escort woman/child immediately to the nearest First Referral Unit (FRU) or Hospital. Do not wait or self-treat.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = DangerRed.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = output.citation,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryNavy
                                )
                            )
                        }
                    }
                }

                is PipelineOutput.ScopeRefusal -> {
                    // Amber Card for Out-of-scope prescription/dosage refusal
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = WarningAmberLight),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Refusal",
                                    tint = WarningAmber,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "OUT OF SCOPE - CONSULT DOCTOR",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = output.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Protocol Safety Policy: Frontline ASHA/ANM workers operate within community protocol boundaries. Prescribing antibiotics and clinical dose calculations are strictly reserved for Medical Officers.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                            )
                        }
                    }
                }

                is PipelineOutput.ProtocolAnswer -> {
                    // Green / Teal Card for Grounded Protocol Answer
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = output.chunk.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryTeal
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = output.chunk.text,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreenLight)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = output.citationText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessGreen
                                    )
                                )
                            }
                        }
                    }
                }

                is PipelineOutput.OutOfCorpus -> {
                    // Grey Card for questions outside verified protocol corpus
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = output.message,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sahaaya strictly avoids hallucinating answers beyond verified government training manuals.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    }
                }
            }
        }
    }
}
