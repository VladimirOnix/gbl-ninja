from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ..gbl_type import GblType
    from ..tag_header import TagHeader
    from ..tag import Tag


@dataclass
class GblProg:
    """GBL Program tag"""

    tag_header: 'TagHeader'
    flash_start_address: int  # UInt -> int
    data: bytes  # ByteArray -> bytes
    tag_data: bytes  # ByteArray -> bytes

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ..gbl_type import GblType
        return GblType.PROG

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblProg(
            tag_header=self.tag_header,
            flash_start_address=self.flash_start_address,
            data=self.data,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )