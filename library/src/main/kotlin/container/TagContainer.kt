package container

import encode.createEndTagWithCrc
import encode.encodeTags
import tag.GblType
import tag.Tag
import tag.TagHeader
import tag.type.GblEnd
import tag.type.GblHeader

class TagContainer : Container {
    private val content: MutableSet<Tag> = mutableSetOf()
    private var isCreated: Boolean = false

    companion object {
        private const val GBL_TAG_ID_HEADER_V3 = 0x03A617EB
        private const val HEADER_SIZE = 8
        private const val HEADER_VERSION = 50331648U
        private const val HEADER_GBL_TYPE = 0U

        private val PROTECTED_TAG_TYPES = setOf(GblType.HEADER_V3, GblType.END)
    }

    override fun create(): ContainerResult<Unit> {
        return try {
            if (isCreated) {
                return ContainerResult.Success(Unit)
            }

            content.clear()

            val headerTag = createHeaderTag()
            content.add(headerTag)

            val endTag = createEndTag()
            content.add(endTag)

            isCreated = true
            ContainerResult.Success(Unit)

        } catch (e: Exception) {
            ContainerResult.Error(
                "Failed to create container: ${e.message}",
                ContainerErrorCode.INTERNAL_ERROR
            )
        }
    }

    override fun add(tag: Tag): ContainerResult<Unit> {
        return try {
            if (!isCreated) {
                return ContainerResult.Error(
                    "Container must be created before adding tags. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                )
            }

            if (isProtectedTag(tag)) {
                return ContainerResult.Error(
                    "Cannot add protected tag: ${tag.tagType}. Protected tags are managed automatically.",
                    ContainerErrorCode.PROTECTED_TAG_VIOLATION
                )
            }

            content.add(tag)
            ContainerResult.Success(Unit)

        } catch (e: Exception) {
            ContainerResult.Error(
                "Failed to add tag: ${e.message}",
                ContainerErrorCode.INTERNAL_ERROR
            )
        }
    }

    override fun remove(tag: Tag): ContainerResult<Unit> {
        return try {
            if (!isCreated) {
                return ContainerResult.Error(
                    "Container must be created before removing tags. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                )
            }

            if (isProtectedTag(tag)) {
                return ContainerResult.Error(
                    "Cannot remove protected tag: ${tag.tagType}. Protected tags are managed automatically.",
                    ContainerErrorCode.PROTECTED_TAG_VIOLATION
                )
            }

            val removed = content.remove(tag)
            if (!removed) {
                return ContainerResult.Error(
                    "Tag not found in container: ${tag.tagType}",
                    ContainerErrorCode.TAG_NOT_FOUND
                )
            }

            ContainerResult.Success(Unit)

        } catch (e: Exception) {
            ContainerResult.Error(
                "Failed to remove tag: ${e.message}",
                ContainerErrorCode.INTERNAL_ERROR
            )
        }
    }

    override fun build(): ContainerResult<List<Tag>> {
        return try {
            if (!isCreated) {
                return ContainerResult.Error(
                    "Container must be created before building. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                )
            }

            val sortedTags = mutableListOf<Tag>()

            content.find { it.tagType == GblType.HEADER_V3 }?.let {
                sortedTags.add(it)
            }

            content.filter {
                it.tagType !in PROTECTED_TAG_TYPES
            }.sortedBy {
                it.tagType.value
            }.forEach { tag ->
                sortedTags.add(tag)
            }

            content.find { it.tagType == GblType.END }?.let {
                sortedTags.add(it)
            }

            ContainerResult.Success(sortedTags)

        } catch (e: Exception) {
            ContainerResult.Error(
                "Failed to build container: ${e.message}",
                ContainerErrorCode.INTERNAL_ERROR
            )
        }
    }

    override fun content(): ContainerResult<ByteArray> {
        return try {
            if (!isCreated) {
                return ContainerResult.Error(
                    "Container must be created before exporting content. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                )
            }

            val tags = build()
            if (tags is ContainerResult.Success) {
                val tagsWithoutEnd = tags.data.filter { it !is GblEnd }
                val endTag = createEndTagWithCrc(tagsWithoutEnd)
                val finalTags = tagsWithoutEnd + endTag

                val byteArray = encodeTags(finalTags)
                ContainerResult.Success(byteArray)
            } else {
                tags as ContainerResult.Error
                ContainerResult.Error(
                    "Failed to build tags for content export: ${tags.message}",
                    tags.code
                )
            }
        } catch (e: Exception) {
            ContainerResult.Error(
                "Failed to export container content: ${e.message}",
                ContainerErrorCode.INTERNAL_ERROR
            )
        }
    }

    fun hasTag(tagType: GblType): Boolean {
        return if (!isCreated) false else content.any { it.tagType == tagType }
    }

    fun getTag(tagType: GblType): Tag? {
        return if (!isCreated) null else content.find { it.tagType == tagType }
    }

    fun getAllTags(tagType: GblType): List<Tag> {
        return if (!isCreated) emptyList() else content.filter { it.tagType == tagType }
    }

    fun isEmpty(): Boolean {
        return if (!isCreated) true else content.size == PROTECTED_TAG_TYPES.size
    }

    fun size(): Int {
        return if (!isCreated) 0 else content.size
    }

    fun getTagTypes(): Set<GblType> {
        return if (!isCreated) emptySet() else content.map { it.tagType }.toSet()
    }

    fun isCreated(): Boolean = isCreated

    fun clear(): ContainerResult<Unit> {
        return try {
            if (!isCreated) {
                return ContainerResult.Error(
                    "Container must be created before clearing. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                )
            }

            content.removeAll { !isProtectedTag(it) }
            ContainerResult.Success(Unit)

        } catch (e: Exception) {
            ContainerResult.Error(
                "Failed to clear container: ${e.message}",
                ContainerErrorCode.INTERNAL_ERROR
            )
        }
    }

    private fun isProtectedTag(tag: Tag): Boolean {
        return tag.tagType in PROTECTED_TAG_TYPES
    }

    private fun createHeaderTag(): Tag {
        val header = GblHeader(
            tagHeader = TagHeader(
                id = GBL_TAG_ID_HEADER_V3.toUInt(),
                length = HEADER_SIZE.toUInt()
            ),
            tagType = GblType.HEADER_V3,
            version = HEADER_VERSION,
            gblType = HEADER_GBL_TYPE,
            tagData = ByteArray(0)
        )

        return header.copy(
            tagData = header.content()
        )
    }

    private fun createEndTag(): Tag {
        return GblEnd(
            tagHeader = TagHeader(
                id = GblType.END.value,
                length = 0U
            ),
            tagType = GblType.END,
            tagData = ByteArray(0),
            gblCrc = 0U
        )
    }
}