package net.azisaba.yomiagekt.extension

import net.azisaba.yomiagekt.config.GuildConfig
import net.azisaba.yomiagekt.config.GuildsConfig
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

/**
 * Get config for each guild
 */
val SlashCommandInteractionEvent.config: GuildConfig
    get() = GuildsConfig[id]

/**
 * Get value from option and try to get member
 */
fun SlashCommandInteractionEvent.optionMember(name: String): Member? = getOption(name)?.asMember

/**
 * Get option as string
 */
fun SlashCommandInteractionEvent.optionString(name: String): String? = getOption(name)?.asString

/**
 * Get option as double
 */
fun SlashCommandInteractionEvent.optionDouble(name: String): Double? = getOption(name)?.asDouble

/**
 * Make request for ephemeral response
 * CAUTION: no .queue() required for this function
 */
fun SlashCommandInteractionEvent.respondEphemeral(
    content: String,
    optionBuilder: ReplyCallbackAction.() -> Unit = {},
) = deferReply(true).setContent(content).apply { optionBuilder() }.queue()

/**
 * Make request for public response
 * CAUTION: no .queue() required for this function
 */
fun SlashCommandInteractionEvent.respondPublic(
    content: String,
    optionBuilder: ReplyCallbackAction.() -> Unit = {},
) = deferReply(false).setContent(content).apply { optionBuilder() }.queue()
