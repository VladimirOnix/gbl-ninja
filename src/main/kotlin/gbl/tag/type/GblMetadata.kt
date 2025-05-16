package parser.data.tag.type

import parser.data.tag.GblType
import parser.data.tag.TagHeader
import gbl.tag.Tag

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
