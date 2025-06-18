package tag

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class TagHeader(
    val id: UInt,
    val length: UInt,
) {
    fun content(): ByteArray {
        val buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)

        buffer.putInt(id.toInt())
        buffer.putInt(length.toInt())

        return buffer.array()
    }
}
