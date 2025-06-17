package gblninja.utils

internal object HexUtils {
    fun parseHexAddress(address: String?): Long? {
        if (address == null) return null

        return try {
            val cleanAddress = address.removePrefix("0x").removePrefix("0X")
            cleanAddress.toLong(16)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid hex address format: $address")
        }
    }

    fun parseHexValue(value: String?): ByteArray? {
        if (value == null) return null

        return try {
            val cleanValue = value.removePrefix("0x").removePrefix("0X")
            if (cleanValue.length % 2 != 0) {
                throw IllegalArgumentException("Hex value must have even number of characters")
            }

            cleanValue.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid hex value format: $value")
        }
    }

    fun bytesToHexString(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }
}