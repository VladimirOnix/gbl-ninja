package encode

import Gbl.Companion.TAG_ID_SIZE
import Gbl.Companion.TAG_LENGTH_SIZE
import tag.type.application.GblApplication
import tag.type.certificate.GblCertificateEcdsaP256
import tag.type.certificate.GblSignatureEcdsaP256
import tag.GblType
import tag.Tag
import tag.TagHeader
import tag.TagWithHeader
import tag.type.GblBootloader
import tag.type.GblEnd
import tag.type.GblEraseProg
import tag.type.GblHeader
import tag.type.GblMetadata
import tag.type.GblProg
import tag.type.GblProgLz4
import tag.type.GblProgLzma
import tag.type.GblSeUpgrade
import tag.type.GblTagDelta
import tag.type.encryption.GblEncryptionData
import tag.type.encryption.GblEncryptionInitAesCcm
import tag.type.version.GblVersionDependency
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

internal fun encodeTags(tags: List<Tag>): ByteArray {
    val totalSize = calculateTotalSize(tags)
    val buffer = ByteBuffer.allocate(totalSize)
    buffer.order(ByteOrder.LITTLE_ENDIAN)

    for (tag in tags) {
        if (tag !is TagWithHeader) continue

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
            println("Found end tag ${tag.tagHeader.length.toInt()}")
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(tag.gblCrc.toInt())

            buffer.array()
        }

        is GblMetadata -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(tag.metaData)

            buffer.array()
        }

        is GblProgLz4 -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(tag.tagData)

            buffer.array()
        }

        is GblProgLzma -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(tag.tagData)

            buffer.array()
        }

        is GblEraseProg -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(tag.tagData)

            buffer.array()
        }

        is GblTagDelta -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(tag.newCrc.toInt())
                .putInt(tag.newSize.toInt())
                .putInt(tag.flashAddr.toInt())

            buffer.put(tag.data)
            buffer.array()
        }

        is GblCertificateEcdsaP256 -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(tag.certificate.structVersion.toByte())
                .put(tag.certificate.flags.toByte())
                .put(tag.certificate.key.toByte())
                .putInt(tag.certificate.version.toInt())
                .put(tag.certificate.signature.toByte())

            buffer.array()
        }

        is GblSignatureEcdsaP256 -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(tag.r.toByte())
                .put(tag.s.toByte())

            buffer.array()
        }

        is GblEncryptionData -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(tag.encryptedGblData)

            buffer.array()
        }

        is GblEncryptionInitAesCcm -> {
            val buffer = ByteBuffer.allocate(tag.tagHeader.length.toInt())
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(tag.msgLen.toInt())
                .put(tag.nonce.toByte())

            buffer.array()
        }

        is GblVersionDependency -> {
            val buffer = ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(tag.imageType.value.toInt())
                .put(tag.statement.toByte())
                .putShort(tag.reversed.toShort())
                .putInt(tag.version.toInt())

            buffer.array()
        }

        else -> ByteArray(0)
    }
}

private fun calculateTotalSize(tags: List<Tag>): Int {
    return tags.sumOf { tag ->
        if (tag is TagWithHeader) {
            TAG_ID_SIZE + TAG_LENGTH_SIZE + tag.tagHeader.length.toInt()
        } else {
            0
        }
    }
}

fun encodeTagsWithCrc(tags: List<Tag>, includeCrc: Boolean = false): ByteArray {
    val crcSize = if (includeCrc) 4 else 0

    val totalSize = tags.sumOf { tag ->
        if (tag is TagWithHeader) {
            TAG_ID_SIZE + TAG_LENGTH_SIZE + tag.tagHeader.length.toInt() + crcSize
        } else {
            0
        }
    }

    val buffer = ByteBuffer.allocate(totalSize)
    buffer.order(ByteOrder.LITTLE_ENDIAN)

    val fileCrc = CRC32()

    for (tag in tags) {
        if (tag is TagWithHeader) {
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
    }

    if (!includeCrc) {
        val endTagIndex = tags.indexOfFirst { it is GblEnd }
        if (endTagIndex != -1) {
            val endTag = tags[endTagIndex] as GblEnd
            val endTagPosition = tags.subList(0, endTagIndex).sumOf {
                if (it is TagWithHeader) {
                    TAG_ID_SIZE + TAG_LENGTH_SIZE + it.tagHeader.length.toInt()
                } else {
                    0
                }
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
        if (tag !is TagWithHeader) continue

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