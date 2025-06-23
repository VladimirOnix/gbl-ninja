//
//  Gbl.swift
//  swift-library
//
//  Created by Ruslan on 22.06.2025.
//

// MARK: - Core Types and Enums

import Foundation

// MARK: - GBL Types
enum GblType: UInt32, CaseIterable {
    case headerV3 = 0x03A617EB
    case bootloader = 0xF50909F5
    case application = 0xF40A0AF4
    case metadata = 0xF60808F6
    case prog = 0xFE0101FE
    case progLz4 = 0xFD0505FD
    case progLzma = 0xFD0707FD
    case eraseProg = 0xFD0303FD
    case seUpgrade = 0x5EA617EB
    case end = 0xFC0404FC
    case tag = 0
    case encryptionData = 0xF90707F9
    case encryptionInit = 0xFA0606FA
    case signatureEcdsaP256 = 0xF70A0AF7
    case certificateEcdsaP256 = 0xF30B0BF3
    case versionDependency = 0x76A617EB
    
    static func from(value: UInt32) -> GblType? {
        return GblType(rawValue: value)
    }
}

// MARK: - Result Types
enum ParseResult {
    case success([Tag])
    case fatal(Error?)
}

enum ParseTagResult {
    case success(TagHeader, Data)
    case fatal(Error?)
}

enum ContainerResult<T> {
    case success(T)
    case error(String, ContainerErrorCode)
}

enum ContainerErrorCode {
    case containerNotCreated
    case protectedTagViolation
    case tagNotFound
    case internalError
}

// MARK: - Tag Header
struct TagHeader: Equatable {
    let id: UInt32
    let length: UInt32
    
    func content() -> Data {
        var data = Data()
        data.append(id.littleEndianData)
        data.append(length.littleEndianData)
        return data
    }
}

// MARK: - Base Tag Protocol
protocol Tag {
    var tagType: GblType { get }
    func copy() -> Tag
    func content() -> Data
}

protocol TagWithHeader: Tag {
    var tagHeader: TagHeader { get }
    var tagData: Data { get }
}

// MARK: - Application Data
struct ApplicationData: Equatable {
    let type: UInt32
    let version: UInt32
    let capabilities: UInt32
    let productId: UInt8
    
    static let appType: UInt32 = 32
    static let appVersion: UInt32 = 5
    static let appCapabilities: UInt32 = 0
    static let appProductId: UInt8 = 54
    
    func content() -> Data {
        var data = Data()
        data.append(type.littleEndianData)
        data.append(version.littleEndianData)
        data.append(capabilities.littleEndianData)
        data.append(productId)
        return data
    }
}

// MARK: - Application Certificate
struct ApplicationCertificate: Equatable {
    let structVersion: UInt8
    let flags: UInt8
    let key: UInt8
    let version: UInt32
    let signature: UInt8
}

// MARK: - Image Type
enum ImageType: UInt32 {
    case application = 0x01
    case bootloader = 0x02
    case se = 0x03
    
    static func from(value: UInt32) -> ImageType? {
        return ImageType(rawValue: value)
    }
}

// MARK: - Utility Extensions
extension UInt32 {
    var littleEndianData: Data {
        return withUnsafeBytes(of: self.littleEndian) { Data($0) }
    }
}

extension UInt8 {
    var littleEndianData: Data {
        return Data([self])
    }
}

extension Data {
    
    func getUInt32(at offset: Int) -> UInt32? {
        guard offset + 4 <= count else { return nil }
        return subdata(in: offset..<offset+4).withUnsafeBytes { $0.load(as: UInt32.self).littleEndian }
    }
    
    func getUInt8(at offset: Int) -> UInt8? {
        guard offset < count else { return nil }
        return self[offset]
    }
}

// MARK: - GBL Errors
enum GblError: Error {
    case fileTooSmall(expected: Int, actual: Int)
    case invalidOffset(Int)
    case invalidTagLength(Int)
    case parsingFailed(String)
    case encodingFailed(String)
}










// MARK: - Tag Implementations

import Foundation

// MARK: - Default Tag
struct DefaultTag: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let tagData: Data
    
    func copy() -> Tag {
        return DefaultTag(
            tagHeader: tagHeader,
            tagType: tagType,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        return tagData
    }
}

// MARK: - GBL Header
struct GblHeader: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let version: UInt32
    let gblType: UInt32
    let tagData: Data
    
    init(tagHeader: TagHeader, tagType: GblType = .headerV3, version: UInt32, gblType: UInt32, tagData: Data) {
        self.tagHeader = tagHeader
        self.tagType = tagType
        self.version = version
        self.gblType = gblType
        self.tagData = tagData
    }
    
    func copy() -> Tag {
        return GblHeader(
            tagHeader: tagHeader,
            tagType: tagType,
            version: version,
            gblType: gblType,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        var data = Data()
        data.append(version.littleEndianData)
        data.append(gblType.littleEndianData)
        return data
    }
}

