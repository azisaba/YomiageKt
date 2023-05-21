package net.azisaba.yomiagekt.commands

import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.VoiceChannelBehavior
import dev.kord.core.behavior.channel.connect
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.rest.builder.interaction.GlobalMultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.subCommand
import dev.kord.voice.AudioProvider
import net.azisaba.yomiagekt.config.UsersConfig
import net.azisaba.yomiagekt.data.Characters
import net.azisaba.yomiagekt.data.YomiageState
import net.azisaba.yomiagekt.data.YomiageStateStore
import net.azisaba.yomiagekt.util.Util
import net.azisaba.yomiagekt.util.Util.optString
import net.azisaba.yomiagekt.util.Util.optSubcommand
import java.util.concurrent.atomic.AtomicReference

object YomiageCommand : CommandHandler {
    override suspend fun handle(interaction: ApplicationCommandInteraction) {
        val guild = interaction.channel.getGuildOrNull()!!
        val member = guild.getMember(interaction.user.id)
        if (interaction.optSubcommand("join") != null) {
            join(interaction, guild, member)
        }
        if (interaction.optSubcommand("leave") != null) {
            val state = YomiageStateStore[guild.id]
            if (state == null) {
                interaction.respondEphemeral { content = "読み上げ中のセッションがありません。" }
                return
            }
            if (state.textChannelId != interaction.channelId) {
                interaction.respondEphemeral { content = "このチャンネルではleaveコマンドを使用できません。" }
                return
            }
            val removedState = YomiageStateStore.remove(guild.id)
            if (removedState != null) {
                removedState.shutdown()
                interaction.respondPublic { content = "<#${removedState.voiceChannelId}>の読み上げを終了しました" }
            } else {
                interaction.respondEphemeral { content = "読み上げ中のセッションがありません。" }
            }
        }
        if (interaction.optSubcommand("where") != null) {
            val state = YomiageStateStore[guild.id]
            if (state != null) {
                interaction.respondEphemeral { content = "下記のチャンネルで読み上げ中です。\nテキストチャンネル: <#${state.textChannelId}>\nボイスチャンネル: <#${state.voiceChannelId}>" }
            } else {
                interaction.respondEphemeral { content = "読み上げ中のセッションがありません。" }
            }
        }
        if (interaction.optSubcommand("credit") != null) {
            val state = YomiageStateStore[guild.id]
            if (state != null) {
                interaction.respondEphemeral {
                    content = "読み上げに使用したキャラクターのクレジット表記:\n" +
                            state.usedCharacters.joinToString("\n") { it.credit }
                }
            } else {
                interaction.respondEphemeral { content = "読み上げ中のセッションがありません。" }
            }
        }
        interaction.optSubcommand("set-voice")?.let { opt ->
            val actor = opt.optString("actor")!!
            val character = Characters.values().find { it.characterName == actor }
            if (character == null) {
                val list = Characters.values().sortedWith(Comparator.comparing { t -> Util.levenshtein(actor, t.characterName.replace("（.*?）".toRegex(), "")) })
                interaction.respondEphemeral {
                    content = "該当するキャラクターが見つかりません。以下のいずれかを選択してください。(`/yomiage voice-list`ですべての話者を表示します)\n" +
                            list.subList(0, 7).joinToString("\n") { "`${it.characterName}`" }
                }
                return
            }
            val userConfig = UsersConfig[interaction.user.id]
            userConfig.character = character
            UsersConfig.save()
            interaction.respondEphemeral {
                content = """
                    話者を${character.characterName}に設定しました。
                    キャラクターの説明: ${character.description}
                    R18利用(年齢制限チャンネル以外は右の表記に関わらず:x:): ${character.nsfwType.description}
                    利用規約: <${character.terms}>
                """.trimIndent()
            }
        }
        if (interaction.optSubcommand("voice-list") != null) {
            interaction.respondEphemeral {
                content = "利用可能なキャラクター:\n${Characters.values().joinToString("\n") { "`${it.characterName}`" }}"
            }
        }
    }

    @OptIn(KordVoice::class)
    private suspend fun join(interaction: ApplicationCommandInteraction, guild: Guild, member: Member) {
        if (YomiageStateStore[guild.id] != null) {
            interaction.respondEphemeral { content = "別の場所で読み上げているため、参加できません。" }
            return
        }
        val voiceState = member.getVoiceStateOrNull()
        val channel = (voiceState?.getChannelOrNull() as? VoiceChannelBehavior)?.asChannel()
        if (channel == null) {
            interaction.respondEphemeral { content = "ボイスチャンネルに参加してください。すでに参加している場合は参加しなおしてください。" }
            return
        }
        val defer = interaction.deferPublicResponse()
        try {
            val ref = AtomicReference<AudioProvider>()
            val connection = channel.connect {
                selfDeaf = true

                audioProvider {
                    ref.get().provide()
                }
            }
            val state = YomiageState(guild.id, interaction.channelId, channel.id, channel.data.nsfw.discordBoolean, connection, ref)
            YomiageStateStore.put(guild.id, state)
            defer.respond { content = "接続しました。\nテキストチャンネル: <#${interaction.channelId}>\nボイスチャンネル: <#${channel.id}>\n\n※このBotはVOICEVOXを使用して音声を生成しています。利用規約:<https://voicevox.hiroshiba.jp/term/>" }
        } catch (e: Exception) {
            defer.respond { content = "エラーが発生しました。" }
            println("Could not join the voice channel ${guild.id} / ${channel.id}")
            e.printStackTrace()
        }
    }

    override fun register(builder: GlobalMultiApplicationCommandBuilder) {
        builder.input("yomiage", "読み上げコマンド") {
            dmPermission = false

            subCommand("join", "VCに参加します")
            subCommand("leave", "読み上げを終了します")
            subCommand("where", "どこで読み上げているかを表示します")
            subCommand("credit", "クレジット表記を表示します")
            subCommand("set-voice", "話し手を設定します") {
                string("actor", "話し手") {
                    required = true
                }
            }
            subCommand("voice-list", "利用可能な話し手を表示します")
        }
    }
}
