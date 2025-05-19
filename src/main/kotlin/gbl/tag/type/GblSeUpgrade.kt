package gbl.tag.type

import gbl.tag.GblType
import gbl.tag.TagHeader
import gbl.tag.Tag
import gbl.tag.TagWithHeader

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
