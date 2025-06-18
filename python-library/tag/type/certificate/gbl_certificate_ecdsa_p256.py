
"""
GBL Certificate ECDSA P256 tag implementation
Exact conversion from Kotlin GblCertificateEcdsaP256.kt
"""

from dataclasses import dataclass
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from ...gbl_type import GblType
    from ...tag_header import TagHeader
    from ...tag import Tag

from .application_certificate import ApplicationCertificate


# Імпорти з інших модулів (будуть додані пізніше):
# from ...tag import Tag
# from ...tag_with_header import TagWithHeader


@dataclass
class GblCertificateEcdsaP256:
    """GBL Certificate ECDSA P256 tag"""

    tag_header: 'TagHeader'
    tag_type: 'GblType'
    tag_data: bytes
    certificate: ApplicationCertificate

    def copy(self) -> 'Tag':
        """Create a copy of the tag"""
        return GblCertificateEcdsaP256(
            tag_header=self.tag_header,
            tag_type=self.tag_type,
            tag_data=self.tag_data,
            certificate=self.certificate
        )