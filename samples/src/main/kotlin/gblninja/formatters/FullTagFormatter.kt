package gblninja.formatters

import gbl.tag.DefaultTag
import tag.GblType
import tag.Tag
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
import tag.type.application.GblApplication
import tag.type.certificate.GblCertificateEcdsaP256
import tag.type.certificate.GblSignatureEcdsaP256
import tag.type.encryption.GblEncryptionData
import tag.type.encryption.GblEncryptionInitAesCcm
import tag.type.version.GblVersionDependency
import utils.toByteArray

internal class FullTagFormatter {
    fun printTags(tags: List<Tag>) {
        println("Full tag information:")
        println("=".repeat(50))

        tags.forEachIndexed { index, tag ->
            val (startAddr, size, additionalInfo) = getTagInfo(tag)

            println()
            println("Tag ${index}: ${tag.tagType}")
            println("-".repeat(30))

            printFormatted("Tag Length", size)

            println("  " + "-".repeat(18))

            when (tag.tagType) {
                GblType.HEADER_V3 -> {
                    val t = tag as GblHeader
                    printFormatted("GBLVersion", t.version)
                    printFormatted("GBLType", t.gblType)
                }

                GblType.BOOTLOADER -> {
                    val t = tag as GblBootloader
                    printFormatted("BootloaderVersion", t.bootloaderVersion)
                    printFormatted("Address", "0x${t.address.toString(16).uppercase()}")
                    printFormatted("Data", t.data)
                }

                GblType.APPLICATION -> {
                    val t = tag as GblApplication
                    printFormatted("Version", t.applicationData.version)
                    printFormatted("Type", t.applicationData.type)
                    printFormatted("ProductId", t.applicationData.productId)
                    printFormatted("Capabilities", t.applicationData.capabilities)

                    printFormatted("Start Address", startAddr)
                }

                GblType.METADATA -> {
                    val t = tag as GblMetadata
                    printFormatted("MetaData (text)", String(t.metaData))
                    printFormatted("MetaData (hex)", t.metaData)
                }

                GblType.PROG -> {
                    val t = tag as GblProg
                    printFormatted("FlashStartAddress", "0x${t.flashStartAddress.toString(16).uppercase()}")
                    printFormatted("Data", t.data)
                }

                GblType.PROG_LZ4 -> {
                    val t = tag as GblProgLz4
                    printFormatted("PROG_LZ4 Data", t.tagData)
                    printFormatted("Start Address", startAddr)
                }

                GblType.PROG_LZMA -> {
                    val t = tag as GblProgLzma
                    printFormatted("PROG_LZMA Data", t.tagData)
                    printFormatted("Start Address", startAddr)
                }

                GblType.ERASEPROG -> {
                    val t = tag as GblEraseProg
                    printFormatted("ERASEPROG Content", t.tagData)
                    printFormatted("Start Address", startAddr)
                }

                GblType.SE_UPGRADE -> {
                    val t = tag as GblSeUpgrade
                    printFormatted("Version", t.version)
                    printFormatted("BlobSize", t.blobSize)
                    printFormatted("SE_UPGRADE Data", t.data)
                    printFormatted("Start Address", startAddr)
                }

                GblType.END -> {
                    val t = tag as GblEnd
                    printFormatted("GblCrc", "0x${t.gblCrc.toString(16).uppercase()}")
                }

                GblType.TAG -> {
                    val t = tag as DefaultTag
                    printFormatted("TagData", t.tagData)
                    printFormatted("Start Address", startAddr)
                    if (additionalInfo.isNotEmpty()) {
                        printFormatted("Additional Info", additionalInfo)
                    }
                }

                GblType.ENCRYPTION_DATA -> {
                    val t = tag as GblEncryptionData
                    printFormatted("EncryptedGblData", t.encryptedGblData)
                    printFormatted("Start Address", startAddr)
                }

                GblType.ENCRYPTION_INIT -> {
                    val t = tag as GblEncryptionInitAesCcm
                    printFormatted("MSGLen", t.msgLen)
                    printFormatted("Nonce", "0x${t.nonce.toString(16)}")
                    printFormatted("Start Address", startAddr)
                }

                GblType.SIGNATURE_ECDSA_P256 -> {
                    val t = tag as GblSignatureEcdsaP256
                    printFormatted("R", "0x${t.r.toString(16)}")
                    printFormatted("S", "0x${t.s.toString(16)}")
                    printFormatted("Start Address", startAddr)
                }

                GblType.CERTIFICATE_ECDSA_P256 -> {
                    val t = tag as GblCertificateEcdsaP256
                    printFormatted("Certificate", t.certificate)
                    printFormatted("Start Address", startAddr)
                }

                GblType.VERSION_DEPENDENCY -> {
                    val t = tag as GblVersionDependency
                    printFormatted("Version", t.version.toString())
                    printFormatted("Reversed", t.reversed)
                    printFormatted("ImageType", t.imageType)
                    printFormatted("Statement", t.statement)
                }

                else -> {
                    if (tag is TagWithHeader) {
                        val t = tag as TagWithHeader
                        printFormatted("TagData", t.tagData)
                    } else {
                        println("  Details not implemented for this type")
                    }
                }
            }
        }
    }

