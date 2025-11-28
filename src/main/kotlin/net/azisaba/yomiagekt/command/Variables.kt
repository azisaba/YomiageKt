package net.azisaba.yomiagekt.command

import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

fun slashCommand(
    name: String,
    description: String,
    slashCommandBuilder: SlashCommandData.() -> Unit = {},
): SlashCommandData = Commands.slash(name, description).apply { slashCommandBuilder() }
