package parse

import gbl.tag.DefaultTag
import tag.GblType
import tag.Tag
import tag.TagHeader
import tag.type.*
import tag.type.application.ApplicationData
import tag.type.application.GblApplication
import tag.type.certificate.ApplicationCertificate
import tag.type.certificate.GblCertificateEcdsaP256
import tag.type.certificate.GblSignatureEcdsaP256
import tag.type.encryption.GblEncryptionData
import tag.type.encryption.GblEncryptionInitAesCcm
import utils.getIntFromBytes

fun parseTagType(
    tagId: UInt,
    length: UInt,
    byteArray: ByteArray,
): Tag {
    val tagType = GblType.fromValue(tagId)

    val tagHeader = TagHeader(
        id = tagId,
        length = length
    )

    return when (tagType) {
        GblType.HEADER_V3 -> {
            val version = getIntFromBytes(byteArray, offset = 0, length = 4).int.toUInt()
            val gblType = getIntFromBytes(byteArray, offset = 4, length = 4).int.toUInt()

            GblHeader(
                tagHeader = tagHeader,
                tagType = tagType,
                version = version,
                gblType = gblType,
                tagData = byteArray,
            )
        }

        GblType.BOOTLOADER -> {
            GblBootloader(
                tagHeader = tagHeader,
                tagType = tagType,
                bootloaderVersion = getIntFromBytes(byteArray, offset = 0, length = 4).int.toUInt(),
                address = getIntFromBytes(byteArray, offset = 4, length = 4).int.toUInt(),
                data = byteArray.copyOfRange(8, byteArray.size),
                tagData = byteArray
            )
        }

        GblType.APPLICATION -> {
            val appData = ApplicationData(
                type = getIntFromBytes(byteArray, offset = 0, length = 4).int.toUInt(),
                version = getIntFromBytes(byteArray, offset = 4, length = 4).int.toUInt(),
                capabilities = getIntFromBytes(byteArray, offset = 8, length = 4).int.toUInt(),
                productId = byteArray[12].toUByte(),
            )

            GblApplication(
                tagHeader = tagHeader,
                tagType = tagType,
                applicationData = appData,
                tagData = byteArray,
            )
        }

        GblType.METADATA -> {
            GblMetadata(
                tagHeader = tagHeader,
                tagType = tagType,
                metaData = byteArray,
                tagData = byteArray,
            )
        }

        GblType.PROG -> {
            GblProg(
                tagHeader = tagHeader,
                tagType = tagType,
                flashStartAddress = getIntFromBytes(byteArray, offset = 0, length = 4).int.toUInt(),
                data = byteArray.copyOfRange(4, byteArray.size),
                tagData = byteArray,
            )
        }

        GblType.PROG_LZ4 -> {
            GblProgLz4(
                tagHeader = tagHeader,
                tagType = tagType,
                tagData = byteArray,
            )
        }

        GblType.PROG_LZMA -> {
            GblProgLzma(
                tagHeader = tagHeader,
                tagType = tagType,
                tagData = byteArray,
            )
        }

        GblType.ERASEPROG -> {
            GblEraseProg(
                tagHeader = tagHeader,
                tagType = tagType,
                tagData = byteArray,
            )
        }

        GblType.SE_UPGRADE -> {
            GblSeUpgrade(
                tagHeader = tagHeader,
                tagType = tagType,
                blobSize = getIntFromBytes(byteArray, offset = 0, length = 4).int.toUInt(),
                version = getIntFromBytes(byteArray, offset = 4, length = 4).int.toUInt(),
                data = byteArray.copyOfRange(8, byteArray.size),
                tagData = byteArray,
            )
        }

        GblType.END -> {
            GblEnd(
                tagHeader = tagHeader,
                tagType = tagType,
                gblCrc = getIntFromBytes(byteArray, offset = 0, length = 4).int.toUInt(),
                tagData = byteArray,
            )
        }

        GblType.ENCRYPTION_DATA -> {
            GblEncryptionData(
                tagHeader = tagHeader,
                tagType = tagType,
                encryptedGblData = byteArray.copyOfRange(8, byteArray.size),
                tagData = byteArray
            )
        }

        GblType.ENCRYPTION_INIT -> {
            GblEncryptionInitAesCcm(
                tagHeader = tagHeader,
                tagType = tagType,
                tagData = byteArray,
                msgLen = getIntFromBytes(byteArray, offset = 0, length = 4).int.toUInt(),
                nonce = byteArray.get(4).toUByte(),
            )
        }

        GblType.SIGNATURE_ECDSA_P256 -> {
            GblSignatureEcdsaP256(
                tagHeader = tagHeader,
                tagType = tagType,
                tagData = byteArray,
                r = byteArray.get(0).toUByte(),
                s = byteArray.get(1).toUByte()
            )
        }

        GblType.CERTIFICATE_ECDSA_P256 -> {
            val certificate = ApplicationCertificate(
                structVersion = byteArray[0].toUByte(),
                flags = byteArray[1].toUByte(),
                key = byteArray[2].toUByte(),
                version = getIntFromBytes(byteArray, offset = 3, length = 4).int.toUInt(),
                signature = byteArray[7].toUByte(),
            )

            GblCertificateEcdsaP256(
                tagHeader = tagHeader,
                tagType = tagType,
                tagData = byteArray,
                certificate = certificate,
            )
        }

        null -> {
            DefaultTag(
                tagType = GblType.TAG,
                tagHeader = tagHeader,
                tagData = byteArray
            )
        }

        else -> {
            DefaultTag(
                tagType = GblType.TAG,
                tagHeader = tagHeader,
                tagData = byteArray
            )
        }
    }
}