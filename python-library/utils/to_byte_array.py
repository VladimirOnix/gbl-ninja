"""
UInt to ByteArray conversion utility
Exact conversion from Kotlin toByteArray.kt
"""

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


# Alternative: Add as method to int (monkey patching)
# This mimics Kotlin extension function behavior
def _to_byte_array_method(self, order: ByteOrder = ByteOrder.LITTLE_ENDIAN) -> bytes:
    """Convert int to byte array"""
    return uint_to_byte_array(self, order)


# Add method to int class
int.to_byte_array = _to_byte_array_method

