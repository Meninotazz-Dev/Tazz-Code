package tazzcode;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * ONDE CADA COISA FICA (igual aos programas "de verdade" do Windows).
 *
 *   C:\Program Files\TazzCode\            O PROGRAMA (só lê, o instalador coloca)
 *       TazzCode.exe, app\TazzCode.jar, app\ia\ (código da IA), runtime\ (o Java)
 *
 *   C:\Users\SeuNome\AppData\Roaming\TazzCode\    SUAS CONFIGURAÇÕES (só suas)
 *       config.properties, erro.log
 *
 *   C:\Users\SeuNome\Documents\TazzCode\          SEUS PROJETOS (fácil de achar e de fazer backup)
 *       projetos\, rascunhos\
 *
 *   C:\ProgramData\TazzCode\                      AS COISAS GRANDES (sem acento e sem espaço!)
 *       plugins\, ia\ (a mini IA), compilados\, temp\
 *
 * Por que o ProgramData? O GCC (C/C++) se perde com caminhos tipo "C:\Users\João Antônio\..."
 * (ele corta no espaço e não entende o "ã"). O ProgramData nunca tem acento nem espaço.
 *
 * Quem usava a versão antiga (tudo em C:\Users\SeuNome\TazzCode) tem as coisas movidas sozinhas
 * na primeira vez que abrir (veja migrar()).
 */
public class Pastas {

    static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final String HOME = System.getProperty("user.home");

    private static Path dados, documentos, compartilhada;

    // =====================================================================
    //  AS 3 PASTAS PRINCIPAIS
    // =====================================================================

    /** Configurações do usuário (AppData\Roaming\TazzCode). */
    public static synchronized Path dados() {
        if (dados == null) {
            String appdata = System.getenv("APPDATA");
            dados = WINDOWS && appdata != null ? Paths.get(appdata, App.NOME) : Paths.get(HOME, ".config", "tazzcode");
            criar(dados);
        }
        return dados;
    }

    /** Projetos e rascunhos (Documentos\TazzCode). */
    public static synchronized Path documentos() {
        if (documentos == null) {
            Path docs = WINDOWS ? Paths.get(HOME, "Documents") : pastaXdg("XDG_DOCUMENTS_DIR", "Documents");
            documentos = Files.isDirectory(docs) ? docs.resolve(App.NOME) : Paths.get(HOME, App.NOME);
            criar(documentos);
        }
        return documentos;
    }

    /** Plugins, mini IA, compilados e temporários (C:\ProgramData\TazzCode: sem acento, sem espaço). */
    public static synchronized Path compartilhada() {
        if (compartilhada == null) {
            java.util.List<Path> opcoes = new java.util.ArrayList<>();
            if (WINDOWS) {
                opcoes.add(Paths.get(System.getenv().getOrDefault("ProgramData", "C:\\ProgramData"), App.NOME));
                opcoes.add(Paths.get(System.getenv().getOrDefault("PUBLIC", "C:\\Users\\Public"), App.NOME));
            } else {
                opcoes.add(Paths.get(HOME, ".local", "share", "tazzcode"));
            }
            for (Path p : opcoes) {
                if (!caminhoSeguro(p.toString())) continue;
                try {
                    Files.createDirectories(p);
                    if (Files.isWritable(p)) { compartilhada = p; break; }
                } catch (Exception ignorado) { }
            }
            if (compartilhada == null) compartilhada = dados();   // último caso
        }
        return compartilhada;
    }

    /** No Linux, o nome da pasta Documentos depende da língua (Documentos, Documents...): ele fica em user-dirs.dirs. */
    private static Path pastaXdg(String chave, String padrao) {
        try {
            for (String linha : Files.readAllLines(Paths.get(HOME, ".config", "user-dirs.dirs"), StandardCharsets.UTF_8)) {
                linha = linha.trim();
                if (linha.startsWith(chave + "=")) {
                    String v = linha.substring(chave.length() + 1).replace("\"", "").replace("$HOME", HOME);
                    if (!v.isEmpty() && !Paths.get(v).equals(Paths.get(HOME))) return Paths.get(v);
                }
            }
        } catch (Exception ignorado) { }
        for (String nome : new String[]{"Documentos", padrao}) if (Files.isDirectory(Paths.get(HOME, nome))) return Paths.get(HOME, nome);
        return Paths.get(HOME, padrao);
    }

    /** Onde o programa está instalado (a pasta do TazzCode.exe ou do Rodar.bat). */
    public static Path instalacao() {
        try {
            Path p = Paths.get(Pastas.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(p)) p = p.getParent();                         // ...\app\TazzCode.jar
            if (p.getFileName() != null && (p.getFileName().toString().equals("app") || p.getFileName().toString().equals("out")))
                p = p.getParent();
            return p;
        } catch (Exception e) {
            return Paths.get(System.getProperty("user.dir"));
        }
    }

    // =====================================================================
    //  AS PASTAS DE CADA COISA
    // =====================================================================

    public static Path arquivoConfig() { return dados().resolve("config.properties"); }
    public static Path arquivoErro()   { return dados().resolve("erro.log"); }

    public static Path projetos()      { return sub(documentos(), "projetos"); }
    public static Path rascunhos()     { return sub(documentos(), "rascunhos"); }

