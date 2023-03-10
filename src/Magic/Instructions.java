package Magic;

import java.lang.reflect.Modifier;
import java.util.*;

class Expression extends Instructions.Instruction {

    String info ;

    @Override
    public boolean isBegin(String s) {
        return true;
    }

    @Override
    public void run(Stack<Instructions.Instruction> stack) {
        Calculator.evaluate(info,stack);
    }

    @Override
    public Instructions.Instruction create(String s, Instructions.Block b) {
        Expression coc = new Expression();
        coc.info = s.strip();
        return coc;
    }

    @Override
    public String toString() {
        return "other->"+info;
    }
}

enum Signals{BREAK,CONTINUE,RETURN,EXCEPTION}

public abstract class Instructions {

    public static Set<Class<? extends Instruction>> BASE_INSTRUCTIONS = new LinkedHashSet<>();

    static {
        for (Class t: Instructions.class.getDeclaredClasses()){
            if (!Modifier.isAbstract(t.getModifiers()))
                BASE_INSTRUCTIONS.add(t);
        }
        //Pour s'assurer qu'elle est a la fin
        BASE_INSTRUCTIONS.add(Expression.class);
    }

    public static Instruction create(String s, Block b) {
        for (Class<? extends Instruction> c : BASE_INSTRUCTIONS) {
            Instruction t;
            try {
                t = (Instruction) c.getDeclaredConstructors()[0].newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (t.isBegin(s)) {
                return t.create(s,b);
            }
        }
        //jamais atteint
        return null;
    }

    public abstract static class Instruction {

        protected Block block ;

        public abstract boolean isBegin(@WithDelimiter String s) ;

        public abstract void run(Stack<Instruction> stack);

        public abstract Instruction create(@WithDelimiter String s, Block b);
    }

    public static class Block extends Instruction {

        private Signals signal = null ;
        public List<Instructions.Program> subProg = new ArrayList<>();

        public HashMap<String, Types.Type> variables = new HashMap<>();

        public HashMap<String, Types.Type> constantes = new HashMap<>();

        public ArrayList<Instruction> instructions = new ArrayList<>();

        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s.toUpperCase(), Keywords.BEGIN.val) == 0 ;
        }

        @Override
        public void run(Stack<Instruction> stack) {
            for (Instruction i : instructions) {
                if (signal!=null) break;
                i.run(stack);
            }
            signal = null;
        }

        public static List<String> decoupe(@WithoutDelimiter String s, String symbol) {
            ArrayList<String> result = new ArrayList<>();
            if (s==null) return result;
            while (Parser.findSymboleDetached(s,symbol)!=-1) {
                String tmp = s.substring(0, Parser.findSymboleDetached(s,symbol));
                result.add(tmp);
                s=s.substring(Parser.findSymboleDetached(s,symbol)+1);
            }
            if(!s.isEmpty())
                result.add(s);

            return result;
        }

        /**
         * fct qui donne le debut et la fin du premier bloc trouvé
         *
         *<pre>
         *     ⎧FACERE
         *     │    SCRIPTO#["hello, world"].
         *     │   ⎧FACERE
         *     │  2│    SCRIPTO#["hello, world"].
         *     │   ⎩COMPLEVIT.
         *     │
         *     │    SCRIPTO#["hello, world"].
         *    1│   ⎧FACERE
         *     │   │    SCRIPTO#["hello, world"].
         *     │   │   ⎧FACERE
         *     │  3│  4│    SCRIPTO#["hello, world"].
         *     │   │   ⎩COMPLEVIT.
         *     │   ⎩COMPLEVIT.
         *     │    SCRIPTO#["hello, world"].
         *     ⎩COMPLEVIT.
         *</pre>
         */
        public static int[] getOutterBlock(String s) {
            final int begin = Parser.findSymboleDetached(s,"FACERE");
            int end =-1;

            if (begin!=-1) {
                int cptBegin = 1;
                int beginTmp = begin+"FACERE".length();
                while(cptBegin != 0) {

                    String reduced = s.substring(beginTmp);
                    int indexBegin= Parser.findSymboleDetached(reduced, "FACERE"   )+"FACERE"   .length();
                    int indexEnd  = Parser.findSymboleDetached(reduced, "COMPLEVIT")+"COMPLEVIT".length();

                    if (indexEnd=="COMPLEVIT".length()-1) {
                        throw new RuntimeException("incomplete block");
                    }

                    if (indexBegin!="FACERE".length()-1 && indexBegin<indexEnd) {
                        beginTmp += Parser.findSymboleDetached(reduced, "FACERE"   )+"FACERE"   .length();
                        cptBegin++;
                        continue;
                    }

                    if (indexBegin=="FACERE".length()-1 || indexEnd<indexBegin) {
                        beginTmp+= Parser.findSymboleDetached(reduced, "COMPLEVIT")+"COMPLEVIT".length();
                        cptBegin--;
                    }
                }
                end=beginTmp;
            }

            return new int[]{begin,end};
        }


