"""
ByteArray append utility
Exact conversion from Kotlin append.kt
"""


def append(byte_array: bytes, other: bytes) -> bytes:
    """
    Append other ByteArray to this ByteArray

    Args:
        byte_array: Original byte array
        other: Byte array to append

    Returns:
        bytes: New byte array with appended data
    """
    result = bytearray(len(byte_array) + len(other))
    result[:len(byte_array)] = byte_array
    result[len(byte_array):] = other
    return bytes(result)


# Alternative: Add as method to bytes (monkey patching)
# This mimics Kotlin extension function behavior
def _append_method(self, other: bytes) -> bytes:
    """Append method for bytes objects"""
    return append(self, other)


# Add method to bytes class
bytes.append = _append_method
