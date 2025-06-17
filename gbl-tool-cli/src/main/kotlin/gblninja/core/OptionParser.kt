package gblninja.core

internal object OptionParser {
    fun parseOptions(args: List<String>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        var i = 0

        while (i < args.size) {
            when (args[i]) {
                "-f", "--file" -> {
                    result["file"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "-F", "--format", "-fmt" -> {
                    result["format"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "-t", "--type" -> {
                    result["type"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "-s", "--size" -> {
                    result["size"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "-d", "--data" -> {
                    result["data"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "-o", "--output" -> {
                    result["output"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "-v", "--version" -> {
                    result["version"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "-m", "--metadata" -> {
                    result["metadata"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "-a", "--address" -> {
                    result["address"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "-n", "--nonce" -> {
                    result["nonce"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "-r", "--r-value" -> {
                    result["r-value"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "--s-value" -> {
                    result["s-value"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "--dependency" -> {
                    result["dependency"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "--index" -> {
                    result["index"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "--product-id" -> {
                    result["product-id"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "--app-type" -> {
                    result["app-type"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "--app-version" -> {
                    result["app-version"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "--capabilities" -> {
                    result["capabilities"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "--msg-len" -> {
                    result["msg-len"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "--decompressed-size" -> {
                    result["decompressed-size"] = getNextArg(args, i) ?: ""
                    i += 2
                }
                "--pack" -> {
                    result["pack"] = "true"
                    i++
                }
                "--add" -> {
                    result["add"] = "true"
                    i++
                }
                "--remove" -> {
                    result["remove"] = "true"
                    i++
                }
                "--set" -> {
                    result["set"] = "true"
                    i++
                }
                "--create" -> {
                    result["create"] = "true"
                    i++
                }
                else -> i++
            }
        }
        return result
    }

    private fun getNextArg(args: List<String>, currentIndex: Int): String? {
        return if (currentIndex + 1 < args.size && !args[currentIndex + 1].startsWith("-")) {
            args[currentIndex + 1]
        } else null
    }
}