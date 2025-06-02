package gbl.tag

import tag.GblType
import tag.Tag
import tag.TagHeader

data class DefaultTag(
    val tagData: ByteArray,
    val tagHeader: TagHeader,
    override val tagType: GblType
) : Tag {
    override fun copy(): DefaultTag {
        return DefaultTag(
            tagData = arrayOf<Byte>().toByteArray(),
            tagHeader = tagHeader,
            tagType = tagType
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultTag

        if (!tagData.contentEquals(other.tagData)) return false
        if (tagHeader != other.tagHeader) return false
        if (tagType != other.tagType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tagData.contentHashCode()
        result = 31 * result + tagHeader.hashCode()
        result = 31 * result + tagType.hashCode()
        return result
    }
}
