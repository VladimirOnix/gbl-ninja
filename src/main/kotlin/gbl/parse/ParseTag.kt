package parser.data.parse

import gbl.GblParser.Companion.TAG_ID_SIZE
import gbl.GblParser.Companion.TAG_LENGTH_SIZE
import gbl.results.ParseTagResult
import gbl.tag.TagHeader
import gbl.utils.getIntFromBytes

fun parseTag(
    byteArray: ByteArray,
    offset: Int = 0,
): ParseTagResult {
    if (offset < 0 || offset + TAG_ID_SIZE + TAG_LENGTH_SIZE > byteArray.size) {
        return ParseTagResult.Fatal("Invalid offset: $offset")
    }

    val tagId = getIntFromBytes(byteArray, offset = offset, length = TAG_ID_SIZE)

    val tagLength = getIntFromBytes(byteArray, offset = offset + TAG_ID_SIZE, length = TAG_LENGTH_SIZE).toInt()

    if (offset + TAG_ID_SIZE + TAG_LENGTH_SIZE + tagLength > byteArray.size) {
        return ParseTagResult.Fatal("Invalid tag length: $tagLength")
    }

    val tagData = byteArray.copyOfRange(
        offset + TAG_ID_SIZE + TAG_LENGTH_SIZE,
        offset + TAG_ID_SIZE + TAG_LENGTH_SIZE + tagLength
    )

    val tagHeader = TagHeader(
        id = tagId,
        length = tagLength.toUInt()
    )

    return ParseTagResult.Success(
        tagHeader = tagHeader,
        tagData = tagData,
    )
}
