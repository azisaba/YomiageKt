package net.azisaba.yomiagekt.data

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.voice.AudioFrame
import dev.kord.voice.AudioProvider
import dev.kord.voice.VoiceConnection
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import net.azisaba.yomiagekt.config.BotConfig
import net.azisaba.yomiagekt.config.GuildsConfig
import net.azisaba.yomiagekt.config.UsersConfig
import net.azisaba.yomiagekt.data.YomiageStateStore.playTrack
import net.azisaba.yomiagekt.util.OpenAIModerationAPI
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference

@OptIn(KordVoice::class)
data class YomiageState(
    val guildId: Snowflake,
    val textChannelId: Snowflake,
    val voiceChannelId: Snowflake,
    val voiceChannelNsfw: Boolean,
    val connection: VoiceConnection,
    var audioProvider: AtomicReference<AudioProvider>,
) {
    companion object {
        private val client = HttpClient(CIO) {
            engine {
                this.requestTimeout = 1000 * 60
            }
        }
    }

    val usedCharacters = mutableSetOf<Characters>()
    private val audioPlayer: AudioPlayer = YomiageStateStore.audioPlayerManager.createPlayer()
    private val queue = ArrayDeque<QueueData>()
    private var previousFile: File? = null

    init {
        audioPlayer.addListener { event ->
            if (event is TrackEndEvent) {
                runBlocking { playNext() }
            }
        }
        audioProvider.set(AudioProvider {
            AudioFrame.fromData(audioPlayer.provide()?.data)
        })
    }

    private val userMentionPattern = "<@!?(\\d+)>".toRegex()
    private val channelMentionPattern = "<#(\\d+)>".toRegex()
    private val roleMentionPattern = "<@&(\\d+)>".toRegex()
    private val urlPattern = "[a-zA-Z0-9]+://[^\\s\\n\\r\\t<>]+".toRegex()

    suspend fun queueUserInput(message: Message) {
        if (message.content.isBlank()) return
        var currentMessage = message.content

        currentMessage = userMentionPattern.replace(currentMessage) {
            val userId = it.groups[1]!!.value
            runBlocking {
                val member = message.getGuild().getMemberOrNull(Snowflake(userId))
                '@' + (member?.nickname ?: message.getGuild().kord.getUser(Snowflake(userId))?.username ?: "謎のユーザー")
            }
        }

        currentMessage = channelMentionPattern.replace(currentMessage) {
            val channelId = it.groups[1]!!.value
            runBlocking {
                "しゃーぷ" + (message.getGuild().getChannelOrNull(Snowflake(channelId))?.name ?: "謎のチャンネル")
            }
        }

        currentMessage = roleMentionPattern.replace(currentMessage) {
            val roleId = it.groups[1]!!.value
            runBlocking {
                '@' + (message.getGuild().getRoleOrNull(Snowflake(roleId))?.name ?: "謎のロール")
            }
        }

        currentMessage = currentMessage.replace("\\|\\|.+?\\|\\|".toRegex(), "")
        currentMessage = currentMessage.replace(urlPattern, "")

        GuildsConfig[message.getGuild().id].dictionary.forEach { (key, value) ->
            currentMessage = currentMessage.replace(key, value)
        }

        if (currentMessage.isBlank()) return

        val userConfig = UsersConfig[message.author!!.id]

        if (userConfig.character.nsfwType == NsfwType.Disallowed && voiceChannelNsfw) {
            // nsfw usage not allowed
            return
        }

        if (!voiceChannelNsfw && !OpenAIModerationAPI.check(currentMessage)) {
            // flagged
            return
        }

        queue(QueueData(currentMessage, userConfig.character))
    }

    private suspend fun queue(queueData: QueueData) {
        if (queueData.message.isBlank()) return
        synchronized(queue) {
            queue.add(queueData)
        }
        if (audioPlayer.playingTrack == null) {
            playNext()
        }
    }

    private suspend fun playNext() {
        previousFile?.delete()
        val queueData = synchronized(queue) {
            if (queue.isEmpty()) return
            queue.removeFirst()
        }

        usedCharacters.add(queueData.character)

        try {
            val encodedMessage = URLEncoder.encode(queueData.message, StandardCharsets.UTF_8)
            val queryUrl = "${BotConfig.config.voicevoxEndpoint}/audio_query?text=$encodedMessage&speaker=${queueData.character.speakerIndex}"
            val queryJson = client.post(queryUrl).bodyAsText()
            if (queryJson.length <= 100) error("response is too short: $queryJson")
            val bytes = client.post("${BotConfig.config.voicevoxEndpoint}/synthesis?speaker=${queueData.character.speakerIndex}") {
                setBody(queryJson)
                header("Content-Type", "application/json")
            }.bodyAsChannel().toByteArray()
            val file = File.createTempFile("yomiagekt", ".wav")
            file.writeBytes(bytes)
            YomiageStateStore.audioPlayerManager.playTrack(file.absolutePath, audioPlayer)
            previousFile = file
        } catch (e: Exception) {
            println("Error consuming queue")
            e.printStackTrace()
            playNext()
        }
    }

    suspend fun shutdown() {
        audioPlayer.destroy()
        connection.shutdown()
    }

    data class QueueData(val message: String, val character: Characters)
}
