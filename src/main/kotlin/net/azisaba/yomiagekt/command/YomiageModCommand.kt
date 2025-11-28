package net.azisaba.yomiagekt.command

import net.azisaba.yomiagekt.extension.config
import net.azisaba.yomiagekt.extension.optionMember
import net.azisaba.yomiagekt.extension.respondEphemeral
import net.azisaba.yomiagekt.extension.respondPublic
import net.azisaba.yomiagekt.extension.subCommand
import net.azisaba.yomiagekt.extension.user
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.CommandData

class YomiageModCommand : Command() {
    override val commandData: CommandData
        get() =
            slashCommand("yomiage-mod", "読み上げ管理コマンド") {
                subCommand("mute", "指定したユーザーからのメッセージを読み上げないようにします") {
                    user("user", "ユーザー", true)
                }
                subCommand("unmute", "指定したユーザーのミュートを解除します") {
                    user("user", "ユーザー", true)
                }
                subCommand("clear-dict", "辞書をすべて削除します")
            }.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_MUTE_OTHERS, Permission.MESSAGE_MANAGE))

    override fun onCommand(event: SlashCommandInteractionEvent) {
        val guild = event.guild ?: return
        when (event.subcommandName) {
            "mute" -> onMute(guild, event)
            "unmute" -> onUnmute(guild, event)
            "clear-dict" -> onClearDict(guild, event)
        }
    }

    fun onMute(
        guild: Guild,
        event: SlashCommandInteractionEvent,
    ) {
        val user =
            event.optionMember("user") ?: run {
                event.reply("ユーザーが存在しません。").queue()
                return
            }

        val responseMsg =
            if (guild.config.modifyMutedUsers { it.add(user.id) }) {
                "``${user.asMention}``をミュートしました"
            } else {
                "``${user.asMention}``はすでにミュートされています"
            }

        event.respondEphemeral(responseMsg) {
            setAllowedMentions(mutableListOf())
        }
    }

    fun onUnmute(
        guild: Guild,
        event: SlashCommandInteractionEvent,
    ) {
        val user =
            event.optionMember("user") ?: run {
                event.reply("ユーザーが存在しません。").queue()
                return
            }

        val responseMsg =
            if (guild.config.modifyMutedUsers { it.remove(user.id) }) {
                "``${user.asMention}``のミュートを解除しました"
            } else {
                "``${user.asMention}``はミュートされていません"
            }

        event.respondEphemeral(responseMsg) {
            setAllowedMentions(mutableListOf())
        }
    }

    fun onClearDict(
        guild: Guild,
        event: SlashCommandInteractionEvent,
    ) {
        guild.config.dictionary.clear()
        event.respondPublic("辞書をすべて削除しました")
    }
}
