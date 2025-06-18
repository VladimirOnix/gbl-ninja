"""
GBL Bootloader tag implementation
Exact conversion from Kotlin GblBootloader.kt
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
class GblBootloader:
    """GBL Bootloader tag"""

    tag_header: 'TagHeader'
    tag_type: 'GblType'
    bootloader_version: int
    address: int
    data: bytes
    tag_data: bytes

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblBootloader(
            tag_header=self.tag_header,
            tag_type=self.tag_type,
            bootloader_version=self.bootloader_version,
            address=self.address,
            data=self.data,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )