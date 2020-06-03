#!/usr/bin/env kscript
@file:Include("ShellAccess.kt")

import java.io.File

/**
 * Returns the directory that java application was started from.
 * https://stackoverflow.com/questions/16239130/java-user-dir-property-what-exactly-does-it-mean
 */
fun executionDirectory(): String =
    System.getProperty("user.dir").ifBlank { null } ?: error("No execution directory")

fun Sequence<File>.exludePaths(vararg strs: String): Sequence<File> {
    return filter { file ->
        val path = file.canonicalPath
        strs.none { path.contains(it) }
    }
}

/**
 * Replaces the contents of this file with the given lines.
 * Overwrites the old file and returns a new File instance with the new content.
 */
fun File.replaceContents(lines: List<String>): File {
    val tempFile = createTempFile()
    tempFile.printWriter().use { writer ->
        lines.forEach { writer.println(it) }
    }
    check(this.delete() && tempFile.renameTo(this)) { "failed to replace file" }
    return tempFile
}

/** Used from [File.edit]. */
class FileEditor(private val file: File) {
    private var lines: MutableList<String> = file.readLines().toMutableList()
    private val lineCount: Int get() = lines.size
    private var currentLineIndex = 0

    fun insertPrevious(line: String) {
        lines.add(currentLineIndex, line)
        currentLineIndex++
    }

    fun insertNext(line: String) {
        lines.add(currentLineIndex + 1, line)
    }

    fun edit(lineTransform: FileEditor.(lineNumber: Int, line: String) -> String?): File {
        currentLineIndex = 0

        while (currentLineIndex < lineCount) {
            val line = lines.getOrNull(currentLineIndex) ?: error("No line at $currentLineIndex")
            val newLine = lineTransform(currentLineIndex, line)
            if (newLine == null) {
                lines.removeAt(currentLineIndex)
            } else {
                lines[currentLineIndex] = newLine
                currentLineIndex++
            }
        }

        return file.replaceContents(lines)
    }
}

/**
 * Edit the contents of a File with a [FileEditor]. The new contents will override the old.
 *
 * @param lineTransform Called with each line of the file in order.
 * Return the new value you would like written in the file, or null to delete the line.
 * You can also call [FileEditor.insertNext] to insert a line immediately above the given line, or
 * [FileEditor.insertPrevious] to insert a line immediately after the given line.
 */
fun File.edit(lineTransform: FileEditor.(lineNumber: Int, line: String) -> String) {
    // Note, the FileEditor class had to be declared above this in the file otherwise the script
    // crashed at runtime, unable to find the FileEditor constructor :(
    FileEditor(this).edit(lineTransform)
}
