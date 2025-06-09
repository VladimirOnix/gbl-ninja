package gblninja.core

import GblParser
import results.ParseResult
import tag.GblType
import tag.Tag
import tag.type.*
import tag.type.application.ApplicationData
import tag.type.application.GblApplication
import tag.type.certificate.ApplicationCertificate
import tag.type.certificate.GblCertificateEcdsaP256
import tag.type.certificate.GblSignatureEcdsaP256
import tag.type.encryption.GblEncryptionData
import tag.type.encryption.GblEncryptionInitAesCcm
import java.io.File
import java.io.FileOutputStream

internal class GblFileManager {
    private val parser = GblParser()

    fun createEmptyGblFile(filename: String) {
        println("Creating empty GBL file: $filename")
        val builder = GblParser.GblBuilder.create()
        val resultBytes = builder.buildToByteArray()
        saveToFile(resultBytes, filename)
    }

    fun addTagToFile(
        inputFile: String,
        outputFile: String?,
        tagType: String?,
        insertIndex: Int,
        address: UInt? = null,
        data: ByteArray? = null,
        version: UInt? = null,
        bootloaderVersion: UInt? = null,
        compressedData: ByteArray? = null,
        decompressedSize: UInt? = null,
        msgLen: UInt? = null,
        nonce: UByte? = null,
        rValue: UByte? = null,
        sValue: UByte? = null,
        certificate: ApplicationCertificate? = null,
        dependencyData: ByteArray? = null,
        metaData: ByteArray? = null,
        appType: UInt? = null,
        appVersion: UInt? = null,
        capabilities: UInt? = null,
        productId: UByte? = null,
        tagData: ByteArray? = null,
        tagName: String? = null
    ) {
        try {
            val file = File(inputFile)
            if (!file.exists()) {
                println("Error: Input file not found: $inputFile")
                return
            }

            println("Adding tag '$tagType' to file: $inputFile at index $insertIndex")

            val parseResult = parser.parseByteArray(file.readBytes())
            val builder = GblParser.GblBuilder.create()

            when (parseResult) {
                is ParseResult.Success -> {
                    val allTags = parseResult.resultList
                    val nonSystemTags = allTags.filter { it.tagType != GblType.HEADER_V3 && it.tagType != GblType.END }

                    if (insertIndex <= 0) {
                        println("Error: Cannot insert at index $insertIndex. Index 0 is reserved for HEADER_V3")
                        println("Valid range for insertion: 1-${nonSystemTags.size + 1}")
                        return
                    }

                    if (insertIndex > nonSystemTags.size + 1) {
                        println("Error: Index $insertIndex is out of range")
                        println("Valid range for insertion: 1-${nonSystemTags.size + 1}")
                        return
                    }

                    println("Inserting new tag at index $insertIndex")

                    val insertPositionInNonSystemTags = insertIndex - 1

                    for (i in 0 until insertPositionInNonSystemTags) {
                        if (i < nonSystemTags.size) {
                            println("Adding existing tag: ${nonSystemTags[i].tagType}")
                            addExistingTagToBuilder(builder, nonSystemTags[i])
                        }
                    }

                    addTagToBuilder(builder, tagType, address, data, version, bootloaderVersion,
                        compressedData, decompressedSize, msgLen, nonce, rValue, sValue,
                        certificate, dependencyData, metaData, appType, appVersion,
                        capabilities, productId, tagData, tagName)

                    for (i in insertPositionInNonSystemTags until nonSystemTags.size) {
                        println("Adding existing tag: ${nonSystemTags[i].tagType}")
                        addExistingTagToBuilder(builder, nonSystemTags[i])
                    }
                }
                is ParseResult.Fatal -> {
                    println("Warning: Failed to parse existing file: ${parseResult.error}")
                    addTagToBuilder(builder, tagType, address, data, version, bootloaderVersion,
                        compressedData, decompressedSize, msgLen, nonce, rValue, sValue,
                        certificate, dependencyData, metaData, appType, appVersion,
                        capabilities, productId, tagData, tagName)
                }
            }

            val resultBytes = builder.buildToByteArray()
            val output = outputFile ?: inputFile

            saveToFile(resultBytes, output)

            println("Successfully added ${tagType} tag to file: $output at index $insertIndex")
            println("Output file size: ${resultBytes.size} bytes")

        } catch (e: Exception) {
            println("Error adding tag to file: ${e.message}")
            e.printStackTrace()
        }
    }