    private fun bytesToHexWithLimit(bytes: ByteArray, maxLines: Int = 10, bytesPerLine: Int = 16): List<String> {
        val allLines = bytes.asSequence()
            .mapIndexed { index, byte -> "%02X".format(byte) }
            .chunked(bytesPerLine)
            .map { it.joinToString(" ") }
            .toList()

        return if (allLines.size > maxLines) {
            allLines.take(maxLines - 1) + listOf("... (${allLines.size - maxLines + 1} more lines, ${bytes.size} bytes total)")
        } else {
            allLines
        }
    }

    private fun printFormatted(label: String, value: Any?) {
        val labelPadding = label.padEnd(20)

        if (value is ByteArray) {
            val lines = bytesToHexWithLimit(value)
            println("  $labelPadding: ${lines.firstOrNull() ?: ""}")
            lines.drop(1).forEach { println(" ".repeat(labelPadding.length + 4) + it) }
        } else {
            println("  $labelPadding: $value")
        }
    }

    private fun getTagInfo(tag: Tag): Triple<String, String, String> {
        return when (tag.tagType) {
            GblType.HEADER_V3 -> {
                val t = tag as GblHeader
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("-", "${totalSize}B", "v${t.version}, type=${t.gblType}")
            }

            GblType.BOOTLOADER -> {
                val t = tag as GblBootloader
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("0x${t.address.toString(16).uppercase()}", "${totalSize}B", "v${t.bootloaderVersion}")
            }

            GblType.APPLICATION -> {
                val t = tag as GblApplication
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("-", "${totalSize}B", "${t.applicationData}")
            }

            GblType.METADATA -> {
                val t = tag as GblMetadata
                val preview = String(t.metaData).take(30)
                val suffix = if (t.metaData.size > 30) "..." else ""
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("-", "${totalSize}B", "\"$preview$suffix\"")
            }

            GblType.PROG -> {
                val t = tag as GblProg
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.data.size + headerSize
                Triple("0x${t.flashStartAddress.toString(16).uppercase()}", "${totalSize}B", "")
            }

            GblType.PROG_LZ4 -> {
                val t = tag as GblProgLz4
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("-", "${totalSize}B", "LZ4 compressed")
            }

            GblType.PROG_LZMA -> {
                val t = tag as GblProgLzma
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("-", "${totalSize}B", "LZMA compressed")
            }

            GblType.SE_UPGRADE -> {
                val t = tag as GblSeUpgrade
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.data.size + headerSize
                Triple("-", "${totalSize}B", "v${t.version}, blob=${t.blobSize}")
            }

            GblType.ERASEPROG -> {
                val t = tag as GblEraseProg
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("-", "${totalSize}B", "")
            }

            GblType.END -> {
                val t = tag as GblEnd
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("-", "${totalSize}B", "CRC=0x${t.gblCrc.toString(16).uppercase()}")
            }

            GblType.ENCRYPTION_DATA -> {
                val t = tag as GblEncryptionData
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("-", "${totalSize}B", "Encrypted")
            }

            GblType.ENCRYPTION_INIT -> {
                val t = tag as GblEncryptionInitAesCcm
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("-", "${totalSize}B", "Len=${t.msgLen}, Nonce=0x${t.nonce.toString(16)}")
            }

            GblType.SIGNATURE_ECDSA_P256 -> {
                val t = tag as GblSignatureEcdsaP256
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("-", "${totalSize}B", "ECDSA P256")
            }

            GblType.CERTIFICATE_ECDSA_P256 -> {
                val t = tag as GblCertificateEcdsaP256
                val headerSize = t.tagHeader.id.toByteArray().size + t.tagHeader.length.toByteArray().size
                val totalSize = t.tagData.size + headerSize
                Triple("-", "${totalSize}B", "Certificate")
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
                    val totalSize = t.tagData.size + headerSize
                    Triple("-", "${totalSize}B", "")
                } else {
                    Triple("-", "-", "")
                }
            }
        }
    }
}