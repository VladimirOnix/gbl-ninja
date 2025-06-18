"""
Image type enumeration
Exact conversion from Kotlin ImageType.kt
"""

from enum import Enum


class ImageType(Enum):
    """Image type enumeration"""

    APPLICATION = 0x01
    BOOTLOADER = 0x02
    SE = 0x03

    def from_value(self, value: int) -> 'ImageType':
        """
        Get ImageType from value

        Args:
            value: Integer value to search for

        Returns:
            ImageType: Matching image type
        """
        for image_type in ImageType:
            if image_type.value == value:
                return image_type
        raise ValueError(f"No ImageType found for value: {value}")