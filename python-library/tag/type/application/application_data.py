import struct
from dataclasses import dataclass


@dataclass
class ApplicationData:
    """Application tag data structure"""

    type: int
    version: int
    capabilities: int
    product_id: int

    # Constants як у Kotlin companion object
    APP_TYPE: int = 32
    APP_VERSION: int = 5
    APP_CAPABILITIES: int = 0
    APP_PRODUCT_ID: int = 54

    def content(self) -> bytes:
        """
        Convert to bytes (little-endian)

        Returns:
            bytes: 13-byte array (4+4+4+1 bytes)
        """
        # Pack as little-endian: 3 x UInt (4 bytes) + 1 x UByte (1 byte)
        return struct.pack('<IIIB', self.type, self.version, self.capabilities, self.product_id)
