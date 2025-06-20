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

// Main application logic for GBL Parser & Builder

// Global variables
let gblBuilder = null;
let builderTags = [];
let currentEditingTag = null;
let currentEditingIndex = null;
let selectedFile = null;

// DOM elements
const mainContainer = document.getElementById('mainContainer');
const uploadArea = document.getElementById('uploadArea');
const fileInput = document.getElementById('fileInput');
const fileInfo = document.getElementById('fileInfo');
const fileName = document.getElementById('fileName');
const fileSize = document.getElementById('fileSize');
const parseBtn = document.getElementById('parseBtn');
const loading = document.getElementById('loading');
const results = document.getElementById('results');
const error = document.getElementById('error');
const success = document.getElementById('success');
const summaryStats = document.getElementById('summaryStats');
const tagList = document.getElementById('tagList');

// Builder UI elements
const tagContainer = document.getElementById('tagContainer');
const saveBtn = document.getElementById('saveBtn');
const builderError = document.getElementById('builderError');
const builderSuccess = document.getElementById('builderSuccess');
const modal = document.getElementById('tagEditModal');
const modalTitle = document.getElementById('modalTitle');
const modalSubtitle = document.getElementById('modalSubtitle');
const formFields = document.getElementById('formFields');
const tagEditForm = document.getElementById('tagEditForm');

// Initialize application
function initializeApp() {
    gblBuilder = Gbl.GblBuilder.create();
    updateBuilderView();
}

// Utility functions
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function showError(message) {
    error.textContent = message;
    error.style.display = 'block';
    setTimeout(() => {
        error.style.display = 'none';
    }, 5000);
}

function showSuccess(message) {
    success.textContent = message;
    success.style.display = 'block';
}

function showBuilderError(message) {
    builderError.textContent = message;
    builderError.style.display = 'block';
    setTimeout(() => {
        builderError.style.display = 'none';
    }, 5000);
}

function showBuilderSuccess(message) {
    builderSuccess.textContent = message;
    builderSuccess.style.display = 'block';
    setTimeout(() => {
        builderSuccess.style.display = 'none';
    }, 3000);
}

function getTagDisplayName(tagName) {
    const nameMap = {
        'HEADER_V3': 'Header (v3)',
        'APPLICATION': 'Application',
        'BOOTLOADER': 'Bootloader',
        'PROG': 'Program Data',
        'PROG_LZ4': 'Program Data (LZ4)',
        'PROG_LZMA': 'Program Data (LZMA)',
        'ERASEPROG': 'Erase Memory',
        'METADATA': 'Metadata',
        'END': 'End of File',
        'ENCRYPTION_DATA': 'Encrypted Data',
        'ENCRYPTION_INIT': 'Encryption Init',
        'SIGNATURE_ECDSA_P256': 'Digital Signature',
        'CERTIFICATE_ECDSA_P256': 'Certificate',
        'SE_UPGRADE': 'SE Upgrade',
        'VERSION_DEPENDENCY': 'Version Dependency'
    };
    return nameMap[tagName] || tagName;
}

function formatTagInfo(tag) {
    const tagName = tag.tagType.name;
    let details = {};
    
    switch (tagName) {
        case 'HEADER_V3':
            details = {
                'Version': tag.version,
                'GBL Type': tag.gblType,
                'Size': `${tag.tagHeader.length} bytes`
            };
            break;
        case 'APPLICATION':
            details = {
                'Application Type': tag.applicationData.type,
                'Version': `0x${tag.applicationData.version.toString(16).toUpperCase()}`,
                'Capabilities': tag.applicationData.capabilities,
                'Product ID': tag.applicationData.productId,
                'Size': `${tag.tagHeader.length} bytes`
            };
            break;
        case 'BOOTLOADER':
            details = {
                'Bootloader Version': `0x${tag.bootloaderVersion.toString(16).toUpperCase()}`,
                'Address': `0x${tag.address.toString(16).toUpperCase()}`,
                'Data Size': `${tag.data.length} bytes`,
                'Total Size': `${tag.tagHeader.length} bytes`
            };
            break;
        case 'PROG':
            details = {
                'Flash Address': `0x${tag.flashStartAddress.toString(16).toUpperCase()}`,
                'Data Size': `${tag.data.length} bytes`,
                'Total Size': `${tag.tagHeader.length} bytes`
            };
            break;
        case 'ERASEPROG':
            details = {
                'Type': 'Memory erase command',
                'Size': `${tag.tagHeader.length} bytes`
            };
            break;
        case 'END':
            details = {
                'CRC32': `0x${tag.gblCrc.toString(16).toUpperCase()}`,
                'Size': `${tag.tagHeader.length} bytes`
            };
            break;
        default:
            details = {
                'Type': 'Unknown tag',
                'Size': `${tag.tagHeader.length} bytes`
            };
    }
    
    return { name: getTagDisplayName(tagName), details };
}

