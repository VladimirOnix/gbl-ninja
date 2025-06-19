from gbl import (
    Gbl, GblType, ParseResultSuccess, ParseResultFatal,
    GblHeader, GblApplication, GblProg, GblBootloader, GblEnd,
    GblMetadata, GblSeUpgrade, GblEraseProg, GblCertificateEcdsaP256,
    GblSignatureEcdsaP256, GblEncryptionInitAesCcm, GblProgLz4, GblProgLzma
)
import os
import struct


def parse_gbl_file(filename):
    print(f"\n=== Parsing {filename} ===")

    if not os.path.exists(filename):
        print(f"❌ File {filename} not found")
        return False

    gbl = Gbl()

    try:
        with open(filename, 'rb') as f:
            data = f.read()

        print(f"File size: {len(data)} bytes")

        result = gbl.parse_byte_array(data)

        if isinstance(result, ParseResultFatal):
            print(f"❌ Parse failed: {result.error}")
            return False

        if isinstance(result, ParseResultSuccess):
            tags = result.result_list
            print(f"✅ Successfully parsed {len(tags)} tags")

            analyze_tags(tags)

            verify_file_integrity(tags, data)

            return True

    except Exception as e:
        print(f"❌ Error reading file: {e}")
        return False


def analyze_tags(tags):
    print("\n--- Tag Analysis ---")

    for i, tag in enumerate(tags, 1):
        tag_name = tag.tag_type.name if tag.tag_type else "UNKNOWN"
        print(f"\n{i}. {tag_name} Tag:")

        if isinstance(tag, GblHeader):
            analyze_header_tag(tag)
        elif isinstance(tag, GblApplication):
            analyze_application_tag(tag)
        elif isinstance(tag, GblProg):
            analyze_prog_tag(tag)
        elif isinstance(tag, GblBootloader):
            analyze_bootloader_tag(tag)
        elif isinstance(tag, GblMetadata):
            analyze_metadata_tag(tag)
        elif isinstance(tag, GblSeUpgrade):
            analyze_se_upgrade_tag(tag)
        elif isinstance(tag, GblEraseProg):
            analyze_erase_prog_tag(tag)
        elif isinstance(tag, GblCertificateEcdsaP256):
            analyze_certificate_tag(tag)
        elif isinstance(tag, GblSignatureEcdsaP256):
            analyze_signature_tag(tag)
        elif isinstance(tag, GblEncryptionInitAesCcm):
            analyze_encryption_init_tag(tag)
        elif isinstance(tag, GblProgLz4):
            analyze_prog_lz4_tag(tag)
        elif isinstance(tag, GblProgLzma):
            analyze_prog_lzma_tag(tag)
        elif isinstance(tag, GblEnd):
            analyze_end_tag(tag)
        else:
            analyze_generic_tag(tag)


def analyze_header_tag(tag):
    print(f"   Version: 0x{tag.version:08X}")
    print(f"   GBL Type: 0x{tag.gbl_type:08X}")
    print(f"   Data Size: {len(tag.tag_data)} bytes")


def analyze_application_tag(tag):
    app_data = tag.application_data
    print(f"   Application Type: {app_data.type}")
    print(f"   Version: 0x{app_data.version:08X}")
    print(f"   Capabilities: 0x{app_data.capabilities:08X}")
    print(f"   Product ID: {app_data.product_id}")

    if len(tag.tag_data) > 13:
        extra_data = tag.tag_data[13:]
        print(f"   Additional Data: {len(extra_data)} bytes")
        if len(extra_data) <= 64:  # Show if not too long
            print(f"     Content: {extra_data}")


def analyze_prog_tag(tag):
    print(f"   Flash Start Address: 0x{tag.flash_start_address:08X}")
    print(f"   Data Size: {len(tag.data)} bytes")

    if len(tag.data) > 0:
        preview = tag.data[:16]
        hex_preview = ' '.join(f'{b:02X}' for b in preview)
        print(f"   Data Preview: {hex_preview}{'...' if len(tag.data) > 16 else ''}")


def analyze_bootloader_tag(tag):
    print(f"   Bootloader Version: 0x{tag.bootloader_version:08X}")
    print(f"   Address: 0x{tag.address:08X}")
    print(f"   Data Size: {len(tag.data)} bytes")

    if len(tag.data) > 0:
        preview = tag.data[:16]
        hex_preview = ' '.join(f'{b:02X}' for b in preview)
        print(f"   Data Preview: {hex_preview}{'...' if len(tag.data) > 16 else ''}")


