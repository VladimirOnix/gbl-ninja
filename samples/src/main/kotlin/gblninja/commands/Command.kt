package gblninja.commands

interface Command {
    fun execute(options: Map<String, String>)
}