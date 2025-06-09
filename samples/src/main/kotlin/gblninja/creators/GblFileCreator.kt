package gblninja.creators

import GblParser
import java.io.FileOutputStream

internal class GblFileCreator {
    fun createGblFile(outputFile: String) {
        println("Creating GBL file...")
        println("Output file: $outputFile")

        val data = GblParser.GblBuilder.create().buildToByteArray()

        saveToFile(data, outputFile)
    }

    private fun saveToFile(data: ByteArray, filename: String) {
        try {
            FileOutputStream(filename).use { it.write(data) }
            println("Saved to $filename")
        } catch (e: Exception) {
            println("Failed to save file: ${e.message}")
            throw e
        }
    }
}