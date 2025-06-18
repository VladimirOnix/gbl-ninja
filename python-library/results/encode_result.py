from dataclasses import dataclass
from typing import Any, Optional


@dataclass
class EncodeResult:
    @dataclass
    class Success:
        byte_array: bytes

        def __eq__(self, other) -> bool:
            if not isinstance(other, EncodeResult.Success):
                return False
            return self.byte_array == other.byte_array

        def __hash__(self) -> int:
            return hash(self.byte_array)

    @dataclass
    class Fatal:
        """Failed encoding result"""
        error: Optional[Any] = None