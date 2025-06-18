"""
Tag encoding functionality for GBL files
Converted from Kotlin encodeTags.kt
"""

import struct
import zlib
from typing import List, Union
from io import BytesIO

# Імпорти з інших модулів (будуть додані пізніше):
# from tag.tag import Tag
# from tag.tag_with_header import TagWithHeader
# from tag.tag_header import TagHeader
# from tag.gbl_type import GblType
# from tag.type.gbl_header import GblHeader
# from tag.type.gbl_bootloader import GblBootloader
# from tag.type.gbl_application import GblApplication
# from tag.type.gbl_prog import GblProg
# from tag.type.gbl_se_upgrade import GblSeUpgrade
# from tag.type.gbl_end import GblEnd
# from tag.type.gbl_metadata import GblMetadata
# from tag.type.gbl_prog_lz4 import GblProgLz4
# from tag.type.gbl_prog_lzma import GblProgLzma
# from tag.type.gbl_erase_prog import GblEraseProg
# from tag.type.gbl_tag_delta import GblTagDelta
# from tag.type.certificate.gbl_certificate_ecdsa_p256 import GblCertificateEcdsaP256
# from tag.type.certificate.gbl_signature_ecdsa_p256 import GblSignatureEcdsaP256
# from tag.type.encryption.gbl_encryption_data import GblEncryptionData
# from tag.type.encryption.gbl_encryption_init_aes_ccm import GblEncryptionInitAesCcm
# from tag.type.version.gbl_version_dependency import GblVersionDependency


# Constants
TAG_ID_SIZE = 4
TAG_LENGTH_SIZE = 4


def encode_tags(tags: List['Tag']) -> bytes:
    """
    Encode list of tags to byte array

    Args:
        tags: List of Tag objects to encode

    Returns:
        bytes: Encoded GBL file content
    """
    total_size = _calculate_total_size(tags)
    buffer = BytesIO()

    for tag in tags:
        if not hasattr(tag, 'tag_header'):
            continue

        # Write tag ID (little-endian)
        buffer.write(struct.pack('<I', tag.tag_header.id))

        # Write tag length (little-endian)
        buffer.write(struct.pack('<I', tag.tag_header.length))

        # Write tag data
        tag_data = generate_tag_data(tag)
        buffer.write(tag_data)

    return buffer.getvalue()


def generate_tag_data(tag: 'Tag') -> bytes:
    """
    Generate tag-specific data based on tag type

    Args:
        tag: Tag object to generate data for

    Returns:
        bytes: Tag-specific data
    """
    # Динамічна перевірка типу тегу за назвою класу
    tag_class_name = tag.__class__.__name__

    if tag_class_name == 'GblHeader':
        return _generate_header_data(tag)

    elif tag_class_name == 'GblBootloader':
        return _generate_bootloader_data(tag)

    elif tag_class_name == 'GblApplication':
        return _generate_application_data(tag)

    elif tag_class_name == 'GblProg':
        return _generate_prog_data(tag)

    elif tag_class_name == 'GblSeUpgrade':
        return _generate_se_upgrade_data(tag)

    elif tag_class_name == 'GblEnd':
        return _generate_end_data(tag)

    elif tag_class_name == 'GblMetadata':
        return _generate_metadata_data(tag)

    elif tag_class_name == 'GblProgLz4':
        return _generate_prog_lz4_data(tag)

    elif tag_class_name == 'GblProgLzma':
        return _generate_prog_lzma_data(tag)

    elif tag_class_name == 'GblEraseProg':
        return _generate_erase_prog_data(tag)

    elif tag_class_name == 'GblTagDelta':
        return _generate_tag_delta_data(tag)

    elif tag_class_name == 'GblCertificateEcdsaP256':
        return _generate_certificate_ecdsa_p256_data(tag)

    elif tag_class_name == 'GblSignatureEcdsaP256':
        return _generate_signature_ecdsa_p256_data(tag)

    elif tag_class_name == 'GblEncryptionData':
        return _generate_encryption_data(tag)

    elif tag_class_name == 'GblEncryptionInitAesCcm':
        return _generate_encryption_init_data(tag)

    elif tag_class_name == 'GblVersionDependency':
        return _generate_version_dependency_data(tag)

    else:
        # Default fallback - return raw tag data
        return getattr(tag, 'tag_data', b'')


def _generate_header_data(tag: 'GblHeader') -> bytes:
    """Generate data for GBL header tag"""
    buffer = BytesIO()
    buffer.write(struct.pack('<I', tag.version))
    buffer.write(struct.pack('<I', tag.gbl_type))
    return buffer.getvalue()


def _generate_bootloader_data(tag: 'GblBootloader') -> bytes:
    """Generate data for bootloader tag"""
    buffer = BytesIO()
    buffer.write(struct.pack('<I', tag.bootloader_version))
    buffer.write(struct.pack('<I', tag.address))
    buffer.write(tag.data)
    return buffer.getvalue()


