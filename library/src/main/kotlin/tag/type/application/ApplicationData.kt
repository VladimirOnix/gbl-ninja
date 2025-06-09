package tag.type.application

import java.nio.ByteBuffer
import java.nio.ByteOrder

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

    fun content(): ByteArray {
        val buffer = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN)

        buffer.putInt(type.toInt())
        buffer.putInt(version.toInt())
        buffer.putInt(capabilities.toInt())
        buffer.put(productId.toByte())

        return buffer.array()
    }
}
