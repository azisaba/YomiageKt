package net.azisaba.yomiagekt

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import net.azisaba.yomiagekt.config.BotConfig
import net.azisaba.yomiagekt.config.GuildsConfig
import net.azisaba.yomiagekt.config.UsersConfig

@OptIn(PrivilegedIntent::class)
suspend fun main() {
    // headless mode
    System.setProperty("java.awt.headless", "true")

    // load config
    BotConfig
    GuildsConfig.load()
    UsersConfig.load()

    val client = Kord(BotConfig.config.botToken)

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

    client.login {
        this.intents =
            Intents(
                Intent.GuildVoiceStates,
                Intent.GuildMessages,
                Intent.MessageContent,
            )
    }
}
