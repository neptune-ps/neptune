package me.filby.neptune.runescript.compiler.trigger

import me.filby.neptune.runescript.compiler.pointer.PointerType
import me.filby.neptune.runescript.compiler.type.Type
import java.util.EnumSet

enum class TestTriggerType(
    override val subjectMode: SubjectMode = SubjectMode.Name,
    override val allowParameters: Boolean = false,
    override val parameters: Type? = null,
    override val allowReturns: Boolean = false,
    override val returns: Type? = null,
    override val pointers: Set<PointerType>? = null
) : TriggerType {
    TEST,
    PROC(allowParameters = true, allowReturns = true, pointers = EnumSet.allOf(PointerType::class.java)),
    LABEL(allowParameters = true, pointers = EnumSet.allOf(PointerType::class.java)),
    ;

    override val id: Int = ordinal

    override val identifier: String get() = name.lowercase()
}
