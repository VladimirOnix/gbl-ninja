from abc import ABC, abstractmethod


from .tag_header import TagHeader

class TagWithHeader(ABC):

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
