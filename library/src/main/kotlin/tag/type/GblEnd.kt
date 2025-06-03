package tag.type

import tag.GblType
import tag.TagHeader
import tag.Tag
import tag.TagWithHeader

data class GblEnd(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    val gblCrc: UInt,
    override val tagData: ByteArray
): Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblEnd(
            tagHeader = tagHeader,
            tagType = tagType,
            gblCrc = gblCrc,
            tagData = arrayOf<Byte>().toByteArray()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GblEnd

        if (tagHeader != other.tagHeader) return false
        if (tagType != other.tagType) return false
        if (gblCrc != other.gblCrc) return false
        if (!tagData.contentEquals(other.tagData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tagHeader.hashCode()
        result = 31 * result + tagType.hashCode()
        result = 31 * result + gblCrc.hashCode()
        result = 31 * result + tagData.contentHashCode()
        return result
    }
}