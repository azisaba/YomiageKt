package net.azisaba.yomiagekt.extension

import net.azisaba.yomiagekt.config.UserConfig
import net.azisaba.yomiagekt.config.UsersConfig
import net.dv8tion.jda.api.entities.Member

val Member.config: UserConfig
    get() = UsersConfig[id]
