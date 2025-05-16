package gbl.tag.type.application

import parser.data.tag.GblType
import parser.data.tag.TagHeader
import gbl.tag.Tag

data class GblApplication(
    val tagHeader: TagHeader,
    override val tagType: GblType,
    val applicationData: ApplicationData,
    val tagData: ByteArray
) : Tag {
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