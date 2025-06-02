import gbl.GblParser
import gbl.tag.Tag
import java.io.File
import java.io.FileOutputStream

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printHelp()
        return
    }

    when (args[0].lowercase()) {
        "gblinfo" -> {
            if (args.size < 2) {
                println("Usage: gblinfo <filename>")
                return
            }
            parseExistingGbl(args[1])
        }
        "gblempty" -> {
            if (args.size < 2) {
                println("Usage: gblempty <output_file>")
                return
            }
            createEmptyGbl(args[1])
        }
        "gblsimple" -> {
            if (args.size < 2) {
                println("Usage: gblsimple <output_file>")
                return
            }
            createSimpleGbl(args[1])
        }
        else -> {
            println("Unknown command: ${args[0]}")
            printHelp()
        }
    }
}

fun printHelp() {
    println("""
        Usage: <command> [options]
        Commands:
          gblinfo <file>     - Parse and display info about a GBL file
          gblempty <file>    - Create an empty GBL file
          gblsimple <file>   - Create a simple GBL file with program data
    """.trimIndent())
}

fun createEmptyGbl(filename: String) {
    val gblData = GblParser.Builder.createEmpty().buildToByteArray()
    saveToFile(gblData, filename)
    println("Empty GBL file created: $filename")
}

fun createSimpleGbl(filename: String) {
    val gblBuilder = GblParser.Builder.createEmpty()
        .addApplication()
        .addProg(231U, ByteArray(1024))
        .addEraseProg()

    val gblData = gblBuilder.buildToByteArray()
    saveToFile(gblData, filename)

    println("Simple GBL file created: $filename")
    println("Size: ${gblData.size} bytes")
}

fun parseExistingGbl(filename: String) {
    println("Parsing file: $filename")

    val file = File(filename)
    if (!file.exists()) {
        println("File not found: $filename")
        return
    }

    val gblData = file.readBytes()
    val parser = GblParser()

    when (val result = parser.parseFile(gblData)) {
        is gbl.results.ParseResult.Success -> {
            printTagInfo(result.resultList)
        }
        is gbl.results.ParseResult.Fatal -> {
            println("Error parsing GBL file: ${result.error}")
        }
    }
}

fun printTagInfo(tags: List<Tag>) {
    println("GBL file contains ${tags.size} tags:")
    tags.forEachIndexed { index, tag ->
        println("Tag ${index + 1}: ${tag.tagType}")
    }
}

fun saveToFile(data: ByteArray, filename: String) {
    try {
        FileOutputStream(filename).use { it.write(data) }
        println("Saved to $filename")
    } catch (e: Exception) {
        println("Failed to save file: ${e.message}")
    }
}
