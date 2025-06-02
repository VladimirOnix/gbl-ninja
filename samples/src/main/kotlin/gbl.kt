import gbl.tag.DefaultTag
import results.ParseResult
import tag.GblType
import tag.Tag
import tag.TagWithHeader
import tag.type.GblBootloader
import tag.type.GblEnd
import tag.type.GblEraseProg
import tag.type.GblHeader
import tag.type.GblMetadata
import tag.type.GblProg
import tag.type.GblProgLz4
import tag.type.GblProgLzma
import tag.type.GblSeUpgrade
import tag.type.application.GblApplication
import tag.type.certificate.GblCertificateEcdsaP256
import tag.type.certificate.GblSignatureEcdsaP256
import tag.type.encryption.GblEncryptionData
import tag.type.encryption.GblEncryptionInitAesCcm
import tag.type.version.GblVersionDependency
import java.io.File
import java.io.FileOutputStream

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printHelp()
        return
    }

    val flags = args.toList()
    val command = when {
        flags.contains("--gblinfo") || flags.contains("-i") -> "gblinfo"
        flags.contains("--gblempty") || flags.contains("-e") -> "gblempty"
        flags.contains("--gblbootloader") || flags.contains("-b") -> "gblbootloader"
        flags.contains("--gblmetadata") || flags.contains("-m") -> "gblmetadata"
        else -> null
    }

    if (command == null) {
        println("Unknown command.")
        printHelp()
        return
    }

    val options = parseOptions(flags)

    val format = options["format"] ?: options["F"] ?: "content"

    when (command) {
        "gblinfo" -> {
            val file = options["file"]
            if (file == null) {
                println("Usage: --gblinfo -f <filename> [--format <content|short>]")
                return
            }
            parseExistingGbl(file, format)
        }
        "gblempty" -> {
            val output = options["file"]
            if (output == null) {
                println("Usage: --gblempty -f <output_file>")
                return
            }
            createEmptyGbl(output)
        }
        "gblbootloader" -> {
            val output = options["file"]
            if (output == null) {
                println("Usage: --gblbootloader -f <output_file>")
                return
            }
            createBootloaderGbl(output)
        }
        "gblmetadata" -> {
            val output = options["file"]
            val version = options["version"]
            if (output == null || version == null) {
                println("Usage: --gblmetadata -f <output_file> -v <version>")
                return
            }
            createMetadataGbl(output, version)
        }
    }
}

fun parseOptions(args: List<String>): Map<String, String> {
    val result = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "-f", "--file" -> {
                if (i + 1 < args.size) {
                    result["file"] = args[i + 1]
                    i += 2
                } else {
                    i++
                }
            }
            "-F", "--format", "-fmt" -> {
                if (i + 1 < args.size) {
                    result["format"] = args[i + 1]
                    i += 2
                } else {
                    i++
                }
            }
            else -> i++
        }
    }
    return result
}


fun printHelp() {
    println(
        """
        Commands:
          -i, --gblinfo <file>             Show information about a GBL file
          -e, --gblempty <file>            Create an empty GBL file
          -b, --gblbootloader <file>       Create a bootloader GBL file
          -m, --gblmetadata <file> -v <version> 
                                           Create a GBL file with metadata

        Arguments:
          -f, --file <path>                Path to the input/output file
          -F, --format <content|short>    Output format for gblinfo command (default: content)

        Examples:
          ./tool --gblinfo -f firmware.gbl --format content
          ./tool -i -f firmware.gbl -F short
          ./tool -e --file empty.gbl
        """.trimIndent()
    )
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

fun parseExistingGbl(filename: String, format: String = "content") {
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
            printTagInfo(result.resultList, format)
        }
        is ParseResult.Fatal -> {
            println("Error parsing GBL file: ${result.error}")
        }
    }
}

fun formatBytes(data: ByteArray): String {
    return data.joinToString(" ") { "%02x".format(it) }
}

