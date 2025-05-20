package gbl.tag.type

import gbl.tag.GblType
import gbl.tag.TagHeader
import gbl.tag.Tag
import gbl.tag.TagWithHeader

data class GblMetadata(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    val metaData: ByteArray,
    override val tagData: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblMetadata(
            tagHeader = tagHeader,
            tagType = tagType,
            metaData = metaData,
            tagData = arrayOf<Byte>().toByteArray()
        )
    }
}
