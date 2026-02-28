package app.podiumpodcasts.podium.ui.vm

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.PodcastHistoryBundle
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class HistoryViewModel(
    val db: AppDatabase
) : ViewModel() {

    val lazyListState = LazyListState()

    private val now = LocalDate.now()
    private val zone = ZoneId.systemDefault()

    private fun LocalDate.toMillis() = this.atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()

    val startOfToday = now
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()

    val startOfWeek = now
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .toMillis()

    val startOfMonth = LocalDate.now()
        .with(TemporalAdjusters.firstDayOfMonth())
        .toMillis()

    val startOfYear = LocalDate.now()
        .withDayOfYear(1)
        .toMillis()

    val historyElements = Pager(
        PagingConfig(
            pageSize = 30
        )
    ) {
        db.podcastHistory()
            .all()
    }.flow

    val todayPager = Pager(
        PagingConfig(
            pageSize = 30
        )
    ) {
        db.podcastHistory()
            .allAfter(startOfToday)
    }.flow

    val weekPager = Pager(
        PagingConfig(
            pageSize = 30
        )
    ) {
        db.podcastHistory()
            .allIn(startOfToday, startOfWeek)
    }.flow

    val monthPager = Pager(
        PagingConfig(
            pageSize = 30
        )
    ) {
        db.podcastHistory()
            .allIn(startOfWeek, startOfMonth)
    }.flow

    val yearPager = Pager(
        PagingConfig(
            pageSize = 30
        )
    ) {
        db.podcastHistory()
            .allIn(startOfMonth, startOfYear)
    }.flow

    val olderPager = Pager(
        PagingConfig(
            pageSize = 30
        )
    ) {
        db.podcastHistory()
            .allBefore(startOfYear)
    }.flow

    fun delete(element: PodcastHistoryBundle) {
        viewModelScope.launch {
            db.podcastHistory()
                .delete(element.history)
        }
    }

}