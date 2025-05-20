package gbl.tag.type.application

import gbl.tag.GblType
import gbl.tag.TagHeader
import gbl.tag.Tag
import gbl.tag.TagWithHeader

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