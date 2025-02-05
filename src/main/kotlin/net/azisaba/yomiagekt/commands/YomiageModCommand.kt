package net.azisaba.yomiagekt.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.rest.builder.interaction.GlobalMultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.subCommand
import dev.kord.rest.builder.interaction.user
import dev.kord.rest.builder.message.allowedMentions
import net.azisaba.yomiagekt.config.GuildsConfig
import net.azisaba.yomiagekt.util.Util.optSnowflake
import net.azisaba.yomiagekt.util.Util.optSubcommand

object YomiageModCommand : CommandHandler {
    override suspend fun handle(interaction: ApplicationCommandInteraction) {
        val guild = interaction.channel.getGuildOrNull()!!
        val config = GuildsConfig[guild.id]
        interaction.optSubcommand("mute")?.let { opt ->
            val userId = opt.optSnowflake("user")!!
            val user = guild.getMemberOrNull(userId) ?: run {
                interaction.respondEphemeral { content = "ユーザーが存在しません。" }
                return
            }
            if (config.modifyMutedUsers { it.add(userId) }) {
                interaction.respondEphemeral {
                    content = "``${user.tag}``をミュートしました"
                    allowedMentions {}
                }
            } else {
                interaction.respondEphemeral {
                    content = "``${user.tag}``はすでにミュートされています"
                    allowedMentions {}
                }
            }
        }
        interaction.optSubcommand("unmute")?.let { opt ->
            val userId = opt.optSnowflake("user")!!
            val user = guild.getMemberOrNull(userId) ?: run {
                interaction.respondEphemeral { content = "ユーザーが存在しません。" }
                return
            }
            if (config.modifyMutedUsers { it.remove(userId) }) {
                interaction.respondEphemeral {
                    content = "``${user.tag}``のミュートを解除しました"
                    allowedMentions {}
                }
            } else {
                interaction.respondEphemeral {
                    content = "``${user.tag}``はミュートされていません"
                    allowedMentions {}
                }
            }
        }
        if (interaction.optSubcommand("clear-dict") != null) {
            config.dictionary.clear()
            interaction.respondPublic { content = "辞書をすべて削除しました" }
        }
    }

    override fun register(builder: GlobalMultiApplicationCommandBuilder) {
        builder.input("yomiage-mod", "読み上げ管理コマンド") {
            dmPermission = false
            defaultMemberPermissions = Permissions(Permission.MuteMembers, Permission.ManageMessages)

            subCommand("mute", "指定したユーザーからのメッセージを読み上げないようにします") {
                user("user", "ユーザー") {
                    required = true
                }
            }
            subCommand("unmute", "指定したユーザーのミュートを解除します") {
                user("user", "ユーザー") {
                    required = true
                }
            }
            subCommand("clear-dict", "辞書をすべて削除します")
        }
    }
}
