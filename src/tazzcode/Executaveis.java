package tazzcode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * ACHA OS PROGRAMAS (python, node, gcc...) antes de rodar.
 *
 * Ordem de procura:
 *   1) instalado no PC (as pastas do PATH do Windows)
 *   2) os PLUGINS do TazzCode
 *
 * E arruma três "pegadinhas" do Windows:
 *   - "npm" e "npx" na verdade são npm.cmd / npx.cmd, e o Java não acha .cmd sozinho
 *     (por isso eles rodam pelo "cmd /c")
 *   - o Windows tem um "python" FALSO (atalho da Microsoft Store) que só abre a loja;
 *     se tiver o plugin do Python, a gente usa o plugin em vez dele
 *   - os plugins entram no PATH dos programas que a gente roda (assim o npx acha o node do plugin)
 */
public class Executaveis {

    static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    /** O PATH que os programas vão usar: o do PC + as pastas dos plugins no fim. */
    public static String pathComPlugins() {
        StringBuilder sb = new StringBuilder(System.getenv().getOrDefault(WINDOWS ? chavePath() : "PATH", ""));
        for (File d : Plugins.pastasInstaladas()) sb.append(File.pathSeparator).append(d.getAbsolutePath());
        return sb.toString();
    }

    /** No Windows a variável pode se chamar "Path" (e não "PATH"). */
    private static String chavePath() {
        for (String k : System.getenv().keySet()) if (k.equalsIgnoreCase("PATH")) return k;
        return "PATH";
    }

    /** Coloca o PATH com plugins no ambiente do processo. */
    public static void prepararAmbiente(Map<String, String> env) {
        String chave = "PATH";
        for (String k : env.keySet()) if (k.equalsIgnoreCase("PATH")) chave = k;
        env.put(chave, pathComPlugins());
        // Pasta temporária sem acento/espaço (o GCC se perde com "C:\\Users\\João Antônio\\...\\Temp")
        if (WINDOWS) {
            String temp = env.getOrDefault("TEMP", "");
            if (!Pastas.caminhoSeguro(temp)) {
                String nova = Pastas.temporarios().toString();
                if (Pastas.caminhoSeguro(nova)) {
                    env.put("TEMP", nova);
                    env.put("TMP", nova);
                }
            }
        }
        // Variáveis que os plugins precisam (ex.: RUSTUP_HOME). Só coloca se o PC ainda não tiver.
        for (Plugins.Plugin p : Plugins.todos()) {
            if (!p.instalado()) continue;
            for (Map.Entry<String, String> e : p.ambiente.entrySet()) {
                if (System.getenv(e.getKey()) == null) env.put(e.getKey(), e.getValue().replace("{pasta}", p.pasta().getAbsolutePath()));
            }
        }
    }

    /**
     * Devolve o comando pronto pra rodar (com o caminho completo do programa),
     * ou null se o programa não existe nem no PC nem nos plugins.
     */
    public static List<String> resolver(List<String> comando) {
        if (comando.isEmpty()) return comando;
        String prog = comando.get(0);
        File achado = acharPrograma(prog);
        if (achado == null) return null;
        List<String> r = new ArrayList<>();
        String nome = achado.getName().toLowerCase();
        if (WINDOWS && (nome.endsWith(".cmd") || nome.endsWith(".bat"))) {
            r.add("cmd.exe");
            r.add("/c");
        }
        r.add(achado.getAbsolutePath());
        r.addAll(comando.subList(1, comando.size()));
        return r;
    }

