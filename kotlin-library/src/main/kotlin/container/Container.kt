package container

import tag.Tag

interface Container {
    fun create(): ContainerResult<Unit>
    fun add(tag: Tag): ContainerResult<Unit>
    fun remove(tag: Tag): ContainerResult<Unit>
    fun build(): ContainerResult<List<Tag>>
    fun content(): ContainerResult<ByteArray>
}