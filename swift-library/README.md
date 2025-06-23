# GBL-Ninja Swift Library

Swift library for parsing and creating files in the GBL (Gecko Bootloader) format. This library provides a comprehensive API for working with GBL files used in Silicon Labs firmware updates.

## Features

* **Parse** GBL files into structured objects
* **Create** new GBL files from scratch
* **Modify** existing tag contents
* **Validate** file integrity with automatic CRC verification
* **Support** for all standard GBL tag types
* **Compression** support for LZ4 and LZMA
* **Security** features including ECDSA signatures and encryption
* **Container system** for advanced tag management
* **Type-safe** Swift implementation with proper error handling

## Installation

### Swift Package Manager

Add the following to your `Package.swift` file:

```swift
dependencies: [
    .package(url: "https://github.com/VladimirOnix/gbl-ninja/tree/main/swift-library", from: "1.0.0")
]
```

### CocoaPods

```ruby
pod 'GBLNinja', '~> 1.0'
```

## Quick Start

### Parsing a GBL File

```swift
import Foundation
import GBLNinja

let gblParser = Gbl()

guard let fileData = try? Data(contentsOf: URL(fileURLWithPath: "firmware.gbl")) else {
    print("Failed to load file")
    return
}

let parseResult = gblParser.parseByteArray(fileData)

switch parseResult {
case .success(let tags):
    print("Successfully parsed \(tags.count) tags")
    tags.forEach { tag in
        print("Tag: \(tag.tagType), Size: \(tag.content().count) bytes")
    }
case .fatal(let error):
    print("Error parsing file: \(error?.localizedDescription ?? "Unknown error")")
}
```

### Creating a New GBL File

```swift
let gblBuilder = Gbl.GblBuilder.create()
    .application(
        type: 32,
        version: 0x10000,
        capabilities: 0,
        productId: 54
    )
    .prog(
        flashStartAddress: 0x1000,
        data: firmwareData
    )

let gblData = gblBuilder.buildToByteArray()
try gblData.write(to: URL(fileURLWithPath: "new_firmware.gbl"))
```

### Advanced: Creating GBL with Security

```swift
let certificate = ApplicationCertificate(
    structVersion: 1,
    flags: 0,
    key: 0,
    version: 1,
    signature: 0
)

let gblBuilder = Gbl.GblBuilder.create()
    .application(type: 32, version: 0x10000)
    .certificateEcdsaP256(certificate)
    .prog(flashStartAddress: 0x1000, data: firmwareData)
    .signatureEcdsaP256(r: 0, s: 0)

let secureGblData = gblBuilder.buildToByteArray()
```

### Modifying Existing Files

```swift
let parseResult = gblParser.parseByteArray(inputData)
if case .success(var tags) = parseResult {
    // Find and modify bootloader tag
    if let bootloaderIndex = tags.firstIndex(where: { $0 is GblBootloader }),
       let bootloaderTag = tags[bootloaderIndex] as? GblBootloader {
        
        let modifiedTag = GblBootloader(
            tagHeader: bootloaderTag.tagHeader,
            tagType: bootloaderTag.tagType,
            bootloaderVersion: 0x20000, // New version
            address: bootloaderTag.address,
            data: bootloaderTag.data,
            tagData: bootloaderTag.tagData
        )
        tags[bootloaderIndex] = modifiedTag
    }

    // Encode modified tags
    let modifiedGblData = gblParser.encode(tags)
    try modifiedGblData.write(to: URL(fileURLWithPath: "modified_firmware.gbl"))
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

```swift
class Gbl {
    func parseByteArray(_ byteArray: Data) -> ParseResult
    func encode(_ tags: [Tag]) -> Data
}
```

#### Gbl.GblBuilder

Builder class for creating GBL files with a fluent API.

```swift
class GblBuilder {
    static func create() -> GblBuilder  // Creates container with header/end tags
    static func empty() -> GblBuilder   // Creates empty container
    
