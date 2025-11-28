package net.azisaba.yomiagekt

import dev.kord.core.Kord
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.flow.toList
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

    client.on<VoiceStateUpdateEvent> {
        if (state.userId == kord.selfId && state.channelId == null) {
            // handle server side "disconnect"
            YomiageStateStore.remove(state.guildId)?.shutdown()
        }
        if (state.channelId == null) return@on
        if (YomiageStateStore[state.guildId]?.voiceChannelId == state.channelId) return@on
        if (kord
                .getChannelOf<VoiceChannel>(state.channelId!!)
                ?.voiceStates
                ?.toList()
                ?.size == 1
        ) {
            // handle server side "leave"
            YomiageStateStore.remove(state.guildId)?.shutdown()
        }
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
