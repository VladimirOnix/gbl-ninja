# GBL-Ninja

**GBL-Ninja** is a comprehensive toolkit for working with GBL (Gecko Bootloader) files used in Silicon Labs firmware updates.

## Project Structure

This repository contains:

- **[library/](library/)** - Core Kotlin library for parsing and creating GBL files
- **[gbl-tool-cli/](gbl-tool-cli/)** - Command-line tool for GBL file manipulation

## Quick Start

### Library Usage

Add the library to your project:

**Gradle (Kotlin DSL)**:
```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.VladimirOnix:gbl-ninja:v3")
}
```

### Basic Example

```kotlin
import Gbl
import results.ParseResult

// Parse existing GBL file
val parser = Gbl()
val result = parser.parseByteArray(gblFileBytes)

when (result) {
    is ParseResult.Success -> {
        println("Parsed ${result.resultList.size} tags")
    }
    is ParseResult.Fatal -> {
        println("Parse error: ${result.error}")
    }
}

// Create new GBL file
val builder = Gbl.GblBuilder.create()
    .application(type = 32U, version = 0x10000U)
    .prog(flashStartAddress = 0x1000U, data = firmwareData)

val gblBytes = builder.buildToByteArray()
```

## Documentation

- **[Library Documentation](library/README.md)** - Kotlin library for parsing and creating GBL files
- **[CLI Tool Documentation](gbl-tool-cli/README.md)** - Command-line interface for GBL file manipulation

## About GBL Format

GBL (Gecko Bootloader) is a binary file format used for firmware updates on Silicon Labs devices. It consists of tagged data blocks that contain:

- Application code and data
- Bootloader updates
- Security certificates and signatures
- Metadata and version information

## Features

- **Parse** existing GBL files into structured data
- **Create** new GBL files from scratch
- **Modify** existing GBL files
- **Validate** file integrity with CRC checks
- **Support** for compression (LZ4, LZMA)
- **Security** features (ECDSA signatures, encryption)

## License

Apache License 2.0

---

For detailed documentation and API reference, see the [library documentation](library/README.md).