// MARK: - GBL Application
struct GblApplication: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let applicationData: ApplicationData
    let tagData: Data
    
    static let appLength = 13
    
    func copy() -> Tag {
        return GblApplication(
            tagHeader: tagHeader,
            tagType: tagType,
            applicationData: applicationData,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        return applicationData.content()
    }
}

// MARK: - GBL Bootloader
struct GblBootloader: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let bootloaderVersion: UInt32
    let address: UInt32
    let data: Data
    let tagData: Data
    
    func copy() -> Tag {
        return GblBootloader(
            tagHeader: tagHeader,
            tagType: tagType,
            bootloaderVersion: bootloaderVersion,
            address: address,
            data: data,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        var result = Data()
        result.append(bootloaderVersion.littleEndianData)
        result.append(address.littleEndianData)
        result.append(data)
        return result
    }
}

// MARK: - GBL Program
struct GblProg: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let flashStartAddress: UInt32
    let data: Data
    let tagData: Data
    
    func copy() -> Tag {
        return GblProg(
            tagHeader: tagHeader,
            tagType: tagType,
            flashStartAddress: flashStartAddress,
            data: data,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        var result = Data()
        result.append(flashStartAddress.littleEndianData)
        result.append(data)
        return result
    }
}

// MARK: - GBL End
struct GblEnd: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let gblCrc: UInt32
    let tagData: Data
    
    func copy() -> Tag {
        return GblEnd(
            tagHeader: tagHeader,
            tagType: tagType,
            gblCrc: gblCrc,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        return gblCrc.littleEndianData
    }
}

// MARK: - GBL Metadata
struct GblMetadata: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let metaData: Data
    let tagData: Data
    
    func copy() -> Tag {
        return GblMetadata(
            tagHeader: tagHeader,
            tagType: tagType,
            metaData: metaData,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        return metaData
    }
}

// MARK: - GBL SE Upgrade
struct GblSeUpgrade: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let blobSize: UInt32
    let version: UInt32
    let data: Data
    let tagData: Data
    
    func copy() -> Tag {
        return GblSeUpgrade(
            tagHeader: tagHeader,
            tagType: tagType,
            blobSize: blobSize,
            version: version,
            data: data,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        var result = Data()
        result.append(blobSize.littleEndianData)
        result.append(version.littleEndianData)
        result.append(data)
        return result
    }
}

// MARK: - GBL Erase Program
struct GblEraseProg: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let tagData: Data
    
    func copy() -> Tag {
        return GblEraseProg(
            tagHeader: tagHeader,
            tagType: tagType,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        return tagData
    }
}

// MARK: - GBL Program LZ4
struct GblProgLz4: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let tagData: Data
    
    func copy() -> Tag {
        return GblProgLz4(
            tagHeader: tagHeader,
            tagType: tagType,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        return tagData
    }
}

// MARK: - GBL Program LZMA
struct GblProgLzma: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let tagData: Data
    
    func copy() -> Tag {
        return GblProgLzma(
            tagHeader: tagHeader,
            tagType: tagType,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        return tagData
    }
}

// MARK: - Security Tags
struct GblCertificateEcdsaP256: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let certificate: ApplicationCertificate
    let tagData: Data
    
    func copy() -> Tag {
        return GblCertificateEcdsaP256(
            tagHeader: tagHeader,
            tagType: tagType,
            certificate: certificate,
            tagData: tagData
        )
    }
    
    func content() -> Data {
        var result = Data()
        result.append(certificate.structVersion)
        result.append(certificate.flags)
        result.append(certificate.key)
        result.append(certificate.version.littleEndianData)
        result.append(certificate.signature)
        return result
    }
}

struct GblSignatureEcdsaP256: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let r: UInt8
    let s: UInt8
    let tagData: Data
    
    func copy() -> Tag {
        return GblSignatureEcdsaP256(
            tagHeader: tagHeader,
            tagType: tagType,
            r: r,
            s: s,
            tagData: tagData
        )
    }
    
    func content() -> Data {
        return Data([r, s])
    }
}

// MARK: - Encryption Tags
struct GblEncryptionData: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let encryptedGblData: Data
    let tagData: Data
    
    func copy() -> Tag {
        return GblEncryptionData(
            tagHeader: tagHeader,
            tagType: tagType,
            encryptedGblData: encryptedGblData,
            tagData: tagData
        )
    }
    
    func content() -> Data {
        return encryptedGblData
    }
}

struct GblEncryptionInitAesCcm: Tag, TagWithHeader, Equatable {
    let tagHeader: TagHeader
    let tagType: GblType
    let msgLen: UInt32
    let nonce: UInt8
    let tagData: Data
    
