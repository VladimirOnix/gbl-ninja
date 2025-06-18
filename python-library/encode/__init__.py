
"""
Encode module for GBL file generation

This module provides functionality for encoding GBL tags into binary format,
including CRC calculation and tag-specific data generation.
"""

from .encode_tags import (
    encode_tags,
    generate_tag_data,
    encode_tags_with_crc,
    create_end_tag_with_crc,
    encode_tags_with_end_tag,
    TAG_ID_SIZE,
    TAG_LENGTH_SIZE
)

__all__ = [
    'encode_tags',
    'generate_tag_data',
    'encode_tags_with_crc',
    'create_end_tag_with_crc',
    'encode_tags_with_end_tag',
    'TAG_ID_SIZE',
    'TAG_LENGTH_SIZE'
]