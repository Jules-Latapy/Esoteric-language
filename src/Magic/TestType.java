package Magic;

import Magic.Types;
import org.junit.Test;

import java.util.Stack;

import static Magic.Calculator.evaluate;
import static org.junit.Assert.*;
public class TestType {

    @Test
    public void testNumerous() {
        Types.Numerus n = new Types.Numerus().create("NIHIL,NIHILNIHILNIHILX");
        assertEquals(0.0001, Types.Numerus.toDouble(n), 0.0);

        n = new Types.Numerus().create(",C");
        assertEquals(0.1, Types.Numerus.toDouble(n), 0.0);

        n = new Types.Numerus().create("CC,");
        assertEquals(200, Types.Numerus.toDouble(n), 0.0);

        n = new Types.Numerus().create("C");
        assertEquals(100, Types.Numerus.toDouble(n), 0.0);

        n = new Types.Numerus().create("XI,");
        assertEquals(11, Types.Numerus.toDouble(n), 0.0);

        n = new Types.Numerus().create(",NIHILX");
        assertEquals(0.01, Types.Numerus.toDouble(n), 0.0);

        n = new Types.Numerus().create(",M");
        assertEquals(0.1, Types.Numerus.toDouble(n), 0.0);

        n = new Types.Numerus().create("-,M");
        assertEquals(-0.1, Types.Numerus.toDouble(n), 0.0);

        assertTrue(n.isType("NIHILI"));
    }

    @Test
    public void testAlbum() {
        Types.Album a = new Types.Album();
        assertEquals("[II,; I,]", a.create("[II; I]").toString());
        assertEquals("[II,]"    , a.create("[II]")   .toString());
        assertEquals("[]"       , a.create("[]")     .toString());

        assertEquals("['i'; 'o']"    , a.create("['i';  'o']").toString());
        assertEquals("[\"i\"; \"o\"]", a.create("[\"i\"; \"o\"]").toString());
        assertEquals("['i'; \"o\"]"    , a.create("['i';  \"o\"]").toString());

        assertFalse(a.isType("[][]"));
        assertTrue (a.isType("[[]]"));
        System.out.println(a.create("[[[II];II];[I;II]]").toString());
    }

    @Test
    public void testToNumerus() {
        assertEquals(Types.Numerus.toNumerus(.01).toString(),",NIHILI");
        assertEquals(Types.Numerus.toNumerus(150.01).toString(),"CL,NIHILI");
        System.out.println(Types.Numerus.toNumerus(.1));
    }

    @Test
    public void testToArrayAccess() {
        System.out.println(evaluate("[C;L;M][I]",new Stack<>()));

        String code = """
                      ALBUM NOMINE A VALET [I;II;III;IX].
                      SCRIPTO![A[I]].
                      SCRIPTO![[C;L;M][II]].
                      SCRIPTO![[C;L;M][II MINUS I]].
                      SCRIPTO![[C;L;M][I;II][NIHIL]].""";
        new Parser(code).run();
    }
}
