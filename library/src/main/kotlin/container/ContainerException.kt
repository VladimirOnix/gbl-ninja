package container

class ContainerException(
    message: String,
    val errorCode: ContainerErrorCode,
    cause: Throwable? = null
) : Exception(message, cause)