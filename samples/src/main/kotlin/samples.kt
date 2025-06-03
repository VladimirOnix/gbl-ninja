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
        flags.contains("--gblcreate") || flags.contains("-c") -> "gblcreate"
        else -> null
    }

    if (command == null) {
        println("Unknown command.")
        printHelp()
        return
    }

    val options = parseOptions(flags)

    when (command) {
        "gblinfo" -> {
            val file = options["file"]
            val format = options["format"] ?: options["F"] ?: "content"
            if (file == null) {
                println("Error: No file specified")
                println("Usage: --gblinfo -f <filename> [--format <content|short>]")
                return
            }
            parseExistingGbl(file, format)
        }
        "gblcreate" -> {
            handleGblCreate(options)
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
            "-t", "--type" -> {
                if (i + 1 < args.size) {
                    result["type"] = args[i + 1]
                    i += 2
                } else {
                    i++
                }
            }
            "-v", "--version" -> {
                if (i + 1 < args.size) {
                    result["version"] = args[i + 1]
                    i += 2
                } else {
                    i++
                }
            }
            "-m", "--metadata" -> {
                if (i + 1 < args.size) {
                    result["metadata"] = args[i + 1]
                    i += 2
                } else {
                    i++
                }
            }
            "-a", "--address" -> {
                if (i + 1 < args.size) {
                    result["address"] = args[i + 1]
                    i += 2
                } else {
                    i++
                }
            }
            "-s", "--size" -> {
                if (i + 1 < args.size) {
                    result["size"] = args[i + 1]
                    i += 2
                } else {
                    i++
                }
            }
            "-d", "--data" -> {
                if (i + 1 < args.size) {
                    result["data"] = args[i + 1]
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
        GBL Tool - Utility for working with GBL files
        
        Commands:
          -i, --gblinfo <file>             Show information about a GBL file
          -c, --gblcreate                  Create a new GBL file
        
        Options for gblinfo:
          -f, --file <path>                Path to the input file
          -F, --format <content|short>     Output format (default: content)
        
        Options for gblcreate:
          -f, --file <path>                Path to the output file
          -t, --type <type>                Type of GBL file:
                                             empty         - empty GBL file
                                             bootloader    - GBL with bootloader
                                             metadata      - GBL with metadata
                                             prog-lz4      - GBL with LZ4 compressed program
                                             prog-lzma     - GBL with LZMA compressed program
                                             se-upgrade    - GBL with SE upgrade
                                             encrypted     - encrypted GBL file
                                             signed        - signed GBL file
                                             version-dep   - GBL with version dependency
          -v, --version <version>          Version (for bootloader/se-upgrade)
          -m, --metadata <text>            Metadata text (for metadata)
          -a, --address <hex>              Address (for bootloader/prog, hex format)
          -s, --size <bytes>               Data size in bytes
          -d, --data <file>                Data file (optional)
          -n, --nonce <hex>                Nonce for encryption (hex format)
          -r, --r-value <hex>              R value for signature (hex format)
          --s-value <hex>                  S value for signature (hex format)
          --dependency <version>           Version dependency string
        
        Examples:
          ./tool --gblinfo -f firmware.gbl --format content
          ./tool -i -f firmware.gbl -F short
          ./tool --gblcreate -f empty.gbl -t empty
          ./tool -c -f boot.gbl -t bootloader -v 1.0 -a 0x1000 -s 1024
          ./tool -c -f meta.gbl -t metadata -m "Firmware v2.1.0"
          ./tool -c -f compressed.gbl -t prog-lz4 -a 0x2000 -s 2048
          ./tool -c -f secure.gbl -t encrypted -s 1024 -n 0x12
          ./tool -c -f signed.gbl -t signed -s 512 -r 0x11 --s-value 0x22
          ./tool -c -f se.gbl -t se-upgrade -v 2 -s 512
          ./tool -c -f dep.gbl -t version-dep --dependency "2.1.0"
        """.trimIndent()
    )
}

fun handleGblCreate(options: Map<String, String>) {
    val outputFile = options["file"]
    val type = options["type"]

    if (outputFile == null) {
        println("Error: No output file specified")
        println("Usage: --gblcreate -f <output_file> -t <type> [additional parameters]")
        return
    }

    if (type == null) {
        println("Error: No GBL file type specified")
        println("Available types: empty, bootloader, metadata, prog-lz4, prog-lzma, se-upgrade, encrypted, signed, certificate, version-dep")
        return
    }

    println("Creating GBL file...")
    println("Output file: $outputFile")
    println("Type: $type")

    when (type.lowercase()) {
        "empty" -> createEmptyGbl(outputFile, options)
        "bootloader" -> createBootloaderGbl(outputFile, options)
        "metadata" -> createMetadataGbl(outputFile, options)
        "prog-lz4" -> createProgLz4Gbl(outputFile, options)
        "prog-lzma" -> createProgLzmaGbl(outputFile, options)
        "se-upgrade" -> createSeUpgradeGbl(outputFile, options)
        "encrypted" -> createEncryptedGbl(outputFile, options)
        "signed" -> createSignedGbl(outputFile, options)
        "version-dep" -> createVersionDependencyGbl(outputFile, options)
        else -> {
            println("Error: Unknown type '$type'")
            println("Available types: empty, bootloader, metadata, prog-lz4, prog-lzma, se-upgrade, encrypted, signed, certificate, version-dep")
        }
    }
}

fun createEmptyGbl(filename: String, options: Map<String, String>) {
    println("Creating empty GBL file...")

    val size = options["size"]?.toIntOrNull() ?: 1024
    println("Data size: $size bytes")

    try {
        val gblBuilder = GblParser.Builder.createEmpty()
            .addApplication()
            .addProg(231U, ByteArray(size))
            .addEraseProg()

        val gblData = gblBuilder.buildToByteArray()
        saveToFile(gblData, filename)

        println("Empty GBL file created successfully!")
        printFileInfo(filename, gblData.size)

    } catch (e: Exception) {
        println("Error creating file: ${e.message}")
    }
}

fun createBootloaderGbl(filename: String, options: Map<String, String>) {
    println("Creating GBL file with bootloader...")

    val version = options["version"] ?: "0.0"
    val addressStr = options["address"] ?: "0x100"
    val size = options["size"]?.toIntOrNull() ?: 1024
    val dataFile = options["data"]

    println("Bootloader version: $version")
    println("Address: $addressStr")
    println("Size: $size bytes")

    val address = try {
        if (addressStr.startsWith("0x", true)) {
            addressStr.substring(2).toUInt(16)
        } else {
            addressStr.toUInt()
        }
    } catch (e: NumberFormatException) {
        println("Error: Invalid address format '$addressStr'")
        return
    }

    val bootloaderData = if (dataFile != null && File(dataFile).exists()) {
        println("Loading data from file: $dataFile")
        File(dataFile).readBytes()
    } else {
        println("Using placeholder data (0xFF)")
        ByteArray(size) { 0xFF.toByte() }
    }

    try {
        val gblBuilder = GblParser.Builder.createEmpty()
            .addApplication()
            .addBootloader(
                bootloaderVersion = 0U,
                address = address,
                data = bootloaderData
            )
            .addProg(231U, byteArrayOf(0x01, 0x02, 0x03))
            .addEraseProg()

        val gblData = gblBuilder.buildToByteArray()
        saveToFile(gblData, filename)

        println("GBL with Bootloader created successfully!")
        printFileInfo(filename, gblData.size)
        println("Bootloader address: 0x${address.toString(16).uppercase()}")

    } catch (e: Exception) {
        println("Error creating file: ${e.message}")
    }
}

fun createMetadataGbl(filename: String, options: Map<String, String>) {
    println("Creating GBL file with metadata...")

    val metadata = options["metadata"]
    if (metadata == null) {
        println("Error: No metadata specified")
        println("Usage: -t metadata -m \"metadata text\"")
        return
    }

    println("Metadata: \"$metadata\"")
    println("Metadata size: ${metadata.length} characters")

    try {
        val gblBuilder = GblParser.Builder.createEmpty()
            .addMetadata(metadata.toByteArray())

        val gblData = gblBuilder.buildToByteArray()
        saveToFile(gblData, filename)

        println("GBL with metadata created successfully!")
        printFileInfo(filename, gblData.size)

    } catch (e: Exception) {
        println("Error creating file: ${e.message}")
    }
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
            println("File parsed successfully")
            printTagInfo(result.resultList, format)
        }
        is ParseResult.Fatal -> {
            println("Error parsing GBL file: ${result.error}")
        }
    }
}

fun createProgLz4Gbl(filename: String, options: Map<String, String>) {
    println("Creating GBL file with LZ4 compressed program...")

    val addressStr = options["address"] ?: "0x1000"
    val size = options["size"]?.toIntOrNull() ?: 1024
    val dataFile = options["data"]

    val address = try {
        if (addressStr.startsWith("0x", true)) {
            addressStr.substring(2).toUInt(16)
        } else {
            addressStr.toUInt()
        }
    } catch (e: NumberFormatException) {
        println("Error: Invalid address format '$addressStr'")
        return
    }

    val originalData = if (dataFile != null && File(dataFile).exists()) {
        println("Loading data from file: $dataFile")
        File(dataFile).readBytes()
    } else {
        println("Using placeholder data")
        ByteArray(size) { (it % 256).toByte() }
    }

    // Simulate LZ4 compression (in real implementation, use actual LZ4 library)
    val compressedData = ByteArray(originalData.size / 2) { (it % 128).toByte() }

    try {
        val gblBuilder = GblParser.Builder.createEmpty()
            .addApplication()
            .addProgLz4(
                flashStartAddress = address,
                compressedData = compressedData,
                decompressedSize = originalData.size.toUInt()
            )
            .addEraseProg()

        val gblData = gblBuilder.buildToByteArray()
        saveToFile(gblData, filename)

        println("GBL with LZ4 compression created successfully!")
        printFileInfo(filename, gblData.size)
        println("Flash address: 0x${address.toString(16).uppercase()}")
        println("Original size: ${originalData.size} bytes")
        println("Compressed size: ${compressedData.size} bytes")

    } catch (e: Exception) {
        println("Error creating file: ${e.message}")
    }
}

fun createProgLzmaGbl(filename: String, options: Map<String, String>) {
    println("Creating GBL file with LZMA compressed program...")

    val addressStr = options["address"] ?: "0x1000"
    val size = options["size"]?.toIntOrNull() ?: 1024
    val dataFile = options["data"]

    val address = try {
        if (addressStr.startsWith("0x", true)) {
            addressStr.substring(2).toUInt(16)
        } else {
            addressStr.toUInt()
        }
    } catch (e: NumberFormatException) {
        println("Error: Invalid address format '$addressStr'")
        return
    }

    val originalData = if (dataFile != null && File(dataFile).exists()) {
        println("Loading data from file: $dataFile")
        File(dataFile).readBytes()
    } else {
        println("Using placeholder data")
        ByteArray(size) { (it % 256).toByte() }
    }

    // Simulate LZMA compression (in real implementation, use actual LZMA library)
    val compressedData = ByteArray(originalData.size / 3) { (it % 64).toByte() }

    try {
        val gblBuilder = GblParser.Builder.createEmpty()
            .addApplication()
            .addProgLzma(
                flashStartAddress = address,
                compressedData = compressedData,
                decompressedSize = originalData.size.toUInt()
            )
            .addEraseProg()

        val gblData = gblBuilder.buildToByteArray()
        saveToFile(gblData, filename)

        println("GBL with LZMA compression created successfully!")
        printFileInfo(filename, gblData.size)
        println("Flash address: 0x${address.toString(16).uppercase()}")
        println("Original size: ${originalData.size} bytes")
        println("Compressed size: ${compressedData.size} bytes")

    } catch (e: Exception) {
        println("Error creating file: ${e.message}")
    }
}

fun createSeUpgradeGbl(filename: String, options: Map<String, String>) {
    println("Creating GBL file with SE upgrade...")

    val version = options["version"]?.toUIntOrNull() ?: 1U
    val size = options["size"]?.toIntOrNull() ?: 512
    val dataFile = options["data"]

    println("SE version: $version")
    println("Data size: $size bytes")

    val seData = if (dataFile != null && File(dataFile).exists()) {
        println("Loading SE data from file: $dataFile")
        File(dataFile).readBytes()
    } else {
        println("Using placeholder SE data")
        ByteArray(size) { 0xAA.toByte() }
    }

    try {
        val gblBuilder = GblParser.Builder.createEmpty()
            .addApplication()
            .addSeUpgrade(
                version = version,
                data = seData
            )
            .addEraseProg()

        val gblData = gblBuilder.buildToByteArray()
        saveToFile(gblData, filename)

        println("GBL with SE upgrade created successfully!")
        printFileInfo(filename, gblData.size)
        println("SE version: $version")
        println("SE data size: ${seData.size} bytes")

    } catch (e: Exception) {
        println("Error creating file: ${e.message}")
    }
}

fun createEncryptedGbl(filename: String, options: Map<String, String>) {
    println("Creating encrypted GBL file...")

    val size = options["size"]?.toIntOrNull() ?: 1024
    val nonce = options["nonce"]?.toUByteOrNull() ?: 0x12U

    println("Data size: $size bytes")
    println("Nonce: 0x${nonce.toString(16).uppercase()}")

    // Create some sample encrypted data
    val encryptedData = ByteArray(size) { ((it + nonce.toInt()) % 256).toByte() }.map { it.toByte() }.toByteArray()

    try {
        val gblBuilder = GblParser.Builder.createEmpty()
            .addApplication()
            .addEncryptionInit(
                msgLen = size.toUInt(),
                nonce = nonce.toUByte()
            )
            .addEncryptionData(encryptedData)
            .addEraseProg()

        val gblData = gblBuilder.buildToByteArray()
        saveToFile(gblData, filename)

        println("Encrypted GBL file created successfully!")
        printFileInfo(filename, gblData.size)
        println("Encryption nonce: 0x${nonce.toString(16).uppercase()}")
        println("Encrypted data size: ${encryptedData.size} bytes")

    } catch (e: Exception) {
        println("Error creating file: ${e.message}")
    }
}

fun createSignedGbl(filename: String, options: Map<String, String>) {
    println("Creating signed GBL file...")

    val size = options["size"]?.toIntOrNull() ?: 1024
    val rValue = options["r"]?.toUByteOrNull() ?: 0x11U
    val sValue = options["s"]?.toUByteOrNull() ?: 0x22U

    println("Data size: $size bytes")
    println("Signature R: 0x${rValue.toString(16).uppercase()}")
    println("Signature S: 0x${sValue.toString(16).uppercase()}")

    try {
        val gblBuilder = GblParser.Builder.createEmpty()
            .addApplication()
            .addProg(0x1000U, ByteArray(size) { (it % 256).toByte() })
            .addSignatureEcdsaP256(
                r = rValue.toUByte(),
                s = sValue.toUByte()
            )
            .addEraseProg()

        val gblData = gblBuilder.buildToByteArray()
        saveToFile(gblData, filename)

        println("Signed GBL file created successfully!")
        printFileInfo(filename, gblData.size)
        println("ECDSA P256 signature applied")

    } catch (e: Exception) {
        println("Error creating file: ${e.message}")
    }
}

fun createVersionDependencyGbl(filename: String, options: Map<String, String>) {
    println("Creating GBL file with version dependency...")

    val dependencyVersion = options["dependency"] ?: "1.0.0"
    val dependencyData = dependencyVersion.toByteArray()

    println("Version dependency: $dependencyVersion")

    try {
        val gblBuilder = GblParser.Builder.createEmpty()
            .addApplication()
            .addVersionDependency(dependencyData)
            .addProg(0x1000U, ByteArray(256) { (it % 256).toByte() })
            .addEraseProg()

        val gblData = gblBuilder.buildToByteArray()
        saveToFile(gblData, filename)

        println("GBL file with version dependency created successfully!")
        printFileInfo(filename, gblData.size)
        println("Version dependency: $dependencyVersion")

    } catch (e: Exception) {
        println("Error creating file: ${e.message}")
    }
}

fun printTagInfo(tags: List<Tag>, format: String = "content") {
    fun bytesToHexWithLimit(bytes: ByteArray, bytesPerLine: Int = 16): List<String> {
        return bytes.asSequence()
            .mapIndexed { index, byte -> "%02X".format(byte) }
            .chunked(bytesPerLine)
            .map { it.joinToString(" ") }
            .toList()
    }

    println("GBL file contains ${tags.size} tag(s):")

    tags.forEachIndexed { index, tag ->
        println()
        println("Tag ${index + 1}: ${tag.tagType}")

        fun printFormatted(label: String, value: Any?) {
            val labelPadding = label.padEnd(20)

            if (value is ByteArray) {
                val lines = bytesToHexWithLimit(value)
                println("  $labelPadding: ${lines.firstOrNull() ?: ""}")
                lines.drop(1).forEach { println(" ".repeat(labelPadding.length + 4) + it) }
            } else {
                println("  $labelPadding: $value")
            }
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
                    printFormatted("Address", "0x${t.address.toString(16).uppercase()}")
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
                    printFormatted("ERASEPROG Content", t.tagData)
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

fun printFileInfo(filename: String, size: Int) {
    println("File name: $filename")
    println("File size: $size bytes")
}

fun saveToFile(data: ByteArray, filename: String) {
    try {
        FileOutputStream(filename).use { it.write(data) }
        println("Saved to $filename")
    } catch (e: Exception) {
        println("Failed to save file: ${e.message}")
        throw e
    }
}