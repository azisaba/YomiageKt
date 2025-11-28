package net.azisaba.yomiagekt.command

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Manage slash commands
 */
object CommandManager {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val commandMap: MutableMap<String, Command> = mutableMapOf()

    fun init() {
        register(DictionaryCommand())
    }

    private fun register(command: Command) {
        val cmdData = command.commandData

        // get command name and check it
        val cmdName = cmdData.name
        if (commandMap.containsKey(cmdName)) {
            throw RuntimeException("This command already registered: $cmdName")
        }

        // store slash command
        commandMap[cmdName] = command
        logger.debug("$cmdName command registered.")
    }
}
