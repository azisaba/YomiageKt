package net.azisaba.yomiagekt.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

@Serializable
data class GuildsConfig(
    val guilds: MutableMap<String, GuildConfig> = mutableMapOf(),
) {
    companion object {
        private lateinit var config: GuildsConfig

        fun load() {
            config =
                File("config/guild.yml").let { file ->
                    if (!file.parentFile.exists()) file.parentFile.mkdirs()
                    if (!file.exists()) file.writeText(Yaml.default.encodeToString(emptyMap<String, GuildConfig>()))
                    Yaml.default.decodeFromString(file.readText())
                }
        }

        fun save() {
            File("config/guild.yml").writeText(Yaml.default.encodeToString(config))
        }

        operator fun get(guildId: String) = config.guilds.computeIfAbsent(guildId) { GuildConfig() }
    }
}

@Serializable
data class GuildConfig(
    val dictionary: MutableList<Pair<String, String>> = mutableListOf(),
    val mutedUsers: MutableSet<String> = mutableSetOf(),
    var noOtoware: Boolean = true,
) {
    fun <R> modifyDictionary(action: (MutableList<Pair<String, String>>) -> R): R = action(dictionary).apply { GuildsConfig.save() }

    fun <R> modifyMutedUsers(action: (MutableSet<String>) -> R): R = action(mutedUsers).apply { GuildsConfig.save() }

    fun <R> modify(action: (GuildConfig) -> R): R = action(this).apply { GuildsConfig.save() }
}