fun printTagInfo(tags: List<Tag>, format: String = "content") {
    println("format $format")
    fun bytesToHexWithLimit(bytes: ByteArray, bytesPerLine: Int = 16): List<String> {
        val hexStrings = bytes.map { "%02X".format(it) }
        val lines = mutableListOf<String>()
        for (i in hexStrings.indices step bytesPerLine) {
            val lineBytes = hexStrings.subList(i, minOf(i + bytesPerLine, hexStrings.size))
            lines.add(lineBytes.joinToString(" "))
        }
        return lines
    }

    println("GBL file contains ${tags.size} tag(s):")

    tags.forEachIndexed { index, tag ->
        println()
        println("Tag ${index + 1}: ${tag.tagType}")

        fun printFormatted(label: String, value: Any?) {
            val strValue = when (value) {
                is ByteArray -> if (format == "short") {
                    val lines = bytesToHexWithLimit(value)
                    lines.joinToString("\n") { it }
                } else formatBytes(value)
                else -> value.toString()
            }
            val chunks = strValue.chunked(23)
            println("  $label: ${chunks.firstOrNull() ?: ""}")
            chunks.drop(1).forEach { println("          ${it.trim()}") }
        }

        if (format == "short") {
            if(tag is TagWithHeader) {
                val t = tag as TagWithHeader
                val lines = bytesToHexWithLimit(t.tagData)
                println("  ${lines.firstOrNull() ?: ""}")
                lines.drop(1).forEach { println("  $it") }
            }
        } else {
            when (tag.tagType) {
                GblType.HEADER_V3 -> {
                    val t = tag as GblHeader
                    printFormatted("GBLVersion", t.version)
                    printFormatted("GBLType", t.gblType)
                }
                GblType.BOOTLOADER -> {
                    val t = tag as GblBootloader
                    printFormatted("BootloaderVersion", t.bootloaderVersion)
                    printFormatted("Address", t.address)
                    printFormatted("Data", t.data)
                }
                GblType.APPLICATION -> {
                    val t = tag as GblApplication
                    printFormatted("Version", t.applicationData.version)
                    printFormatted("Type", t.applicationData.type)
                    printFormatted("ProductId", t.applicationData.productId)
                    printFormatted("Capabilities", t.applicationData.capabilities)
                }
                GblType.METADATA -> {
                    val t = tag as GblMetadata
                    printFormatted("MetaData", t.metaData)
                }
                GblType.PROG -> {
                    val t = tag as GblProg
                    printFormatted("FlashStartAddress", t.flashStartAddress)
                    printFormatted("Data", t.data)
                }
                GblType.PROG_LZ4 -> {
                    val t = tag as GblProgLz4
                    printFormatted("PROG_LZ4 Data", t.tagData)
                }
                GblType.PROG_LZMA -> {
                    val t = tag as GblProgLzma
                    printFormatted("PROG_LZMA Data", t.tagData)
                }
                GblType.ERASEPROG -> {
                    val t = tag as GblEraseProg
                    printFormatted("ERASEPROG Data", t.tagData)
                }
                GblType.SE_UPGRADE -> {
                    val t = tag as GblSeUpgrade
                    printFormatted("Version", t.version)
                    printFormatted("BlobSize", t.blobSize)
                    printFormatted("SE_UPGRADE Data", t.data)
                }
                GblType.END -> {
                    val t = tag as GblEnd
                    printFormatted("GblCrc", t.gblCrc)
                }
                GblType.TAG -> {
                    val t = tag as DefaultTag
                    printFormatted("TagData", t.tagData)
                }
                GblType.ENCRYPTION_DATA -> {
                    val t = tag as GblEncryptionData
                    printFormatted("EncryptedGblData", t.encryptedGblData)
                }
                GblType.ENCRYPTION_INIT -> {
                    val t = tag as GblEncryptionInitAesCcm
                    printFormatted("MSGLen", t.msgLen)
                    printFormatted("Nonce", t.nonce)
                }
                GblType.SIGNATURE_ECDSA_P256 -> {
                    val t = tag as GblSignatureEcdsaP256
                    printFormatted("R", t.r)
                    printFormatted("S", t.s)
                }
                GblType.CERTIFICATE_ECDSA_P256 -> {
                    val t = tag as GblCertificateEcdsaP256
                    printFormatted("Certificate", t.certificate)
                }
                GblType.VERSION_DEPENDENCY -> {
                    val t = tag as GblVersionDependency
                    printFormatted("Version", t.version)
                    printFormatted("Reversed", t.reversed)
                    printFormatted("ImageType", t.imageType)
                    printFormatted("Statement", t.statement)
                }
                else -> {
                    println("  Details not implemented for this type")
                }
            }
        }
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
