package net.azisaba.yomiagekt.config

import com.charleskorn.kaml.Yaml
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

@Serializable
data class GuildsConfig(
    val guilds: MutableMap<Snowflake, GuildConfig> = mutableMapOf(),
) {
    companion object {
        private lateinit var config: GuildsConfig

        fun load() {
            config = File("config/guild.yml").let { file ->
                if (!file.parentFile.exists()) file.parentFile.mkdirs()
                if (!file.exists()) file.writeText(Yaml.default.encodeToString(emptyMap<Snowflake, GuildConfig>()))
                Yaml.default.decodeFromString(file.readText())
            }
        }

        fun save() {
            File("config/guild.yml").writeText(Yaml.default.encodeToString(config))
        }

        operator fun get(guildId: Snowflake) = config.guilds.computeIfAbsent(guildId) { GuildConfig() }
    }
}

@Serializable
data class GuildConfig(
    val dictionary: MutableList<Pair<String, String>> = mutableListOf(),
    val mutedUsers: MutableSet<Snowflake> = mutableSetOf(),
) {
    fun <R> modifyDictionary(action: (MutableList<Pair<String, String>>) -> R): R =
        action(dictionary).apply { GuildsConfig.save() }

    fun <R> modifyMutedUsers(action: (MutableSet<Snowflake>) -> R): R =
        action(mutedUsers).apply { GuildsConfig.save() }
}
