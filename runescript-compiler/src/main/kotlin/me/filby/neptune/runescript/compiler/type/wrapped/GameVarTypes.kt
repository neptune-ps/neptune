package me.filby.neptune.runescript.compiler.type.wrapped

import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.PrimitiveType
import me.filby.neptune.runescript.compiler.type.Type

// base game variable type
public sealed interface GameVarType : WrappedType {
    override val code: Char?
        get() = null

    override val baseType: BaseVarType?
        get() = null

    override val defaultValue: Any?
        get() = null
}

// implementations
public data class VarPlayerType(override val inner: Type) : GameVarType {
    override val representation: String = "varp<${inner.representation}>"
}

public object VarBitType : GameVarType {
    override val inner: Type = PrimitiveType.INT
    override val representation: String = "varbit<${inner.representation}>"
    override fun toString(): String = "VarBitType"
}

public data class VarClientType(override val inner: Type) : GameVarType {
    override val representation: String = "varc<${inner.representation}>"
}

public data class VarClanType(override val inner: Type) : GameVarType {
    override val representation: String = "varclan<${inner.representation}>"
}

public data class VarClanSettingsType(override val inner: Type) : GameVarType {
    override val representation: String = "varclansettings<${inner.representation}>"
}
