package gblninja.commands

import gblninja.core.GblFileManager

internal class FinalizeCommand : Command {
    private val fileManager = GblFileManager()

    override fun execute(options: Map<String, String>) {
        val file = options["file"]
        val outputFile = options["output"] ?: file

        if (file == null) {
            println("Error: No file specified")
            println("Usage: --create -f <filename> [-o <output_file>]")
            return
        }

        try {
            fileManager.finalizeGblFile(file, outputFile)
            println("END tag added to file: ${outputFile ?: file}")
        } catch (e: Exception) {
            println("Error finalizing GBL file: ${e.message}")
        }
    }
}