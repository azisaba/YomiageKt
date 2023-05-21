package net.azisaba.yomiagekt.data

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.entity.Snowflake
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object YomiageStateStore {
    val audioPlayerManager = DefaultAudioPlayerManager()
    private val states = mutableMapOf<Snowflake, YomiageState>()

    init {
        AudioSourceManagers.registerLocalSource(audioPlayerManager)
    }

    suspend fun DefaultAudioPlayerManager.playTrack(query: String, player: AudioPlayer): AudioTrack {
        val track = suspendCoroutine<AudioTrack> {
            this.loadItem(query, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    it.resume(track)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    it.resume(playlist.tracks.first())
                }

                override fun noMatches() {
                    TODO()
                }

                override fun loadFailed(exception: FriendlyException?) {
                    if (exception != null) {
                        it.resumeWithException(exception)
                    }
                }
            })
        }

        player.playTrack(track)

        return track
    }

    fun put(guildId: Snowflake, state: YomiageState) {
        if (states[guildId] != null) {
            error("state is already registered for $guildId")
        }
        states[guildId] = state
    }

    operator fun get(guildId: Snowflake) = states[guildId]

    fun remove(guildId: Snowflake) = states.remove(guildId)
}
