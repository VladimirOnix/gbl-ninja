import container.ContainerResult
import container.TagContainer
import encode.createEndTagWithCrc
import encode.encodeTags
import gbl.tag.DefaultTag
import parse.parseTag
import parse.parseTagType
import results.ParseResult
import results.ParseTagResult
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
import utils.append
import utils.putUIntToByteArray

class Gbl {
    companion object {
        internal const val HEADER_SIZE = 8
        internal const val TAG_ID_SIZE = 4
        internal const val TAG_LENGTH_SIZE = 4
    }

    fun parseByteArray(byteArray: ByteArray): ParseResult {
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

    class GblBuilder {
        private val container = TagContainer()

        companion object {
            fun create(): GblBuilder {
                val builder = GblBuilder()
                builder.container.create()
                return builder
            }

            fun empty(): GblBuilder {
                return GblBuilder()
            }
        }

        fun encryptionData(encryptedGblData: ByteArray): GblBuilder {
            val tag = GblEncryptionData(
                tagHeader = TagHeader(
                    id = GblType.ENCRYPTION_DATA.value,
                    length = encryptedGblData.size.toUInt()
                ),
                tagType = GblType.ENCRYPTION_DATA,
                encryptedGblData = encryptedGblData,
                tagData = encryptedGblData.copyOf()
            )

            container.add(tag)
            return this
        }

        fun encryptionInit(msgLen: UInt, nonce: UByte): GblBuilder {
            val tag = GblEncryptionInitAesCcm(
                tagHeader = TagHeader(
                    id = GblType.ENCRYPTION_INIT.value,
                    length = 5U
                ),
                tagType = GblType.ENCRYPTION_INIT,
                msgLen = msgLen,
                nonce = nonce,
                tagData = generateEncryptionInitTagData(msgLen, nonce)
            )

            container.add(tag)
            return this
        }

        fun signatureEcdsaP256(r: UByte, s: UByte): GblBuilder {
            val tag = GblSignatureEcdsaP256(
                tagHeader = TagHeader(
                    id = GblType.SIGNATURE_ECDSA_P256.value,
                    length = 2U
                ),
                tagType = GblType.SIGNATURE_ECDSA_P256,
                r = r,
                s = s,
                tagData = generateSignatureEcdsaP256TagData(r, s)
            )

            container.add(tag)
            return this
        }

        fun certificateEcdsaP256(certificate: ApplicationCertificate): GblBuilder {
            val tag = GblCertificateEcdsaP256(
                tagHeader = TagHeader(
                    id = GblType.CERTIFICATE_ECDSA_P256.value,
                    length = 8U
                ),
                tagType = GblType.CERTIFICATE_ECDSA_P256,
                certificate = certificate,
                tagData = generateCertificateEcdsaP256TagData(certificate)
            )

            container.add(tag)
            return this
        }

        fun versionDependency(dependencyData: ByteArray): GblBuilder {
            val tag = DefaultTag(
                tagHeader = TagHeader(
                    id = GblType.VERSION_DEPENDENCY.value,
                    length = dependencyData.size.toUInt()
                ),
                tagType = GblType.VERSION_DEPENDENCY,
                tagData = dependencyData.copyOf()
            )

            container.add(tag)
            return this
        }

        fun bootloader(
            bootloaderVersion: UInt,
            address: UInt,
            data: ByteArray
        ): GblBuilder {
            val tag = GblBootloader(
                tagHeader = TagHeader(
                    id = GblType.BOOTLOADER.value,
                    length = (8U + data.size.toUInt())
                ),
                tagType = GblType.BOOTLOADER,
                bootloaderVersion = bootloaderVersion,
                address = address,
                data = data,
                tagData = generateBootloaderTagData(bootloaderVersion, address, data)
            )

            container.add(tag)
            return this
        }

        fun metadata(metaData: ByteArray): GblBuilder {
            val tag = GblMetadata(
                tagHeader = TagHeader(
                    id = GblType.METADATA.value,
                    length = metaData.size.toUInt()
                ),
                tagType = GblType.METADATA,
                metaData = metaData,
                tagData = metaData.copyOf()
            )

            container.add(tag)
            return this
        }

        fun prog(flashStartAddress: UInt, data: ByteArray): GblBuilder {
            val tag = GblProg(
                tagHeader = TagHeader(
                    id = GblType.PROG.value,
                    length = (4U + data.size.toUInt())
                ),
                tagType = GblType.PROG,
                flashStartAddress = flashStartAddress,
                data = data,
                tagData = generateProgTagData(flashStartAddress, data)
            )

            container.add(tag)
            return this
        }

        fun progLz4(
            flashStartAddress: UInt,
            compressedData: ByteArray,
            decompressedSize: UInt
        ): GblBuilder {
            val tag = GblProgLz4(
                tagHeader = TagHeader(
                    id = GblType.PROG_LZ4.value,
                    length = (8U + compressedData.size.toUInt())
                ),
                tagType = GblType.PROG_LZ4,
                tagData = generateProgLz4TagData(flashStartAddress, compressedData, decompressedSize)
            )

            container.add(tag)
            return this
        }

        fun progLzma(
            flashStartAddress: UInt,
            compressedData: ByteArray,
            decompressedSize: UInt
        ): GblBuilder {
            val tag = GblProgLzma(
                tagHeader = TagHeader(
                    id = GblType.PROG_LZMA.value,
                    length = (8U + compressedData.size.toUInt())
                ),
                tagType = GblType.PROG_LZMA,
                tagData = generateProgLzmaTagData(flashStartAddress, compressedData, decompressedSize)
            )

            container.add(tag)
            return this
        }

        fun seUpgrade(version: UInt, data: ByteArray): GblBuilder {
            val blobSize = data.size.toUInt()
            val tag = GblSeUpgrade(
                tagHeader = TagHeader(
                    id = GblType.SE_UPGRADE.value,
                    length = (8U + blobSize)
                ),
                tagType = GblType.SE_UPGRADE,
                blobSize = blobSize,
                version = version,
                data = data,
                tagData = generateSeUpgradeTagData(blobSize, version, data)
            )

            container.add(tag)
            return this
        }

        fun application(
            type: UInt = ApplicationData.APP_TYPE,
            version: UInt = ApplicationData.APP_VERSION,
            capabilities: UInt = ApplicationData.APP_CAPABILITIES,
            productId: UByte = ApplicationData.APP_PRODUCT_ID,
            additionalData: ByteArray = ByteArray(0)
        ): GblBuilder {
            val applicationData = ApplicationData(type, version, capabilities, productId)
            val tagData = applicationData.content().append(additionalData)

            val tag = GblApplication(
                tagHeader = TagHeader(
                    id = GblType.APPLICATION.value,
                    length = tagData.size.toUInt()
                ),
                tagType = GblType.APPLICATION,
                tagData = tagData,
                applicationData = applicationData
            )

            container.add(tag)
            return this
        }

        fun eraseProg(): GblBuilder {
            val tag = GblEraseProg(
                tagHeader = TagHeader(
                    id = GblType.ERASEPROG.value,
                    length = 8U
                ),
                tagType = GblType.ERASEPROG,
                tagData = ByteArray(8)
            )

            container.add(tag)
            return this
        }

        fun get(): List<Tag> {
            val list = container.build()

            return if(list is ContainerResult.Success) {
                list.data
            } else {
                emptyList()
            }
        }

        fun buildToList(): List<Tag> {
            val tags = container.build().getOrDefault(emptyList())
            val tagsWithoutEnd = tags.filter { it !is GblEnd }
            val endTag = createEndTagWithCrc(tagsWithoutEnd)
            return tagsWithoutEnd + endTag
        }

        fun buildToByteArray(): ByteArray {
            val tags = buildToList()
            return encodeTags(tags)
        }

        fun hasTag(tagType: GblType): Boolean {
            return container.hasTag(tagType)
        }

        fun getTag(tagType: GblType): Tag? {
            return container.getTag(tagType)
        }

        fun removeTag(tag: Tag): ContainerResult<Unit> {
            return container.remove(tag)
        }

        fun clear(): ContainerResult<Unit> {
            return container.clear()
        }

        fun size(): Int {
            return container.size()
        }

        fun isEmpty(): Boolean {
            return container.isEmpty()
        }

        fun getTagTypes(): Set<GblType> {
            return container.getTagTypes()
        }

        private fun generateEncryptionInitTagData(msgLen: UInt, nonce: UByte): ByteArray {
            val result = ByteArray(5)
            putUIntToByteArray(result, 0, msgLen)
            result[4] = nonce.toByte()
            return result
        }

        private fun generateSignatureEcdsaP256TagData(r: UByte, s: UByte): ByteArray {
            val result = ByteArray(2)
            result[0] = r.toByte()
            result[1] = s.toByte()
            return result
        }

        private fun generateCertificateEcdsaP256TagData(certificate: ApplicationCertificate): ByteArray {
            val result = ByteArray(8)
            result[0] = certificate.structVersion.toByte()
            result[1] = certificate.flags.toByte()
            result[2] = certificate.key.toByte()
            putUIntToByteArray(result, 3, certificate.version)
            result[7] = certificate.signature.toByte()
            return result
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

        private fun generateProgTagData(flashStartAddress: UInt, data: ByteArray): ByteArray {
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

        private fun <T> ContainerResult<T>.getOrDefault(default: T): T {
            return if (this is ContainerResult.Success) {
                this.data
            } else {
                default
            }
        }
    }
}