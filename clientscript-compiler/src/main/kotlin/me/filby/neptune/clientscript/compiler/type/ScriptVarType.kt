package me.filby.neptune.clientscript.compiler.type

import me.filby.neptune.runescript.compiler.type.BaseVarType
import me.filby.neptune.runescript.compiler.type.Type

enum class ScriptVarType(
    override val code: Char?,
    override val baseType: BaseVarType = BaseVarType.INTEGER,
    override val defaultValue: Any? = -1,
    representation: String? = null
) : Type {
    // INT
    // BOOLEAN
    SEQ('A'),
    LOC_SHAPE('H', representation = "locshape"),
    COMPONENT('I'),
    IDKIT('K'),
    MIDI('M'),
    NPC_MODE('N'),
    NAMEDOBJ('O'),
    SYNTH('P'),
    AREA('R'),
    STAT('S'),
    NPC_STAT('T'),
    MAPAREA('`', representation = "wma"),
    // COORDGRID
    GRAPHIC('d'),
    FONTMETRICS('f'),
    ENUM('g'),
    JINGLE('j'),
    LOC('l'),
    MODEL('m'),
    NPC('n'),
    OBJ('o'),
    PLAYER_UID('p'),
    // STRING
    SPOTANIM('t'),
    NPC_UID('u'),
    INV('v'),
    TEXTURE('x'),
    CATEGORY('y'),
    // CHAR
    MAPELEMENT('µ'),
    HITMARK('×'),
    STRUCT('J'),
    DBROW('Ð'),
    INTERFACE('a'),
    TOPLEVELINTERFACE('F'),
    OVERLAYINTERFACE('L'),
    MOVESPEED('Ý'),
    // LONG
    ENTITYOVERLAY('-'),
    DBTABLE(null),
    STRINGVECTOR('¸'),
    ;

    override val representation: String = representation ?: name.lowercase()
}
