# GBL Python Library

A Python implementation for parsing and generating Silicon Labs GBL (Gecko Bootloader) files, converted from the original Kotlin GBL-Ninja library.

## Features

* **Parse** GBL files into structured objects
* **Create** new GBL files from scratch
* **Validate** file integrity with automatic CRC verification
* **Support** for all standard GBL tag types
* **Container system** for advanced tag management

## Quick Start

### Parsing an existing GBL file

```python
from gbl import Gbl
from results.parse_result import ParseResult

# Create parser instance
gbl_parser = Gbl()

# Read and parse GBL file
with open("firmware.gbl", "rb") as f:
    gbl_data = f.read()

result = gbl_parser.parse_byte_array(gbl_data)

if isinstance(result, ParseResult.Success):
    print(f"Successfully parsed {len(result.result_list)} tags")
    for i, tag in enumerate(result.result_list):
        print(f"Tag {i}: {tag.tag_type}")
else:
    print(f"Parse failed: {result.error}")
```

### Creating a new GBL file

```python
from gbl import Gbl

# Create builder
builder = Gbl().GblBuilder.create()

# Add application tag
builder.application(
    type_val=32,
    version=0x10000,
    capabilities=0,
    product_id=54
)

# Add program data
firmware_data = b"YOUR_FIRMWARE_DATA_HERE"
builder.prog(flash_start_address=0x1000, data=firmware_data)

# Add metadata
builder.metadata(b"Version 1.0.0")

# Build to bytes
gbl_bytes = builder.build_to_byte_array()

# Save to file
with open("output.gbl", "wb") as f:
    f.write(gbl_bytes)

print(f"Created GBL file: {len(gbl_bytes)} bytes")
```

## Supported Tag Types

| Tag Type             | Description                        |
|----------------------|------------------------------------|
| HEADER_V3            | GBL file header (version 3)       |
| BOOTLOADER           | Bootloader information             |
| APPLICATION          | Application data                   |
| METADATA             | File metadata                      |
| PROG                 | Raw programming data               |
| PROG_LZ4             | LZ4-compressed programming data    |
| PROG_LZMA            | LZMA-compressed programming data   |
| ERASEPROG            | Memory erase command               |
| ENCRYPTION_DATA      | Encrypted payload data             |
| ENCRYPTION_INIT      | Encryption initialization data     |
| SIGNATURE_ECDSA_P256 | ECDSA P-256 signature              |
| CERTIFICATE_ECDSA_P256| ECDSA P-256 certificate           |
| SE_UPGRADE           | SE upgrade information             |
| END                  | Final tag with CRC                 |

## Builder Methods

### Application Tag
```python
builder.application(
    type_val: int = 32,           # Application type
    version: int = 0x10000,       # Application version
    capabilities: int = 0,        # Application capabilities
    product_id: int = 54          # Product ID
)
```

### Program Tag
```python
builder.prog(
    flash_start_address: int,     # Flash start address
    data: bytes                   # Program data
)
```

### Bootloader Tag
```python
builder.bootloader(
    bootloader_version: int,      # Bootloader version
    address: int,                 # Load address
    data: bytes                   # Bootloader data
)
```

### Metadata Tag
```python
builder.metadata(
    meta_data: bytes              # Metadata content
)
```

## Error Handling

The library uses result types for error handling:

```python
# Parse result
if isinstance(result, ParseResult.Success):
    tags = result.result_list
    # Process tags...
else:
    print(f"Parse error: {result.error}")

# Container result
if isinstance(result, ContainerResult.Success):
    data = result.data
    # Use data...
else:
    print(f"Error: {result.message}")
```

## Complete Example

```python
from gbl import Gbl
from results.parse_result import ParseResult

# Create builder
builder = Gbl().GblBuilder.create()

# Add application info
builder.application(
    type_val=32,
    version=0x010203,  # Version 1.2.3
    capabilities=0x00000001,
    product_id=42
)

# Add main firmware
with open("main_firmware.bin", "rb") as f:
    main_fw = f.read()
builder.prog(flash_start_address=0x8000, data=main_fw)

# Add bootloader
with open("bootloader.bin", "rb") as f:
    bootloader = f.read()
builder.bootloader(
    bootloader_version=0x010001,
    address=0x0000,
    data=bootloader
)

# Add version info as metadata
version_info = b"Firmware v1.2.3 - Built 2024-01-15"
builder.metadata(version_info)

# Build and save
gbl_data = builder.build_to_byte_array()
with open("complete_firmware.gbl", "wb") as f:
    f.write(gbl_data)

print(f"Created firmware file: {len(gbl_data)} bytes")
```

## Requirements

- Python 3.7+
- No external dependencies (uses only Python standard library)

## License

Apache License 2.0
