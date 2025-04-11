package me.filby.neptune.runescript.compiler.symbol

import me.filby.neptune.runescript.compiler.type.PrimitiveType
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SymbolTableTest {
    @BeforeEach
    fun setupBasicTable() {
        root = SymbolTable()
    }

    @Test
    fun testSymbolTable() {
        val sub1 = root.createSubTable()
        val sub2 = root.createSubTable()

        // create symbol #1 and insert it into sub1 and verify second insertion fails
        val symbol1 = BasicSymbol("symbol", PrimitiveType.INT)
        assertTrue(sub1.insert(SymbolType.Basic(PrimitiveType.INT), symbol1))
        assertFalse(sub1.insert(SymbolType.Basic(PrimitiveType.INT), symbol1))

        // create symbol #2 and insert it into sub2 and verify second insertion fails
        val symbol2 = BasicSymbol("symbol", PrimitiveType.INT)
        assertTrue(sub2.insert(SymbolType.Basic(PrimitiveType.INT), symbol2))
        assertFalse(sub2.insert(SymbolType.Basic(PrimitiveType.INT), symbol2))

        // lookup the symbol from the first sub table and compare result with the two symbols that have been inserted
        val lookupSymbol1 = sub1.find(SymbolType.Basic(PrimitiveType.INT), "symbol")
        assertSame(symbol1, lookupSymbol1)
        assertNotSame(symbol2, lookupSymbol1)

        // create symbol #3 and insert it into root, this allows us to test symbol shadowing
        val symbol3 = BasicSymbol("symbol", PrimitiveType.INT)
        root.insert(SymbolType.Basic(PrimitiveType.INT), symbol3)
        assertSame(root.find(SymbolType.Basic(PrimitiveType.INT), "symbol"), symbol3)
        assertSame(sub1.find(SymbolType.Basic(PrimitiveType.INT), "symbol"), symbol1)
    }

    private companion object {
        lateinit var root: SymbolTable
    }
}
