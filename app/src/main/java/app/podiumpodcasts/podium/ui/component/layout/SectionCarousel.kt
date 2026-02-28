package app.podiumpodcasts.podium.ui.component.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SectionCarousel(
    modifier: Modifier = Modifier,
    title: String,
    onClickExpand: () -> Unit,
    suffix: @Composable () -> Unit = { },
    content: LazyListScope.() -> Unit
) {
    Section(
        modifier = modifier,
        title = title,
        onClickExpand = onClickExpand
    ) {
        LazyRow(
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
            content = content
        )

        suffix()
    }
}