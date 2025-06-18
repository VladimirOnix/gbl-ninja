package tag.type

import tag.GblType
import tag.TagHeader
import tag.Tag
import tag.TagWithHeader

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
