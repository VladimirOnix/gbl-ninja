package gblninja.core

internal class HelpPrinter {
    fun printHelp() {
        println("""
        GBL Tool - Utility for working with GBL files
        
        Commands:
          -i, --gblinfo                    Show information about a GBL file
          -c, --gblcreate                  Create a new GBL file
          --pack                           Create empty GBL file with Header tag only
          --add                            Add a tag to existing GBL file at specific index
          --remove                         Remove tag from GBL file by index
          --set                            Replace tag in GBL file at specific index
          --create                         Add END tag to GBL file if not present
        
        Options for gblinfo:
          -f, --file <path>                Path to the input GBL file to analyze
          -F, --format <format>            Output format for analysis:
                                             compact  - compact table view (default)
                                             c        - same as compact
                                             full     - detailed view with hex data
                                             f        - same as full
                                             hex      - same as full
                                             h        - same as full
        
        Options for gblcreate:
          -f, --file <path>                Path to the output file
          -t, --type <type>                Type of GBL file:
                                             empty         - empty GBL file
                                             bootloader    - GBL with bootloader
                                             metadata      - GBL with metadata
                                             prog-lz4      - GBL with LZ4 compressed program
                                             prog-lzma     - GBL with LZMA compressed program
                                             se-upgrade    - GBL with SE upgrade
                                             encrypted     - encrypted GBL file
                                             signed        - signed GBL file
                                             version-dep   - GBL with version dependency
          -v, --version <version>          Version (for bootloader/se-upgrade)
          -m, --metadata <text>            Metadata text (for metadata)
          -a, --address <hex>              Address (for bootloader/prog, hex format)
          -s, --size <bytes>               Data size in bytes
          -d, --data <file>                Data file (optional)
          -n, --nonce <hex>                Nonce for encryption (hex format)
          -r, --r-value <hex>              R value for signature (hex format)
          --s-value <hex>                  S value for signature (hex format)
          --dependency <version>           Version dependency string
        
        Options for tag operations (add/remove/set):
          -f, --file <path>                Input/Output GBL file
          -o, --output <path>              Output file (default: overwrite input)
          --index <N>                      Tag index (REQUIRED for all operations)
          --type <tag_type>                Tag type (for add/set operations)
          -a, --address <hex>              Address for tags that require it
          -d, --data <file|string>         Data file path or hex string
          -v, --version <version>          Version number
          -m, --metadata <text|file>       Metadata text or file path
          -r, --r-value <hex>              R value for signature
          --s-value <hex>                  S value for signature
          --dependency <version>           Version dependency string
        
        Available tag types for operations:
          application                      - Application tag
          metadata                         - Metadata tag  
          prog                            - Program data tag
          bootloader                      - Bootloader tag
          se-upgrade                      - SE upgrade tag
          encryption-init                 - Encryption initialization
          signature                       - Digital signature
          eraseprog                       - Erase program tag
          end                             - End tag (for --create command)
          
        Examples for gblinfo:
          # View tags in GBL file (compact format)
          ./gbl-ninja -i -f firmware.gbl
          
          # View detailed tag information  
          ./gbl-ninja -i -f firmware.gbl -F full
          
        Examples for gblcreate:
          # Create empty GBL file
          ./gbl-ninja -c -f empty.gbl -t empty -s 2048
          
        Examples for pack:
          # Create empty GBL file with only Header tag
          ./gbl-ninja --pack -f empty.gbl
          
        Examples for add:
          # Add metadata tag at index 1
          ./gbl-ninja --add -f firmware.gbl --index 1 --type metadata --metadata "My metadata"
          
          # Add program tag at index 2 with data from file
          ./gbl-ninja --add -f firmware.gbl --index 2 --type prog --address "0x08000000" --data program.bin
          
          # Add application tag at index 0
          ./gbl-ninja --add -f firmware.gbl --index 0 --type application
          
          # Add signature tag at index 3
          ./gbl-ninja --add -f firmware.gbl --index 3 --type signature --r-value "deadbeef" --s-value "cafebabe"
          
        Examples for remove:
          # Remove tag at index 2
          ./gbl-ninja --remove -f firmware.gbl --index 2
          
          # Remove tag at index 0
          ./gbl-ninja --remove -f firmware.gbl --index 0
          
        Examples for set:
          # Replace tag at index 1 with metadata tag
          ./gbl-ninja --set -f firmware.gbl --index 1 --type metadata --metadata "New metadata"
          
          # Replace tag at index 2 with program tag
          ./gbl-ninja --set -f firmware.gbl --index 2 --type prog --address "0x08004000" --data newprogram.bin
          
        Examples for create:
          # Add END tag if not present
          ./gbl-ninja --create -f firmware.gbl
          
        Typical workflow:
          1. Create empty GBL:     ./gbl-ninja --pack -f my.gbl
          2. View tags:           ./gbl-ninja -i -f my.gbl  
          3. Add application:     ./gbl-ninja --add -f my.gbl --index 0 --type application
          4. Add program data:    ./gbl-ninja --add -f my.gbl --index 1 --type prog --address "0x08000000" --data app.bin
          5. Add metadata:        ./gbl-ninja --add -f my.gbl --index 2 --type metadata --metadata "Version 1.0"
          6. Finalize with END:   ./gbl-ninja --create -f my.gbl
          7. Verify final:        ./gbl-ninja -i -f my.gbl
          
        Important notes:
          - Index is REQUIRED for add, remove, and set operations
          - Index 0 means insert at the beginning, higher numbers insert after existing tags
          - For remove operations, specify the exact index of the tag to remove
          - For set operations, specify the index of the tag to replace
          - Use --data with file path for binary data or hex string for simple values
          - Address values should be in hex format like "0x08000000"
          
        Output format examples:
          
          Compact format shows:
          - Tag number, type, start address, size, and brief info
          - Suitable for quick overview and finding tag indices
          
          Full format shows:
          - All tag details with hex data dumps
          - Complete tag structure information
          - Hex data limited to first 10 lines per field
        """.trimIndent())
    }
}