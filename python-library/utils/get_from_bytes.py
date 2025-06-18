"""
ByteBuffer utilities for reading from bytes
Exact conversion from Kotlin getFromBytes.kt
"""

import struct
from typing import Any


def get_from_bytes(byte_array: bytes, offset: int = 0, length: int = 4) -> Any:
    """
    Get ByteBuffer from bytes at given offset (little-endian)

    Args:
        byte_array: Source byte array
        offset: Starting position (default 0)
        length: Number of bytes to read (default 4)

    Returns:
        object: Object with methods to read data (mimics ByteBuffer)
    """
    if offset < 0 or offset + length > len(byte_array):
        raise ValueError(f"Invalid offset/length: offset={offset}, length={length}, array_size={len(byte_array)}")

    data = byte_array[offset:offset + length]

    class ByteBufferWrapper:
        def __init__(self, data: bytes):
            self.data = data

        @property
        def int(self) -> int:
            """Get as integer (little-endian)"""
            if len(self.data) == 1:
                return struct.unpack('<B', self.data)[0]
            elif len(self.data) == 2:
                return struct.unpack('<H', self.data)[0]
            elif len(self.data) == 4:
                return struct.unpack('<I', self.data)[0]
            elif len(self.data) == 8:
                return struct.unpack('<Q', self.data)[0]
            else:
                raise ValueError(f"Unsupported data length for int: {len(self.data)}")

    return ByteBufferWrapper(data)