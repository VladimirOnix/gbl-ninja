package tag.type

import tag.GblType
import tag.TagHeader
import tag.Tag
import tag.TagWithHeader

data class GblBootloader(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    val bootloaderVersion: UInt,
    val address: UInt,
    val data: ByteArray,
    override val tagData: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblBootloader(
            tagHeader = tagHeader,
            tagType = tagType,
            bootloaderVersion = bootloaderVersion,
            address = address,
            data = data,
            tagData = arrayOf<Byte>().toByteArray()
        )
    }
}