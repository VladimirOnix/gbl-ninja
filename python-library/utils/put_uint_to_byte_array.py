"""
Put UInt to ByteArray utility
Exact conversion from Kotlin putUIntToByteArray.kt
"""

import struct


def put_uint_to_byte_array(array: bytearray, offset: int, value: int) -> None:
    """
    Put UInt value into byte array at given offset (little-endian)

    Args:
        array: Target byte array (mutable)
        offset: Position to write to
        value: UInt value to write
    """
    if offset < 0 or offset + 4 > len(array):
        raise ValueError(f"Invalid offset: offset={offset}, array_size={len(array)}")

    # Pack as little-endian 32-bit unsigned integer
    packed = struct.pack('<I', value)
    array[offset:offset + 4] = packed