    // Tag building methods
    func application(type: UInt32, version: UInt32, capabilities: UInt32,
                   productId: UInt8, additionalData: Data) -> GblBuilder
    func bootloader(bootloaderVersion: UInt32, address: UInt32, data: Data) -> GblBuilder
    func prog(flashStartAddress: UInt32, data: Data) -> GblBuilder
    func progLz4(flashStartAddress: UInt32, compressedData: Data, decompressedSize: UInt32) -> GblBuilder
    func progLzma(flashStartAddress: UInt32, compressedData: Data, decompressedSize: UInt32) -> GblBuilder
    func seUpgrade(version: UInt32, data: Data) -> GblBuilder
    func metadata(_ metaData: Data) -> GblBuilder
    func eraseProg() -> GblBuilder
    func versionDependency(_ dependencyData: Data) -> GblBuilder
    func certificateEcdsaP256(_ certificate: ApplicationCertificate) -> GblBuilder
    func signatureEcdsaP256(r: UInt8, s: UInt8) -> GblBuilder
    func encryptionData(_ encryptedGblData: Data) -> GblBuilder
    func encryptionInit(msgLen: UInt32, nonce: UInt8) -> GblBuilder
    
    // Build methods
    func buildToByteArray() -> Data
    func buildToList() -> [Tag]
    func get() -> [Tag]
    
    // Container management
    func hasTag(_ tagType: GblType) -> Bool
    func getTag(_ tagType: GblType) -> Tag?
    func removeTag(_ tag: Tag) -> ContainerResult<Void>
    func clear() -> ContainerResult<Void>
    func size() -> Int
    func isEmpty() -> Bool
    func getTagTypes() -> Set<GblType>
}
```

### Data Structures

#### ApplicationData

```swift
struct ApplicationData {
    let type: UInt32
    let version: UInt32
    let capabilities: UInt32
    let productId: UInt8
    
    static let appType: UInt32 = 32
    static let appVersion: UInt32 = 5
    static let appCapabilities: UInt32 = 0
    static let appProductId: UInt8 = 54
}
```

#### ApplicationCertificate

```swift
struct ApplicationCertificate {
    let structVersion: UInt8
    let flags: UInt8
    let key: UInt8
    let version: UInt32
    let signature: UInt8
}
```

### Result Types

#### ParseResult

```swift
enum ParseResult {
    case success([Tag])
    case fatal(Error?)
}
```

#### ContainerResult

```swift
enum ContainerResult<T> {
    case success(T)
    case error(String, ContainerErrorCode)
}

enum ContainerErrorCode {
    case containerNotCreated
    case protectedTagViolation
    case tagNotFound
    case internalError
}
```

## Key Differences from Kotlin Version

### 1. Data Types
- **Kotlin** `UInt`, `UByte` → **Swift** `UInt32`, `UInt8`
- **Kotlin** `ByteArray` → **Swift** `Data`

### 2. Error Handling
- **Kotlin** exceptions → **Swift** `Error` protocol with `throws`
- **Kotlin** sealed classes → **Swift** enums with associated values

### 3. Memory Management
- **Kotlin** garbage collection → **Swift** ARC (Automatic Reference Counting)
- Swift uses value types (structs) for data structures, improving performance

### 4. Null Safety
- **Kotlin** nullable types (`?`) → **Swift** optionals (`?`)
- Similar syntax but different underlying implementation

### 5. Collections
- **Kotlin** `MutableList` → **Swift** `Array`
- **Kotlin** `MutableSet` → **Swift** `Set`

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

```swift
let builder = Gbl.GblBuilder.create()

// Check if tag exists
if builder.hasTag(.application) {
    let appTag = builder.getTag(.application)
}

// Remove specific tag
let result = builder.removeTag(someTag)
switch result {
case .success:
    print("Tag removed")
case .error(let message, _):
    print("Error: \(message)")
}

