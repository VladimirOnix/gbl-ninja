from typing import List, Union, Optional
from copy import deepcopy

from container.container_result import ContainerResult
from container.tag_container import TagContainer
from encode.encode_tags import create_end_tag_with_crc, encode_tags
from tag.default_tag import DefaultTag
from parse.parse_tag import parse_tag
from parse.parse_tag_type import parse_tag_type
from results.parse_result import ParseResult
from results.parse_tag_result import ParseTagResult
from tag.gbl_type import GblType
from tag.tag import Tag
from tag.tag_header import TagHeader
from tag.type.gbl_end import GblEnd
from tag.type.gbl_header import GblHeader
from tag.type.gbl_bootloader import GblBootloader
from tag.type.gbl_metadata import GblMetadata
from tag.type.gbl_prog import GblProg
from tag.type.gbl_prog_lz4 import GblProgLz4
from tag.type.gbl_prog_lzma import GblProgLzma
from tag.type.gbl_se_upgrade import GblSeUpgrade
from tag.type.gbl_erase_prog import GblEraseProg
from tag.type.encryption.gbl_encryption_data import GblEncryptionData
from tag.type.encryption.gbl_encryption_init_aes_ccm import GblEncryptionInitAesCcm
from tag.type.certificate.gbl_signature_ecdsa_p256 import GblSignatureEcdsaP256
from tag.type.certificate.gbl_certificate_ecdsa_p256 import GblCertificateEcdsaP256
from tag.type.certificate.application_certificate import ApplicationCertificate
from tag.type.application.application_data import ApplicationData
from tag.type.application.gbl_application import GblApplication
from utils.append import append
from utils.put_uint_to_byte_array import put_uint_to_byte_array


class Gbl:
    HEADER_SIZE = 8
    TAG_ID_SIZE = 4
    TAG_LENGTH_SIZE = 4

    def parse_byte_array(self, byte_array: bytes) -> Union[ParseResult.Success, ParseResult.Fatal]:

        offset = 0
        size = len(byte_array)
        raw_tags: List[Tag] = []

        if len(byte_array) < self.HEADER_SIZE:
            return ParseResult.Fatal(
                f"File is too small to be a valid gbl file. Expected at least {self.HEADER_SIZE} bytes, got {len(byte_array)} bytes.")

        while offset < size:
            result = parse_tag(byte_array, offset)

            if isinstance(result, ParseTagResult.Fatal):
                break

            if isinstance(result, ParseTagResult.Success):
                header, data = result.tag_header, result.tag_data

                try:
                    parsed_tag = parse_tag_type(
                        tag_id=header.id,
                        length=header.length,
                        byte_array=data
                    )

                    if parsed_tag:
                        raw_tags.append(parsed_tag)

                    offset += self.TAG_ID_SIZE + self.TAG_LENGTH_SIZE + header.length
                except Exception as e:
                    break

        return ParseResult.Success(raw_tags)

    def encode(self, tags: List[Tag]) -> bytes:

        tags_without_end = [tag for tag in tags if not isinstance(tag, GblEnd)]

        end_tag = create_end_tag_with_crc(tags_without_end)

        final_tags = tags_without_end + [end_tag]

        return encode_tags(final_tags)

    @property
    def GblBuilder(self) -> type:
        return GblBuilder


