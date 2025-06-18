from .container_error_code import ContainerErrorCode

class ContainerException(Exception):

    def __init__(self, message: str, error_code: ContainerErrorCode, cause: Exception = None):
        super().__init__(message)
        self.error_code = error_code
        self.cause = cause