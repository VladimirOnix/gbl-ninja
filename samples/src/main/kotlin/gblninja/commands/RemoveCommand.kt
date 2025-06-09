package gblninja.commands

import gblninja.core.GblFileManager

internal class RemoveCommand : Command {
    private val fileManager = GblFileManager()

    override fun execute(options: Map<String, String>) {
        val file = options["file"]
        val index = options["index"]

        if (file == null) {
            println("Error: No file specified")
            println("Usage: --remove -f <filename> --index <N>")
            return
        }

        if (index == null) {
            println("Error: Index is required for remove operation")
            println("Usage: --remove -f <filename> --index <N>")
            println("Index specifies which tag to remove (0 = first tag, 1 = second tag, etc.)")
            return
        }

        val indexValue = index.toIntOrNull()
        if (indexValue == null || indexValue < 0) {
            println("Error: Invalid index '$index'. Index must be a non-negative integer.")
            return
        }

        try {
            val updatedOptions = options.toMutableMap()
            updatedOptions["target"] = "by_index"
            updatedOptions["index"] = index

            fileManager.createRemoveTagCommand(updatedOptions)
        } catch (e: Exception) {
            println("Error removing tag: ${e.message}")
            e.printStackTrace()
        }
    }
}