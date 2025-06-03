package tag.type

import tag.GblType
import tag.TagHeader
import tag.Tag
import tag.TagWithHeader

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
