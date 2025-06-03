# 🔍 Gecko Bootloader Parser SDK

**Gecko Bootloader Ninja SDK** is a Kotlin library for parsing and creating files in the GBL (Gecko Bootloader) format. It allows you to parse, analyze, modify, and generate GBL files used for firmware updates on Silicon Labs-based devices.

## 📝 Description

**GBL (Gecko Bootloader)** is a binary file format used for firmware updates on Silicon Labs devices. This library provides a convenient API to work with such files, enabling:

* Parsing GBL files into structured objects
* Modifying tag contents
* Creating new GBL files
* Adding and removing tags

---

## 📘 GBL Format Overview
The GBL format is a container format designed by Silicon Labs for firmware updates. It consists of a sequence of tags, where each tag serves a specific purpose in the update process.

### Basic Structure

Each GBL file is composed of multiple tags in sequence
Every tag has a Tag ID (4 bytes), Length field (4 bytes), and Data (variable length)
The file always ends with an END tag containing a CRC checksum

### Key Features

* **Modular design**: Different tag types for different purposes
* **Compression support**: LZ4 and LZMA compression options
* **Security**: Signature verification and encryption capabilities
* **Versioning**: Version dependency checks for safe updates
* **Multiple update types**: Application, bootloader, and secure element updates

### File Processing Flow

1. Bootloader reads the header tag
2. Tags are processed sequentially
3. Verification occurs (signatures, CRC)
4. Flash operations are performed according to tag commands
5. Device resets after successful update

---

## 🛠️ Installation

**For gradle.kts**:
- Add this inside *repositories* block:
  ```kotlin
  repositories {
      maven("https://jitpack.io")
  }
  ```
- Add dependency:
  ```kotlin
  dependencies {
      implementation("com.github.VladimirOnix:gbl-ninja:v3")
  }
  ```

**For gradle (Groovy)**:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.VladimirOnix:gbl-ninja:v3'
}
```

---

## 🚀 Usage

### Parsing a GBL File

```kotlin
val gblParser = GblParser()

val file = File("firmware.gbl")
val byteArray = file.readBytes()

val byteArray = this::class.java.getResourceAsStream("/firmware.gbl")?.readBytes()
    ?: throw FileNotFoundException("firmware.gbl not found")

val parseResult = gblParser.parse(byteArray)

when (parseResult) {
    is ParseResult.Success -> {
        val tags = parseResult.tags
        println("Successfully parsed ${tags.size} tags")
        tags.forEach { tag ->
            println("Tag: ${tag.tagType}, Size: ${tag.tagData.size} bytes")
        }
    }
    is ParseResult.Fatal -> {
        println("Error parsing file: ${parseResult.error}")
    }
}
```

### Creating a New GBL File

```kotlin
val gblBuilder = GblParser.Builder.createEmpty()
    .addHeader(version = 3U, gblType = GblFileType.APPLICATION)
    .addApplication(
        applicationData = ApplicationData(
            type = 1U,
            version = 0x10000U,
            capabilities = 0U,
            productId = ByteArray(16) { 0 }
        )
    )
    .addProg(address = 0x1000U, data = firmwareData)
    .addEnd()

val gblBytes = gblBuilder.buildToByteArray()

File("new_firmware.gbl").writeBytes(gblBytes)
```

### Advanced: Creating GBL with Certificate

```kotlin
val certificate = ApplicationCertificate(
    structVersion = 1U,
    flags = 0U,
    key = loadEcdsaP256PublicKey(), 
    version = 1U,
    signature = ByteArray(64) 
)

val gblBuilder = GblParser.Builder.createEmpty()
    .addHeader(version = 3U, gblType = GblFileType.APPLICATION)
    .addApplication(applicationData)
    .addCertificateEcdsaP256(certificate)
    .addProg(address = 0x1000U, data = firmwareData)
    .addSignatureEcdsaP256(signatureData) 
    .addEnd()

