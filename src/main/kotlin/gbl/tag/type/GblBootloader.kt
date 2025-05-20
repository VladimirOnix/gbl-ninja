package gbl.tag.type

import gbl.tag.GblType
import gbl.tag.TagHeader
import gbl.tag.Tag
import gbl.tag.TagWithHeader

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