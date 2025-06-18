from typing import Set, List, Optional
import struct
import zlib

from .container import Container
from .container_result import ContainerResult
from .container_error_code import ContainerErrorCode


# Ці імпорти мають бути з інших модулів проекту:
# from tag.gbl_type import GblType
# from tag.tag import Tag
# from tag.tag_header import TagHeader
# from tag.type.gbl_header import GblHeader
# from tag.type.gbl_end import GblEnd


class TagContainer(Container):
    """Main tag container implementation"""

    # Constants
    GBL_TAG_ID_HEADER_V3 = 0x03A617EB
    HEADER_SIZE = 8
    HEADER_VERSION = 50331648
    HEADER_GBL_TYPE = 0

    # Це має бути імпортовано з GblType
    # PROTECTED_TAG_TYPES = {GblType.HEADER_V3, GblType.END}

    def __init__(self):
        self._content: Set['Tag'] = set()
        self._is_created = False

    def create(self) -> ContainerResult:
        """Create container with header and end tags"""
        try:
            if self._is_created:
                return ContainerResult.Success(None)

            self._content.clear()

            # Create header tag
            header_tag = self._create_header_tag()
            self._content.add(header_tag)

            # Create end tag
            end_tag = self._create_end_tag()
            self._content.add(end_tag)

            self._is_created = True
            return ContainerResult.Success(None)

        except Exception as e:
            return ContainerResult.Error(
                f"Failed to create container: {str(e)}",
                ContainerErrorCode.INTERNAL_ERROR
            )

    def add(self, tag: 'Tag') -> ContainerResult:
        """Add tag to container"""
        try:
            if not self._is_created:
                return ContainerResult.Error(
                    "Container must be created before adding tags. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                )

            if self._is_protected_tag(tag):
                return ContainerResult.Error(
                    f"Cannot add protected tag: {tag.tag_type}. Protected tags are managed automatically.",
                    ContainerErrorCode.PROTECTED_TAG_VIOLATION
                )

            self._content.add(tag)
            return ContainerResult.Success(None)

        except Exception as e:
            return ContainerResult.Error(
                f"Failed to add tag: {str(e)}",
                ContainerErrorCode.INTERNAL_ERROR
            )

    def remove(self, tag: 'Tag') -> ContainerResult:
        """Remove tag from container"""
        try:
            if not self._is_created:
                return ContainerResult.Error(
                    "Container must be created before removing tags. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                )

            if self._is_protected_tag(tag):
                return ContainerResult.Error(
                    f"Cannot remove protected tag: {tag.tag_type}. Protected tags are managed automatically.",
                    ContainerErrorCode.PROTECTED_TAG_VIOLATION
                )

            if tag not in self._content:
                return ContainerResult.Error(
                    f"Tag not found in container: {tag.tag_type}",
                    ContainerErrorCode.TAG_NOT_FOUND
                )

            self._content.remove(tag)
            return ContainerResult.Success(None)

        except Exception as e:
            return ContainerResult.Error(
                f"Failed to remove tag: {str(e)}",
                ContainerErrorCode.INTERNAL_ERROR
            )

    def build(self) -> ContainerResult:
        """Build ordered list of tags"""
        try:
            if not self._is_created:
                return ContainerResult.Error(
                    "Container must be created before building. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                )

            sorted_tags = []

            # Add header tag first
            header_tag = self._find_tag_by_type(GblType.HEADER_V3)
            if header_tag:
                sorted_tags.append(header_tag)

            # Add non-protected tags sorted by tag type value
            non_protected_tags = [
                tag for tag in self._content
                if tag.tag_type not in self.PROTECTED_TAG_TYPES
            ]
            non_protected_tags.sort(key=lambda tag: tag.tag_type.value)
            sorted_tags.extend(non_protected_tags)

            # Add end tag last
            end_tag = self._find_tag_by_type(GblType.END)
            if end_tag:
                sorted_tags.append(end_tag)

            return ContainerResult.Success(sorted_tags)

        except Exception as e:
            return ContainerResult.Error(
                f"Failed to build container: {str(e)}",
                ContainerErrorCode.INTERNAL_ERROR
            )

    def content(self) -> ContainerResult:
        """Export container content as bytes"""
        try:
            if not self._is_created:
                return ContainerResult.Error(
                    "Container must be created before exporting content. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                )

            build_result = self.build()
            if isinstance(build_result, ContainerResult.Error):
                return ContainerResult.Error(
                    f"Failed to build tags for content export: {build_result.message}",
                    build_result.code
                )

            tags = build_result.data
            tags_without_end = [tag for tag in tags if not isinstance(tag, GblEnd)]

            # Create new end tag with calculated CRC
            end_tag = self._create_end_tag_with_crc(tags_without_end)
            final_tags = tags_without_end + [end_tag]

            # Encode tags to bytes
            byte_array = self._encode_tags(final_tags)
            return ContainerResult.Success(byte_array)

        except Exception as e:
            return ContainerResult.Error(
                f"Failed to export container content: {str(e)}",
                ContainerErrorCode.INTERNAL_ERROR
            )

        # ===============================
        # Query Methods
        # ===============================

        def has_tag(self, tag_type: 'GblType') -> bool:
            """Check if container has tag of specific type"""
            if not self._is_created:
                return False
            return any(tag.tag_type == tag_type for tag in self._content)

        def get_tag(self, tag_type: 'GblType') -> Optional['Tag']:
            """Get first tag of specific type"""
            if not self._is_created:
                return None
            return next((tag for tag in self._content if tag.tag_type == tag_type), None)

        def get_all_tags(self, tag_type: 'GblType') -> List['Tag']:
            """Get all tags of specific type"""
            if not self._is_created:
                return []
            return [tag for tag in self._content if tag.tag_type == tag_type]

        def is_empty(self) -> bool:
            """Check if container is empty (only protected tags)"""
            if not self._is_created:
                return True
            return len(self._content) == len(self.PROTECTED_TAG_TYPES)

        def size(self) -> int:
            """Get number of tags in container"""
            if not self._is_created:
                return 0
            return len(self._content)

        def get_tag_types(self) -> Set['GblType']:
            """Get set of all tag types in container"""
            if not self._is_created:
                return set()
            return {tag.tag_type for tag in self._content}

        def is_created(self) -> bool:
            """Check if container is created"""
            return self._is_created

        def clear(self) -> ContainerResult:
            """Clear all non-protected tags"""
            try:
                if not self._is_created:
                    return ContainerResult.Error(
                        "Container must be created before clearing. Call create() first.",
                        ContainerErrorCode.CONTAINER_NOT_CREATED
                    )

                # Remove all non-protected tags
                self._content = {tag for tag in self._content if self._is_protected_tag(tag)}
                return ContainerResult.Success(None)

            except Exception as e:
                return ContainerResult.Error(
                    f"Failed to clear container: {str(e)}",
                    ContainerErrorCode.INTERNAL_ERROR
                )

        # ===============================
        # Private Helper Methods
        # ===============================

        def _is_protected_tag(self, tag: 'Tag') -> bool:
            """Check if tag is protected"""
            return tag.tag_type in self.PROTECTED_TAG_TYPES

        def _find_tag_by_type(self, tag_type: 'GblType') -> Optional['Tag']:
            """Find first tag by type"""
            return next((tag for tag in self._content if tag.tag_type == tag_type), None)

        def _create_header_tag(self) -> 'GblHeader':
            """Create header tag"""
            header = GblHeader(
                tag_header=TagHeader(
                    id=self.GBL_TAG_ID_HEADER_V3,
                    length=self.HEADER_SIZE
                ),
                version=self.HEADER_VERSION,
                gbl_type=self.HEADER_GBL_TYPE,
                tag_data=bytes(0)
            )

            # Set tag data to the content
            header.tag_data = header.content()
            return header

        def _create_end_tag(self) -> 'GblEnd':
            """Create empty end tag"""
            return GblEnd(
                tag_header=TagHeader(
                    id=GblType.END.value,
                    length=0
                ),
                gbl_crc=0,
                tag_data=bytes(0)
            )

        def _create_end_tag_with_crc(self, tags: List['Tag']) -> 'GblEnd':
            """Create end tag with calculated CRC"""
            crc = zlib.crc32(b'')  # Initialize CRC

            # Calculate CRC over all tag data
            for tag in tags:
                if hasattr(tag, 'tag_header'):
                    # Include tag header
                    tag_id_bytes = struct.pack('<I', tag.tag_header.id)
                    tag_length_bytes = struct.pack('<I', tag.tag_header.length)
                    tag_data = tag.content()

                    crc = zlib.crc32(tag_id_bytes, crc)
                    crc = zlib.crc32(tag_length_bytes, crc)
                    crc = zlib.crc32(tag_data, crc)

            # Include END tag header in CRC
            end_tag_id = GblType.END.value
            end_tag_length = 4
            end_header_bytes = struct.pack('<II', end_tag_id, end_tag_length)
            crc = zlib.crc32(end_header_bytes, crc)

            # Create END tag with CRC
            crc_value = crc & 0xFFFFFFFF
            crc_bytes = struct.pack('<I', crc_value)

            return GblEnd(
                tag_header=TagHeader(
                    id=end_tag_id,
                    length=end_tag_length
                ),
                gbl_crc=crc_value,
                tag_data=crc_bytes
            )

        def _encode_tags(self, tags: List['Tag']) -> bytes:
            """Encode tags to byte array"""
            result = bytearray()

            for tag in tags:
                if hasattr(tag, 'tag_header'):
                    # Write tag ID and length
                    result.extend(struct.pack('<I', tag.tag_header.id))
                    result.extend(struct.pack('<I', tag.tag_header.length))

                    # Write tag content
                    tag_data = tag.content()
                    result.extend(tag_data)

            return bytes(result)