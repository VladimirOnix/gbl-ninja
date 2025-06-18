"""
GBL Encryption Init AES CCM tag implementation
Exact conversion from Kotlin GblEncryptionInitAesCcm.kt
"""

from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ...gbl_type import GblType
    from ...tag_header import TagHeader
    from ...tag import Tag


# Імпорти з інших модулів (будуть додані пізніше):
# from ...tag import Tag
# from ...tag_with_header import TagWithHeader


@dataclass
class GblEncryptionInitAesCcm:
    """GBL Encryption Init AES CCM tag"""

    tag_header: 'TagHeader'
    tag_type: 'GblType'
    msg_len: int  # UInt -> int
    nonce: int  # UByte -> int
    tag_data: bytes

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblEncryptionInitAesCcm(
            tag_header=self.tag_header,
            tag_type=self.tag_type,
            tag_data=bytes(),  # Empty byte array як у Kotlin
            msg_len=self.msg_len,
            nonce=self.nonce
        )
