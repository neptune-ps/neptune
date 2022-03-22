package me.filby.neptune.runescript.compiler.diagnostics

/**
 * A class containing different types of diagnostic message texts.
 */
internal object DiagnosticMessage {
    // internal compiler errors
    const val UNSUPPORTED_SYMBOLTYPE_TO_TYPE = "Internal compiler error: Unsupported SymbolType -> Type conversion: %s"
    const val NULL_TYPE_IN_ASSIGNMENT = "Internal compiler error: null type in assignment: lhs=%s, rhs=%s"
    const val INVALID_BINARYEXPR_TYPEHINT = "Internal compiler error: int or boolean type hint expected: %s"
    const val INVALID_MATHOP = "Internal compiler error: '%s' is not a valid binary math operation."

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

    // binary expression specific
    const val BINOP_INVALID_TYPES = "Operator '%s' cannot be applied to '%s', '%s'."

    // local variable specific
    const val LOCAL_REFERENCE_UNRESOLVED = "'$%s' cannot be resolved to a local variable."
    const val LOCAL_REFERENCE_NOT_ARRAY = "Access of indexed value of non-array type variable '$%s'."
    const val LOCAL_ARRAY_REFERENCE_NOINDEX = "'$%s' is a reference to an array variable without specifying the index."
}
