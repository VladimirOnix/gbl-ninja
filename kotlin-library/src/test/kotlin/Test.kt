import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import results.ParseResult
import tag.GblType
import tag.type.GblHeader
import tag.type.GblProg
import java.io.File
import java.io.FileOutputStream

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class GblValidationTest {
//
//    private val validGblPath = "src/test/samples/src/test/resources/valid_file.gbl"
//    private val invalidGblPath = "src/test/samples/src/test/resources/invalid_file.gbl"
//
//    @BeforeAll
//    fun setUp() {
//        val testResourcesDir = File("src/test/samples/src/test/resources")
//        if (!testResourcesDir.exists()) {
//            testResourcesDir.mkdirs()
//        }
//        createValidAndInvalidGbls()
//    }
//
//    @Test
//    @DisplayName("Valid GBL should parse successfully")
//    fun testValidGblParsingSuccess() {
//        val validGblData = File(validGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(validGblData)
//
//        assertTrue(parseResult is ParseResult.Success)
//    }
//
//    @Test
//    @DisplayName("Valid GBL should have non-empty result list")
//    fun testValidGblResultList() {
//        val validGblData = File(validGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(validGblData)
//
//        assertTrue(parseResult is ParseResult.Success)
//        val tags = (parseResult as ParseResult.Success).resultList
//
//        assertNotNull(tags)
//    }
//
//    @Test
//    @DisplayName("Valid GBL result list should not be empty")
//    fun testValidGblResultListNotEmpty() {
//        val validGblData = File(validGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(validGblData)
//
//        assertTrue(parseResult is ParseResult.Success)
//        val tags = (parseResult as ParseResult.Success).resultList
//
//        assertFalse(tags.isEmpty())
//    }
//
//    @Test
//    @DisplayName("Valid GBL should contain exactly four tags")
//    fun testValidGblTagCount() {
//        val validGblData = File(validGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(validGblData)
//
//        assertTrue(parseResult is ParseResult.Success)
//        val tags = (parseResult as ParseResult.Success).resultList
//
//        assertEquals(4, tags.size)
//    }
//
//    @Test
//    @DisplayName("Valid GBL should contain a header tag")
//    fun testValidGblContainsHeaderTag() {
//        val validGblData = File(validGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(validGblData)
//
//        assertTrue(parseResult is ParseResult.Success)
//        val tags = (parseResult as ParseResult.Success).resultList
//
//        assertTrue(tags.any { it is GblHeader })
//    }
//
//    @Test
//    @DisplayName("Valid GBL should contain an application tag")
//    fun testValidGblContainsApplicationTag() {
//        val validGblData = File(validGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(validGblData)
//
//        assertTrue(parseResult is ParseResult.Success)
//        val tags = (parseResult as ParseResult.Success).resultList
//
//        assertTrue(tags.any { it.tagType == GblType.APPLICATION })
//    }
//
//    @Test
//    @DisplayName("Valid GBL should contain a program tag")
//    fun testValidGblContainsProgramTag() {
//        val validGblData = File(validGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(validGblData)
//
//        assertTrue(parseResult is ParseResult.Success)
//        val tags = (parseResult as ParseResult.Success).resultList
//
//        assertTrue(tags.any { it is GblProg })
//    }
//
//    @Test
//    @DisplayName("Valid GBL header should have non-zero version")
//    fun testValidGblHeaderVersion() {
//        val validGblData = File(validGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(validGblData)
//
//        assertTrue(parseResult is ParseResult.Success)
//        val tags = (parseResult as ParseResult.Success).resultList
//        val headerTag = tags.first { it is GblHeader } as GblHeader
//
//        assertNotEquals(0U, headerTag.version)
//    }
//
//    @Test
//    @DisplayName("Invalid GBL should be either Fatal or Success with issues")
//    fun testInvalidGblResultType() {
//        val invalidGblData = File(invalidGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(invalidGblData)
//
//        assertTrue(parseResult is ParseResult.Fatal || parseResult is ParseResult.Success)
//    }
//
//    @Test
//    @DisplayName("Invalid GBL with Fatal result should have non-null error")
//    fun testInvalidGblFatalErrorNonNull() {
//        val invalidGblData = File(invalidGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(invalidGblData)
//
//        if (parseResult is ParseResult.Fatal) {
//            val error = parseResult.error
//            assertNotNull(error)
//        }
//    }
//
//    @Test
//    @DisplayName("Invalid GBL with Fatal result should have non-empty error")
//    fun testInvalidGblFatalErrorNonEmpty() {
//        val invalidGblData = File(invalidGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(invalidGblData)
//
//        if (parseResult is ParseResult.Fatal) {
//            val error = parseResult.error
//            assertFalse(error == null)
//        }
//    }
//
//    @Test
//    @DisplayName("Invalid GBL with Success result should have inconsistent header version")
//    fun testInvalidGblSuccessWithInconsistentHeaderVersion() {
//        val invalidGblData = File(invalidGblPath).readBytes()
//        val parser = GblParser()
//        val parseResult = parser.parseFile(invalidGblData)
//
//        if (parseResult is ParseResult.Success) {
//            val tags = parseResult.resultList
//
//            if (tags.any { it is GblHeader }) {
//                val headerTag = tags.first { it is GblHeader } as GblHeader
//                if (headerTag.version != 0x01000000U) {
//                    assertTrue(true)
//                } else {
//                    assertNotEquals(0U, headerTag.gblType)
//                }
//            }
//        }
//    }
//
//    @Test
//    @DisplayName("Should create non-null GBL file")
//    fun testGblFileCreationNonNull() {
//        val gblBuilder = GblParser.Builder.empty()
//            .application(
//                type = 2U,
//                version = 0x01000000U,
//                capabilities = 0x00000001U
//            )
//            .prog(
//                flashStartAddress = 0x08000000U,
//                data = "PROGRAM_DATA".toByteArray()
//            )
//
//        val encodedGbl = gblBuilder.buildToByteArray()
//        assertNotNull(encodedGbl)
//    }
//
//    @Test
//    @DisplayName("Should create non-empty GBL file")
//    fun testGblFileCreationNonEmpty() {
//        val gblBuilder = GblParser.Builder.empty()
//            .application(
//                type = 2U,
//                version = 0x01000000U,
//                capabilities = 0x00000001U
//            )
//            .prog(
//                flashStartAddress = 0x08000000U,
//                data = "PROGRAM_DATA".toByteArray()
//            )
//
//        val encodedGbl = gblBuilder.buildToByteArray()
//        assertTrue(encodedGbl.isNotEmpty())
//    }
//
//    @Test
//    @DisplayName("Created GBL should be parseable")
//    fun testCreatedGblIsParseable() {
//        val gblBuilder = GblParser.Builder.empty()
//            .application(
//                type = 2U,
//                version = 0x01000000U,
//                capabilities = 0x00000001U
//            )
//            .prog(
//                flashStartAddress = 0x08000000U,
//                data = "PROGRAM_DATA".toByteArray()
//            )
//
//        val encodedGbl = gblBuilder.buildToByteArray()
//        val parser = GblParser()
//        val parsed = parser.parseFile(encodedGbl)
//
//        assertTrue(parsed is ParseResult.Success)
//    }
//
//    @Test
//    @DisplayName("Created GBL should contain non-empty tags list")
//    fun testCreatedGblContainsNonEmptyTags() {
//        val gblBuilder = GblParser.Builder.empty()
//            .application(
//                type = 2U,
//                version = 0x01000000U,
//                capabilities = 0x00000001U
//            )
//            .prog(
//                flashStartAddress = 0x08000000U,
//                data = "PROGRAM_DATA".toByteArray()
//            )
//
//        val encodedGbl = gblBuilder.buildToByteArray()
//        val parser = GblParser()
//        val parsed = parser.parseFile(encodedGbl)
//
//        assertTrue(parsed is ParseResult.Success)
//        val tags = (parsed as ParseResult.Success).resultList
//        assertFalse(tags.isEmpty())
//    }
//
//    private fun createValidAndInvalidGbls() {
//        println("\n===== CREATING VALID AND INVALID GBL FILES FOR TESTING =====")
//
//        val gblBuilder = GblParser.Builder.empty()
//            .application(
//                type = 2U,
//                version = 0x01000000U,
//                capabilities = 0x00000001U
//            )
//            .prog(
//                flashStartAddress = 0x08000000U,
//                data = "PROGRAM_DATA".toByteArray()
//            )
//
//        val a = gblBuilder.buildToByteArray()
//
//        val gblData = gblBuilder.buildToList()
//        val encodedGbl = GblParser().encode(gblData)
//
//        val parsed = GblParser().parseFile(a)
//        if (parsed is ParseResult.Success) {
//            val tags = parsed.resultList
//            println("Successfully parsed GBL file with ${tags.size} tags:")
//            tags.forEach {
//                println(it)
//            }
//        }
//
//        saveToFile(encodedGbl, validGblPath)
//        println("Valid GBL file created: $validGblPath")
//        println("Size: ${encodedGbl.size} bytes")
//
//        val invalidGbl = encodedGbl.copyOf()
//
//        if (invalidGbl.size > 12) {
//            invalidGbl[0] = 0xFF.toByte()
//            invalidGbl[4] = 0x00.toByte()
//            invalidGbl[8] = 0xAA.toByte()
//        }
//
//        if (invalidGbl.size > 50) {
//            invalidGbl[40] = 0xFF.toByte()
//            invalidGbl[41] = 0xFF.toByte()
//            invalidGbl[42] = 0xFF.toByte()
//        }
//
//        saveToFile(invalidGbl, invalidGblPath)
//        println("Invalid GBL file created: $invalidGblPath")
//        println("Size: ${invalidGbl.size} bytes")
//    }
//
//    private fun saveToFile(data: ByteArray, filename: String) {
//        try {
//            val directory = File(filename).parentFile
//            if (directory != null && !directory.exists()) {
//                directory.mkdirs()
//            }
//
//            FileOutputStream(filename).use { fos ->
//                fos.write(data)
//            }
//        } catch (e: Exception) {
//            println("Error saving file: ${e.message}")
//            fail("Failed to save test file: ${e.message}")
//        }
//    }
//}