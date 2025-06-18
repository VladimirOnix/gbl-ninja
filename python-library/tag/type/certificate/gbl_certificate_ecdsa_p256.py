from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ...gbl_type import GblType
    from ...tag_header import TagHeader
    from ...tag import Tag

from .application_certificate import ApplicationCertificate


@dataclass
class GblCertificateEcdsaP256:
    """GBL Certificate ECDSA P256 tag"""

    tag_header: 'TagHeader'
    tag_data: bytes
    certificate: ApplicationCertificate

    @property
    def tag_type(self) -> 'GblType':
        """Get tag type"""
        from ...gbl_type import GblType
        return GblType.CERTIFICATE_ECDSA_P256

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblCertificateEcdsaP256(
            tag_header=self.tag_header,
            tag_data=self.tag_data,
            certificate=self.certificate
        )