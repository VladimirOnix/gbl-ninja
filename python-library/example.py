from gbl import Gbl
from results.parse_result import ParseResult

def example_basic_usage():
    """Basic usage example"""

    # Parse existing GBL file
    gbl_parser = Gbl()

    # Example: read from file
    try:
        with open("firmware.gbl", "rb") as f:
            gbl_data = f.read()

        result = gbl_parser.parse_byte_array(gbl_data)

        if isinstance(result, ParseResult.Success):
            print(f"Successfully parsed {len(result.result_list)} tags")
            for i, tag in enumerate(result.result_list):
                print(f"Tag {i}: {tag.tag_type}")
        else:
            print(f"Parse failed: {result.error}")

    except FileNotFoundError:
        print("File not found - this is just an example")

    # Create new GBL file
    builder = Gbl().GblBuilder.create()

    # Add application tag
    builder.application(type_val=32, version=0x10000)

    # Add program data
    firmware_data = b"SAMPLE_FIRMWARE_DATA"
    builder.prog(flash_start_address=0x1000, data=firmware_data)

    # Add metadata
    builder.metadata(b"Version 1.0")

    # Build to bytes
    gbl_bytes = builder.build_to_byte_array()
    print(f"Created GBL file: {len(gbl_bytes)} bytes")

    # Parse it back to verify
    verify_result = gbl_parser.parse_byte_array(gbl_bytes)
    if isinstance(verify_result, ParseResult.Success):
        print(f"Verification: parsed {len(verify_result.result_list)} tags")


if __name__ == "__main__":
    example_basic_usage()