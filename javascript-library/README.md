# GBL-Ninja JavaScript Library

JavaScript implementation of the GBL (Gecko Bootloader) file parser and builder. This library provides comprehensive functionality for working with GBL files in web browsers and Node.js environments.

## Features

* **Parse** GBL files into structured objects
* **Create** new GBL files from scratch  
* **Modify** existing tag contents
* **Validate** file integrity with automatic CRC verification
* **Support** for all standard GBL tag types
* **Container system** for advanced tag management
* **Drag & Drop** web interface for easy file manipulation
* **Cross-platform** compatibility (browsers and Node.js)

## Project Structure

```
javascript-library/
├── gbl-library.js          # Core GBL library (standalone)
├── web/                    # Web-based parser & builder
│   ├── index.html         # Main HTML interface
│   ├── styles.css         # UI styling
│   └── script.js          # Web application logic
└── README.md              # This file
```

## Quick Start

### Browser Usage

Include the library in your HTML:

```html
<script src="gbl-library.js"></script>
<script>
// Parse existing GBL file
const gbl = new Gbl();
const fileData = new Uint8Array([...]); // Your GBL file data

const parseResult = gbl.parseByteArray(fileData);

if (parseResult.type === 'Success') {
    console.log(`Parsed ${parseResult.resultList.length} tags`);
    parseResult.resultList.forEach(tag => {
        console.log(`Tag: ${tag.tagType.name}, Size: ${tag.tagHeader.length} bytes`);
    });
} else {
    console.error(`Parse error: ${parseResult.error}`);
}

// Create new GBL file
const builder = Gbl.GblBuilder.create()
    .application(32, 0x10000, 0, 54)
    .prog(0x1000, new Uint8Array([0x00, 0x01, 0x02, 0x03]));

const gblBytes = builder.buildToByteArray();
</script>
```

### Node.js Usage

```javascript
const fs = require('fs');
// Import the library (you'll need to adapt for Node.js module system)

// Parse existing GBL file
const fileData = fs.readFileSync('firmware.gbl');
const gbl = new Gbl();
const parseResult = gbl.parseByteArray(new Uint8Array(fileData));

if (parseResult.type === 'Success') {
    console.log(`Successfully parsed ${parseResult.resultList.length} tags`);
} else {
    console.error(`Parse failed: ${parseResult.error}`);
}

// Create new GBL file
const builder = Gbl.GblBuilder.create()
    .application(32, 0x10000, 0, 54)
    .prog(0x1000, new Uint8Array([0x00, 0x01, 0x02, 0x03]));

const gblBytes = builder.buildToByteArray();
fs.writeFileSync('new_firmware.gbl', Buffer.from(gblBytes));
```

## Web Interface

The library includes a complete web-based GBL parser and builder located in the `web/` directory.

### Features

* **🎯 Drag & Drop Interface** - Simply drag .gbl files to parse them
* **📊 Visual Analysis** - View detailed tag information and statistics  
* **🔧 Interactive Builder** - Create GBL files by dragging tag types
* **✏️ Tag Editor** - Modify tag parameters with user-friendly forms
* **💾 Export Functionality** - Download created GBL files instantly
* **📱 Responsive Design** - Works on desktop and mobile devices

### Running the Web Interface

1. **Local Development:**
   ```bash
   cd javascript-library/web/
   # Serve the files using any HTTP server, for example:
   python -m http.server 8000
   # or
   npx serve .
   ```

2. **Open in Browser:**
   Navigate to `http://localhost:8000` to access the interface

3. **Usage:**
   - **Parser Tab:** Upload or drag .gbl files to analyze them
   - **Builder Tab:** Drag tag types from the palette to create new GBL files

## API Reference

### Core Classes

#### Gbl

Main parser class for reading and writing GBL files.

```javascript
class Gbl {
    parseByteArray(byteArray)  // Returns ParseResult
    encode(tags)               // Returns Uint8Array
}
```

#### Gbl.GblBuilder

Builder class for creating GBL files with a fluent API.

