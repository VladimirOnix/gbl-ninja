package tag.type

import tag.GblType
import tag.TagHeader
import tag.Tag
import tag.TagWithHeader

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