def analyze_metadata_tag(tag):
    print(f"   Metadata Size: {len(tag.meta_data)} bytes")

    try:
        text_content = tag.meta_data.decode('utf-8', errors='ignore')
        if text_content.isprintable():
            print(f"   Content (text): {text_content[:100]}{'...' if len(text_content) > 100 else ''}")
        else:
            hex_preview = ' '.join(f'{b:02X}' for b in tag.meta_data[:32])
            print(f"   Content (hex): {hex_preview}{'...' if len(tag.meta_data) > 32 else ''}")
    except:
        hex_preview = ' '.join(f'{b:02X}' for b in tag.meta_data[:32])
        print(f"   Content (hex): {hex_preview}{'...' if len(tag.meta_data) > 32 else ''}")


def analyze_se_upgrade_tag(tag):
    print(f"   Blob Size: {tag.blob_size}")
    print(f"   Version: 0x{tag.version:08X}")
    print(f"   Data Size: {len(tag.data)} bytes")


def analyze_erase_prog_tag(tag):
    print(f"   Tag Data Size: {len(tag.tag_data)} bytes")
    if len(tag.tag_data) >= 8:
        try:
            val1, val2 = struct.unpack('<II', tag.tag_data[:8])
            print(f"   Value 1: 0x{val1:08X}")
            print(f"   Value 2: 0x{val2:08X}")
        except:
            hex_data = ' '.join(f'{b:02X}' for b in tag.tag_data)
            print(f"   Raw Data: {hex_data}")


def analyze_certificate_tag(tag):
    cert = tag.certificate
    print(f"   Struct Version: {cert.struct_version}")
    print(f"   Flags: 0x{cert.flags:02X}")
    print(f"   Key: 0x{cert.key:02X}")
    print(f"   Version: 0x{cert.version:08X}")
    print(f"   Signature: 0x{cert.signature:02X}")


def analyze_signature_tag(tag):
    print(f"   R: 0x{tag.r:02X}")
    print(f"   S: 0x{tag.s:02X}")


def analyze_encryption_init_tag(tag):
    print(f"   Message Length: {tag.msg_len}")
    print(f"   Nonce: 0x{tag.nonce:02X}")


def analyze_prog_lz4_tag(tag):
    print(f"   Compressed Data Size: {len(tag.tag_data)} bytes")

    if len(tag.tag_data) >= 8:
        try:
            flash_addr, decomp_size = struct.unpack('<II', tag.tag_data[:8])
            print(f"   Flash Address: 0x{flash_addr:08X}")
            print(f"   Decompressed Size: {decomp_size} bytes")
            compressed_data = tag.tag_data[8:]
            print(f"   Actual Compressed Data: {len(compressed_data)} bytes")
        except:
            print(f"   Raw tag data (first 16 bytes): {tag.tag_data[:16].hex()}")


def analyze_prog_lzma_tag(tag):
    print(f"   Compressed Data Size: {len(tag.tag_data)} bytes")

    if len(tag.tag_data) >= 8:
        try:
            flash_addr, decomp_size = struct.unpack('<II', tag.tag_data[:8])
            print(f"   Flash Address: 0x{flash_addr:08X}")
            print(f"   Decompressed Size: {decomp_size} bytes")
            compressed_data = tag.tag_data[8:]
            print(f"   Actual Compressed Data: {len(compressed_data)} bytes")
        except:
            print(f"   Raw tag data (first 16 bytes): {tag.tag_data[:16].hex()}")


def analyze_end_tag(tag):
    print(f"   CRC32: 0x{tag.gbl_crc:08X}")
    print("   This tag marks the end of the GBL file")


def analyze_generic_tag(tag):
    print(f"   Tag Type ID: 0x{tag.tag_header.id:08X}")
    print(f"   Data Length: {tag.tag_header.length}")

    if hasattr(tag, 'tag_data') and len(tag.tag_data) > 0:
        preview = tag.tag_data[:16]
        hex_preview = ' '.join(f'{b:02X}' for b in preview)
        print(f"   Data Preview: {hex_preview}{'...' if len(tag.tag_data) > 16 else ''}")


