package tag.type

import tag.GblType
import tag.TagHeader
import tag.Tag
import tag.TagWithHeader

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
