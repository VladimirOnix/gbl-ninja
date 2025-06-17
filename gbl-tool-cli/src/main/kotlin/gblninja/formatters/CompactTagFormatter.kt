package gblninja.formatters

import tag.GblType
import tag.Tag
import tag.TagWithHeader
import tag.type.*
import tag.type.application.GblApplication
import tag.type.certificate.GblCertificateEcdsaP256
import tag.type.certificate.GblSignatureEcdsaP256
import tag.type.encryption.GblEncryptionData
import tag.type.encryption.GblEncryptionInitAesCcm
import tag.type.version.GblVersionDependency
import utils.toByteArray

internal class CompactTagFormatter {
    fun printTags(tags: List<Tag>) {
        println("GBL file contains ${tags.size} tag(s):")
        println()
        println("%-4s %-20s %-12s %-8s %s".format("No.", "Tag Type", "Start Addr", "Size", "Additional Info"))
        println("-".repeat(65))

        tags.forEachIndexed { index, tag ->
            val tagNum = "${index}."
            val tagType = tag.tagType.toString()
            val (startAddr, size, additionalInfo) = getTagInfo(tag)

            println(
                "%-4s %-20s %-12s %-8s %s".format(
                    tagNum,
                    tagType.take(20),
                    startAddr,
                    size,
                    additionalInfo
                )
            )
        }
    }

    private fun getTagInfo(tag: Tag): Triple<String, String, String> {
        return when (tag.tagType) {
            GblType.HEADER_V3 -> {
                val t = tag as GblHeader
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.tagData.size + headerSize}B", "v${t.version}, type=${t.gblType}")
            }

            GblType.BOOTLOADER -> {
                val t = tag as GblBootloader
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple(
                    "0x${t.address.toString(16).uppercase()}",
                    "${t.tagData.size + headerSize}B",
                    "v${t.bootloaderVersion}"
                )
            }

            GblType.APPLICATION -> {
                val t = tag as GblApplication
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.tagData.size + headerSize}B", "${t.applicationData}")
            }

            GblType.METADATA -> {
                val t = tag as GblMetadata
                val preview = String(t.metaData).take(30)
                val suffix = if (t.metaData.size > 30) "..." else ""
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.tagData.size + headerSize}B", "\"$preview$suffix\"")
            }

            GblType.PROG -> {
                val t = tag as GblProg
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("0x${t.flashStartAddress.toString(16).uppercase()}", "${t.data.size + headerSize}B", "")
            }

            GblType.PROG_LZ4 -> {
                val t = tag as GblProgLz4
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.tagData.size + headerSize}B", "LZ4 compressed")
            }

            GblType.PROG_LZMA -> {
                val t = tag as GblProgLzma
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.tagData.size + headerSize}B", "LZMA compressed")
            }

            GblType.SE_UPGRADE -> {
                val t = tag as GblSeUpgrade
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.data.size + headerSize}B", "v${t.version}, blob=${t.blobSize}")
            }

            GblType.ERASEPROG -> {
                val t = tag as GblEraseProg
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.tagData.size + headerSize}B", "")
            }

            GblType.END -> {
                val t = tag as GblEnd
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.tagData.size + headerSize}B", "CRC=0x${t.gblCrc.toString(16).uppercase()}")
            }

            GblType.ENCRYPTION_DATA -> {
                val t = tag as GblEncryptionData
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.tagData.size + headerSize}B", "Encrypted")
            }

            GblType.ENCRYPTION_INIT -> {
                val t = tag as GblEncryptionInitAesCcm
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.tagData.size + headerSize}B", "Len=${t.msgLen}, Nonce=0x${t.nonce.toString(16)}")
            }

            GblType.SIGNATURE_ECDSA_P256 -> {
                val t = tag as GblSignatureEcdsaP256
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.tagData.size + headerSize}B", "ECDSA P256")
            }

            GblType.CERTIFICATE_ECDSA_P256 -> {
                val t = tag as GblCertificateEcdsaP256
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                Triple("-", "${t.tagData.size + headerSize}B", "Certificate")
            }

            GblType.VERSION_DEPENDENCY -> {
                val t = tag as GblVersionDependency
                val versionStr = t.version.toString().take(20)
                Triple("-", "${t.version.toByteArray().size}B", "v$versionStr")
            }

            else -> {
                if (tag is TagWithHeader) {
                    val t = tag as TagWithHeader
                    val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                    Triple("-", "${t.tagData.size + headerSize}B", "")
                } else {
                    Triple("-", "-", "")
                }
            }
        }
    }
}