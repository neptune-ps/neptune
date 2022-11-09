package me.filby.neptune.runescript.compiler.codegen

public enum class Opcode {
    PUSH_CONSTANT,
    PUSH_VAR,
    POP_VAR,
    DEFINE_ARRAY,
    SWITCH,
    BRANCH,
    BRANCH_NOT,
    BRANCH_EQUALS,
    BRANCH_LESS_THAN,
    BRANCH_GREATER_THAN,
    BRANCH_LESS_THAN_OR_EQUALS,
    BRANCH_GREATER_THAN_OR_EQUALS,
    LONG_BRANCH_NOT,
    LONG_BRANCH_EQUALS,
    LONG_BRANCH_LESS_THAN,
    LONG_BRANCH_GREATER_THAN,
    LONG_BRANCH_LESS_THAN_OR_EQUALS,
    LONG_BRANCH_GREATER_THAN_OR_EQUALS,
    OBJ_BRANCH_NOT,
    OBJ_BRANCH_EQUALS,
    JOIN_STRING,
    DISCARD,
    GOSUB,
    COMMAND,
    RETURN,

    // math operations
    ADD,
    SUB,
    MULTIPLY,
    DIVIDE,
    MODULO,
    OR,
    AND,
    LONG_ADD,
    LONG_SUB,
    LONG_MULTIPLY,
    LONG_DIVIDE,
    LONG_MODULO,
    LONG_OR,
    LONG_AND,

    /**
     * Marks the source line number of the code that follows. This opcode is
     * not meant to have any runtime alternative and is just intended for
     * building a line number table.
     *
     * Operand:
     *  - [Int] - The source line number.
     */
    LINENUMBER,
    ;
}