    func copy() -> Tag {
        return GblEncryptionInitAesCcm(
            tagHeader: tagHeader,
            tagType: tagType,
            msgLen: msgLen,
            nonce: nonce,
            tagData: Data()
        )
    }
    
    func content() -> Data {
        var result = Data()
        result.append(msgLen.littleEndianData)
        result.append(nonce)
        return result
    }
}

// MARK: - Version Dependency
struct GblVersionDependency: Tag, Equatable {
    let tagType: GblType
    let imageType: ImageType
    let statement: UInt8
    let reversed: UInt16
    let version: UInt32
    
    func copy() -> Tag {
        return GblVersionDependency(
            tagType: tagType,
            imageType: imageType,
            statement: statement,
            reversed: reversed,
            version: version
        )
    }
    
    func content() -> Data {
        var result = Data()
        result.append(imageType.rawValue.littleEndianData)
        result.append(statement)
        result.append(withUnsafeBytes(of: reversed.littleEndian) { Data($0) })
        result.append(version.littleEndianData)
        return result
    }
}









// MARK: - Main GBL Parser and Builder

import Foundation
import CommonCrypto

class Gbl {
    static let headerSize = 8
    static let tagIdSize = 4
    static let tagLengthSize = 4
    
    // MARK: - Parsing
    func parseByteArray(_ byteArray: Data) -> ParseResult {
        var offset = 0
        let size = byteArray.count
        var rawTags: [Tag] = []
        
        guard byteArray.count >= Self.headerSize else {
            return .fatal(GblError.fileTooSmall(expected: Self.headerSize, actual: byteArray.count))
        }
        
        while offset < size {
            let result = parseTag(byteArray, offset: offset)
            
            switch result {
            case .fatal:
                break
            case .success(let header, let data):
                do {
                    let parsedTag = try parseTagType(
                        tagId: header.id,
                        length: header.length,
                        byteArray: data
                    )
                    rawTags.append(parsedTag)
                    offset += Self.tagIdSize + Self.tagLengthSize + Int(header.length)
                } catch {
                    break
                }
            }
        }
        
        return .success(rawTags)
    }
    
    // MARK: - Encoding
    func encode(_ tags: [Tag]) -> Data {
        let tagsWithoutEnd = tags.filter { !($0 is GblEnd) }
        let endTag = createEndTagWithCrc(tagsWithoutEnd)
        let finalTags = tagsWithoutEnd + [endTag]
        return encodeTags(finalTags)
    }
    
    // MARK: - Private Parsing Methods
    private func parseTag(_ byteArray: Data, offset: Int = 0) -> ParseTagResult {
        let totalHeaderSize = Self.tagIdSize + Self.tagLengthSize
        
        guard offset >= 0 && offset + totalHeaderSize <= byteArray.count else {
            return .fatal(GblError.invalidOffset(offset))
        }
        
        guard let tagId = byteArray.getUInt32(at: offset) else {
            return .fatal(GblError.parsingFailed("Failed to read tag ID"))
        }
        
        guard let tagLength = byteArray.getUInt32(at: offset + Self.tagIdSize) else {
            return .fatal(GblError.parsingFailed("Failed to read tag length"))
        }
        
        let dataStartOffset = offset + totalHeaderSize
        let dataEndOffset = dataStartOffset + Int(tagLength)
        
        guard dataEndOffset <= byteArray.count else {
            return .fatal(GblError.invalidTagLength(Int(tagLength)))
        }
        
        let tagData = byteArray.subdata(in: dataStartOffset..<dataEndOffset)
        let tagHeader = TagHeader(id: tagId, length: tagLength)
        
        return .success(tagHeader, tagData)
    }
    
