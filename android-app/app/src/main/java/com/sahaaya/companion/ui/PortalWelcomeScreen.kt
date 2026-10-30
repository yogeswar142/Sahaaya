package com.sahaaya.companion.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahaaya.companion.R
import com.sahaaya.companion.ui.i18n.AppLanguage

@Composable
fun PortalWelcomeScreen(
    language: AppLanguage,
    isDarkTheme: Boolean,
    onEnterPortal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDarkTheme) BackgroundDark else Color.White
    val logoRes = if (isDarkTheme) R.drawable.ic_sahaaya_logo else R.drawable.ic_sahaaya_symbol_bright

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Rounded Logo Container matching reference image
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        width = 2.dp,
                        color = if (isDarkTheme) PrimaryTealDarkTheme.copy(alpha = 0.5f) else Color(0xFFB2DFDB),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .background(if (isDarkTheme) SurfaceElevatedDark else Color.White)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Sahaaya Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // App Heading
            Text(
                text = "SAHAAYA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Frontline Health Companion",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = if (isDarkTheme) TextSecondaryDark else Color(0xFF5A7184)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Enter Portal Button with arrow
            Button(
                onClick = onEnterPortal,
                modifier = Modifier
                    .height(52.dp)
                    .width(220.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkTheme) PrimaryTealDarkTheme else PrimaryTeal
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Enter Portal",
                        color = if (isDarkTheme) Color(0xFF042F2C) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = if (isDarkTheme) Color(0xFF042F2C) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = "Offline-First • Chandrapuri Block",
                fontSize = 13.sp,
                color = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
