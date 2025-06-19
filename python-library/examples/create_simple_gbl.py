from gbl import (
    Gbl, ApplicationData, ApplicationCertificate,
    ParseResultSuccess, ParseResultFatal
)
import os


def create_simple_gbl():
    print("=== Creating Simple GBL File ===")

    builder = Gbl().GblBuilder.create()

    builder.application(
        type_val=32,
        version=0x10000,
        capabilities=0x00000001,
        product_id=54
    )

    firmware_data = bytearray(b"SAMPLE_FIRMWARE_DATA_" + b"\x00" * 100)
    builder.prog(
        flash_start_address=0x08000000,
        data=firmware_data
    )

    builder.erase_prog()

    gbl_bytes = builder.build_to_byte_array()

    filename = "simple_firmware.gbl"
    with open(filename, 'wb') as f:
        f.write(gbl_bytes)

    print(f"Created {filename}: {len(gbl_bytes)} bytes")
    print(f"Tags included: {len(builder.build_to_list())} total tags")

    return filename


def create_advanced_gbl():
    print("\n=== Creating Advanced GBL File ===")

    builder = Gbl().GblBuilder.create()

    bootloader_data = bytearray(b"BOOTLOADER_V2.1" + b"\x00" * 50)
    builder.bootloader(
        bootloader_version=0x02010000,
        address=0x08000000,
        data=bootloader_data
    )

    additional_app_data = bytearray(b"APP_METADATA_EXTRA_INFO")
    builder.application(
        type_val=32,
        version=0x20000,
        capabilities=0x00000003,
        product_id=78,
        additional_data=additional_app_data
    )

    metadata = bytearray(b"Product: Smart Device v2.0\nBuild: 2024-06-19\nFeatures: WiFi,BLE")
    builder.metadata(metadata)

    main_firmware = bytearray(b"MAIN_FIRMWARE_CODE_" + b"\xAA" * 200)
    builder.prog(
        flash_start_address=0x08001000,
        data=main_firmware
    )

    config_data = bytearray(b"CONFIG_SECTION_" + b"\x55" * 64)
    builder.prog(
        flash_start_address=0x08080000,
        data=config_data
    )

    se_data = bytearray(b"SECURE_ELEMENT_UPGRADE_" + b"\xFF" * 128)
    builder.se_upgrade(
        version=0x01020003,
        data=se_data
    )

    certificate = ApplicationCertificate(
        struct_version=1,
        flags=0x02,
        key=0x01,
        version=0x10000,
        signature=0xAB
    )
    builder.certificate_ecdsa_p256(certificate)

    builder.signature_ecdsa_p256(r=0xCD, s=0xEF)

    builder.encryption_init(msg_len=1024, nonce=0x42)

    dependency_data = bytearray(b"\x01\x00\x00\x00\x02\x00\x00\x10\x00\x00")
    builder.version_dependency(dependency_data)

    gbl_bytes = builder.build_to_byte_array()

    filename = "advanced_firmware.gbl"
    with open(filename, 'wb') as f:
        f.write(gbl_bytes)

    print(f"Created {filename}: {len(gbl_bytes)} bytes")

    tags = builder.build_to_list()
    print(f"Tags included ({len(tags)} total):")
    for i, tag in enumerate(tags, 1):
        tag_name = tag.tag_type.name if tag.tag_type else "UNKNOWN"
        tag_size = len(tag.content()) if hasattr(tag, 'content') else 0
        print(f"  {i}. {tag_name} ({tag_size} bytes)")

    return filename


def create_compressed_gbl():
    print("\n=== Creating GBL with Compressed Data ===")

    builder = Gbl().GblBuilder.create()

    builder.application(
        type_val=32,
        version=0x30000,
        capabilities=0x00000007,
        product_id=99
    )

    original_size = 2048
    compressed_firmware_lz4 = bytearray(b"LZ4_COMPRESSED_DATA_" + b"\x12\x34\x56\x78" * 50)

    builder.prog_lz4(
        flash_start_address=0x08002000,
        compressed_data=compressed_firmware_lz4,
        decompressed_size=original_size
    )

    compressed_firmware_lzma = bytearray(b"LZMA_COMPRESSED_DATA_" + b"\x87\x65\x43\x21" * 40)

    builder.prog_lzma(
        flash_start_address=0x08004000,
        compressed_data=compressed_firmware_lzma,
        decompressed_size=1536
    )

    gbl_bytes = builder.build_to_byte_array()
    filename = "compressed_firmware.gbl"

    with open(filename, 'wb') as f:
        f.write(gbl_bytes)

    print(f"Created {filename}: {len(gbl_bytes)} bytes")
    print("Includes LZ4 and LZMA compressed sections")

    return filename


def verify_created_files():
    print("\n=== Verifying Created Files ===")

    gbl = Gbl()
    created_files = [
        "simple_firmware.gbl",
        "advanced_firmware.gbl",
        "compressed_firmware.gbl"
    ]

    for filename in created_files:
        if os.path.exists(filename):
            try:
                with open(filename, 'rb') as f:
                    data = f.read()

                result = gbl.parse_byte_array(data)

                if isinstance(result, ParseResultSuccess):
                    tags = result.result_list
                    print(f"✓ {filename}: Successfully parsed {len(tags)} tags")

                    tag_counts = {}
                    for tag in tags:
                        tag_name = tag.tag_type.name if tag.tag_type else "UNKNOWN"
                        tag_counts[tag_name] = tag_counts.get(tag_name, 0) + 1

                    for tag_name, count in tag_counts.items():
                        print(f"    {tag_name}: {count}")

                elif isinstance(result, ParseResultFatal):
                    print(f"✗ {filename}: Parse failed - {result.error}")

            except Exception as e:
                print(f"✗ {filename}: Error reading file - {e}")
        else:
            print(f"✗ {filename}: File not found")


def demonstrate_builder_operations():
    print("\n=== Demonstrating Builder Operations ===")

    builder = Gbl().GblBuilder.create()

    print(f"Initial state: {builder.size()} tags, Empty: {builder.is_empty()}")

    builder.application(type_val=32, version=0x40000)
    # FIX: Convert bytes to bytearray to avoid .copy() method issues
    test_data = bytearray(b"test_data")
    builder.prog(flash_start_address=0x1000, data=test_data)

    print(f"After adding tags: {builder.size()} tags")
    print(f"Tag types: {[t.name for t in builder.get_tag_types()]}")

    from gbl import GblType
    print(f"Has APPLICATION tag: {builder.has_tag(GblType.APPLICATION)}")
    print(f"Has BOOTLOADER tag: {builder.has_tag(GblType.BOOTLOADER)}")

    app_tag = builder.get_tag(GblType.APPLICATION)
    if app_tag:
        print(f"Found APPLICATION tag with type: {type(app_tag).__name__}")

    print(f"Before clear: {builder.size()} tags")
    clear_result = builder.clear()
    print(f"After clear: {builder.size()} tags (only protected tags remain)")


if __name__ == "__main__":
    print("GBL Creation Examples")
    print("=" * 50)

    try:
        simple_file = create_simple_gbl()
        advanced_file = create_advanced_gbl()
        compressed_file = create_compressed_gbl()

        verify_created_files()

        demonstrate_builder_operations()

        print("\n" + "=" * 50)
        print("All examples completed successfully!")
        print("Created files:")
        for filename in [simple_file, advanced_file, compressed_file]:
            if os.path.exists(filename):
                size = os.path.getsize(filename)
                print(f"  - {filename} ({size} bytes)")

    except Exception as e:
        print(f"Error during execution: {e}")
        import traceback
        traceback.print_exc()