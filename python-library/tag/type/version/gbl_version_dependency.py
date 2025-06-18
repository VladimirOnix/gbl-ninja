"""
GBL Version Dependency tag implementation
Exact conversion from Kotlin GblVersionDependency.kt
"""

from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ...gbl_type import GblType
    from ...tag import Tag

from .image_type import ImageType


# Імпорти з інших модулів (будуть додані пізніше):
# from ...tag import Tag


@dataclass
class GblVersionDependency:
    """GBL Version Dependency tag"""

    tag_type: 'GblType'
    image_type: ImageType
    statement: int  # UByte -> int
    reversed: int  # UShort -> int
    version: int  # UInt -> int

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblVersionDependency(
            tag_type=self.tag_type,
            image_type=self.image_type,
            statement=self.statement,
            reversed=self.reversed,
            version=self.version
        )