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
    AND
    ;
}
