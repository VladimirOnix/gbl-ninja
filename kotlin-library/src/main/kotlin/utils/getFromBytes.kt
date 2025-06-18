package utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal fun getFromBytes(byteArray: ByteArray, offset: Int = 0, length: Int = 4): ByteBuffer {
    return ByteBuffer.wrap(byteArray, offset, length)
        .order(ByteOrder.LITTLE_ENDIAN)
}