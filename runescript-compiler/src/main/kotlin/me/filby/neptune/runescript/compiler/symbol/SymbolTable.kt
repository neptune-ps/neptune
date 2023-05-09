package me.filby.neptune.runescript.compiler.symbol

import com.google.common.collect.HashBasedTable
import kotlin.reflect.KClass

/**
 * A table that contains [Symbol]s. The table provides helper functions for inserting and looking up symbols.
 *
 * The table may or may not have a [parent]. To create a symbol table with a `parent`, see [createSubTable].
 *
 * See Also: [Symbol table](https://en.wikipedia.org/wiki/Symbol_table)
 * @see createSubTable
 */
public class SymbolTable private constructor(private val parent: SymbolTable? = null) {
    /**
     * Table of all symbols defined in the table. This does not include symbols
     * defined in a parent.
     */
    private val symbols: HashBasedTable<SymbolType<*>, String, Symbol> = HashBasedTable.create()

    // default constructor that just defines null parent
    public constructor() : this(null)

    /**
     * Inserts [symbol] into the table and indicates if the insertion was
     * successful.
     */
    public fun <T : Symbol> insert(type: SymbolType<T>, symbol: T): Boolean {
        if (symbols.contains(type, symbol.name)) {
            return false
        }
        symbols.put(type, symbol.name, symbol)
        return true
    }

    /**
     * Searches for a symbol with [name] and [type]. If one is not found the
     * search it applied to the parent table recursively.
     */
    public fun <T : Symbol> find(type: SymbolType<T>, name: String): T? {
        @Suppress("UNCHECKED_CAST")
        var symbol = symbols.get(type, name) as? T
        if (symbol == null && parent != null) {
            symbol = parent.find(type, name)
        }
        return symbol
    }

    /**
     * Searches for all symbols in the table and all parent tables with the name of [name],
     * and optionally a [type].
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Symbol> findAll(name: String, type: KClass<T>): Sequence<T> = sequence {
        for (symbol in symbols.column(name).values) {
            if (type.isInstance(symbol)) {
                yield(symbol as T)
            }
        }
        if (parent != null) {
            yieldAll(parent.findAll(name, type))
        }
    }

    /**
     * Searches for all symbols in the table and all parent tables with the name of [name],
     * and optionally a type of [T].
     */
    public inline fun <reified T : Symbol> findAll(name: String): Sequence<T> {
        return findAll(name, T::class)
    }

    /**
     * Creates a new [SymbolType] with `this` as the parent.
     */
    public fun createSubTable(): SymbolTable {
        return SymbolTable(this)
    }
}
