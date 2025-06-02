package tag

enum class GblType(val value: UInt) {
    HEADER_V3(0x03A617EBU),
    BOOTLOADER(0xF50909F5U),
    APPLICATION(0xF40A0AF4U),
    METADATA(0xF60808F6U),
    PROG(0xFE0101FEU),
    PROG_LZ4(0xFD0505FDU),
    PROG_LZMA(0xFD0707FDU),
    ERASEPROG(0xFD0303FDU),
    SE_UPGRADE(0x5EA617EBU),
    END(0xFC0404FCU),
    TAG(0U),
    ENCRYPTION_DATA(0xF90707F9U),
    ENCRYPTION_INIT(0xFA0606FAU),
    SIGNATURE_ECDSA_P256(0xF70A0AF7U),
    CERTIFICATE_ECDSA_P256(0xF30B0BF3U),
    VERSION_DEPENDENCY(0x76A617EBU);

    companion object {
        fun fromValue(value: UInt): GblType? {
            return entries.find { it.value == value }
        }
    }

}