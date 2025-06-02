package tag.type.certificate

import tag.GblType
import tag.TagHeader
import tag.Tag
import tag.TagWithHeader

data class GblSignatureEcdsaP256(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    override val tagData: ByteArray,
    val r: UByte,
    val s: UByte
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblSignatureEcdsaP256(
            tagHeader = tagHeader,
            tagType = tagType,
            tagData = tagData,
            r = r,
            s = s
        )
    }
}