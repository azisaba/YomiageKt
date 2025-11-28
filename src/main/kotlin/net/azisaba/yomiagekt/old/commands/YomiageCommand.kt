package net.azisaba.yomiagekt.old.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.rest.builder.interaction.GlobalMultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.subCommand
import net.azisaba.yomiagekt.old.data.YomiageStateStore
import net.azisaba.yomiagekt.old.util.Util.optSubcommand

object YomiageCommand : CommandHandler {
    override suspend fun handle(interaction: ApplicationCommandInteraction) {
        val guild = interaction.channel.getGuildOrNull()!!
        val member = guild.getMember(interaction.user.id)
        val state = YomiageStateStore[guild.id]
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
