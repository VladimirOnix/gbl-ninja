from abc import ABC, abstractmethod


class Tag(ABC):
    """Base tag interface"""

    @property
    @abstractmethod
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        pass

    @abstractmethod
    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        pass

    def content(self) -> bytes:
        """
        Get tag content as bytes

        Returns:
            bytes: Tag data content
        """
        # Імпорт всередині функції для уникнення циклічного імпорту
        from encode.encode_tags import generate_tag_data

        tag_data = generate_tag_data(self)
        return tag_data