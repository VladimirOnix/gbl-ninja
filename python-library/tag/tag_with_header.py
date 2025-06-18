"""
Tag with header interface
Exact conversion from Kotlin TagWithHeader.kt
"""

from abc import ABC, abstractmethod


# Імпорти з інших модулів (будуть додані пізніше):
# from .tag_header import TagHeader


class TagWithHeader(ABC):
    """Interface for tags that have a header"""

    @property
    @abstractmethod
    def tag_header(self) -> 'TagHeader':
        """Get tag header"""
        pass

    @property
    @abstractmethod
    def tag_data(self) -> bytes:
        """Get tag data"""
        pass
