package Magic;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static Magic.Instructions.Block.decoupe;
import static Magic.Parser.clean;
import static Magic.Types.*;
import static Magic.Types.Type.createType;

public class Calculator {

    public static final String regexForPure = "\\s+|FICTUS|VERUM|NIHIL|[-\\+,IVXLCDM]+|;|('.')|(\".*\")|\\[|\\]";

    /*------------------*/
    /* BOOLEAN OPERATOR */
    /*------------------*/

    @Priority(1)
    public static Veritas ET     (@PreCalculate Veritas v1, @PreCalculate Veritas v2) {Veritas result = new Veritas(); result.value = v1.value && v2.value; return result;}
    @Priority(2)
    public static Veritas OR     (@PreCalculate Veritas v1, @PreCalculate Veritas v2) {Veritas result = new Veritas(); result.value = v1.value || v2.value; return result;}
    @Priority(0)
    public static Veritas NOT    (@PreCalculate Veritas v)                            {Veritas result = new Veritas(); result.value = !v.value; return result;}
    @Priority(3)
    public static Veritas EST    (@PreCalculate Type    n1, @PreCalculate Type n2)    {Veritas result = new Veritas(); result.value = n1.equals(n2); return result;}
    @Priority(3)
    public static Veritas INF    (@PreCalculate Numerus n1, @PreCalculate Numerus n2) {Veritas result = new Veritas(); result.value = Numerus.toDouble(n1) < Numerus.toDouble(n2); return result;}
    @Priority(3)
    public static Veritas SUP    (@PreCalculate Numerus n1, @PreCalculate Numerus n2) {Veritas result = new Veritas(); result.value = Numerus.toDouble(n1) > Numerus.toDouble(n2); return result;}
    @Priority(3)
    public static Veritas SUP_EST(@PreCalculate Numerus n1, @PreCalculate Numerus n2) {Veritas result = new Veritas(); result.value = Numerus.toDouble(n1) >= Numerus.toDouble(n2); return result;}
    @Priority(3)
    public static Veritas INF_EST(@PreCalculate Numerus n1, @PreCalculate Numerus n2) {Veritas result = new Veritas(); result.value = Numerus.toDouble(n1) <= Numerus.toDouble(n2); return result;}

    /*---------------------*/
    /* ARITHMETIC OPERATOR */
    /*---------------------*/

    @Priority(2)
    public static Numerus PLUS (@PreCalculate Numerus n) {return n;}
    @Priority(2)
    public static Numerus MINUS(@PreCalculate Numerus n) {Numerus result = n.copy();result.negativ=!result.negativ; return result; }
    @Priority(2)
    public static Numerus PLUS (@PreCalculate Numerus n1, @PreCalculate Numerus n2){return Numerus.toNumerus(Numerus.toDouble(n1) + Numerus.toDouble(n2));}
    @Priority(3)
    public static Numerus MINUS(@PreCalculate Numerus n1, @PreCalculate Numerus n2){return Numerus.toNumerus(Numerus.toDouble(n1) - Numerus.toDouble(n2));}
    @Priority(0)
    public static Numerus TIME (@PreCalculate Numerus n1, @PreCalculate Numerus n2){return Numerus.toNumerus(Numerus.toDouble(n1) * Numerus.toDouble(n2));}
    @Priority(1)
    public static Numerus DIV  (@PreCalculate Numerus n1, @PreCalculate Numerus n2){return Numerus.toNumerus(Numerus.toDouble(n1) / Numerus.toDouble(n2));}
    @Priority(0)
    public static Numerus POT  (@PreCalculate Numerus n1, @PreCalculate Numerus n2){return Numerus.toNumerus( Math.pow ( Numerus.toDouble(n1), Numerus.toDouble(n2)));}
    @Priority(0)
    public static Type    VALET(@PreCalculate Type affected, @PreCalculate Type value, String name, Stack<Instructions.Instruction> stack) {
        if (!isCst(name,stack)) {
            affected.value = value.value;
        }else {
            throw new RuntimeException("cannot assign const");
        }
        return affected;
    }

