"""
Tag parsing functionality - converts bytes to tag headers
Converted from Kotlin ParseTag.kt
"""

import struct
from typing import Tuple, Union
from dataclasses import dataclass

# Імпорти з інших модулів (будуть додані пізніше):
# from tag.tag_header import TagHeader
# from results.parse_tag_result import ParseTagResult


# Constants
TAG_ID_SIZE = 4
TAG_LENGTH_SIZE = 4


@dataclass
class ParseTagResult:
    """Result of tag parsing operation"""

    @dataclass
    class Success:
        tag_header: 'TagHeader'
        tag_data: bytes

        def __bool__(self):
            return True

    @dataclass
    class Fatal:
        error: str

        def __bool__(self):
            return False


def parse_tag(byte_array: bytes, offset: int = 0) -> Union[ParseTagResult.Success, ParseTagResult.Fatal]:
    """
    Parse a single tag from byte array starting at offset

    Args:
        byte_array: Input byte array containing GBL data
        offset: Starting position in the array

    Returns:
        ParseTagResult: Success with tag header and data, or Fatal with error
    """
    # Validate offset and minimum size
    if offset < 0 or offset + TAG_ID_SIZE + TAG_LENGTH_SIZE > len(byte_array):
        return ParseTagResult.Fatal(f"Invalid offset: {offset}")

    try:
        # Extract tag ID (little-endian)
        tag_id = struct.unpack('<I', byte_array[offset:offset + TAG_ID_SIZE])[0]

        # Extract tag length (little-endian)
        tag_length = struct.unpack('<I', byte_array[offset + TAG_ID_SIZE:offset + TAG_ID_SIZE + TAG_LENGTH_SIZE])[0]

        # Validate tag length
        if offset + TAG_ID_SIZE + TAG_LENGTH_SIZE + tag_length > len(byte_array):
            return ParseTagResult.Fatal(f"Invalid tag length: {tag_length}")

        # Extract tag data
        data_start = offset + TAG_ID_SIZE + TAG_LENGTH_SIZE
        data_end = data_start + tag_length
        tag_data = byte_array[data_start:data_end]

        # Create tag header (імпорт буде додано пізніше)
        from tag.tag_header import TagHeader
        tag_header = TagHeader(id=tag_id, length=tag_length)

        return ParseTagResult.Success(
            tag_header=tag_header,
            tag_data=tag_data
        )

    except struct.error as e:
        return ParseTagResult.Fatal(f"Struct unpacking error: {str(e)}")
    except Exception as e:
        return ParseTagResult.Fatal(f"Unexpected error: {str(e)}")


def get_int_from_bytes(byte_array: bytes, offset: int = 0, length: int = 4) -> int:
    """
    Extract integer from bytes at given offset (little-endian)

    Args:
        byte_array: Source byte array
        offset: Starting position
        length: Number of bytes to read (default 4)

    Returns:
        int: Extracted integer value
    """
    if offset + length > len(byte_array):
        raise ValueError(f"Not enough bytes: need {length} at offset {offset}, have {len(byte_array)}")

    data = byte_array[offset:offset + length]

    if length == 1:
        return struct.unpack('<B', data)[0]
    elif length == 2:
        return struct.unpack('<H', data)[0]
    elif length == 4:
        return struct.unpack('<I', data)[0]
    elif length == 8:
        return struct.unpack('<Q', data)[0]
    else:
        raise ValueError(f"Unsupported length: {length}")