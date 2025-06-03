package tag.type.version

enum class ImageType(
    val value: UInt
) {
    APPLICATION(0x01u),
    BOOTLOADER(0x02u),
    SE(0x03u);

    fun fromValue(value: UInt): ImageType {
        return values().first { it.value == value }
    }
}