    public static List<Method> operators = Arrays.stream(Calculator.class.getDeclaredMethods())
                                            .filter(
                                                    m-> Modifier.isPublic(m.getModifiers())
                                                        && m.isAnnotationPresent(Priority.class))
                                            .sorted(Comparator.comparingInt(m ->-((Priority) m.getAnnotations()[0]).value())).toList();

    public static Types.Type evaluate(@WithDelimiter String s, Stack<Instructions.Instruction> stack) {
        s=s.strip();
        if (s==null || s.isBlank()) return null;

        /*---------------------*/
        /*        ALBUM        */
        /*---------------------*/

        if (new Album().isType(s)) {
            Album result = new Album();
            result.value = new ArrayList<>();
            for (String s1 : decoupe(s.substring(1,s.length()-1),";")) {
                result.value.add(evaluate(s1,stack));
            }
            return result;
        }

        /*------------------------*/
        /*        OPERATOR        */
        /*------------------------*/

        for (Method m : operators) {
            int index = Parser.findSymboleDetached(s, m.getName()) ;
            if (index!=-1) {
                try {

                    if (m.getParameterCount() == 2 && !s.substring(0,index).isBlank()) {
                        return (Types.Type) m.invoke(null, evaluate(s.substring(0, index), stack), evaluate(s.substring(index + m.getName().length()), stack));
                    }
                    if (m.getParameterCount() == 1 && s.substring(0,index).isBlank()) {
                        return (Types.Type) m.invoke(null, evaluate(s.substring(index + m.getName().length()), stack));
                    }

                    //uniquement pour valet
                    if (m.getParameterCount() == 4 && !s.substring(0,index).isBlank()) {
                        return (Types.Type) m.invoke(null, evaluate(s.substring(0, index), stack), evaluate(s.substring(index + m.getName().length()), stack), s.substring(0, index), stack);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /*-----------------------------*/
        /*        FUNCTION CALL        */
        /*-----------------------------*/

        int indexBang = Parser.findSymbol(s,"!");

        if (indexBang!=-1) {
            Types.Type e = evaluate(s.substring(indexBang+1),stack);
            if (e == null) {
                return execute(s.substring(0,indexBang), new ArrayList<>(), stack);
            }
            return execute(s.substring(0,indexBang), ((Album)e).value, stack);
        }

        /*----------------------------*/
        /*        ARRAY ACCESS        */
        /*----------------------------*/
        //pas d'operator, par de tableau(si unique dans la chaine), pas de fonction
        int indexAccess=indexAccess(s);
        if (indexAccess!=-1) {
            Album tab = (Album)evaluate(s.substring(0,indexAccess),stack);
            Types.Type tabOrNum = evaluate(s.substring(indexAccess),stack);

            if (tabOrNum instanceof Numerus index )
                return tab.value.get((int)Numerus.toDouble(index));

            if (tabOrNum instanceof Album index && index.value.size()>1)
                throw new RuntimeException("multiple index");

            Numerus index = (Numerus) ((Album)tabOrNum).value.get(0);

            return tab.value.get((int)Numerus.toDouble(index));
        }

        /*--------------------------*/
        /*        ONLY VALUE        */
        /*--------------------------*/

        //par definition valeur unique
        if (s.replaceAll(regexForPure,"").isEmpty()) {
            return createType(s);
        }

        /*------------------*/
        /*     VAR NAME     */
        /*------------------*/

        return resolve(clean(s),stack);
    }

    private static int indexAccess(String s) {
        s=s.strip();
        if (s==null) return -1 ;
        if (s.isEmpty()) return -1 ;
        if (!s.matches(".*\\[.*\\].*")) return -1;

        if (s.matches("[^\\[]+\\[.+")) return s.indexOf("[");

        Stack<Parser.LitteralType> context = new Stack<>();

        /*--------------*/
        /*  [C;V;L][I]  */
        /*--------------*/

        boolean alreadyBegun = false;
        for (int i = 0; i < s.length(); i++) {
            if (context.isEmpty()) {
                if (s.charAt(i) == '"') {
                    context.push(Parser.LitteralType.str);
                    continue;
                }
                if (s.charAt(i) == '\'') {
                    context.push(Parser.LitteralType.chr);
                    continue;
                }
                if (s.charAt(i) == '[') {
                    //si c'est vide une 2eme fois
                    if (alreadyBegun) return i;
                    context.push(Parser.LitteralType.tab);
                    alreadyBegun=true;
                    continue;
                }
            }

            if (s.charAt(i) == '[') {
                context.push(Parser.LitteralType.tab);
                continue;
            }

            if (!context.isEmpty() && context.peek() == Parser.LitteralType.str && s.charAt(i) == '"') {
                context.pop();
            }
            if (!context.isEmpty() && context.peek() == Parser.LitteralType.chr && s.charAt(i) == '\'') {
                context.pop();
            }
            if (!context.isEmpty() && context.peek() == Parser.LitteralType.tab && s.charAt(i) == ']') {
                context.pop();
            }
        }
        return -1;
    }

    private static Type execute(String name,@PreCalculate List<Type> params, Stack<Instructions.Instruction> stack) {
        Instructions.Program p = resolve(name, params, stack);
        if (p==null) {
            throw new RuntimeException("cannot resolve function:"+name+"!"+params);
        }
        stack.push(p);
        if (params!=null)
            for (int i =0; i<params.size(); i++) {
                Type value = params.get(i);
                String nameParam = p.paramInfo.keySet().stream().toList().get(i);

                if (p.block.variables.containsKey(nameParam)) {
                    p.block.variables.put(nameParam,value);
                    continue;
                }
                if (p.block.constantes.containsKey(nameParam)) {
                    p.block.constantes.put(nameParam,value);;
                }
            }

        p.block.run(stack);
        stack.pop();
        return p.retour ;
    }

    private static Instructions.Program resolve(String name, List<Type> params, Stack<Instructions.Instruction> stack) {
        for (Instructions.Instruction i : stack) {
            if (i instanceof Instructions.Program p) {
                if(p.name.equals(name) && compareCollection(params.stream().map(e -> e.getClass().getSimpleName()).toList(), p.paramInfo.values().stream().toList())) {
                    return p;
                }
            }

            //programme au meme niveau
            for (Instructions.Program p : i.block.subProg) {
                if(p.name.equals(name) && compareCollection(params.stream().map(e -> e.getClass().getSimpleName().toUpperCase()).toList(), p.paramInfo.values().stream().toList())) {
                    return p;
                }
            }
        }

        return null;
    }

    public static boolean compareCollection(List l1, List l2) {
        Iterator targetIt = l1.iterator();
        for (Object obj:l2)
            if (targetIt.hasNext()) {
                if (!obj.equals(targetIt.next()))
                    return false;
            }
            else
                return false;

        return true;
    }

    private static Types.Type resolve(String name, Stack<Instructions.Instruction> stack) {
        for (Instructions.Instruction i : stack) {
            Types.Type t =  i.block.variables.get(clean(name));
            if(t != null) {
                return t;
            }

            t =  i.block.constantes.get(clean(name));
            if(t != null) {
                return t;
            }
        }
        return null;
    }

    private static boolean isCst(String name, Stack<Instructions.Instruction> stack) {
        for (Instructions.Instruction i : stack) {
            Types.Type t =  i.block.variables.get(clean(name));
            if(t != null) {
                return false;
            }

            t =  i.block.constantes.get(clean(name));
            if(t != null) {
                return true;
            }
        }
        return false;
    }

    private static int[] getInnerBlock(String s) {

        int begin = Parser.findSymbol(s,"(");
        int end  = -1;

        if (begin!=-1) {
            while(Parser.findSymbol(s.substring(begin+"(".length()),"(")!=-1) {
                begin += Parser.findSymbol(s.substring(begin+"(".length()), "(")+"(".length();
            }
            end = Parser.findSymbol(s.substring(begin),")")+")".length();
        }

        return new int[]{begin,begin+end};
    }
}
