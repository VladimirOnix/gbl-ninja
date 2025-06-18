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