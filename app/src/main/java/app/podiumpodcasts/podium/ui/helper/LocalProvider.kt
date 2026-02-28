package app.podiumpodcasts.podium.ui.helper

import androidx.compose.runtime.compositionLocalOf
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.api.db.AppDatabase

val LocalDatabase = compositionLocalOf<AppDatabase> { null!! }
val LocalSettingsRepository = compositionLocalOf<SettingsRepository> { null!! }