    private func parseTagType(tagId: UInt32, length: UInt32, byteArray: Data) throws -> Tag {
        let tagType = GblType.from(value: tagId) ?? .tag
        let tagHeader = TagHeader(id: tagId, length: length)
        
        switch tagType {
        case .headerV3:
            guard let version = byteArray.getUInt32(at: 0),
                  let gblType = byteArray.getUInt32(at: 4) else {
                throw GblError.parsingFailed("Failed to parse header tag")
            }
            return GblHeader(
                tagHeader: tagHeader,
                tagType: tagType,
                version: version,
                gblType: gblType,
                tagData: byteArray
            )
            
        case .bootloader:
            guard let bootloaderVersion = byteArray.getUInt32(at: 0),
                  let address = byteArray.getUInt32(at: 4) else {
                throw GblError.parsingFailed("Failed to parse bootloader tag")
            }
            let data = byteArray.subdata(in: 8..<byteArray.count)
            return GblBootloader(
                tagHeader: tagHeader,
                tagType: tagType,
                bootloaderVersion: bootloaderVersion,
                address: address,
                data: data,
                tagData: byteArray
            )
            
        case .application:
            guard let type = byteArray.getUInt32(at: 0),
                  let version = byteArray.getUInt32(at: 4),
                  let capabilities = byteArray.getUInt32(at: 8),
                  let productId = byteArray.getUInt8(at: 12) else {
                throw GblError.parsingFailed("Failed to parse application tag")
            }
            let appData = ApplicationData(
                type: type,
                version: version,
                capabilities: capabilities,
                productId: productId
            )
            return GblApplication(
                tagHeader: tagHeader,
                tagType: tagType,
                applicationData: appData,
                tagData: byteArray
            )
            
        case .metadata:
            return GblMetadata(
                tagHeader: tagHeader,
                tagType: tagType,
                metaData: byteArray,
                tagData: byteArray
            )
            
        case .prog:
            guard let flashStartAddress = byteArray.getUInt32(at: 0) else {
                throw GblError.parsingFailed("Failed to parse prog tag")
            }
            let data = byteArray.subdata(in: 4..<byteArray.count)
            return GblProg(
                tagHeader: tagHeader,
                tagType: tagType,
                flashStartAddress: flashStartAddress,
                data: data,
                tagData: byteArray
            )
            
        case .progLz4:
            return GblProgLz4(
                tagHeader: tagHeader,
                tagType: tagType,
                tagData: byteArray
            )
            
        case .progLzma:
            return GblProgLzma(
                tagHeader: tagHeader,
                tagType: tagType,
                tagData: byteArray
            )
            
        case .eraseProg:
            return GblEraseProg(
                tagHeader: tagHeader,
                tagType: tagType,
                tagData: byteArray
            )
            
        case .seUpgrade:
            guard let blobSize = byteArray.getUInt32(at: 0),
                  let version = byteArray.getUInt32(at: 4) else {
                throw GblError.parsingFailed("Failed to parse SE upgrade tag")
            }
            let data = byteArray.subdata(in: 8..<byteArray.count)
            return GblSeUpgrade(
                tagHeader: tagHeader,
                tagType: tagType,
                blobSize: blobSize,
                version: version,
                data: data,
                tagData: byteArray
            )
            
        case .end:
            guard let gblCrc = byteArray.getUInt32(at: 0) else {
                throw GblError.parsingFailed("Failed to parse end tag")
            }
            return GblEnd(
                tagHeader: tagHeader,
                tagType: tagType,
                gblCrc: gblCrc,
                tagData: byteArray
            )
            
        case .encryptionData:
            let encryptedData = byteArray.subdata(in: 8..<byteArray.count)
            return GblEncryptionData(
                tagHeader: tagHeader,
                tagType: tagType,
                encryptedGblData: encryptedData,
                tagData: byteArray
            )
            
        case .encryptionInit:
            guard let msgLen = byteArray.getUInt32(at: 0),
                  let nonce = byteArray.getUInt8(at: 4) else {
                throw GblError.parsingFailed("Failed to parse encryption init tag")
            }
            return GblEncryptionInitAesCcm(
                tagHeader: tagHeader,
                tagType: tagType,
                msgLen: msgLen,
                nonce: nonce,
                tagData: byteArray
            )
            
        case .signatureEcdsaP256:
            guard let r = byteArray.getUInt8(at: 0),
                  let s = byteArray.getUInt8(at: 1) else {
                throw GblError.parsingFailed("Failed to parse signature tag")
            }
            return GblSignatureEcdsaP256(
                tagHeader: tagHeader,
                tagType: tagType,
                r: r,
                s: s,
                tagData: byteArray
            )
            
        case .certificateEcdsaP256:
            guard let structVersion = byteArray.getUInt8(at: 0),
                  let flags = byteArray.getUInt8(at: 1),
                  let key = byteArray.getUInt8(at: 2),
                  let version = byteArray.getUInt32(at: 3),
                  let signature = byteArray.getUInt8(at: 7) else {
                throw GblError.parsingFailed("Failed to parse certificate tag")
            }
            let certificate = ApplicationCertificate(
                structVersion: structVersion,
                flags: flags,
                key: key,
                version: version,
                signature: signature
            )
            return GblCertificateEcdsaP256(
                tagHeader: tagHeader,
                tagType: tagType,
                certificate: certificate,
                tagData: byteArray
            )
            
        default:
            return DefaultTag(
                tagHeader: tagHeader,
                tagType: .tag,
                tagData: byteArray
            )
        }
    }
    
    // MARK: - Encoding Methods
    func encodeTags(_ tags: [Tag]) -> Data {
        var result = Data()
        
        for tag in tags {
            guard let tagWithHeader = tag as? TagWithHeader else { continue }
            
            // Tag ID (4 bytes)
            result.append(tagWithHeader.tagHeader.id.littleEndianData)
            
            // Tag Length (4 bytes)
            result.append(tagWithHeader.tagHeader.length.littleEndianData)
            
            // Tag Data
            let tagData = generateTagData(tag)
            result.append(tagData)
        }
        
        return result
    }
    
