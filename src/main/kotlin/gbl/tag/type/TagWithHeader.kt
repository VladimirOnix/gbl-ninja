package gbl.tag.type

import gbl.tag.TagHeader

interface TagWithHeader {
    val tagHeader: TagHeader
    val tagData: ByteArray
}