// Tab switching functionality
document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
        const targetTab = tab.dataset.tab;
        
        // Update tab buttons
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        
        // Update tab content
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.remove('active');
        });
        document.getElementById(targetTab).classList.add('active');
        
        // Update container width based on active tab
        if (targetTab === 'parser') {
            mainContainer.classList.add('parser-active');
        } else {
            mainContainer.classList.remove('parser-active');
        }
    });
});

// Parser event handlers
uploadArea.addEventListener('click', () => {
    fileInput.click();
});

fileInput.addEventListener('change', handleFileSelect);

uploadArea.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadArea.classList.add('dragover');
});

uploadArea.addEventListener('dragleave', (e) => {
    e.preventDefault();
    uploadArea.classList.remove('dragover');
});

uploadArea.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadArea.classList.remove('dragover');
    
    const files = e.dataTransfer.files;
    if (files.length > 0) {
        handleFile(files[0]);
    }
});

function handleFileSelect(e) {
    if (e.target.files.length > 0) {
        handleFile(e.target.files[0]);
    }
}

function handleFile(file) {
    if (!file.name.toLowerCase().endsWith('.gbl')) {
        showError('Please select a file with .gbl extension');
        return;
    }

    selectedFile = file;
    
    fileName.textContent = file.name;
    fileSize.textContent = formatFileSize(file.size);
    fileInfo.style.display = 'block';
    parseBtn.style.display = 'inline-block';
    
    results.style.display = 'none';
    error.style.display = 'none';
    success.style.display = 'none';
}

parseBtn.addEventListener('click', async () => {
    if (!selectedFile) return;
    
    loading.style.display = 'block';
    parseBtn.style.display = 'none';
    error.style.display = 'none';
    success.style.display = 'none';
    results.style.display = 'none';
    
    try {
        const arrayBuffer = await selectedFile.arrayBuffer();
        const uint8Array = new Uint8Array(arrayBuffer);
        
        const gbl = new Gbl();
        const parseResult = gbl.parseByteArray(uint8Array);
        
        loading.style.display = 'none';
        parseBtn.style.display = 'inline-block';
        
        if (parseResult.type === 'Success') {
            const tags = parseResult.resultList;
            showSuccess(`File successfully parsed! Found ${tags.length} tags.`);
            displayResults(tags, uint8Array.length);
        } else {
            showError(`Parse error: ${parseResult.error}`);
        }
        
    } catch (err) {
        loading.style.display = 'none';
        parseBtn.style.display = 'inline-block';
        showError(`File reading error: ${err.message}`);
    }
});

function displayResults(tags, fileSize) {
    const tagCounts = {};
    let totalDataSize = 0;
    
    tags.forEach(tag => {
        const tagName = tag.tagType.name;
        tagCounts[tagName] = (tagCounts[tagName] || 0) + 1;
        totalDataSize += tag.tagHeader.length;
    });

    summaryStats.innerHTML = `
        <div class="stat-item">
            <span class="stat-value">${tags.length}</span>
            <span class="stat-label">Total Tags</span>
        </div>
        <div class="stat-item">
            <span class="stat-value">${formatFileSize(fileSize)}</span>
            <span class="stat-label">File Size</span>
        </div>
        <div class="stat-item">
            <span class="stat-value">${formatFileSize(totalDataSize)}</span>
            <span class="stat-label">Data Size</span>
        </div>
        <div class="stat-item">
            <span class="stat-value">${Object.keys(tagCounts).length}</span>
            <span class="stat-label">Tag Types</span>
        </div>
    `;

    tagList.innerHTML = '';
    
    tags.forEach((tag, index) => {
        const tagInfo = formatTagInfo(tag);
        const tagElement = document.createElement('div');
        tagElement.className = 'tag-item';
        
        const detailsHtml = Object.entries(tagInfo.details)
            .map(([label, value]) => `
                <div class="detail-item">
                    <span class="detail-label">${label}:</span>
                    <span class="detail-value">${value}</span>
                </div>
            `).join('');

        tagElement.innerHTML = `
            <div class="tag-header">
                <div class="tag-name">${tagInfo.name}</div>
                <div class="tag-type">${tag.tagType.name}</div>
            </div>
            <div class="tag-details">
                ${detailsHtml}
            </div>
        `;
        
        tagList.appendChild(tagElement);
    });

    results.style.display = 'block';
}

