package Magic;

import java.util.List;
import java.util.function.Function;

public class NativeLibrairy {
    Function<List<Types.Type>,Types.Type> print = (l) -> {
        System.out.println("$>"+l.get(0));
        return null;
    };

    public NativeLibrairy(Instructions.Block main) {
        new NativeFunction("IUSSUS SCRIPTO[PHRASIS  NOMINE s]", main, print);
        new NativeFunction("IUSSUS SCRIPTO[NUMERUS  NOMINE n]", main, print);
        new NativeFunction("IUSSUS SCRIPTO[VERITAS  NOMINE v]", main, print);
        new NativeFunction("IUSSUS SCRIPTO[ALBUM    NOMINE a]", main, print);
        new NativeFunction("IUSSUS SCRIPTO[SYMBOLUM NOMINE s]", main, print);

        /*------math------*/
        new NativeFunction("IUSSUS ABS[NUMERUS NOMINE n] REDIT NUMERUS", main, (l) -> {
            Types.Numerus result = new Types.Numerus();
            result.value = Types.Numerus.toNumerus(Math.abs(Types.Numerus.toDouble((Types.Numerus)l.get(0)))).value;
            return result;
        });
        /*
        new NativeFunction("IUSSUS TOPHR[NUMERUS  NOMINE n] REDIT PHRASIS", main, null);
        new NativeFunction("IUSSUS TOPHR[VERITAS  NOMINE n] REDIT PHRASIS", main, null);
        new NativeFunction("IUSSUS TOPHR[ALBUM    NOMINE n] REDIT PHRASIS", main, null);
        new NativeFunction("IUSSUS TOPHR[SYMBOLUM NOMINE n] REDIT PHRASIS", main, null);

        new NativeFunction("IUSSUS TONUM[PHRASIS  NOMINE n] REDIT NUMERUS", main, null);
        new NativeFunction("IUSSUS TONUM[VERITAS  NOMINE n] REDIT NUMERUS", main, null);
        new NativeFunction("IUSSUS TONUM[ALBUM    NOMINE n] REDIT NUMERUS", main, null);
        new NativeFunction("IUSSUS TONUM[SYMBOLUM NOMINE n] REDIT NUMERUS", main, null);


        new NativeFunction("IUSSUS IUNCTA [IUSSUS F; ALBUM NOMINE n] REDIT ?"    , main, null);//reduce
        new NativeFunction("IUSSUS MUTATUS[IUSSUS F; ALBUM NOMINE n] REDIT ALBUM", main, null);//map
        new NativeFunction("IUSSUS CONTRA [          ALBUM NOMINE n] REDIT ALBUM", main, null);//reverse

        new NativeFunction("IUSSUS OMNES      [IUSSUS F; ALBUM NOMINE n] REDIT VERITAS", main, null);//allmatch
        new NativeFunction("IUSSUS SALTEM_UNUM[IUSSUS F; ALBUM NOMINE n] REDIT VERITAS", main, null);//anymatch
        new NativeFunction("IUSSUS NULLUS     [IUSSUS F; ALBUM NOMINE n] REDIT VERITAS", main, null);//nonematch
        new NativeFunction("IUSSUS PRIMUS_QUI [IUSSUS F; ALBUM NOMINE n] REDIT ?", main, null);//first match
        new NativeFunction("IUSSUS ULTIMA_QUI [IUSSUS F; ALBUM NOMINE n] REDIT ?", main, null);//last match
        new NativeFunction("IUSSUS QUANTITAS  [          ALBUM NOMINE n] REDIT NUMERUS", main, null);//size
        new NativeFunction("IUSSUS PERCOLANTUR[IUSSUS F; ALBUM NOMINE n] REDIT ALBUM", main, null);//filter
        new NativeFunction("IUSSUS CONNECT    [ALBUM NOMINE n1;ALBUM NOMINE n2] REDIT ALBUM", main, null);//concat
        new NativeFunction("IUSSUS DIPONERE   [IUSSUS F; ALBUM NOMINE n] REDIT ALBUM", main, null);//sort
        */
    }
}
