package gblninja.core

import GblParser
import results.ParseResult
import java.io.File

internal class GblFileParser {
    fun parseFile(file: File): ParseResult.Success {
        val gblData = file.readBytes()
        val parser = GblParser()
        
        return when (val result = parser.parseByteArray(gblData)) {
            is ParseResult.Success -> result
            is ParseResult.Fatal -> throw Exception(result.error.toString())
        }
    }
}