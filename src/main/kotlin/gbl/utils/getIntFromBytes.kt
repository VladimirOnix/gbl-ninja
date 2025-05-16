package gbl.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal fun getIntFromBytes(byteArray: ByteArray, offset: Int = 0, length: Int = 4): UInt {
    return ByteBuffer.wrap(byteArray, offset, length)
        .order(ByteOrder.LITTLE_ENDIAN).int.toUInt()
}