package parser.data.tag

enum class GblType(val value: Long) {
    HEADER_V3(0x03A617EBL),
    BOOTLOADER(0xF50909F5L),
    APPLICATION(0xF40A0AF4L),
    METADATA(0xF60808F6L),
    PROG(0xFE0101FEL),
    PROG_LZ4(0xFD0505FDL),
    PROG_LZMA(0xFD0707FDL),
    ERASEPROG(0xFD0303FDL),
    SE_UPGRADE(0x5EA617EBL),
    END(0xFC0404FC),
    TAG(0),
    ENCRYPTION_DATA(0xF90707F9),
    ENCRYPTION_INIT(0xFA0606FA),
    SIGNATURE_ECDSA_P256(0xF70A0AF7),
    CERTIFICATE_ECDSA_P256(0xF30B0BF3),
    VERSION_DEPENDENCY(0x76A617EBL);

    companion object {
        fun fromValue(value: Long): GblType? {
            return entries.find { it.value == value }
        }
    }
}