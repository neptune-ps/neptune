package me.filby.neptune.runescript.runtime.impl.opcodes

/**
 * A class of constants for the runtime requires opcodes. These opcodes are
 * not required to be used, but are assumed to be used when using the base
 * opcode implementations.
 *
 * @see CoreOpcodesBase
 * @see MathOpcodesBase
 */
public object BaseCoreOpcodes {
    // language required ops
    public const val PUSH_CONSTANT_INT: Int = 0
    public const val PUSH_CONSTANT_STRING: Int = 1
    public const val PUSH_CONSTANT_LONG: Int = 2
    public const val PUSH_LOCAL: Int = 3
    public const val POP_LOCAL: Int = 4
    public const val PUSH_GAME: Int = 5
    public const val POP_GAME: Int = 6
    public const val BRANCH: Int = 7
    public const val BRANCH_NOT: Int = 8
    public const val BRANCH_EQUALS: Int = 9
    public const val BRANCH_LESS_THAN: Int = 10
    public const val BRANCH_GREATER_THAN: Int = 11
    public const val BRANCH_LESS_THAN_OR_EQUALS: Int = 12
    public const val BRANCH_GREATER_THAN_OR_EQUALS: Int = 13
    public const val LONG_BRANCH_NOT: Int = 14
    public const val LONG_BRANCH_EQUALS: Int = 15
    public const val LONG_BRANCH_LESS_THAN: Int = 16
    public const val LONG_BRANCH_GREATER_THAN: Int = 17
    public const val LONG_BRANCH_LESS_THAN_OR_EQUALS: Int = 18
    public const val LONG_BRANCH_GREATER_THAN_OR_EQUALS: Int = 19
    public const val OBJ_BRANCH_NOT: Int = 20
    public const val OBJ_BRANCH_EQUALS: Int = 21
    public const val JOIN_STRING: Int = 22
    public const val POP_DISCARD: Int = 23
    public const val GOSUB_WITH_PARAMS: Int = 24
    public const val RETURN: Int = 25
    public const val SWITCH: Int = 26

    // int math ops
    public const val ADD: Int = 100
    public const val SUB: Int = 101
    public const val MULTIPLY: Int = 102
    public const val DIVIDE: Int = 103
    public const val MODULO: Int = 104
    public const val OR: Int = 105
    public const val AND: Int = 106

    // long math ops
    public const val ADD_LONG: Int = 200
    public const val SUB_LONG: Int = 201
    public const val MULTIPLY_LONG: Int = 202
    public const val DIVIDE_LONG: Int = 203
    public const val MODULO_LONG: Int = 204
    public const val OR_LONG: Int = 205
    public const val AND_LONG: Int = 206

    // TODO remove the below opcodes

    // string utils
    public const val TOSTRING: Int = 300
    public const val TOSTRING_LONG: Int = 301
    public const val STRING_LENGTH: Int = 302
    public const val SUBSTRING: Int = 303
    public const val STRING_INDEXOF_STRING: Int = 304
    public const val APPEND: Int = 305

    // misc
    public const val PRINTLN: Int = 400
    public const val TIME: Int = 401
    public const val DELAY: Int = 402
    public const val STACKTRACE_SIZE: Int = 403
    public const val STACKTRACE_GET: Int = 404
}
