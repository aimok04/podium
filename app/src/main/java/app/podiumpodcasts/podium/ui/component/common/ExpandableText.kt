package app.podiumpodcasts.podium.ui.component.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    autoSize: TextAutoSize? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    minAutoSize: TextUnit? = null,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val expandedState = remember { mutableStateOf(false) }
    val hasVisualOverflow = remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = expandedState.value && hasVisualOverflow.value
    ) { expanded ->
        Text(
            text = text,
            color = color,
            modifier = if(hasVisualOverflow.value) modifier.clickable {
                expandedState.value = !expandedState.value
            } else modifier,
            autoSize = if(expanded)
                null
            else
                autoSize,
            fontSize = if(expanded && minAutoSize != null)
                minAutoSize
            else
                fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = if(expanded) Int.MAX_VALUE else maxLines,
            minLines = minLines,
            onTextLayout = {
                onTextLayout?.invoke(it)

                if(expanded) return@Text
                hasVisualOverflow.value = it.hasVisualOverflow
            },
            style = style
        )
    }
}