// Builder drag and drop functionality
let draggedElement = null;

document.querySelectorAll('.draggable-tag').forEach(tag => {
    tag.addEventListener('dragstart', (e) => {
        draggedElement = e.target;
        e.target.classList.add('dragging');
        e.dataTransfer.effectAllowed = 'copy';
        e.dataTransfer.setData('text/plain', e.target.dataset.tagType);
    });

    tag.addEventListener('dragend', (e) => {
        e.target.classList.remove('dragging');
        draggedElement = null;
    });
});

tagContainer.addEventListener('dragover', (e) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'copy';
    tagContainer.classList.add('drag-over');
});

tagContainer.addEventListener('dragleave', (e) => {
    // Only remove the class if we're leaving the container entirely
    if (!tagContainer.contains(e.relatedTarget)) {
        tagContainer.classList.remove('drag-over');
    }
});

tagContainer.addEventListener('drop', (e) => {
    e.preventDefault();
    tagContainer.classList.remove('drag-over');
    
    const tagType = e.dataTransfer.getData('text/plain');
    if (tagType) {
        addTagToBuilder(tagType);
    }
});

function addTagToBuilder(tagTypeName) {
    try {
        switch(tagTypeName) {
            case 'APPLICATION':
                gblBuilder.application();
                showBuilderSuccess('Application tag added');
                break;
            case 'PROG':
                gblBuilder.prog(0, new Uint8Array([0x00, 0x01, 0x02, 0x03]));
                showBuilderSuccess('Program data tag added');
                break;
            case 'ERASEPROG':
                gblBuilder.eraseProg();
                showBuilderSuccess('Erase program tag added');
                break;
            case 'BOOTLOADER':
                gblBuilder.bootloader(0x20000000, 0x20000000, new Uint8Array([0x00, 0x01, 0x02, 0x03]));
                showBuilderSuccess('Bootloader tag added');
                break;
            case 'METADATA':
                gblBuilder.metadata(new Uint8Array([0x00, 0x01, 0x02, 0x03]));
                showBuilderSuccess('Metadata tag added');
                break;
            default:
                // For other tag types, add them manually to the container
                try {
                    const tagType = GblType.fromName(tagTypeName);
                    if (tagType) {
                        const tag = new DefaultTag(
                            new TagHeader(tagType.value, 4),
                            tagType,
                            new Uint8Array([0x00, 0x01, 0x02, 0x03])
                        );
                        gblBuilder.container.add(tag);
                        showBuilderSuccess(`${getTagDisplayName(tagTypeName)} tag added`);
                    }
                } catch (innerErr) {
                    showBuilderError(`Tag type ${tagTypeName} not supported yet`);
                }
                break;
        }
        updateBuilderView();
    } catch (err) {
        showBuilderError(`Failed to add tag: ${err.message}`);
    }
}

function updateBuilderView() {
    try {
        console.log('Updating builder view...');
        
        // Completely clear the container
        tagContainer.innerHTML = '';
        
        const tags = gblBuilder.get();
        console.log('Current tags:', tags ? tags.length : 0);
        
        if (!tags || !Array.isArray(tags) || tags.length === 0) {
            tagContainer.innerHTML = '<div class="empty-container" id="emptyContainer">Drag tags here to create GBL file</div>';
            return;
        }
        
        // Check if we only have default tags (header and end)
        const nonDefaultTags = tags.filter(tag => 
            tag.tagType.name !== 'HEADER_V3' && tag.tagType.name !== 'END'
        );
        
        console.log('Non-default tags:', nonDefaultTags.length);
        
        if (nonDefaultTags.length === 0 && tags.length <= 2) {
            // Show default tags but also show empty message
            tags.forEach((tag, index) => {
                console.log(`Creating element for tag ${index}:`, tag.tagType.name);
                const tagElement = createTagElement(tag, index);
                tagContainer.appendChild(tagElement);
            });
            
            const emptyDiv = document.createElement('div');
            emptyDiv.className = 'empty-container';
            emptyDiv.style.marginTop = '20px';
            emptyDiv.textContent = 'Drag more tags here to build your GBL file';
            tagContainer.appendChild(emptyDiv);
            return;
        }
        
        // Show all tags
        tags.forEach((tag, index) => {
            console.log(`Creating element for tag ${index}:`, tag.tagType.name);
            const tagElement = createTagElement(tag, index);
            tagContainer.appendChild(tagElement);
        });
        
        console.log('Builder view updated successfully');
        
    } catch (err) {
        console.error('Error updating builder view:', err);
        tagContainer.innerHTML = '<div class="empty-container">Error loading tags</div>';
    }
}

