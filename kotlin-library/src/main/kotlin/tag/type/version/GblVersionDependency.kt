package tag.type.version

import tag.GblType
import tag.Tag

data class GblVersionDependency(
    override val tagType: GblType,
    val imageType: ImageType,
    val statement: UByte,
    val reversed: UShort,
    val version: UInt,
): Tag {
    override fun copy(): Tag {
        return GblVersionDependency(
            tagType = tagType,
            imageType = imageType,
            statement = statement,
            reversed = reversed,
            version = version,
        )
    }
}