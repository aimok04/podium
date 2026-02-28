package app.podiumpodcasts.podium.ui.vm.discover

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.apple.ApplePodcastClient
import app.podiumpodcasts.podium.api.apple.model.Genre
import app.podiumpodcasts.podium.api.model.PodcastPreviewModel
import app.podiumpodcasts.podium.ui.dialog.CountryCodeSelectorState
import app.podiumpodcasts.podium.ui.dialog.bottomsheet.PodcastPreviewBottomSheetState
import kotlinx.coroutines.launch

enum class Topics(
    @StringRes val label: Int,
    val genre: Genre?
) {
    ALL(R.string.topic_all, null),
    NEWS(R.string.topic_news, Genre.NEWS),
    CULTURE(R.string.topic_culture, Genre.CULTURE),
    EDUCATION(R.string.topic_education, Genre.EDUCATION),
    COMEDY(R.string.topic_comedy, Genre.COMEDY),
    TECHNOLOGY(R.string.topic_technology, Genre.TECHNOLOGY),
    SCIENCE(R.string.topic_science, Genre.SCIENCE),
    TRUE_CRIME(R.string.topic_true_crime, Genre.TRUE_CRIME),
    HEALTH_AND_FITNESS(R.string.topic_health_and_fitness, Genre.HEALTH_AND_FITNESS),
    BUSINESS(R.string.topic_business, Genre.BUSINESS),
    DOCUMENTARY(R.string.topic_documentary, Genre.DOCUMENTARY),
    HISTORY(R.string.topic_history, Genre.HISTORY),
    PLACES_AND_TRAVEL(R.string.topic_places_and_travel, Genre.PLACES_AND_TRAVEL),
    FOOD(R.string.topic_food, Genre.FOOD),
    ARTS(R.string.topic_arts, Genre.ARTS),
    MUSIC(R.string.topic_music, Genre.MUSIC),
    BOOKS(R.string.topic_books, Genre.BOOKS),
    SPORTS(R.string.topic_sports, Genre.SPORTS),
    TV_AND_FILM(R.string.topic_tv_and_film, Genre.TV_AND_FILM),
    MENTAL_HEALTH(R.string.topic_mental_health, Genre.MENTAL_HEALTH),
    SELF_IMPROVEMENT(R.string.topic_self_improvement, Genre.SELF_IMPROVEMENT),
    RELATIONSHIPS(R.string.topic_relationships, Genre.RELATIONSHIPS),
    RELIGION_AND_SPIRITUALITY(
        R.string.topic_religion_and_spirituality,
        Genre.RELIGION_AND_SPIRITUALITY
    ),
    KIDS_AND_FAMILY(R.string.topic_kids_and_family, Genre.KIDS_AND_FAMILY)
}

interface State {
    class Loading() : State
    data class Done(
        val result: List<PodcastPreviewModel>,
        val countryCode: String
    ) : State

    data class Error(val error: String) : State
}

class DiscoverViewModel(
    val defaultCountryCode: String
) : ViewModel() {

    val applePodcastClient = ApplePodcastClient()

    val previewBottomSheetState = PodcastPreviewBottomSheetState()
    val countryCodeSelectorState = CountryCodeSelectorState(
        defaultCountryCode = defaultCountryCode
    )

    val countryCode = mutableStateOf("")
    val states = mutableStateMapOf<Int, State>()

    fun clickPodcastPreview(
        preview: PodcastPreviewModel
    ) {
        if(preview.fetchUrl.startsWith("itunes-lookup:")) {
            viewModelScope.launch {
                try {
                    preview.fetchUrl = applePodcastClient.lookup.getRssFeedUrl(
                        id = preview.fetchUrl.replaceFirst("itunes-lookup:", "")
                    )

                    previewBottomSheetState.show(preview)
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            previewBottomSheetState.show(preview)
        }
    }

    fun updateCountryCode(
        countryCode: String,
        currentPage: Int
    ) {
        this@DiscoverViewModel.countryCode.value = countryCode

        resetStates()
        updatePage(currentPage)
    }

    fun resetStates() {
        Topics.entries.forEachIndexed { index, _ ->
            states[index] = State.Loading()
        }
    }

    fun updatePage(
        index: Int
    ) {
        val state = states[index]
        if(state is State.Done) return

        viewModelScope.launch {
            val topic = Topics.entries[index]

            states[index] = try {
                val elements = applePodcastClient.topPodcasts.load(
                    countryCode = countryCode.value,
                    genre = topic.genre
                )

                State.Done(
                    result = elements,
                    countryCode = countryCode.value
                )
            } catch(e: Exception) {
                e.printStackTrace()
                State.Error(e.toString())
            }
        }
    }
}