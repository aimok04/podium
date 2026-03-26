package app.podiumpodcasts.podium.ui.custom.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Rounded.Forward: ImageVector
    get() {
        if(_forward != null) {
            return _forward!!
        }
        _forward = materialIcon(name = "Rounded.Forward") {
            materialPath {
                moveTo(18.92f, 13.0f)
                curveToRelative(-0.5f, 0.0f, -0.91f, 0.37f, -0.98f, 0.86f)
                curveToRelative(-0.48f, 3.37f, -3.77f, 5.84f, -7.42f, 4.96f)
                curveToRelative(-2.25f, -0.54f, -3.91f, -2.27f, -4.39f, -4.53f)
                curveTo(5.32f, 10.42f, 8.27f, 7.0f, 12.0f, 7.0f)
                verticalLineToRelative(2.79f)
                curveToRelative(0.0f, 0.45f, 0.54f, 0.67f, 0.85f, 0.35f)
                lineToRelative(3.79f, -3.79f)
                curveToRelative(0.2f, -0.2f, 0.2f, -0.51f, 0.0f, -0.71f)
                lineToRelative(-3.79f, -3.79f)
                curveToRelative(-0.31f, -0.31f, -0.85f, -0.09f, -0.85f, 0.36f)
                lineTo(12.0f, 5.0f)
                curveToRelative(-4.94f, 0.0f, -8.84f, 4.48f, -7.84f, 9.6f)
                curveToRelative(0.6f, 3.11f, 2.9f, 5.5f, 5.99f, 6.19f)
                curveToRelative(4.83f, 1.08f, 9.15f, -2.2f, 9.77f, -6.67f)
                curveToRelative(0.09f, -0.59f, -0.4f, -1.12f, -1.0f, -1.12f)
                close()
            }
        }
        return _forward!!
    }

private var _forward: ImageVector? = null