function createTagElement(tag, index) {
    const tagElement = document.createElement('div');
    tagElement.className = 'container-tag';
    
    const isProtected = tag.tagType.name === 'HEADER_V3' || tag.tagType.name === 'END';
    const canEdit = tag.tagType.name !== 'END';
    
    if (isProtected) {
        tagElement.classList.add('fixed');
    }
    
    const tagInfo = formatTagInfo(tag);
    
    tagElement.innerHTML = `
        <div class="tag-info">
            <div class="tag-name">${tagInfo.name}</div>
            <div class="tag-details">${Object.entries(tagInfo.details).map(([k,v]) => `${k}: ${v}`).join(', ')}</div>
        </div>
        <div class="tag-actions">
            ${canEdit ? `<button class="tag-action-btn edit" data-index="${index}">✏️ Edit</button>` : ''}
            ${!isProtected ? `<button class="tag-action-btn delete" data-index="${index}">🗑️ Delete</button>` : ''}
        </div>
    `;
    
    // Add event listeners directly instead of using onclick
    if (canEdit) {
        const editBtn = tagElement.querySelector('.edit');
        if (editBtn) {
            editBtn.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                editTag(index);
            });
        }
    }
    
    if (!isProtected) {
        const deleteBtn = tagElement.querySelector('.delete');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                console.log('Delete button clicked for index:', index);
                removeTagFromBuilder(index);
            });
        }
    }
    
    return tagElement;
}

function removeTagFromBuilder(index) {
    try {
        console.log('Removing tag at index:', index);
        
        const result = gblBuilder.removeTagAt(index);
        
        if (result.type === 'Success') {
            showBuilderSuccess('Tag removed');
            updateBuilderView();
        } else {
            showBuilderError(`Failed to remove tag: ${result.message}`);
        }
        
    } catch (err) {
        console.error('Error removing tag:', err);
        showBuilderError(`Failed to remove tag: ${err.message}`);
    }
}

function editTag(index) {
    try {
        const tags = gblBuilder.get();
        
        if (!tags || !Array.isArray(tags)) {
            showBuilderError('No tags available');
            return;
        }
        
        if (index < 0 || index >= tags.length) {
            showBuilderError('Invalid tag index');
            return;
        }
        
        const tag = tags[index];
        
        if (!tag) {
            showBuilderError('Tag not found');
            return;
        }
        
        if (tag.tagType.name === 'END') {
            showBuilderError('End tag cannot be edited');
            return;
        }
        
        currentEditingTag = tag;
        currentEditingIndex = index;
        
        openEditModal(tag);
        
    } catch (err) {
        console.error('Error opening editor:', err);
        showBuilderError(`Failed to open editor: ${err.message}`);
    }
}

