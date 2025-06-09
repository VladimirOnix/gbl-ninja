package container

enum class ContainerErrorCode {
    CONTAINER_NOT_CREATED,
    PROTECTED_TAG_VIOLATION,
    DUPLICATE_TAG,
    TAG_NOT_FOUND,
    INVALID_TAG,
    INTERNAL_ERROR
}
