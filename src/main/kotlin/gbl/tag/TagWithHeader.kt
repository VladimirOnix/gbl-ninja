package gbl.tag

interface TagWithHeader {
    val tagHeader: TagHeader
    val tagData: ByteArray
}