package tag.type.encryption

import tag.GblType
import tag.TagHeader
import tag.Tag
import tag.TagWithHeader

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