function openEditModal(tag) {
    const tagName = tag.tagType.name;
    modalTitle.textContent = `Edit ${getTagDisplayName(tagName)}`;
    modalSubtitle.textContent = `Configure ${tagName} tag parameters`;
    
    // Clear previous form fields
    formFields.innerHTML = '';
    
    // Create form fields based on tag type
    switch (tagName) {
        case 'HEADER_V3':
            formFields.innerHTML = `
                <div class="form-group">
                    <label for="version">Version:</label>
                    <input type="number" id="version" name="version" value="${tag.version || 3}" min="0" max="4294967295">
                </div>
                <div class="form-group">
                    <label for="gblType">GBL Type:</label>
                    <select id="gblType" name="gblType">
                        <option value="0" ${tag.gblType === 0 ? 'selected' : ''}>Application</option>
                        <option value="1" ${tag.gblType === 1 ? 'selected' : ''}>Bootloader</option>
                    </select>
                </div>
            `;
            break;
            
        case 'APPLICATION':
            formFields.innerHTML = `
                <div class="form-group">
                    <label for="appType">Application Type:</label>
                    <input type="number" id="appType" name="appType" value="${tag.applicationData?.type || 32}" min="0" max="4294967295">
                </div>
                <div class="form-group">
                    <label for="appVersion">Version:</label>
                    <input type="number" id="appVersion" name="appVersion" value="${tag.applicationData?.version || 5}" min="0" max="4294967295">
                </div>
                <div class="form-group">
                    <label for="capabilities">Capabilities:</label>
                    <input type="number" id="capabilities" name="capabilities" value="${tag.applicationData?.capabilities || 0}" min="0" max="4294967295">
                </div>
                <div class="form-group">
                    <label for="productId">Product ID:</label>
                    <input type="number" id="productId" name="productId" value="${tag.applicationData?.productId || 54}" min="0" max="255">
                </div>
            `;
            break;
            
        case 'BOOTLOADER':
            formFields.innerHTML = `
                <div class="form-group">
                    <label for="bootloaderVersion">Bootloader Version:</label>
                    <input type="number" id="bootloaderVersion" name="bootloaderVersion" value="${tag.bootloaderVersion || 0}" min="0" max="4294967295">
                </div>
                <div class="form-group">
                    <label for="address">Address:</label>
                    <input type="number" id="address" name="address" value="${tag.address || 0}" min="0" max="4294967295">
                </div>
                <div class="form-group">
                    <label for="data">Data (hex bytes):</label>
                    <input type="text" id="data" name="data" value="${Array.from(tag.data || []).map(b => b.toString(16).padStart(2, '0')).join(' ')}" placeholder="e.g., 00 01 02 03">
                </div>
            `;
            break;
            
        case 'PROG':
            formFields.innerHTML = `
                <div class="form-group">
                    <label for="flashAddress">Flash Address:</label>
                    <input type="number" id="flashAddress" name="flashAddress" value="${tag.flashStartAddress || 0}" min="0" max="4294967295">
                </div>
                <div class="form-group">
                    <label for="progData">Program Data (hex bytes):</label>
                    <textarea id="progData" name="progData" rows="4" placeholder="e.g., 00 01 02 03 04 05 06 07">${Array.from(tag.data || []).map(b => b.toString(16).padStart(2, '0')).join(' ')}</textarea>
                </div>
            `;
            break;
            
        case 'METADATA':
            formFields.innerHTML = `
                <div class="form-group">
                    <label for="metaData">Metadata (hex bytes):</label>
                    <textarea id="metaData" name="metaData" rows="4" placeholder="e.g., 00 01 02 03 04 05 06 07">${Array.from(tag.data || []).map(b => b.toString(16).padStart(2, '0')).join(' ')}</textarea>
                </div>
            `;
            break;
            
        default:
            formFields.innerHTML = `
                <div class="form-group">
                    <label for="genericData">Tag Data (hex bytes):</label>
                    <textarea id="genericData" name="genericData" rows="4" placeholder="e.g., 00 01 02 03 04 05 06 07">${Array.from(tag.data || []).map(b => b.toString(16).padStart(2, '0')).join(' ')}</textarea>
                </div>
            `;
            break;
    }
    
    modal.style.display = 'block';
}

