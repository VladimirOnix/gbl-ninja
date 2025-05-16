package gbl.tag.type

import parser.data.tag.GblType
import gbl.tag.TagHeader
import gbl.tag.Tag

data class GblProg(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    val flashStartAddress: UInt,
    val data: ByteArray,
    override val tagData: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblProg(
            tagHeader = tagHeader,
            tagType = tagType,
            flashStartAddress = flashStartAddress,
            data = data,
            tagData = arrayOf<Byte>().toByteArray()
        )
    }
}
