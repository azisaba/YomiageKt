package net.azisaba.yomiagekt.command

import net.azisaba.yomiagekt.audio.AudioPlayerSendHandler
import net.azisaba.yomiagekt.config.UsersConfig
import net.azisaba.yomiagekt.data.Characters
import net.azisaba.yomiagekt.data.NsfwType
import net.azisaba.yomiagekt.data.YomiageState
import net.azisaba.yomiagekt.data.YomiageStateStore
import net.azisaba.yomiagekt.extension.config
import net.azisaba.yomiagekt.extension.optionString
import net.azisaba.yomiagekt.extension.respond
import net.azisaba.yomiagekt.extension.respondEphemeral
import net.azisaba.yomiagekt.extension.respondPublic
import net.azisaba.yomiagekt.extension.string
import net.azisaba.yomiagekt.extension.subCommand
import net.azisaba.yomiagekt.util.Util
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
                isSelfDeafened = true

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
        val state = YomiageStateStore[guild.id]
        val actor = event.optionString("actor") ?: return
        val character = Characters.entries.find { it.characterName == actor }

        // If not found in Characters
        if (character == null) {
            val nearCharacters =
                Characters.entries.sortedWith(
                    Comparator.comparing { t ->
                        Util.levenshtein(actor, t.characterName.replace("（.*?）".toRegex(), ""))
                    },
                )
            event.respondEphemeral(
                "該当するキャラクターが見つかりません。以下のいずれかを選択してください。(`/yomiage voice-list`ですべての話者を表示します)\n" +
                    nearCharacters.subList(0, 7).joinToString("\n") { "`${it.characterName}`" },
            )
            return
        }

        // set user config
        member.config.character = character
        UsersConfig.save()

        // feedback to user
        val responseMsg =
            if (state?.textChannelId == event.channelId && state?.voiceChannelNsfw == true) {
                """
                話者を${character.characterName}に設定しました。
                キャラクターの説明: ${character.description}
                R18利用: ${character.nsfwType.description} ${if (character.nsfwType == NsfwType.Disallowed) "(このチャンネルでは読み上げされません)" else ""}
                利用規約: <${character.terms}>
                """.trimIndent()
            } else {
                """
                話者を${character.characterName}に設定しました。
                キャラクターの説明: ${character.description}
                R18利用(年齢制限チャンネル以外は右の表記に関わらず:x:): ${character.nsfwType.description}
                利用規約: <${character.terms}>
                """.trimIndent()
            }
        event.respondEphemeral(responseMsg)
    }

    fun voiceList(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {
        val state = YomiageStateStore[guild.id]
        val responseMsg =
            "利用可能なキャラクター:\n" +
                if (state?.textChannelId == event.channelId && state?.voiceChannelNsfw == true) {
                    Characters.entries.filter { it.nsfwType != NsfwType.Disallowed }.joinToString("\n") { "`${it.characterName}`" }
                } else {
                    Characters.entries.joinToString("\n") { "`${it.characterName}`" }
                }
        event.respondEphemeral(responseMsg)
    }

    fun skip(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {
        val state = YomiageStateStore[guild.id]
        if (state != null && state.textChannelId == event.channelId) {
            state.stopTrack()
            event.respondPublic("現在再生中の読み上げをスキップしました。")
        } else {
            event.respondEphemeral("読み上げ中のセッションがありません。")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
