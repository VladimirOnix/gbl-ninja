from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ..gbl_type import GblType
    from ..tag_header import TagHeader
    from ..tag import Tag


@dataclass
class GblSeUpgrade:
    """GBL SE Upgrade tag"""

    tag_header: 'TagHeader'
    blob_size: int  # UInt -> int
    version: int  # UInt -> int
    data: bytes  # ByteArray -> bytes
    tag_data: bytes  # ByteArray -> bytes

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ..gbl_type import GblType
        return GblType.SE_UPGRADE

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblSeUpgrade(
            tag_header=self.tag_header,
            blob_size=self.blob_size,
            version=self.version,
            data=self.data,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )