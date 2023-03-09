package Magic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MagicInterpreteur {

    public static void main(String[] args) throws IOException {
        MagicInterpreteur.run();
    }

    public static void run() throws IOException {
        new Parser(read()).run();
    }

    public static String read() throws IOException {

        Path path = Paths.get("example.magic");

        return Files.readAllLines(path).stream().reduce((s1,s2)->s1+" "+s2).get();
    }
}