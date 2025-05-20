
# 🔍 Gecko Bootloader Parser SDK

**Gecko Bootloader Parser SDK** is a Kotlin library for parsing and creating files in the GBL (Gecko Bootloader) format. It allows you to parse, analyze, modify, and generate GBL files used for firmware updates on Silicon Labs-based devices.

## 📝 Description

**GBL (Gecko Bootloader)** is a binary file format used for firmware updates on Silicon Labs devices. This library provides a convenient API to work with such files, enabling:

* Parsing GBL files into structured objects
* Modifying tag contents
* Creating new GBL files
* Adding and removing tags

---

## 📘 GBL Format Overview
The GBL format is a container format designed by Silicon Labs for firmware updates. It consists of a sequence of tags, where each tag serves a specific purpose in the update process.
* Basic Structure

Each GBL file is composed of multiple tags in sequence
Every tag has a Tag ID (4 bytes), Length field (4 bytes), and Data (variable length)
The file always ends with an END tag containing a CRC checksum

* Key Features

Modular design: Different tag types for different purposes
Compression support: LZ4 and LZMA compression options
Security: Signature verification and encryption capabilities
Versioning: Version dependency checks for safe updates
Multiple update types: Application, bootloader, and secure element updates

* File Processing Flow

Bootloader reads the header tag
Tags are processed sequentially
Verification occurs (signatures, CRC)
Flash operations are performed according to tag commands
Device resets after successful update

---

## 🛠️ Installation

**for gradle.kts**:
- add this inside *repositories* block:
    - maven("https://jitpack.io")
- implementation("com.github.VladimirOnix:gbl-ninja:v3")

---

## 🚀 Usage

### Parsing a GBL File

```kotlin
import parser.GblParser

val gblParser = GblParser()

val byteArray = readFileAsByteArray("firmware.gbl")
val parseResult = gblParser.parse(byteArray)

when (parseResult) {
    is ParseResult.Success -> {
        val tags = parseResult.tags
        tags.forEach { tag ->
            println(tag)
        }
    }
    is ParseResult.Fatal -> {
        println("Error parsing file")
    }
}
```

### Creating a New GBL File

```kotlin
import parser.data.encode.encodeTags
import parser.data.encode.encodeTagsWithEndTag
import parser.data.tag.TagInterface

val tags: List<Tag> = ...

val gblBytes = encode(tags)

writeByteArrayToFile(gblBytesWithEnd, "new_firmware.gbl")
```

### Modifying an Existing GBL File

```kotlin
val parseResult = gblParser.parseHexEncodedFile(inputBytes)
if (parseResult is ParseResult.Success) {
    val tags = parseResult.tags.toMutableList()

    val bootloaderTagIndex = tags.indexOfFirst { it is GblBootloader }
    if (bootloaderTagIndex != -1) {
        val bootloaderTag = tags[bootloaderTagIndex] as GblBootloader

        val modifiedTag = bootloaderTag.copy(bootloaderVersion = 0x20000u)
        tags[bootloaderTagIndex] = modifiedTag
    }

    val modifiedGblBytes = encodeTagsWithEndTag(tags)
    writeByteArrayToFile(modifiedGblBytes, "modified_firmware.gbl")
}
```

---

## 📚 Supported Tag Types

| Tag Type             | Description                      |
|----------------------|----------------------------------|
| HEADER_V3            | GBL file header (version 3)      |
| BOOTLOADER           | Bootloader information           |
| APPLICATION          | Application data                 |
| METADATA             | File metadata                    |
| PROG                 | Raw programming data             |
| PROG_LZ4             | LZ4-compressed programming data  |
| PROG_LZMA            | LZMA-compressed programming data |
| ERASEPROG            | Memory erase command             |
| VERSION_DEPENDENCY   | Version requirements check       |
| ENCRYPTION_DATA      | Encrypted payload data           |
| ENCRYPTION_INIT      | Encryption initialization data   |
| SIGNATURE_ECDSA_P256 | ECDSA P-256 signature for auth   |
| SE_UPGRADE           | SE upgrade information           |
| END                  | Final tag with CRC               |


---

## 🔍 GBL File Structure

Each GBL file consists of a sequence of tags, where each tag has the following structure:

* **Tag ID** (4 bytes, `uint32`)
* **Data Length** (4 bytes, `uint32`)
* **Tag Data** (byte array of specified length)

The final tag in the file must always be an **END** tag, which contains a CRC for verifying file integrity.

---

## 🧩 API & Interfaces

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

sealed class ParseTagResult {
    data class Success(
        val tagHeader: TagHeader,
        val tagData: ByteArray,
    ) : ParseTagResult()

    data class Fatal(val error: Any? = null) : ParseTagResult()
}
```

## ⚠️ Limitations & Notes

* The library uses **Little-Endian** byte order for reading/writing values
* Only **GBL version 3** files are supported

---

## 📋 Example: Parse and Print Tag Info

```kotlin
val parser = GblParser()
val result = parser.parse(fileBytes)

if (result is ParseResult.Success) {
    result.tags.forEach { tag ->
        when (tag) {
            is GblHeader -> println("GBL Header: version ${tag.version}, type ${tag.gblType}")
            is GblBootloader -> println("Bootloader: version ${tag.bootloaderVersion}, address ${tag.address.toString(16)}")
            is GblApplication -> println("Application: type ${tag.applicationData.type}, version ${tag.applicationData.version}")
            is GblEnd -> println("END tag: CRC = ${tag.gblCrc.toString(16)}")
            else -> println("Other tag: ${tag.tagType}")
        }
    }
}
```

---

## 📜 License

This library is released under the **Apache License**.

---