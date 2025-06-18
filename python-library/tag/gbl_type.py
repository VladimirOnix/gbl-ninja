from enum import Enum
from typing import Optional


class GblType(Enum):
    HEADER_V3 = 0x03A617EB
    BOOTLOADER = 0xF50909F5
    APPLICATION = 0xF40A0AF4
    METADATA = 0xF60808F6
    PROG = 0xFE0101FE
    PROG_LZ4 = 0xFD0505FD
    PROG_LZMA = 0xFD0707FD
    ERASEPROG = 0xFD0303FD
    SE_UPGRADE = 0x5EA617EB
    END = 0xFC0404FC
    TAG = 0x00000000
    ENCRYPTION_DATA = 0xF90707F9
    ENCRYPTION_INIT = 0xFA0606FA
    SIGNATURE_ECDSA_P256 = 0xF70A0AF7
    CERTIFICATE_ECDSA_P256 = 0xF30B0BF3
    VERSION_DEPENDENCY = 0x76A617EB

    @classmethod
    def from_value(cls, value: int) -> Optional['GblType']:
        for gbl_type in cls:
            if gbl_type.value == value:
                return gbl_type
        return None
