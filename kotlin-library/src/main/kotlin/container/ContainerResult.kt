package container

sealed class ContainerResult<out T> {
    data class Success<T>(val data: T) : ContainerResult<T>()
    data class Error(val message: String, val code: ContainerErrorCode) : ContainerResult<Nothing>()
}