def _generate_application_data(tag: 'GblApplication') -> bytes:
    """Generate data for application tag"""
    app_data = tag.application_data
    buffer = BytesIO()

    # Write application data fields
    buffer.write(struct.pack('<I', app_data.type))
    buffer.write(struct.pack('<I', app_data.version))
    buffer.write(struct.pack('<I', app_data.capabilities))
    buffer.write(struct.pack('<B', app_data.product_id))

    # Write any additional data if present
    if tag.tag_header.length > 13:
        remaining_data = tag.tag_data[13:]
        buffer.write(remaining_data)

    return buffer.getvalue()


def _generate_prog_data(tag: 'GblProg') -> bytes:
    """Generate data for program tag"""
    buffer = BytesIO()
    buffer.write(struct.pack('<I', tag.flash_start_address))
    buffer.write(tag.data)
    return buffer.getvalue()


def _generate_se_upgrade_data(tag: 'GblSeUpgrade') -> bytes:
    """Generate data for SE upgrade tag"""
    buffer = BytesIO()
    buffer.write(struct.pack('<I', tag.blob_size))
    buffer.write(struct.pack('<I', tag.version))
    buffer.write(tag.data)
    return buffer.getvalue()


def _generate_end_data(tag: 'GblEnd') -> bytes:
    """Generate data for end tag"""
    print(f"Found end tag {tag.tag_header.length}")
    buffer = BytesIO()
    buffer.write(struct.pack('<I', tag.gbl_crc))
    return buffer.getvalue()


def _generate_metadata_data(tag: 'GblMetadata') -> bytes:
    """Generate data for metadata tag"""
    return tag.meta_data


def _generate_prog_lz4_data(tag: 'GblProgLz4') -> bytes:
    """Generate data for LZ4 compressed program tag"""
    return tag.tag_data


def _generate_prog_lzma_data(tag: 'GblProgLzma') -> bytes:
    """Generate data for LZMA compressed program tag"""
    return tag.tag_data


def _generate_erase_prog_data(tag: 'GblEraseProg') -> bytes:
    """Generate data for erase program tag"""
    return tag.tag_data


def _generate_tag_delta_data(tag: 'GblTagDelta') -> bytes:
    """Generate data for tag delta"""
    buffer = BytesIO()
    buffer.write(struct.pack('<I', tag.new_crc))
    buffer.write(struct.pack('<I', tag.new_size))
    buffer.write(struct.pack('<I', tag.flash_addr))
    buffer.write(tag.data)
    return buffer.getvalue()


def _generate_certificate_ecdsa_p256_data(tag: 'GblCertificateEcdsaP256') -> bytes:
    """Generate data for ECDSA P256 certificate tag"""
    buffer = BytesIO()
    buffer.write(struct.pack('<B', tag.certificate.struct_version))
    buffer.write(struct.pack('<B', tag.certificate.flags))
    buffer.write(struct.pack('<B', tag.certificate.key))
    buffer.write(struct.pack('<I', tag.certificate.version))
    buffer.write(struct.pack('<B', tag.certificate.signature))
    return buffer.getvalue()


def _generate_signature_ecdsa_p256_data(tag: 'GblSignatureEcdsaP256') -> bytes:
    """Generate data for ECDSA P256 signature tag"""
    buffer = BytesIO()
    buffer.write(struct.pack('<B', tag.r))
    buffer.write(struct.pack('<B', tag.s))
    return buffer.getvalue()


def _generate_encryption_data(tag: 'GblEncryptionData') -> bytes:
    """Generate data for encryption data tag"""
    return tag.encrypted_gbl_data


def _generate_encryption_init_data(tag: 'GblEncryptionInitAesCcm') -> bytes:
    """Generate data for encryption init tag"""
    buffer = BytesIO()
    buffer.write(struct.pack('<I', tag.msg_len))
    buffer.write(struct.pack('<B', tag.nonce))
    return buffer.getvalue()


def _generate_version_dependency_data(tag: 'GblVersionDependency') -> bytes:
    """Generate data for version dependency tag"""
    buffer = BytesIO()
    buffer.write(struct.pack('<I', tag.image_type.value))
    buffer.write(struct.pack('<B', tag.statement))
    buffer.write(struct.pack('<H', tag.reversed))
    buffer.write(struct.pack('<I', tag.version))
    return buffer.getvalue()


def _calculate_total_size(tags: List['Tag']) -> int:
    """Calculate total size needed for all tags"""
    total_size = 0
    for tag in tags:
        if hasattr(tag, 'tag_header'):
            total_size += TAG_ID_SIZE + TAG_LENGTH_SIZE + tag.tag_header.length
    return total_size


