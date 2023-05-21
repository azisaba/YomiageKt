package net.azisaba.yomiagekt.commands

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.rest.builder.interaction.GlobalMultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.number
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.subCommand
import dev.kord.rest.builder.message.create.embed
import net.azisaba.yomiagekt.config.GuildsConfig
import net.azisaba.yomiagekt.util.Util.optDouble
import net.azisaba.yomiagekt.util.Util.optString
import net.azisaba.yomiagekt.util.Util.optSubcommand
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

object DictionaryCommand : CommandHandler {
    override suspend fun handle(interaction: ApplicationCommandInteraction) {
        interaction.optSubcommand("add")?.let { opt ->
            val before = opt.optString("before")!!
            val after = opt.optString("after")!!
            add(interaction, before, after)
        }
        interaction.optSubcommand("remove")?.let { opt ->
            val before = opt.optString("before")!!
            remove(interaction, before)
        }
        interaction.optSubcommand("remove-at")?.let { opt ->
            val index = opt.optDouble("index")!!.roundToInt()
            removeAt(interaction, index)
        }
        interaction.optSubcommand("list")?.let { opt ->
            val page = opt.optDouble("page")?.roundToInt() ?: 1
            list(interaction, page)
        }
    }

    private suspend fun add(interaction: ApplicationCommandInteraction, before: String, after: String) {
        GuildsConfig[interaction.channel.getGuildOrNull()!!.id].modifyDictionary {
            it.removeIf { pair -> pair.first == before } // remove is needed to update the order of the dictionary
            it += before to after
        }
        interaction.respondPublic { content = "辞書に「$before」→「$after」を登録しました" }
    }

    private suspend fun remove(interaction: ApplicationCommandInteraction, before: String) {
        GuildsConfig[interaction.channel.getGuildOrNull()!!.id].modifyDictionary {
            it.removeIf { pair -> pair.first == before }
        }
        interaction.respondPublic { content = "辞書から「$before」を削除しました" }
    }

    private suspend fun removeAt(interaction: ApplicationCommandInteraction, index: Int) {
        GuildsConfig[interaction.channel.getGuildOrNull()!!.id].modifyDictionary {
            it.removeAt(index)
        }
        interaction.respondPublic { content = "辞書から${index}個目の言葉を削除しました" }
    }

    private suspend fun list(interaction: ApplicationCommandInteraction, page: Int) {
        val dict = GuildsConfig[interaction.channel.getGuildOrNull()!!.id].dictionary
        if (dict.isEmpty()) {
            interaction.respondEphemeral { content = "辞書は空っぽです。" }
            return
        }
        val maxPage = ceil(dict.size / 30.0).toInt()
        val actualPage = min(maxPage, page)
        var index = 0
        val minIndex = (actualPage - 1) * 30
        val maxIndex = actualPage * 30
        var content = ""
        dict.forEach { (before, after) ->
            if (index !in minIndex..maxIndex) return@forEach
            content += "$index: `$before` → `$after`\n"
            index++
        }
        interaction.respondEphemeral {
            embed {
                description = content
            }
        }
    }

    override fun register(builder: GlobalMultiApplicationCommandBuilder) {
        builder.input("dict", "辞書") {
            dmPermission = false

            subCommand("add", "辞書に言葉を追加") {
                string("before", "置き換え前の文字列") {
                    required = true
                }
                string("after", "置き換え後の文字列") {
                    required = true
                }
            }
            subCommand("remove", "辞書から言葉を削除") {
                string("before", "置き換え前の文字列") {
                    required = true
                }
            }
            subCommand("remove-at", "辞書からindexを指定して言葉を削除") {
                number("index", "index") {
                    required = true
                    minValue = 0.0
                }
            }
            subCommand("list", "辞書一覧") {
                number("page", "ページ") {
                    minValue = 1.0
                }
            }
        }
    }
}
