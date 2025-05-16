package parser.data.tag.type

import parser.data.tag.GblType
import parser.data.tag.TagHeader
import gbl.tag.Tag

data class GblHeader(
    override val tagHeader: TagHeader,
    override val tagType: GblType = GblType.HEADER_V3,
    val version: UInt,
    val gblType: UInt,
    override val tagData: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblHeader(
            tagHeader = tagHeader,
            version = version,
            gblType = gblType,
            tagData = arrayOf<Byte>().toByteArray()
        )
    }
}
