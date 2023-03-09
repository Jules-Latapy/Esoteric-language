package Magic;

import java.lang.reflect.Modifier;
import java.util.*;

import static Magic.Instructions.Block.decoupe;
import static java.lang.Math.abs;

enum RomanDigit {
    NIHIL, I, V, X, L, C, D, M;

    static int digitToInt(RomanDigit r) {
        return switch (r) {
            case NIHIL -> 0;
            case I -> 1;
            case V -> 5;
            case X -> 10;
            case L -> 50;
            case C -> 100;
            case D -> 500;
            case M -> 1000;
        };
    }
}

class Pair<T> {
    public T right;
    public T left;

    public Pair(T r, T l) {
        right = r ;
        left = l ;
    }
}

//VERITAS, ALBUM, NUMERUS, SYMBOLUM, PHRASIS
abstract class Types{

    @SuppressWarnings("rawtypes")
    public static Set<Class<Type>> BASE_TYPE = new HashSet<>();

    static {
        for (Class t: Types.class.getDeclaredClasses()){
            if (!Modifier.isAbstract(t.getModifiers()))
                BASE_TYPE.add(t);
        }
    }

    abstract static class Type<T> {

        /**
         * "XCVM" -> new Numerus().create("XCVM")
         */
        static Type createType(String data) {
            for (Class<Type> clas : BASE_TYPE) {
                try {
                    Type t = (Type) clas.getDeclaredConstructors()[0].newInstance();

                    if ((Boolean) clas.getDeclaredMethod("isType", String.class).invoke(t,data)) {
                        return t.create(data);
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

        public T value;

        //on peut garder les espaces autour de s
        abstract Type<T> create(@WithDelimiter @PreCalculate String s);

        abstract boolean isType(@WithDelimiter @PreCalculate String s);

        abstract Type<T> copy() ;

        @Override
        public String toString() {
            return ""+value;
        }
    }

    static class Numerus extends Type<Pair<List<RomanDigit>>> {

        boolean negativ = false ;

        static int exp = 0 ;

        static double toDouble(Numerus n) {

            int postVirg = 0 ;
            int preVirg  = 0 ;

            for (RomanDigit r : n.value.right) {
                preVirg += RomanDigit.digitToInt(r);
            }

            exp = 0;
            List<RomanDigit> withoutExp = n.value.left.stream().dropWhile(e->{
                if (e == RomanDigit.NIHIL){
                    exp++;
                    return true;
                } else {
                    return false;
                }
            }).toList();

            for (RomanDigit r : withoutExp) {
                postVirg += RomanDigit.digitToInt(r);
            }

            return  n.negativ? -Double.parseDouble(preVirg+"."+"0".repeat(exp)+postVirg ):
                                Double.parseDouble(preVirg+"."+"0".repeat(exp)+postVirg );
        }


        private static String integerToNumerusString(int i) {
            return "I".repeat(i)

            .replaceAll("I{5}","V")

            .replaceAll("VV","X")

            .replaceAll("X{5}","L")

            .replaceAll("LL","C")

            .replaceAll("C{5}","D")

            .replaceAll("DD","M");
        }

        static Numerus toNumerus(double d) {
            boolean negativ = d<0;

            String rightS = integerToNumerusString((int)abs(d));
            String leftS  = Double.toString(d).replaceAll(".*\\.","");

            /*-------------------*/
            int index = 0;
            while(index< leftS.length() && leftS.charAt(index)=='0'){index++;}

            String zeroAfterDot = leftS.substring(0, index).replaceAll("0","NIHIL");
            if (!leftS.substring(index).isEmpty())
                leftS=integerToNumerusString(Integer.parseInt(leftS.substring(index)));
            else
                leftS="";

            return new Types.Numerus().create((negativ?"-":"")+rightS+","+zeroAfterDot+leftS);
        }

        /*
         * IIII -> 4
         * XI   -> 11
         */
        @Override
        Numerus create(String s) {

            Numerus           result = new Numerus();
            Pair<List<RomanDigit>> value  = new Pair(new ArrayList<>(),new ArrayList<>());

            s = Parser.clean(s).replaceAll("_*|\\+*|(--)*","");

            String preVirgule = s.split(",")[0];

            /*-------------------------------------------------------*/
            /*                      Pre virgule                      */
            /*-------------------------------------------------------*/

            for (int i=0; i<preVirgule.length(); i++) {

                if (preVirgule.substring(i).length()>= RomanDigit.NIHIL.toString().length() &&
                    preVirgule.substring(i, RomanDigit.NIHIL.toString().length()+i).equals(RomanDigit.NIHIL.toString())) {

                    i+= RomanDigit.NIHIL.toString().length()-1;
                    value.right.add(RomanDigit.NIHIL);
                    continue;
                }

                if (preVirgule.charAt(i)=='-') {
                    result.negativ = true;
                    continue;
                }
                value.right.add(RomanDigit.valueOf(Character.toString(preVirgule.charAt(i))));
            }

            /*--------------------------------------------------------*/
            /*                      Post virgule                      */
            /*--------------------------------------------------------*/

            if (s.split(",").length > 1) {
                String postVirgule = s.split(",")[1];
                for (int i=0; i<postVirgule.length(); i++) {
                    if (postVirgule.substring(i).length()>= RomanDigit.NIHIL.toString().length() &&
                            postVirgule.substring(i, RomanDigit.NIHIL.toString().length()+i).equals(RomanDigit.NIHIL.toString())) {

                        i+= RomanDigit.NIHIL.toString().length()-1;
                        value.left.add(RomanDigit.NIHIL);
                        continue;
                    }
                    value.left.add(RomanDigit.valueOf(Character.toString(postVirgule.charAt(i))));
                }
            }

            result.value = value ;
            return result;
        }

        @Override
        boolean isType(String s) {
            if (s==null || s.isEmpty()) return false ;
            s=s.strip();
            for (RomanDigit r: RomanDigit.values()) {
                s=s.replaceAll(r.toString(),"");
            }
            s=s.replaceAll("[,+-]+","");
            return s.isEmpty();
        }

        @Override
        Numerus copy() {
            Numerus result = new Numerus();
            Pair<List<RomanDigit>> value = new Pair(new ArrayList<>(),new ArrayList<>());
            value.right.addAll(this.value.right);
            value.left.addAll(this.value.left);
            result.value = value;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Numerus n)
                return Calculator.compareCollection(n.value.left ,value.left ) &&
                       Calculator.compareCollection(n.value.right,value.right);
            else
                return false;
        }

        @Override
        public String toString() {
            return  (negativ?"-":"")+value.right.toString().replaceAll("[, \\[\\]]*","")+","+
                                     value.left.toString().replaceAll("[, \\[\\]]*","");
        }
    }

    static class Veritas extends Type<Boolean> {

        @Override
        Veritas create(String s) {
            s=s.strip();
            Veritas result = new Veritas();
            if (s.equalsIgnoreCase("VERUM")){
                result.value = true ;
            }else
            if (s.equalsIgnoreCase("FICTUS")) {
                result.value = false ;
            } else {
                throw new RuntimeException("type incompatible");
            }

            return result;
        }
        @Override
        boolean isType(String s) {
            if (s==null) return false ;
            s=s.strip();
            if (s.isEmpty()) return false ;
            return s.equalsIgnoreCase("FICTUS") || s.equalsIgnoreCase("VERUM") ;
        }

        @Override
        Veritas copy() {
            Veritas result = new Veritas();
            result.value = this.value;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Veritas n)
                return value.equals(n.value);
            else
                return false;
        }
    }

    static class Phrasis extends Type<String> {

        @Override
        Phrasis create(String s) {
            s=s.strip();
            Phrasis p = new Phrasis();
            p.value = s.substring(1, s.length()-1);
            return p ;
        }

        @Override
        boolean isType(String s) {
            if (s==null) return false ;
            s=s.strip();
            if (s.isEmpty()) return false ;
            return s.matches("^\".*\"$") ;
        }

        @Override
        Phrasis copy() {
            Phrasis result = new Phrasis();
            result.value = this.value;
            return result;
        }

        @Override
        public String toString() {
            return "\""+value+"\"";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Phrasis p)
                return value.equals(p.value);
            else
                return false;
        }
    }

