package tag.type.application

data class ApplicationData(
    val type: UInt,
    val version: UInt,
    val capabilities: UInt,
    val productId: UByte
) {
    companion object {
        const val APP_TYPE: UInt = 32U
        const val APP_VERSION: UInt = 5U
        const val APP_CAPABILITIES: UInt = 0U
        const val APP_PRODUCT_ID: UByte = 54U
    }
}
