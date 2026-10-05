package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    private fun strokeIcon(name: String, block: androidx.compose.ui.graphics.vector.ImageVector.Builder.() -> Unit): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply(block).build()
    }

    val Schedule: ImageVector = strokeIcon("Schedule") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 2f)
            arcToRelative(10f, 10f, 0f, true, false, 0f, 20f)
            arcToRelative(10f, 10f, 0f, true, false, 0f, -20f)
            close()
            moveTo(12f, 6f)
            verticalLineTo(12f)
            lineTo(16f, 14f)
        }
    }

    val RemoveRedEye: ImageVector = strokeIcon("RemoveRedEye") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(1f, 12f)
            curveTo(3.5f, 7f, 7.5f, 4f, 12f, 4f)
            curveTo(16.5f, 4f, 20.5f, 7f, 23f, 12f)
            curveTo(20.5f, 17f, 16.5f, 20f, 12f, 20f)
            curveTo(7.5f, 20f, 3.5f, 17f, 1f, 12f)
            close()
            moveTo(12f, 9f)
            arcToRelative(3f, 3f, 0f, true, false, 0f, 6f)
            arcToRelative(3f, 3f, 0f, true, false, 0f, -6f)
            close()
        }
    }

    val OpenInFull: ImageVector = strokeIcon("OpenInFull") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(15f, 3f)
            horizontalLineTo(21f)
            verticalLineTo(9f)
            moveTo(9f, 21f)
            horizontalLineTo(3f)
            verticalLineTo(15f)
            moveTo(21f, 3f)
            lineTo(14f, 10f)
            moveTo(3f, 21f)
            lineTo(10f, 14f)
        }
    }

    val Code: ImageVector = strokeIcon("Code") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(16f, 18f)
            lineTo(22f, 12f)
            lineTo(16f, 6f)
            moveTo(8f, 6f)
            lineTo(2f, 12f)
            lineTo(8f, 18f)
        }
    }

    val OpenInNew: ImageVector = strokeIcon("OpenInNew") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(18f, 13f)
            verticalLineTo(19f)
            arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
            horizontalLineTo(5f)
            arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
            verticalLineTo(8f)
            arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
            horizontalLineTo(11f)
            moveTo(15f, 3f)
            horizontalLineTo(21f)
            verticalLineTo(9f)
            moveTo(10f, 14f)
            lineTo(21f, 3f)
        }
    }

    val ContentCopy: ImageVector = strokeIcon("ContentCopy") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9f, 9f)
            horizontalLineTo(20f)
            verticalLineTo(20f)
            horizontalLineTo(9f)
            close()
            moveTo(5f, 15f)
            horizontalLineTo(4f)
            verticalLineTo(4f)
            horizontalLineTo(15f)
            verticalLineTo(5f)
        }
    }

    val History: ImageVector = strokeIcon("History") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 12f)
            arcToRelative(9f, 9f, 0f, true, false, 3f, -6.7f)
            lineTo(3f, 8f)
            moveTo(3f, 3f)
            verticalLineTo(8f)
            horizontalLineTo(8f)
            moveTo(12f, 7f)
            verticalLineTo(12f)
            lineTo(15f, 14f)
        }
    }

    val Restore: ImageVector = strokeIcon("Restore") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 7f)
            verticalLineTo(13f)
            horizontalLineTo(9f)
            moveTo(3.5f, 13f)
            arcToRelative(9f, 9f, 0f, true, false, 2.5f, -7.5f)
            lineTo(3f, 13f)
        }
    }

    val Sync: ImageVector = strokeIcon("Sync") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 4f)
            verticalLineTo(10f)
            horizontalLineTo(15f)
            moveTo(3f, 20f)
            verticalLineTo(14f)
            horizontalLineTo(9f)
            moveTo(20.5f, 9f)
            arcToRelative(9f, 9f, 0f, false, false, -15f, -3.4f)
            lineTo(21f, 10f)
            moveTo(3.5f, 15f)
            arcToRelative(9f, 9f, 0f, false, false, 15f, 3.4f)
            lineTo(3f, 14f)
        }
    }

    val LightMode: ImageVector = strokeIcon("LightMode") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 8f)
            arcToRelative(4f, 4f, 0f, true, false, 0f, 8f)
            arcToRelative(4f, 4f, 0f, true, false, 0f, -8f)
            close()
            moveTo(12f, 2f)
            verticalLineTo(4f)
            moveTo(12f, 20f)
            verticalLineTo(22f)
            moveTo(4.93f, 4.93f)
            lineTo(6.34f, 6.34f)
            moveTo(17.66f, 17.66f)
            lineTo(19.07f, 19.07f)
            moveTo(2f, 12f)
            horizontalLineTo(4f)
            moveTo(20f, 12f)
            horizontalLineTo(22f)
            moveTo(6.34f, 17.66f)
            lineTo(4.93f, 19.07f)
            moveTo(19.07f, 4.93f)
            lineTo(17.66f, 6.34f)
        }
    }

    val DarkMode: ImageVector = strokeIcon("DarkMode") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21f, 12.79f)
            arcTo(9f, 9f, 0f, true, true, 11.21f, 3f)
            arcTo(7f, 7f, 0f, false, false, 21f, 12.79f)
            close()
        }
    }

    val ChatBubble: ImageVector = strokeIcon("ChatBubble") {
        path(
            fill = SolidColor(Color.Black)
        ) {
            moveTo(20f, 2f)
            horizontalLineTo(4f)
            curveTo(2.9f, 2f, 2f, 2.9f, 2f, 4f)
            verticalLineTo(22f)
            lineTo(6f, 18f)
            horizontalLineTo(20f)
            curveTo(21.1f, 18f, 22f, 17.1f, 22f, 16f)
            verticalLineTo(4f)
            curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f)
            close()
        }
    }

    val ArticleFilled: ImageVector = strokeIcon("ArticleFilled") {
        path(
            fill = SolidColor(Color.Black)
        ) {
            moveTo(19f, 3f)
            horizontalLineTo(5f)
            curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineTo(19f)
            curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
            horizontalLineTo(19f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            verticalLineTo(5f)
            curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
            close()
            moveTo(14f, 17f)
            horizontalLineTo(7f)
            verticalLineTo(15f)
            horizontalLineTo(14f)
            verticalLineTo(17f)
            close()
            moveTo(17f, 13f)
            horizontalLineTo(7f)
            verticalLineTo(11f)
            horizontalLineTo(17f)
            verticalLineTo(13f)
            close()
            moveTo(17f, 9f)
            horizontalLineTo(7f)
            verticalLineTo(7f)
            horizontalLineTo(17f)
            verticalLineTo(9f)
            close()
        }
    }

    val ArticleOutlined: ImageVector = strokeIcon("ArticleOutlined") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 3f)
            horizontalLineTo(19f)
            verticalLineTo(21f)
            horizontalLineTo(5f)
            close()
            moveTo(8f, 8f)
            horizontalLineTo(16f)
            moveTo(8f, 12f)
            horizontalLineTo(16f)
            moveTo(8f, 16f)
            horizontalLineTo(13f)
        }
    }

    val PhotoLibraryFilled: ImageVector = strokeIcon("PhotoLibraryFilled") {
        path(
            fill = SolidColor(Color.Black)
        ) {
            moveTo(22f, 16f)
            verticalLineTo(4f)
            curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f)
            horizontalLineTo(8f)
            curveTo(6.9f, 2f, 6f, 2.9f, 6f, 4f)
            verticalLineTo(16f)
            curveTo(6f, 17.1f, 6.9f, 18f, 8f, 18f)
            horizontalLineTo(20f)
            curveTo(21.1f, 18f, 22f, 17.1f, 22f, 16f)
            close()
            moveTo(11f, 12f)
            lineTo(13.03f, 14.71f)
            lineTo(16f, 11f)
            lineTo(20f, 16f)
            horizontalLineTo(8f)
            lineTo(11f, 12f)
            close()
            moveTo(2f, 6f)
            verticalLineTo(20f)
            curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f)
            horizontalLineTo(18f)
            verticalLineTo(20f)
            horizontalLineTo(4f)
            verticalLineTo(6f)
            horizontalLineTo(2f)
            close()
        }
    }

    val PhotoLibraryOutlined: ImageVector = strokeIcon("PhotoLibraryOutlined") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 3f)
            horizontalLineTo(21f)
            verticalLineTo(21f)
            horizontalLineTo(3f)
            close()
            moveTo(8.5f, 8.5f)
            arcToRelative(1.5f, 1.5f, 0f, true, false, 0f, 3f)
            arcToRelative(1.5f, 1.5f, 0f, true, false, 0f, -3f)
            close()
            moveTo(21f, 15f)
            lineTo(16f, 10f)
            lineTo(5f, 21f)
        }
    }

    val AdminFilled: ImageVector = strokeIcon("AdminFilled") {
        path(
            fill = SolidColor(Color.Black)
        ) {
            moveTo(12f, 1f)
            lineTo(3f, 5f)
            verticalLineTo(11f)
            curveTo(3f, 16.55f, 6.84f, 21.74f, 12f, 23f)
            curveTo(17.16f, 21.74f, 21f, 16.55f, 21f, 11f)
            verticalLineTo(5f)
            lineTo(12f, 1f)
            close()
            moveTo(12f, 7f)
            curveTo(13.66f, 7f, 15f, 8.34f, 15f, 10f)
            curveTo(15f, 11.66f, 13.66f, 13f, 12f, 13f)
            curveTo(10.34f, 13f, 9f, 11.66f, 9f, 10f)
            curveTo(9f, 8.34f, 10.34f, 7f, 12f, 7f)
            close()
            moveTo(18f, 17f)
            horizontalLineTo(6f)
            verticalLineTo(15.6f)
            curveTo(6f, 13.6f, 10f, 12.5f, 12f, 12.5f)
            curveTo(14f, 12.5f, 18f, 13.6f, 18f, 15.6f)
            verticalLineTo(17f)
            close()
        }
    }

    val AdminOutlined: ImageVector = strokeIcon("AdminOutlined") {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 22f)
            curveTo(17f, 20f, 20f, 16f, 20f, 11f)
            verticalLineTo(5f)
            lineTo(12f, 2f)
            lineTo(4f, 5f)
            verticalLineTo(11f)
            curveTo(4f, 16f, 7f, 20f, 12f, 22f)
            close()
            moveTo(12f, 8f)
            arcToRelative(2f, 2f, 0f, true, false, 0f, 4f)
            arcToRelative(2f, 2f, 0f, true, false, 0f, -4f)
            close()
            moveTo(8f, 16f)
            curveTo(9f, 14.5f, 15f, 14.5f, 16f, 16f)
        }
    }
}