    private func generateTagData(_ tag: Tag) -> Data {
        return tag.content()
    }
    
    func createEndTagWithCrc(_ tags: [Tag]) -> GblEnd {
        var totalData = Data()

        for tag in tags {
            guard let tagWithHeader = tag as? TagWithHeader else { continue }

            totalData.append(tagWithHeader.tagHeader.id.littleEndianData)
            totalData.append(tagWithHeader.tagHeader.length.littleEndianData)
            totalData.append(generateTagData(tag))
        }

        let endTagId = GblType.end.rawValue
        let endTagLength = UInt32(Gbl.tagLengthSize)

        totalData.append(endTagId.littleEndianData)
        totalData.append(endTagLength.littleEndianData)

        var hash = [UInt8](repeating: 0, count: Int(CC_SHA256_DIGEST_LENGTH))
        totalData.withUnsafeBytes {
            _ = CC_SHA256($0.baseAddress, CC_LONG(totalData.count), &hash)
        }

        let crcValue = hash.prefix(4).withUnsafeBytes { $0.load(as: UInt32.self) }

        return GblEnd(
            tagHeader: TagHeader(id: endTagId, length: endTagLength),
            tagType: .end,
            gblCrc: crcValue,
            tagData: crcValue.littleEndianData
        )
    }
}

// MARK: - GBL Builder Extension
extension Gbl {
    class GblBuilder {
        private let container = TagContainer()
        
        static func create() -> GblBuilder {
            let builder = GblBuilder()
            let _ = builder.container.create()
            return builder
        }
        
        static func empty() -> GblBuilder {
            return GblBuilder()
        }
        
        // MARK: - Builder Methods
        func encryptionData(_ encryptedGblData: Data) -> GblBuilder {
            let tag = GblEncryptionData(
                tagHeader: TagHeader(
                    id: GblType.encryptionData.rawValue,
                    length: UInt32(encryptedGblData.count)
                ),
                tagType: .encryptionData,
                encryptedGblData: encryptedGblData,
                tagData: encryptedGblData
            )
            let _ = container.add(tag)
            return self
        }
        
        func encryptionInit(msgLen: UInt32, nonce: UInt8) -> GblBuilder {
            let tag = GblEncryptionInitAesCcm(
                tagHeader: TagHeader(
                    id: GblType.encryptionInit.rawValue,
                    length: 5
                ),
                tagType: .encryptionInit,
                msgLen: msgLen,
                nonce: nonce,
                tagData: generateEncryptionInitTagData(msgLen: msgLen, nonce: nonce)
            )
            let _ = container.add(tag)
            return self
        }
        
        func signatureEcdsaP256(r: UInt8, s: UInt8) -> GblBuilder {
            let tag = GblSignatureEcdsaP256(
                tagHeader: TagHeader(
                    id: GblType.signatureEcdsaP256.rawValue,
                    length: 2
                ),
                tagType: .signatureEcdsaP256,
                r: r,
                s: s,
                tagData: generateSignatureEcdsaP256TagData(r: r, s: s)
            )
            let _ = container.add(tag)
            return self
        }
        
        func certificateEcdsaP256(_ certificate: ApplicationCertificate) -> GblBuilder {
            let tag = GblCertificateEcdsaP256(
                tagHeader: TagHeader(
                    id: GblType.certificateEcdsaP256.rawValue,
                    length: 8
                ),
                tagType: .certificateEcdsaP256,
                certificate: certificate,
                tagData: generateCertificateEcdsaP256TagData(certificate)
            )
            let _ = container.add(tag)
            return self
        }
        
        func versionDependency(_ dependencyData: Data) -> GblBuilder {
            let tag = DefaultTag(
                tagHeader: TagHeader(
                    id: GblType.versionDependency.rawValue,
                    length: UInt32(dependencyData.count)
                ),
                tagType: .versionDependency,
                tagData: dependencyData
            )
            let _ = container.add(tag)
            return self
        }
        
        func bootloader(
            bootloaderVersion: UInt32,
            address: UInt32,
            data: Data
        ) -> GblBuilder {
            let tag = GblBootloader(
                tagHeader: TagHeader(
                    id: GblType.bootloader.rawValue,
                    length: UInt32(8 + data.count)
                ),
                tagType: .bootloader,
                bootloaderVersion: bootloaderVersion,
                address: address,
                data: data,
                tagData: generateBootloaderTagData(
                    bootloaderVersion: bootloaderVersion,
                    address: address,
                    data: data
                )
            )
            let _ = container.add(tag)
            return self
        }
        
