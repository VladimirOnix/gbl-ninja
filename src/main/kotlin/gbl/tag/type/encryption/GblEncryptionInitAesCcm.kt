package gbl.tag.type.encryption

import gbl.tag.GblType
import gbl.tag.TagHeader
import gbl.tag.Tag
import gbl.tag.TagWithHeader

data class GblEncryptionInitAesCcm(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    val msgLen: UInt,
    val nonce: UByte,
    override val tagData: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblEncryptionInitAesCcm(
            tagHeader = tagHeader,
            tagType = tagType,
            tagData = arrayOf<Byte>().toByteArray(),
            msgLen = msgLen,
            nonce = nonce
        )
    }

}