package tag.type.certificate

data class ApplicationCertificate(
    val structVersion: UByte,
    val flags: UByte,
    val key: UByte,
    val version: UInt,
    val signature: UByte
)