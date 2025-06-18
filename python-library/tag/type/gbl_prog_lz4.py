from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ..gbl_type import GblType
    from ..tag_header import TagHeader
    from ..tag import Tag


@dataclass
class GblProgLz4:
    """GBL Program LZ4 compressed tag"""

    tag_header: 'TagHeader'
    tag_data: bytes  # ByteArray -> bytes

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ..gbl_type import GblType
        return GblType.PROG_LZ4

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblProgLz4(
            tag_header=self.tag_header,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )