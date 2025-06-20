// GBL Library - Complete implementation for parsing and building GBL files

class GblType {
    static HEADER_V3 = new Uint8Array([0xEB, 0x17, 0xA6, 0x03]);
    static BOOTLOADER = new Uint8Array([0xF5, 0x09, 0x09, 0xF5]);
    static APPLICATION = new Uint8Array([0xF4, 0x0A, 0x0A, 0xF4]);
    static METADATA = new Uint8Array([0xF6, 0x08, 0x08, 0xF6]);
    static PROG = new Uint8Array([0xFE, 0x01, 0x01, 0xFE]);
    static PROG_LZ4 = new Uint8Array([0xFD, 0x05, 0x05, 0xFD]);
    static PROG_LZMA = new Uint8Array([0xFD, 0x07, 0x07, 0xFD]);
    static ERASEPROG = new Uint8Array([0xFD, 0x03, 0x03, 0xFD]);
    static SE_UPGRADE = new Uint8Array([0xEB, 0x17, 0xA6, 0x5E]);
    static END = new Uint8Array([0xFC, 0x04, 0x04, 0xFC]);
    static TAG = new Uint8Array([0x00, 0x00, 0x00, 0x00]);
    static ENCRYPTION_DATA = new Uint8Array([0xF9, 0x07, 0x07, 0xF9]);
    static ENCRYPTION_INIT = new Uint8Array([0xFA, 0x06, 0x06, 0xFA]);
    static SIGNATURE_ECDSA_P256 = new Uint8Array([0xF7, 0x0A, 0x0A, 0xF7]);
    static CERTIFICATE_ECDSA_P256 = new Uint8Array([0xF3, 0x0B, 0x0B, 0xF3]);
    static VERSION_DEPENDENCY = new Uint8Array([0xEB, 0x17, 0xA6, 0x76]);

    constructor(name, value) {
        this.name = name;
        this.value = value;
    }

    static fromValue(value) {
        const types = [
            ['HEADER_V3', this.HEADER_V3],
            ['BOOTLOADER', this.BOOTLOADER],
            ['APPLICATION', this.APPLICATION],
            ['METADATA', this.METADATA],
            ['PROG', this.PROG],
            ['PROG_LZ4', this.PROG_LZ4],
            ['PROG_LZMA', this.PROG_LZMA],
            ['ERASEPROG', this.ERASEPROG],
            ['SE_UPGRADE', this.SE_UPGRADE],
            ['END', this.END],
            ['TAG', this.TAG],
            ['ENCRYPTION_DATA', this.ENCRYPTION_DATA],
            ['ENCRYPTION_INIT', this.ENCRYPTION_INIT],
            ['SIGNATURE_ECDSA_P256', this.SIGNATURE_ECDSA_P256],
            ['CERTIFICATE_ECDSA_P256', this.CERTIFICATE_ECDSA_P256],
            ['VERSION_DEPENDENCY', this.VERSION_DEPENDENCY]
        ];

        for (const [name, typeValue] of types) {
            if (this.arraysEqual(value, typeValue)) {
                return new GblType(name, typeValue);
            }
        }
        return null;
    }

    static fromName(name) {
        switch(name) {
            case 'HEADER_V3': return new GblType(name, this.HEADER_V3);
            case 'BOOTLOADER': return new GblType(name, this.BOOTLOADER);
            case 'APPLICATION': return new GblType(name, this.APPLICATION);
            case 'METADATA': return new GblType(name, this.METADATA);
            case 'PROG': return new GblType(name, this.PROG);
            case 'PROG_LZ4': return new GblType(name, this.PROG_LZ4);
            case 'PROG_LZMA': return new GblType(name, this.PROG_LZMA);
            case 'ERASEPROG': return new GblType(name, this.ERASEPROG);
            case 'SE_UPGRADE': return new GblType(name, this.SE_UPGRADE);
            case 'END': return new GblType(name, this.END);
            case 'ENCRYPTION_DATA': return new GblType(name, this.ENCRYPTION_DATA);
            case 'ENCRYPTION_INIT': return new GblType(name, this.ENCRYPTION_INIT);
            case 'SIGNATURE_ECDSA_P256': return new GblType(name, this.SIGNATURE_ECDSA_P256);
            case 'CERTIFICATE_ECDSA_P256': return new GblType(name, this.CERTIFICATE_ECDSA_P256);
            case 'VERSION_DEPENDENCY': return new GblType(name, this.VERSION_DEPENDENCY);
            default: return null;
        }
    }

    static arraysEqual(a, b) {
        if (a.length !== b.length) return false;
        for (let i = 0; i < a.length; i++) {
            if (a[i] !== b[i]) return false;
        }
        return true;
    }

    static getValueAsUint32(typeArray) {
        const view = new DataView(typeArray.buffer, typeArray.byteOffset, 4);
        return view.getUint32(0, true);
    }
}

class ParseResult {
    static Success = class {
        constructor(resultList) {
            this.resultList = resultList;
            this.type = 'Success';
        }
    };

    static Fatal = class {
        constructor(error = null) {
            this.error = error;
            this.type = 'Fatal';
        }
    };
}

class ParseTagResult {
    static Success = class {
        constructor(tagHeader, tagData) {
            this.tagHeader = tagHeader;
            this.tagData = tagData;
            this.type = 'Success';
        }
    };

    static Fatal = class {
        constructor(error = null) {
            this.error = error;
            this.type = 'Fatal';
        }
    };
}

class ContainerResult {
    static Success = class {
        constructor(data) {
            this.data = data;
            this.type = 'Success';
        }
    };

    static Error = class {
        constructor(message, code) {
            this.message = message;
            this.code = code;
            this.type = 'Error';
        }
    };
}

const ContainerErrorCode = {
    CONTAINER_NOT_CREATED: 'CONTAINER_NOT_CREATED',
    PROTECTED_TAG_VIOLATION: 'PROTECTED_TAG_VIOLATION',
    TAG_NOT_FOUND: 'TAG_NOT_FOUND',
    INTERNAL_ERROR: 'INTERNAL_ERROR'
};

class TagHeader {
    constructor(id, length) {
        this.id = id;
        this.length = length;
    }

    content() {
        const buffer = new ArrayBuffer(8);
        const view = new DataView(buffer);
        
        const idValue = GblType.getValueAsUint32(this.id);
        view.setUint32(0, idValue, true);
        view.setUint32(4, this.length, true);
        
        return new Uint8Array(buffer);
    }
}

class Tag {
    constructor() {
        if (this.constructor === Tag) {
            throw new Error("Tag is an abstract class and cannot be instantiated");
        }
    }

    copy() {
        throw new Error("copy method must be implemented");
    }

    content() {
        return this.generateTagData();
    }

    generateTagData() {
        return new Uint8Array(0);
    }
}

class GblHeader extends Tag {
    constructor(tagHeader, tagType, version, gblType, tagData) {
        super();
        this.tagHeader = tagHeader;
        this.tagType = tagType;
        this.version = version;
        this.gblType = gblType;
        this.tagData = tagData;
    }

    copy() {
        return new GblHeader(
            this.tagHeader,
            this.tagType,
            this.version,
            this.gblType,
            new Uint8Array(0)
        );
    }

    generateTagData() {
        const buffer = new ArrayBuffer(8);
        const view = new DataView(buffer);
        view.setUint32(0, this.version, true);
        view.setUint32(4, this.gblType, true);
        return new Uint8Array(buffer);
    }
}

class GblBootloader extends Tag {
    constructor(tagHeader, tagType, bootloaderVersion, address, data, tagData) {
        super();
        this.tagHeader = tagHeader;
        this.tagType = tagType;
        this.bootloaderVersion = bootloaderVersion;
        this.address = address;
        this.data = data;
        this.tagData = tagData;
    }

