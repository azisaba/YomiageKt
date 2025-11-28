package net.azisaba.yomiagekt.command

import net.azisaba.yomiagekt.audio.AudioPlayerSendHandler
import net.azisaba.yomiagekt.extension.respond
import net.azisaba.yomiagekt.extension.respondEphemeral
import net.azisaba.yomiagekt.extension.string
import net.azisaba.yomiagekt.extension.subCommand
import net.azisaba.yomiagekt.old.data.YomiageState
import net.azisaba.yomiagekt.old.data.YomiageStateStore
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.LoggerFactory

class YomiageCommand : Command() {
    override val commandData: CommandData
        get() =
            slashCommand("yomiage", "読み上げコマンド") {
                subCommand("join", "VCに参加します")
                subCommand("leave", "読み上げを終了します")
                subCommand("where", "どこで読み上げているかを表示します")
                subCommand("credit", "クレジット表記を表示します")
                subCommand("set-voice", "話し手を設定します") {
                    string("actor", "話し手")
                }
                subCommand("voice-list", "利用可能な話し手を表示します")
                subCommand("skip", "現在再生してる読み上げをスキップします")
            }

    override fun onCommand(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "join" -> join(event)
            "leave" -> leave(event)
            "where" -> where(event)
            "credit" -> credit(event)
            "set-voice" -> setVoice(event)
            "voice-list" -> voiceList(event)
            "skip" -> skip(event)
        }
    }

    /**
     * Join to the vc
     */
    fun join(event: SlashCommandInteractionEvent) {
        val guild = event.guild ?: return
        val member = event.member ?: return

        // get voice state and check bot already joined
        val state = YomiageStateStore[guild.id]
        if (state != null) {
            event.respondEphemeral("別の場所で読み上げているため、参加できません。")
            return
        }

        // get member's current voice channel
        val memberJoiningCh =
            member.voiceState?.channel ?: run {
                event.respondEphemeral("ボイスチャンネルに参加してください。すでに参加している場合は参加しなおしてください。")
                return
            }

        // for > 3sec process
        val deferredMsg = event.deferReply()
        val guildId = guild.id
        val textChannelId =
            event.channelId ?: run {
                event.respondEphemeral("テキストチャンネルの取得に失敗しました。再度実行してください。")
                return
            }
        val voiceChannelId = memberJoiningCh.id
        val nsfw = event.channel.asTextChannel().isNSFW

        try {
            guild.audioManager.apply {
                openAudioConnection(memberJoiningCh)
                val state =
                    YomiageState(guildId, textChannelId, voiceChannelId, nsfw) {
                        sendingHandler = AudioPlayerSendHandler(this)
                    }
                YomiageStateStore[guildId] = state
            }
        } catch (e: Exception) {
            deferredMsg.respond("エラーが発生しました。")
            logger.error("Failed to join the voice channel ($guildId - $voiceChannelId)", e)
        }

        val responseMsg =
            if (nsfw) {
                """
                接続しました。
                テキストチャンネル: <#$textChannelId>
                ボイスチャンネル: <#$voiceChannelId>

                :exclamation: すべてのメッセージが読み上げされます。
                :warning: R18利用が禁止されているキャラクターはメッセージの内容に関わらず読み上げされません。
                
                ※このBotはVOICEVOXを使用して音声を生成しています。利用規約:<https://voicevox.hiroshiba.jp/term/>
                """.trimIndent()
            } else {
                """
                接続しました。
                テキストチャンネル: <#$textChannelId>
                ボイスチャンネル: <#$voiceChannelId>

                :exclamation: 不適切と判定されたメッセージは読み上げされません。
                
                ※このBotはVOICEVOXを使用して音声を生成しています。利用規約:<https://voicevox.hiroshiba.jp/term/>
                """.trimIndent()
            }
        deferredMsg.respond(responseMsg)
    }

    fun leave(event: SlashCommandInteractionEvent) {}

    fun where(event: SlashCommandInteractionEvent) {}

    fun credit(event: SlashCommandInteractionEvent) {}

    fun setVoice(event: SlashCommandInteractionEvent) {}

    fun voiceList(event: SlashCommandInteractionEvent) {}

    fun skip(event: SlashCommandInteractionEvent) {}

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
