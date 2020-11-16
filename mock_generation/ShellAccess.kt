#!/usr/bin/env kscript
/**
 * Functions for executing shell commands and receiving the result.
 */

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.lang.ProcessBuilder.Redirect
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

/**
 * Executes the receiving string and returns the stdOut as a single line.
 * If the result is more than one line an exception is thrown.
 * @throws IOException if an I/O error occurs
 */
fun String.executeForSingleLine(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS,
    suppressStderr: Boolean = false,
    redactedTokens: List<String> = emptyList(),
    throwOnFailure: Boolean = false
): String {
    val lines = executeForLines(
        workingDir,
        timeoutAmount,
        timeoutUnit,
        suppressStderr = suppressStderr,
        redactedTokens = redactedTokens,
        throwOnFailure = throwOnFailure
    )
        .filter { it.isNotBlank() }
    return lines.singleOrNull() ?: throw IllegalStateException("Expected single line but got: $lines")
}

/**
 * Executes the receiving string and returns the stdOut as a list of all lines outputted.
 * @throws IOException if an I/O error occurs
 * @param clearQuotes If true, double quotes will be removed if they wrap the line
 */
fun String.executeForLines(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS,
    clearQuotes: Boolean = false,
    suppressStderr: Boolean = false,
    redactedTokens: List<String> = emptyList(),
    throwOnFailure: Boolean = false
): List<String> {
    return executeForBufferedReader(
        workingDir,
        timeoutAmount,
        timeoutUnit,
        suppressStderr = suppressStderr,
        redactedTokens = redactedTokens,
        throwOnFailure = throwOnFailure
    )
        .readLines()
        .map { if (clearQuotes) it.removePrefix("\"").removeSuffix("\"") else it }
}

/**
 * Executes the receiving string and returns the stdOut as a BufferedReader.
 * @throws IOException if an I/O error occurs
 */
fun String.executeForBufferedReader(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS,
    suppressStderr: Boolean = false,
    redactedTokens: List<String> = emptyList(),
    throwOnFailure: Boolean = false
): BufferedReader {
    val stderrRedirectBehavior = if (suppressStderr) Redirect.PIPE else Redirect.INHERIT
    return execute(
        workingDir,
        timeoutAmount,
        timeoutUnit,
        stderrRedirectBehavior = stderrRedirectBehavior,
        redactedTokens = redactedTokens,
        throwOnFailure = throwOnFailure
    ).stdOut
}

/**
 * Executes the receiving string and returns the result,
 * including exit code, stdOut, and stdErr streams.
 * Some commands do not work if the command is redirected to PIPE.
 * Use ProcessBuilder.Redirect.INHERIT in those cases.
 *
 * @throws IOException if an I/O error occurs
 */
fun String.execute(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS,
    timeoutMessage: String? = null,
    stdoutRedirectBehavior: Redirect = Redirect.PIPE,
    stderrRedirectBehavior: Redirect = Redirect.PIPE,
    redactedTokens: List<String> = emptyList(),
    throwOnFailure: Boolean = false
): ProcessResult {
    var commandToLog = this
    redactedTokens.forEach { commandToLog = commandToLog.replace(it, "<redacted>") }
    // The command is disabled by default on local run to avoid cluttering people's consoles;
    // Only printed on CI or if KSCRIPT_SHELL_ACCESS_DEBUG is set
    if (!isMacOs() || !System.getenv("KSCRIPT_SHELL_ACCESS_DEBUG").isNullOrEmpty()) {
        System.err.println("Executing command [workingDir: '$workingDir']: $commandToLog")
    }

    return ProcessBuilder("/bin/sh", "-c", this)
        .directory(workingDir)
        .redirectOutput(stdoutRedirectBehavior)
        .redirectError(stderrRedirectBehavior)
        .start()
        .apply {
            waitFor(timeoutAmount, timeoutUnit)
            if (isAlive) {
                destroyForcibly()
                println("Command timed out after ${timeoutUnit.toSeconds(timeoutAmount)} seconds: '$commandToLog'")
                if (
                    stdoutRedirectBehavior.type() == Redirect.Type.PIPE ||
                    stderrRedirectBehavior.type() == Redirect.Type.PIPE
                ) {
                    println(
                        listOf(
                            "Note: Timeout can potentially be due to deadlock when using stdout=PIPE and/or stderr=PIPE",
                            " and the child process (subpocess running the command) generates enough output to a pipe",
                            " (~50 KB) such that it blocks waiting for the OS pipe buffer to accept more data.",
                            "Please consider writing to a stdout/stderr to temp-file instead in such situations!"
                        ).joinToString("")
                    )
                }
                timeoutMessage?.let { println(it) }
                exitProcess(1)
            }
        }
        .let { process ->
            val result = ProcessResult(process.exitValue(), process.inputStream.bufferedReader(), process.errorStream.bufferedReader())
            check(!(throwOnFailure && result.failed)) {
                "Command failed with exit-code(${process.exitValue()}): '$commandToLog'"
            }
            result
        }
}

fun isMacOs(): Boolean = System.getProperty("os.name").contains("mac", ignoreCase = true)

data class ProcessResult(val exitCode: Int, val stdOut: BufferedReader, val stdErr: BufferedReader) {
    val succeeded: Boolean = exitCode == 0
    val failed: Boolean = !succeeded
}

fun BufferedReader.print() {
    lineSequence().forEach { println(it) }
}