// Get all tag types
let tagTypes = builder.getTagTypes()
```

## Error Handling Best Practices

Always check parse results and handle errors appropriately:

```swift
switch parser.parseByteArray(data) {
case .success(let tags):
    // Process tags
    tags.forEach { tag in
        // Handle each tag
    }
case .fatal(let error):
    logger.error("Failed to parse GBL: \(error?.localizedDescription ?? "Unknown error")")
    // Handle error appropriately
}
```

## Testing the Converted Code

### 1. Unit Tests

Create comprehensive unit tests to verify functionality:

```swift
import XCTest
@testable import GBLNinja

class GblTests: XCTestCase {
    func testGblParsing() {
        let gbl = Gbl()
        let testData = createTestGblData()
        
        let result = gbl.parseByteArray(testData)
        
        switch result {
        case .success(let tags):
            XCTAssertFalse(tags.isEmpty)
            XCTAssertTrue(tags.contains { $0 is GblHeader })
        case .fatal:
            XCTFail("Parsing should succeed")
        }
    }
    
    func testGblBuilding() {
        let builder = Gbl.GblBuilder.create()
            .application(type: 32, version: 0x10000)
            .prog(flashStartAddress: 0x1000, data: Data([1, 2, 3, 4]))
        
        let gblData = builder.buildToByteArray()
        XCTAssertFalse(gblData.isEmpty)
        
        // Test round-trip
        let gbl = Gbl()
        let parseResult = gbl.parseByteArray(gblData)
        
        switch parseResult {
        case .success(let tags):
            XCTAssertTrue(tags.contains { $0 is GblApplication })
            XCTAssertTrue(tags.contains { $0 is GblProg })
        case .fatal:
            XCTFail("Round-trip parsing should succeed")
        }
    }
}
```

### 2. Integration Tests

Test with real GBL files:

```swift
func testRealGblFile() {
    guard let testFileURL = Bundle(for: type(of: self)).url(forResource: "test", withExtension: "gbl"),
          let testData = try? Data(contentsOf: testFileURL) else {
        XCTFail("Failed to load test file")
        return
    }
    
    let gbl = Gbl()
    let result = gbl.parseByteArray(testData)
    
    switch result {
    case .success(let tags):
        // Verify expected tags are present
        XCTAssertTrue(tags.contains { $0.tagType == .headerV3 })
        XCTAssertTrue(tags.contains { $0.tagType == .end })
    case .fatal(let error):
        XCTFail("Real file parsing failed: \(error?.localizedDescription ?? "Unknown error")")
    }
}
```

### 3. Performance Tests

Compare performance with the original Kotlin version:

```swift
func testPerformance() {
    let testData = createLargeTestGblData()
    let gbl = Gbl()
    
    measure {
        let _ = gbl.parseByteArray(testData)
    }
}
```

## Potential Migration Challenges

### 1. Byte Order Handling
- Ensure little-endian byte order is maintained across platforms
- Test on both Intel and Apple Silicon Macs

### 2. Memory Usage
- Swift's value types may use more memory for large data structures
- Consider using `class` instead of `struct` for large tag data if needed

### 3. Threading
- Swift's actor model vs. Kotlin's coroutines
- Ensure thread safety for concurrent access

### 4. Platform Differences
- iOS memory constraints vs. JVM garbage collection
- Different optimization strategies needed

## Migration Guidelines

1. **Start with Core Types**: Convert enums and basic data structures first
2. **Test Incrementally**: Test each converted component thoroughly
3. **Maintain API Compatibility**: Keep the same public interface where possible
4. **Leverage Swift Features**: Use Swift's strong type system and memory management
5. **Performance Profiling**: Compare performance with original implementation

## Limitations

* Only **GBL version 3** files are fully supported
* **Large files** are loaded entirely into memory
* **Thread safety**: Create separate parser instances for concurrent use
* **Compression**: LZ4 and LZMA require additional dependencies for decompression
* **Little-Endian**: All multi-byte values use little-endian byte order

## License

Apache License 2.0

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## Support

For issues and questions:
- Create an issue on GitHub
- Check the wiki for common problems
- Review the API documentation
