package net.azisaba.yomiagekt.command

import net.azisaba.yomiagekt.extension.config
import net.azisaba.yomiagekt.extension.number
import net.azisaba.yomiagekt.extension.optionDouble
import net.azisaba.yomiagekt.extension.optionString
import net.azisaba.yomiagekt.extension.respondEphemeral
import net.azisaba.yomiagekt.extension.respondPublic
import net.azisaba.yomiagekt.extension.string
import net.azisaba.yomiagekt.extension.subCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import kotlin.math.ceil
import kotlin.math.min

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
        val guild = event.guild ?: return
        val member = event.member ?: return
        when (event.subcommandName) {
            "add" -> add(guild, member, event)
            "remove" -> remove(guild, member, event)
            "remove-at" -> removeAt(guild, member, event)
            "list" -> list(guild, member, event)
        }
    }

    fun add(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {
        val before = event.optionString("before") ?: return
        val after = event.optionString("after") ?: return

        // remove old and set new dictionary
        guild.config.modifyDictionary {
            it.removeIf { pair -> pair.first == before } // remove is needed to update the order of the dictionary
            it += before to after
        }
        event.respondPublic("辞書に「$before」→「$after」を登録しました")
    }

    fun remove(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {
        val before = event.optionString("before") ?: return
        guild.config.modifyDictionary {
            it.removeIf { pair -> pair.first == before }
        }
        event.respondPublic("辞書から「$before」を削除しました")
    }

    fun removeAt(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {
        val index: Int = event.optionDouble("index")?.toInt() ?: return
        guild.config.modifyDictionary {
            it.removeAt(index)
        }
        event.respondPublic("辞書から${index}個目の言葉を削除しました")
    }

    fun list(
        guild: Guild,
        member: Member,
        event: SlashCommandInteractionEvent,
    ) {
        val dict = guild.config.dictionary
        if (dict.isEmpty()) {
            event.respondEphemeral("このギルドの辞書は空っぽです。何か追加してみませんか?")
            return
        }

        val maxPage = ceil(dict.size.div(LINE_PER_PAGE.toDouble())).toInt()

        val page = event.optionDouble("page")?.toInt() ?: 1
        require(page in 1..maxPage) {
            event.respondEphemeral("現在、辞書は${maxPage}ページまでしかありません。超えない範囲のページ番号を入力してください。")
            return
        }

        // calc index
        val actualPage = min(maxPage, page) - 1 // For adjust 0-indexed list
        val minIndex = actualPage * LINE_PER_PAGE
        val maxIndex = (actualPage + 1) * LINE_PER_PAGE

        // get sub list and join to string
        val extractedDict = dict.subList(minIndex, min(maxIndex, dict.size))
        val content =
            extractedDict
                .mapIndexed { index, pair -> "${minIndex + index + 1}: `${pair.first}` → `${pair.second}`" }
                .joinToString("\n")

        // send response embed
        event
            .deferReply()
            .setEmbeds(
                EmbedBuilder().setDescription(content).build(),
            ).queue()
    }

    companion object {
        const val LINE_PER_PAGE: Int = 30
    }
}
