import struct
from dataclasses import dataclass


@dataclass
class TagHeader:
    id: int
    length: int

    def content(self) -> bytes:
        return struct.pack('<II', self.id, self.length)