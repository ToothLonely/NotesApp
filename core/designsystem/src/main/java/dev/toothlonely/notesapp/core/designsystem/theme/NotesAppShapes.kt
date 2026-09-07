package dev.toothlonely.notesapp.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

internal val NotesAppMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

object NotesAppShapes {
    val floating: Shape = RoundedCornerShape(36.dp)
    val floatingNavigation: Shape = RoundedCornerShape(32.dp)
    val full: Shape = CircleShape
}