        private static Block createBlockPure(@WithoutDelimiter String s) {
            Block result = new Block();
            result.instructions = new ArrayList<>();

            for (String base : decoupe(s,".")) {
                if (!base.isBlank()) {
                    result.instructions.add(Instructions.create(base,null));
                }
            }
            result.block=result;
            return  result;
        }

        public Instruction create(String s, Block __) {
            s = s.substring(Keywords.BEGIN.val.length(), s.length()- Keywords.END.val.length()-1);

            int[] beginEnd = getOutterBlock(s);

            if (beginEnd[0]==-1) {
                return createBlockPure(s);
            }

            Block result = new Block() ;
            result.instructions = new ArrayList<>();

            while (beginEnd[0] != -1) {

                String before = s.substring(0,beginEnd[0]);

                int lastDotBeforBlock = Parser.findLastSymbol(before, ".")+1;

                String lastInstrBeforeBlock = s.substring(lastDotBeforBlock, beginEnd[0]);

                if (!lastInstrBeforeBlock.isBlank())
                    before = before.substring(0,lastDotBeforBlock);

                for (String instr : decoupe(before,"."))
                    if (!instr.isBlank())
                        result.instructions.add(Instructions.create(instr, null));

                if (lastInstrBeforeBlock.isBlank())
                    result.instructions.add(create(s.substring(beginEnd[0], beginEnd[1]), null));//block seule
                else
                    result.instructions.add(Instructions.create(lastInstrBeforeBlock, (Block) create(s.substring(beginEnd[0], beginEnd[1]), null)));


                s=s.substring(beginEnd[1]);
                beginEnd = getOutterBlock(s);
            }

            String after = s.substring(beginEnd[1]+1);;
            for (String instr : decoupe(after,".")) {
                if (!instr.isBlank())
                    result.instructions.add(Instructions.create(instr, null));
            }

            result.block=result;
            return result;
        }

