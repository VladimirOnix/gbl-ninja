import gbl.GblParser
import gbl.tag.Tag
import gbl.tag.type.certificate.ApplicationCertificate
import java.io.File
import java.io.FileOutputStream

fun main() {
    createSimpleGbl()
    createBootloaderAndProgGbl()
    createCompressedProgramGbl()
    createEncryptedGbl()
    createSeUpgradeGbl()

    println("\nANALYZING EXISTING GBL FILES\n")
    parseExistingGbl("samples/src/main/assets/simple_gbl.gbl")
    parseExistingGbl("samples/src/main/assets/compressed_prog_gbl.gbl")
    parseExistingGbl("samples/src/main/assets/empty.gbl")
    parseExistingGbl("samples/src/main/assets/encrypted_gbl.gbl")
    println("\nANALYZING BOOTLOADER GBL FILE")
    parseExistingGbl("samples/src/main/assets/bootloader_prog_gbl.gbl")
}

fun createSimpleGbl() {
    println("\nCREATING SIMPLE GBL FILE")

    val gblBuilder = GblParser.Builder.createEmpty()
        .addApplication()
        .addProg(231U, ByteArray(1024))
        .addEraseProg()

    val gblData = gblBuilder.buildToByteArray()
    saveToFile(gblData, "samples/src/main/assets/simple_gbl.gbl")

    println("Simple GBL file created")
    println("Size: ${gblData.size} bytes")
}

fun createBootloaderAndProgGbl() {
    println("\nCREATING GBL WITH BOOTLOADER AND PROGRAM DATA =====")

    val bootloaderData = "BOOTLOADER".toByteArray()
    val programData = "PROGRAM_DATA".toByteArray()

    val gblBuilder = GblParser.Builder.createEmpty()
        .addApplication(
            type = 2U,
            version = 0x01000000U,
            capabilities = 0x00000001U
        )
        .addBootloader(
            bootloaderVersion = 0x01000000U,
            address = 0x08000000U,
            data = bootloaderData
        )
        .addProg(
            flashStartAddress = 0x08010000U,
            data = programData
        )
        .addEraseProg()
        .addMetadata("Sample metadata information".toByteArray())

    val gblData = gblBuilder.buildToList()
    val encodedGbl = GblParser().encode(gblData)
    saveToFile(encodedGbl, "samples/src/main/assets/bootloader_prog_gbl.gbl")

    println("GBL with bootloader and program data created")
    println("Number of tags: ${gblData.size}")
    println("Size: ${encodedGbl.size} bytes")
}

fun createCompressedProgramGbl() {
    println("\nCREATING GBL WITH COMPRESSED PROGRAM DATA =====")

    val lz4CompressedData = "COMPRESSED_LZ4_DATA".toByteArray()
    val lzmaCompressedData = "COMPRESSED_LZMA_DATA".toByteArray()

    val gblBuilder = GblParser.Builder.createEmpty()
        .addApplication()
        .addProgLz4(
            flashStartAddress = 0x08000000U,
            compressedData = lz4CompressedData,
            decompressedSize = 1024U
        )
        .addProgLzma(
            flashStartAddress = 0x08020000U,
            compressedData = lzmaCompressedData,
            decompressedSize = 2048U
        )

    val gblData = gblBuilder.buildToByteArray()
    saveToFile(gblData, "samples/src/main/assets/compressed_prog_gbl.gbl")

    println("GBL with compressed data created")
    println("Size: ${gblData.size} bytes")
    println("Compression used: LZ4 and LZMA")
}

fun createEncryptedGbl() {
    println("\nCREATING ENCRYPTED GBL FILE =====")

    val encryptedData = "ENCRYPTED_GBL_DATA".toByteArray()

    val gblBuilder = GblParser.Builder.createEmpty()
        .addEncryptionInit(
            msgLen = encryptedData.size.toUInt(),
            nonce = 123U.toUByte()
        )
        .addEncryptionData(encryptedData)
        .addSignatureEcdsaP256(
            r = 0xAAU.toUByte(),
            s = 0xBBU.toUByte()
        )
        .addMetadata("Sample metadata information".toByteArray())
        .addCertificateEcdsaP256(
            certificate = ApplicationCertificate(
                structVersion = 1U.toUByte(),
                flags = 0U.toUByte(),
                key = 2U.toUByte(),
                version = 0x01020304U,
                signature = 0xFFU.toUByte()
            )
        )

    val gblData = gblBuilder.buildToByteArray()
    saveToFile(gblData, "samples/src/main/assets/encrypted_gbl.gbl")

    println("Encrypted GBL file created")
    println("Size: ${gblData.size} bytes")
    println("Using ECDSA P-256 signature")
}

fun createSeUpgradeGbl() {
    println("\nCREATING GBL FOR SE UPGRADE")

    val seUpgradeData = "SE_UPGRADE_DATA".toByteArray()

    val gblBuilder = GblParser.Builder.createEmpty()
        .addApplication()
        .addSeUpgrade(
            version = 0x01000000U,
            data = seUpgradeData
        )
        .addVersionDependency("1.0.0".toByteArray())

    val gblData = gblBuilder.buildToByteArray()
    saveToFile(gblData, "se_upgrade_gbl.gbl")

    println("GBL for SE upgrade created")
    println("Size: ${gblData.size} bytes")
    println("SE version: 1.0.0.0")
}

fun parseExistingGbl(filename: String) {
    println("\nAnalyzing file: $filename")

    try {
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
    } catch (e: Exception) {
        println("Exception while parsing GBL file: ${e.message}")
        e.printStackTrace()
    }
}

fun printTagInfo(tags: List<Tag>) {
    println("Successfully parsed GBL file with ${tags.size} tags:")

    tags.forEachIndexed { index, tag ->
        println("\nTag ${index + 1}:")
        println("Type: ${tag.tagType}")

        when (tag) {
            is gbl.tag.type.GblHeader -> {
                println("GBL Version: 0x${tag.version.toString(16).padStart(8, '0')}")
                println("GBL Type: ${tag.gblType}")
            }

            is gbl.tag.type.GblBootloader -> {
                println("Bootloader Version: 0x${tag.bootloaderVersion.toString(16).padStart(8, '0')}")
                println("Address: 0x${tag.address.toString(16).padStart(8, '0')}")
                println("Data Size: ${tag.data.size} bytes")
            }

            is gbl.tag.type.GblProg -> {
                println("Flash Start Address: 0x${tag.flashStartAddress.toString(16).padStart(8, '0')}")
                println("Data Size: ${tag.data.size} bytes")
            }

            is gbl.tag.type.application.GblApplication -> {
                println("App Type: ${tag.applicationData.type}")
                println("App Version: 0x${tag.applicationData.version.toString(16).padStart(8, '0')}")
                println("App Capabilities: 0x${tag.applicationData.capabilities.toString(16).padStart(8, '0')}")
                println("Product ID: ${tag.applicationData.productId}")
            }

            is gbl.tag.type.certificate.GblCertificateEcdsaP256 -> {
                println("Certificate Version: ${tag.certificate.structVersion}")
                println("Certificate Flags: ${tag.certificate.flags}")
            }
        }
    }
}

fun saveToFile(data: ByteArray, filename: String) {
    try {
        FileOutputStream(filename).use { fos ->
            fos.write(data)
        }
        println("✓ File saved: $filename")
    } catch (e: Exception) {
        println("❌ Error saving file: ${e.message}")
    }
}