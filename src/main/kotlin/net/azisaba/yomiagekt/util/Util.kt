package net.azisaba.yomiagekt.util

import dev.kord.common.entity.CommandArgument
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.SubCommand
import dev.kord.common.entity.optional.Optional
import dev.kord.core.cache.data.AttachmentData
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.core.entity.interaction.Interaction
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import kotlin.math.min

object Util {
    private fun Interaction.optAny(name: String): Any? =
        when (this) {
            is ApplicationCommandInteraction ->
                this.data
                    .data
                    .options
                    .value
                    ?.find { it.name == name }
                    ?.value
                    ?.value
                    ?.value

            is ModalSubmitInteraction ->
                this.textInputs[name]?.value
                    ?: this.data.data.options.value?.find { it.name == name }?.value?.value?.value

            else -> null
        }

    fun Interaction.optString(name: String) = optAny(name)?.toString()

    fun Interaction.optSnowflake(name: String) = optString(name)?.toULong()?.let { Snowflake(it) }

    fun Interaction.optLong(name: String) = optDouble(name)?.toLong()

    fun Interaction.optDouble(name: String) = optString(name)?.toDouble()

    fun Interaction.optBoolean(name: String) = optString(name)?.toBoolean()

    fun Interaction.optAttachments(): List<AttachmentData> =
        this.data
            .data
            .resolvedObjectsData
            .value
            ?.attachments
            ?.value
            ?.values
            ?.toList()
            ?: emptyList()

    fun Interaction.optSubcommand(name: String) =
        this.data
            .data
            .options
            .value
            ?.find { it.name == name }
            ?.values

    fun Interaction.optSubCommands(groupName: String, subCommandName: String): SubCommand? =
        this.data
            .data
            .options
            .value
            ?.find { it.name == groupName }
            ?.subCommands
            ?.value
            ?.find { it.name == subCommandName }

    private fun Optional<List<CommandArgument<*>>>.optAny(name: String) = value?.find { it.name == name }?.value
    fun Optional<List<CommandArgument<*>>>.optString(name: String) = optAny(name)?.toString()
    fun Optional<List<CommandArgument<*>>>.optDouble(name: String) = optString(name)?.toDouble()
    fun Optional<List<CommandArgument<*>>>.optBoolean(name: String) = optAny(name) as Boolean?
    fun Optional<List<CommandArgument<*>>>.optSnowflake(name: String) = optAny(name) as Snowflake?
    fun Optional<List<CommandArgument<*>>>.optLong(name: String) = optAny(name) as Long?

    fun levenshtein(lhs: CharSequence, rhs: CharSequence) : Int {
        if (lhs == rhs) return 0
        if (lhs.isEmpty()) return rhs.length
        if (rhs.isEmpty()) return lhs.length

        val lhsLength = lhs.length + 1
        val rhsLength = rhs.length + 1

        var cost = Array(lhsLength) { it }
        var newCost = Array(lhsLength) { 0 }

        for (i in 1 until rhsLength) {
            newCost[0] = i

            for (j in 1 until lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength - 1]
    }
}
