package gblninja.commands

import gblninja.core.GblFileParser
import gblninja.formatters.TagInfoFormatter
import java.io.File

internal class InfoCommand : Command {
    private val parser = GblFileParser()
    private val formatter = TagInfoFormatter()
    
    override fun execute(options: Map<String, String>) {
        val file = options["file"]
        val format = options["format"] ?: "compact"
        
        if (file == null) {
            println("Error: No file specified")
            println("Usage: --gblinfo -f <filename> [--format <compact|full>]")
            return
        }
        
        val fileObj = File(file)
        if (!fileObj.exists()) {
            println("File not found: $file")
            return
        }
        
        try {
            val parseResult = parser.parseFile(fileObj)
            formatter.printFileInfo(fileObj, parseResult, format)
        } catch (e: Exception) {
            println("Error parsing GBL file: ${e.message}")
        }
    }
}