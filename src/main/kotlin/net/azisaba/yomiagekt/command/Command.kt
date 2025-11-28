package net.azisaba.yomiagekt.command

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

/**
 * Base class of slash command
 */
abstract class Command {
    abstract val commandData: CommandData

    abstract fun onCommand(event: SlashCommandInteractionEvent)

    fun onComplete(event: CommandAutoCompleteInteractionEvent) {}
}
