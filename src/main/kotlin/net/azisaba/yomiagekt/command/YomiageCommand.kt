package net.azisaba.yomiagekt.command

import net.azisaba.yomiagekt.audio.AudioPlayerSendHandler
import net.azisaba.yomiagekt.extension.respond
import net.azisaba.yomiagekt.extension.respondEphemeral
import net.azisaba.yomiagekt.extension.respondPublic
import net.azisaba.yomiagekt.extension.string
import net.azisaba.yomiagekt.extension.subCommand
import net.azisaba.yomiagekt.old.data.YomiageState
import net.azisaba.yomiagekt.old.data.YomiageStateStore
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
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
        val guild = event.guild ?: return
        val member = event.member ?: return
        when (event.subcommandName) {
            "join" -> join(guild, member, event)
            "leave" -> leave(guild, member, event)
            "where" -> where(guild, member, event)
            "credit" -> credit(guild, member, event)
            "set-voice" -> setVoice(guild, member, event)
            "voice-list" -> voiceList(guild, member, event)
            "skip" -> skip(guild, member, event)
        }
    }

    /**
     * Join to the vc
     */
    fun join(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {
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

    /**
     * Leave from current vc
     */
    fun leave(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {
        // get state and is running
        val state = YomiageStateStore[guild.id]
        if (state == null) {
            event.respondEphemeral("読み上げ中のセッションがありません。")
            return
        }

        // check text channel id
        if (event.channelId != state.textChannelId) {
            event.respondEphemeral("このチャンネルではleaveコマンドを使用できません。")
            return
        }

        // remove state and shutdown
        val removedState = YomiageStateStore.remove(guild.id)
        if (removedState != null) {
            removedState.shutdown()
            event.respondPublic("<#${removedState.voiceChannelId}>の読み上げを終了しました")
        } else {
            // for safety handling
            event.respondEphemeral("読み上げ中のセッションがありません。")
        }
    }

    fun where(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {
        val state = YomiageStateStore[guild.id]
        val responseMsg =
            if (state != null) {
                """
                下記のチャンネルで読み上げ中です。
                テキストチャンネル: <#${state.textChannelId}>
                ボイスチャンネル: <#${state.voiceChannelId}>
                """.trimIndent()
            } else {
                "読み上げ中のセッションがありません。"
            }
        event.respondEphemeral(responseMsg)
    }

    fun credit(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {
        val state = YomiageStateStore[guild.id]
        if (state != null && state.textChannelId == event.channelId) {
            event.respondPublic(
                "読み上げに使用したキャラクターのクレジット表記:\n" +
                    state.usedCharacters.joinToString("\n") { it.credit },
            )
        } else {
            event.respondEphemeral("読み上げ中のセッションがありません。")
        }
    }

    fun setVoice(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {
    }

    fun voiceList(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {}

    fun skip(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {}

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
