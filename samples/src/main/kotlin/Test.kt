import gbl.GblParser
import gbl.tag.Tag
import gbl.tag.type.application.ApplicationData
import gbl.tag.type.certificate.ApplicationCertificate
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.forEachIndexed

fun main() {
    createSimpleGbl()

    createBootloaderAndProgGbl()

    createCompressedProgramGbl()

    createEncryptedGbl()

    parseExistingGbl("samples/src/main/assets/simple_gbl.bin")
}

fun createSimpleGbl() {
    println("Creating Simple GBL")

    val gblBuilder = GblParser.Builder.createEmpty()
        .addApplication(
            type = 1U,
            version = 0x01020304U,
            capabilities = 0xFFFFFFFFU,
            productId = 42U
        )

    val gblData = gblBuilder.buildToByteArray()

    saveToFile(gblData, "samples/src/main/assets/simple_gbl.bin")

    println("Simple GBL file created with size: ${gblData.size} bytes")
    println()
}

fun createBootloaderAndProgGbl() {
    println("Creating GBL with Bootloader and Program Data")

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

    val gblData = gblBuilder.buildToByteArray()

    saveToFile(gblData, "samples/src/main/assets/bootloader_prog_gbl.bin")

    println("Bootloader and Program GBL file created with size: ${gblData.size} bytes")
    println()
}

fun createCompressedProgramGbl() {
    println("Creating GBL with Compressed Program Data ===")

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

    saveToFile(gblData, "samples/src/main/assets/compressed_prog_gbl.bin")

    println("Compressed Program GBL file created with size: ${gblData.size} bytes")
    println()
}

fun createEncryptedGbl() {
    println("Creating GBL with Encryption ===")

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

    saveToFile(gblData, "samples/src/main/assets/encrypted_gbl.bin")

    println("Encrypted GBL file created with size: ${gblData.size} bytes")
    println()
}

fun parseExistingGbl(filename: String) {
    println("Parsing GBL File: $filename ===")

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

    println()
}

fun printTagInfo(tags: List<Tag>) {
    println("Successfully parsed GBL file with ${tags.size} tags:")

    tags.forEachIndexed { index, tag ->
        println("Tag ${index + 1}:")
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
                println("  Certificate Version: ${tag.certificate.structVersion}")
                println("  Certificate Flags: ${tag.certificate.flags}")
            }
        }
    }
}

fun saveToFile(data: ByteArray, filename: String) {
    try {
        FileOutputStream(filename).use { fos ->
            fos.write(data)
        }
        println("File saved: $filename")
    } catch (e: Exception) {
        println("Error saving file: ${e.message}")
    }
}

fun createSeUpgradeGbl() {
    println("=== Creating GBL with SE Upgrade Data ===")

    val seUpgradeData = "SE_UPGRADE_DATA".toByteArray()

    val gblBuilder = GblParser.Builder.createEmpty()
        .addApplication()
        .addSeUpgrade(
            version = 0x01000000U,
            data = seUpgradeData
        )
        .addVersionDependency("1.0.0".toByteArray())

    val gblData = gblBuilder.buildToByteArray()

    saveToFile(gblData, "se_upgrade_gbl.bin")

    println("SE Upgrade GBL file created with size: ${gblData.size} bytes")
    println()
}

fun mainWithAllExamples() {
    createSimpleGbl()

    createBootloaderAndProgGbl()

    createCompressedProgramGbl()

    createEncryptedGbl()

    parseExistingGbl("simple_gbl.bin")

    createSeUpgradeGbl()
}