    public static Path plugins()       { return sub(compartilhada(), "plugins"); }
    public static Path ia()            { return sub(compartilhada(), "ia"); }
    public static Path compilados()    { return sub(compartilhada(), "compilados"); }
    public static Path temporarios()   { return sub(compartilhada(), "temp"); }

    /** Antes era a pasta única. Agora quem chama recebe a pasta das configurações. */
    @Deprecated
    public static Path raiz() { return dados(); }

    /** O caminho só tem letras sem acento, números e símbolos simples (nada de espaço)? */
    public static boolean caminhoSeguro(String caminho) {
        for (char c : caminho.toCharArray()) if (c > 126 || c == ' ') return false;
        return true;
    }

    // =====================================================================
    //  MUDANÇA DA VERSÃO ANTIGA (tudo em C:\Users\SeuNome\TazzCode)
    // =====================================================================

    /** Chamado uma vez quando o TazzCode abre. Devolve uma mensagem se mudou alguma coisa (ou null). */
    public static String migrar() {
        Path velha = Paths.get(HOME, App.NOME);
        Path publica = WINDOWS ? Paths.get(System.getenv().getOrDefault("PUBLIC", "C:\\Users\\Public"), App.NOME) : null;
        boolean mudou = false;
        try {
            // documentos() pode ser a própria pasta velha (se não tiver "Documents"): aí não muda nada
            if (Files.isDirectory(velha) && !velha.equals(documentos()) && !velha.equals(compartilhada())) {
                Path cfgVelho = velha.resolve("config.properties");
                if (Files.isRegularFile(cfgVelho) && !Files.exists(arquivoConfig())) {
                    copiarConfigTrocando(cfgVelho, arquivoConfig(), velha.resolve("projetos").toString(), projetos().toString(),
                            velha.resolve("rascunhos").toString(), rascunhos().toString());
                    Files.delete(cfgVelho);
                    mudou = true;
                }
                mudou |= mover(velha.resolve("projetos"), projetos());
                mudou |= mover(velha.resolve("rascunhos"), rascunhos());
                mudou |= mover(velha.resolve("plugins"), plugins());
                mudou |= mover(velha.resolve("ia"), ia());
                for (String lixo : new String[]{"compilados", "temp"}) apagar(velha.resolve(lixo));
                try { Files.deleteIfExists(velha.resolve("erro.log")); } catch (Exception ignorado) { }
                apagarSeVazia(velha);
            }
            if (publica != null && Files.isDirectory(publica) && !publica.equals(compartilhada())) {
                mudou |= mover(publica.resolve("plugins"), plugins());
                mudou |= mover(publica.resolve("ia"), ia());
                for (String lixo : new String[]{"compilados", "temp"}) apagar(publica.resolve(lixo));
                apagarSeVazia(publica);
            }
        } catch (Exception e) {
            return "Não consegui mudar tudo de lugar: " + e.getMessage();
        }
        return mudou ? "O " + App.NOME + " organizou as pastas:\n\n"
                + "  Configurações:  " + dados() + "\n"
                + "  Seus projetos:  " + projetos() + "\n"
                + "  Plugins e IA:   " + compartilhada() + "\n\n"
                + "Nada foi apagado: só mudou de lugar." : null;
    }

    /** Copia o config.properties trocando os caminhos antigos dos projetos pelos novos. */
    private static void copiarConfigTrocando(Path de, Path para, String... trocas) throws IOException {
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(de, StandardCharsets.UTF_8)) { p.load(r); }
        for (String chave : p.stringPropertyNames()) {
            String v = p.getProperty(chave);
            for (int i = 0; i + 1 < trocas.length; i += 2) v = v.replace(trocas[i], trocas[i + 1]);
            p.setProperty(chave, v);
        }
        try (Writer w = Files.newBufferedWriter(para, StandardCharsets.UTF_8)) { p.store(w, App.NOME + " - configuracoes"); }
    }

    /** Move tudo que está dentro de "de" pra dentro de "para" (o que já existir lá, fica). */
    private static boolean mover(Path de, Path para) {
        if (!Files.isDirectory(de)) return false;
        boolean mudou = false;
        try (Stream<Path> itens = Files.list(de)) {
            for (Path item : (Iterable<Path>) itens::iterator) {
                Path destino = para.resolve(item.getFileName().toString());
                if (Files.exists(destino)) continue;
                try {
                    Files.move(item, destino);
                    mudou = true;
                } catch (Exception ignorado) { }   // arquivo em uso: fica onde está
            }
        } catch (Exception ignorado) { }
        apagarSeVazia(de);
        return mudou;
    }

    private static void apagarSeVazia(Path p) {
        try (Stream<Path> s = Files.list(p)) {
            if (!s.findAny().isPresent()) Files.delete(p);
        } catch (Exception ignorado) { }
    }

    private static void apagar(Path p) {
        if (!Files.exists(p)) return;
        try (Stream<Path> s = Files.walk(p)) {
            s.sorted(java.util.Comparator.reverseOrder()).forEach(x -> { try { Files.delete(x); } catch (Exception ignorado) { } });
        } catch (Exception ignorado) { }
    }

    private static Path sub(Path pai, String nome) {
        Path p = pai.resolve(nome);
        criar(p);
        return p;
    }

    private static void criar(Path p) {
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            throw new RuntimeException("Não consegui criar a pasta " + p, e);
        }
    }
}