        func metadata(_ metaData: Data) -> GblBuilder {
            let tag = GblMetadata(
                tagHeader: TagHeader(
                    id: GblType.metadata.rawValue,
                    length: UInt32(metaData.count)
                ),
                tagType: .metadata,
                metaData: metaData,
                tagData: metaData
            )
            let _ = container.add(tag)
            return self
        }
        
        func prog(flashStartAddress: UInt32, data: Data) -> GblBuilder {
            let tag = GblProg(
                tagHeader: TagHeader(
                    id: GblType.prog.rawValue,
                    length: UInt32(4 + data.count)
                ),
                tagType: .prog,
                flashStartAddress: flashStartAddress,
                data: data,
                tagData: generateProgTagData(flashStartAddress: flashStartAddress, data: data)
            )
            let _ = container.add(tag)
            return self
        }
        
        func progLz4(
            flashStartAddress: UInt32,
            compressedData: Data,
            decompressedSize: UInt32
        ) -> GblBuilder {
            let tag = GblProgLz4(
                tagHeader: TagHeader(
                    id: GblType.progLz4.rawValue,
                    length: UInt32(8 + compressedData.count)
                ),
                tagType: .progLz4,
                tagData: generateProgLz4TagData(
                    flashStartAddress: flashStartAddress,
                    compressedData: compressedData,
                    decompressedSize: decompressedSize
                )
            )
            let _ = container.add(tag)
            return self
        }
        
        func progLzma(
            flashStartAddress: UInt32,
            compressedData: Data,
            decompressedSize: UInt32
        ) -> GblBuilder {
            let tag = GblProgLzma(
                tagHeader: TagHeader(
                    id: GblType.progLzma.rawValue,
                    length: UInt32(8 + compressedData.count)
                ),
                tagType: .progLzma,
                tagData: generateProgLzmaTagData(
                    flashStartAddress: flashStartAddress,
                    compressedData: compressedData,
                    decompressedSize: decompressedSize
                )
            )
            let _ = container.add(tag)
            return self
        }
        
        func seUpgrade(version: UInt32, data: Data) -> GblBuilder {
            let blobSize = UInt32(data.count)
            let tag = GblSeUpgrade(
                tagHeader: TagHeader(
                    id: GblType.seUpgrade.rawValue,
                    length: UInt32(8 + data.count)
                ),
                tagType: .seUpgrade,
                blobSize: blobSize,
                version: version,
                data: data,
                tagData: generateSeUpgradeTagData(blobSize: blobSize, version: version, data: data)
            )
            let _ = container.add(tag)
            return self
        }
        
        func application(
            type: UInt32 = ApplicationData.appType,
            version: UInt32 = ApplicationData.appVersion,
            capabilities: UInt32 = ApplicationData.appCapabilities,
            productId: UInt8 = ApplicationData.appProductId,
            additionalData: Data = Data()
        ) -> GblBuilder {
            let applicationData = ApplicationData(
                type: type,
                version: version,
                capabilities: capabilities,
                productId: productId
            )
            let tagData = applicationData.content() + additionalData
            
            let tag = GblApplication(
                tagHeader: TagHeader(
                    id: GblType.application.rawValue,
                    length: UInt32(tagData.count)
                ),
                tagType: .application,
                applicationData: applicationData,
                tagData: tagData
            )
            let _ = container.add(tag)
            return self
        }
        
        func eraseProg() -> GblBuilder {
            let tag = GblEraseProg(
                tagHeader: TagHeader(
                    id: GblType.eraseProg.rawValue,
                    length: 8
                ),
                tagType: .eraseProg,
                tagData: Data(count: 8)
            )
            let _ = container.add(tag)
            return self
        }
        
        // MARK: - Build Methods
        func get() -> [Tag] {
            switch container.build() {
            case .success(let tags):
                return tags
            case .error:
                return []
            }
        }
        
        func buildToList() -> [Tag] {
            let tags = getOrDefault(container.build(), default: [])
            let tagsWithoutEnd = tags.filter { !($0 is GblEnd) }
            let gbl = Gbl()
            let endTag = gbl.createEndTagWithCrc(tagsWithoutEnd)
            return tagsWithoutEnd + [endTag]
        }
        
        func buildToByteArray() -> Data {
            let tags = buildToList()
            let gbl = Gbl()
            return gbl.encodeTags(tags)
        }
        
        func hasTag(_ tagType: GblType) -> Bool {
            return container.hasTag(tagType)
        }
        
        func getTag(_ tagType: GblType) -> Tag? {
            return container.getTag(tagType)
        }
        
        func removeTag(_ tag: Tag) -> ContainerResult<Void> {
            return container.remove(tag)
        }
        
        func clear() -> ContainerResult<Void> {
            return container.clear()
        }
        
        func size() -> Int {
            return container.size()
        }
        
        func isEmpty() -> Bool {
            return container.isEmpty()
        }
        
        func getTagTypes() -> Set<GblType> {
            return container.getTagTypes()
        }
        
