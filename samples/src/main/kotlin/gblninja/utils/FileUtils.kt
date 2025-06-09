package gblninja.utils

import java.io.File

internal object FileUtils {
    fun readFileData(filename: String?): ByteArray? {
        if (filename == null) return null

        val file = File(filename)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $filename")
        }

        return file.readBytes()
    }

    fun validateFileExists(filename: String?): Boolean {
        return filename != null && File(filename).exists()
    }

    fun getFileSize(filename: String): Long {
        return File(filename).length()
    }
}