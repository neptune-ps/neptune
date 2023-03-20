package me.filby.neptune.clientscript.compiler.command

import me.filby.neptune.clientscript.compiler.command.DbFindCommandHandler.DbColumnType
import me.filby.neptune.runescript.compiler.configuration.command.DynamicCommandHandler
import me.filby.neptune.runescript.compiler.configuration.command.TypeCheckingContext
import me.filby.neptune.runescript.compiler.type
import me.filby.neptune.runescript.compiler.type.MetaType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.TupleType

/**
 * Handles the `db_getfield` command that returns a dynamic type
 * based on the column that was passed in.
 *
 * Example:
 * ```
 * $int, $obj, $string = db_getfield(some_row, table:column, 0);
 * ```
 */
class DbGetFieldCommandHandler : DynamicCommandHandler {
    override fun TypeCheckingContext.typeCheck() {
        val dbrowType = typeManager.find("dbrow")

        // check first argument as a dbrow
        checkArgument(0, dbrowType)

        // check column as dbcolumn
        val columnExpr = checkArgument(1, DbColumnType(MetaType.Any))

        // check field id as int
        checkArgument(2, PrimitiveType.INT)

        // typehint the second argument using the dbcolumn type if it was valid
        val columnReturnType = (columnExpr?.type as? DbColumnType)?.inner

        // define the expected types based on what is currently known
        val expectedTypes = TupleType(
            dbrowType,
            DbColumnType(columnReturnType ?: MetaType.Any),
            PrimitiveType.INT
        )

        // compare the expected types with the actual types
        if (!checkArgumentTypes(expectedTypes)) {
            expression.type = MetaType.Error
            return
        }

        // verify the columnExpr type is valid
        if (columnReturnType == null) {
            val reportExpr = columnExpr ?: expression
            reportExpr.reportError(diagnostics, "Unable to extract type from argument.")
            expression.type = MetaType.Error
            return
        }

        // set the return type
        expression.type = columnReturnType
    }
}