        // MARK: - Private Helper Methods
        private func generateEncryptionInitTagData(msgLen: UInt32, nonce: UInt8) -> Data {
            var result = Data()
            result.append(msgLen.littleEndianData)
            result.append(nonce)
            return result
        }
        
        private func generateSignatureEcdsaP256TagData(r: UInt8, s: UInt8) -> Data {
            return Data([r, s])
        }
        
        private func generateCertificateEcdsaP256TagData(_ certificate: ApplicationCertificate) -> Data {
            var result = Data()
            result.append(certificate.structVersion)
            result.append(certificate.flags)
            result.append(certificate.key)
            result.append(certificate.version.littleEndianData)
            result.append(certificate.signature)
            return result
        }
        
        private func generateBootloaderTagData(
            bootloaderVersion: UInt32,
            address: UInt32,
            data: Data
        ) -> Data {
            var result = Data()
            result.append(bootloaderVersion.littleEndianData)
            result.append(address.littleEndianData)
            result.append(data)
            return result
        }
        
        private func generateProgTagData(flashStartAddress: UInt32, data: Data) -> Data {
            var result = Data()
            result.append(flashStartAddress.littleEndianData)
            result.append(data)
            return result
        }
        
        private func generateProgLz4TagData(
            flashStartAddress: UInt32,
            compressedData: Data,
            decompressedSize: UInt32
        ) -> Data {
            var result = Data()
            result.append(flashStartAddress.littleEndianData)
            result.append(decompressedSize.littleEndianData)
            result.append(compressedData)
            return result
        }
        
        private func generateProgLzmaTagData(
            flashStartAddress: UInt32,
            compressedData: Data,
            decompressedSize: UInt32
        ) -> Data {
            var result = Data()
            result.append(flashStartAddress.littleEndianData)
            result.append(decompressedSize.littleEndianData)
            result.append(compressedData)
            return result
        }
        
        private func generateSeUpgradeTagData(
            blobSize: UInt32,
            version: UInt32,
            data: Data
        ) -> Data {
            var result = Data()
            result.append(blobSize.littleEndianData)
            result.append(version.littleEndianData)
            result.append(data)
            return result
        }
        
        private func getOrDefault<T>(_ result: ContainerResult<T>, default: T) -> T {
            switch result {
            case .success(let data):
                return data
            case .error:
                return `default`
            }
        }
    }
}











// MARK: - Container System

import Foundation

// MARK: - Container Protocol
protocol Container {
    func create() -> ContainerResult<Void>
    func add(_ tag: Tag) -> ContainerResult<Void>
    func remove(_ tag: Tag) -> ContainerResult<Void>
    func build() -> ContainerResult<[Tag]>
    func content() -> ContainerResult<Data>
}

// MARK: - Tag Container Implementation
class TagContainer: Container {
    private var tags: Set<TagWrapper> = Set()
    private var isCreated: Bool = false
    
    private static let gblTagIdHeaderV3: UInt32 = 0x03A617EB
    private static let headerSize = 8
    private static let headerVersion: UInt32 = 50331648
    private static let headerGblType: UInt32 = 0
    
    private static let protectedTagTypes: Set<GblType> = [.headerV3, .end]
    
    // MARK: - Container Methods
    func create() -> ContainerResult<Void> {
        if isCreated {
            return .success(())
        }

        tags.removeAll()

        let headerTag = createHeaderTag()
        tags.insert(TagWrapper(headerTag))

        let endTag = createEndTag()
        tags.insert(TagWrapper(endTag))

        isCreated = true
        return .success(())
    }
    
    func add(_ tag: Tag) -> ContainerResult<Void> {
        guard isCreated else {
            return .error(
                "Container must be created before adding tags. Call create() first.",
                .containerNotCreated
            )
        }
        
        if isProtectedTag(tag) {
            return .error(
                "Cannot add protected tag: \(tag.tagType). Protected tags are managed automatically.",
                .protectedTagViolation
            )
        }
        
        tags.insert(TagWrapper(tag))
        return .success(())
    }
    
    func remove(_ tag: Tag) -> ContainerResult<Void> {
        guard isCreated else {
            return .error(
                "Container must be created before removing tags. Call create() first.",
                .containerNotCreated
            )
        }
        
        if isProtectedTag(tag) {
            return .error(
                "Cannot remove protected tag: \(tag.tagType). Protected tags are managed automatically.",
                .protectedTagViolation
            )
        }
        
        let wrapper = TagWrapper(tag)
        let removed = tags.remove(wrapper)
        
        guard removed != nil else {
            return .error(
                "Tag not found in container: \(tag.tagType)",
                .tagNotFound
            )
        }
        
        return .success(())
    }
    
