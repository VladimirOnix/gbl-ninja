from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ..gbl_type import GblType
    from ..tag_header import TagHeader
    from ..tag import Tag


@dataclass
class GblMetadata:
    """GBL Metadata tag"""

    tag_header: 'TagHeader'
    meta_data: bytes
    tag_data: bytes

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ..gbl_type import GblType
        return GblType.METADATA

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblMetadata(
            tag_header=self.tag_header,
            meta_data=self.meta_data,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )