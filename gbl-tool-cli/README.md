# GBL Ninja CLI Tool

Command-line tool for analyzing, creating, and modifying GBL (Gecko Bootloader) files used in Silicon Labs firmware updates.

## Features

- **Analyze** GBL files and display tag information
- **Create** new GBL files with header and end tags
- **Add/Remove/Replace** tags at specific positions
- **Pack** empty GBL files with header only

## Installation

### Prerequisites
- Java 17 or higher
- `gbl-ninja.jar` file (built from library)

### Setup
1. Place `gbl-ninja.jar` in the same directory as wrapper scripts
2. Make executable: `chmod +x gblninja.sh` (Linux/macOS)

## Usage

```bash
# Linux/macOS
./gblninja.sh [command] [options]

# Windows  
gblninja.bat [command] [options]

# Direct JAR
java -jar gbl-ninja.jar [command] [options]
```

## Commands

### Analyze Files (`-i` / `--gblinfo`)
```bash
# Basic analysis
./gblninja.sh -i -f firmware.gbl

# Detailed view with hex data
./gblninja.sh -i -f firmware.gbl --format full
```

### Create New GBL (`-c` / `--gblcreate`)
```bash
# Create complete GBL file with header and end tags
./gblninja.sh -c -f new_firmware.gbl
```

### Create Empty GBL (`--pack`)
```bash
# Create GBL with only header tag
./gblninja.sh --pack -f empty.gbl
```

### Add Tags (`--add`)
```bash
# Add metadata at index 1
./gblninja.sh --add -f firmware.gbl --index 1 --type metadata --metadata "Version 1.0"

# Add program data at index 2  
./gblninja.sh --add -f firmware.gbl --index 2 --type prog --address 0x08000000 --data program.bin

# Add application tag
./gblninja.sh --add -f firmware.gbl --index 1 --type application
```

### Remove Tags (`--remove`)
```bash
./gblninja.sh --remove -f firmware.gbl --index 2
```

### Replace Tags (`--set`)
```bash
./gblninja.sh --set -f firmware.gbl --index 1 --type metadata --metadata "New Version"
```

### Finalize GBL (`--create`)
```bash
# Add END tag if missing
./gblninja.sh --create -f firmware.gbl
```

## Supported Tag Types

| Tag Type | Description | Required Parameters |
|----------|-------------|-------------------|
| `application` | Application data | None (uses defaults) |
| `bootloader` | Bootloader information | `--address`, `--data`, `--version` |
| `prog` | Program data | `--address`, `--data` |
| `prog_lz4` | LZ4 compressed program | `--address`, `--data`, `--decompressed-size` |
| `prog_lzma` | LZMA compressed program | `--address`, `--data`, `--decompressed-size` |
| `metadata` | Metadata text/data | `--metadata` or `--data` |
| `se_upgrade` | SE upgrade data | `--version`, `--data` |
| `eraseprog` | Erase command | None |
| `signature` | ECDSA signature | `--r-value`, `--s-value` |
| `encryption_data` | Encrypted data | `--data` |
| `encryption_init` | Encryption init | `--msg-len`, `--nonce` |

## Common Options

| Option | Alias | Description |
|--------|-------|-------------|
| `--file <path>` | `-f` | Input/output GBL file |
| `--output <path>` | `-o` | Output file (default: overwrite input) |
| `--format <format>` | `-F` | Output format: `compact` or `full` |
| `--index <N>` | | Tag position (required for add/remove/set) |
| `--type <type>` | `-t` | Tag type for add/set operations |
| `--address <hex>` | `-a` | Memory address |
| `--data <file>` | `-d` | Data file or hex string |
| `--metadata <text>` | `-m` | Metadata text or file |

## Examples

### Complete Workflow
```bash
# 1. Create empty GBL
./gblninja.sh --pack -f firmware.gbl

# 2. Add application tag  
./gblninja.sh --add -f firmware.gbl --index 1 --type application

# 3. Add program data
./gblninja.sh --add -f firmware.gbl --index 2 --type prog --address 0x08000000 --data app.bin

# 4. Add metadata
./gblninja.sh --add -f firmware.gbl --index 3 --type metadata --metadata "Firmware v1.0"

# 5. Finalize with END tag
./gblninja.sh --create -f firmware.gbl

# 6. Verify result
./gblninja.sh -i -f firmware.gbl
```

### Data Formats
```bash
# File data
--data firmware.bin

# Hex string  
--data 0x48656c6c6f

# Text metadata
--metadata "Version 1.0"

# Hex address
--address 0x08000000
```

## Index System

Tags are positioned by index in the GBL file:

```
Index 0: HEADER_V3    (automatic)
Index 1: First user tag
Index 2: Second user tag  
Index N: Last user tag
Index X: END tag      (automatic)
```

**Note:** You cannot modify HEADER_V3 (index 0) or END tags directly.

## Output Formats

### Compact (default)
```
No.  Tag Type             Start Addr   Size     Additional Info
-----------------------------------------------------------------
0.   HEADER_V3            -            16B      v50331648, type=0
1.   APPLICATION          -            21B      type=32, version=5
2.   PROG                 0x8000000    1028B    
3.   END                  -            12B      CRC=0x12345678
```

### Full
Shows detailed hex dumps and complete tag information.

## License

Apache License 2.0