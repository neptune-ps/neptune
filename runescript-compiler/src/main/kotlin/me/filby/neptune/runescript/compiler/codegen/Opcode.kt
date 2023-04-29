package me.filby.neptune.runescript.compiler.codegen

import me.filby.neptune.runescript.compiler.codegen.script.Label
import me.filby.neptune.runescript.compiler.codegen.script.SwitchTable
import me.filby.neptune.runescript.compiler.symbol.BasicSymbol
import me.filby.neptune.runescript.compiler.symbol.LocalVariableSymbol
import me.filby.neptune.runescript.compiler.symbol.ScriptSymbol
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.type.BaseVarType

/**
 * Represents a single bytecode opcode. Each opcode has a single operand type of [T].
 */
public sealed class Opcode<T : Any> {
    /**
     * Pushes a constant [Int] to the stack.
     *
     * Operand: The value to push to the stack.
     */
    public object PushConstantInt : Opcode<Int>()

    /**
     * Pushes a constant [String] to the stack.
     *
     * Operand: The value to push to the stack.
     */
    public object PushConstantString : Opcode<String>()

    /**
     * Pushes a constant [Long] to the stack.
     *
     * Operand: The value to push to the stack.
     */
    public object PushConstantLong : Opcode<Long>()

    /**
     * Pushes a constant [Symbol] to the stack.
     *
     * Operand: The value to push to the stack.
     */
    public object PushConstantSymbol : Opcode<Symbol>()

    /**
     * Pushes the value of the local variable to the stack.
     *
     * Operand: The local variable.
     */
    public object PushLocalVar : Opcode<LocalVariableSymbol>()

    /**
     * Pops a value from the stack and stores it in the local variable.
     *
     * Operand: The local variable.
     */
    public object PopLocalVar : Opcode<LocalVariableSymbol>()

    /**
     * Pushes the value of the game variable to the stack.
     *
     * Operand: The game variable.
     */
    public object PushVar : Opcode<BasicSymbol>()

    /**
     * Pops a value from the stack and stores it in the game variable.
     *
     * Operand: The game variable.
     */
    public object PopVar : Opcode<BasicSymbol>()

    /**
     * Defines a local array variable.
     *
     * Operand: The local variable.
     */
    public object DefineArray : Opcode<LocalVariableSymbol>()

    /**
     * Looks up a location to jump to based on a popped value from the stack.
     *
     * Operand: The switch table defining all cases and jump locations.
     */
    public object Switch : Opcode<SwitchTable>()

    /**
     * Jumps to the defined label.
     *
     * Operand: The label to jump to.
     */
    public object Branch : Opcode<Label>()

    /**
     * Pops two [Int]s from the stack and if they are **not** equal (`!`) jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object BranchNot : Opcode<Label>()

    /**
     * Pops two [Int]s from the stack and if they are equal (`=`) jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object BranchEquals : Opcode<Label>()

    /**
     * Pops two [Int]s from the stack and if the first is less than (`<`) the second jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object BranchLessThan : Opcode<Label>()

    /**
     * Pops two [Int]s from the stack and if the first is greater than (`>`) the second jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object BranchGreaterThan : Opcode<Label>()

    /**
     * Pops two [Int]s from the stack and if the first is less than or equal (`<=`) to the second jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object BranchLessThanOrEquals : Opcode<Label>()

    /**
     * Pops two [Int]s from the stack and if the first is greater than or equal (`>=`) to the second jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object BranchGreaterThanOrEquals : Opcode<Label>()

    /**
     * Pops two [Long]s from the stack and if they are **not** equal (`!`) jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object LongBranchNot : Opcode<Label>()

    /**
     * Pops two [Long]s from the stack and if they are equal (`=`) jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object LongBranchEquals : Opcode<Label>()

    /**
     * Pops two [Long]s from the stack and if the first is less than (`<`) the second jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object LongBranchLessThan : Opcode<Label>()

    /**
     * Pops two [Long]s from the stack and if the first is greater than (`>`) the second jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object LongBranchGreaterThan : Opcode<Label>()

    /**
     * Pops two [Long]s from the stack and if the first is less than or equal (`<=`) to the second jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object LongBranchLessThanOrEquals : Opcode<Label>()

    /**
     * Pops two [Long]s from the stack and if the first is greater than or equal (`>=`) to the second jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object LongBranchGreaterThanOrEquals : Opcode<Label>()

    /**
     * Pops two [Any]s from the stack and if they are **not** equal (`!`) jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object ObjBranchNot : Opcode<Label>()

    /**
     * Pops two [Any]s from the stack and if they are equal (`=`) jumps to the label.
     *
     * Operand: The label to jump to.
     */
    public object ObjBranchEquals : Opcode<Label>()

