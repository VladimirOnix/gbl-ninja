package gbl

import gbl.encode.createEndTagWithCrc
import gbl.encode.encodeTags
import gbl.results.ParseResult
import gbl.results.ParseTagResult
import gbl.tag.DefaultTag
import gbl.tag.Tag
import gbl.tag.TagHeader
import gbl.tag.type.*
import gbl.tag.type.application.ApplicationData
import gbl.tag.type.application.GblApplication
import gbl.tag.type.certificate.ApplicationCertificate
import gbl.tag.type.certificate.GblCertificateEcdsaP256
import gbl.tag.type.certificate.GblSignatureEcdsaP256
import gbl.tag.type.encryption.GblEncryptionData
import gbl.tag.type.encryption.GblEncryptionInitAesCcm
import gbl.utils.putUIntToByteArray
import parser.data.parse.parseTag
import parser.data.parse.parseTagType
import parser.data.tag.GblType

class GblParser {
    companion object {
        internal const val HEADER_SIZE = 8
        internal const val TAG_ID_SIZE = 4
        internal const val TAG_LENGTH_SIZE = 4
        private const val GBL_TAG_ID_HEADER_V3 = 0x03A617EB

        const val HEADER_VERSION = 50331648U
        const val HEADER_GBL_TYPE = 0U
    }

    fun parseFile(byteArray: ByteArray): ParseResult {
        var offset = 0
        val size = byteArray.size
        val rawTags: MutableList<Tag> = mutableListOf()

        if (byteArray.size < HEADER_SIZE) {
            return ParseResult.Fatal("File is too small to be a valid gbl file. Expected at least $HEADER_SIZE bytes, got ${byteArray.size} bytes.")
        }

        while (offset < size) {
            val result = parseTag(byteArray, offset)

            when (result) {
                is ParseTagResult.Fatal -> {
                    break
                }

                is ParseTagResult.Success -> {
                    val (header, data) = result

                    try {
                        val parsedTag = parseTagType(
                            tagId = header.id,
                            length = header.length,
                            byteArray = data
                        )

                        rawTags.add(parsedTag)

                        offset += TAG_ID_SIZE + TAG_LENGTH_SIZE + header.length.toInt()
                    } catch (e: Exception) {

                        break
                    }
                }
            }
        }

        return ParseResult.Success(rawTags)
    }


    fun encode(tags: List<Tag>): ByteArray {
        val tagsWithoutEnd = tags.filter { it !is GblEnd }

        val endTag = createEndTagWithCrc(tagsWithoutEnd)

        val finalTags = tagsWithoutEnd + endTag

        return encodeTags(finalTags)
    }

    class Builder {
        private val gblTags = mutableListOf<Tag>()

        companion object {
            fun createEmpty(): Builder {
                val build = Builder()
                    .addHeader()

                return build
            }
        }

        private fun addHeader(
            version: UInt = HEADER_VERSION,
            gblType: UInt = HEADER_GBL_TYPE
        ): Builder {
            val tag = GblHeader(
                tagHeader = TagHeader(
                    id = GBL_TAG_ID_HEADER_V3.toUInt(),
                    length = HEADER_SIZE.toUInt(),
                ),
                tagType = GblType.HEADER_V3,
                version = version,
                gblType = gblType,
                tagData = ByteArray(0),
            )

            val header = tag.copy(
                tagData = tag.generateData()
            )

            gblTags.add(header)
            return this
        }

        fun addEncryptionData(
            encryptedGblData: ByteArray
        ): Builder {
            val gblType = GblType.ENCRYPTION_DATA

            val tag = GblEncryptionData(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = encryptedGblData.size.toUInt()
                ),
                tagType = gblType,
                encryptedGblData = encryptedGblData,
                tagData = ByteArray(0)
            )

            val tagData = encryptedGblData.copyOf()

            val encryptionData = tag.copy(
                tagData = tagData
            )

            gblTags.add(encryptionData)
            return this
        }

        fun addEncryptionInit(
            msgLen: UInt,
            nonce: UByte
        ): Builder {
            val gblType = GblType.ENCRYPTION_INIT

            val tag = GblEncryptionInitAesCcm(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = 5U
                ),
                tagType = gblType,
                msgLen = msgLen,
                nonce = nonce,
                tagData = ByteArray(0)
            )

            val tagData = generateEncryptionInitTagData(msgLen, nonce)

            val encryptionInit = tag.copy(
                tagData = tagData
            )

