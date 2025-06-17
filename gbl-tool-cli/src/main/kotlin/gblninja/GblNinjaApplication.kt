package gblninja

import gblninja.commands.*
import gblninja.core.*

internal class GblNinjaApplication {
    private val commandFactory = CommandFactory()
    private val helpPrinter = HelpPrinter()

    fun run(args: Array<String>) {
        if (args.isEmpty()) {
            helpPrinter.printHelp()
            return
        }

        try {
            val commandType = determineCommand(args)
            if (commandType == null) {
                println("Unknown command.")
                helpPrinter.printHelp()
                return
            }

            val options = OptionParser.parseOptions(args.toList())
            val command = commandFactory.createCommand(commandType)
            command.execute(options)

        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    private fun determineCommand(args: Array<String>): CommandType? {
        val flags = args.toList()
        return when {
            flags.contains("--gblinfo") || flags.contains("-i") -> CommandType.INFO
            flags.contains("--gblcreate") || flags.contains("-c") -> CommandType.CREATE
            flags.contains("--pack") -> CommandType.PACK
            flags.contains("--add") -> CommandType.ADD
            flags.contains("--remove") -> CommandType.REMOVE
            flags.contains("--set") -> CommandType.SET
            flags.contains("--create") -> CommandType.FINALIZE
            else -> null
        }
    }
}
