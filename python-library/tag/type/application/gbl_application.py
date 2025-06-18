from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ...gbl_type import GblType
    from ...tag_header import TagHeader
    from ...tag import Tag

from .application_data import ApplicationData


# Імпорти з інших модулів (будуть додані пізніше):
# from ...tag import Tag
# from ...tag_with_header import TagWithHeader


@dataclass
class GblApplication:
    """GBL Application tag"""

    tag_header: 'TagHeader'
    tag_type: 'GblType'
    application_data: ApplicationData
    tag_data: bytes

    # Constant як у Kotlin companion object
    APP_LENGTH: int = 13

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblApplication(
            tag_header=self.tag_header,
            tag_type=self.tag_type,
            application_data=self.application_data,
            tag_data=bytes()  # Empty byte array як у Kotlin
        )