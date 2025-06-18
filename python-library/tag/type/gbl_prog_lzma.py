"""
GBL Program LZMA tag implementation
Exact conversion from Kotlin GblProgLzma.kt
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
class GblProgLzma:
    """GBL Program LZMA compressed tag"""

    tag_header: 'TagHeader'
    tag_type: 'GblType'
    tag_data: bytes  # ByteArray -> bytes

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblProgLzma(
            tag_header=self.tag_header,
            tag_type=self.tag_type,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )