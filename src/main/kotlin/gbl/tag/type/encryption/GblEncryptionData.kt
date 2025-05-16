package gbl.tag.type.encryption

import parser.data.tag.GblType
import gbl.tag.TagHeader
import gbl.tag.Tag
import gbl.tag.TagWithHeader

data class GblEncryptionData(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    override val tagData: ByteArray,
    val encryptedGblData: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblEncryptionData(
            tagHeader = tagHeader,
            tagType = tagType,
            tagData = tagData,
            encryptedGblData = encryptedGblData
        )
    }
}