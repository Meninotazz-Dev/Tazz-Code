package tazzcode;

import javax.swing.SwingUtilities;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * O VIGIA DE TRAVAMENTOS.
 *
 * Uma thread que, a cada segundo, "cutuca" a thread da janela (a que desenha tudo).
 * Se a janela demorar mais de 5 segundos pra responder, o TazzCode está travado:
 * o vigia anota O QUE a janela estava fazendo naquela hora no arquivo travamentos.log
 * (na pasta das configurações: menu Arquivo > Abrir pasta das configurações).
 *
 * Assim, quando travar, é só mandar esse arquivo pro Claude que dá pra ver o culpado.
 */
public class Vigia {

    private static final long LIMITE_MS = 5000;
    private static volatile long ultimaResposta = System.currentTimeMillis();
    private static volatile Thread threadDaJanela;

    public static void comecar() {
        SwingUtilities.invokeLater(() -> threadDaJanela = Thread.currentThread());
        Thread t = new Thread(Vigia::vigiar, "Vigia de travamentos");
        t.setDaemon(true);
        t.start();
    }

    private static void vigiar() {
        boolean anotouEsse = false;
        long comecou = 0;
        while (true) {
            try { Thread.sleep(1000); } catch (InterruptedException e) { return; }
            SwingUtilities.invokeLater(() -> ultimaResposta = System.currentTimeMillis());
            long parado = System.currentTimeMillis() - ultimaResposta;
            if (parado > LIMITE_MS) {
                if (!anotouEsse) {
                    comecou = ultimaResposta;
                    anotar("TRAVOU (a janela está parada há " + parado / 1000 + " s). O que ela estava fazendo:", true);
                    anotouEsse = true;
                }
            } else if (anotouEsse) {
                anotar("Destravou depois de " + (ultimaResposta - comecou) / 1000 + " s.\n", false);
                anotouEsse = false;
            }
        }
    }

    private static void anotar(String titulo, boolean comPilha) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("] ")
          .append(App.NOME).append(" ").append(App.VERSAO).append(" - ").append(titulo).append("\n");
        Thread t = threadDaJanela;
        if (comPilha && t != null) {
            for (StackTraceElement e : t.getStackTrace()) sb.append("    at ").append(e).append("\n");
            Runtime r = Runtime.getRuntime();
            sb.append("    memória: ").append((r.totalMemory() - r.freeMemory()) / 1048576).append(" MB usados de ")
              .append(r.maxMemory() / 1048576).append(" MB\n");
        }
        try {
            Path log = Pastas.dados().resolve("travamentos.log");
            if (Files.exists(log) && Files.size(log) > 500_000) Files.delete(log);   // não deixa crescer pra sempre
            Files.writeString(log, sb.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignorado) { }
    }
}
