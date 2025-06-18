# GBL-Ninja Library

Kotlin library for parsing and creating files in the GBL (Gecko Bootloader) format. This library provides a comprehensive API for working with GBL files used in Silicon Labs firmware updates.

## Features

* **Parse** GBL files into structured objects
* **Create** new GBL files from scratch
* **Modify** existing tag contents
* **Validate** file integrity with automatic CRC verification
* **Support** for all standard GBL tag types
* **Compression** support for LZ4 and LZMA
* **Security** features including ECDSA signatures and encryption
* **Container system** for advanced tag management
* **Serialization** to JSON for storage and transfer

## Installation

**Gradle (Kotlin DSL)**:
```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.VladimirOnix:gbl-ninja:v3")
}
```

## Quick Start

### Parsing a GBL File

```kotlin
val gblParser = Gbl()

val file = File("firmware.gbl")
val byteArray = file.readBytes()

val parseResult = gblParser.parseByteArray(byteArray)

when (parseResult) {
    is ParseResult.Success -> {
        val tags = parseResult.resultList
        println("Successfully parsed ${tags.size} tags")
        tags.forEach { tag ->
            println("Tag: ${tag.tagType}, Size: ${tag.content().size} bytes")
        }
    }
    is ParseResult.Fatal -> {
        println("Error parsing file: ${parseResult.error}")
    }
}
```

### Creating a New GBL File

```kotlin
val gblBuilder = Gbl.GblBuilder.create()
    .application(
        type = 32U,
        version = 0x10000U,
        capabilities = 0U,
        productId = 54U
    )
    .prog(
        flashStartAddress = 0x1000U, 
        data = firmwareData
    )

val gblBytes = gblBuilder.buildToByteArray()
File("new_firmware.gbl").writeBytes(gblBytes)
```

### Advanced: Creating GBL with Security

```kotlin
val certificate = ApplicationCertificate(
    structVersion = 1U,
    flags = 0U,
    key = 0U,
    version = 1U,
    signature = 0U
)

val gblBuilder = Gbl.GblBuilder.create()
    .application(type = 32U, version = 0x10000U)
    .certificateEcdsaP256(certificate)
    .prog(flashStartAddress = 0x1000U, data = firmwareData)
    .signatureEcdsaP256(r = 0U, s = 0U)

val secureGblBytes = gblBuilder.buildToByteArray()
```

### Modifying Existing Files

```kotlin
val parseResult = gblParser.parseByteArray(inputBytes)
if (parseResult is ParseResult.Success) {
    val tags = parseResult.resultList.toMutableList()

    // Find and modify bootloader tag
    val bootloaderTagIndex = tags.indexOfFirst { it is GblBootloader }
    if (bootloaderTagIndex != -1) {
        val bootloaderTag = tags[bootloaderTagIndex] as GblBootloader
        val modifiedTag = bootloaderTag.copy(bootloaderVersion = 0x20000U)
        tags[bootloaderTagIndex] = modifiedTag
    }

    // Encode modified tags
    val modifiedGblBytes = gblParser.encode(tags)
    File("modified_firmware.gbl").writeBytes(modifiedGblBytes)
}
```

## GBL Format Overview

The GBL format is a container format designed by Silicon Labs for firmware updates. It consists of a sequence of tags, where each tag serves a specific purpose in the update process.

### Basic Structure

Each GBL file is composed of multiple tags in sequence. Every tag has:
- **Tag ID** (4 bytes) - Identifies the tag type
- **Length field** (4 bytes) - Size of tag data
- **Data** (variable length) - Tag-specific content

The file always ends with an **END** tag containing a CRC32 checksum for integrity verification.

### File Structure Diagram

```
┌─────────────┬─────────────┬──────────────────┐
│  Tag ID     │ Data Length │    Tag Data      │
│  (4 bytes)  │  (4 bytes)  │   (variable)     │
│  uint32_le  │  uint32_le  │   byte array     │
└─────────────┴─────────────┴──────────────────┘
```

**Important**: All multi-byte values use **Little-Endian** byte order.

## Supported Tag Types

| Tag Type             | ID (Hex)   | Description                        | Data Size |
|----------------------|------------|------------------------------------|-----------|
| HEADER_V3            | 0x03A617EB | GBL file header (version 3)       | Variable  |
| BOOTLOADER           | 0xF50909F5 | Bootloader information             | Variable  |
| APPLICATION          | 0xF40A0AF4 | Application data                   | Variable  |
| METADATA             | 0xF60808F6 | File metadata                      | Variable  |
| PROG                 | 0xFE0101FE | Raw programming data               | Variable  |
| PROG_LZ4             | 0xFD0505FD | LZ4-compressed programming data    | Variable  |
| PROG_LZMA            | 0xFD0707FD | LZMA-compressed programming data   | Variable  |
| ERASEPROG            | 0xFD0303FD | Memory erase command               | 8 bytes   |
| VERSION_DEPENDENCY   | 0x76A617EB | Version requirements check         | Variable  |
| ENCRYPTION_DATA      | 0xF90707F9 | Encrypted payload data             | Variable  |
| ENCRYPTION_INIT      | 0xFA0606FA | Encryption initialization data     | Variable  |
| SIGNATURE_ECDSA_P256 | 0xF70A0AF7 | ECDSA P-256 signature for auth    | 64 bytes  |
| CERTIFICATE_ECDSA_P256| 0xF30B0BF3 | ECDSA P-256 certificate          | Variable  |
| SE_UPGRADE           | 0x5EA617EB | SE upgrade information             | Variable  |
| END                  | 0xFC0404FC | Final tag with CRC                 | 4 bytes   |

