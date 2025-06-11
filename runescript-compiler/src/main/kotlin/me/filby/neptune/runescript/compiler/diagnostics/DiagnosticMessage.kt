package me.filby.neptune.runescript.compiler.diagnostics

/**
 * A class containing different types of diagnostic message texts.
 */
public object DiagnosticMessage {
    // internal compiler errors
    public const val UNSUPPORTED_SYMBOLTYPE_TO_TYPE: String =
        "Internal compiler error: Unsupported SymbolType -> Type conversion: %s"
    public const val CASE_WITHOUT_SWITCH: String = "Internal compiler error: Case without switch statement as parent."
    public const val RETURN_ORPHAN: String =
        "Internal compiler error: Orphaned `return` statement, no parent `script` node found."
    public const val TRIGGER_TYPE_NOT_FOUND: String = "Internal compiler error: The trigger '%s' has no declaration."

    // custom command handler errors
    public const val CUSTOM_HANDLER_NOTYPE: String =
        "Internal compiler error: Custom command handler did not assign return type."
    public const val CUSTOM_HANDLER_NOSYMBOL: String =
        "Internal compiler error: Custom command handler did not assign symbol."

    // code gen internal compiler errors
    public const val SYMBOL_IS_NULL: String = "Internal compiler error: Symbol has not been defined for the node."
    public const val TYPE_HAS_NO_BASETYPE: String = "Internal compiler error: Type has no defined base type: %s."
    public const val TYPE_HAS_NO_DEFAULT: String =
        "Internal compiler error: Return type '%s' has no defined default value."
    public const val INVALID_CONDITION: String =
        "Internal compiler error: %s is not a supported expression type for conditions."
    public const val NULL_CONSTANT: String = "Internal compiler error: %s evaluated to 'null' constant value."
    public const val EXPRESSION_NO_SUBEXPR: String = "Internal compiler error: No sub expression node."

    // node type agnostic messages
    public const val GENERIC_INVALID_TYPE: String = "'%s' is not a valid type."
    public const val GENERIC_TYPE_MISMATCH: String = "Type mismatch: '%s' was given but '%s' was expected."
    public const val GENERIC_UNRESOLVED_SYMBOL: String = "'%s' could not be resolved to a symbol."
    public const val ARITHMETIC_INVALID_TYPE: String = "Type mismatch: '%s' was given but 'int' or 'long' was expected."

    // script node specific
    public const val SCRIPT_REDECLARATION: String = "[%s,%s] is already defined."
    public const val SCRIPT_LOCAL_REDECLARATION: String = "'$%s' is already defined."
    public const val SCRIPT_TRIGGER_INVALID: String = "'%s' is not a valid trigger type."
    public const val SCRIPT_TRIGGER_NO_PARAMETERS: String =
        "The trigger type '%s' is not allowed to have parameters defined."
    public const val SCRIPT_TRIGGER_EXPECTED_PARAMETERS: String = "The trigger type '%s' is expected to accept (%s)."
    public const val SCRIPT_TRIGGER_NO_RETURNS: String = "The trigger type '%s' is not allowed to return values."
    public const val SCRIPT_TRIGGER_EXPECTED_RETURNS: String = "The trigger type '%s' is expected to return (%s)."
    public const val SCRIPT_SUBJECT_ONLY_GLOBAL: String = "Trigger '%s' only allows global subjects."
    public const val SCRIPT_SUBJECT_NO_GLOBAL: String = "Trigger '%s' does not allow global subjects."
    public const val SCRIPT_SUBJECT_NO_CAT: String = "Trigger '%s' does not allow category subjects."

    // switch statement node specific
    public const val SWITCH_INVALID_TYPE: String = "'%s' is not allowed within a switch statement."
    public const val SWITCH_DUPLICATE_DEFAULT: String = "Duplicate default label."
    public const val SWITCH_CASE_NOT_CONSTANT: String = "Switch case value is not a constant expression."

    // assignment statement node specific
    public const val ASSIGN_MULTI_ARRAY: String = "Arrays are not allowed in multi-assignment statements."

    // condition expression specific
    public const val CONDITION_INVALID_NODE_TYPE: String = "Conditions are only allowed to be binary expressions."

    // binary expression specific
    public const val BINOP_INVALID_TYPES: String = "Operator '%s' cannot be applied to '%s', '%s'."
    public const val BINOP_TUPLE_TYPE: String = "%s side of binary expressions can only have one type but has '%s'."

    // call expression specific
    public const val COMMAND_REFERENCE_UNRESOLVED: String = "'%s' cannot be resolved to a command."
    public const val COMMAND_NOARGS_EXPECTED: String = "'%s' is expected to have no arguments but has '%s'."
    public const val PROC_REFERENCE_UNRESOLVED: String = "'~%s' cannot be resolved to a proc."
    public const val PROC_NOARGS_EXPECTED: String = "'~%s' is expected to have no arguments but has '%s'."
    public const val JUMP_REFERENCE_UNRESOLVED: String = "'@%s' cannot be resolved to a label."
    public const val JUMP_NOARGS_EXPECTED: String = "'@%s' is expected to have no arguments but has '%s'."
    public const val CLIENTSCRIPT_REFERENCE_UNRESOLVED: String = "'%s' cannot be resolved to a clientscript."
    public const val CLIENTSCRIPT_NOARGS_EXPECTED: String = "'%s' is expected to have no arguments but has '%s'."
    public const val HOOK_TRANSMIT_LIST_UNEXPECTED: String = "Unexpected hook transmit list."

    // local variable specific
    public const val LOCAL_DECLARATION_INVALID_TYPE: String = "'%s' is not allowed to be declared as a type."
    public const val LOCAL_REFERENCE_UNRESOLVED: String = "'$%s' cannot be resolved to a local variable."
    public const val LOCAL_REFERENCE_NOT_ARRAY: String = "Access of indexed value of non-array type variable '$%s'."
    public const val LOCAL_ARRAY_INVALID_TYPE: String = "'%s' is not allowed to be used as an array."
    public const val LOCAL_ARRAY_REFERENCE_NOINDEX: String =
        "'$%s' is a reference to an array variable without specifying the index."

    // game var specific
    public const val GAME_REFERENCE_UNRESOLVED: String = "'%%%s' cannot be resolved to a game variable."

    // constant variable specific
    public const val CONSTANT_REFERENCE_UNRESOLVED: String = "'^%s' cannot be resolved to a constant."
    public const val CONSTANT_CYCLIC_REF: String = "Cyclic constant references are not permitted: %s."
    public const val CONSTANT_UNKNOWN_TYPE: String = "Unable to infer type for '^%s'."
    public const val CONSTANT_PARSE_ERROR: String = "Unable to parse constant value of '%s' into type '%s'."
    public const val CONSTANT_NONCONSTANT: String = "Constant value of '%s' evaluated to a non-constant expression."

    // prefix/postfix operator specific
    public const val FIX_INVALID_VARIABLE_KIND: String = "%s operator '%s' cannot be applied to arrays."
    public const val FIX_OPERATOR_INVALID_TYPE: String = "%s operator '%s' cannot be applied to type '%s'."

    // feature specific errors
    public const val FEATURE_UNSUPPORTED: String = "Compiler feature '%s' is not enabled."
}