    func build() -> ContainerResult<[Tag]> {
        guard isCreated else {
            return .error(
                "Container must be created before building. Call create() first.",
                .containerNotCreated
            )
        }
        
        var sortedTags: [Tag] = []
        
        // Add header tag first
        if let headerTag = tags.first(where: { $0.tag.tagType == .headerV3 })?.tag {
            sortedTags.append(headerTag)
        }
        
        // Add non-protected tags sorted by tag type value
        let nonProtectedTags = tags
            .filter { !Self.protectedTagTypes.contains($0.tag.tagType) }
            .map { $0.tag }
            .sorted { $0.tagType.rawValue < $1.tagType.rawValue }
        
        sortedTags.append(contentsOf: nonProtectedTags)
        
        // Add end tag last
        if let endTag = tags.first(where: { $0.tag.tagType == .end })?.tag {
            sortedTags.append(endTag)
        }
        
        return .success(sortedTags)
    }
    
    func content() -> ContainerResult<Data> {
        guard isCreated else {
            return .error(
                "Container must be created before exporting content. Call create() first.",
                .containerNotCreated
            )
        }
        
        switch build() {
        case .success(let tags):
            let tagsWithoutEnd = tags.filter { !($0 is GblEnd) }
            let gbl = Gbl()
            let endTag = gbl.createEndTagWithCrc(tagsWithoutEnd)
            let finalTags = tagsWithoutEnd + [endTag]
            
            let byteArray = gbl.encodeTags(finalTags)
            return .success(byteArray)
            
        case .error(let message, let code):
            return .error(
                "Failed to build tags for content export: \(message)",
                code
            )
        }
    }
    
    // MARK: - Query Methods
    func hasTag(_ tagType: GblType) -> Bool {
        guard isCreated else { return false }
        return tags.contains { $0.tag.tagType == tagType }
    }
    
    func getTag(_ tagType: GblType) -> Tag? {
        guard isCreated else { return nil }
        return tags.first { $0.tag.tagType == tagType }?.tag
    }
    
    func getAllTags(_ tagType: GblType) -> [Tag] {
        guard isCreated else { return [] }
        return tags.compactMap { $0.tag.tagType == tagType ? $0.tag : nil }
    }
    
    func isEmpty() -> Bool {
        guard isCreated else { return true }
        return tags.count == Self.protectedTagTypes.count
    }
    
    func size() -> Int {
        guard isCreated else { return 0 }
        return tags.count
    }
    
    func getTagTypes() -> Set<GblType> {
        guard isCreated else { return Set() }
        return Set(tags.map { $0.tag.tagType })
    }
    
    func isContainerCreated() -> Bool {
        return isCreated
    }
    
    func clear() -> ContainerResult<Void> {
        guard isCreated else {
            return .error(
                "Container must be created before clearing. Call create() first.",
                .containerNotCreated
            )
        }

        tags = tags.filter { isProtectedTag($0.tag) }
        return .success(())
    }
    
    // MARK: - Private Methods
    private func isProtectedTag(_ tag: Tag) -> Bool {
        return Self.protectedTagTypes.contains(tag.tagType)
    }
    
    private func createHeaderTag() -> Tag {
        let header = GblHeader(
            tagHeader: TagHeader(
                id: Self.gblTagIdHeaderV3,
                length: UInt32(Self.headerSize)
            ),
            tagType: .headerV3,
            version: Self.headerVersion,
            gblType: Self.headerGblType,
            tagData: Data()
        )
        
        let headerWithData = GblHeader(
            tagHeader: header.tagHeader,
            tagType: header.tagType,
            version: header.version,
            gblType: header.gblType,
            tagData: header.content()
        )
        
        return headerWithData
    }
    
    private func createEndTag() -> Tag {
        return GblEnd(
            tagHeader: TagHeader(
                id: GblType.end.rawValue,
                length: 0
            ),
            tagType: .end,
            gblCrc: 0,
            tagData: Data()
        )
    }
}

// MARK: - Tag Wrapper for Set Storage
private struct TagWrapper: Hashable {
    let tag: Tag
    
    init(_ tag: Tag) {
        self.tag = tag
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(tag.tagType.rawValue)
        if let tagWithHeader = tag as? TagWithHeader {
            hasher.combine(tagWithHeader.tagHeader.id)
            hasher.combine(tagWithHeader.tagHeader.length)
        }
    }
    
    static func == (lhs: TagWrapper, rhs: TagWrapper) -> Bool {
        guard lhs.tag.tagType == rhs.tag.tagType else { return false }
        
        if let lhsTagWithHeader = lhs.tag as? TagWithHeader,
           let rhsTagWithHeader = rhs.tag as? TagWithHeader {
            return lhsTagWithHeader.tagHeader.id == rhsTagWithHeader.tagHeader.id &&
                   lhsTagWithHeader.tagHeader.length == rhsTagWithHeader.tagHeader.length
        }
        
        return true
    }
}
