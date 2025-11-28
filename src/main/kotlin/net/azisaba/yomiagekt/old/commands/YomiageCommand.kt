package net.azisaba.yomiagekt.old.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.rest.builder.interaction.GlobalMultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.subCommand
import net.azisaba.yomiagekt.config.UsersConfig
import net.azisaba.yomiagekt.old.data.Characters
import net.azisaba.yomiagekt.old.data.NsfwType
import net.azisaba.yomiagekt.old.data.YomiageStateStore
import net.azisaba.yomiagekt.old.util.Util
import net.azisaba.yomiagekt.old.util.Util.optString
import net.azisaba.yomiagekt.old.util.Util.optSubcommand

object YomiageCommand : CommandHandler {
    override suspend fun handle(interaction: ApplicationCommandInteraction) {
        val guild = interaction.channel.getGuildOrNull()!!
        val member = guild.getMember(interaction.user.id)
        val state = YomiageStateStore[guild.id]
        if (interaction.optSubcommand("credit") != null) {
            if (state != null && state.textChannelId == interaction.channelId) {
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
                val list =
                    Characters.values().sortedWith(
                        Comparator.comparing { t ->
                            Util.levenshtein(actor, t.characterName.replace("（.*?）".toRegex(), ""))
                        },
                    )
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
                content =
                    if (state?.textChannelId == interaction.channelId && state.voiceChannelNsfw) {
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
            }
        }
        if (interaction.optSubcommand("voice-list") != null) {
            interaction.respondEphemeral {
                content = "利用可能なキャラクター:\n" +
                    if (state?.textChannelId == interaction.channelId && state.voiceChannelNsfw) {
                        Characters.values().filter { it.nsfwType != NsfwType.Disallowed }.joinToString("\n") { "`${it.characterName}`" }
                    } else {
                        Characters.values().joinToString("\n") { "`${it.characterName}`" }
                    }
            }
        }
        if (interaction.optSubcommand("skip") != null) {
            if (state != null && state.textChannelId == interaction.channelId) {
                state.stopTrack()
                interaction.respondPublic { content = "現在再生中の読み上げをスキップしました。" }
            } else {
                interaction.respondEphemeral { content = "読み上げ中のセッションがありません。" }
            }
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
            subCommand("skip", "現在再生してる読み上げをスキップします")
        }
    }
}
