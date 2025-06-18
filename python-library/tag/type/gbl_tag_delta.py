from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ..gbl_type import GblType
    from ..tag_header import TagHeader
    from ..tag import Tag


from ..tag import Tag
from ..tag_with_header import TagWithHeader


@dataclass
class GblTagDelta:
    tag_header: 'TagHeader'
    tag_type: 'GblType'
    tag_data: bytes  # ByteArray -> bytes
    new_crc: int  # UInt -> int
    new_size: int  # UInt -> int
    flash_addr: int  # UInt -> int
    data: bytes  # ByteArray -> bytes

    def copy(self) -> 'Tag':
        return GblTagDelta(
            tag_header=self.tag_header,
            tag_type=self.tag_type,
            tag_data=self.tag_data,
            new_crc=self.new_crc,
            new_size=self.new_size,
            flash_addr=self.flash_addr,
            data=self.data
        )