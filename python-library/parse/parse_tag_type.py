"""
Tag type parsing - converts tag headers and data to specific tag objects
Converted from Kotlin ParseTagType.kt
"""

import struct
from typing import Optional


# Імпорти з інших модулів (будуть додані пізніше):
# from tag.gbl_type import GblType
# from tag.tag import Tag
# from tag.tag_header import TagHeader
# from tag.type.gbl_header import GblHeader
# from tag.type.gbl_bootloader import GblBootloader
# from tag.type.gbl_application import GblApplication
# from tag.type.gbl_metadata import GblMetadata
# from tag.type.gbl_prog import GblProg
# from tag.type.gbl_prog_lz4 import GblProgLz4
# from tag.type.gbl_prog_lzma import GblProgLzma
# from tag.type.gbl_erase_prog import GblEraseProg
# from tag.type.gbl_se_upgrade import GblSeUpgrade
# from tag.type.gbl_end import GblEnd
# from tag.type.encryption.gbl_encryption_data import GblEncryptionData
# from tag.type.encryption.gbl_encryption_init_aes_ccm import GblEncryptionInitAesCcm
# from tag.type.certificate.gbl_signature_ecdsa_p256 import GblSignatureEcdsaP256
# from tag.type.certificate.gbl_certificate_ecdsa_p256 import GblCertificateEcdsaP256
# from tag.type.application.application_data import ApplicationData
# from tag.type.certificate.application_certificate import ApplicationCertificate
# from tag.default_tag import DefaultTag


def parse_tag_type(tag_id: int, length: int, byte_array: bytes) -> Optional['Tag']:
    """
    Parse byte array into specific tag type based on tag ID

    Args:
        tag_id: Tag identifier
        length: Tag data length
        byte_array: Tag data bytes

    Returns:
        Tag: Parsed tag object or None if parsing fails
    """
    # Імпорти будуть додані пізніше
    from tag.gbl_type import GblType
    from tag.tag_header import TagHeader

    gbl_type = GblType.from_value(tag_id)
    tag_header = TagHeader(id=tag_id, length=length)

    try:
        if gbl_type == GblType.HEADER_V3:
            return _parse_header_tag(tag_header, byte_array)

        elif gbl_type == GblType.BOOTLOADER:
            return _parse_bootloader_tag(tag_header, byte_array)

        elif gbl_type == GblType.APPLICATION:
            return _parse_application_tag(tag_header, byte_array)

        elif gbl_type == GblType.METADATA:
            return _parse_metadata_tag(tag_header, byte_array)

        elif gbl_type == GblType.PROG:
            return _parse_prog_tag(tag_header, byte_array)

        elif gbl_type == GblType.PROG_LZ4:
            return _parse_prog_lz4_tag(tag_header, byte_array)

        elif gbl_type == GblType.PROG_LZMA:
            return _parse_prog_lzma_tag(tag_header, byte_array)

        elif gbl_type == GblType.ERASEPROG:
            return _parse_erase_prog_tag(tag_header, byte_array)

        elif gbl_type == GblType.SE_UPGRADE:
            return _parse_se_upgrade_tag(tag_header, byte_array)

        elif gbl_type == GblType.END:
            return _parse_end_tag(tag_header, byte_array)

        elif gbl_type == GblType.ENCRYPTION_DATA:
            return _parse_encryption_data_tag(tag_header, byte_array)

        elif gbl_type == GblType.ENCRYPTION_INIT:
            return _parse_encryption_init_tag(tag_header, byte_array)

        elif gbl_type == GblType.SIGNATURE_ECDSA_P256:
            return _parse_signature_ecdsa_p256_tag(tag_header, byte_array)

        elif gbl_type == GblType.CERTIFICATE_ECDSA_P256:
            return _parse_certificate_ecdsa_p256_tag(tag_header, byte_array)

        elif gbl_type is None:
            return _parse_default_tag(tag_header, byte_array)

        else:
            return _parse_default_tag(tag_header, byte_array)

    except Exception as e:
        print(f"Error parsing tag type {gbl_type}: {str(e)}")
        return _parse_default_tag(tag_header, byte_array)