def encode_tags_with_crc(tags: List['Tag'], include_crc: bool = False) -> bytes:
    """
    Encode tags with optional per-tag CRC

    Args:
        tags: List of tags to encode
        include_crc: Whether to include CRC for each tag

    Returns:
        bytes: Encoded data with optional CRCs
    """
    crc_size = 4 if include_crc else 0

    total_size = sum(
        TAG_ID_SIZE + TAG_LENGTH_SIZE + tag.tag_header.length + crc_size
        for tag in tags if hasattr(tag, 'tag_header')
    )

    buffer = BytesIO()
    file_crc = zlib.crc32(b'')

    for tag in tags:
        if not hasattr(tag, 'tag_header'):
            continue

        # Prepare tag components
        tag_id_bytes = struct.pack('<I', tag.tag_header.id)
        tag_length_bytes = struct.pack('<I', tag.tag_header.length)
        tag_data = generate_tag_data(tag)

        if include_crc:
            # Calculate CRC for this tag
            tag_crc = zlib.crc32(b'')
            tag_crc = zlib.crc32(tag_id_bytes, tag_crc)
            tag_crc = zlib.crc32(tag_length_bytes, tag_crc)
            tag_crc = zlib.crc32(tag_data, tag_crc)

            # Update file CRC
            file_crc = zlib.crc32(tag_id_bytes, file_crc)
            file_crc = zlib.crc32(tag_length_bytes, file_crc)
            file_crc = zlib.crc32(tag_data, file_crc)

            # Write tag with CRC
            buffer.write(tag_id_bytes)
            buffer.write(tag_length_bytes)
            buffer.write(tag_data)
            buffer.write(struct.pack('<I', tag_crc & 0xFFFFFFFF))
        else:
            # Write tag without CRC
            buffer.write(tag_id_bytes)
            buffer.write(tag_length_bytes)
            buffer.write(tag_data)

            # Update file CRC
            file_crc = zlib.crc32(tag_id_bytes, file_crc)
            file_crc = zlib.crc32(tag_length_bytes, file_crc)
            file_crc = zlib.crc32(tag_data, file_crc)

    result = buffer.getvalue()

    # If not including per-tag CRC, update the END tag CRC
    if not include_crc:
        # Find END tag position and update its CRC
        end_tag_index = -1
        for i, tag in enumerate(tags):
            if hasattr(tag, '__class__') and tag.__class__.__name__ == 'GblEnd':
                end_tag_index = i
                break

        if end_tag_index != -1:
            # Calculate position of CRC field in END tag
            end_tag_position = sum(
                TAG_ID_SIZE + TAG_LENGTH_SIZE + tag.tag_header.length
                for j, tag in enumerate(tags[:end_tag_index])
                if hasattr(tag, 'tag_header')
            )

            crc_position = end_tag_position + TAG_ID_SIZE + TAG_LENGTH_SIZE

            # Update CRC in the result
            result_array = bytearray(result)
            struct.pack_into('<I', result_array, crc_position, file_crc & 0xFFFFFFFF)
            result = bytes(result_array)

    return result


def create_end_tag_with_crc(tags: List['Tag']) -> 'GblEnd':
    """
    Create END tag with calculated CRC for all preceding tags

    Args:
        tags: List of tags to calculate CRC for

    Returns:
        GblEnd: END tag with calculated CRC
    """
    crc = zlib.crc32(b'')

    # Calculate CRC over all tag data
    for tag in tags:
        if not hasattr(tag, 'tag_header'):
            continue

        tag_id_bytes = struct.pack('<I', tag.tag_header.id)
        tag_length_bytes = struct.pack('<I', tag.tag_header.length)
        tag_data = generate_tag_data(tag)

        crc = zlib.crc32(tag_id_bytes, crc)
        crc = zlib.crc32(tag_length_bytes, crc)
        crc = zlib.crc32(tag_data, crc)

    # Include END tag header in CRC
    from tag.gbl_type import GblType  # Буде додано пізніше
    end_tag_id = GblType.END.value
    end_tag_length = TAG_LENGTH_SIZE

    end_tag_id_bytes = struct.pack('<I', end_tag_id)
    end_tag_length_bytes = struct.pack('<I', end_tag_length)

    crc = zlib.crc32(end_tag_id_bytes, crc)
    crc = zlib.crc32(end_tag_length_bytes, crc)

    # Prepare CRC value and data
    crc_value = crc & 0xFFFFFFFF
    crc_bytes = struct.pack('<I', crc_value)

    # Create END tag (імпорт буде додано пізніше)
    from tag.tag_header import TagHeader
    from tag.type.gbl_end import GblEnd

    return GblEnd(
        tag_header=TagHeader(
            id=end_tag_id,
            length=end_tag_length
        ),
        gbl_crc=crc_value,
        tag_data=crc_bytes
    )


def encode_tags_with_end_tag(tags: List['Tag']) -> bytes:
    """
    Encode tags and automatically add END tag with CRC

    Args:
        tags: List of tags to encode

    Returns:
        bytes: Complete GBL file with END tag
    """
    # Filter out existing END tags
    tags_without_end = [
        tag for tag in tags
        if not (hasattr(tag, '__class__') and tag.__class__.__name__ == 'GblEnd')
    ]

    # Create END tag with CRC
    end_tag = create_end_tag_with_crc(tags_without_end)

    # Combine and encode
    final_tags = tags_without_end + [end_tag]
    return encode_tags(final_tags)