    /** Acha o arquivo do programa. */
    public static File acharPrograma(String prog) {
        File direto = new File(prog);
        if (direto.isAbsolute() || prog.contains("/") || prog.contains("\\")) {
            if (direto.isFile()) return direto;
            for (String ext : extensoes()) if (new File(prog + ext).isFile()) return new File(prog + ext);
            return null;
        }
        // 1) no PC
        File noPc = procurar(prog, System.getenv().getOrDefault(WINDOWS ? chavePath() : "PATH", ""));
        boolean pythonFalso = noPc != null && noPc.getAbsolutePath().toLowerCase().contains("\\windowsapps\\")
                && prog.toLowerCase().startsWith("python");
        if (noPc != null && !pythonFalso) return noPc;
        // 2) nos plugins
        StringBuilder pastas = new StringBuilder();
        for (File d : Plugins.pastasInstaladas()) pastas.append(d.getAbsolutePath()).append(File.pathSeparator);
        File noPlugin = procurar(prog, pastas.toString());
        if (noPlugin == null) {
            Plugins.Plugin p = Plugins.quemTem(prog);
            if (p != null && p.apelidos.containsKey(prog)) noPlugin = procurar(p.apelidos.get(prog), pastas.toString());
            if (noPlugin == null && prog.equals("python3")) noPlugin = procurar("python", pastas.toString());
        }
        if (noPlugin != null) return noPlugin;
        // 3) No Linux o Python se chama "python3" (e o pip, "pip3")
        if (noPc == null && !WINDOWS) {
            String outro = prog.equals("python") ? "python3" : prog.equals("pip") ? "pip3" : null;
            if (outro != null) return procurar(outro, System.getenv().getOrDefault("PATH", ""));
        }
        return noPc;   // se só tinha o python falso, devolve ele mesmo (melhor que nada)
    }

    /** O programa existe no PC (sem contar plugins)? */
    public static boolean noPc(String prog) {
        File f = procurar(prog, System.getenv().getOrDefault(WINDOWS ? chavePath() : "PATH", ""));
        return f != null && !(f.getAbsolutePath().toLowerCase().contains("\\windowsapps\\") && prog.startsWith("python"));
    }

    private static File procurar(String prog, String path) {
        List<String> exts = new ArrayList<>(extensoes());
        if (prog.contains(".")) exts.add(0, "");   // "cmd.exe" já tem extensão
        for (String pasta : path.split(File.pathSeparator)) {
            if (pasta.isEmpty()) continue;
            for (String ext : exts) {
                File f = new File(pasta.replace("\"", ""), prog + ext);
                if (f.isFile()) return f;
            }
        }
        return null;
    }

    private static List<String> extensoes() {
        if (!WINDOWS) return Arrays.asList("");
        List<String> l = new ArrayList<>(Arrays.asList("", ".exe", ".cmd", ".bat", ".com"));
        String pathext = System.getenv("PATHEXT");
        if (pathext != null) for (String e : pathext.toLowerCase().split(";")) if (!e.isEmpty() && !l.contains(e)) l.add(e);
        // "" no Windows só vale se o arquivo tiver extensão; senão é um arquivo sem extensão (não roda)
        l.remove("");
        return l;
    }

    // =====================================================================
    //  QUAL LINUX É ESTE? (pra mostrar o comando certo de instalar)
    // =====================================================================

    private static java.util.Map<String, String> osRelease;

    private static java.util.Map<String, String> lerOsRelease() {
        if (osRelease == null) {
            osRelease = new java.util.HashMap<>();
            try {
                for (String l : java.nio.file.Files.readAllLines(java.nio.file.Paths.get("/etc/os-release"))) {
                    int i = l.indexOf('=');
                    if (i > 0) osRelease.put(l.substring(0, i), l.substring(i + 1).replace("\"", ""));
                }
            } catch (Exception ignorado) { }
        }
        return osRelease;
    }

    /** "debian" (Ubuntu, Mint, Kali, Pop!_OS, Zorin, Winux...), "fedora", "suse" ou "arch" (Arch, Manjaro...). */
    public static String familiaLinux() {
        java.util.Map<String, String> o = lerOsRelease();
        String tudo = (o.getOrDefault("ID", "") + " " + o.getOrDefault("ID_LIKE", "")).toLowerCase();
        if (tudo.contains("arch")) return "arch";
        if (tudo.contains("suse")) return "suse";
        if (tudo.contains("fedora") || tudo.contains("rhel") || tudo.contains("centos")) return "fedora";
        return "debian";
    }

    /** O nome bonitinho do Linux (ex.: "Ubuntu 24.04 LTS", "Fedora Linux 41"). */
    public static String nomeLinux() {
        java.util.Map<String, String> o = lerOsRelease();
        return o.getOrDefault("PRETTY_NAME", o.getOrDefault("NAME", "Linux"));
    }
}
