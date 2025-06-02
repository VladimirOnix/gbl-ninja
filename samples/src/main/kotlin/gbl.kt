import results.ParseResult
import tag.Tag
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
        "gblbootloader" -> {
            if (args.size < 2) {
                println("Usage: gblbootloader <output_file>")
                return
            }
            createBootloaderGbl(args[1])
        }
        "gblmetadata" -> {
            if (args.size < 2) {
                println("Usage: gblmetadata <output_file>")
                return
            }
            createMetadataGbl(args[1], args[2])
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
          gblinfo <file>                - Parse and display info about a GBL file
          gblempty <file>               - Create an empty GBL file with application and dummy data
          gblbootloader <file>          - Create a GBL file with bootloader and program data
          gblmetadata <file> <metadata> - Create a GBL file with metadata tag only
    """.trimIndent())
}

fun createEmptyGbl(filename: String) {
    val gblBuilder = GblParser.Builder.createEmpty()
        .addApplication()
        .addProg(231U, ByteArray(1024))
        .addEraseProg()

    val gblData = gblBuilder.buildToByteArray()
    saveToFile(gblData, filename)

    println("Empty GBL file created: $filename")
    println("Size: ${gblData.size} bytes")
}

fun createBootloaderGbl(filename: String) {
    val gblBuilder = GblParser.Builder.createEmpty()
        .addApplication()
        .addBootloader(
            bootloaderVersion = 0U,
            address = 0x100U,
            data = ByteArray(1024) { 0xFF.toByte() }
        )
        .addProg(231U, byteArrayOf(0x01, 0x02, 0x03))
        .addEraseProg()

    val gblData = gblBuilder.buildToByteArray()
    saveToFile(gblData, filename)

    println("GBL with Bootloader created: $filename")
    println("Size: ${gblData.size} bytes")
}

fun createMetadataGbl(filename: String, metadata: String) {
    val gblBuilder = GblParser.Builder.createEmpty()
        .addMetadata(metadata.toByteArray())

    val gblData = gblBuilder.buildToByteArray()
    saveToFile(gblData, filename)

    println("Metadata GBL file created: $filename")
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
        is ParseResult.Success -> {
            printTagInfo(result.resultList)
        }
        is ParseResult.Fatal -> {
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
