package tag

import encode.generateTagData

interface Tag {
    val tagType: GblType
    fun copy(): Tag

    fun content(): ByteArray {
        val tagData = generateTagData(this)

        return tagData
    }
}