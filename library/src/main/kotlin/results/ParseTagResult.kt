package results

import tag.TagHeader

sealed class ParseTagResult {
    data class Success(
        val tagHeader: TagHeader,
        val tagData: ByteArray,
    ) : ParseTagResult()

    data class Fatal(val error: Any? = null) : ParseTagResult()
}