        @Override
        public String toString() {
            return instructions.toString();
        }
    }

    public static class Const extends Instruction {
        public String type ;
        public String value;

        public String name ;
        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s.toUpperCase(), "INCOMMUTABILIS") == 0 ;
        }

        @Override
        public void run(Stack<Instruction> stack) {
            //ajouter la variable au dernier programme crée
            stack.peek().block.constantes.put(name,Calculator.evaluate(value, stack));
        }

        /**
         *                [Type]       [nom]      [valeur]
         * INCOMMUTABILIS ALBUM NOMINE NAME VALET [I,II,III,IX].
         */
        @Override
        public Instruction create(String s, Block __) {
            int begin = Parser.findSymboleDetached(s.toUpperCase(), Keywords.CONST.val);
            s = s.substring(begin+ Keywords.CONST.val.length());
            /*------------------------------*/
            //puisqu'une constante a forcement une valeur
            int typeEnd = Parser.findSymboleDetached(s.toUpperCase(), Keywords.NAME.val);
            int nameEnd = Parser.findSymboleDetached(s.toUpperCase(), Keywords.VALUE.val);

            Const result = new Const();
            result.type = s.substring(0, typeEnd).strip();
            result.name = s.substring(typeEnd+ Keywords.NAME.val.length(),nameEnd).strip();
            result.value = s.substring(nameEnd+ Keywords.VALUE.val.length()).strip();
            return result;
        }

        @Override
        public String toString() {
            return type+":"+name+"="+value;
        }
    }

    public static class Var extends Instruction {
        public String type ;
        public String value = "";
        public String name ;

        @Override
        public boolean isBegin(String s) {
            s=s.strip();

            for (Class<Types.Type> t : Types.BASE_TYPE) {

                if (Parser.findSymboleDetached(s.toUpperCase(), t.getSimpleName().toUpperCase()) == 0)
                    return true;
            }
            //pour les variables sans types
            return Parser.findSymboleDetached(s.toUpperCase(), "NOMINE") == 0;
        }

        @Override
        public void run(Stack<Instruction> stack) {

            if (value != null ) {
                Types.Type calculatedValue = Calculator.evaluate(value, stack);

                if (!calculatedValue.getClass().getSimpleName().equalsIgnoreCase(type))
                    throw new RuntimeException("incompatible type, declared: "+type+" given: "+calculatedValue.getClass().getSimpleName());

                stack.peek().block.variables.put(Parser.clean(name), calculatedValue);

                return;
            }
            stack.peek().block.variables.put(Parser.clean(name), null);
        }

        /*
         * [Type]       [nom]      [valeur]
         *
         * ALBUM NOMINE NAME VALET [I,II,III,IX].
         *       NOMINE NAME VALET [I,II,III,IX].
         *       NOMINE NAME
         *
         * /!\ affectation
         *              NAME VALET [I,II,III,IX].
         */
        @Override
        public Instruction create(String s, Block __) {
            Var result = new Var();

            int endType = Parser.findSymboleDetached(s.toUpperCase(), Keywords.NAME.val) ;
            int endNom  = Parser.findSymboleDetached(s.toUpperCase(), Keywords.VALUE.val) ;
            //si il a na pas de type
            if (s.substring(0, endType).isBlank()) {

                //Si il n'y a pas de type et pas de valeur
                if (endNom!=-1) {
                    result.type = null;
                    result.name = s.substring(endType+ Keywords.NAME.val.length(), endNom).strip();
                    result.value= s.substring(endNom+ Keywords.VALUE.val.length()).strip();
                }
                else {
                    result.type = null;
                    result.name = s.substring(endType+ Keywords.NAME.val.length()).strip();
                    result.value= null;
                }
            }
            else {
                //si il a un type et une valeur
                if (endNom!=-1) {
                    result.type = s.substring(0, endType).strip();
                    result.name = s.substring(endType+ Keywords.NAME.val.length(), endNom).strip();
                    result.value= s.substring(endNom+ Keywords.VALUE.val.length()).strip();
                } else {
                    result.type = s.substring(0, endType).strip();
                    result.name = s.substring(endType+ Keywords.NAME.val.length()).strip();
                    result.value= null ;
                }
            }
            return result;
        }

        @Override
        public String toString() {
            return type+":"+name+"="+value;
        }
    }

    public static class If extends Instruction {

        public String condition;
        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s, "SI") == 0 ;
        }

        @Override
        public void run(Stack<Instruction> stack) {
            stack.add(this);
            if(((Types.Veritas) Calculator.evaluate(condition, stack)).value) {
                for (Instruction i : block.instructions) {
                    if (i instanceof Else || block.signal!=null ) break;
                    i.run(stack);
                }
            }
            else {
                for (Instruction i :block.instructions.stream().dropWhile(i -> !(i instanceof Else)).skip(1).toList()) {
                    if (block.signal!=null ) break;
                    i.run(stack);
                }
            }
            stack.pop();
        }

        @Override
        public Instruction create(String s, Block b) {
            If si = new If();
            si.block = b;
            si.condition = s.substring(Parser.findSymboleDetached(s.toUpperCase(), Keywords.IF.val)+ Keywords.IF.val.length());
            return si;
        }

        @Override
        public String toString() {
            return "if "+condition+block;
        }
    }

    public static class Else extends Instruction {

        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s.toUpperCase(), Keywords.ELSE.val) == 0 ;
        }

        @Override
        public void run(Stack<Instruction> stack) {
            throw new RuntimeException("Else statement at wrong place");
        }

        @Override
        public Instruction create(String s, Block __) {
            return new Else();
        }

        @Override
        public String toString() {
            return "] Else [";
        }
    }

    public static class Switch extends Instruction {

        private String value ;
        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s.toUpperCase(), Keywords.SWITCH.val) == 0 ;
        }

        @Override
        public void run(Stack<Instruction> stack) {
            stack.add(this);
            boolean defaut = false;
            Types.Type thingToCompare = Calculator.evaluate(value, stack);
            for (Instruction i : block.instructions) {
                if (!defaut) {
                    if (i instanceof Case cas) {
                        if (thingToCompare.equals(Calculator.evaluate(cas.value, stack))) {
                            cas.run(stack);
                            break;
                        }
                    } else if (i instanceof Else) {
                        defaut = true;
                    }
                }
                else
                    i.run(stack);

                if (block.signal!=null) break;
            }
            stack.remove(this);
        }

        @Override
        public Instruction create(String s, Block b) {
            Switch si = new Switch();
            si.block = b;
            si.value = s.substring(Parser.findSymboleDetached(s.toUpperCase(), Keywords.SWITCH.val)+ Keywords.SWITCH.val.length());
            return si;
        }

        @Override
        public String toString() {
            return "switch "+value+block;
        }
    }

    public static class Case extends Instruction {

        public String value;

        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s.toUpperCase(), Keywords.CASE.val) == 0 ;
        }

        @Override
        public void run(Stack<Instruction> stack) {
            stack.add(this);
            block.run(stack);
            stack.remove(this);
        }

        @Override
        public Instruction create(String s, Block b) {
            Case si = new Case();
            si.block = b;
            si.value = s.substring(Parser.findSymboleDetached(s.toUpperCase(), Keywords.CASE.val)+ Keywords.CASE.val.length());
            return si;
        }

        @Override
        public String toString() {
            return "case "+value+block;
        }
    }

    public static class Break extends Instruction {

        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s.toUpperCase(), Keywords.BREAK.val) == 0 ;
        }

        @Override
        public void run(Stack<Instruction> stack) {
            Stack<Instructions.Instruction> stack2 = (Stack<Instructions.Instruction>) stack.clone();
            Collections.reverse(stack2);

            for(Instruction i : stack2) {
                i.block.signal = Signals.BREAK;

                if (i instanceof Instructions.While || i instanceof For) {
                    break;
                }
            }
        }

        @Override
        public Instruction create(String s, Block __) {
            return new Break();
        }

        @Override
        public String toString() {
            return "break";
        }
    }

    public static class Continue extends Instruction {

        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s.toUpperCase(), Keywords.CONTINUE.val) == 0 ;
        }

        @Override
        public void run(Stack<Instruction> stack) {
            ArrayList<Instructions.Instruction> stack2 = (ArrayList<Instructions.Instruction>) stack.clone();
            Collections.reverse(stack2);

            for(Instruction i : stack2) {
                i.block.signal = Signals.CONTINUE;

                if (i instanceof Instructions.While || i instanceof For) {
                    break;
                }
            }
        }

        @Override
        public Instruction create(String s, Block __) {
            return new Continue();
        }

        @Override
        public String toString() {
            return "continue";
        }
    }

    public static class Return extends Instruction {

        private String value;
        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s.toUpperCase(), Keywords.RETURN.val) == 0 ;
        }

        @Override
        public void run(Stack<Instruction> stack) {
            Stack<Instructions.Instruction> stack2 = (Stack<Instructions.Instruction>) stack.clone();
            Collections.reverse(stack2);

            for(Instruction i : stack2) {

                i.block.signal=Signals.RETURN;

                if (i instanceof Instructions.Program p) {
                    p.retour = (Calculator.evaluate(value, stack));
                    return;
                }
            }
        }

        @Override
        public Instruction create(String s, Block b) {
            Return result = new Return();
            String value = s.substring(Parser.findSymboleDetached(s, Keywords.RETURN.val)+ Keywords.RETURN.val.length());

            if (value.isBlank())
                value = null;

            result.value = value;
            return result;
        }

        @Override
        public String toString() {
            return "return"+value;
        }
    }

    public static class While extends Instruction {

        public String value ;

        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s.toUpperCase(), Keywords.WHILE.val) == 0 ;
        }

        @Override
        public void run(Stack<Instruction> stack) {
            stack.add(this);
            while (((Types.Veritas)Calculator.evaluate(value, stack)).value) {
                for (Instruction i : block.instructions) {
                    i.run(stack);
                    if (block.signal!= null) {
                        if (block.signal==Signals.CONTINUE) break;
                        else {
                            stack.remove(this);
                            return;
                        }
                    }
                }
            }
            stack.remove(this);
        }

        @Override
        public Instruction create(String s, Block b) {
            While result = new While();
            result.block = b;
            result.value = s.substring(Parser.findSymboleDetached(s.toUpperCase(), Keywords.WHILE.val)+ Keywords.WHILE.val.length());
            return result;
        }

        @Override
        public String toString() {
            return "while "+value+block;
        }
    }

    public static class For extends Instruction {

        private String list ;
        private String nomVar;

        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s.toUpperCase(), Keywords.FOR.val) == 0 ;
        }

        @Override
        public void run(Stack<Instruction> stack) {
            stack.add(this);
            for(Types.Type t : ((Types.Album)Calculator.evaluate(list, stack)).value) {
                block.variables.put(Parser.clean(nomVar),t);
                for (Instruction i : block.instructions) {
                    i.run(stack);
                    if (block.signal!= null) {
                        if (block.signal==Signals.CONTINUE) break;
                        else {
                            stack.remove(this);
                            return;
                        }
                    }
                }
            }
            block.variables.remove(Parser.clean(nomVar));
            stack.remove(this);
        }

        @Override
        public Instruction create(String s, Block b) {
            For result = new For();
            int listBegin = Parser.findSymboleDetached(s.toUpperCase(), Keywords.FOR.val)+ Keywords.FOR.val.length();
            int listEnd   = Parser.findSymboleDetached(s.toUpperCase(), Keywords.NAME.val);
            result.block = b;
            result.list = s.substring(listBegin,listEnd);
            result.nomVar = s.substring(listEnd+ Keywords.NAME.val.length());
            return result;
        }

        @Override
        public String toString() {
            return "for "+nomVar+" in "+list +" do "+ block;
        }
    }

    /*
     * un programme connait tout les programme defini interieurement, au meme niveau ainsi que les programme parent
     */
    public static class Program extends Instruction {

        public String name;

        public TreeMap<String,String> paramInfo = new TreeMap<>();

        private String param;

        public Types.Type retour = null;

        @Override
        public boolean isBegin(String s) {
            s=s.strip();
            return Parser.findSymboleDetached(s.toUpperCase(), Keywords.PROGRAM.val) == 0 ;
        }

        /*
         * ne pas confondre avec un appel
         */
        @Override
        public void run(Stack<Instruction> stack) {
            stack.push(this);
            for (String s: Block.decoupe(param,";")) {
                Const c = new Const();
                if (c.isBegin(s)) {
                    c = (Const) c.create(s,null);
                    c.run(stack);
                    paramInfo.put(c.name,c.type.toUpperCase());
                }
                else {
                    Var v = new Var();
                    v = (Var) v.create(s,null);
                    v.run(stack);
                    paramInfo.put(v.name,v.type.toUpperCase());
                }
            }
            stack.pop();
            stack.peek().block.subProg.add(this);
        }

        /*
         * IUSSUS namefct[NOMINE param] REDIT NUMERUS
         * IUSSUS namefct[NOMINE param]
         * IUSSUS namefct
         */
        @Override
        public Instruction create(String s, Block b) {
            Program result = new Program();

            s = s.substring(Parser.findSymboleDetached(s.toUpperCase(), Keywords.PROGRAM.val)+ Keywords.PROGRAM.val.length());

            int paramBeg = s.indexOf("[");
            int typeBeg  = Parser.findSymboleDetached(s, Keywords.RETURN.val);
            int paramEnd = typeBeg ==-1?s.length():typeBeg;

            if (paramBeg != -1) {
                result.name = Parser.clean(s.substring(0,paramBeg));
                result.param = s.substring(paramBeg+1, paramEnd).strip();
                result.param = result.param.substring(0,result.param.length()-1);
            }
            else {
                if (typeBeg != -1) {
                    result.name = Parser.clean(s.substring(0,typeBeg));
                }
                else {
                    result.name = Parser.clean(s);
                }
            }

            if (typeBeg != -1)
                for (Class<Types.Type> t : Types.BASE_TYPE)
                    if (t.getSimpleName().equalsIgnoreCase(s.substring(typeBeg+ Keywords.RETURN.val.length())))
                        try {
                            result.retour = (Types.Type) t.getDeclaredConstructors()[0].newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

            result.block = b;
            return result;
        }

        @Override
        public String toString() {
            return name+paramInfo;
        }
    }
}
