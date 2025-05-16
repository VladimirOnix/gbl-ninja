package gbl.tag.type

import parser.data.tag.GblType
import gbl.tag.TagHeader
import gbl.tag.Tag

data class GblEraseProg(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    override val tagData: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblEraseProg(
            tagHeader = tagHeader,
            tagType = tagType,
            tagData = arrayOf<Byte>().toByteArray()
        )
    }
}
