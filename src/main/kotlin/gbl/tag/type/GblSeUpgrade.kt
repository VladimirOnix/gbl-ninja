package parser.data.tag.type

import parser.data.tag.GblType
import parser.data.tag.TagHeader
import gbl.tag.Tag

data class GblSeUpgrade(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    val blobSize: UInt,
    val version: UInt,
    val data: ByteArray,
    override val tagData: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblSeUpgrade(
            tagHeader = tagHeader,
            tagType = tagType,
            blobSize = blobSize,
            version = version,
            data = data,
            tagData = arrayOf<Byte>().toByteArray()
        )
    }
}
