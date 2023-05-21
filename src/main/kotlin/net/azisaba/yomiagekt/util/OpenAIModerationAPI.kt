package net.azisaba.yomiagekt.util

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.azisaba.yomiagekt.config.BotConfig

object OpenAIModerationAPI {
    private val cache = mutableMapOf<String, Boolean>()
    internal val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(CIO) {
        engine {
            this.requestTimeout = 1000 * 10
        }
    }

    /**
     * Checks if [string] follows the terms of OpenAI
     * @return true if the string is safe; false otherwise
     */
    suspend fun check(string: String): Boolean {
        cache[string]?.let { return it }
        val result = run {
            if (BotConfig.config.openAIApiKey.isBlank()) {
                true
            } else {
                try {
                    val moderationResponse = client.post("https://api.openai.com/v1/moderations") {
                        setBody(json.encodeToString(PostModerationBody(string)))
                        header("Authorization", "Bearer ${BotConfig.config.openAIApiKey}")
                        header("Content-Type", "application/json")
                    }.bodyAsText().let { json.decodeFromString(PostModerationResponse.serializer(), it) }
                    moderationResponse.results.all { !it.flagged }
                } catch (e: Exception) {
                    println("OpenAI API returned error:")
                    e.printStackTrace()
                    true
                }
            }
        }
        cache[string] = result
        return result
    }

    @Serializable
    private data class PostModerationBody(
        val input: String,
    )

    @Serializable
    private data class PostModerationResponse(
        val id: String,
        val model: String,
        val results: List<PostModerationResponseResults>,
    )

    @Serializable
    private data class PostModerationResponseResults(
        val flagged: Boolean,
    )
}
