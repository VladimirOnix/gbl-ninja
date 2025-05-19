---

# 🔍 Gecko Bootloader Parser SDK

**Gecko Bootloader Parser SDK** is a Kotlin library for parsing and creating files in the GBL (Gecko Bootloader) format. It allows you to parse, analyze, modify, and generate GBL files used for firmware updates on Silicon Labs-based devices.

---

## 📝 Description

**GBL (Gecko Bootloader)** is a binary file format used for firmware updates on Silicon Labs devices. This library provides a convenient API to work with such files, enabling:

* Parsing GBL files into structured objects
* Modifying tag contents
* Creating new GBL files
* Adding and removing tags

---

## 🛠️ Installation

Add the dependency to your `build.gradle`:

```groovy
implementation(files("libs/GblCommander-1.0.jar"))
```

---

## 🚀 Usage

### Parsing a GBL File

```kotlin
import parser.GblParser

// Create a parser instance
val gblParser = GblParser()

// Read and parse the file
val byteArray = readFileAsByteArray("firmware.gbl")
val parseResult = gblParser.parse(byteArray)

// Handle the result
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

// Assume you have a list of tags (either parsed or manually created)
val tags: List<TagInterface> = ...

val gblBytes = encode(tags)

// Write to file
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

        // Modify the version
        val modifiedTag = bootloaderTag.copy(bootloaderVersion = 0x20000u)
        tags[bootloaderTagIndex] = modifiedTag
    }

    val modifiedGblBytes = encodeTagsWithEndTag(tags)
    writeByteArrayToFile(modifiedGblBytes, "modified_firmware.gbl")
}
```

---

## 📚 Supported Tag Types

| Tag Type    | Description                      |
| ----------- | -------------------------------- |
| HEADER\_V3  | GBL file header (version 3)      |
| BOOTLOADER  | Bootloader information           |
| APPLICATION | Application data                 |
| METADATA    | File metadata                    |
| PROG        | Raw programming data             |
| PROG\_LZ4   | LZ4-compressed programming data  |
| PROG\_LZMA  | LZMA-compressed programming data |
| ERASEPROG   | Memory erase command             |
| SE\_UPGRADE | SE upgrade information           |
| END         | Final tag with CRC               |

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
// Main interface for all tags
interface TagInterface {
    val tagHeader: TagHeader
    val tagType: GblType
    val tagData: ByteArray
}

// Tag header structure
data class TagHeader(
    val id: UInt,
    val length: UInt
)

// Parsing result
sealed class ParseResult {
    data class Success(val tags: List<TagInterface>) : ParseResult()
    object Fatal : ParseResult()
}
```

### Byte Utilities

```kotlin
// Read values from a byte array in little-endian order
fun getFromBytes(byteArray: ByteArray, offset: Int = 0, length: Int = 4): ByteBuffer
```

---

## ⚠️ Limitations & Notes

* The library uses **Little-Endian** byte order for reading/writing values
* Only **GBL version 3** files are supported

---

## 📋 Example: Parse and Print Tag Info

```kotlin
val parser = GblParser()
val result = parser.parseHexEncodedFile(fileBytes)

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

This library is released under the **MIT License**.

---