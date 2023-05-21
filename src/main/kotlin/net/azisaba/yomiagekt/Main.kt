package net.azisaba.yomiagekt

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import net.azisaba.yomiagekt.commands.DictionaryCommand
import net.azisaba.yomiagekt.commands.YomiageCommand
import net.azisaba.yomiagekt.commands.YomiageModCommand
import net.azisaba.yomiagekt.config.BotConfig
import net.azisaba.yomiagekt.config.GuildsConfig
import net.azisaba.yomiagekt.config.UsersConfig
import net.azisaba.yomiagekt.data.YomiageStateStore

@OptIn(PrivilegedIntent::class)
suspend fun main() {
    // headless mode
    System.setProperty("java.awt.headless", "true")

    // load config
    BotConfig
    GuildsConfig.load()
    UsersConfig.load()

    val client = Kord(BotConfig.config.botToken)

    val commands = mapOf(
        "yomiage" to YomiageCommand,
        "yomiage-mod" to YomiageModCommand,
        "dict" to DictionaryCommand,
    )

    client.createGlobalApplicationCommands {
        commands.values.distinct().forEach { it.register(this) }
    }

    client.on<ApplicationCommandInteractionCreateEvent> {
        if (interaction.user.isBot) return@on
        commands.forEach { (name, command) ->
            if (interaction.invokedCommandName == name) {
                command.handle(interaction)
            }
        }
    }

    client.on<ReadyEvent> {
        println("Logged in as ${kord.getSelf().tag}!")
    }

    client.on<MessageCreateEvent> {
        val guild = getGuildOrNull() ?: return@on
        val state = YomiageStateStore[guild.id] ?: return@on
        if (state.textChannelId != message.channelId) return@on
        val config = GuildsConfig[guild.id]
        if (message.author?.isBot != false) return@on
        if (config.mutedUsers.contains(message.author?.id)) return@on
        state.queueUserInput(message)
    }

    client.on<VoiceStateUpdateEvent> {
        if (state.userId == kord.selfId && state.channelId == null) {
            // handle server side "disconnect"
            YomiageStateStore.remove(state.guildId)?.shutdown()
        }
    }

    client.login {
        this.intents = Intents(
            Intent.GuildVoiceStates,
            Intent.GuildMessages,
            Intent.MessageContent,
        )
    }
}
