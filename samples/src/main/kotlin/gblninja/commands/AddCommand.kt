package gblninja.commands

import gblninja.core.GblFileManager

class AddCommand : Command {
    private val fileManager = GblFileManager()

    override fun execute(options: Map<String, String>) {
        val file = options["file"]
        val tagType = options["type"]
        val index = options["index"]

        if (file == null) {
            println("Error: No file specified")
            println("Usage: --add -f <filename> --index <N> --type <tag_type> [tag_parameters]")
            fileManager.printAvailableTagTypes()
            return
        }

        if (index == null) {
            println("Error: Index is required for add operation")
            println("Usage: --add -f <filename> --index <N> --type <tag_type> [tag_parameters]")
            println("Index specifies where to insert the tag (0 = beginning, 1 = after first tag, etc.)")
            return
        }

        val indexValue = index.toIntOrNull()
        if (indexValue == null || indexValue < 0) {
            println("Error: Invalid index '$index'. Index must be a non-negative integer.")
            return
        }

        if (tagType == null) {
            println("Error: No tag type specified")
            println("Usage: --add -f <filename> --index <N> --type <tag_type> [tag_parameters]")
            fileManager.printAvailableTagTypes()
            return
        }

        try {
            val updatedOptions = options.toMutableMap()
            updatedOptions["file"] = file
            updatedOptions["type"] = tagType
            updatedOptions["index"] = index

            fileManager.createAddTagCommand(updatedOptions)
        } catch (e: Exception) {
            println("Error adding tag: ${e.message}")
            e.printStackTrace()
        }
    }
}