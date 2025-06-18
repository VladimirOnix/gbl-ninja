from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ..gbl_type import GblType
    from ..tag_header import TagHeader
    from ..tag import Tag


@dataclass
class GblEraseProg:
    """GBL Erase Program tag"""

    tag_header: 'TagHeader'
    tag_data: bytes

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ..gbl_type import GblType
        return GblType.ERASEPROG

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblEraseProg(
            tag_header=self.tag_header,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )