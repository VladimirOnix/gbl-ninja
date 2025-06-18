
"""
Results module for GBL operation results

This module provides result types for all GBL operations including
parsing, encoding, and tag parsing operations.
"""

from .encode_result import EncodeResult
from .parse_tag_result import ParseTagResult
from .parse_result import ParseResult

__all__ = [
    'EncodeResult',
    'ParseTagResult',
    'ParseResult'
]