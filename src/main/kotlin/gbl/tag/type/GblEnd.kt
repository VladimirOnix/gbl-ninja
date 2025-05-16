package parser.data.tag.type

import parser.data.tag.GblType
import parser.data.tag.TagHeader
import gbl.tag.Tag

data class GblEnd(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    val gblCrc: UInt,
    override val tagData: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblEnd(
            tagHeader = tagHeader,
            tagType = tagType,
            gblCrc = gblCrc,
            tagData = arrayOf<Byte>().toByteArray()
        )
    }
}