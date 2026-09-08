package dev.toothlonely.notesapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.R
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppMotion
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSplashScreen(
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
    val scale = remember(isPreview) {
        Animatable(if (isPreview) SPLASH_SETTLED_SCALE else SPLASH_INITIAL_SCALE)
    }
    val alpha = remember { Animatable(SPLASH_VISIBLE_ALPHA) }
    val currentOnAnimationFinished by rememberUpdatedState(onAnimationFinished)

    LaunchedEffect(isPreview) {
        if (isPreview) return@LaunchedEffect

        scale.animateTo(
            targetValue = SPLASH_SETTLED_SCALE,
            animationSpec = tween(
                durationMillis = NotesAppMotion.splashEnterMillis,
                easing = FastOutSlowInEasing,
            ),
        )
        delay(NotesAppMotion.splashHoldMillis)
        coroutineScope {
            launch {
                scale.animateTo(
                    targetValue = SPLASH_EXIT_SCALE,
                    animationSpec = tween(
                        durationMillis = NotesAppMotion.splashExitMillis,
                        easing = FastOutLinearInEasing,
                    ),
                )
            }
            launch {
                alpha.animateTo(
                    targetValue = SPLASH_HIDDEN_ALPHA,
                    animationSpec = tween(NotesAppMotion.splashExitMillis),
                )
            }
        }
        currentOnAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .graphicsLayer { this.alpha = alpha.value },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(NotesAppSizes.splashIcon)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimatedSplashScreenPreview() {
    NotesAppTheme {
        AnimatedSplashScreen(onAnimationFinished = {})
    }
}

private const val SPLASH_INITIAL_SCALE = 0f
private const val SPLASH_SETTLED_SCALE = 1f
private const val SPLASH_EXIT_SCALE = 2f
private const val SPLASH_VISIBLE_ALPHA = 1f
private const val SPLASH_HIDDEN_ALPHA = 0f
