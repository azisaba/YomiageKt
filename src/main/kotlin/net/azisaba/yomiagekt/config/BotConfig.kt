package net.azisaba.yomiagekt.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlComment
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File

@Serializable
data class BotConfig(
    @YamlComment(
        "次回起動時にbot.ymlを更新するかどうかを指定します。",
        "trueにした場合、次回起動時にbot.ymlは上書きされ、overwrite設定は自動的にfalseになります。",
    )
    var overwrite: Boolean = true,
    val botToken: String = "<bot token here>",
    val openAIApiKey: String = "",
    var voicevoxEndpoint: String = "http://localhost:50021",
) {
    companion object {
        private val yaml = Yaml(configuration = YamlConfiguration(encodeDefaults = true, strictMode = false))

        val config: BotConfig = File("config/bot.yml").let { file ->
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            if (!file.exists()) file.writeText(yaml.encodeToString(BotConfig()))
            yaml.decodeFromString(serializer(), file.readText())
        }

        init {
            config.voicevoxEndpoint = config.voicevoxEndpoint.trimEnd('/')

            if (config.overwrite) {
                config.overwrite = false
                File("config/bot.yml").writeText(yaml.encodeToString(config))
            }
        }
    }
}
