package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.runescript.compiler.codegen.Opcode
import me.filby.neptune.runescript.compiler.configuration.command.CodeGeneratorContext
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType
import me.filby.neptune.runescript.compiler.type.Type
import me.filby.neptune.runescript.compiler.type.wrapped.WrappedType

public class DbFindCommandHandler(private val withCount: Boolean) : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        // lookup the column expression
        val columnExpr = checkArgument(0, DbColumnType(MetaType.Any))

        // typehint the second argument using the dbcolumn type if it was valid
        val keyType = (columnExpr?.type as? DbColumnType)?.inner
        checkArgument(1, keyType)

        // define the expected types based on what is currently known
        val expectedTypes = TupleType(
            DbColumnType(keyType ?: MetaType.Any),
            keyType ?: MetaType.Any
        )

        // check that the key type is not a tuple type
        if (keyType is TupleType) {
            columnExpr.reportError(diagnostics, "Tuple columns are not supported.")
        } else {
            // compare the expected types with the actual types
            checkArgumentTypes(expectedTypes)
        }

        // set the return type
        expression.type = if (withCount) PrimitiveType.INT else MetaType.Unit
    }

    override fun CodeGeneratorContext.generateCode() {
        // should not get to this point unless first argument is a dbcolumn
        val columnType = (expression.arguments[0].type as DbColumnType).inner
        val stackType = when (val baseType = columnType.baseType) {
            BaseVarType.INTEGER -> 0
            BaseVarType.LONG -> 1
            BaseVarType.STRING -> 2
            else -> error("Unsupported base type: $baseType")
        }

        // emit the arguments
        expression.arguments.visit()

        // emit the stack type to pop the key value from
        instruction(Opcode.PushConstantInt, stackType)

        // emit the command
        command()
    }

    public class DbColumnType(override val inner: Type) : WrappedType {
        public override val representation: String = "dbcolumn<${inner.representation}>"
        public override val code: Char? = null
        public override val baseType: BaseVarType = BaseVarType.INTEGER
        public override val defaultValue: Any = -1
    }
}
