package net.azisaba.yomiagekt.extension

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

fun SlashCommandData.subCommand(
    name: String,
    description: String,
    subCommandBuilder: SubcommandData.() -> Unit = {},
) = SubcommandData(name, description).apply { subCommandBuilder() }.apply { this@subCommand.addSubcommands(this) }
