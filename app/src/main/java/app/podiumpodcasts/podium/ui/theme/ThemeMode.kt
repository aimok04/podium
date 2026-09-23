package app.podiumpodcasts.podium.ui.theme

enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromKey(key: String) = entries.find { it.key == key } ?: SYSTEM
    }
}