    copy() {
        return new GblBootloader(
            this.tagHeader,
            this.tagType,
            this.bootloaderVersion,
            this.address,
            this.data,
            new Uint8Array(0)
        );
    }

    generateTagData() {
        const buffer = new ArrayBuffer(8 + this.data.length);
        const view = new DataView(buffer);
        view.setUint32(0, this.bootloaderVersion, true);
        view.setUint32(4, this.address, true);
        
        const result = new Uint8Array(buffer);
        result.set(this.data, 8);
        return result;
    }
}

class ApplicationData {
    static APP_TYPE = 32;
    static APP_VERSION = 5;
    static APP_CAPABILITIES = 0;
    static APP_PRODUCT_ID = 54;

    constructor(type, version, capabilities, productId) {
        this.type = type;
        this.version = version;
        this.capabilities = capabilities;
        this.productId = productId;
    }

    content() {
        const buffer = new ArrayBuffer(13);
        const view = new DataView(buffer);
        view.setUint32(0, this.type, true);
        view.setUint32(4, this.version, true);
        view.setUint32(8, this.capabilities, true);
        view.setUint8(12, this.productId);
        return new Uint8Array(buffer);
    }
}

class GblApplication extends Tag {
    constructor(tagHeader, tagType, applicationData, tagData) {
        super();
        this.tagHeader = tagHeader;
        this.tagType = tagType;
        this.applicationData = applicationData;
        this.tagData = tagData;
    }

    copy() {
        return new GblApplication(
            this.tagHeader,
            this.tagType,
            this.applicationData,
            new Uint8Array(0)
        );
    }

    generateTagData() {
        const appData = this.applicationData.content();
        if (this.tagHeader.length > 13) {
            const additionalData = this.tagData.slice(13);
            const result = new Uint8Array(appData.length + additionalData.length);
            result.set(appData);
            result.set(additionalData, appData.length);
            return result;
        }
        return appData;
    }
}

class GblProg extends Tag {
    constructor(tagHeader, tagType, flashStartAddress, data, tagData) {
        super();
        this.tagHeader = tagHeader;
        this.tagType = tagType;
        this.flashStartAddress = flashStartAddress;
        this.data = data;
        this.tagData = tagData;
    }

    copy() {
        return new GblProg(
            this.tagHeader,
            this.tagType,
            this.flashStartAddress,
            this.data,
            new Uint8Array(0)
        );
    }

    generateTagData() {
        const buffer = new ArrayBuffer(4 + this.data.length);
        const view = new DataView(buffer);
        view.setUint32(0, this.flashStartAddress, true);
        
        const result = new Uint8Array(buffer);
        result.set(this.data, 4);
        return result;
    }
}

class GblEraseProg extends Tag {
    constructor(tagHeader, tagType, tagData) {
        super();
        this.tagHeader = tagHeader;
        this.tagType = tagType;
        this.tagData = tagData;
    }

    copy() {
        return new GblEraseProg(
            this.tagHeader,
            this.tagType,
            new Uint8Array(0)
        );
    }

    generateTagData() {
        return new Uint8Array(8);
    }
}

class GblEnd extends Tag {
    constructor(tagHeader, tagType, gblCrc, tagData) {
        super();
        this.tagHeader = tagHeader;
        this.tagType = tagType;
        this.gblCrc = gblCrc;
        this.tagData = tagData;
    }

    copy() {
        return new GblEnd(
            this.tagHeader,
            this.tagType,
            this.gblCrc,
            new Uint8Array(0)
        );
    }

    generateTagData() {
        const buffer = new ArrayBuffer(4);
        const view = new DataView(buffer);
        view.setUint32(0, this.gblCrc, true);
        return new Uint8Array(buffer);
    }
}

class DefaultTag extends Tag {
    constructor(tagHeader, tagType, tagData) {
        super();
        this.tagHeader = tagHeader;
        this.tagType = tagType;
        this.tagData = tagData;
    }

    copy() {
        return new DefaultTag(
            this.tagHeader,
            this.tagType,
            new Uint8Array(0)
        );
    }

