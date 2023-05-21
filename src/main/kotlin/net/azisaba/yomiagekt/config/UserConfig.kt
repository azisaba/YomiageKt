package net.azisaba.yomiagekt.config

import com.charleskorn.kaml.Yaml
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.azisaba.yomiagekt.data.Characters
import java.io.File

@Serializable
data class UsersConfig(
    val users: MutableMap<Snowflake, UserConfig> = mutableMapOf(),
) {
    companion object {
        private lateinit var config: UsersConfig

        fun load() {
            config = File("config/user.yml").let { file ->
                if (!file.parentFile.exists()) file.parentFile.mkdirs()
                if (!file.exists()) file.writeText(Yaml.default.encodeToString(emptyMap<Snowflake, UserConfig>()))
                Yaml.default.decodeFromString(file.readText())
            }
        }

        fun save() {
            File("config/user.yml").writeText(Yaml.default.encodeToString(config))
        }

        operator fun get(userId: Snowflake) = config.users.computeIfAbsent(userId) { UserConfig() }
    }
}

@Serializable
data class UserConfig(
    var character: Characters = Characters.ZUNDA_N,
)
