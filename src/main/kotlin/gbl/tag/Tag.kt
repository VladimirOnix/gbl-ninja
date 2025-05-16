package gbl.tag

import parser.data.encode.generateTagData
import parser.data.tag.GblType

interface Tag {
    val tagType: GblType
    fun copy(): Tag

    fun generateData(): ByteArray {
        val tagData = generateTagData(this)

        return tagData
    }
}