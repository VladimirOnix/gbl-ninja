from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ...gbl_type import GblType
    from ...tag_header import TagHeader
    from ...tag import Tag

from .application_data import ApplicationData


@dataclass
class GblApplication:
    """GBL Application tag"""

    tag_header: 'TagHeader'
    application_data: ApplicationData
    tag_data: bytes

    # Constant як у Kotlin companion object
    APP_LENGTH: int = 13

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ...gbl_type import GblType
        return GblType.APPLICATION

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblApplication(
            tag_header=self.tag_header,
            application_data=self.application_data,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )