from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ...gbl_type import GblType
    from ...tag_header import TagHeader
    from ...tag import Tag


@dataclass
class GblSignatureEcdsaP256:
    """GBL Signature ECDSA P256 tag"""

    tag_header: 'TagHeader'
    tag_data: bytes
    r: int  # UByte -> int
    s: int  # UByte -> int

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ...gbl_type import GblType
        return GblType.SIGNATURE_ECDSA_P256

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblSignatureEcdsaP256(
            tag_header=self.tag_header,
            tag_data=self.tag_data,
            r=self.r,
            s=self.s
        )