    /**
     * Joins a number of strings together into a single string.
     *
     * Operand: The number of strings to join together.
     */
    public object JoinString : Opcode<Int>()

    /**
     * Discards a value on the stack.
     *
     * Operand: The stack type to discard a value from.
     */
    public object Discard : Opcode<BaseVarType>()

    /**
     * Calls another script with an optional return values.
     *
     * Operand: The script to call.
     */
    public object Gosub : Opcode<ScriptSymbol>()

    /**
     * Jumps to another script while never returning to the original call site.
     *
     * Operand: The script to jump to.
     */
    public object Jump : Opcode<ScriptSymbol>()

    /**
     * Calls an engine command with optional return values.
     *
     * Operand: The command to call.
     */
    public object Command : Opcode<ScriptSymbol>()

    /**
     * Returns from the script.
     *
     * Operand: N/A
     */
    public object Return : Opcode<Unit>()

    // math operations

    /**
     * Adds two [Int]s.
     *
     * Operand: N/A
     */
    public object Add : Opcode<Unit>()

    /**
     * Subtracts two [Int]s.
     *
     * Operand: N/A
     */
    public object Sub : Opcode<Unit>()

    /**
     * Multiplies two [Int]s together.
     *
     * Operand: N/A
     */
    public object Multiply : Opcode<Unit>()

    /**
     * Divides two [Int]s together.
     *
     * Operand: N/A
     */
    public object Divide : Opcode<Unit>()

    /**
     * Finds the remainder when dividing two [Int]s.
     *
     * Operand: N/A
     */
    public object Modulo : Opcode<Unit>()

    /**
     * Applies bitwise-or on two [Int]s.
     *
     * Operand: N/A
     */
    public object Or : Opcode<Unit>()

    /**
     * Applies bitwise-and on two [Int]s.
     *
     * Operand: N/A
     */
    public object And : Opcode<Unit>()

    /**
     * Adds two [Long]s.
     *
     * Operand: N/A
     */
    public object LongAdd : Opcode<Unit>()

    /**
     * Subtracts two [Long]s.
     *
     * Operand: N/A
     */
    public object LongSub : Opcode<Unit>()

    /**
     * Multiplies two [Long]s together.
     *
     * Operand: N/A
     */
    public object LongMultiply : Opcode<Unit>()

    /**
     * Divides two [Long]s together.
     *
     * Operand: N/A
     */
    public object LongDivide : Opcode<Unit>()

    /**
     * Finds the remainder when dividing two [Long]s.
     *
     * Operand: N/A
     */
    public object LongModulo : Opcode<Unit>()

    /**
     * Applies bitwise-or on two [Long]s.
     *
     * Operand: N/A
     */
    public object LongOr : Opcode<Unit>()

    /**
     * Applies bitwise-and on two [Long]s.
     *
     * Operand: N/A
     */
    public object LongAnd : Opcode<Unit>()

    /**
     * Marks the source line number of the code that follows. This opcode is
     * not meant to have any runtime alternative and is just intended for
     * building a line number table.
     *
     * Operand: The source line number.
     */
    public object LineNumber : Opcode<Int>()
}
