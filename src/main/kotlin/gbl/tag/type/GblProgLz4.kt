package gbl.tag.type

import parser.data.tag.GblType
import gbl.tag.TagHeader
import gbl.tag.Tag

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
