package results

sealed class EncodeResult {
    data class Success(
        val byteArray: ByteArray
    ) : EncodeResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            return byteArray.contentEquals(other.byteArray)
        }

        override fun hashCode(): Int {
            return byteArray.contentHashCode()
        }
    }

    data class Fatal(val error: Any? = null) : EncodeResult()
}