package utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun putUIntToByteArray(array: ByteArray, offset: Int, value: UInt) {
    ByteBuffer.wrap(array, offset, 4)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putInt(value.toInt())
}