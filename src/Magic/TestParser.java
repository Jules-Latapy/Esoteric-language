package Magic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestParser {

    @Test
    public void testNextSymbol() {
        assertEquals( Parser.findSymbol("{';';}",";"),4,0);
        assertEquals( Parser.findSymbol("{;;}",";"),1,0);
        assertEquals( Parser.findSymbol("||",";"), -1,0);
        assertEquals( Parser.findSymbol("",";"), -1,0);

        assertEquals( Parser.findSymbol("{and}","and"), 1,0);
    }

    @Test
    public void testNextSymbolDetached() {
        assertEquals(Parser.findSymboleDetached("rr;hello;rr", ";hello;"), 2, 0);
        assertEquals(Parser.findSymboleDetached("rr;hello;rr", "hello;"), 3, 0);
        assertEquals(Parser.findSymboleDetached("rr;hello;rr", ";hello"), 2, 0);
        assertEquals(Parser.findSymboleDetached("rr;hello;rr", "hello"), 3, 0);
        assertEquals(Parser.findSymboleDetached("rrhello;rr", "hello"), -1, 0);
        assertEquals(Parser.findSymboleDetached("rrhellorr", "hello"), -1, 0);
        assertEquals(Parser.findSymboleDetached("hello;rr", "hello"), 0, 0);
        assertEquals(Parser.findSymboleDetached("rr;hello", "hello"), 3, 0);
        System.out.println(Parser.findSymboleDetached("SCRIPTO![\"block3\"].SCRIPTO![\"hello, world\"]","."));
    }

    @Test
    public void testLastSymbol() {
        assertEquals(Parser.findLastSymbol("rr;hello;rr", ";"), 8, 0);
        assertEquals(Parser.findLastSymbol("rr;hello[;]rr", ";"), 2, 0);
        assertEquals(Parser.findLastSymbol("rr';hello;'rr", ";"), -1, 0);

        System.out.println(Parser.findLastSymbol("SCRIPTO![\"block3\"].SCRIPTO![\"hello, world\"].","."));
    }
}
