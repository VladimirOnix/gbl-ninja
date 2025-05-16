import gbl.GblParser

fun main() {
    val gbl = GblParser.Builder
        .createEmpty()
        .addApplication()
        .addMetadata("Hello bear".toByteArray())
        .buildToList()

    for (tag in gbl) {
        println(tag)
    }
}