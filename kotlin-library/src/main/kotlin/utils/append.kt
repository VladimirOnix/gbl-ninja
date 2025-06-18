package utils

fun ByteArray.append(other: ByteArray): ByteArray {
    val result = ByteArray(this.size + other.size)
    this.copyInto(result, 0)
    other.copyInto(result, this.size)
    return result
}