// Save GBL file functionality
saveBtn.addEventListener('click', () => {
    try {
        const fileData = gblBuilder.buildToByteArray();
        
        if (fileData.length === 0) {
            showBuilderError('No data to save');
            return;
        }
        
        const blob = new Blob([fileData], { type: 'application/octet-stream' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'generated.gbl';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        
        showBuilderSuccess('GBL file successfully created and downloaded!');
        
    } catch (err) {
        showBuilderError(`Failed to create file: ${err.message}`);
    }
});

// Modal event listeners
document.querySelector('.close').addEventListener('click', () => {
    modal.style.display = 'none';
});

document.getElementById('cancelBtn').addEventListener('click', () => {
    modal.style.display = 'none';
});

window.addEventListener('click', (e) => {
    if (e.target === modal) {
        modal.style.display = 'none';
    }
});

// Handle form submission
tagEditForm.addEventListener('submit', (e) => {
    e.preventDefault();
    saveTagChanges();
});

function saveTagChanges() {
    try {
        if (!currentEditingTag || currentEditingIndex === null) {
            showBuilderError('No tag selected for editing');
            return;
        }
        
        const formData = new FormData(tagEditForm);
        const tagName = currentEditingTag.tagType.name;
        
        let newTag = null;
        
        // Create new tag based on type with updated values
        switch (tagName) {
            case 'HEADER_V3':
                const version = parseInt(formData.get('version')) || 3;
                const gblType = parseInt(formData.get('gblType')) || 0;
                
                const headerType = new GblType('HEADER_V3', GblType.HEADER_V3);
                newTag = new GblHeader(
                    new TagHeader(GblType.HEADER_V3, 8),
                    headerType,
                    version,
                    gblType,
                    new Uint8Array(0)
                );
                break;
                
            case 'APPLICATION':
                const appType = parseInt(formData.get('appType')) || 32;
                const appVersion = parseInt(formData.get('appVersion')) || 5;
                const capabilities = parseInt(formData.get('capabilities')) || 0;
                const productId = parseInt(formData.get('productId')) || 54;
                
                const applicationData = new ApplicationData(appType, appVersion, capabilities, productId);
                const appTagData = applicationData.content();
                
                const applicationTagType = new GblType('APPLICATION', GblType.APPLICATION);
                newTag = new GblApplication(
                    new TagHeader(GblType.APPLICATION, appTagData.length),
                    applicationTagType,
                    applicationData,
                    appTagData
                );
                break;
                
            case 'BOOTLOADER':
                const bootVersion = parseInt(formData.get('bootloaderVersion')) || 0;
                const address = parseInt(formData.get('address')) || 0;
                const dataStr = formData.get('data') || '';
                const data = parseHexString(dataStr);
                
                const bootloaderType = new GblType('BOOTLOADER', GblType.BOOTLOADER);
                newTag = new GblBootloader(
                    new TagHeader(GblType.BOOTLOADER, 8 + data.length),
                    bootloaderType,
                    bootVersion,
                    address,
                    data,
                    new Uint8Array(8 + data.length)
                );
                break;
                
            case 'PROG':
                const flashAddr = parseInt(formData.get('flashAddress')) || 0;
                const progDataStr = formData.get('progData') || '';
                const progData = parseHexString(progDataStr);
                
                const progType = new GblType('PROG', GblType.PROG);
                newTag = new GblProg(
                    new TagHeader(GblType.PROG, 4 + progData.length),
                    progType,
                    flashAddr,
                    progData,
                    new Uint8Array(4 + progData.length)
                );
                break;
                
            case 'METADATA':
                const metaDataStr = formData.get('metaData') || '';
                const metaData = parseHexString(metaDataStr);
                
                const metadataType = new GblType('METADATA', GblType.METADATA);
                newTag = new DefaultTag(
                    new TagHeader(GblType.METADATA, metaData.length),
                    metadataType,
                    metaData
                );
                break;
                
            default:
                const genericDataStr = formData.get('genericData') || '';
                const genericData = parseHexString(genericDataStr);
                
                newTag = new DefaultTag(
                    new TagHeader(currentEditingTag.tagType.value, genericData.length),
                    currentEditingTag.tagType,
                    genericData
                );
                break;
        }
        
        if (newTag) {
            const result = gblBuilder.updateTagAt(currentEditingIndex, newTag);
            
            if (result.type === 'Success') {
                showBuilderSuccess(`${getTagDisplayName(tagName)} tag updated`);
                updateBuilderView();
                modal.style.display = 'none';
                
                // Reset editing state
                currentEditingTag = null;
                currentEditingIndex = null;
            } else {
                showBuilderError(`Failed to update tag: ${result.message}`);
            }
        } else {
            showBuilderError('Failed to create updated tag');
        }
        
    } catch (err) {
        console.error('Error saving changes:', err);
        showBuilderError(`Failed to save changes: ${err.message}`);
    }
}

function parseHexString(hexStr) {
    if (!hexStr || typeof hexStr !== 'string') {
        return new Uint8Array([]);
    }
    
    // Remove any non-hex characters and split by spaces or every 2 characters
    const cleanHex = hexStr.replace(/[^0-9a-fA-F]/g, '');
    const bytes = [];
    
    for (let i = 0; i < cleanHex.length; i += 2) {
        const hex = cleanHex.substr(i, 2);
        if (hex.length === 2) {
            bytes.push(parseInt(hex, 16));
        }
    }
    
    return new Uint8Array(bytes);
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', initializeApp);