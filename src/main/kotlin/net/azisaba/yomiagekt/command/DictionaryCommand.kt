package net.azisaba.yomiagekt.command

import net.azisaba.yomiagekt.extension.number
import net.azisaba.yomiagekt.extension.string
import net.azisaba.yomiagekt.extension.subCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

class DictionaryCommand : Command() {
    override val commandData: CommandData
        get() =
            slashCommand("dict", "辞書") {
                subCommand("add", "辞書に言葉を追加") {
                    string("before", "置き換え前の文字列", true)
                    string("after", "置き換え後の文字列", true)
                }
                subCommand("remove", "辞書から言葉を削除") {
                    string("before", "置き換え前の文字列", true)
                }
                subCommand("remove-at", "辞書からindexを指定して言葉を削除") {
                    number("index", "index", true, minValue = 0.0)
                }
                subCommand("list", "辞書一覧") {
                    number("page", "ページ", minValue = 1.0)
                }
            }

    override fun onCommand(event: SlashCommandInteractionEvent) {
    }
}
