#!/usr/bin/env kscript
@file:DependsOn("info.picocli:picocli:3.9.6")

/**
 * Defines a standard pattern for collecting arguments from the command line using the Picocli library.
 * Manual is at https://picocli.info/man/3.x
 */

import picocli.CommandLine
import java.lang.reflect.Field
import kotlin.script.templates.standard.ScriptTemplateWithArgs

/**
 * Have a class extend this, and add Picocli annotations to define arguments.
 * Call [parseArgs] with this class type to parse the cli args into your kotlin class.
 *
 */
abstract class CommandLineArgs : Runnable {

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Show help"])
    var usageHelpRequested: Boolean = false

    override fun run() {}

    /** Builds a toString reflectively using whatever properties are defined in the class. */
    override fun toString(): String {
        return "${javaClass.simpleName} {" +
            "\n${props.joinToString(separator = ",\n") { "\t${it.name} : ${it.get(this)}" }}" +
            "\n}"
    }

    private val props: List<Field>
        get() {
            return javaClass.declaredFields.toList().onEach { it.isAccessible = true }
        }
}

/** Parses the cli args into your custom kotlin class of the given type. */
inline fun <reified T : CommandLineArgs> ScriptTemplateWithArgs.parseArgs(): T {
    val argsClass = T::class.java.newInstance()
    CommandLine.run(argsClass, System.err, *args)
    if (argsClass.usageHelpRequested) {
        System.exit(0)
    }
    return argsClass
}
