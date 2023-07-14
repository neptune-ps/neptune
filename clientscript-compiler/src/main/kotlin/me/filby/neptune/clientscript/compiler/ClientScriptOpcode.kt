package me.filby.neptune.clientscript.compiler

/**
 * An enumeration of opcode names with their id and how the operand is written in binary.
 *
 * This only contains the opcode and no information about the signatures of commands.
 */
enum class ClientScriptOpcode(val id: Int, val largeOperand: Boolean = false) {
    PUSH_CONSTANT_INT(0, true),
    PUSH_VARP(1, true),
    POP_VARP(2, true),
    PUSH_CONSTANT_STRING(3, true),
    BRANCH(6, true),
    BRANCH_NOT(7, true),
    BRANCH_EQUALS(8, true),
    BRANCH_LESS_THAN(9, true),
    BRANCH_GREATER_THAN(10, true),
    RETURN(21),
    PUSH_VARBIT(25, true),
    POP_VARBIT(27, true),
    BRANCH_LESS_THAN_OR_EQUALS(31, true),
    BRANCH_GREATER_THAN_OR_EQUALS(32, true),
    PUSH_INT_LOCAL(33, true),
    POP_INT_LOCAL(34, true),
    PUSH_STRING_LOCAL(35, true),
    POP_STRING_LOCAL(36, true),
    JOIN_STRING(37, true),
    POP_INT_DISCARD(38),
    POP_STRING_DISCARD(39),
    GOSUB_WITH_PARAMS(40, true),
    PUSH_VARC_INT(42, true),
    POP_VARC_INT(43, true),
    DEFINE_ARRAY(44, true),
    PUSH_ARRAY_INT(45, true),
    POP_ARRAY_INT(46, true),
    PUSH_VARC_STRING(49, true),
    POP_VARC_STRING(50, true),
    SWITCH(60, true),
    PUSH_VARCLANSETTING(74, true),
    PUSH_VARCLAN(76, true),
    ADD(4000),
    SUB(4001),
    MULTIPLY(4002),
    DIVIDE(4003),
    MODULO(4011),
    AND(4014),
    OR(4015),
}
