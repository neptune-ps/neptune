package me.filby.neptune.runescript.compiler.symbol

import me.filby.neptune.runescript.compiler.type.PrimitiveType
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertFalse

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
        val symbol1 = ConfigSymbol("symbol", PrimitiveType.INT)
        assert(sub1.insert(SymbolType.Config(PrimitiveType.INT), symbol1))
        assertFalse(sub1.insert(SymbolType.Config(PrimitiveType.INT), symbol1))

        // create symbol #2 and insert it into sub2 and verify second insertion fails
        val symbol2 = ConfigSymbol("symbol", PrimitiveType.INT)
        assert(sub2.insert(SymbolType.Config(PrimitiveType.INT), symbol2))
        assertFalse(sub2.insert(SymbolType.Config(PrimitiveType.INT), symbol2))

        // lookup the symbol from the first sub table and compare result with the two symbols that have been inserted
        val lookupSymbol1 = sub1.find(SymbolType.Config(PrimitiveType.INT), "symbol")
        assert(symbol1 === lookupSymbol1)
        assert(symbol2 !== lookupSymbol1)

        // create symbol #3 and insert it into root, this allows us to test symbol shadowing
        val symbol3 = ConfigSymbol("symbol", PrimitiveType.INT)
        root.insert(SymbolType.Config(PrimitiveType.INT), symbol3)
        assert(root.find(SymbolType.Config(PrimitiveType.INT), "symbol") === symbol3)
        assert(sub1.find(SymbolType.Config(PrimitiveType.INT), "symbol") === symbol1)
    }

    private companion object {
        lateinit var root: SymbolTable
    }
}
