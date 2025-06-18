import struct
from typing import Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from tag.gbl_type import GblType
    from tag.tag import Tag
    from tag.tag_header import TagHeader


def get_int_from_bytes(byte_array: bytes, offset: int = 0, length: int = 4) -> int:
    """
    Extract integer from bytes at given offset (little-endian)
    """
    if offset + length > len(byte_array):
        raise ValueError(f"Not enough bytes: need {length} at offset {offset}, have {len(byte_array)}")

    data = byte_array[offset:offset + length]

    if length == 1:
        return struct.unpack('<B', data)[0]
    elif length == 2:
        return struct.unpack('<H', data)[0]
    elif length == 4:
        return struct.unpack('<I', data)[0]
    elif length == 8:
        return struct.unpack('<Q', data)[0]
    else:
        raise ValueError(f"Unsupported length: {length}")


def parse_tag_type(tag_id: int, length: int, byte_array: bytes) -> Optional['Tag']:
    """
    Parse byte array into specific tag type based on tag ID
    """
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


def _parse_header_tag(tag_header, byte_array: bytes):
    """Parse GBL header tag"""
    if len(byte_array) < 8:
        return None

    try:
        version = get_int_from_bytes(byte_array, offset=0, length=4)
        gbl_type = get_int_from_bytes(byte_array, offset=4, length=4)

        from tag.type.gbl_header import GblHeader
        from tag.gbl_type import GblType

        return GblHeader(
            tag_header=tag_header,
            tag_type=GblType.HEADER_V3,
            version=version,
            gbl_type=gbl_type,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_bootloader_tag(tag_header, byte_array: bytes):
    """Parse bootloader tag"""
    if len(byte_array) < 8:
        return None

    try:
        bootloader_version = get_int_from_bytes(byte_array, offset=0, length=4)
        address = get_int_from_bytes(byte_array, offset=4, length=4)
        data = byte_array[8:]

        from tag.type.gbl_bootloader import GblBootloader
        from tag.gbl_type import GblType

        return GblBootloader(
            tag_header=tag_header,
            tag_type=GblType.BOOTLOADER,
            bootloader_version=bootloader_version,
            address=address,
            data=data,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_application_tag(tag_header, byte_array: bytes):
    """Parse application tag"""
    if len(byte_array) < 13:
        return None

    try:
        app_type = get_int_from_bytes(byte_array, offset=0, length=4)
        version = get_int_from_bytes(byte_array, offset=4, length=4)
        capabilities = get_int_from_bytes(byte_array, offset=8, length=4)
        product_id = byte_array[12]

        from tag.type.application.application_data import ApplicationData
        from tag.type.application.gbl_application import GblApplication

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


def _parse_metadata_tag(tag_header, byte_array: bytes):
    """Parse metadata tag"""
    try:
        from tag.type.gbl_metadata import GblMetadata
        from tag.gbl_type import GblType

        return GblMetadata(
            tag_header=tag_header,
            tag_type=GblType.METADATA,
            meta_data=byte_array,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_prog_tag(tag_header, byte_array: bytes):
    """Parse program data tag"""
    if len(byte_array) < 4:
        return None

    try:
        flash_start_address = get_int_from_bytes(byte_array, offset=0, length=4)
        data = byte_array[4:]

        from tag.type.gbl_prog import GblProg
        from tag.gbl_type import GblType

        return GblProg(
            tag_header=tag_header,
            tag_type=GblType.PROG,
            flash_start_address=flash_start_address,
            data=data,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_prog_lz4_tag(tag_header, byte_array: bytes):
    """Parse LZ4 compressed program tag"""
    try:
        from tag.type.gbl_prog_lz4 import GblProgLz4
        from tag.gbl_type import GblType

        return GblProgLz4(
            tag_header=tag_header,
            tag_type=GblType.PROG_LZ4,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_prog_lzma_tag(tag_header, byte_array: bytes):
    """Parse LZMA compressed program tag"""
    try:
        from tag.type.gbl_prog_lzma import GblProgLzma
        from tag.gbl_type import GblType

        return GblProgLzma(
            tag_header=tag_header,
            tag_type=GblType.PROG_LZMA,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_erase_prog_tag(tag_header, byte_array: bytes):
    """Parse erase program tag"""
    try:
        from tag.type.gbl_erase_prog import GblEraseProg
        from tag.gbl_type import GblType

        return GblEraseProg(
            tag_header=tag_header,
            tag_type=GblType.ERASEPROG,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_se_upgrade_tag(tag_header, byte_array: bytes):
    """Parse SE upgrade tag"""
    if len(byte_array) < 8:
        return None

    try:
        blob_size = get_int_from_bytes(byte_array, offset=0, length=4)
        version = get_int_from_bytes(byte_array, offset=4, length=4)
        data = byte_array[8:]

        from tag.type.gbl_se_upgrade import GblSeUpgrade
        from tag.gbl_type import GblType

        return GblSeUpgrade(
            tag_header=tag_header,
            tag_type=GblType.SE_UPGRADE,
            blob_size=blob_size,
            version=version,
            data=data,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_end_tag(tag_header, byte_array: bytes):
    """Parse end tag"""
    if len(byte_array) < 4:
        return None

    try:
        gbl_crc = get_int_from_bytes(byte_array, offset=0, length=4)

        from tag.type.gbl_end import GblEnd
        from tag.gbl_type import GblType

        return GblEnd(
            tag_header=tag_header,
            tag_type=GblType.END,
            gbl_crc=gbl_crc,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_encryption_data_tag(tag_header, byte_array: bytes):
    """Parse encryption data tag"""
    try:
        from tag.type.encryption.gbl_encryption_data import GblEncryptionData
        from tag.gbl_type import GblType

        return GblEncryptionData(
            tag_header=tag_header,
            tag_type=GblType.ENCRYPTION_DATA,
            encrypted_gbl_data=byte_array,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_encryption_init_tag(tag_header, byte_array: bytes):
    """Parse encryption init tag"""
    if len(byte_array) < 5:
        return None

    try:
        msg_len = get_int_from_bytes(byte_array, offset=0, length=4)
        nonce = byte_array[4]

        from tag.type.encryption.gbl_encryption_init_aes_ccm import GblEncryptionInitAesCcm
        from tag.gbl_type import GblType

        return GblEncryptionInitAesCcm(
            tag_header=tag_header,
            tag_type=GblType.ENCRYPTION_INIT,
            msg_len=msg_len,
            nonce=nonce,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_signature_ecdsa_p256_tag(tag_header, byte_array: bytes):
    """Parse ECDSA P256 signature tag"""
    if len(byte_array) < 2:
        return None

    try:
        r = byte_array[0]
        s = byte_array[1]

        from tag.type.certificate.gbl_signature_ecdsa_p256 import GblSignatureEcdsaP256
        from tag.gbl_type import GblType

        return GblSignatureEcdsaP256(
            tag_header=tag_header,
            tag_type=GblType.SIGNATURE_ECDSA_P256,
            r=r,
            s=s,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_certificate_ecdsa_p256_tag(tag_header, byte_array: bytes):
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
        from tag.gbl_type import GblType

        certificate = ApplicationCertificate(
            struct_version=struct_version,
            flags=flags,
            key=key,
            version=version,
            signature=signature
        )

        return GblCertificateEcdsaP256(
            tag_header=tag_header,
            tag_type=GblType.CERTIFICATE_ECDSA_P256,
            certificate=certificate,
            tag_data=byte_array
        )
    except Exception:
        return None


def _parse_default_tag(tag_header, byte_array: bytes):
    """Parse unknown tag as default tag"""
    try:
        from tag.gbl_type import GblType
        from tag.default_tag import DefaultTag

        return DefaultTag(
            tag_header=tag_header,
            _tag_type=GblType.TAG,
            tag_data=byte_array
        )
    except Exception:
        return None