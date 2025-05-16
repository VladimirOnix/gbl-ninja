package parser.data.tag.type

import parser.data.tag.TagHeader

interface TagWithHeader {
    val tagHeader: TagHeader
    val tagData: ByteArray
}