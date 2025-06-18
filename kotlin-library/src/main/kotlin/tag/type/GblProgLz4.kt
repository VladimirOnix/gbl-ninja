package tag.type

import tag.GblType
import tag.TagHeader
import tag.Tag
import tag.TagWithHeader

data class GblProgLz4(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    override val tagData: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblProgLz4(
            tagHeader = tagHeader,
            tagType = tagType,
            tagData = arrayOf<Byte>().toByteArray()
        )
    }
}