```javascript
// Create builder with header and end tags
const builder = Gbl.GblBuilder.create();

// Available methods:
builder.application(type, version, capabilities, productId, additionalData)
builder.bootloader(bootloaderVersion, address, data)
builder.prog(flashStartAddress, data)
builder.metadata(data)
builder.eraseProg()

// Build methods:
builder.buildToByteArray()  // Returns Uint8Array
builder.buildToList()       // Returns Array<Tag>
builder.get()              // Returns current tags

// Container management:
builder.hasTag(tagTypeName)
builder.getTag(tagTypeName)
builder.removeTagAt(index)
builder.clear()
```

### Tag Types

All standard GBL tag types are supported:

| Tag Type | Description |
|----------|-------------|
| `HEADER_V3` | GBL file header (version 3) |
| `APPLICATION` | Application data and metadata |
| `BOOTLOADER` | Bootloader information |
| `PROG` | Raw programming data |
| `PROG_LZ4` | LZ4-compressed programming data |
| `PROG_LZMA` | LZMA-compressed programming data |
| `ERASEPROG` | Memory erase command |
| `METADATA` | File metadata |
| `ENCRYPTION_DATA` | Encrypted payload data |
| `ENCRYPTION_INIT` | Encryption initialization |
| `SIGNATURE_ECDSA_P256` | ECDSA P-256 signature |
| `CERTIFICATE_ECDSA_P256` | ECDSA P-256 certificate |
| `SE_UPGRADE` | SE upgrade information |
| `VERSION_DEPENDENCY` | Version requirements |
| `END` | Final tag with CRC |

## Advanced Usage

### Container System

The library includes a sophisticated container system:

```javascript
const builder = Gbl.GblBuilder.create();

// Check if tag exists
if (builder.hasTag('APPLICATION')) {
    const appTag = builder.getTag('APPLICATION');
}

// Remove specific tag by index
const result = builder.removeTagAt(1);
if (result.type === 'Success') {
    console.log('Tag removed successfully');
}

// Clear all non-protected tags
builder.clear();
```

### Error Handling

Always check parse results:

```javascript
const parseResult = gbl.parseByteArray(data);

if (parseResult.type === 'Success') {
    // Process tags
    parseResult.resultList.forEach(tag => {
        console.log(`Processing ${tag.tagType.name}`);
    });
} else {
    console.error(`Parse failed: ${parseResult.error}`);
}
```

### Working with Files

```javascript
// Read file from input element
const fileInput = document.getElementById('fileInput');
fileInput.addEventListener('change', async (e) => {
    const file = e.target.files[0];
    const arrayBuffer = await file.arrayBuffer();
    const uint8Array = new Uint8Array(arrayBuffer);
    
    const parseResult = gbl.parseByteArray(uint8Array);
    // Process result...
});

// Download created GBL file
function downloadGBL(gblBytes, filename = 'firmware.gbl') {
    const blob = new Blob([gblBytes], { type: 'application/octet-stream' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
}
```

## Browser Compatibility

- **Modern Browsers:** Chrome 60+, Firefox 55+, Safari 12+, Edge 79+
- **Required APIs:** Uint8Array, DataView, ArrayBuffer, File API
- **Optional:** Drag & Drop API (for web interface)

## Limitations

* **File Size:** Large files are loaded entirely into memory
* **Compression:** LZ4 and LZMA decompression not implemented (data preserved as-is)
* **Storage:** No localStorage/sessionStorage support in artifact environments
* **Threading:** Single-threaded execution (JavaScript limitation)

## Development

### File Structure

```
gbl-library.js    # Complete standalone library
web/
├── index.html    # Web interface HTML
├── styles.css    # Modern UI styling  
└── script.js     # Web app + embedded library
```

### Customization

The web interface is fully customizable:

- Modify `styles.css` for different themes
- Update `script.js` for additional functionality
- Extend the core library for new tag types

## Contributing

1. Fork the repository
2. Make your changes to the JavaScript files
3. Test with both browser and Node.js environments
4. Submit a pull request

## License

Apache License 2.0

## Related Projects

- **Kotlin Library:** Original implementation with advanced features
- **Python Library:** Python port with identical functionality
- **CLI Tool:** Command-line interface for batch processing