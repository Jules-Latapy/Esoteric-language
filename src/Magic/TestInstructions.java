package Magic;

import Magic.Instructions;
import org.junit.Test;

import java.util.Stack;

import static org.junit.Assert.assertEquals;

public class TestInstructions {

    @Test
    public void testDecoupe() {
        System.out.println(Instructions.Block.decoupe("SCRIPTO#[\"hello, world\"].SCRIPTO#[\"hello, world\"]. SCRIPTO#[\"hello, world\"].\n SCRIPTO#[\"hello, world\"]..hello","."));
    }
    @Test
    public void testOutterBlock() {
        String code =  """
                        SCRIPTO#["hello, world"].
                        FACERE
                            SCRIPTO#["hello, world"].
                            FACERE
                                SCRIPTO#["hello, world"].
                            COMPLEVIT.
                        COMPLEVIT.
                        SCRIPTO#["hello, world"].""";

        int[] beginEnd = Instructions.Block.getOutterBlock(code);
        assertEquals(code.substring(beginEnd[0],beginEnd[1]),
                 """
                        FACERE
                            SCRIPTO#["hello, world"].
                            FACERE
                                SCRIPTO#["hello, world"].
                            COMPLEVIT.
                        COMPLEVIT""");
    }

    @Test
    public void creationBlocTest() {
        String code = """
                FACERE
                    SCRIPTO#["block1"].
                    SI VERUM FACERE
                        SCRIPTO!["block2"].
                    COMPLEVIT.
                    FACERE
                        SCRIPTO!["block3"].SCRIPTO!["hello, world"].
                    COMPLEVIT.
                    SCRIPTO!["block1.2"]
                COMPLEVIT""";
        /*System.out.println(*/code=code.replaceAll("\\s+"," ").replaceAll("\\. ",".")/*)*/;
        System.out.println(new Instructions.Block().create(code,null).toString());
    }

    @Test
    public void testBloc() {
        String code = """
                FACERE
                    SI VERUM FACERE
                        IUSSUS u[NUMERUS NOMINE V] FACERE
                        COMPLEVIT.
                    COMPLEVIT.
                COMPLEVIT.""";
        System.out.println();
        Instructions.Block b = (Instructions.Block) new Instructions.Block().create(code,null);
        System.out.println(b);
        b.run(new Stack<>());
        System.out.println(b);
    }

    @Test
    public void testConst() {
        Instructions.Const c = (Instructions.Const) new Instructions.Const().create("INCOMMUTABILIS VERITAS NOMINE NAME VALET FICTUS",null);
        System.out.println(c.type+c.name+c.value);
    }

    @Test
    public void testVar() {
        Instructions.Var c = (Instructions.Var) new Instructions.Var().create("VERITAS NOMINE NAME VALET FICTUS",null);
        System.out.println(c.type+c.name+c.value);

        c = (Instructions.Var) new Instructions.Var().create("NOMINE NAME VALET FICTUS",null);
        System.out.println(c.type+c.name+c.value);

        c = (Instructions.Var) new Instructions.Var().create("NOMINE NAME",null);
        System.out.println(c.type+c.name+c.value);
    }

    @Test
    public void testIf() {
        String code = """
                FACERE
                    SI VERUM FACERE
                        
                    ALIUS.
                        
                    COMPLEVIT.
                COMPLEVIT.""";
        Instructions.Block b = (Instructions.Block) new Instructions.Block().create(code,null);
        b.run(new Stack<>());
        System.out.println(b);
    }

    @Test
    public void testSwitch() {
        String code = """
                FACERE
                    SECUNDUM 'E' FACERE
                        QUANDO 'e' FACERE COMPLEVIT.
                        QUANDO 'f' FACERE COMPLEVIT.
                        ALIUS.
                           
                    COMPLEVIT.
                COMPLEVIT.""";
        Instructions.Block b = (Instructions.Block) new Instructions.Block().create(code,null);
        b.run(new Stack<>());
        System.out.println(b);
    }

    @Test
    public void testWhile() {
        String code = """
                FACERE
                    QUANDIU FICTUS FACERE
                        RUMPITUR.
                        PROGREDI.
                    COMPLEVIT.
                COMPLEVIT.""";
        Instructions.Block b = (Instructions.Block) new Instructions.Block().create(code,null);
        b.run(new Stack<>());
        System.out.println(b);
    }

    @Test
    public void testFor() {
        String code = """
                FACERE
                    TRAICERE [I;II;III;IX] NOMINE u FACERE
                        RUMPITUR.
                    COMPLEVIT.
                COMPLEVIT.""";
        Instructions.Block b = (Instructions.Block) new Instructions.Block().create(code,null);
        b.run(new Stack<>());
        System.out.println(b);
    }

    @Test
    public void testProgram() {
        String code = """
                FACERE
                    IUSSUS namefct[NUMERUS NOMINE _] REDIT NUMERUS FACERE
                        REDIT X.
                    COMPLEVIT.
                COMPLEVIT.""";
        Instructions.Block b = (Instructions.Block) new Instructions.Block().create(code,null);

        Instructions.Block main = new Instructions.Block();
        main.block = b;

        Stack<Instructions.Instruction> s = new Stack<>();
        s.push(main);

        b.run(s);
        System.out.println(b);
    }
}
