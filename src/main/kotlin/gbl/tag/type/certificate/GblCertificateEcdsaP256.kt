package gbl.tag.type.certificate

import parser.data.tag.GblType
import gbl.tag.TagHeader
import gbl.tag.Tag
import gbl.tag.TagWithHeader

data class GblCertificateEcdsaP256(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    override val tagData: ByteArray,
    val certificate: ApplicationCertificate
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblCertificateEcdsaP256(
            tagHeader = tagHeader,
            tagType = tagType,
            tagData = tagData,
            certificate = certificate
        )
    }
}