    generateTagData() {
        return this.tagData;
    }
}

// Modified TagContainer to use array instead of Set for better index support
class TagContainer {
    static GBL_TAG_ID_HEADER_V3 = GblType.HEADER_V3;
    static HEADER_SIZE = 8;
    static HEADER_VERSION = 50331648;
    static HEADER_GBL_TYPE = 0;
    static PROTECTED_TAG_TYPES = new Set(['HEADER_V3', 'END']);

    constructor() {
        this.tags = []; // Changed from Set to Array
        this.isCreated = false;
    }

    create() {
        try {
            if (this.isCreated) {
                return new ContainerResult.Success(null);
            }

            this.tags = [];

            const headerTag = this.createHeaderTag();
            this.tags.push(headerTag);

            const endTag = this.createEndTag();
            this.tags.push(endTag);

            this.isCreated = true;
            return new ContainerResult.Success(null);
        } catch (e) {
            return new ContainerResult.Error(
                `Failed to create container: ${e.message}`,
                ContainerErrorCode.INTERNAL_ERROR
            );
        }
    }

    add(tag) {
        try {
            if (!this.isCreated) {
                return new ContainerResult.Error(
                    "Container must be created before adding tags. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                );
            }

            if (this.isProtectedTag(tag)) {
                return new ContainerResult.Error(
                    `Cannot add protected tag: ${tag.tagType.name}. Protected tags are managed automatically.`,
                    ContainerErrorCode.PROTECTED_TAG_VIOLATION
                );
            }

            // Insert before the END tag
            const endIndex = this.tags.findIndex(t => t.tagType.name === 'END');
            if (endIndex !== -1) {
                this.tags.splice(endIndex, 0, tag);
            } else {
                this.tags.push(tag);
            }
            
            return new ContainerResult.Success(null);
        } catch (e) {
            return new ContainerResult.Error(
                `Failed to add tag: ${e.message}`,
                ContainerErrorCode.INTERNAL_ERROR
            );
        }
    }

    remove(tag) {
        try {
            if (!this.isCreated) {
                return new ContainerResult.Error(
                    "Container must be created before removing tags. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                );
            }

            if (this.isProtectedTag(tag)) {
                return new ContainerResult.Error(
                    `Cannot remove protected tag: ${tag.tagType.name}. Protected tags are managed automatically.`,
                    ContainerErrorCode.PROTECTED_TAG_VIOLATION
                );
            }

            const index = this.tags.indexOf(tag);
            if (index === -1) {
                return new ContainerResult.Error(
                    `Tag not found in container: ${tag.tagType.name}`,
                    ContainerErrorCode.TAG_NOT_FOUND
                );
            }

            this.tags.splice(index, 1);
            return new ContainerResult.Success(null);
        } catch (e) {
            return new ContainerResult.Error(
                `Failed to remove tag: ${e.message}`,
                ContainerErrorCode.INTERNAL_ERROR
            );
        }
    }

    removeAt(index) {
        try {
            if (!this.isCreated) {
                return new ContainerResult.Error(
                    "Container must be created before removing tags. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                );
            }

            if (index < 0 || index >= this.tags.length) {
                return new ContainerResult.Error(
                    `Invalid index: ${index}`,
                    ContainerErrorCode.TAG_NOT_FOUND
                );
            }

            const tag = this.tags[index];
            if (this.isProtectedTag(tag)) {
                return new ContainerResult.Error(
                    `Cannot remove protected tag: ${tag.tagType.name}. Protected tags are managed automatically.`,
                    ContainerErrorCode.PROTECTED_TAG_VIOLATION
                );
            }

            this.tags.splice(index, 1);
            return new ContainerResult.Success(null);
        } catch (e) {
            return new ContainerResult.Error(
                `Failed to remove tag: ${e.message}`,
                ContainerErrorCode.INTERNAL_ERROR
            );
        }
    }

    updateAt(index, newTag) {
        try {
            if (!this.isCreated) {
                return new ContainerResult.Error(
                    "Container must be created before updating tags. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                );
            }

            if (index < 0 || index >= this.tags.length) {
                return new ContainerResult.Error(
                    `Invalid index: ${index}`,
                    ContainerErrorCode.TAG_NOT_FOUND
                );
            }

            const oldTag = this.tags[index];
            if (this.isProtectedTag(oldTag) && oldTag.tagType.name !== newTag.tagType.name) {
                return new ContainerResult.Error(
                    `Cannot replace protected tag: ${oldTag.tagType.name}`,
                    ContainerErrorCode.PROTECTED_TAG_VIOLATION
                );
            }

            this.tags[index] = newTag;
            return new ContainerResult.Success(null);
        } catch (e) {
            return new ContainerResult.Error(
                `Failed to update tag: ${e.message}`,
                ContainerErrorCode.INTERNAL_ERROR
            );
        }
    }

    build() {
        try {
            if (!this.isCreated) {
                return new ContainerResult.Error(
                    "Container must be created before building. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                );
            }

            const sortedTags = [];

            const headerTag = this.tags.find(tag => tag.tagType.name === 'HEADER_V3');
            if (headerTag) {
                sortedTags.push(headerTag);
            }

            this.tags
                .filter(tag => !TagContainer.PROTECTED_TAG_TYPES.has(tag.tagType.name))
                .sort((a, b) => {
                    const aValue = GblType.getValueAsUint32(a.tagType.value);
                    const bValue = GblType.getValueAsUint32(b.tagType.value);
                    return aValue - bValue;
                })
                .forEach(tag => sortedTags.push(tag));

            const endTag = this.tags.find(tag => tag.tagType.name === 'END');
            if (endTag) {
                sortedTags.push(endTag);
            }

            return new ContainerResult.Success(sortedTags);
        } catch (e) {
            return new ContainerResult.Error(
                `Failed to build container: ${e.message}`,
                ContainerErrorCode.INTERNAL_ERROR
            );
        }
    }

    hasTag(tagTypeName) {
        if (!this.isCreated) return false;
        return this.tags.some(tag => tag.tagType.name === tagTypeName);
    }

    getTag(tagTypeName) {
        if (!this.isCreated) return null;
        return this.tags.find(tag => tag.tagType.name === tagTypeName);
    }

    isEmpty() {
        return !this.isCreated || this.tags.length === TagContainer.PROTECTED_TAG_TYPES.size;
    }

    size() {
        return !this.isCreated ? 0 : this.tags.length;
    }

    getTagTypes() {
        if (!this.isCreated) return new Set();
        return new Set(this.tags.map(tag => tag.tagType.name));
    }

    clear() {
        try {
            if (!this.isCreated) {
                return new ContainerResult.Error(
                    "Container must be created before clearing. Call create() first.",
                    ContainerErrorCode.CONTAINER_NOT_CREATED
                );
            }

            this.tags = this.tags.filter(tag => this.isProtectedTag(tag));
            return new ContainerResult.Success(null);
        } catch (e) {
            return new ContainerResult.Error(
                `Failed to clear container: ${e.message}`,
                ContainerErrorCode.INTERNAL_ERROR
            );
        }
    }

    isProtectedTag(tag) {
        return TagContainer.PROTECTED_TAG_TYPES.has(tag.tagType.name);
    }

    createHeaderTag() {
        const headerType = new GblType('HEADER_V3', GblType.HEADER_V3);
        const header = new GblHeader(
            new TagHeader(
                GblType.HEADER_V3,
                TagContainer.HEADER_SIZE
            ),
            headerType,
            TagContainer.HEADER_VERSION,
            TagContainer.HEADER_GBL_TYPE,
            new Uint8Array(0)
        );

        return new GblHeader(
            header.tagHeader,
            header.tagType,
            header.version,
            header.gblType,
            header.generateTagData()
        );
    }

    createEndTag() {
        const endType = new GblType('END', GblType.END);
        return new GblEnd(
            new TagHeader(
                GblType.END,
                0
            ),
            endType,
            0,
            new Uint8Array(0)
        );
    }
}

class Utils {
    static getIntFromBytes(byteArray, offset = 0, length = 4) {
        const view = new DataView(byteArray.buffer, byteArray.byteOffset + offset, length);
        return view.getUint32(0, true);
    }

    static putUIntToByteArray(array, offset, value) {
        const view = new DataView(array.buffer, array.byteOffset + offset, 4);
        view.setUint32(0, value, true);
    }

    static appendByteArrays(first, second) {
        const result = new Uint8Array(first.length + second.length);
        result.set(first, 0);
        result.set(second, first.length);
        return result;
    }

    static calculateCRC32(data) {
        const table = [];
        for (let i = 0; i < 256; i++) {
            let c = i;
            for (let j = 0; j < 8; j++) {
                c = (c & 1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1);
            }
            table[i] = c;
        }

        let crc = 0 ^ (-1);
        for (let i = 0; i < data.length; i++) {
            crc = (crc >>> 8) ^ table[(crc ^ data[i]) & 0xFF];
        }
        return (crc ^ (-1)) >>> 0;
    }
}

class Gbl {
    static HEADER_SIZE = 8;
    static TAG_ID_SIZE = 4;
    static TAG_LENGTH_SIZE = 4;

    parseByteArray(byteArray) {
        let offset = 0;
        const size = byteArray.length;
        const rawTags = [];

        if (byteArray.length < Gbl.HEADER_SIZE) {
            return new ParseResult.Fatal(`File is too small to be a valid gbl file. Expected at least ${Gbl.HEADER_SIZE} bytes, got ${byteArray.length} bytes.`);
        }

        while (offset < size) {
            const result = this.parseTag(byteArray, offset);

            if (result.type === 'Fatal') {
                break;
            }

            if (result.type === 'Success') {
                const { tagHeader, tagData } = result;

                try {
                    const parsedTag = this.parseTagType(
                        tagHeader.id,
                        tagHeader.length,
                        tagData
                    );

                    rawTags.push(parsedTag);
                    offset += Gbl.TAG_ID_SIZE + Gbl.TAG_LENGTH_SIZE + tagHeader.length;
                } catch (e) {
                    break;
                }
            }
        }

        return new ParseResult.Success(rawTags);
    }

    parseTag(byteArray, offset = 0) {
        if (offset < 0 || offset + Gbl.TAG_ID_SIZE + Gbl.TAG_LENGTH_SIZE > byteArray.length) {
            return new ParseTagResult.Fatal(`Invalid offset: ${offset}`);
        }

        const tagIdBytes = byteArray.slice(offset, offset + Gbl.TAG_ID_SIZE);
        const tagLength = Utils.getIntFromBytes(byteArray, offset + Gbl.TAG_ID_SIZE, Gbl.TAG_LENGTH_SIZE);

        if (offset + Gbl.TAG_ID_SIZE + Gbl.TAG_LENGTH_SIZE + tagLength > byteArray.length) {
            return new ParseTagResult.Fatal(`Invalid tag length: ${tagLength}`);
        }

        const tagData = byteArray.slice(
            offset + Gbl.TAG_ID_SIZE + Gbl.TAG_LENGTH_SIZE,
            offset + Gbl.TAG_ID_SIZE + Gbl.TAG_LENGTH_SIZE + tagLength
        );

        const tagHeader = new TagHeader(tagIdBytes, tagLength);

        return new ParseTagResult.Success(tagHeader, tagData);
    }

    parseTagType(tagId, length, byteArray) {
        const tagType = GblType.fromValue(tagId);
        const tagHeader = new TagHeader(tagId, length);

        if (!tagType) {
            return new DefaultTag(tagHeader, new GblType('TAG', GblType.TAG), byteArray);
        }

        switch (tagType.name) {
            case 'HEADER_V3':
                const version = Utils.getIntFromBytes(byteArray, 0, 4);
                const gblType = Utils.getIntFromBytes(byteArray, 4, 4);
                return new GblHeader(tagHeader, tagType, version, gblType, byteArray);

            case 'BOOTLOADER':
                return new GblBootloader(
                    tagHeader,
                    tagType,
                    Utils.getIntFromBytes(byteArray, 0, 4),
                    Utils.getIntFromBytes(byteArray, 4, 4),
                    byteArray.slice(8),
                    byteArray
                );

            case 'APPLICATION':
                const appData = new ApplicationData(
                    Utils.getIntFromBytes(byteArray, 0, 4),
                    Utils.getIntFromBytes(byteArray, 4, 4),
                    Utils.getIntFromBytes(byteArray, 8, 4),
                    byteArray[12]
                );
                return new GblApplication(tagHeader, tagType, appData, byteArray);

            case 'PROG':
                return new GblProg(
                    tagHeader,
                    tagType,
                    Utils.getIntFromBytes(byteArray, 0, 4),
                    byteArray.slice(4),
                    byteArray
                );

            case 'ERASEPROG':
                return new GblEraseProg(tagHeader, tagType, byteArray);

            case 'END':
                return new GblEnd(
                    tagHeader,
                    tagType,
                    Utils.getIntFromBytes(byteArray, 0, 4),
                    byteArray
                );

            default:
                return new DefaultTag(tagHeader, tagType, byteArray);
        }
    }

    encode(tags) {
        const tagsWithoutEnd = tags.filter(tag => !(tag instanceof GblEnd));
        const endTag = this.createEndTagWithCrc(tagsWithoutEnd);
        const finalTags = [...tagsWithoutEnd, endTag];
        return this.encodeTags(finalTags);
    }

    encodeTags(tags) {
        const totalSize = this.calculateTotalSize(tags);
        const buffer = new ArrayBuffer(totalSize);
        const view = new DataView(buffer);
        let offset = 0;

        for (const tag of tags) {
            if (!tag.tagHeader) continue;

            const idValue = GblType.getValueAsUint32(tag.tagHeader.id);
            view.setUint32(offset, idValue, true);
            offset += 4;

            view.setUint32(offset, tag.tagHeader.length, true);
            offset += 4;

            const tagData = tag.generateTagData();
            const result = new Uint8Array(buffer);
            result.set(tagData, offset);
            offset += tagData.length;
        }

        return new Uint8Array(buffer);
    }

    calculateTotalSize(tags) {
        return tags.reduce((total, tag) => {
            if (tag.tagHeader) {
                return total + Gbl.TAG_ID_SIZE + Gbl.TAG_LENGTH_SIZE + tag.tagHeader.length;
            }
            return total;
        }, 0);
    }

    createEndTagWithCrc(tags) {
        let crcData = new Uint8Array(0);

        for (const tag of tags) {
            if (!tag.tagHeader) continue;

            const tagIdBuffer = new ArrayBuffer(4);
            const tagIdView = new DataView(tagIdBuffer);
            tagIdView.setUint32(0, GblType.getValueAsUint32(tag.tagHeader.id), true);

            const tagLengthBuffer = new ArrayBuffer(4);
            const tagLengthView = new DataView(tagLengthBuffer);
            tagLengthView.setUint32(0, tag.tagHeader.length, true);

            const tagData = tag.generateTagData();

            crcData = Utils.appendByteArrays(crcData, new Uint8Array(tagIdBuffer));
            crcData = Utils.appendByteArrays(crcData, new Uint8Array(tagLengthBuffer));
            crcData = Utils.appendByteArrays(crcData, tagData);
        }

        const endTagIdBuffer = new ArrayBuffer(4);
        const endTagIdView = new DataView(endTagIdBuffer);
        endTagIdView.setUint32(0, GblType.getValueAsUint32(GblType.END), true);

        const endTagLengthBuffer = new ArrayBuffer(4);
        const endTagLengthView = new DataView(endTagLengthBuffer);
        endTagLengthView.setUint32(0, 4, true);

        crcData = Utils.appendByteArrays(crcData, new Uint8Array(endTagIdBuffer));
        crcData = Utils.appendByteArrays(crcData, new Uint8Array(endTagLengthBuffer));

        const crcValue = Utils.calculateCRC32(crcData);

        const endType = new GblType('END', GblType.END);
        return new GblEnd(
            new TagHeader(GblType.END, 4),
            endType,
            crcValue,
            new Uint8Array(4)
        );
    }

    static GblBuilder = class {
        constructor() {
            this.container = new TagContainer();
        }

        static create() {
            const builder = new Gbl.GblBuilder();
            builder.container.create();
            return builder;
        }

        static empty() {
            return new Gbl.GblBuilder();
        }

        application(type = ApplicationData.APP_TYPE, version = ApplicationData.APP_VERSION, 
                   capabilities = ApplicationData.APP_CAPABILITIES, productId = ApplicationData.APP_PRODUCT_ID, 
                   additionalData = new Uint8Array(0)) {
            const applicationData = new ApplicationData(type, version, capabilities, productId);
            const tagData = Utils.appendByteArrays(applicationData.content(), additionalData);

            const appType = new GblType('APPLICATION', GblType.APPLICATION);
            const tag = new GblApplication(
                new TagHeader(GblType.APPLICATION, tagData.length),
                appType,
                applicationData,
                tagData
            );

            this.container.add(tag);
            return this;
        }

        bootloader(bootloaderVersion, address, data) {
            const bootType = new GblType('BOOTLOADER', GblType.BOOTLOADER);
            const tag = new GblBootloader(
                new TagHeader(GblType.BOOTLOADER, 8 + data.length),
                bootType,
                bootloaderVersion,
                address,
                data,
                new Uint8Array(8 + data.length)
            );

            this.container.add(tag);
            return this;
        }

        metadata(data) {
            const metaType = new GblType('METADATA', GblType.METADATA);
            const tag = new DefaultTag(
                new TagHeader(GblType.METADATA, data.length),
                metaType,
                data
            );

            this.container.add(tag);
            return this;
        }

        prog(flashStartAddress, data) {
            const progType = new GblType('PROG', GblType.PROG);
            const tag = new GblProg(
                new TagHeader(GblType.PROG, 4 + data.length),
                progType,
                flashStartAddress,
                data,
                new Uint8Array(4 + data.length)
            );

            this.container.add(tag);
            return this;
        }

        eraseProg() {
            const eraseType = new GblType('ERASEPROG', GblType.ERASEPROG);
            const tag = new GblEraseProg(
                new TagHeader(GblType.ERASEPROG, 8),
                eraseType,
                new Uint8Array(8)
            );

            this.container.add(tag);
            return this;
        }

        get() {
            const result = this.container.build();
            return result.type === 'Success' ? result.data : this.container.tags;
        }

        buildToList() {
            const tags = this.get();
            const tagsWithoutEnd = tags.filter(tag => !(tag instanceof GblEnd));
            const gbl = new Gbl();
            const endTag = gbl.createEndTagWithCrc(tagsWithoutEnd);
            return [...tagsWithoutEnd, endTag];
        }

        buildToByteArray() {
            const tags = this.buildToList();
            const gbl = new Gbl();
            return gbl.encodeTags(tags);
        }

        hasTag(tagTypeName) {
            return this.container.hasTag(tagTypeName);
        }

        getTag(tagTypeName) {
            return this.container.getTag(tagTypeName);
        }

        removeTag(tag) {
            return this.container.remove(tag);
        }

        removeTagAt(index) {
            return this.container.removeAt(index);
        }

        updateTagAt(index, newTag) {
            return this.container.updateAt(index, newTag);
        }

        clear() {
            return this.container.clear();
        }

        size() {
            return this.container.size();
        }

        isEmpty() {
            return this.container.isEmpty();
        }

        getTagTypes() {
            return this.container.getTagTypes();
        }
    };
}