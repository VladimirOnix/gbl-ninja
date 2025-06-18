package tag.type.application

import tag.GblType
import tag.TagHeader
import tag.Tag
import tag.TagWithHeader

data class GblApplication(
    override val tagHeader: TagHeader,
    override val tagType: GblType,
    val applicationData: ApplicationData,
    override val tagData: ByteArray
) : Tag, TagWithHeader {
    override fun copy(): Tag {
        return GblApplication(
            tagHeader = tagHeader,
            tagType = tagType,
            applicationData = applicationData,
            tagData = arrayOf<Byte>().toByteArray()
        )
    }

    companion object {
        const val APP_LENGTH = 13
    }
}