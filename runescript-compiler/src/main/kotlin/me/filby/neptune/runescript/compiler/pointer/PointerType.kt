package me.filby.neptune.runescript.compiler.pointer

public enum class PointerType(public val representation: String) {
    ACTIVE_PLAYER("active_player"),
    ACTIVE_PLAYER2(".active_player"),
    P_ACTIVE_PLAYER("p_active_player"),
    P_ACTIVE_PLAYER2(".p_active_player"),
    ACTIVE_NPC("active_npc"),
    ACTIVE_NPC2(".active_npc"),
    ACTIVE_LOC("active_loc"),
    ACTIVE_LOC2(".active_loc"),
    ACTIVE_OBJ("active_obj"),
    ACTIVE_OBJ2(".active_obj"),
    ACTIVE_COMPONENT("active_component"),
    ACTIVE_COMPONENT2(".active_component"),
    ACTIVE_CLANSETTINGS("active_clansettings"),
    ACTIVE_CLANCHANNEL("active_clanchannel"),
    ACTIVE_CLANPROFILE("active_clanprofile"),
    ACTIVE_TILE("active_tile"),
    FIND_HUNT("find_hunt"),
    ;

    public companion object {
        private val NAME_TO_TYPE = PointerType.values().associateBy { it.name.lowercase() }

        public fun forName(name: String): PointerType? = NAME_TO_TYPE[name]
    }
}
