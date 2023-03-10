package Magic;

import java.util.Stack;

public class Parser {

    public static final String symboleSeparateur = " \t\n\0{}<>[]{}(),;.\"'^-+*/|:#";

    private String code;
    public Parser(String code) {
        this.code = enleverComment(code);
    }

    private String enleverComment(String s) {
        LitteralType context = null;
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            if (context == null) {
                if (s.charAt(i) == '(') {
                    context = LitteralType.comment;
                    continue;
                }
                if (s.charAt(i) == '"') {
                    context = LitteralType.str;
                    result.append(s.charAt(i));
                    continue;
                }
                if (s.charAt(i) == '\'') {
                    context = LitteralType.chr;
                    result.append(s.charAt(i));
                    continue;
                }
            }

            if (context == LitteralType.comment && s.charAt(i) == ')') {
                result.append(" ");
                context = null;
                continue;
            }
            if (context == LitteralType.str && s.charAt(i) == '"') {
                result.append(s.charAt(i));
                context = null;
                continue;
            }
            if (context == LitteralType.chr && s.charAt(i) == '\'') {
                result.append(s.charAt(i));
                context = null;
                continue;
            }

            if (context != LitteralType.comment) {
                result.append(s.charAt(i));
            }
        }

        return result.toString();
    }

    public void run() {
        Stack<Instructions.Instruction> stack = new Stack<>();
        Instructions.Block main = new Instructions.Block();
        main.block = (Instructions.Block) new Instructions.Block().create(Keywords.BEGIN.val+" "+code+" "+Keywords.END.val+".", null);

        new NativeLibrairy(main);

        System.out.println(main.block);
        stack.push(main);
        main.block.run(stack);
    }

    public static int findSymboleDetached(String code, String symbol) {
        if (symbol.isEmpty() || code.isEmpty()) return -1;

        boolean beginSymboleOk = symboleSeparateur.indexOf(symbol.charAt(0)) != -1;
        boolean endingSymboleOk = symboleSeparateur.indexOf(symbol.charAt(symbol.length() - 1)) != -1;

        //code:     rr;hello;rr
        //symbole:    ;hello;
        //tout le temps vrai si nextSymbol trouve
        if (beginSymboleOk && endingSymboleOk) {
            return findSymbol(code, symbol);
        }

        int next = 0;

        //pour le reste il faut verifier
        do {
            if (code.length()<symbol.length() || next>code.length()) return -1;

            next = findSymbol(code.substring(next), symbol);
            boolean beginCodeOk = next <= 0 || symboleSeparateur.indexOf(code.charAt(next - 1)) != -1;
            boolean endingCodeOk = next + symbol.length() >= code.length() || symboleSeparateur.indexOf(code.charAt(next + symbol.length())) != -1;

            if (next != -1) {
                //code:     rr;hello;rr
                //symbole:    ;hello
                if (beginSymboleOk && endingCodeOk) {
                    return next;
                }

                //code:     rr;hello;rr
                //symbole:     hello;
                if (beginCodeOk && endingSymboleOk) {
                    return next;
                }

                //code:     rr;hello;rr
                //symbole:     hello
                if (beginCodeOk && endingCodeOk) {
                    return next;
                }

                code = code.substring(next + 1);

            }
        }
        while (next != -1);

        return -1;
    }

    public enum LitteralType {str,chr,comment,tab}

    public static int findSymbol(String code, String symbol) {

        if (code==null) return -1;

        Stack<LitteralType> context = new Stack<>();

        for (int i = 0; i < code.length(); i++) {
            if (context.isEmpty()) {
                if (code.charAt(i) == '"') {
                    context.push(LitteralType.str);
                    continue;
                }
                if (code.charAt(i) == '\'') {
                    context.push(LitteralType.chr);
                    continue;
                }
                if (code.charAt(i) == '[') {
                    context.push(LitteralType.tab);
                    continue;
                }

                if (code.substring(i).length() >= symbol.length() &&
                        code.substring(i, symbol.length() + i).equals(symbol)) {
                    return i;
                }
            }

            //parce que c'est stackable
            if (code.charAt(i) == '[') {
                context.push(LitteralType.tab);
                continue;
            }

            if (!context.isEmpty() && context.peek() == LitteralType.str && code.charAt(i) == '"') {
                context.pop();
            }
            if (!context.isEmpty() && context.peek() == LitteralType.chr && code.charAt(i) == '\'') {
                context.pop();
            }
            if (!context.isEmpty() && context.peek() == LitteralType.tab && code.charAt(i) == ']') {
                context.pop();
            }
        }

        return -1;
    }

    public static int findLastSymbol(String code, String symbol) {

        code = new StringBuilder(code).reverse().toString();

        if (code==null) return -1;

        Stack<LitteralType> context = new Stack<>();

        for (int i = 0; i < code.length(); i++) {
            if (context.isEmpty()) {
                if (code.charAt(i) == '"') {
                    context.push(LitteralType.str);
                    continue;
                }
                if (code.charAt(i) == '\'') {
                    context.push(LitteralType.chr);
                    continue;
                }
                if (code.charAt(i) == ']') {
                    context.push(LitteralType.tab);
                    continue;
                }

                if (code.substring(i).length() >= symbol.length() &&
                        code.substring(i, symbol.length() + i).equals(symbol)) {
                    return (code.length()-1)-i;
                }
            }

            //parce que c'est stackable
            if (code.charAt(i) == ']') {
                context.push(LitteralType.tab);
                continue;
            }

            if (!context.isEmpty() && context.peek() == LitteralType.str && code.charAt(i) == '"') {
                context.pop();
            }
            if (!context.isEmpty() && context.peek() == LitteralType.chr && code.charAt(i) == '\'') {
                context.pop();
            }
            if (!context.isEmpty() && context.peek() == LitteralType.tab && code.charAt(i) == '[') {
                context.pop();
            }
        }

        return -1;
    }

    public static String clean(String base) {
        return base.replaceAll("[ \t\n]*", "");
    }
}
