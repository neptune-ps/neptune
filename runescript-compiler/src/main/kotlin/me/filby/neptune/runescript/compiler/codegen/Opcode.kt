package me.filby.neptune.runescript.compiler.codegen

public sealed class Opcode {
    public object PushConstant : Opcode()
    public object PushVar : Opcode()
    public object PopVar : Opcode()
    public object DefineArray : Opcode()
    public object Switch : Opcode()
    public object Branch : Opcode()
    public object BranchNot : Opcode()
    public object BranchEquals : Opcode()
    public object BranchLessThan : Opcode()
    public object BranchGreaterThan : Opcode()
    public object BranchLessThanOrEquals : Opcode()
    public object BranchGreaterThanOrEquals : Opcode()
    public object LongBranchNot : Opcode()
    public object LongBranchEquals : Opcode()
    public object LongBranchLessThan : Opcode()
    public object LongBranchGreaterThan : Opcode()
    public object LongBranchLessThanOrEquals : Opcode()
    public object LongBranchGreaterThanOrEquals : Opcode()
    public object ObjBranchNot : Opcode()
    public object ObjBranchEquals : Opcode()
    public object JoinString : Opcode()
    public object Discard : Opcode()
    public object Gosub : Opcode()
    public object Command : Opcode()
    public object Return : Opcode()

    // math operations
    public object Add : Opcode()
    public object Sub : Opcode()
    public object Multiply : Opcode()
    public object Divide : Opcode()
    public object Modulo : Opcode()
    public object Or : Opcode()
    public object And : Opcode()
    public object LongAdd : Opcode()
    public object LongSub : Opcode()
    public object LongMultiply : Opcode()
    public object LongDivide : Opcode()
    public object LongModulo : Opcode()
    public object LongOr : Opcode()
    public object LongAnd : Opcode()

    /**
     * Marks the source line number of the code that follows. This opcode is
     * not meant to have any runtime alternative and is just intended for
     * building a line number table.
     *
     * Operand:
     *  - [Int] - The source line number.
     */
    public object LineNumber : Opcode()
}
