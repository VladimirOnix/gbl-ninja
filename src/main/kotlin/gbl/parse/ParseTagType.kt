package parser.data.parse

import gbl.tag.type.application.ApplicationData
import gbl.tag.DefaultTag
import gbl.tag.TagHeader
import gbl.tag.Tag
import gbl.tag.type.application.GblApplication
import gbl.tag.type.GblBootloader
import gbl.tag.type.GblEnd
import gbl.tag.type.GblEraseProg
import gbl.tag.type.GblHeader
import gbl.tag.type.GblMetadata
import gbl.tag.type.GblProg
import gbl.tag.type.GblProgLz4
import gbl.tag.type.GblProgLzma
import gbl.tag.type.GblSeUpgrade
import parser.data.tag.GblType
import gbl.tag.type.certificate.ApplicationCertificate
import gbl.tag.type.certificate.GblCertificateEcdsaP256
import gbl.tag.type.certificate.GblSignatureEcdsaP256
import gbl.tag.type.encryption.GblEncryptionData
import gbl.tag.type.encryption.GblEncryptionInitAesCcm
import gbl.utils.getIntFromBytes

fun parseTagType(
    tagId: UInt,
    length: UInt,
    byteArray: ByteArray,
): Tag {

    val tagType = GblType.fromValue(tagId.toLong())

    val tagHeader = TagHeader(
        id = tagId,
        length = length
    )

    return when(tagType) {
        GblType.HEADER_V3 -> {
            val version = getIntFromBytes(byteArray, offset = 0, length = 4)
            val gblType = getIntFromBytes(byteArray, offset = 4, length = 4)

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
                bootloaderVersion = getIntFromBytes(byteArray, offset = 0, length = 4),
                address = getIntFromBytes(byteArray, offset = 4, length = 4),
                data = byteArray.copyOfRange(8, byteArray.size),
                tagData = byteArray
            )
        }
        GblType.APPLICATION -> {
            val appData = ApplicationData(
                type = getIntFromBytes(byteArray, offset = 0, length = 4),
                version = getIntFromBytes(byteArray, offset = 4, length = 4),
                capabilities = getIntFromBytes(byteArray, offset = 8, length = 4),
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
                flashStartAddress = getIntFromBytes(byteArray, offset = 0, length = 4),
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
                blobSize = getIntFromBytes(byteArray, offset = 0, length = 4),
                version = getIntFromBytes(byteArray, offset = 4, length = 4),
                data = byteArray.copyOfRange(8, byteArray.size),
                tagData = byteArray,
            )
        }
        GblType.END -> {
            GblEnd(
                tagHeader = tagHeader,
                tagType = tagType,
                gblCrc = getIntFromBytes(byteArray, offset = 0, length = 4),
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
                msgLen = getIntFromBytes(byteArray, offset = 0, length = 4),
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
                version = getIntFromBytes(byteArray, offset = 3, length = 4),
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