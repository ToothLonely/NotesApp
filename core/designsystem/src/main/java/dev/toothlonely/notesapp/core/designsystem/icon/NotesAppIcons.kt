package dev.toothlonely.notesapp.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object NotesAppIcons {
    val Add: ImageVector by lazy {
        ImageVector.Builder(
            name = "Add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 13f)
                horizontalLineTo(13f)
                verticalLineTo(19f)
                horizontalLineTo(11f)
                verticalLineTo(13f)
                horizontalLineTo(5f)
                verticalLineTo(11f)
                horizontalLineTo(11f)
                verticalLineTo(5f)
                horizontalLineTo(13f)
                verticalLineTo(11f)
                horizontalLineTo(19f)
                close()
            }
        }.build()
    }

    val Back: ImageVector by lazy {
        ImageVector.Builder(
            name = "Back",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = true,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 11f)
                horizontalLineTo(7.83f)
                lineTo(13.42f, 5.41f)
                lineTo(12f, 4f)
                lineTo(4f, 12f)
                lineTo(12f, 20f)
                lineTo(13.42f, 18.59f)
                lineTo(7.83f, 13f)
                horizontalLineTo(20f)
                close()
            }
        }.build()
    }

    val Note: ImageVector by lazy {
        ImageVector.Builder(
            name = "Note",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(6f, 2.5f)
                horizontalLineTo(14f)
                lineTo(19.5f, 8f)
                verticalLineTo(20f)
                curveTo(19.5f, 20.83f, 18.83f, 21.5f, 18f, 21.5f)
                horizontalLineTo(6f)
                curveTo(5.17f, 21.5f, 4.5f, 20.83f, 4.5f, 20f)
                verticalLineTo(4f)
                curveTo(4.5f, 3.17f, 5.17f, 2.5f, 6f, 2.5f)
                close()
                moveTo(14f, 2.5f)
                verticalLineTo(8f)
                horizontalLineTo(19.5f)
                moveTo(8f, 13f)
                horizontalLineTo(16f)
                moveTo(8f, 17f)
                horizontalLineTo(14f)
            }
        }.build()
    }
}