            gblTags.add(encryptionInit)
            return this
        }

        fun addSignatureEcdsaP256(
            r: UByte,
            s: UByte
        ): Builder {
            val gblType = GblType.SIGNATURE_ECDSA_P256

            val tag = GblSignatureEcdsaP256(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = 2U
                ),
                tagType = gblType,
                r = r,
                s = s,
                tagData = ByteArray(0)
            )

            val tagData = generateSignatureEcdsaP256TagData(r, s)

            val signature = tag.copy(
                tagData = tagData
            )

            gblTags.add(signature)
            return this
        }

        fun addCertificateEcdsaP256(
            certificate: ApplicationCertificate
        ): Builder {
            val gblType = GblType.CERTIFICATE_ECDSA_P256

            val tag = GblCertificateEcdsaP256(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = 8U
                ),
                tagType = gblType,
                certificate = certificate,
                tagData = ByteArray(0)
            )

            val tagData = generateCertificateEcdsaP256TagData(certificate)

            val certificateTag = tag.copy(
                tagData = tagData
            )

            gblTags.add(certificateTag)
            return this
        }

        fun addVersionDependency(
            dependencyData: ByteArray
        ): Builder {
            val gblType = GblType.VERSION_DEPENDENCY

            val tag = DefaultTag(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = dependencyData.size.toUInt()
                ),
                tagType = gblType,
                tagData = ByteArray(0)
            )

            val tagData = dependencyData.copyOf()

            val versionDependency = tag.copy(
                tagData = tagData
            )

            gblTags.add(versionDependency)
            return this
        }


        private fun generateEncryptionInitTagData(
            msgLen: UInt,
            nonce: UByte
        ): ByteArray {
            val result = ByteArray(5)

            putUIntToByteArray(result, 0, msgLen)

            result[4] = nonce.toByte()

            return result
        }

        private fun generateSignatureEcdsaP256TagData(
            r: UByte,
            s: UByte
        ): ByteArray {
            val result = ByteArray(2)

            result[0] = r.toByte()
            result[1] = s.toByte()

            return result
        }

        private fun generateCertificateEcdsaP256TagData(
            certificate: ApplicationCertificate
        ): ByteArray {
            val result = ByteArray(8)

            result[0] = certificate.structVersion.toByte()

            result[1] = certificate.flags.toByte()

            result[2] = certificate.key.toByte()

            putUIntToByteArray(result, 3, certificate.version)

            result[7] = certificate.signature.toByte()

            return result
        }

        fun addBootloader(
            bootloaderVersion: UInt,
            address: UInt,
            data: ByteArray
        ): Builder {
            val gblType = GblType.BOOTLOADER

            val tag = GblBootloader(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = (8U + data.size.toUInt())
                ),
                tagType = gblType,
                bootloaderVersion = bootloaderVersion,
                address = address,
                data = data,
                tagData = ByteArray(0)
            )

            val tagData = generateBootloaderTagData(bootloaderVersion, address, data)

            val bootloader = tag.copy(
                tagData = tagData
            )

            gblTags.add(bootloader)
            return this
        }

        fun addMetadata(
            metaData: ByteArray
        ): Builder {
            val gblType = GblType.METADATA

            val tag = GblMetadata(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = metaData.size.toUInt()
                ),
                tagType = gblType,
                metaData = metaData,
                tagData = ByteArray(0)
            )

            val tagData = tag.generateData()

            val metadata = tag.copy(
                tagData = tagData
            )

            gblTags.add(metadata)
            return this
        }

        fun addProg(
            flashStartAddress: UInt,
            data: ByteArray
        ): Builder {
            val gblType = GblType.PROG

            val tag = GblProg(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = (4U + data.size.toUInt())
                ),
                tagType = gblType,
                flashStartAddress = flashStartAddress,
                data = data,
                tagData = ByteArray(0)
            )

            val tagData = generateProgTagData(flashStartAddress, data)

            val prog = tag.copy(
                tagData = tagData
            )

            gblTags.add(prog)
            return this
        }

        fun addProgLz4(
            flashStartAddress: UInt,
            compressedData: ByteArray,
            decompressedSize: UInt
        ): Builder {
            val gblType = GblType.PROG_LZ4

            val tagDataSize = 8U + compressedData.size.toUInt()

            val tag = GblProgLz4(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = tagDataSize
                ),
                tagType = gblType,
                tagData = ByteArray(0)
            )

            val tagData = generateProgLz4TagData(flashStartAddress, compressedData, decompressedSize)

            val progLz4 = tag.copy(
                tagData = tagData
            )

            gblTags.add(progLz4)
            return this
        }

        fun addProgLzma(
            flashStartAddress: UInt,
            compressedData: ByteArray,
            decompressedSize: UInt
        ): Builder {
            val gblType = GblType.PROG_LZMA

            val tagDataSize =
                8U + compressedData.size.toUInt()

            val tag = GblProgLzma(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = tagDataSize
                ),
                tagType = gblType,
                tagData = ByteArray(0)
            )

            val tagData = generateProgLzmaTagData(flashStartAddress, compressedData, decompressedSize)

            val progLzma = tag.copy(
                tagData = tagData
            )

            gblTags.add(progLzma)
            return this
        }

        fun addSeUpgrade(
            version: UInt,
            data: ByteArray
        ): Builder {
            val gblType = GblType.SE_UPGRADE

            val blobSize = data.size.toUInt()

            val tag = GblSeUpgrade(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = (8U + blobSize)
                ),
                tagType = gblType,
                blobSize = blobSize,
                version = version,
                data = data,
                tagData = ByteArray(0)
            )

            val tagData = generateSeUpgradeTagData(blobSize, version, data)

            val seUpgrade = tag.copy(
                tagData = tagData
            )

            gblTags.add(seUpgrade)
            return this
        }

        private fun addEndTag(): Builder {
            val endTag = createEndTagWithCrc(this.gblTags)

            gblTags.add(endTag)
            return this
        }

        fun addApplication(
            type: UInt = ApplicationData.APP_TYPE,
            version: UInt = ApplicationData.APP_VERSION,
            capabilities: UInt = ApplicationData.APP_CAPABILITIES,
            productId: UByte = ApplicationData.APP_PRODUCT_ID,
            tagData: ByteArray = ByteArray(0)
        ): Builder {
            val gblType = GblType.APPLICATION

            val tag = GblApplication(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = (GblApplication.APP_LENGTH + tagData.size).toUInt()
                ),
                tagType = gblType,
                tagData = tagData,
                applicationData = ApplicationData(
                    type = type,
                    version = version,
                    capabilities = capabilities,
                    productId = productId,
                )
            )

            val tagData = tag.generateData()

            val application = tag.copy(
                tagData = tagData
            )

            gblTags.add(application)
            return this
        }

        fun addEraseProg(): Builder {
            val gblType = GblType.ERASEPROG

            val tag = GblEraseProg(
                tagHeader = TagHeader(
                    id = gblType.value.toUInt(),
                    length = HEADER_SIZE.toUInt()
                ),
                tagType = gblType,
                tagData = ByteArray(0),
            )

            val eraseProg = tag.copy(
                tagData = tag.generateData()
            )

            gblTags.add(eraseProg)
            return this
        }

        fun buildToList(): List<Tag> {
            val builder = this.addEndTag()
            return builder.gblTags.toList()
        }

        fun buildToByteArray(): ByteArray {
            val tagsWithoutEnd = gblTags.filter { it !is GblEnd }

            val endTag = createEndTagWithCrc(tagsWithoutEnd)

            val finalTags = tagsWithoutEnd + endTag

            return encodeTags(finalTags)
        }

        private fun generateBootloaderTagData(
            bootloaderVersion: UInt,
            address: UInt,
            data: ByteArray
        ): ByteArray {
            val result = ByteArray(8 + data.size)

            putUIntToByteArray(result, 0, bootloaderVersion)

            putUIntToByteArray(result, 4, address)

            data.copyInto(result, 8)

            return result
        }

        private fun generateProgTagData(
            flashStartAddress: UInt,
            data: ByteArray
        ): ByteArray {
            val result = ByteArray(4 + data.size)

            putUIntToByteArray(result, 0, flashStartAddress)

            data.copyInto(result, 4)

            return result
        }

        private fun generateProgLz4TagData(
            flashStartAddress: UInt,
            compressedData: ByteArray,
            decompressedSize: UInt
        ): ByteArray {
            val result = ByteArray(8 + compressedData.size)

            putUIntToByteArray(result, 0, flashStartAddress)

            putUIntToByteArray(result, 4, decompressedSize)

            compressedData.copyInto(result, 8)

            return result
        }

        private fun generateProgLzmaTagData(
            flashStartAddress: UInt,
            compressedData: ByteArray,
            decompressedSize: UInt
        ): ByteArray {
            val result = ByteArray(8 + compressedData.size)

            putUIntToByteArray(result, 0, flashStartAddress)

            putUIntToByteArray(result, 4, decompressedSize)

            compressedData.copyInto(result, 8)

            return result
        }

        private fun generateSeUpgradeTagData(
            blobSize: UInt,
            version: UInt,
            data: ByteArray
        ): ByteArray {
            val result = ByteArray(8 + data.size)

            putUIntToByteArray(result, 0, blobSize)

            putUIntToByteArray(result, 4, version)

            data.copyInto(result, 8)

            return result
        }
    }
}