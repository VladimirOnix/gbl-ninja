package gblninja.commands

import gblninja.commands.AddCommand
import gblninja.core.CommandType
import gblninja.commands.FinalizeCommand
import gblninja.commands.InfoCommand
import gblninja.commands.PackCommand
import gblninja.commands.RemoveCommand
import gblninja.commands.SetCommand

internal class CommandFactory {
    fun createCommand(type: CommandType): Command {
        return when (type) {
            CommandType.INFO -> InfoCommand()
            CommandType.CREATE -> CreateCommand()
            CommandType.PACK -> PackCommand()
            CommandType.ADD -> AddCommand()
            CommandType.REMOVE -> RemoveCommand()
            CommandType.SET -> SetCommand()
            CommandType.FINALIZE -> FinalizeCommand()
        }
    }
}