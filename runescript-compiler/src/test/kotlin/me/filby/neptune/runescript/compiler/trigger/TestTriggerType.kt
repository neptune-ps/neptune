package me.filby.neptune.runescript.compiler.trigger

import me.filby.neptune.runescript.compiler.type.Type

enum class TestTriggerType(
    override val subjectMode: SubjectMode = SubjectMode.Name,
    override val allowParameters: Boolean = false,
    override val parameters: Type? = null,
    override val allowReturns: Boolean = false,
    override val returns: Type? = null,
) : TriggerType {
    TEST,
    PROC(allowParameters = true, allowReturns = true),
    LABEL(allowParameters = true),
    ;

    override val id: Int = ordinal

    override val identifier: String get() = name.lowercase()
}
