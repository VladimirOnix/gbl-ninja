from dataclasses import dataclass
from typing import Any
from .container_error_code import ContainerErrorCode

@dataclass
class ContainerResult:

    @dataclass
    class Success:
        data: Any

        def __bool__(self):
            return True

    @dataclass
    class Error:
        message: str
        code: ContainerErrorCode

        def __bool__(self):
            return False