def verify_file_integrity(tags, original_data):
    print("\n--- File Integrity Check ---")

    # Find END tag
    end_tag = None
    for tag in tags:
        if isinstance(tag, GblEnd):
            end_tag = tag
            break

    if not end_tag:
        print("❌ No END tag found - file may be corrupted")
        return False

    print(f"Original CRC32: 0x{end_tag.gbl_crc:08X}")

    try:
        from gbl import create_end_tag_with_crc

        tags_without_end = [tag for tag in tags if not isinstance(tag, GblEnd)]
        calculated_end_tag = create_end_tag_with_crc(tags_without_end)

        print(f"Calculated CRC32: 0x{calculated_end_tag.gbl_crc:08X}")

        if end_tag.gbl_crc == calculated_end_tag.gbl_crc:
            print("✅ File integrity verified - CRC matches")
            return True
        else:
            print("❌ File integrity check failed - CRC mismatch")
            return False

    except Exception as e:
        print(f"❌ Error during integrity check: {e}")
        return False


def compare_gbl_files(file1, file2):
    print(f"\n=== Comparing {file1} vs {file2} ===")

    gbl = Gbl()

    # Parse both files
    try:
        with open(file1, 'rb') as f:
            data1 = f.read()
        result1 = gbl.parse_byte_array(data1)

        with open(file2, 'rb') as f:
            data2 = f.read()
        result2 = gbl.parse_byte_array(data2)

    except Exception as e:
        print(f"❌ Error reading files: {e}")
        return

    if not (isinstance(result1, ParseResultSuccess) and isinstance(result2, ParseResultSuccess)):
        print("❌ Failed to parse one or both files")
        return

    tags1 = result1.result_list
    tags2 = result2.result_list

    print(f"File 1: {len(tags1)} tags, {len(data1)} bytes")
    print(f"File 2: {len(tags2)} tags, {len(data2)} bytes")

    def count_tags_by_type(tags):
        counts = {}
        for tag in tags:
            tag_name = tag.tag_type.name if tag.tag_type else "UNKNOWN"
            counts[tag_name] = counts.get(tag_name, 0) + 1
        return counts

    counts1 = count_tags_by_type(tags1)
    counts2 = count_tags_by_type(tags2)

    all_tag_types = set(counts1.keys()) | set(counts2.keys())

    print("\nTag Type Comparison:")
    for tag_type in sorted(all_tag_types):
        count1 = counts1.get(tag_type, 0)
        count2 = counts2.get(tag_type, 0)
        status = "✅" if count1 == count2 else "❌"
        print(f"  {status} {tag_type}: {count1} vs {count2}")


def extract_firmware_data(filename):
    print(f"\n=== Extracting Firmware Data from {filename} ===")

    gbl = Gbl()

    try:
        with open(filename, 'rb') as f:
            data = f.read()

        result = gbl.parse_byte_array(data)

        if isinstance(result, ParseResultFatal):
            print(f"❌ Parse failed: {result.error}")
            return

        tags = result.result_list
        prog_tags = [tag for tag in tags if isinstance(tag, GblProg)]

        if not prog_tags:
            print("No programming data found in file")
            return

        print(f"Found {len(prog_tags)} programming section(s)")

        for i, prog_tag in enumerate(prog_tags):
            output_filename = f"{filename}_prog_section_{i}.bin"

            with open(output_filename, 'wb') as f:
                f.write(prog_tag.data)

            print(f"  Section {i}: Address 0x{prog_tag.flash_start_address:08X}, "
                  f"{len(prog_tag.data)} bytes -> {output_filename}")

    except Exception as e:
        print(f"❌ Error: {e}")


def main():
    print("GBL Parsing Examples")
    print("=" * 50)

    test_files = [
        "n2k.gbl",
        "advanced_firmware.gbl",
        "compressed_firmware.gbl",
        "simple_firmware.gbl"
    ]

    parsed_files = []

    for filename in test_files:
        if os.path.exists(filename):
            success = parse_gbl_file(filename)
            if success:
                parsed_files.append(filename)
        else:
            print(f"\n⚠️  {filename} not found - skipping")

    if len(parsed_files) >= 2:
        compare_gbl_files(parsed_files[0], parsed_files[1])

    if parsed_files:
        extract_firmware_data(parsed_files[0])

    print("\n" + "=" * 50)
    print("Parsing examples completed!")

    if not parsed_files:
        print("\n⚠️  No GBL files found to parse.")
        print("Run create_gbl_example.py first to create test files.")


if __name__ == "__main__":
    main()