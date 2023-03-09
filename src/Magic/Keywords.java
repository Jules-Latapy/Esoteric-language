package Magic;

public enum Keywords {
    CONST("INCOMMUTABILIS"),
    NAME("NOMINE"),
    VALUE("VALET"),
    BEGIN("FACERE"),
    END("COMPLEVIT"),
    IF("SI"),
    ELSE("ALIUS"),
    SWITCH("SECUNDUM"),
    CASE("QUANDO"),
    WHILE("QUANDIU"),
    FOR("TRAICERE"),
    BREAK("RUMPITUR"),
    CONTINUE("PROGREDI"),
    PROGRAM("IUSSUS"),
    RETURN("REDIT");

    public String val ;
    
    private Keywords(String word) {
        val=word;
    }
}
