package gblninja.commands

import gblninja.core.GblFileManager

internal class PackCommand : Command {
    private val fileManager = GblFileManager()
    
    override fun execute(options: Map<String, String>) {
        val file = options["file"]
        
        if (file == null) {
            println("Error: No file specified")
            println("Usage: --pack -f <filename>")
            return
        }
        
        try {
            fileManager.createEmptyGblFile(file)
            println("Empty GBL file with Header tag created: $file")
        } catch (e: Exception) {
            println("Error creating empty GBL file: ${e.message}")
        }
    }
}