package tag.type

import tag.GblType
import tag.TagHeader
import tag.Tag
import tag.TagWithHeader

class GblTagDelta(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    override val tagData: ByteArray,
    val newCrc: UInt,
    val newSize: UInt,
    val flashAddr: UInt,
    val data: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblTagDelta(
            tagHeader = tagHeader,
            tagType = tagType,
            tagData = tagData,
            newCrc = newCrc,
            newSize = newSize,
            flashAddr = flashAddr,
            data = data
        )
    }
}