package net.azisaba.yomiagekt.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class AudioPlayerSendHandler(
    private val audioPlayer: AudioPlayer,
    private var lastFrame: AudioFrame? = null,
) : AudioSendHandler {
    override fun canProvide(): Boolean {
        lastFrame = audioPlayer.provide()
        return lastFrame != null
    }

    override fun provide20MsAudio(): ByteBuffer? = ByteBuffer.wrap(lastFrame?.data)

    override fun isOpus(): Boolean = true
}
