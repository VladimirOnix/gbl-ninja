from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ..gbl_type import GblType
    from ..tag_header import TagHeader
    from ..tag import Tag


@dataclass
class GblEnd:
    """GBL End tag with CRC"""

    tag_header: 'TagHeader'
    gbl_crc: int
    tag_data: bytes

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ..gbl_type import GblType
        return GblType.END

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblEnd(
            tag_header=self.tag_header,
            gbl_crc=self.gbl_crc,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )

    def __eq__(self, other) -> bool:
        """Equality comparison"""
        if not isinstance(other, GblEnd):
            return False

        return (self.tag_header == other.tag_header and
                self.tag_type == other.tag_type and
                self.gbl_crc == other.gbl_crc and
                self.tag_data == other.tag_data)

    def __hash__(self) -> int:
        """Hash implementation"""
        return hash((self.tag_header, self.tag_type, self.gbl_crc, self.tag_data))