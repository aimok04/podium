package app.podiumpodcasts.podium.ui.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.api.apple.ApplePodcastClient
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.manager.AddPodcastResult
import app.podiumpodcasts.podium.manager.PodcastManager
import app.podiumpodcasts.podium.utils.rss.buildPodiumRssParser
import app.podiumpodcasts.podium.utils.rss.toPodcast
import app.podiumpodcasts.podium.utils.rss.toPodcastEpisode
import kotlinx.coroutines.launch

interface AddPodcastState {
    open class Idle() : AddPodcastState
    open class Loading() : AddPodcastState
    open class Preview(
        val imageUrl: String,
        val podcast: PodcastModel,
        val episodes: List<PodcastEpisodeModel>
    ) : AddPodcastState

    open class Done(
        val podcast: PodcastModel
    ) : AddPodcastState

    open class Duplicate(
        val duplicate: PodcastModel
    ) : AddPodcastState

    open class Error(
        val reason: String
    ) : AddPodcastState
}

class AddPodcastViewModel(
    val db: AppDatabase
) : ViewModel() {

    val podcastManager = PodcastManager(db)

    val applePodcastClient = ApplePodcastClient()

    var state by mutableStateOf<AddPodcastState>(AddPodcastState.Idle())

    var origin by mutableStateOf("")
    var seedColor by mutableStateOf<Color?>(null)

    fun fetchRssPodcast() {
        viewModelScope.launch {
            state = AddPodcastState.Loading()

            try {
                var fileSize = 0L
                val rssParser = buildPodiumRssParser { size -> fileSize = size }

                val rssChannel = rssParser.getRssChannel(origin)

                val podcast = rssChannel.toPodcast(origin, fileSize, null)
                val episodes = rssChannel.items.map { it.toPodcastEpisode(podcast = podcast) }

                seedColor = null
                state = AddPodcastState.Preview(
                    imageUrl = rssChannel.image?.url ?: "",
                    podcast = podcast,
                    episodes = episodes
                )
            } catch(e: Exception) {
                e.printStackTrace()
                state = AddPodcastState.Error(
                    reason = e.toString()
                )
            }
        }
    }

    fun addPodcast() {
        viewModelScope.launch {
            if(state is AddPodcastState.Preview) {
                try {
                    val podcast = (state as AddPodcastState.Preview).podcast
                    val episodes = (state as AddPodcastState.Preview).episodes

                    val result = podcastManager.addPodcast(
                        podcast = podcast,
                        episodes = episodes,
                        seedColor = seedColor
                    )

                    state = when(result) {
                        is AddPodcastResult.Duplicate ->
                            AddPodcastState.Duplicate(result.duplicate)

                        is AddPodcastResult.Created ->
                            AddPodcastState.Done(result.podcast)

                        else ->
                            AddPodcastState.Error("UNHANDLED_RESPONSE")
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                    state = AddPodcastState.Error(
                        reason = e.toString()
                    )
                }
            }
        }
    }

}