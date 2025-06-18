from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ...gbl_type import GblType
    from ...tag_header import TagHeader
    from ...tag import Tag


@dataclass
class GblEncryptionInitAesCcm:
    """GBL Encryption Init AES CCM tag"""

    tag_header: 'TagHeader'
    msg_len: int  # UInt -> int
    nonce: int  # UByte -> int
    tag_data: bytes

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ...gbl_type import GblType
        return GblType.ENCRYPTION_INIT

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblEncryptionInitAesCcm(
            tag_header=self.tag_header,
            tag_data=bytes(),  # Empty byte array як у Kotlin
            msg_len=self.msg_len,
            nonce=self.nonce
        )