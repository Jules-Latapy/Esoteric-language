package Magic;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

public class NativeFunction {

    private final Function<List<Types.Type>, Types.Type> fct;

    public NativeFunction(@WithDelimiter String declaration, Instructions.Block blockToRegister, Function<List<Types.Type>, Types.Type> fct) {
        this.fct = fct;

        Instructions.Program coorespondance = (Instructions.Program) new Instructions.Program().create(declaration, null);

        coorespondance.block = new Instructions.Block() {
            @Override
            public void run(Stack<Instructions.Instruction> stack) {
                List<Types.Type> params = new ArrayList<>();
                for (String name : coorespondance.paramInfo.keySet())
                    if (this.variables.containsKey(name))
                        params.add(this.variables.get(name)) ;
                    else
                        params.add(this.constantes.get(name)) ;

                fct.apply(params);
            }
        };

        //enregistrement des variables
        Stack<Instructions.Instruction> s = new Stack<>();
        s.push(blockToRegister);
        coorespondance.run(s);
    }
}