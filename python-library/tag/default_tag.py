from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from .gbl_type import GblType
    from .tag_header import TagHeader
    from .tag import Tag


@dataclass
class DefaultTag:
    """Default tag implementation for unknown tag types"""

    tag_data: bytes
    tag_header: 'TagHeader'
    _tag_type: 'GblType'

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        return self._tag_type

    def copy(self) -> 'DefaultTag':
        """Create a copy of the tag"""
        return DefaultTag(
            tag_data=bytes(),  # Empty byte array як у Kotlin
            tag_header=self.tag_header,
            _tag_type=self._tag_type
        )

    def __eq__(self, other) -> bool:
        """Equality comparison"""
        if not isinstance(other, DefaultTag):
            return False

        return (self.tag_data == other.tag_data and
                self.tag_header == other.tag_header and
                self._tag_type == other._tag_type)

    def __hash__(self) -> int:
        """Hash implementation"""
        return hash((self.tag_data, self.tag_header, self._tag_type))