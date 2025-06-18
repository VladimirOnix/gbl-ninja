from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ...gbl_type import GblType
    from ...tag_header import TagHeader
    from ...tag import Tag


@dataclass
class GblEncryptionData:
    """GBL Encryption Data tag"""

    tag_header: 'TagHeader'
    tag_data: bytes
    encrypted_gbl_data: bytes

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ...gbl_type import GblType
        return GblType.ENCRYPTION_DATA

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblEncryptionData(
            tag_header=self.tag_header,
            tag_data=self.tag_data,
            encrypted_gbl_data=self.encrypted_gbl_data
        )