    fun setTagInFile(
        inputFile: String,
        tagType: String?,
        outputFile: String?,
        tagIndex: Int,
        address: UInt? = null,
        data: ByteArray? = null,
        version: UInt? = null,
        bootloaderVersion: UInt? = null,
        compressedData: ByteArray? = null,
        decompressedSize: UInt? = null,
        msgLen: UInt? = null,
        nonce: UByte? = null,
        rValue: UByte? = null,
        sValue: UByte? = null,
        certificate: ApplicationCertificate? = null,
        dependencyData: ByteArray? = null,
        metaData: ByteArray? = null,
        appType: UInt? = null,
        appVersion: UInt? = null,
        capabilities: UInt? = null,
        productId: UByte? = null,
        tagData: ByteArray? = null,
        tagName: String? = null
    ) {
        try {
            val file = File(inputFile)
            if (!file.exists()) {
                println("Error: Input file not found: $inputFile")
                return
            }

            println("Setting tag '$tagType' at index $tagIndex in file: $inputFile")

            val parseResult = parser.parseByteArray(file.readBytes())
            val builder = GblParser.GblBuilder.create()

            when (parseResult) {
                is ParseResult.Success -> {
                    val allTags = parseResult.resultList

                    if (tagIndex < 0 || tagIndex >= allTags.size) {
                        println("Error: Index $tagIndex is out of range. File has ${allTags.size} tags (indices 0-${allTags.size - 1})")
                        return
                    }

                    val targetTag = allTags[tagIndex]

                    if (targetTag.tagType == GblType.HEADER_V3) {
                        println("Error: Cannot modify HEADER_V3 tag at index $tagIndex")
                        return
                    }

                    if (targetTag.tagType == GblType.END) {
                        println("Error: Cannot modify END tag at index $tagIndex")
                        return
                    }

                    println("Replacing tag at index $tagIndex (${targetTag.tagType}) with $tagType")

                    allTags.forEachIndexed { index, tag ->
                        when {
                            tag.tagType == GblType.HEADER_V3 -> {
                            }
                            tag.tagType == GblType.END -> {
                            }
                            index == tagIndex -> {
                                println("Skipping tag at index $index (${tag.tagType}) for replacement")
                            }
                            else -> {
                                println("Adding existing tag at index $index: ${tag.tagType}")
                                addExistingTagToBuilder(builder, tag)
                            }
                        }
                    }

                    addTagToBuilder(builder, tagType, address, data, version, bootloaderVersion,
                        compressedData, decompressedSize, msgLen, nonce, rValue, sValue,
                        certificate, dependencyData, metaData, appType, appVersion,
                        capabilities, productId, tagData, tagName)
                }
                is ParseResult.Fatal -> {
                    println("Error: Failed to parse existing file: ${parseResult.error}")
                    return
                }
            }

            val resultBytes = builder.buildToByteArray()
            val output = outputFile ?: inputFile

            saveToFile(resultBytes, output)

            println("Successfully replaced tag at index $tagIndex with $tagType in file: $output")
            println("Output file size: ${resultBytes.size} bytes")

        } catch (e: Exception) {
            println("Error setting tag in file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun addExistingTagsToBuilderWithInsertion(
        builder: GblParser.GblBuilder,
        tags: List<Tag>,
        insertIndex: Int
    ) {
        val filteredTags = tags.filter { it.tagType != GblType.HEADER_V3 && it.tagType != GblType.END }
        println("Processing ${filteredTags.size} existing tags, inserting new tag at index $insertIndex")

        var currentIndex = 0

        while (currentIndex < insertIndex && currentIndex < filteredTags.size) {
            println("Adding existing tag at index $currentIndex: ${filteredTags[currentIndex].tagType}")
            addExistingTagToBuilder(builder, filteredTags[currentIndex])
            currentIndex++
        }

        println("Position $insertIndex reserved for new tag")

        while (currentIndex < filteredTags.size) {
            println("Adding existing tag at shifted position ${currentIndex + 1}: ${filteredTags[currentIndex].tagType}")
            addExistingTagToBuilder(builder, filteredTags[currentIndex])
            currentIndex++
        }
    }

    private fun addExistingTagsToBuilderWithReplacement(
        builder: GblParser.GblBuilder,
        tags: List<Tag>,
        replaceIndex: Int
    ) {
        val filteredTags = tags.filter { it.tagType != GblType.HEADER_V3 && it.tagType != GblType.END }
        println("Processing ${filteredTags.size} existing tags, replacing tag at index ${replaceIndex + 1}")

        filteredTags.forEachIndexed { index, tag ->
            if (index == replaceIndex) {
                println("Skipping tag at index ${index + 1} (${tag.tagType}) for replacement")
            } else {
                println("Adding existing tag at index ${index + 1}: ${tag.tagType}")
                addExistingTagToBuilder(builder, tag)
            }
        }
    }

    fun createSetTagCommand(options: Map<String, String>) {
        val inputFile = options["file"] ?: run {
            println("Error: Input file not specified (use -f or --file)")
            return
        }

        val tagType = options["type"] ?: run {
            println("Error: Tag type not specified (use --type)")
            printAvailableTagTypes()
            return
        }

        val indexStr = options["index"] ?: run {
            println("Error: Index not specified (use --index)")
            return
        }

        val tagIndex = indexStr.toIntOrNull() ?: run {
            println("Error: Invalid index '$indexStr'. Index must be a non-negative integer.")
            return
        }

        if (tagIndex < 0) {
            println("Error: Index must be non-negative")
            return
        }

        val outputFile = options["output"]
        val params = parseTagParameters(options)

        setTagInFile(
            inputFile = inputFile,
            tagType = tagType,
            outputFile = outputFile,
            tagIndex = tagIndex,
            address = params.address,
            data = params.data,
            version = params.version,
            bootloaderVersion = params.bootloaderVersion,
            compressedData = params.compressedData,
            decompressedSize = params.decompressedSize,
            msgLen = params.msgLen,
            nonce = params.nonce,
            rValue = params.rValue,
            sValue = params.sValue,
            certificate = params.certificate,
            dependencyData = params.dependencyData,
            metaData = params.metaData,
            appType = params.appType,
            appVersion = params.appVersion,
            capabilities = params.capabilities,
            productId = params.productId,
            tagData = params.tagData,
            tagName = params.tagName
        )
    }

    fun createRemoveTagCommand(options: Map<String, String>) {
        val inputFile = options["file"] ?: run {
            println("Error: Input file not specified (use -f or --file)")
            return
        }

        val indexStr = options["index"] ?: run {
            println("Error: Index not specified (use --index)")
            return
        }

        val tagIndex = indexStr.toIntOrNull() ?: run {
            println("Error: Invalid index '$indexStr'. Index must be a non-negative integer.")
            return
        }

        if (tagIndex < 0) {
            println("Error: Index must be non-negative")
            return
        }

        val outputFile = options["output"]

        removeTagFromFile(inputFile, tagIndex, outputFile)
    }

    fun removeTagFromFile(inputFile: String, tagIndex: Int, outputFile: String?) {
        try {
            val file = File(inputFile)
            if (!file.exists()) {
                println("Error: Input file not found: $inputFile")
                return
            }

            println("Removing tag at index $tagIndex from file: $inputFile")

            val parseResult = parser.parseByteArray(file.readBytes())
            val builder = GblParser.GblBuilder.create()

            when (parseResult) {
                is ParseResult.Success -> {
                    val allTags = parseResult.resultList

                    if (tagIndex < 0 || tagIndex >= allTags.size) {
                        println("Error: Index $tagIndex is out of range. File has ${allTags.size} tags (indices 0-${allTags.size - 1})")
                        return
                    }

                    val targetTag = allTags[tagIndex]

                    if (targetTag.tagType == GblType.HEADER_V3) {
                        println("Error: Cannot remove HEADER_V3 tag at index $tagIndex")
                        return
                    }

                    if (targetTag.tagType == GblType.END) {
                        println("Error: Cannot remove END tag at index $tagIndex")
                        return
                    }

                    println("Removing tag at index $tagIndex (${targetTag.tagType})")

                    allTags.forEachIndexed { index, tag ->
                        when {
                            tag.tagType == GblType.HEADER_V3 -> {
                            }
                            tag.tagType == GblType.END -> {
                            }
                            index == tagIndex -> {
                                println("Removing tag at index $index (${tag.tagType})")
                            }
                            else -> {
                                println("Keeping existing tag at index $index: ${tag.tagType}")
                                addExistingTagToBuilder(builder, tag)
                            }
                        }
                    }
                }
                is ParseResult.Fatal -> {
                    println("Error: Failed to parse file: ${parseResult.error}")
                    return
                }
            }

            val resultBytes = builder.buildToByteArray()
            val output = outputFile ?: inputFile

            saveToFile(resultBytes, output)

            println("Successfully removed tag at index $tagIndex from file: $output")
            println("Output file size: ${resultBytes.size} bytes")

        } catch (e: Exception) {
            println("Error removing tag from file: ${e.message}")
            e.printStackTrace()
        }
    }

    fun finalizeGblFile(inputFile: String, outputFile: String?) {
        try {
            val file = File(inputFile)
            if (!file.exists()) {
                println("Error: Input file not found: $inputFile")
                return
            }

            println("Finalizing GBL file: $inputFile")

            val parseResult = parser.parseByteArray(file.readBytes())
            val builder = GblParser.GblBuilder.create()

            when (parseResult) {
                is ParseResult.Success -> {
                    addExistingTagsToBuilder(builder, parseResult.resultList)
                }
                is ParseResult.Fatal -> {
                    println("Warning: Failed to parse existing file: ${parseResult.error}")
                }
            }

            val resultBytes = builder.buildToByteArray()
            val output = outputFile ?: inputFile

            saveToFile(resultBytes, output)

            println("Successfully finalized GBL file: $output")
            println("Output file size: ${resultBytes.size} bytes")

        } catch (e: Exception) {
            println("Error finalizing GBL file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun addExistingTagsToBuilder(builder: GblParser.GblBuilder, tags: List<Tag>) {
        val filteredTags = tags.filter { it.tagType != GblType.HEADER_V3 && it.tagType != GblType.END }
        println("Adding ${filteredTags.size} existing tags to builder")

        filteredTags.forEach { tag ->
            addExistingTagToBuilder(builder, tag)
        }
    }

    private fun addExistingTagsToBuilderWithRemoval(
        builder: GblParser.GblBuilder,
        tags: List<Tag>,
        removeIndex: Int
    ) {
        val filteredTags = tags.filter { it.tagType != GblType.HEADER_V3 && it.tagType != GblType.END }
        var currentIndex = 0

        filteredTags.forEach { tag ->
            if (currentIndex == removeIndex) {
                println("Removing tag at index $currentIndex (${tag.tagType})")
            } else {
                println("Adding existing tag at index $currentIndex: ${tag.tagType}")
                addExistingTagToBuilder(builder, tag)
            }
            currentIndex++
        }
    }

    private fun addExistingTagToBuilder(builder: GblParser.GblBuilder, tag: Tag) {
        try {
            when (tag) {
                is GblApplication -> {
                    builder.application(
                        tag.applicationData.type,
                        tag.applicationData.version,
                        tag.applicationData.capabilities,
                        tag.applicationData.productId,
                        tag.tagData
                    )
                }
                is GblBootloader -> {
                    builder.bootloader(tag.bootloaderVersion, tag.address, tag.data)
                }
                is GblMetadata -> {
                    println("Adding metadata: ${tag.metaData}")
                    builder.metadata(tag.metaData)
                }
                is GblProg -> {
                    builder.prog(tag.flashStartAddress, tag.data)
                }
                is GblProgLz4 -> {
                    // builder.progLz4(tag.flashStartAddress, tag.compressedData, tag.decompressedSize)
                }
                is GblProgLzma -> {
                    // builder.progLzma(tag.flashStartAddress, tag.compressedData, tag.decompressedSize)
                }
                is GblSeUpgrade -> {
                    builder.seUpgrade(tag.version, tag.data)
                }
                is GblEraseProg -> {
                    builder.eraseProg()
                }
                is GblEncryptionData -> {
                    builder.encryptionData(tag.encryptedGblData)
                }
                is GblEncryptionInitAesCcm -> {
                    builder.encryptionInit(tag.msgLen, tag.nonce)
                }
                is GblSignatureEcdsaP256 -> {
                    builder.signatureEcdsaP256(tag.r, tag.s)
                }
                is GblCertificateEcdsaP256 -> {
                    builder.certificateEcdsaP256(tag.certificate)
                }
                else -> {
                    if (tag.tagType == GblType.VERSION_DEPENDENCY) {
                        builder.versionDependency(tag.content())
                    }
                }
            }
        } catch (e: Exception) {
            println("Warning: Failed to add existing tag ${tag.tagType}: ${e.message}")
        }
    }

    private fun addTagToBuilder(
        builder: GblParser.GblBuilder,
        tagType: String?,
        address: UInt?,
        data: ByteArray?,
        version: UInt?,
        bootloaderVersion: UInt?,
        compressedData: ByteArray?,
        decompressedSize: UInt?,
        msgLen: UInt?,
        nonce: UByte?,
        rValue: UByte?,
        sValue: UByte?,
        certificate: ApplicationCertificate?,
        dependencyData: ByteArray?,
        metaData: ByteArray?,
        appType: UInt?,
        appVersion: UInt?,
        capabilities: UInt?,
        productId: UByte?,
        tagData: ByteArray?,
        tagName: String?
    ) {
        println("Adding tag: $tagType")

        when (tagType?.lowercase()?.replace("-", "_")) {
            "bootloader", "boot" -> {
                val bootVer = bootloaderVersion ?: 1u
                val addr = address ?: 0x08000000u
                val bootData = data ?: ByteArray(0)
                println("  Bootloader: version=$bootVer, address=0x${addr.toString(16)}, data size=${bootData.size}")
                builder.bootloader(bootVer, addr, bootData)
            }

            "metadata", "meta" -> {
                val metaDataBytes = metaData ?: data ?: ByteArray(0)
                println("  Metadata: '${String(metaDataBytes, Charsets.UTF_8)}' (${metaDataBytes.size} bytes)")
                builder.metadata(metaDataBytes)
            }

            "prog", "program" -> {
                val flashAddr = address ?: 0x08000000u
                val progData = data ?: ByteArray(0)
                println("  Program: address=0x${flashAddr.toString(16)}, data size=${progData.size}")
                builder.prog(flashAddr, progData)
            }

            "prog_lz4", "proglz4", "lz4" -> {
                val flashAddr = address ?: 0x08000000u
                val compressed = compressedData ?: data ?: ByteArray(0)
                val decompSize = decompressedSize ?: compressed.size.toUInt()
                println("  Program LZ4: address=0x${flashAddr.toString(16)}, compressed=${compressed.size}, decompressed=$decompSize")
                builder.progLz4(flashAddr, compressed, decompSize)
            }

            "prog_lzma", "proglzma", "lzma" -> {
                val flashAddr = address ?: 0x08000000u
                val compressed = compressedData ?: data ?: ByteArray(0)
                val decompSize = decompressedSize ?: compressed.size.toUInt()
                println("  Program LZMA: address=0x${flashAddr.toString(16)}, compressed=${compressed.size}, decompressed=$decompSize")
                builder.progLzma(flashAddr, compressed, decompSize)
            }

            "se_upgrade", "seupgrade", "se" -> {
                val ver = version ?: 1u
                val seData = data ?: ByteArray(0)
                println("  SE Upgrade: version=$ver, data size=${seData.size}")
                builder.seUpgrade(ver, seData)
            }

            "application", "app" -> {
                val type = appType ?: ApplicationData.APP_TYPE
                val ver = appVersion ?: ApplicationData.APP_VERSION
                val caps = capabilities ?: ApplicationData.APP_CAPABILITIES
                val prodId = productId ?: ApplicationData.APP_PRODUCT_ID
                val appData = tagData ?: data ?: ByteArray(0)
                println("  Application: type=$type, version=$ver, capabilities=$caps, productId=$prodId, data size=${appData.size}")
                builder.application(type, ver, caps, prodId, appData)
            }

            "eraseprog", "erase" -> {
                println("  Erase Program")
                builder.eraseProg()
            }

            "encryption_data", "encryptiondata", "encrypt" -> {
                val encData = data ?: ByteArray(32)
                println("  Encryption Data: size=${encData.size}")
                builder.encryptionData(encData)
            }

            "encryption_init", "encryptioninit", "encryptinit" -> {
                val msgLength = msgLen ?: 256u
                val nonceValue = nonce ?: 0u.toUByte()
                println("  Encryption Init: msgLen=$msgLength, nonce=$nonceValue")
                builder.encryptionInit(msgLength, nonceValue)
            }

            "signature_ecdsa_p256", "signature", "ecdsa", "sign" -> {
                val r = rValue ?: 0u.toUByte()
                val s = sValue ?: 0u.toUByte()
                println("  Signature ECDSA P256: r=$r, s=$s")
                builder.signatureEcdsaP256(r, s)
            }

            "certificate_ecdsa_p256", "certificate", "cert" -> {
                if (certificate != null) {
                    println("  Certificate ECDSA P256")
                    builder.certificateEcdsaP256(certificate)
                } else {
                    println("Warning: Certificate object is required for certificate tag")
                }
            }

            "version_dependency", "versiondep", "version" -> {
                val depData = dependencyData ?: data ?: ByteArray(4)
                println("  Version Dependency: data size=${depData.size}")
                builder.versionDependency(depData)
            }

            "tag" -> {
                val name = tagName ?: "custom_tag"
                println("  Custom Tag: name=$name")
                builder.tag(name)
            }

            else -> {
                println("Error: Unknown tag type '$tagType'")
                printAvailableTagTypes()
            }
        }
    }

    private fun parseMetadataParameter(value: String?): ByteArray? {
        if (value == null) return null

        return when {
            File(value).exists() -> {
                try {
                    File(value).readBytes()
                } catch (e: Exception) {
                    println("Warning: Failed to read metadata file '$value': ${e.message}")
                    value.toByteArray(Charsets.UTF_8) // Fallback to text
                }
            }
            else -> value.toByteArray(Charsets.UTF_8)
        }
    }

    private fun parseDataParameter(value: String?): ByteArray? {
        if (value == null) return null

        return when {
            value.startsWith("0x", ignoreCase = true) -> {
                try {
                    val hexString = value.removePrefix("0x").removePrefix("0X")
                    hexStringToByteArray(hexString)
                } catch (e: Exception) {
                    println("Warning: Invalid hex string '$value', treating as text")
                    value.toByteArray(Charsets.UTF_8)
                }
            }
            File(value).exists() -> {
                try {
                    File(value).readBytes()
                } catch (e: Exception) {
                    println("Warning: Failed to read file '$value': ${e.message}")
                    null
                }
            }
            else -> value.toByteArray(Charsets.UTF_8)
        }
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        val cleanHex = hex.replace("\\s".toRegex(), "")
        require(cleanHex.length % 2 == 0) { "Hex string must have even length" }

        return cleanHex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun createAddTagCommand(options: Map<String, String>) {
        val inputFile = options["file"] ?: run {
            println("Error: Input file not specified (use -f or --file)")
            return
        }

        val tagType = options["type"] ?: run {
            println("Error: Tag type not specified (use --type)")
            printAvailableTagTypes()
            return
        }

        val indexStr = options["index"] ?: run {
            println("Error: Index not specified (use --index)")
            return
        }

        val insertIndex = indexStr.toIntOrNull() ?: run {
            println("Error: Invalid index '$indexStr'. Index must be a non-negative integer.")
            return
        }

        if (insertIndex < 0) {
            println("Error: Index must be non-negative")
            return
        }

        val outputFile = options["output"]
        val params = parseTagParameters(options)

        addTagToFile(
            inputFile = inputFile,
            outputFile = outputFile,
            tagType = tagType,
            insertIndex = insertIndex,
            address = params.address,
            data = params.data,
            version = params.version,
            bootloaderVersion = params.bootloaderVersion,
            compressedData = params.compressedData,
            decompressedSize = params.decompressedSize,
            msgLen = params.msgLen,
            nonce = params.nonce,
            rValue = params.rValue,
            sValue = params.sValue,
            certificate = params.certificate,
            dependencyData = params.dependencyData,
            metaData = params.metaData,
            appType = params.appType,
            appVersion = params.appVersion,
            capabilities = params.capabilities,
            productId = params.productId,
            tagData = params.tagData,
            tagName = params.tagName
        )
    }

    private fun parseTagParameters(options: Map<String, String>): TagParameters {
        return TagParameters(
            address = options["address"]?.removePrefix("0x")?.toUIntOrNull(16),
            data = parseDataParameter(options["data"]),
            version = options["version"]?.toUIntOrNull(),
            bootloaderVersion = options["bootloader-version"]?.toUIntOrNull(),
            compressedData = parseDataParameter(options["compressed-data"]),
            decompressedSize = options["decompressed-size"]?.toUIntOrNull(),
            msgLen = options["msg-len"]?.toUIntOrNull(),
            nonce = options["nonce"]?.removePrefix("0x")?.toUByteOrNull(16),
            rValue = options["r-value"]?.removePrefix("0x")?.toUByteOrNull(16),
            sValue = options["s-value"]?.removePrefix("0x")?.toUByteOrNull(16),
            certificate = null,
            dependencyData = parseDataParameter(options["dependency-data"]),
            metaData = parseMetadataParameter(options["metadata"]),
            appType = options["app-type"]?.toUIntOrNull(),
            appVersion = options["app-version"]?.toUIntOrNull(),
            capabilities = options["capabilities"]?.toUIntOrNull(),
            productId = options["product-id"]?.toUByteOrNull(), // Исправлено: убрали дефис из ключа
            tagData = parseDataParameter(options["tag-data"]),
            tagName = options["tag-name"]
        )
    }

    fun printAvailableTagTypes() {
        println("Available tag types:")
        println()
        println("  bootloader       --address <hex> --data <file> --version <N>")
        println("  metadata         --metadata <text|file> OR --data <file|hex>")
        println("  prog             --address <hex> --data <file>")
        println("  prog_lz4         --address <hex> --data <file> --decompressed-size <N>")
        println("  prog_lzma        --address <hex> --data <file> --decompressed-size <N>")
        println("  se_upgrade       --version <N> --data <file>")
        println("  application      [--app-type <N>] [--app-version <N>] [--capabilities <N>] [--product-id <N>] [--data <file>]")
        println("  eraseprog        (no parameters)")
        println("  encryption_data  --data <file|hex>")
        println("  encryption_init  --msg-len <N> --nonce <hex>")
        println("  signature        --r-value <hex> --s-value <hex>")
        println("  certificate      (advanced usage)")
        println("  version_dependency --dependency <version> OR --data <file|hex>")
        println()
        println("Examples:")
        println("  gblninja --add -f input.gbl --index 1 --type prog --address '0x08000000' --data program.bin")
        println("  gblninja --set -f input.gbl --index 0 --type metadata --metadata 'Version 1.0'")
        println("  gblninja --remove -f input.gbl --index 2")
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

    private data class TagParameters(
        val address: UInt?,
        val data: ByteArray?,
        val version: UInt?,
        val bootloaderVersion: UInt?,
        val compressedData: ByteArray?,
        val decompressedSize: UInt?,
        val msgLen: UInt?,
        val nonce: UByte?,
        val rValue: UByte?,
        val sValue: UByte?,
        val certificate: ApplicationCertificate?,
        val dependencyData: ByteArray?,
        val metaData: ByteArray?,
        val appType: UInt?,
        val appVersion: UInt?,
        val capabilities: UInt?,
        val productId: UByte?,
        val tagData: ByteArray?,
        val tagName: String?
    )
}