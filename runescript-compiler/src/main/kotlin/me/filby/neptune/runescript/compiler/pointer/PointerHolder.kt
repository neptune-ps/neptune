package me.filby.neptune.runescript.compiler.pointer

/**
 * Contains sets of [PointerType]s used to mark which pointers are required, set,
 * or corrupted by the script or command.
 *
 * @property required The required pointer types to invoke.
 * @property set The pointer types that will be set after invoking.
 * @property conditionalSet When `true`, [set] will only be used when used in a condition.
 * @property corrupted The pointer types that will be corrupted when invoking.
 */
public data class PointerHolder(
    public val required: Set<PointerType>,
    public val set: Set<PointerType>,
    public val conditionalSet: Boolean,
    public val corrupted: Set<PointerType>,
)
