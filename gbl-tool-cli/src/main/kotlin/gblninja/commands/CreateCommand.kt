package gblninja.commands

import gblninja.creators.GblFileCreator

internal class CreateCommand : Command {
    private val creator = GblFileCreator()
    
    override fun execute(options: Map<String, String>) {
        val outputFile = options["file"]

        if (outputFile == null) {
            println("Error: No output file specified")
            println("Usage: --gblcreate -f <output_file>")
            return
        }

        try {
            creator.createGblFile(outputFile)
            println("GBL file created successfully: $outputFile")
        } catch (e: Exception) {
            println("Error creating GBL file: ${e.message}")
        }
    }
}
