package parser.data.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun UInt.toByteArray(order: ByteOrder = ByteOrder.LITTLE_ENDIAN): ByteArray {
    return ByteBuffer.allocate(UInt.SIZE_BYTES)
        .order(order)
        .putInt(this.toInt())
        .array()
}