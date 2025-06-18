import struct
from enum import Enum


class ByteOrder(Enum):
    """Byte order enumeration"""
    LITTLE_ENDIAN = "little"
    BIG_ENDIAN = "big"


def uint_to_byte_array(value: int, order: ByteOrder = ByteOrder.LITTLE_ENDIAN) -> bytes:
    """
    Convert UInt to ByteArray

    Args:
        value: UInt value to convert
        order: Byte order (default LITTLE_ENDIAN)

    Returns:
        bytes: 4-byte array representation
    """
    if order == ByteOrder.LITTLE_ENDIAN:
        return struct.pack('<I', value)
    else:  # BIG_ENDIAN
        return struct.pack('>I', value)