def _parse_header_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblHeader']:
    """Parse GBL header tag"""
    if len(byte_array) < 8:
        return None

    try:
        version = get_int_from_bytes(byte_array, offset=0, length=4)
        gbl_type = get_int_from_bytes(byte_array, offset=4, length=4)

        from tag.type.gbl_header import GblHeader
        return GblHeader(
            tag_header=tag_header,
            version=version,
            gbl_type=gbl_type,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_bootloader_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblBootloader']:
    """Parse bootloader tag"""
    if len(byte_array) < 8:
        return None

    try:
        bootloader_version = get_int_from_bytes(byte_array, offset=0, length=4)
        address = get_int_from_bytes(byte_array, offset=4, length=4)
        data = byte_array[8:]

        from tag.type.gbl_bootloader import GblBootloader
        return GblBootloader(
            tag_header=tag_header,
            bootloader_version=bootloader_version,
            address=address,
            data=data,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_application_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblApplication']:
    """Parse application tag"""
    if len(byte_array) < 13:
        return None

    try:
        app_type = get_int_from_bytes(byte_array, offset=0, length=4)
        version = get_int_from_bytes(byte_array, offset=4, length=4)
        capabilities = get_int_from_bytes(byte_array, offset=8, length=4)
        product_id = byte_array[12]

        from tag.type.application.application_data import ApplicationData
        from tag.type.gbl_application import GblApplication

        app_data = ApplicationData(
            type=app_type,
            version=version,
            capabilities=capabilities,
            product_id=product_id
        )

        return GblApplication(
            tag_header=tag_header,
            application_data=app_data,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_metadata_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblMetadata']:
    """Parse metadata tag"""
    try:
        from tag.type.gbl_metadata import GblMetadata
        return GblMetadata(
            tag_header=tag_header,
            meta_data=byte_array,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_prog_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblProg']:
    """Parse program data tag"""
    if len(byte_array) < 4:
        return None

    try:
        flash_start_address = get_int_from_bytes(byte_array, offset=0, length=4)
        data = byte_array[4:]

        from tag.type.gbl_prog import GblProg
        return GblProg(
            tag_header=tag_header,
            flash_start_address=flash_start_address,
            data=data,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_prog_lz4_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblProgLz4']:
    """Parse LZ4 compressed program tag"""
    try:
        from tag.type.gbl_prog_lz4 import GblProgLz4
        return GblProgLz4(
            tag_header=tag_header,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_prog_lzma_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblProgLzma']:
    """Parse LZMA compressed program tag"""
    try:
        from tag.type.gbl_prog_lzma import GblProgLzma
        return GblProgLzma(
            tag_header=tag_header,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_erase_prog_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblEraseProg']:
    """Parse erase program tag"""
    try:
        from tag.type.gbl_erase_prog import GblEraseProg
        return GblEraseProg(
            tag_header=tag_header,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_se_upgrade_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblSeUpgrade']:
    """Parse SE upgrade tag"""
    if len(byte_array) < 8:
        return None

    try:
        blob_size = get_int_from_bytes(byte_array, offset=0, length=4)
        version = get_int_from_bytes(byte_array, offset=4, length=4)
        data = byte_array[8:]

        from tag.type.gbl_se_upgrade import GblSeUpgrade
        return GblSeUpgrade(
            tag_header=tag_header,
            blob_size=blob_size,
            version=version,
            data=data,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_end_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblEnd']:
    """Parse end tag"""
    if len(byte_array) < 4:
        return None

    try:
        gbl_crc = get_int_from_bytes(byte_array, offset=0, length=4)

        from tag.type.gbl_end import GblEnd
        return GblEnd(
            tag_header=tag_header,
            gbl_crc=gbl_crc,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_encryption_data_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblEncryptionData']:
    """Parse encryption data tag"""
    if len(byte_array) < 8:
        return None

    try:
        encrypted_gbl_data = byte_array[8:]

        from tag.type.encryption.gbl_encryption_data import GblEncryptionData
        return GblEncryptionData(
            tag_header=tag_header,
            encrypted_gbl_data=encrypted_gbl_data,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_encryption_init_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblEncryptionInitAesCcm']:
    """Parse encryption init tag"""
    if len(byte_array) < 5:
        return None

    try:
        msg_len = get_int_from_bytes(byte_array, offset=0, length=4)
        nonce = byte_array[4]

        from tag.type.encryption.gbl_encryption_init_aes_ccm import GblEncryptionInitAesCcm
        return GblEncryptionInitAesCcm(
            tag_header=tag_header,
            msg_len=msg_len,
            nonce=nonce,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_signature_ecdsa_p256_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['GblSignatureEcdsaP256']:
    """Parse ECDSA P256 signature tag"""
    if len(byte_array) < 2:
        return None

    try:
        r = byte_array[0]
        s = byte_array[1]

        from tag.type.certificate.gbl_signature_ecdsa_p256 import GblSignatureEcdsaP256
        return GblSignatureEcdsaP256(
            tag_header=tag_header,
            r=r,
            s=s,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_certificate_ecdsa_p256_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional[
    'GblCertificateEcdsaP256']:
    """Parse ECDSA P256 certificate tag"""
    if len(byte_array) < 8:
        return None

    try:
        struct_version = byte_array[0]
        flags = byte_array[1]
        key = byte_array[2]
        version = get_int_from_bytes(byte_array, offset=3, length=4)
        signature = byte_array[7]

        from tag.type.certificate.application_certificate import ApplicationCertificate
        from tag.type.certificate.gbl_certificate_ecdsa_p256 import GblCertificateEcdsaP256

        certificate = ApplicationCertificate(
            struct_version=struct_version,
            flags=flags,
            key=key,
            version=version,
            signature=signature
        )

        return GblCertificateEcdsaP256(
            tag_header=tag_header,
            certificate=certificate,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_default_tag(tag_header: 'TagHeader', byte_array: bytes) -> Optional['DefaultTag']:
    """Parse unknown tag as default tag"""
    try:
        from tag.gbl_type import GblType
        from tag.default_tag import DefaultTag

        return DefaultTag(
            tag_header=tag_header,
            tag_type=GblType.TAG,
            tag_data=byte_array
        )
    except Exception:
        return None