    static class Symbolum extends Type<Character> {

        @Override
        Symbolum create(String s) {
            s=s.strip();
            Symbolum c = new Symbolum();
            c.value = s.charAt(1);
            return c;
        }

        @Override
        boolean isType(String s) {
            if (s==null) return false ;
            s=s.strip();
            if (s.isEmpty()) return false ;
            return s.matches("^'.*'$") ;
        }

        @Override
        Symbolum copy() {
            Symbolum result = new Symbolum();
            result.value = this.value;
            return result;
        }

        @Override
        public String toString() {
            return "'"+value+"'";
        }
    }

    @SuppressWarnings("rawtypes")
    static class Album extends Type<ArrayList<Type>> {

        @Override
        Album create(String s) {
            s=s.strip().substring(1, s.length()-1);

            Album result = new Album();
            result.value = new ArrayList<>();

            if (s.isBlank()) return  result;

            for (String value : decoupe(s,";"))
                result.value.add(createType(value));

            return result;
        }

        @Override
        boolean isType(String s) {
            if (s==null) return false ;
            s=s.strip();
            if (s.isEmpty()) return false ;
            if (!s.matches("^\\[.*\\]$")) return false;

            Stack<Parser.LitteralType> context = new Stack<>();

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
                        if (alreadyBegun) return false;
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

            return true ;
        }

        @Override
        Album copy() {
            Album result = new Album();
            ArrayList<Type> value = new ArrayList<>();
            for (Type t :result.value)
                value.add(t.copy());

            result.value = value;
            return result;
        }

        public String toString() {
            StringBuilder s = new StringBuilder("[");
            for (int i=0; i<value.size(); i++) {
                s.append(value.get(i).toString());
                if(i!=value.size()-1)
                    s.append("; ");
            }
            return s+"]";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Album a)
                return Calculator.compareCollection(a.value,this.value);
            else
                return false;
        }
    }
}