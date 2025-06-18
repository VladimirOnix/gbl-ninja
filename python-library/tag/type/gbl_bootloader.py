from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ..gbl_type import GblType
    from ..tag_header import TagHeader
    from ..tag import Tag


@dataclass
class GblBootloader:
    """GBL Bootloader tag"""

    tag_header: 'TagHeader'
    bootloader_version: int
    address: int
    data: bytes
    tag_data: bytes

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ..gbl_type import GblType
        return GblType.BOOTLOADER

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblBootloader(
            tag_header=self.tag_header,
            bootloader_version=self.bootloader_version,
            address=self.address,
            data=self.data,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )