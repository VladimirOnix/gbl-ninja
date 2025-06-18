"""
Tag header structure
Exact conversion from Kotlin TagHeader.kt
"""

import struct
from dataclasses import dataclass


@dataclass
class TagHeader:
    """Tag header containing ID and length"""

    id: int
    length: int

    def content(self) -> bytes:
        """
        Convert tag header to bytes (little-endian)

        Returns:
            bytes: 8-byte header (4 bytes ID + 4 bytes length)
        """
        # Pack as little-endian: 4-byte ID + 4-byte length
        return struct.pack('<II', self.id, self.length)