val secureGblBytes = gblBuilder.buildToByteArray()
```

### Modifying an Existing GBL File

```kotlin
val parseResult = gblParser.parse(inputBytes)
if (parseResult is ParseResult.Success) {
    val tags = parseResult.tags.toMutableList()

    val bootloaderTagIndex = tags.indexOfFirst { it is GblBootloader }
    if (bootloaderTagIndex != -1) {
        val bootloaderTag = tags[bootloaderTagIndex] as GblBootloader
        val modifiedTag = bootloaderTag.copy(bootloaderVersion = 0x20000U)
        tags[bootloaderTagIndex] = modifiedTag
    }

    val builder = GblParser.Builder.createEmpty()
    tags.forEach { tag ->
        when (tag) {
            is GblHeader -> builder.addHeader(tag.version, tag.gblType)
            is GblBootloader -> builder.addBootloader(tag.bootloaderVersion, tag.address, tag.tagData)
            is GblApplication -> builder.addApplication(tag.applicationData)
            is GblProg -> builder.addProg(tag.address, tag.tagData)
        }
    }
    
    val modifiedGblBytes = builder.buildToByteArray()
    File("modified_firmware.gbl").writeBytes(modifiedGblBytes)
}
```

---

## 📚 Supported Tag Types

| Tag Type             | ID (Hex) | Description                      | Data Size       |
|----------------------|----------|----------------------------------|-----------------|
| HEADER_V3            | 0x03A617EB | GBL file header (version 3)    | Variable        |
| BOOTLOADER           | 0xF40A0AF4 | Bootloader information         | Variable        |
| APPLICATION          | 0xF10A0AF1 | Application data               | Variable        |
| METADATA             | 0xF60A0AF6 | File metadata                  | Variable        |
| PROG                 | 0x01 | Raw programming data               | Variable        |
| PROG_LZ4             | 0x02 | LZ4-compressed programming data    | Variable        |
| PROG_LZMA            | 0x03 | LZMA-compressed programming data   | Variable        |
| ERASEPROG            | 0x04 | Memory erase command               | 8 bytes         |
| VERSION_DEPENDENCY   | 0x05 | Version requirements check         | Variable        |
| ENCRYPTION_DATA      | 0x06 | Encrypted payload data             | Variable        |
| ENCRYPTION_INIT      | 0x07 | Encryption initialization data     | Variable        |
| SIGNATURE_ECDSA_P256 | 0x08 | ECDSA P-256 signature for auth    | 64 bytes        |
| CERTIFICATE_ECDSA_P256| 0x09 | ECDSA P-256 certificate          | Variable        |
| SE_UPGRADE           | 0x0A | SE upgrade information             | Variable        |
| END                  | 0xFC0404FC | Final tag with CRC            | 4 bytes         |

---

## 🔍 GBL File Structure

Each GBL file consists of a sequence of tags, where each tag has the following structure:

```
┌─────────────┬─────────────┬──────────────────┐
│  Tag ID     │ Data Length │    Tag Data      │
│  (4 bytes)  │  (4 bytes)  │   (variable)     │
│  uint32_le  │  uint32_le  │   byte array     │
└─────────────┴─────────────┴──────────────────┘
```

**Important**: All multi-byte values use **Little-Endian** byte order.

The final tag in the file must always be an **END** tag, which contains a CRC32 for verifying file integrity.

---

## 🧩 API Reference

### Core Interfaces and Classes

```kotlin
interface Tag {
    val tagHeader: TagHeader
    val tagType: GblType
    val tagData: ByteArray
}

data class TagHeader(
    val id: UInt,
    val length: UInt
)

sealed class ParseResult {
    data class Success(val tags: List<Tag>) : ParseResult()
    data class Fatal(val error: String) : ParseResult()
}

sealed class ParseTagResult {
    data class Success(
        val tagHeader: TagHeader,
        val tagData: ByteArray,
    ) : ParseTagResult()

    data class Fatal(val error: Any? = null) : ParseTagResult()
}
```

### Builder Pattern

```kotlin
class GblParser {
    class Builder {
        companion object {
            fun createEmpty(): Builder
        }
        
        fun addHeader(version: UInt, gblType: GblFileType): Builder
        fun addBootloader(version: UInt, address: UInt, data: ByteArray): Builder
        fun addApplication(applicationData: ApplicationData): Builder
        fun addProg(address: UInt, data: ByteArray): Builder
        fun addProgLz4(address: UInt, compressedData: ByteArray): Builder
        fun addProgLzma(address: UInt, compressedData: ByteArray): Builder
        fun addEraseProg(address: UInt, length: UInt): Builder
        fun addCertificateEcdsaP256(certificate: ApplicationCertificate): Builder
        fun addSignatureEcdsaP256(signature: ByteArray): Builder
        fun addEnd(): Builder
        
        fun buildToByteArray(): ByteArray
    }
}
```

---

## ⚠️ Limitations & Notes

* The library uses **Little-Endian** byte order for reading/writing values
* Only **GBL version 3** files are fully supported
* **CRC verification** is performed automatically during parsing
* **Memory usage**: Large firmware files are loaded entirely into memory
* **Thread safety**: Parser instances are not thread-safe, create separate instances for concurrent use
* **Compression**: LZ4 and LZMA decompression requires additional dependencies

---

## 📜 License

This library is released under the **Apache License 2.0**.

---