package tag

import encode.generateTagData

interface Tag {
    val tagType: GblType
    fun copy(): Tag

    fun generateData(): ByteArray {
        val tagData = generateTagData(this)

        return tagData
    }
}