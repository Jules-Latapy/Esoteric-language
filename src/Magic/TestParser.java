package Magic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestParser {

    @Test
    public void testNextSymbol() {
        assertEquals( Parser.nextSymbol("{';';}",";"),4,0);
        assertEquals( Parser.nextSymbol("{;;}",";"),1,0);
        assertEquals( Parser.nextSymbol("||",";"), -1,0);
        assertEquals( Parser.nextSymbol("",";"), -1,0);

        assertEquals( Parser.nextSymbol("{and}","and"), 1,0);
    }

    @Test
    public void testNextSymbolDetached() {
        assertEquals(Parser.nextSymboleDetached("rr;hello;rr", ";hello;"), 2, 0);
        assertEquals(Parser.nextSymboleDetached("rr;hello;rr", "hello;"), 3, 0);
        assertEquals(Parser.nextSymboleDetached("rr;hello;rr", ";hello"), 2, 0);
        assertEquals(Parser.nextSymboleDetached("rr;hello;rr", "hello"), 3, 0);
        assertEquals(Parser.nextSymboleDetached("rrhello;rr", "hello"), -1, 0);
        assertEquals(Parser.nextSymboleDetached("rrhellorr", "hello"), -1, 0);
        assertEquals(Parser.nextSymboleDetached("hello;rr", "hello"), 0, 0);
        assertEquals(Parser.nextSymboleDetached("rr;hello", "hello"), 3, 0);
        System.out.println(Parser.nextSymboleDetached("SCRIPTO![\"block3\"].SCRIPTO![\"hello, world\"]","."));
    }
}
