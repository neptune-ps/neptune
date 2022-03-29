package me.filby.neptune.runescript.compiler.codegen

import me.filby.neptune.runescript.compiler.codegen.script.Label
import me.filby.neptune.runescript.compiler.codegen.script.SwitchTable
import me.filby.neptune.runescript.compiler.symbol.Symbol
import me.filby.neptune.runescript.compiler.type.BaseVarType

// TODO probably will need multiple implementations for special cases
public sealed class Instruction<T>(public val opcode: Opcode, public val operand: T)

public class IntInstruction(opcode: Opcode, operand: Int) : Instruction<Int>(opcode, operand)
public class StringInstruction(opcode: Opcode, operand: String) : Instruction<String>(opcode, operand)
public class LongInstruction(opcode: Opcode, operand: Long) : Instruction<Long>(opcode, operand)
public class BranchInstruction(opcode: Opcode, operand: Label) : Instruction<Label>(opcode, operand)
public class SwitchInstruction(opcode: Opcode, operand: SwitchTable) : Instruction<SwitchTable>(opcode, operand)
public class ReferenceInstruction(opcode: Opcode, operand: Symbol) : Instruction<Symbol>(opcode, operand)
public class BaseTypeInstruction(opcode: Opcode, operand: BaseVarType) : Instruction<BaseVarType>(opcode, operand)
