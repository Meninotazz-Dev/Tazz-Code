package tazzcode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abre o TazzCode de novo (pra aplicar o tema novo, por exemplo).
 *
 * Como funciona: pergunta pro sistema "qual comando abriu este programa?"
 * (ex.: java -cp out tazzcode.Main, ou o TazzCode.exe) e roda ele de novo,
 * passando o projeto e os arquivos que estavam abertos. Depois fecha este.
 */
public class Reiniciar {

    public static void agora(File projeto, List<String> arquivosAbertos) {
        try {
            ProcessHandle.Info info = ProcessHandle.current().info();
            String comando = info.command().orElse(null);
            if (comando == null) throw new IllegalStateException("não sei qual comando abriu o TazzCode");
            List<String> cmd = new ArrayList<>();
            cmd.add(comando);
            // Os argumentos de antes, MENOS os arquivos que estavam abertos (a gente passa de novo abaixo)
            List<String> antes = new ArrayList<>(Arrays.asList(info.arguments().orElse(new String[0])));
            int main = antes.indexOf("tazzcode.Main");
            if (main >= 0) antes = antes.subList(0, main + 1);                       // java ... tazzcode.Main
            else if (comando.toLowerCase().contains("java")) antes = jeitoPadrao(); // java sem os argumentos (raro)
            else antes = new ArrayList<>();                                          // TazzCode.exe / bin/TazzCode
            cmd.addAll(antes);
            if (projeto != null) cmd.add("--projeto=" + projeto.getAbsolutePath());
            cmd.addAll(arquivosAbertos);
            new ProcessBuilder(cmd).inheritIO().start();
            System.exit(0);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Não consegui reiniciar sozinho (" + e.getMessage() + ").\nFeche e abra o " + App.NOME + " de novo.",
                    App.NOME, javax.swing.JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }

    private static List<String> jeitoPadrao() {
        return new ArrayList<>(Arrays.asList("-cp", System.getProperty("java.class.path"), "tazzcode.Main"));
    }
}
