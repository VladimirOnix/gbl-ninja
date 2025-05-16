package gbl.tag.type.certificate

import parser.data.tag.GblType
import gbl.tag.TagHeader
import gbl.tag.Tag
import gbl.tag.TagWithHeader

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