## API Reference

### Core Classes

#### Gbl

Main parser class for reading and writing GBL files.

```kotlin
class Gbl {
    fun parseByteArray(byteArray: ByteArray): ParseResult
    fun encode(tags: List<Tag>): ByteArray
}
```

#### Gbl.GblBuilder

Builder class for creating GBL files with a fluent API.

```kotlin
class GblBuilder {
    companion object {
        fun create(): GblBuilder  // Creates container with header/end tags
        fun empty(): GblBuilder   // Creates empty container
    }
    
    // Tag building methods
    fun application(type: UInt = 32U, version: UInt = 5U, capabilities: UInt = 0U, 
                   productId: UByte = 54U, additionalData: ByteArray = ByteArray(0)): GblBuilder
    fun bootloader(bootloaderVersion: UInt, address: UInt, data: ByteArray): GblBuilder
    fun prog(flashStartAddress: UInt, data: ByteArray): GblBuilder
    fun progLz4(flashStartAddress: UInt, compressedData: ByteArray, decompressedSize: UInt): GblBuilder
    fun progLzma(flashStartAddress: UInt, compressedData: ByteArray, decompressedSize: UInt): GblBuilder
    fun seUpgrade(version: UInt, data: ByteArray): GblBuilder
    fun metadata(metaData: ByteArray): GblBuilder
    fun eraseProg(): GblBuilder
    fun versionDependency(dependencyData: ByteArray): GblBuilder
    fun certificateEcdsaP256(certificate: ApplicationCertificate): GblBuilder
    fun signatureEcdsaP256(r: UByte, s: UByte): GblBuilder
    fun encryptionData(encryptedGblData: ByteArray): GblBuilder
    fun encryptionInit(msgLen: UInt, nonce: UByte): GblBuilder
    
    // Build methods
    fun buildToByteArray(): ByteArray
    fun buildToList(): List<Tag>
    fun get(): List<Tag>
    
    // Container management
    fun hasTag(tagType: GblType): Boolean
    fun getTag(tagType: GblType): Tag?
    fun removeTag(tag: Tag): ContainerResult<Unit>
    fun clear(): ContainerResult<Unit>
    fun size(): Int
    fun isEmpty(): Boolean
    fun getTagTypes(): Set<GblType>
}
```

### Data Classes

#### ApplicationData

```kotlin
data class ApplicationData(
    val type: UInt,
    val version: UInt,
    val capabilities: UInt,
    val productId: UByte
) {
    companion object {
        const val APP_TYPE: UInt = 32U
        const val APP_VERSION: UInt = 5U
        const val APP_CAPABILITIES: UInt = 0U
        const val APP_PRODUCT_ID: UByte = 54U
    }
}
```

#### ApplicationCertificate

```kotlin
data class ApplicationCertificate(
    val structVersion: UByte,
    val flags: UByte,
    val key: UByte,
    val version: UInt,
    val signature: UByte
)
```

### Result Types

#### ParseResult

```kotlin
sealed class ParseResult {
    data class Success(val resultList: List<Tag>) : ParseResult()
    data class Fatal(val error: Any? = null) : ParseResult()
}
```

#### ContainerResult

```kotlin
sealed class ContainerResult<out T> {
    data class Success<T>(val data: T) : ContainerResult<T>()
    data class Error(val message: String, val code: ContainerErrorCode) : ContainerResult<Nothing>()
}

enum class ContainerErrorCode {
    CONTAINER_NOT_CREATED,
    PROTECTED_TAG_VIOLATION,
    TAG_NOT_FOUND,
    INTERNAL_ERROR
}
```

## Container System

The library includes a sophisticated container system for managing GBL tags:

- **Automatic management** of protected tags (HEADER_V3 and END)
- **Validation** of tag integrity and relationships
- **Error handling** with detailed error codes
- **Flexible building** capabilities

### Protected Tags

The container automatically manages:
- **HEADER_V3**: Always first tag, created automatically
- **END**: Always last tag, contains calculated CRC

You cannot manually add or remove these tags - they are managed by the container system.

## Advanced Features

### Tag Management

```kotlin
val builder = Gbl.GblBuilder.create()

// Check if tag exists
if (builder.hasTag(GblType.APPLICATION)) {
    val appTag = builder.getTag(GblType.APPLICATION)
}

// Remove specific tag
val result = builder.removeTag(someTag)
when (result) {
    is ContainerResult.Success -> println("Tag removed")
    is ContainerResult.Error -> println("Error: ${result.message}")
}

// Get all tag types
val tagTypes = builder.getTagTypes()
```

## File Processing Flow

1. **Bootloader** reads the header tag to determine file format version
2. **Tags** are processed sequentially according to their type
3. **Verification** occurs (signatures, CRC checksums)
4. **Flash operations** are performed according to tag commands
5. **Device** resets after successful update completion

## Best Practices

### Error Handling

Always check parse results and handle errors appropriately:

```kotlin
when (val result = parser.parseByteArray(data)) {
    is ParseResult.Success -> {
        // Process tags
        result.resultList.forEach { tag ->
            // Handle each tag
        }
    }
    is ParseResult.Fatal -> {
        logger.error("Failed to parse GBL: ${result.error}")
        // Handle error appropriately
    }
}
```

## Limitations

* Only **GBL version 3** files are fully supported
* **Large files** are loaded entirely into memory
* **Thread safety**: Create separate parser instances for concurrent use
* **Compression**: LZ4 and LZMA require additional dependencies for decompression
* **Little-Endian**: All multi-byte values use little-endian byte order

## License

Apache License 2.0