class GblBuilder:
    def __init__(self):
        self.container = TagContainer()

    @classmethod
    def create(cls) -> 'GblBuilder':
        builder = cls()
        builder.container.create()
        return builder

    @classmethod
    def empty(cls) -> 'GblBuilder':
        return cls()

    def encryption_data(self, encrypted_gbl_data: bytes) -> 'GblBuilder':
        tag = GblEncryptionData(
            tag_header=TagHeader(
                id=GblType.ENCRYPTION_DATA.value,
                length=len(encrypted_gbl_data)
            ),
            tag_type=GblType.ENCRYPTION_DATA,
            encrypted_gbl_data=encrypted_gbl_data,
            tag_data=encrypted_gbl_data.copy()
        )

        self.container.add(tag)
        return self

    def encryption_init(self, msg_len: int, nonce: int) -> 'GblBuilder':
        tag = GblEncryptionInitAesCcm(
            tag_header=TagHeader(
                id=GblType.ENCRYPTION_INIT.value,
                length=5
            ),
            tag_type=GblType.ENCRYPTION_INIT,
            msg_len=msg_len,
            nonce=nonce,
            tag_data=self._generate_encryption_init_tag_data(msg_len, nonce)
        )

        self.container.add(tag)
        return self

    def signature_ecdsa_p256(self, r: int, s: int) -> 'GblBuilder':
        tag = GblSignatureEcdsaP256(
            tag_header=TagHeader(
                id=GblType.SIGNATURE_ECDSA_P256.value,
                length=2
            ),
            tag_type=GblType.SIGNATURE_ECDSA_P256,
            r=r,
            s=s,
            tag_data=self._generate_signature_ecdsa_p256_tag_data(r, s)
        )

        self.container.add(tag)
        return self

    def certificate_ecdsa_p256(self, certificate: ApplicationCertificate) -> 'GblBuilder':
        tag = GblCertificateEcdsaP256(
            tag_header=TagHeader(
                id=GblType.CERTIFICATE_ECDSA_P256.value,
                length=8
            ),
            tag_type=GblType.CERTIFICATE_ECDSA_P256,
            certificate=certificate,
            tag_data=self._generate_certificate_ecdsa_p256_tag_data(certificate)
        )

        self.container.add(tag)
        return self

    def version_dependency(self, dependency_data: bytes) -> 'GblBuilder':
        tag = DefaultTag(
            tag_header=TagHeader(
                id=GblType.VERSION_DEPENDENCY.value,
                length=len(dependency_data)
            ),
            _tag_type=GblType.VERSION_DEPENDENCY,
            tag_data=dependency_data.copy()
        )

        self.container.add(tag)
        return self

    def bootloader(self, bootloader_version: int, address: int, data: bytes) -> 'GblBuilder':
        tag = GblBootloader(
            tag_header=TagHeader(
                id=GblType.BOOTLOADER.value,
                length=8 + len(data)
            ),
            tag_type=GblType.BOOTLOADER,
            bootloader_version=bootloader_version,
            address=address,
            data=data,
            tag_data=self._generate_bootloader_tag_data(bootloader_version, address, data)
        )

        self.container.add(tag)
        return self

    def metadata(self, meta_data: bytes) -> 'GblBuilder':
        tag = GblMetadata(
            tag_header=TagHeader(
                id=GblType.METADATA.value,
                length=len(meta_data)
            ),
            tag_type=GblType.METADATA,
            meta_data=meta_data,
            tag_data=meta_data.copy()
        )

        self.container.add(tag)
        return self

    def prog(self, flash_start_address: int, data: bytes) -> 'GblBuilder':
        tag = GblProg(
            tag_header=TagHeader(
                id=GblType.PROG.value,
                length=4 + len(data)
            ),
            tag_type=GblType.PROG,
            flash_start_address=flash_start_address,
            data=data,
            tag_data=self._generate_prog_tag_data(flash_start_address, data)
        )

        self.container.add(tag)
        return self

    def prog_lz4(self, flash_start_address: int, compressed_data: bytes, decompressed_size: int) -> 'GblBuilder':
        """Add LZ4 compressed program tag"""
        tag = GblProgLz4(
            tag_header=TagHeader(
                id=GblType.PROG_LZ4.value,
                length=8 + len(compressed_data)
            ),
            tag_type=GblType.PROG_LZ4,
            tag_data=self._generate_prog_lz4_tag_data(flash_start_address, compressed_data, decompressed_size)
        )

        self.container.add(tag)
        return self

    def prog_lzma(self, flash_start_address: int, compressed_data: bytes, decompressed_size: int) -> 'GblBuilder':
        """Add LZMA compressed program tag"""
        tag = GblProgLzma(
            tag_header=TagHeader(
                id=GblType.PROG_LZMA.value,
                length=8 + len(compressed_data)
            ),
            tag_type=GblType.PROG_LZMA,
            tag_data=self._generate_prog_lzma_tag_data(flash_start_address, compressed_data, decompressed_size)
        )

        self.container.add(tag)
        return self

    def se_upgrade(self, version: int, data: bytes) -> 'GblBuilder':
        """Add SE upgrade tag"""
        blob_size = len(data)
        tag = GblSeUpgrade(
            tag_header=TagHeader(
                id=GblType.SE_UPGRADE.value,
                length=8 + blob_size
            ),
            tag_type=GblType.SE_UPGRADE,
            blob_size=blob_size,
            version=version,
            data=data,
            tag_data=self._generate_se_upgrade_tag_data(blob_size, version, data)
        )

        self.container.add(tag)
        return self

    def application(self, type_val: int = ApplicationData.APP_TYPE,
                    version: int = ApplicationData.APP_VERSION,
                    capabilities: int = ApplicationData.APP_CAPABILITIES,
                    product_id: int = ApplicationData.APP_PRODUCT_ID,
                    additional_data: bytes = b'') -> 'GblBuilder':
        """Add application tag"""
        application_data = ApplicationData(type_val, version, capabilities, product_id)
        tag_data = append(application_data.content(), additional_data)

        tag = GblApplication(
            tag_header=TagHeader(
                id=GblType.APPLICATION.value,
                length=len(tag_data)
            ),
            tag_type=GblType.APPLICATION,
            tag_data=tag_data,
            application_data=application_data
        )

        self.container.add(tag)
        return self

    def erase_prog(self) -> 'GblBuilder':
        """Add erase program tag"""
        tag = GblEraseProg(
            tag_header=TagHeader(
                id=GblType.ERASEPROG.value,
                length=8
            ),
            tag_type=GblType.ERASEPROG,
            tag_data=bytes(8)
        )

        self.container.add(tag)
        return self

    def get(self) -> List[Tag]:
        """Get list of tags"""
        build_result = self.container.build()

        if isinstance(build_result, ContainerResult.Success):
            return build_result.data
        else:
            return []

    def build_to_list(self) -> List[Tag]:
        """Build to tag list with END tag"""
        tags = self._get_or_default(self.container.build(), [])
        tags_without_end = [tag for tag in tags if not isinstance(tag, GblEnd)]
        end_tag = create_end_tag_with_crc(tags_without_end)
        return tags_without_end + [end_tag]

    def build_to_byte_array(self) -> bytes:
        """Build to byte array"""
        tags = self.build_to_list()
        return encode_tags(tags)

    def has_tag(self, tag_type: GblType) -> bool:
        """Check if container has tag of specific type"""
        return self.container.has_tag(tag_type)

    def get_tag(self, tag_type: GblType) -> Optional[Tag]:
        """Get tag of specific type"""
        return self.container.get_tag(tag_type)

    def remove_tag(self, tag: Tag) -> ContainerResult:
        """Remove tag from container"""
        return self.container.remove(tag)

    def clear(self) -> ContainerResult:
        """Clear container"""
        return self.container.clear()

    def size(self) -> int:
        """Get container size"""
        return self.container.size()

    def is_empty(self) -> bool:
        """Check if container is empty"""
        return self.container.is_empty()

    def get_tag_types(self) -> set:
        """Get set of tag types"""
        return self.container.get_tag_types()

    # Private helper methods (точно як у Kotlin)

    def _generate_encryption_init_tag_data(self, msg_len: int, nonce: int) -> bytes:
        """Generate encryption init tag data"""
        result = bytearray(5)
        put_uint_to_byte_array(result, 0, msg_len)
        result[4] = nonce
        return bytes(result)

    def _generate_signature_ecdsa_p256_tag_data(self, r: int, s: int) -> bytes:
        """Generate signature ECDSA P256 tag data"""
        result = bytearray(2)
        result[0] = r
        result[1] = s
        return bytes(result)

    def _generate_certificate_ecdsa_p256_tag_data(self, certificate: ApplicationCertificate) -> bytes:
        """Generate certificate ECDSA P256 tag data"""
        result = bytearray(8)
        result[0] = certificate.struct_version
        result[1] = certificate.flags
        result[2] = certificate.key
        put_uint_to_byte_array(result, 3, certificate.version)
        result[7] = certificate.signature
        return bytes(result)

    def _generate_bootloader_tag_data(self, bootloader_version: int, address: int, data: bytes) -> bytes:
        """Generate bootloader tag data"""
        result = bytearray(8 + len(data))
        put_uint_to_byte_array(result, 0, bootloader_version)
        put_uint_to_byte_array(result, 4, address)
        result[8:8 + len(data)] = data
        return bytes(result)

    def _generate_prog_tag_data(self, flash_start_address: int, data: bytes) -> bytes:
        """Generate program tag data"""
        result = bytearray(4 + len(data))
        put_uint_to_byte_array(result, 0, flash_start_address)
        result[4:4 + len(data)] = data
        return bytes(result)

    def _generate_prog_lz4_tag_data(self, flash_start_address: int, compressed_data: bytes,
                                    decompressed_size: int) -> bytes:
        """Generate LZ4 program tag data"""
        result = bytearray(8 + len(compressed_data))
        put_uint_to_byte_array(result, 0, flash_start_address)
        put_uint_to_byte_array(result, 4, decompressed_size)
        result[8:8 + len(compressed_data)] = compressed_data
        return bytes(result)

    def _generate_prog_lzma_tag_data(self, flash_start_address: int, compressed_data: bytes,
                                     decompressed_size: int) -> bytes:
        """Generate LZMA program tag data"""
        result = bytearray(8 + len(compressed_data))
        put_uint_to_byte_array(result, 0, flash_start_address)
        put_uint_to_byte_array(result, 4, decompressed_size)
        result[8:8 + len(compressed_data)] = compressed_data
        return bytes(result)

    def _generate_se_upgrade_tag_data(self, blob_size: int, version: int, data: bytes) -> bytes:
        """Generate SE upgrade tag data"""
        result = bytearray(8 + len(data))
        put_uint_to_byte_array(result, 0, blob_size)
        put_uint_to_byte_array(result, 4, version)
        result[8:8 + len(data)] = data
        return bytes(result)

    def _get_or_default(self, container_result: ContainerResult, default):
        """Get data from container result or return default"""
        if isinstance(container_result, ContainerResult.Success):
            return container_result.data
        else:
            return default