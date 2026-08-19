package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val gradient: List<Color>
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Default.Headphones,
        title = "Bem-vindo ao Brasa Player Aurora",
        description = "Seu player de música offline com áudio de alta qualidade, equalização profissional e personalização total.",
        gradient = listOf(Color(0xFF6C3CE1), Color(0xFF00F0FF))
    ),
    OnboardingPage(
        icon = Icons.Default.Equalizer,
        title = "Equalizador Profissional",
        description = "5 bandas de frequência, bass boost, virtualizer 3D e balanço de canal para moldar o som do seu jeito.",
        gradient = listOf(Color(0xFFFF007F), Color(0xFFFFD166))
    ),
    OnboardingPage(
        icon = Icons.AutoMirrored.Filled.QueueMusic,
        title = "Playlists Inteligentes",
        description = "Organize suas músicas em playlists personalizadas, acesse favoritas, recentes e mais tocadas com um toque.",
        gradient = listOf(Color(0xFF2979FF), Color(0xFF00E5FF))
    ),
    OnboardingPage(
        icon = Icons.Default.AutoAwesome,
        title = "Temas & Visualizador",
        description = "Mais de 15 temas prontos, editor de paleta personalizado, Material You e visualizador de áudio animado.",
        gradient = listOf(Color(0xFFFF6B35), Color(0xFFF72585))
    )
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage

    // Animated gradient rotation
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_rotation")
    val gradientAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientAngle"
    )

    val isLastPage = currentPage == onboardingPages.size - 1

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Animated gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            onboardingPages[currentPage].gradient[0].copy(alpha = 0.15f),
                            onboardingPages[currentPage].gradient[1].copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background,
                            onboardingPages[currentPage].gradient[0].copy(alpha = 0.12f)
                        ),
                        center = Offset(
                            0.5f + 0.3f * Math.cos(Math.toRadians(gradientAngle.toDouble())).toFloat(),
                            0.5f + 0.3f * Math.sin(Math.toRadians(gradientAngle.toDouble())).toFloat()
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isLastPage) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Pular", fontSize = 13.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val pageData = onboardingPages[page]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon with gradient background
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(24.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(pageData.gradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = pageData.icon,
                            contentDescription = pageData.title,
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Title
                    Text(
                        text = pageData.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = pageData.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Bottom section: dots + button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page indicators
                Row(
                    modifier = Modifier.semantics {
                        contentDescription = "Página ${currentPage + 1} de ${onboardingPages.size}"
                    },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(onboardingPages.size) { index ->
                        val isSelected = currentPage == index
                        val width by animateFloatAsState(
                            targetValue = if (isSelected) 28f else 8f,
                            animationSpec = tween(300),
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .width(width.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(onboardingPages[currentPage].gradient)
                                    } else {
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            )
                                        )
                                    }
                                )
                                .semantics {
                                    contentDescription = if (isSelected) "Página ${index + 1}, selecionada" else "Página ${index + 1}"
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Button
                Button(
                    onClick = {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = onboardingPages[currentPage].gradient[0]
                    )
                ) {
                    Icon(
                        imageVector = if (isLastPage) Icons.Default.PlayArrow else Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLastPage) "Começar a Ouvir" else "Próximo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
