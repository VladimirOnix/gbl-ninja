"""
GBL Program tag implementation
Exact conversion from Kotlin GblProg.kt
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
class GblProg:
    """GBL Program tag"""

    tag_header: 'TagHeader'
    tag_type: 'GblType'
    flash_start_address: int  # UInt -> int
    data: bytes  # ByteArray -> bytes
    tag_data: bytes  # ByteArray -> bytes

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblProg(
            tag_header=self.tag_header,
            tag_type=self.tag_type,
            flash_start_address=self.flash_start_address,
            data=self.data,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )
