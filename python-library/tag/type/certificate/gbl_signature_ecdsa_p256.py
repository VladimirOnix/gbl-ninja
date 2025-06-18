from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ...gbl_type import GblType
    from ...tag_header import TagHeader
    from ...tag import Tag


# Імпорти з інших модулів (будуть додані пізніше):
# from ...tag import Tag
# from ...tag_with_header import TagWithHeader


@dataclass
class GblSignatureEcdsaP256:
    """GBL Signature ECDSA P256 tag"""

    tag_header: 'TagHeader'
    tag_type: 'GblType'
    tag_data: bytes
    r: int  # UByte -> int
    s: int  # UByte -> int

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblSignatureEcdsaP256(
            tag_header=self.tag_header,
            tag_type=self.tag_type,
            tag_data=self.tag_data,
            r=self.r,
            s=self.s
        )