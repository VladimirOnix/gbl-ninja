from enum import Enum


class ImageType(Enum):
    APPLICATION = 0x01
    BOOTLOADER = 0x02
    SE = 0x03

    def from_value(self, value: int) -> 'ImageType':
        for image_type in ImageType:
            if image_type.value == value:
                return image_type
        raise ValueError(f"No ImageType found for value: {value}")