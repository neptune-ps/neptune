package me.filby.neptune.runescript.compiler.diagnostics

/**
 * A class containing different types of diagnostic message texts.
 */
internal object DiagnosticMessage {
    // internal compiler errors
    const val UNSUPPORTED_SYMBOLTYPE_TO_TYPE = "Internal compiler error: Unsupported SymbolType -> Type conversion: %s"
    const val INVALID_BINEXP_STATE = "Internal compiler error: not in condition or calc"
    const val INVALID_MATHOP = "Internal compiler error: '%s' is not a valid binary math operation."
    const val INVALID_CONDITIONOP = "Internal compiler error: '%s' is not a valid binary condition operation."
    const val CASE_WITHOUT_SWITCH = "Internal compiler error: Case without switch statement as parent."
    const val LOCAL_REFERENCE_WRONG = "Internal compiler error: '$%s' refers to a non-local variable symbol: %s"
    const val RETURN_ORPHAN = "Internal compiler error: Orphaned `return` statement, no parent `script` node found."

    // code gen internal compiler errors
    const val SYMBOL_IS_NULL = "Internal compiler error: Symbol has not been defined for the node."
    const val TYPE_HAS_NO_BASETYPE = "Internal compiler error: Type has no defined base type: %s."
    const val TYPE_HAS_NO_DEFAULT = "Internal compiler error: Return type '%s' has no defined default value."
    const val INVALID_CONDITION = "Internal compiler error: %s is not a supported expression type for conditions."
    const val NULL_CONSTANT = "Internal compiler error: %s evaluated to 'null' constant value."
    const val STRINGLIT_NO_SUBEXPR = "Internal compiler error: No sub expression node."

    // node type agnostic messages
    const val GENERIC_INVALID_TYPE = "'%s' is not a valid type."
    const val GENERIC_TYPE_MISMATCH = "Type mismatch: '%s' was given but '%s' was expected."
    const val GENERIC_UNRESOLVED_SYMBOL = "'%s' could not be resolved to a symbol."

    // script node specific
    const val SCRIPT_REDECLARATION = "[%s,%s] is already defined."
    const val SCRIPT_LOCAL_REDECLARATION = "'$%s' is already defined."
    const val SCRIPT_TRIGGER_INVALID = "'%s' is not a valid trigger type."
    const val SCRIPT_TRIGGER_NO_PARAMETERS = "The trigger type '%s' is not allowed to have parameters defined."
    const val SCRIPT_TRIGGER_EXPECTED_PARAMETERS = "The trigger type '%s' is expected to accept (%s)."
    const val SCRIPT_TRIGGER_NO_RETURNS = "The trigger type '%s' is not allowed to return values."
    const val SCRIPT_TRIGGER_EXPECTED_RETURNS = "The trigger type '%s' is expected to return (%s)."

    // switch statement node specific
    const val SWITCH_DUPLICATE_DEFAULT = "Duplicate default label."
    const val SWITCH_CASE_NOT_CONSTANT = "Switch case value is not a constant expression."

    // assignment statement node specific
    const val ASSIGN_MULTI_ARRAY = "Arrays are not allowed in multi-assignment statements."

    // condition expression specific
    const val CONDITION_INVALID_NODE_TYPE = "Conditions are only allowed to be binary expressions."

    // binary expression specific
    const val BINOP_INVALID_TYPES = "Operator '%s' cannot be applied to '%s', '%s'."
    const val BINOP_TUPLE_TYPE = "%s side of binary expressions can only have one type but has '%s'."

    // call expression specific
    const val COMMAND_REFERENCE_UNRESOLVED = "'%s' cannot be resolved to a command."
    const val COMMAND_NOARGS_EXPECTED = "'%s' is expected to have no arguments but has '%s'."
    const val PROC_REFERENCE_UNRESOLVED = "'~%s' cannot be resolved to a proc."
    const val PROC_NOARGS_EXPECTED = "'~%s' is expected to have no arguments but has '%s'."
    const val JUMP_REFERENCE_UNRESOLVED = "'%s' cannot be resolved to a label."

    // jump expression specific
    const val JUMP_CALL_IN_CS2 = "Jumps are not allowed in ClientScript."

    // local variable specific
    const val LOCAL_REFERENCE_UNRESOLVED = "'$%s' cannot be resolved to a local variable."
    const val LOCAL_REFERENCE_NOT_ARRAY = "Access of indexed value of non-array type variable '$%s'."
    const val LOCAL_ARRAY_REFERENCE_NOINDEX = "'$%s' is a reference to an array variable without specifying the index."

    // game var specific
    const val GAME_REFERENCE_UNRESOLVED = "'%%%s' cannot be resolved to a game variable."

    // constant variable specific
    const val CONSTANT_REFERENCE_UNRESOLVED = "'^%s' cannot be resolved to a constant."
}
