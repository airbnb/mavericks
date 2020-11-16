#!/usr/bin/env kscript

import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

fun copyToClipboard(str: String): Throwable? {
    return catch {
        val stringSelection = StringSelection(str)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(stringSelection, null)
    }
}

fun openInBrowser(url: String): Throwable? {
    return openInBrowser(URI(url))
}

fun openInBrowser(uri: URI): Throwable? {
    return catch {
        Desktop.getDesktop()?.browse(uri)
    }
}

/** Try to run the given block, returning a Throwable if it fails with any exception. */
fun catch(block: () -> Unit): Throwable? {
    @Suppress("Detekt.TooGenericExceptionCaught")
    return try {
        block()
        null
    } catch (e: Throwable) {
        e
    }
}
