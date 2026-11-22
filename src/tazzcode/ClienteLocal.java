package tazzcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Conversa com a IA PRÓPRIA do TazzCode (o programa em Python da pasta "ia").
 *
 *   1) se o servidor Python não estiver ligado, liga ele (python ia/servidor.py)
 *   2) manda a pergunta pra http://127.0.0.1:7878/perguntar
 *   3) a resposta volta aos pedacinhos
 *
 * Tudo roda no seu computador: não precisa de internet nem de chave.
 */
public class ClienteLocal {

    private static final String ENDERECO = "http://127.0.0.1:7878";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3))
            .proxy(java.net.ProxySelector.of(null)).build();   // é no próprio PC: sem proxy
    private static Process servidor;
    private static volatile boolean cancelado;

    /** Onde ficam a mini IA e o llama.cpp. (A gente ainda vai decidir a pasta final: é só mudar aqui.) */
    public static File pastaDaIA() {
        String c = Config.texto("ia.pasta", "");
        return c.isEmpty() ? Pastas.ia().toFile() : new File(c);
    }

    /** Onde está o código Python da IA (a pasta "ia" que vem junto com o TazzCode). */
    public static File codigoDaIA() {
        List<File> lugares = new ArrayList<>();
        File aqui = new File(System.getProperty("user.dir"));
        lugares.add(Pastas.instalacao().resolve("app").resolve("ia").toFile());   // instalado (Program Files)
        lugares.add(Pastas.instalacao().resolve("ia").toFile());                  // pelo Rodar.bat
        lugares.add(new File(aqui, "ia"));
        lugares.add(new File(aqui.getParentFile() == null ? aqui : aqui.getParentFile(), "ia"));
        try {
            File jar = new File(ClienteLocal.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File pasta = jar.isFile() ? jar.getParentFile() : jar;
            lugares.add(new File(pasta, "ia"));
            if (pasta.getParentFile() != null) lugares.add(new File(pasta.getParentFile(), "ia"));
        } catch (Exception ignorado) { }
        for (File f : lugares) if (new File(f, "servidor.py").isFile()) return f;
        return null;
    }

    // =====================================================================
    //  LIGAR / DESLIGAR
    // =====================================================================

    /** Pergunta como a IA está: devolve o JSON do /saude, ou null se estiver desligada. */
    public static Map<?, ?> saude() {
        try {
            HttpResponse<String> r = HTTP.send(HttpRequest.newBuilder(URI.create(ENDERECO + "/saude")).timeout(Duration.ofSeconds(2)).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return r.statusCode() == 200 ? (Map<?, ?>) Json.ler(r.body()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Acha o Python (do PC ou do plugin). */
    public static List<String> python() {
        for (String nome : new String[]{"python", "py", "python3"}) {
            List<String> r = Executaveis.resolver(new ArrayList<>(Arrays.asList(nome)));
            if (r != null) return r;
        }
        return null;
    }

    /** Liga o servidor Python, se precisar. Lança erro com explicação se não der. */
    public static synchronized void ligar() throws Exception {
        if (saude() != null) return;
        File codigo = codigoDaIA();
        if (codigo == null) throw new Exception("não achei a pasta \"ia\" do TazzCode (ela tem que ficar junto do Rodar.bat)");
        List<String> cmd = python();
        if (cmd == null) throw new Exception("SEM_PYTHON");
        cmd = new ArrayList<>(cmd);
        cmd.addAll(Arrays.asList("-u", new File(codigo, "servidor.py").getAbsolutePath(), "--pasta", pastaDaIA().getAbsolutePath()));
        pastaDaIA().mkdirs();
        ProcessBuilder pb = new ProcessBuilder(cmd).directory(codigo).redirectErrorStream(true)
                .redirectOutput(new File(pastaDaIA(), "servidor.log"));
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        pb.environment().put("PYTHONDONTWRITEBYTECODE", "1");   // não tenta escrever no Program Files
        Executaveis.prepararAmbiente(pb.environment());
        servidor = pb.start();
        Runtime.getRuntime().addShutdownHook(new Thread(ClienteLocal::desligar));
        for (int i = 0; i < 60; i++) {   // espera até 15 s
            if (saude() != null) return;
            if (!servidor.isAlive()) break;
            Thread.sleep(250);
        }
        String log = "";
        try { log = new String(java.nio.file.Files.readAllBytes(new File(pastaDaIA(), "servidor.log").toPath()), StandardCharsets.UTF_8); } catch (Exception ignorado) { }
        throw new Exception("a IA não ligou. " + (log.isEmpty() ? "" : "Mensagem: " + log.trim().replace("\n", " ")));
    }

    public static void desligar() {
        try {
            HTTP.send(HttpRequest.newBuilder(URI.create(ENDERECO + "/desligar")).timeout(Duration.ofSeconds(1))
                    .POST(HttpRequest.BodyPublishers.ofString("{}")).build(), HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignorado) { }
        if (servidor != null && servidor.isAlive()) servidor.destroy();
    }

    public static void cancelar() { cancelado = true; }

    // =====================================================================
    //  PERGUNTAR
    // =====================================================================

    /**
     * Manda a pergunta. Os pedaços da resposta chegam em "pedaco"; avisos (tipo "pensando...") em "aviso".
     * No fim chama "fim" com quem respondeu ("especialista" ou "mini-ia"), ou "ERRO: ..." se deu problema.
     */
    public static void perguntar(String pergunta, Map<String, Object> contexto, List<ClienteClaude.Mensagem> historico,
                                 Consumer<String> pedaco, Consumer<String> aviso, Consumer<String> fim) {
        cancelado = false;
        Thread t = new Thread(() -> {
            try {
                ligar();
                Map<String, Object> corpo = new java.util.LinkedHashMap<>();
                corpo.put("pergunta", pergunta);
                corpo.put("contexto", contexto);
                List<Object> h = new ArrayList<>();
                for (ClienteClaude.Mensagem m : historico) {
                    Map<String, Object> mm = new java.util.LinkedHashMap<>();
                    mm.put("papel", m.papel);
                    mm.put("texto", m.texto);
                    h.add(mm);
                }
                corpo.put("historico", h);
                HttpRequest req = HttpRequest.newBuilder(URI.create(ENDERECO + "/perguntar")).timeout(Duration.ofMinutes(5))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(Json.escrever(corpo), StandardCharsets.UTF_8)).build();
                HttpResponse<java.io.InputStream> r = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());
                String quem = "especialista";
                try (BufferedReader in = new BufferedReader(new InputStreamReader(r.body(), StandardCharsets.UTF_8))) {
                    String linha;
                    while ((linha = in.readLine()) != null) {
                        if (cancelado) { fim.accept("ERRO: (parado)"); return; }
                        if (!linha.startsWith("data:")) continue;
                        Map<?, ?> m = (Map<?, ?>) Json.ler(linha.substring(5).trim());
                        if (m.containsKey("texto")) pedaco.accept(String.valueOf(m.get("texto")));
                        if (m.containsKey("aviso")) aviso.accept(String.valueOf(m.get("aviso")));
                        if (m.containsKey("fim")) quem = String.valueOf(m.get("quem"));
                    }
                }
                fim.accept(quem);
            } catch (Exception e) {
                fim.accept("ERRO: " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // =====================================================================
    //  INSTALAR A MINI IA
    // =====================================================================

    /** Roda o ia/instalar.py. Cada linha que ele escreve chega em "linha". No fim: null (deu certo) ou o erro. */
    public static void instalarMiniIA(Consumer<String> linha, Consumer<String> fim) {
        Thread t = new Thread(() -> {
            try {
                File codigo = codigoDaIA();
                if (codigo == null) throw new Exception("não achei a pasta \"ia\" do TazzCode");
                List<String> cmd = python();
                if (cmd == null) throw new Exception("SEM_PYTHON");
                cmd = new ArrayList<>(cmd);
                cmd.addAll(Arrays.asList("-u", new File(codigo, "instalar.py").getAbsolutePath(), "--pasta", pastaDaIA().getAbsolutePath()));
                ProcessBuilder pb = new ProcessBuilder(cmd).directory(codigo).redirectErrorStream(true);
                pb.environment().put("PYTHONIOENCODING", "utf-8");
        pb.environment().put("PYTHONDONTWRITEBYTECODE", "1");   // não tenta escrever no Program Files
                Executaveis.prepararAmbiente(pb.environment());
                Process p = pb.start();
                String erro = null;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String l;
                    while ((l = in.readLine()) != null) {
                        if (l.startsWith("ERRO ")) erro = l.substring(5);
                        linha.accept(l);
                    }
                }
                if (p.waitFor() != 0 && erro == null) erro = "o instalador parou com erro";
                desligar();   // o servidor vai religar já enxergando a mini IA
                fim.accept(erro);
            } catch (Exception e) {
                fim.accept(e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
