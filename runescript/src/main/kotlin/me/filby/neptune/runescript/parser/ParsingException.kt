package me.filby.neptune.runescript.parser

import java.lang.RuntimeException

public class ParsingException(
    message: String?,
    cause: Throwable?,
    private val line: Int,
    private val column: Int
) : RuntimeException(message, cause) {

    override val message: String
        get() = "line %s:%s: %s".format(line, column, super.message)

}
