package net.azisaba.yomiagekt.extension

import net.azisaba.yomiagekt.config.GuildConfig
import net.azisaba.yomiagekt.config.GuildsConfig
import net.dv8tion.jda.api.entities.Guild

/**
 * Get config for each guild
 */
val Guild.config: GuildConfig
    get() = GuildsConfig[id]
