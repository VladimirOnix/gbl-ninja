package gblninja.formatters

import gblninja.formatters.FullTagFormatter
import results.ParseResult
import java.io.File

internal class TagInfoFormatter {
    fun printFileInfo(file: File, parseResult: ParseResult.Success, format: String) {
        println("Parsing file: ${file.name}")
        println("File size: ${file.length()} bytes")
        println()

        val compactFormatter = CompactTagFormatter()
        val fullFormatter = FullTagFormatter()

        when (format.lowercase()) {
            "compact", "c" -> compactFormatter.printTags(parseResult.resultList)
            "full", "f", "hex", "h" -> fullFormatter.printTags(parseResult.resultList)
            else -> {
                println("Unknown format '$format'. Using compact format.")
                compactFormatter.printTags(parseResult.resultList)
            }
        }
    }
}