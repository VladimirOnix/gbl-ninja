"""
GBL End tag implementation
Exact conversion from Kotlin GblEnd.kt
"""

from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ..gbl_type import GblType
    from ..tag_header import TagHeader
    from ..tag import Tag


# Імпорти з інших модулів (будуть додані пізніше):
# from ..tag import Tag
# from ..tag_with_header import TagWithHeader


@dataclass
class GblEnd:
    """GBL End tag with CRC"""

    tag_header: 'TagHeader'
    tag_type: 'GblType'
    gbl_crc: int
    tag_data: bytes

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblEnd(
            tag_header=self.tag_header,
            tag_type=self.tag_type,
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
