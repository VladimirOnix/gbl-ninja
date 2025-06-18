from dataclasses import dataclass


@dataclass
class ApplicationCertificate:
    """Application certificate structure"""

    struct_version: int  # UByte -> int
    flags: int  # UByte -> int
    key: int  # UByte -> int
    version: int  # UInt -> int
    signature: int  # UByte -> int

