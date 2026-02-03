package net.azisaba.yomiagekt.bot

import club.minnced.discord.jdave.interop.JDaveSessionFactory
import net.azisaba.yomiagekt.command.CommandManager
import net.azisaba.yomiagekt.config.BotConfig
import net.azisaba.yomiagekt.config.GuildsConfig
import net.azisaba.yomiagekt.config.UsersConfig
import net.azisaba.yomiagekt.data.YomiageStateStore
import net.azisaba.yomiagekt.extension.config
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.audio.AudioModuleConfig
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class YomiageBot : ListenerAdapter() {
    private lateinit var bot: JDA

    fun main() {
        // Load config
        BotConfig.init()
        GuildsConfig.load()
        UsersConfig.load()
        logger.info("Config loaded!")

        // Create bot instance
        bot =
            JDABuilder
                .createDefault(BotConfig.config.botToken)
                .enableIntents(
                    listOf(
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                    ),
                ).addEventListeners(this)
                .setAudioModuleConfig(AudioModuleConfig().withDaveSessionFactory(JDaveSessionFactory()))
                .build()

        // init and register slash commands
        CommandManager.init()
        logger.info("CommandManager initialized.")
    }

    override fun onReady(event: ReadyEvent) {
        bot.updateCommands().addCommands(CommandManager.getAllCommandData()).queue()
        logger.info("Slash command registered.")

        logger.info("Logged in as ${bot.selfUser.asTag}")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        // ignore bot message
        if (event.author.isBot) return

        // get guild and yomiage state
        val guild = event.guild
        val state = YomiageStateStore[guild.id] ?: return
        if (state.textChannelId != event.channel.id) return

        // check config
        val config = guild.config
        if (config.mutedUsers.contains(event.author.id)) return // ignore muted user

        val message = event.message
        val content = message.contentRaw

        // on skip
        if (content == "^skip") {
            state.stopTrack()
            message.reply("現在再生中の読み上げをスキップしました。").queue()
            return
        }

        // ignore prefixed message
        if (content.startsWith("^")) return

        // queue message
        state.queueUserInput(message, guild, bot)
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val state = event.voiceState
        val channelId = state.channel?.id
        if (channelId == null && state.member.id == bot.selfUser.id) {
            // handle server side "disconnect"
            YomiageStateStore.remove(state.guild.id)?.shutdown()
            event.guild.audioManager.closeAudioConnection()
            return
        }
        event.jda.gatewayPool.schedule({
            val audioManager = event.guild.audioManager
            val ch = audioManager.connectedChannel ?: return@schedule
            if (ch.members.none { m -> !m.user.isBot }) {
                audioManager.closeAudioConnection()
            }
        }, 50, TimeUnit.MILLISECONDS)
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        CommandManager.onCommand(event)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
