package com.planner.app.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planner.app.core.theme.ShapeCard
import com.planner.app.core.theme.ShapePill
import kotlinx.coroutines.launch

private val slides = listOf(
    Triple("Track what matters", "Build a flexible system around your habits — not the other way around.", "📋"),
    Triple("Log in seconds", "Quick status updates, smart forms for complex workouts, or just tap Done.", "⚡"),
    Triple("See your patterns", "Streaks, compliance heatmaps, and AI-powered weekly insights.", "📊"),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(pageCount = { slides.size + 1 })
    val scope = rememberCoroutineScope()
    val selectedPresets = remember { mutableStateListOf<PresetTemplate>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            if (page < slides.size) {
                SlidePage(
                    emoji = slides[page].third,
                    title = slides[page].first,
                    description = slides[page].second,
                )
            } else {
                PresetPickerPage(
                    selected = selectedPresets,
                    onToggle = { preset ->
                        if (selectedPresets.contains(preset)) selectedPresets.remove(preset)
                        else selectedPresets.add(preset)
                    },
                )
            }
        }

        // Page dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(slides.size + 1) { index ->
                val color by animateColorAsState(
                    if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    label = "DotColor",
                )
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }

        // CTA button
        Button(
            onClick = {
                if (pagerState.currentPage < slides.size) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    viewModel.completeOnboarding(selectedPresets)
                    onDone()
                }
            },
            shape = ShapePill,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 32.dp)
                .height(52.dp),
        ) {
            Text(
                text = if (pagerState.currentPage < slides.size) "Continue" else "Get started",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun SlidePage(emoji: String, title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = emoji, style = MaterialTheme.typography.displayMedium.copy(fontSize = MaterialTheme.typography.displayMedium.fontSize * 2))
        Spacer(Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PresetPickerPage(
    selected: List<PresetTemplate>,
    onToggle: (PresetTemplate) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Pick a few to start",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "You can add more later, or start from scratch.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(defaultPresets) { preset ->
                val isSelected = selected.contains(preset)
                Surface(
                    shape = ShapeCard,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .then(
                            if (isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, ShapeCard)
                            else Modifier
                        )
                        .clickable { onToggle(preset) },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(preset.emoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}
