package results

import tag.Tag

sealed class ParseResult {
    data class Success(
        val resultList: List<Tag>
    ) : ParseResult()

    data class Fatal(val error: Any? = null) : ParseResult()
}




