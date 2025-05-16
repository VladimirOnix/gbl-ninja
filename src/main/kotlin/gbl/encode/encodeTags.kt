package parser.data.encode

import gbl.GblParser.Companion.TAG_ID_SIZE
import gbl.GblParser.Companion.TAG_LENGTH_SIZE
import parser.data.tag.GblType
import gbl.tag.Tag
import parser.data.tag.TagHeader
import parser.data.tag.type.*
import gbl.tag.type.application.GblApplication
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

internal fun encodeTags(tags: List<Tag>): ByteArray {
    val totalSize = calculateTotalSize(tags)
    val buffer = ByteBuffer.allocate(totalSize)
    buffer.order(ByteOrder.LITTLE_ENDIAN)

    for (tag in tags) {
        buffer.putInt(tag.tagHeader.id.toInt())

        buffer.putInt(tag.tagHeader.length.toInt())

        val tagData = generateTagData(tag)
        buffer.put(tagData)
    }

    return buffer.array()
}

fun generateTagData(tag: Tag): ByteArray {
    return when (tag) {
        is GblHeader -> {
            val buffer = ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(tag.version.toInt())
                .putInt(tag.gblType.toInt())
            buffer.array()
        }

        is GblBootloader -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(tag.bootloaderVersion.toInt())
                .putInt(tag.address.toInt())

            buffer.put(tag.data)
            buffer.array()
        }

        is GblApplication -> {
            val appData = tag.applicationData
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(appData.type.toInt())
                .putInt(appData.version.toInt())
                .putInt(appData.capabilities.toInt())
                .put(appData.productId.toByte())

            if (tag.tagHeader.length > 13u) {
                val remainingData = tag.tagData.copyOfRange(13, tag.tagData.size)
                buffer.put(remainingData)
            }
            buffer.array()
        }

        is GblProg -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(tag.flashStartAddress.toInt())

            buffer.put(tag.data)
            buffer.array()
        }

        is GblSeUpgrade -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(tag.blobSize.toInt())
                .putInt(tag.version.toInt())

            buffer.put(tag.data)
            buffer.array()
        }

        is GblEnd -> {
            val buffer = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(tag.gblCrc.toInt())
            buffer.array()
        }

        is GblMetadata -> {
            tag.metaData
        }

        is GblProgLz4 -> {
            tag.tagData
        }

        is GblProgLzma -> {
            tag.tagData
        }

        is GblEraseProg -> {
            tag.tagData
        }

         is GblTagDelta -> {
             tag.tagData
         }


    }
}

private fun calculateTotalSize(tags: List<Tag>): Int {
    return tags.sumOf { tag ->
        TAG_ID_SIZE + TAG_LENGTH_SIZE + tag.tagHeader.length.toInt()
    }
}

fun encodeTagsWithCrc(tags: List<Tag>, includeCrc: Boolean = false): ByteArray {
    val crcSize = if (includeCrc) 4 else 0

    val totalSize = tags.sumOf { tag ->
        TAG_ID_SIZE + TAG_LENGTH_SIZE + tag.tagHeader.length.toInt() + crcSize
    }

    val buffer = ByteBuffer.allocate(totalSize)
    buffer.order(ByteOrder.LITTLE_ENDIAN)

    val fileCrc = CRC32()

    for (tag in tags) {
        val tagIdBytes = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(tag.tagHeader.id.toInt())
            .array()

        val tagLengthBytes = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(tag.tagHeader.length.toInt())
            .array()

        val tagData = generateTagData(tag)

        if (includeCrc) {
            val crc = CRC32()
            crc.update(tagIdBytes)
            crc.update(tagLengthBytes)
            crc.update(tagData)

            fileCrc.update(tagIdBytes)
            fileCrc.update(tagLengthBytes)
            fileCrc.update(tagData)

            buffer.put(tagIdBytes)
            buffer.put(tagLengthBytes)
            buffer.put(tagData)

            buffer.putInt(crc.value.toInt())
        } else {
            buffer.put(tagIdBytes)
            buffer.put(tagLengthBytes)
            buffer.put(tagData)

            fileCrc.update(tagIdBytes)
            fileCrc.update(tagLengthBytes)
            fileCrc.update(tagData)
        }
    }

    if (!includeCrc) {
        val endTagIndex = tags.indexOfFirst { it is GblEnd }
        if (endTagIndex != -1) {
            val endTag = tags[endTagIndex] as GblEnd
            val endTagPosition = tags.subList(0, endTagIndex).sumOf {
                TAG_ID_SIZE + TAG_LENGTH_SIZE + it.tagHeader.length.toInt()
            }

            val crcPosition = endTagPosition + TAG_ID_SIZE + TAG_LENGTH_SIZE

            buffer.position(crcPosition)
            buffer.putInt(fileCrc.value.toInt())
        }
    }

    return buffer.array()
}

fun createEndTagWithCrc(tags: List<Tag>): GblEnd {
    val crc = CRC32()

    for (tag in tags) {
        val tagIdBytes = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(tag.tagHeader.id.toInt())
            .array()

        val tagLengthBytes = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(tag.tagHeader.length.toInt())
            .array()

        val tagData = generateTagData(tag)

        crc.update(tagIdBytes)
        crc.update(tagLengthBytes)
        crc.update(tagData)
    }

    val endTagId = GblType.END.value.toInt()
    val endTagLength = TAG_LENGTH_SIZE

    val endTagIdBytes = ByteBuffer.allocate(TAG_LENGTH_SIZE)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putInt(endTagId)
        .array()

    val endTagLengthBytes = ByteBuffer.allocate(TAG_LENGTH_SIZE)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putInt(endTagLength)
        .array()

    crc.update(endTagIdBytes)
    crc.update(endTagLengthBytes)

    val crcValue = crc.value.toInt()
    val crcBytes = ByteBuffer.allocate(TAG_LENGTH_SIZE)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putInt(crcValue)
        .array()

    return GblEnd(
        tagHeader = TagHeader(
            id = GblType.END.value.toUInt(),
            length = TAG_LENGTH_SIZE.toUInt()
        ),
        tagType = GblType.END,
        gblCrc = crcValue.toUInt(),
        tagData = crcBytes
    )
}

fun encodeTagsWithEndTag(tags: List<Tag>): ByteArray {
    val tagsWithoutEnd = tags.filter { it !is GblEnd }

    val endTag = createEndTagWithCrc(tagsWithoutEnd)

    val finalTags = tagsWithoutEnd + endTag

    return encodeTags(finalTags)
}