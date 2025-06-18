from abc import ABC, abstractmethod
from typing import List
from .container_result import ContainerResult


class Container(ABC):
    """Container interface for tag management"""

    @abstractmethod
    def create(self) -> ContainerResult:
        """Create container with default tags"""
        pass

    @abstractmethod
    def add(self, tag: 'Tag') -> ContainerResult:
        """Add tag to container"""
        pass

    @abstractmethod
    def remove(self, tag: 'Tag') -> ContainerResult:
        """Remove tag from container"""
        pass

    @abstractmethod
    def build(self) -> ContainerResult:
        """Build ordered list of tags"""
        pass

    @abstractmethod
    def content(self) -> ContainerResult:
        """Export container content as bytes"""
        pass