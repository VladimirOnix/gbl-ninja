"""
Parser status and result types
Exact conversion from Kotlin ParserStatus.kt
"""

from dataclasses import dataclass
from typing import List, Any, Optional


# Імпорти з інших модулів (будуть додані пізніше):
# from tag.tag import Tag


@dataclass
class ParseResult:
    """Result of GBL file parsing operation"""

    @dataclass
    class Success:
        """Successful parsing result"""
        result_list: List['Tag']

    @dataclass
    class Fatal:
        """Failed parsing result"""
        error: Optional[Any] = None