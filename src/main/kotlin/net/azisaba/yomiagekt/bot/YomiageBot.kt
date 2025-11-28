package net.azisaba.yomiagekt.bot

import net.azisaba.yomiagekt.config.BotConfig
import net.azisaba.yomiagekt.config.GuildsConfig
import net.azisaba.yomiagekt.config.UsersConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class YomiageBot : ListenerAdapter() {
    private lateinit var bot: JDA

    fun main() {
        // Load config
        BotConfig
        GuildsConfig.load()
        UsersConfig.load()
        logger.info("Config loaded!")

        // Create bot instance
        bot =
            JDABuilder
                .createDefault(BotConfig.config.botToken)
                .addEventListeners(this)
                .build()
    }

    override fun onReady(event: ReadyEvent) {
        logger.info("Bot on ready!")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
