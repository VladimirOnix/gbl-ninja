from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ..gbl_type import GblType
    from ..tag_header import TagHeader
    from ..tag import Tag


@dataclass
class GblHeader:
    """GBL Header tag (version 3)"""

    tag_header: 'TagHeader'
    version: int
    gbl_type: int
    tag_data: bytes

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ..gbl_type import GblType
        return GblType.HEADER_V3

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblHeader(
            tag_header=self.tag_header,
            version=self.version,
            gbl_type=self.gbl_type,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )