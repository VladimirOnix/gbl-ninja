"""
Parse tag operation result types
Exact conversion from Kotlin ParseTagResult.kt
"""

from dataclasses import dataclass
from typing import Any, Optional


# Імпорти з інших модулів (будуть додані пізніше):
# from tag.tag_header import TagHeader


@dataclass
class ParseTagResult:
    """Result of tag parsing operation"""

    @dataclass
    class Success:
        """Successful tag parsing result"""
        tag_header: 'TagHeader'
        tag_data: bytes

    @dataclass
    class Fatal:
        """Failed tag parsing result"""
        error: Optional[Any] = None