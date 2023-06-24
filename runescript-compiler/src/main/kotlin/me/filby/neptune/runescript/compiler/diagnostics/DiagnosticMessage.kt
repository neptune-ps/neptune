package me.filby.neptune.runescript.compiler.diagnostics

/**
 * A class containing different types of diagnostic message texts.
 */
internal object DiagnosticMessage {
    // internal compiler errors
    const val UNSUPPORTED_SYMBOLTYPE_TO_TYPE = "Internal compiler error: Unsupported SymbolType -> Type conversion: %s"
    const val CASE_WITHOUT_SWITCH = "Internal compiler error: Case without switch statement as parent."
    const val RETURN_ORPHAN = "Internal compiler error: Orphaned `return` statement, no parent `script` node found."
    const val TRIGGER_TYPE_NOT_FOUND = "Internal compiler error: The trigger '%s' has no declaration."

    // custom command handler errors
    const val CUSTOM_HANDLER_NOTYPE = "Internal compiler error: Custom command handler did not assign return type."
    const val CUSTOM_HANDLER_NOSYMBOL = "Internal compiler error: Custom command handler did not assign symbol."

    // code gen internal compiler errors
    const val SYMBOL_IS_NULL = "Internal compiler error: Symbol has not been defined for the node."
    const val TYPE_HAS_NO_BASETYPE = "Internal compiler error: Type has no defined base type: %s."
    const val TYPE_HAS_NO_DEFAULT = "Internal compiler error: Return type '%s' has no defined default value."
    const val INVALID_CONDITION = "Internal compiler error: %s is not a supported expression type for conditions."
    const val NULL_CONSTANT = "Internal compiler error: %s evaluated to 'null' constant value."
    const val EXPRESSION_NO_SUBEXPR = "Internal compiler error: No sub expression node."

    // node type agnostic messages
    const val GENERIC_INVALID_TYPE = "'%s' is not a valid type."
    const val GENERIC_TYPE_MISMATCH = "Type mismatch: '%s' was given but '%s' was expected."
    const val GENERIC_UNRESOLVED_SYMBOL = "'%s' could not be resolved to a symbol."
    const val ARITHMETIC_INVALID_TYPE = "Type mismatch: '%s' was given but 'int' or 'long' was expected."

    // script node specific
    const val SCRIPT_REDECLARATION = "[%s,%s] is already defined."
    const val SCRIPT_LOCAL_REDECLARATION = "'$%s' is already defined."
    const val SCRIPT_TRIGGER_INVALID = "'%s' is not a valid trigger type."
    const val SCRIPT_TRIGGER_NO_PARAMETERS = "The trigger type '%s' is not allowed to have parameters defined."
    const val SCRIPT_TRIGGER_EXPECTED_PARAMETERS = "The trigger type '%s' is expected to accept (%s)."
    const val SCRIPT_TRIGGER_NO_RETURNS = "The trigger type '%s' is not allowed to return values."
    const val SCRIPT_TRIGGER_EXPECTED_RETURNS = "The trigger type '%s' is expected to return (%s)."
    const val SCRIPT_SUBJECT_ONLY_GLOBAL = "Trigger '%s' only allows global subjects."
    const val SCRIPT_SUBJECT_NO_GLOBAL = "Trigger '%s' does not allow global subjects."
    const val SCRIPT_SUBJECT_NO_CAT = "Trigger '%s' does not allow category subjects."

    // switch statement node specific
    const val SWITCH_INVALID_TYPE = "'%s' is not allowed within a switch statement."
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
    const val JUMP_REFERENCE_UNRESOLVED = "'@%s' cannot be resolved to a label."
    const val JUMP_NOARGS_EXPECTED = "'@%s' is expected to have no arguments but has '%s'."
    const val CLIENTSCRIPT_REFERENCE_UNRESOLVED = "'%s' cannot be resolved to a clientscript."
    const val CLIENTSCRIPT_NOARGS_EXPECTED = "'%s' is expected to have no arguments but has '%s'."

    // local variable specific
    const val LOCAL_DECLARATION_INVALID_TYPE = "'%s' is not allowed to be declared as a type."
    const val LOCAL_REFERENCE_UNRESOLVED = "'$%s' cannot be resolved to a local variable."
    const val LOCAL_REFERENCE_NOT_ARRAY = "Access of indexed value of non-array type variable '$%s'."
    const val LOCAL_ARRAY_INVALID_TYPE = "'%s' is not allowed to be used as an array."
    const val LOCAL_ARRAY_REFERENCE_NOINDEX = "'$%s' is a reference to an array variable without specifying the index."

    // game var specific
    const val GAME_REFERENCE_UNRESOLVED = "'%%%s' cannot be resolved to a game variable."

    // constant variable specific
    const val CONSTANT_REFERENCE_UNRESOLVED = "'^%s' cannot be resolved to a constant."
    const val CONSTANT_CYCLIC_REF = "Cyclic constant references are not permitted: %s."
    const val CONSTANT_UNKNOWN_TYPE = "Unable to infer type for '^%s'."
    const val CONSTANT_PARSE_ERROR = "Unable to parse constant value of '%s' into type '%s'."
    const val CONSTANT_NONCONSTANT = "Constant value of '%s' evaluated to a non-constant expression."
}
