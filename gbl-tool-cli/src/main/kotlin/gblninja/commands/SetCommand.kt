package gblninja.commands

import gblninja.core.GblFileManager

internal class SetCommand : Command {
    private val fileManager = GblFileManager()

    override fun execute(options: Map<String, String>) {
        val file = options["file"]
        val tagType = options["type"]
        val index = options["index"]

        if (file == null) {
            println("Error: No file specified")
            println("Usage: --set -f <filename> --index <N> --type <tag_type> [tag_parameters]")
            fileManager.printAvailableTagTypes()
            return
        }

        if (index == null) {
            println("Error: Index is required for set operation")
            println("Usage: --set -f <filename> --index <N> --type <tag_type> [tag_parameters]")
            println("Index specifies which tag to replace (0 = first tag, 1 = second tag, etc.)")
            return
        }

        val indexValue = index.toIntOrNull()
        if (indexValue == null || indexValue < 0) {
            println("Error: Invalid index '$index'. Index must be a non-negative integer.")
            return
        }

        if (tagType == null) {
            println("Error: No tag type specified")
            println("Usage: --set -f <filename> --index <N> --type <tag_type> [tag_parameters]")
            fileManager.printAvailableTagTypes()
            return
        }

        try {
            val updatedOptions = options.toMutableMap()
            updatedOptions["type"] = tagType
            updatedOptions["index"] = index

            fileManager.createSetTagCommand(updatedOptions)
        } catch (e: Exception) {
            println("Error setting tag: ${e.message}")
            e.printStackTrace()
        }
    }
}