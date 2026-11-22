package tazzcode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * PLUGINS: linguagens "portáteis" que o TazzCode baixa e guarda na pasta dele
 * (C:\Users\SeuNome\TazzCode\plugins). Assim dá pra rodar Python, Node, Go... SEM instalar no PC!
 *
 * Como funciona:
 *   1) o plugin descobre na internet qual é a versão mais nova (cada site tem uma lista em JSON)
 *   2) baixa o .zip
 *   3) extrai na pasta do plugin
 *   4) quando você aperta F5, se o programa não estiver instalado no PC, o TazzCode usa o do plugin
 *
 * Se a linguagem estiver instalada no PC, ela tem prioridade (o plugin é o "plano B").
 */
public class Plugins {

    /** Diz o progresso do download: (texto, 0 a 100; -1 = sem porcentagem). */
    public interface Progresso {
        void mostrar(String texto, int porcento);
        boolean cancelado();
    }

    /** Descobre o link do download (pode precisar consultar a internet). */
    interface Descobridor {
        String url(HttpClient http) throws Exception;
    }

    public static class Plugin {
        public final String id, nome, descricao;
        public final List<String> linguagens;          // nomes do Catalogo que esse plugin faz rodar
        public final List<String> comandos;            // programas que ele traz (python, pip, node, npm...)
        public final List<String> pastasDeProgramas;   // onde estão os .exe dentro da pasta do plugin
        public String tamanho = "";
        public String depende = "";                    // outro plugin que precisa junto (ex.: Kotlin precisa do JDK)
        public final Map<String, String> apelidos = new LinkedHashMap<>();   // ex.: make -> mingw32-make
        /** Variáveis de ambiente que o plugin precisa ({pasta} = a pasta do plugin). Ex.: RUSTUP_HOME. */
        public final Map<String, String> ambiente = new LinkedHashMap<>();
        /**
         * Se o download for um INSTALADOR (.exe) em vez de .zip: os argumentos pra ele instalar
         * sozinho, sem perguntar nada, dentro da pasta do plugin ({pasta}).
         *   Inno Setup: /VERYSILENT /DIR={pasta}      NSIS: /S /D={pasta}      7-Zip: -o{pasta} -y
         */
        public String instalador;
        /** NSIS não aceita aspas no /D=, então ele roda por um .bat. */
        public boolean viaBat;
        /** Se o download for um programa .exe sozinho (sem zip): o nome com que ele é salvo. */
        public String arquivoUnico;
        Descobridor descobridor;
        PosInstalacao depois;

        Plugin(String id, String nome, String descricao, String linguagens, String comandos, String pastas) {
            this.id = id;
            this.nome = nome;
            this.descricao = descricao;
            this.linguagens = Arrays.asList(linguagens.split("\\|"));
            this.comandos = Arrays.asList(comandos.split(" "));
            this.pastasDeProgramas = Arrays.asList(pastas.split(" "));
        }

        public File pasta() { return Pastas.plugins().resolve(id).toFile(); }
        public boolean instalado() { return new File(pasta(), ".instalado").isFile(); }
        @Override public String toString() { return nome; }
    }

    interface PosInstalacao {
        void rodar(Plugin p, HttpClient http, Progresso prog) throws Exception;
    }

    private static final List<Plugin> TODOS = new ArrayList<>();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS).connectTimeout(Duration.ofSeconds(20)).build();

    static {
        Plugin p;

        p = add(new Plugin("python", "Python", "Python 3 portátil (versão \"embutível\" oficial do python.org) + pip. "
                + "Obs.: não vem com Tkinter/Turtle (pra isso, instale no PC).",
                "Python|Mojo", "python pip", ". Scripts"));
        p.tamanho = "~30 MB";
        p.descobridor = http -> {
            // Lista as pastas de versão do python.org e acha a mais nova que tem o .zip "embed" pro Windows
            String lista = texto(http, "https://www.python.org/ftp/python/");
            for (String menor : new String[]{"3.13", "3.12"}) {
                List<int[]> versoes = new ArrayList<>();
                Matcher m = Pattern.compile("href=\"" + Pattern.quote(menor) + "\\.(\\d+)/\"").matcher(lista);
                while (m.find()) versoes.add(new int[]{Integer.parseInt(m.group(1))});
                versoes.sort((a, b) -> b[0] - a[0]);
                for (int[] v : versoes) {
                    String ver = menor + "." + v[0];
                    String url = "https://www.python.org/ftp/python/" + ver + "/python-" + ver + "-embed-amd64.zip";
                    if (existe(http, url)) return url;
                }
            }
            throw new IOException("não achei o Python embutível no python.org");
        };
        p.depois = (pl, http, prog) -> {
            // O Python "embutível" vem sem o pip. Liga o "import site" e instala o pip.
            File[] pth = pl.pasta().listFiles((d, n) -> n.endsWith("._pth"));
            if (pth != null) for (File f : pth) {
                String t = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                t = t.replace("#import site", "import site");
                if (!t.contains("Lib\\site-packages")) t += "\nLib\\site-packages\n";
                Files.write(f.toPath(), t.getBytes(StandardCharsets.UTF_8));
            }
            prog.mostrar("Instalando o pip...", -1);
            File getPip = new File(pl.pasta(), "get-pip.py");
            baixar(http, "https://bootstrap.pypa.io/get-pip.py", getPip, prog);
            rodarAgora(pl.pasta(), new File(pl.pasta(), "python.exe").getAbsolutePath(), "get-pip.py", "--no-warn-script-location");
        };

        p = add(new Plugin("node", "Node.js", "Node.js LTS portátil (traz node, npm e npx). Faz rodar JavaScript, TypeScript (tsx), "
                + "CoffeeScript, EJS, Sass e os modelos de projeto Node/React grandes.",
                "JavaScript|TypeScript|CoffeeScript|EJS (Embedded JavaScript)", "node npm npx", "."));
        p.tamanho = "~30 MB";
        p.descobridor = http -> {
            Object lista = Json.ler(texto(http, "https://nodejs.org/dist/index.json"));
            for (Object o : (List<?>) lista) {
                Map<?, ?> v = (Map<?, ?>) o;
                if (!Boolean.FALSE.equals(v.get("lts"))) {
                    String ver = String.valueOf(v.get("version"));
                    return "https://nodejs.org/dist/" + ver + "/node-" + ver + "-win-x64.zip";
                }
            }
            throw new IOException("não achei a versão LTS do Node");
        };

        p = add(new Plugin("jdk", "Java (JDK 21)", "JDK 21 portátil (Eclipse Temurin). Traz java, javac e jshell.",
                "Java|JShell (Java interativo)|Groovy|Scala", "java javac jshell jar", "bin"));
        p.tamanho = "~190 MB";
        p.ambiente.put("JAVA_HOME", "{pasta}");   // o Maven, o Kotlin e o Groovy procuram o Java por aqui
        p.descobridor = http -> "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse";

        p = add(new Plugin("go", "Go", "Go portátil oficial (go.dev).", "Go", "go gofmt", "bin"));
        p.tamanho = "~80 MB";
        p.descobridor = http -> {
            Object lista = Json.ler(texto(http, "https://go.dev/dl/?mode=json"));
            Map<?, ?> nova = (Map<?, ?>) ((List<?>) lista).get(0);
            for (Object f : (List<?>) nova.get("files")) {
                Map<?, ?> a = (Map<?, ?>) f;
                if ("windows".equals(a.get("os")) && "amd64".equals(a.get("arch")) && "archive".equals(a.get("kind"))) {
                    return "https://go.dev/dl/" + a.get("filename");
                }
            }
            throw new IOException("não achei o Go pra Windows");
        };

        p = add(new Plugin("deno", "Deno", "Deno: roda JavaScript e TypeScript (um programa só, bem levinho).",
                "JavaScript|TypeScript", "deno", "."));
        p.tamanho = "~40 MB";
        p.descobridor = http -> "https://github.com/denoland/deno/releases/latest/download/deno-x86_64-pc-windows-msvc.zip";

        p = add(new Plugin("bun", "Bun", "Bun: roda JavaScript e TypeScript bem rápido.", "JavaScript|TypeScript", "bun", "."));
        p.tamanho = "~40 MB";
        p.descobridor = http -> "https://github.com/oven-sh/bun/releases/latest/download/bun-windows-x64.zip";

        p = add(new Plugin("php", "PHP", "PHP portátil oficial (windows.php.net). Roda .php e os modelos PHP Web.", "PHP", "php", "."));
        p.tamanho = "~30 MB";
        p.descobridor = http -> {
            Map<?, ?> rel = (Map<?, ?>) Json.ler(texto(http, "https://windows.php.net/downloads/releases/releases.json"));
            String melhor = null;
            for (Object k : rel.keySet()) if (melhor == null || compararVersao(k.toString(), melhor) > 0) melhor = k.toString();
            Map<?, ?> v = (Map<?, ?>) rel.get(melhor);
            for (Object k : v.keySet()) {
                String chave = k.toString();
                if (chave.startsWith("nts-") && chave.endsWith("-x64") && v.get(k) instanceof Map) {
                    Map<?, ?> zip = (Map<?, ?>) ((Map<?, ?>) v.get(k)).get("zip");
                    if (zip != null) return "https://windows.php.net/downloads/releases/" + zip.get("path");
                }
            }
            throw new IOException("não achei o PHP pra Windows");
        };

        p = add(new Plugin("gcc", "C / C++ / Fortran (GCC)", "Compiladores GCC portáteis (WinLibs/MinGW-w64): gcc, g++, gfortran e make. "
                + "Faz rodar C, C++, Fortran e Assembly (com gcc).",
                "C|C++|Fortran|Objective-C|Makefile", "gcc g++ gfortran mingw32-make gdb", "bin"));
        p.tamanho = "~250 MB";
        p.apelidos.put("make", "mingw32-make");
        p.descobridor = http -> {
            Map<?, ?> rel = (Map<?, ?>) Json.ler(texto(http, "https://api.github.com/repos/brechtsanders/winlibs_mingw/releases/latest"));
            for (Object o : (List<?>) rel.get("assets")) {
                Map<?, ?> a = (Map<?, ?>) o;
                String nome = String.valueOf(a.get("name"));
                if (nome.matches("winlibs-x86_64-posix-seh-gcc-.*-mingw-w64ucrt-.*\\.zip") && !nome.contains("llvm")) {
                    return String.valueOf(a.get("browser_download_url"));
                }
            }
            throw new IOException("não achei o GCC no WinLibs");
        };

        p = add(new Plugin("zig", "Zig", "Zig portátil (ziglang.org). O Zig também compila C!", "Zig", "zig", "."));
        p.tamanho = "~80 MB";
        p.descobridor = http -> {
            Map<?, ?> idx = (Map<?, ?>) Json.ler(texto(http, "https://ziglang.org/download/index.json"));
            String melhor = null;
            for (Object k : idx.keySet()) if (!k.toString().equals("master") && (melhor == null || compararVersao(k.toString(), melhor) > 0)) melhor = k.toString();
            Map<?, ?> win = (Map<?, ?>) ((Map<?, ?>) idx.get(melhor)).get("x86_64-windows");
            return String.valueOf(win.get("tarball"));
        };

        p = add(new Plugin("julia", "Julia", "Julia portátil (julialang.org).", "Julia", "julia", "bin"));
        p.tamanho = "~200 MB";
        p.descobridor = http -> {
            Map<?, ?> vs = (Map<?, ?>) Json.ler(texto(http, "https://julialang-s3.julialang.org/bin/versions.json"));
            String melhor = null;
            for (Object k : vs.keySet()) {
                Map<?, ?> v = (Map<?, ?>) vs.get(k);
                if (Boolean.TRUE.equals(v.get("stable")) && (melhor == null || compararVersao(k.toString(), melhor) > 0)) melhor = k.toString();
            }
            for (Object f : (List<?>) ((Map<?, ?>) vs.get(melhor)).get("files")) {
                Map<?, ?> a = (Map<?, ?>) f;
                if ("winnt".equals(a.get("os")) && "x86_64".equals(a.get("arch")) && "zip".equals(a.get("extension"))) return String.valueOf(a.get("url"));
            }
            throw new IOException("não achei a Julia pra Windows");
        };

        p = add(new Plugin("kotlin", "Kotlin", "Compilador Kotlin (precisa de Java: instale o plugin do JDK também, se não tiver Java no PC).",
                "Kotlin", "kotlinc kotlin", "bin"));
        p.tamanho = "~80 MB";
        p.depende = "jdk";
        p.descobridor = http -> {
            Map<?, ?> rel = (Map<?, ?>) Json.ler(texto(http, "https://api.github.com/repos/JetBrains/kotlin/releases/latest"));
            for (Object o : (List<?>) rel.get("assets")) {
                Map<?, ?> a = (Map<?, ?>) o;
                if (String.valueOf(a.get("name")).matches("kotlin-compiler-[\\d.]+\\.zip")) return String.valueOf(a.get("browser_download_url"));
            }
            throw new IOException("não achei o compilador Kotlin");
        };

        p = add(new Plugin("dart", "Dart", "Dart SDK portátil (dart.dev).", "Dart", "dart", "bin"));
        p.tamanho = "~200 MB";
        p.descobridor = http -> "https://storage.googleapis.com/dart-archive/channels/stable/release/latest/sdk/dartsdk-windows-x64-release.zip";

        p = add(new Plugin("dotnet", ".NET SDK (C# e F#)", ".NET SDK portátil (pelo instalador oficial dotnet-install). Faz rodar C# (.cs) e F# (.fsx).",
                "C#|F#|Visual Basic .NET", "dotnet", "."));
        p.tamanho = "~250 MB";
        p.descobridor = http -> "script";   // esse não é .zip: usa o script oficial da Microsoft
        p.depois = (pl, http, prog) -> {
            File script = new File(pl.pasta(), "dotnet-install.ps1");
            baixar(http, "https://dot.net/v1/dotnet-install.ps1", script, prog);
            prog.mostrar("Instalando o .NET (isso demora uns minutos)...", -1);
            rodarAgora(pl.pasta(), "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", script.getAbsolutePath(),
                    "-Channel", "LTS", "-InstallDir", pl.pasta().getAbsolutePath());
        };

        // =================== MAIS PLUGINS (v0.6) ===================

        p = add(new Plugin("lua", "Lua", "Lua 5.4 portátil (LuaBinaries).", "Lua", "lua54 luac54", "."));
        p.tamanho = "~1 MB";
        p.apelidos.put("lua", "lua54");
        p.apelidos.put("luac", "luac54");
        p.descobridor = http -> "https://downloads.sourceforge.net/project/luabinaries/5.4.6/Tools%20Executables/lua-5.4.6_Win64_bin.zip";

        p = add(new Plugin("ruby", "Ruby", "Ruby (RubyInstaller), instalado só na pasta do TazzCode.", "Ruby", "ruby irb gem", "bin"));
        p.tamanho = "~40 MB";
        p.instalador = "/VERYSILENT /SUPPRESSMSGBOXES /NORESTART /CURRENTUSER /TASKS= /DIR={pasta}";
        p.descobridor = http -> github(http, "oneclick/rubyinstaller2", "rubyinstaller-[\\d.]+-\\d+-x64\\.exe");

        p = add(new Plugin("rust", "Rust", "Rust (rustup) com o compilador \"gnu\" (não precisa do Visual Studio). Fica todo dentro da pasta do TazzCode.",
                "Rust", "rustc cargo rustup", "cargo/bin"));
        p.tamanho = "~300 MB";
        p.ambiente.put("RUSTUP_HOME", "{pasta}/rustup");
        p.ambiente.put("CARGO_HOME", "{pasta}/cargo");
        p.descobridor = http -> "script";
        p.depois = (pl, http, prog) -> {
            File init = new File(pl.pasta(), "rustup-init.exe");
            baixar(http, "https://static.rust-lang.org/rustup/dist/x86_64-pc-windows-msvc/rustup-init.exe", init, prog);
            prog.mostrar("Instalando o Rust (demora uns minutos)...", -1);
            ProcessBuilder pb = new ProcessBuilder(init.getAbsolutePath(), "-y", "--no-modify-path", "--profile", "minimal",
                    "--default-host", "x86_64-pc-windows-gnu", "--default-toolchain", "stable").directory(pl.pasta()).redirectErrorStream(true);
            pb.environment().put("RUSTUP_HOME", new File(pl.pasta(), "rustup").getAbsolutePath());
            pb.environment().put("CARGO_HOME", new File(pl.pasta(), "cargo").getAbsolutePath());
            Process pr = pb.start();
            byte[] saida = pr.getInputStream().readAllBytes();
            if (pr.waitFor() != 0) throw new IOException("o rustup falhou: " + new String(saida, StandardCharsets.UTF_8).trim());
            Files.deleteIfExists(init.toPath());
        };

        p = add(new Plugin("perl", "Perl", "Strawberry Perl portátil (traz o Perl e mais um gcc).", "Perl", "perl cpan", "perl/bin c/bin"));
        p.tamanho = "~150 MB";
        p.descobridor = http -> github(http, "StrawberryPerl/Perl-Dist-Strawberry", "strawberry-perl-[\\d.]+-64bit-portable\\.zip");

        p = add(new Plugin("r", "R", "R (CRAN), instalado só na pasta do TazzCode.", "R", "R Rscript", "bin"));
        p.tamanho = "~90 MB";
        p.instalador = "/VERYSILENT /SUPPRESSMSGBOXES /NORESTART /CURRENTUSER /DIR={pasta}";
        p.descobridor = http -> {
            String pag = texto(http, "https://cloud.r-project.org/bin/windows/base/");
            Matcher m = Pattern.compile("(R-[\\d.]+-win\\.exe)").matcher(pag);
            if (!m.find()) throw new IOException("não achei o R pra Windows");
            return "https://cloud.r-project.org/bin/windows/base/" + m.group(1);
        };

        p = add(new Plugin("octave", "GNU Octave (MATLAB grátis)", "GNU Octave portátil: roda os arquivos .m do MATLAB.",
                "MATLAB", "octave-cli octave", "mingw64/bin ucrt64/bin clang64/bin"));
        p.tamanho = "~600 MB";
        p.descobridor = http -> {
            String v = maiorVersao(texto(http, "https://ftp.gnu.org/gnu/octave/windows/"), "octave-([\\d.]+)-w64\\.zip\"");
            return "https://ftp.gnu.org/gnu/octave/windows/octave-" + v + "-w64.zip";
        };

        p = add(new Plugin("racket", "Racket (e Scheme)", "Racket, instalado só na pasta do TazzCode. Também roda Scheme.",
                "Racket|Scheme", "racket raco", "."));
        p.tamanho = "~170 MB";
        p.instalador = "/S /D={pasta}";
        p.viaBat = true;
        p.apelidos.put("scheme", "racket");
        p.descobridor = http -> {
            String v = maiorVersao(texto(http, "https://download.racket-lang.org/"), "[Vv]ersion (\\d+\\.\\d+)");
            return "https://download.racket-lang.org/installers/" + v + "/racket-" + v + "-x86_64-win32-cs.exe";
        };

        p = add(new Plugin("prolog", "SWI-Prolog", "SWI-Prolog, instalado só na pasta do TazzCode.", "Prolog", "swipl", "bin"));
        p.tamanho = "~30 MB";
        p.instalador = "/S /D={pasta}";
        p.viaBat = true;
        p.descobridor = http -> {
            String v = maiorVersao(texto(http, "https://www.swi-prolog.org/download/stable"), "swipl-([\\d.]+-\\d+)\\.x64\\.exe");
            return "https://www.swi-prolog.org/download/stable/bin/swipl-" + v + ".x64.exe";
        };

        p = add(new Plugin("erlang", "Erlang/OTP", "Erlang/OTP. Ele também é a base do Elixir e do Gleam.", "Erlang", "erl escript erlc", "bin"));
        p.tamanho = "~120 MB";
        p.instalador = "/S /D={pasta}";
        p.viaBat = true;
        p.descobridor = http -> github(http, "erlang/otp", "otp_win64_[\\d.]+\\.exe");

        p = add(new Plugin("elixir", "Elixir", "Elixir (precisa do Erlang: o TazzCode instala junto se faltar).", "Elixir", "elixir elixirc iex mix", "bin"));
        p.tamanho = "~10 MB";
        p.depende = "erlang";
        p.descobridor = http -> github(http, "elixir-lang/elixir", "elixir-otp-\\d+\\.zip");

        p = add(new Plugin("gleam", "Gleam", "Gleam (precisa do Erlang: o TazzCode instala junto se faltar).", "Gleam", "gleam", "."));
        p.tamanho = "~15 MB";
        p.depende = "erlang";
        p.descobridor = http -> github(http, "gleam-lang/gleam", "gleam-v[\\d.]+-x86_64-pc-windows-msvc\\.zip");

        p = add(new Plugin("fpc", "Free Pascal", "Free Pascal (o compilador do Pascal/Delphi), instalado só na pasta do TazzCode.",
                "Delphi / Object Pascal", "fpc", "bin/i386-win32"));
        p.tamanho = "~80 MB";
        p.instalador = "/VERYSILENT /SUPPRESSMSGBOXES /NORESTART /CURRENTUSER /DIR={pasta}";
        p.descobridor = http -> "https://downloads.sourceforge.net/project/freepascal/Win32/3.2.2/fpc-3.2.2.win32.and.win64.exe";

        p = add(new Plugin("nim", "Nim", "Nim portátil (precisa do GCC: o TazzCode instala junto se faltar).", "Nim", "nim nimble", "bin"));
        p.tamanho = "~50 MB";
        p.depende = "gcc";
        p.descobridor = http -> {
            String v = maiorVersao(texto(http, "https://nim-lang.org/install_windows.html"), "nim-([\\d.]+)_x64\\.zip");
            return "https://nim-lang.org/download/nim-" + v + "_x64.zip";
        };

        p = add(new Plugin("haxe", "Haxe", "Haxe portátil.", "Haxe", "haxe haxelib", "."));
        p.tamanho = "~10 MB";
        p.descobridor = http -> github(http, "HaxeFoundation/haxe", "haxe-[\\d.]+-win64\\.zip");

        p = add(new Plugin("v", "V", "Linguagem V portátil (já vem com um compilador de C pequenininho).", "V", "v", "."));
        p.tamanho = "~10 MB";
        p.descobridor = http -> github(http, "vlang/v", "v_windows\\.zip");

        p = add(new Plugin("scala", "Scala", "Scala CLI portátil (o comando \"scala\" do Scala 3).", "Scala", "scala-cli", "."));
        p.tamanho = "~70 MB";
        p.apelidos.put("scala", "scala-cli");
        p.descobridor = http -> github(http, "VirtusLab/scala-cli", "scala-cli-x86_64-pc-win32\\.zip");

        p = add(new Plugin("groovy", "Groovy", "Apache Groovy (precisa de Java: o TazzCode instala o JDK junto se faltar).", "Groovy", "groovy groovyc", "bin"));
        p.tamanho = "~30 MB";
        p.depende = "jdk";
        p.descobridor = http -> {
            String v = maiorVersao(texto(http, "https://archive.apache.org/dist/groovy/"), "href=\"(4\\.\\d+\\.\\d+)/\"");
            return "https://archive.apache.org/dist/groovy/" + v + "/distribution/apache-groovy-binary-" + v + ".zip";
        };

        p = add(new Plugin("maven", "Maven (projetos Java)", "Apache Maven: faz rodar os modelos Spring Boot, JavaFX e Javalin. "
                + "(Precisa de Java: o TazzCode instala o JDK junto se faltar.)", "Java", "mvn", "bin"));
        p.tamanho = "~10 MB";
        p.depende = "jdk";
        p.descobridor = http -> {
            String v = maiorVersao(texto(http, "https://archive.apache.org/dist/maven/maven-3/"), "href=\"(3\\.\\d+\\.\\d+)/\"");
            return "https://archive.apache.org/dist/maven/maven-3/" + v + "/binaries/apache-maven-" + v + "-bin.zip";
        };

        p = add(new Plugin("dmd", "D (DMD)", "Compilador D oficial (traz o dmd e o rdmd).", "D", "dmd rdmd", "windows/bin64 windows/bin"));
        p.tamanho = "~50 MB";
        p.descobridor = http -> {
            String v = texto(http, "https://downloads.dlang.org/releases/LATEST").trim();
            return "https://downloads.dlang.org/releases/2.x/" + v + "/dmd." + v + ".windows.zip";
        };

        p = add(new Plugin("git", "Git Bash (bash, sh, awk)", "Git for Windows portátil: traz o bash, o sh, o awk, o sed e o git. "
                + "Faz rodar Shell/Bash, sh e AWK.", "Shell / Bash|AWK", "bash sh awk sed git", "bin usr/bin"));
        p.tamanho = "~60 MB";
        p.instalador = "-o\"{pasta}\" -y";
        p.viaBat = true;
        p.descobridor = http -> github(http, "git-for-windows/git", "PortableGit-[\\d.]+-64-bit\\.7z\\.exe");

        // =================== AINDA MAIS PLUGINS (v0.6) ===================

        p = add(new Plugin("nasm", "NASM (Assembly)", "Montador NASM. Pra virar programa, também precisa do GCC (o TazzCode instala junto).",
                "Assembly (NASM)", "nasm ndisasm", "."));
        p.tamanho = "~1 MB";
        p.depende = "gcc";
        p.descobridor = http -> {
            String v = maiorVersao(texto(http, "https://www.nasm.us/pub/nasm/releasebuilds/"), "href=\"(\\d+\\.\\d+(?:\\.\\d+)?)/\"");
            return "https://www.nasm.us/pub/nasm/releasebuilds/" + v + "/win64/nasm-" + v + "-win64.zip";
        };

        p = add(new Plugin("ahk", "AutoHotkey", "AutoHotkey v2 portátil.", "AutoHotkey", "AutoHotkey64 AutoHotkey32", "."));
        p.tamanho = "~3 MB";
        p.descobridor = http -> "https://www.autohotkey.com/download/ahk-v2.zip";

        p = add(new Plugin("autoit", "AutoIt", "AutoIt v3 portátil.", "AutoIt", "AutoIt3 AutoIt3_x64", "."));
        p.tamanho = "~15 MB";
        p.descobridor = http -> "https://www.autoitscript.com/files/autoit3/autoit-v3.zip";

        p = add(new Plugin("ballerina", "Ballerina", "Ballerina portátil (já vem com o Java dele).", "Ballerina", "bal", "bin"));
        p.tamanho = "~250 MB";
        p.descobridor = http -> github(http, "ballerina-platform/ballerina-distribution", "ballerina-[\\d.]+-swan-lake\\.zip");

        p = add(new Plugin("freebasic", "FreeBASIC", "Compilador FreeBASIC portátil.", "BASIC (FreeBASIC)", "fbc fbc64", "."));
        p.tamanho = "~70 MB";
        p.apelidos.put("fbc", "fbc64");
        p.descobridor = http -> "https://downloads.sourceforge.net/project/fbc/FreeBASIC-1.10.1/Binaries-Windows/FreeBASIC-1.10.1-win64.zip";

        p = add(new Plugin("babashka", "Babashka (Clojure)", "Babashka: roda Clojure na hora, sem precisar de Java. No TazzCode, escolha \"Babashka\" no \"com:\" da barra.",
                "Clojure", "bb", "."));
        p.tamanho = "~30 MB";
        p.descobridor = http -> github(http, "babashka/babashka", "babashka-[\\d.]+-windows-amd64\\.zip");

        p = add(new Plugin("cmake", "CMake", "CMake portátil.", "CMake", "cmake ctest", "bin"));
        p.tamanho = "~45 MB";
        p.descobridor = http -> github(http, "Kitware/CMake", "cmake-[\\d.]+-windows-x86_64\\.zip");

        p = add(new Plugin("ispc", "ISPC", "Compilador ISPC (Intel) portátil.", "ISPC", "ispc", "bin"));
        p.tamanho = "~30 MB";
        p.descobridor = http -> github(http, "ispc/ispc", "ispc-v[\\d.]+-windows\\.zip");

        p = add(new Plugin("lean", "Lean 4", "Lean 4 portátil.", "Lean", "lean lake", "bin"));
        p.tamanho = "~250 MB";
        p.descobridor = http -> github(http, "leanprover/lean4", "lean-[\\d.]+-windows\\.zip");

        p = add(new Plugin("processing", "Processing", "Processing portátil (desenho e animação com código).", "Processing", "processing-java Processing", "."));
        p.tamanho = "~200 MB";
        p.descobridor = http -> github(http, "processing/processing4", "processing-[\\d.]+-windows-x64\\.zip");

        p = add(new Plugin("raku", "Raku", "Rakudo (Raku) portátil.", "Raku", "raku rakudo", "bin"));
        p.tamanho = "~40 MB";
        p.descobridor = http -> github(http, "rakudo/rakudo", "rakudo-moar-[\\d.\\-]+-win-x86_64-msvc\\.zip");

        p = add(new Plugin("solc", "Solidity (solc)", "Compilador Solidity (um programa só).", "Solidity", "solc", "."));
        p.tamanho = "~10 MB";
        p.arquivoUnico = "solc.exe";
        p.descobridor = http -> github(http, "ethereum/solidity", "solc-windows\\.exe");

        p = add(new Plugin("terraform", "Terraform", "Terraform portátil (um programa só).", "Terraform (HCL)", "terraform", "."));
        p.tamanho = "~30 MB";
        p.descobridor = http -> {
            Map<?, ?> info = (Map<?, ?>) Json.ler(texto(http, "https://checkpoint-api.hashicorp.com/v1/check/terraform"));
            String v = String.valueOf(info.get("current_version"));
            return "https://releases.hashicorp.com/terraform/" + v + "/terraform_" + v + "_windows_amd64.zip";
        };

        p = add(new Plugin("ghdl", "GHDL (VHDL)", "Simulador GHDL portátil pra VHDL.", "VHDL", "ghdl", "bin"));
        p.tamanho = "~60 MB";
        p.descobridor = http -> github(http, "ghdl/ghdl", "ghdl-mcode-[\\d.]+-ucrt64\\.zip");

        p = add(new Plugin("wasmtime", "Wasmtime (WebAssembly)", "Wasmtime: roda WebAssembly (.wat) na hora.", "WebAssembly (texto)", "wasmtime", "."));
        p.tamanho = "~20 MB";
        p.descobridor = http -> github(http, "bytecodealliance/wasmtime", "wasmtime-v[\\d.]+-x86_64-windows\\.zip");

        p = add(new Plugin("arduino", "Arduino CLI", "Arduino CLI portátil (compila os .ino). Na 1ª vez, rode no Terminal: arduino-cli core install arduino:avr",
                "Arduino", "arduino-cli", "."));
        p.tamanho = "~30 MB";
        p.descobridor = http -> github(http, "arduino/arduino-cli", "arduino-cli_[\\d.]+_Windows_64bit\\.zip");
    }

    private static Plugin add(Plugin p) {
        TODOS.add(p);
        return p;
    }

    public static List<Plugin> todos() {
        if (!Executaveis.WINDOWS) return new ArrayList<>();   // plugins são de Windows; no Linux usa o apt
        return TODOS;
    }

    public static Plugin porId(String id) {
        for (Plugin p : TODOS) if (p.id.equals(id)) return p;
        return null;
    }

    /** Qual plugin traz esse programa (ex.: "npx" -> Node.js)? */
    public static Plugin quemTem(String comando) {
        String c = semExtensao(new File(comando).getName()).toLowerCase();
        for (Plugin p : TODOS) {
            for (String cmd : p.comandos) if (cmd.equalsIgnoreCase(c)) return p;
            for (String ap : p.apelidos.keySet()) if (ap.equalsIgnoreCase(c)) return p;
        }
        if (c.equals("py") || c.equals("python3")) return porId("python");
        return null;
    }

    /** Plugins que fazem uma linguagem rodar. */
    public static List<Plugin> paraLinguagem(String nomeLinguagem) {
        List<Plugin> l = new ArrayList<>();
        for (Plugin p : TODOS) if (p.linguagens.contains(nomeLinguagem)) l.add(p);
        return l;
    }

    /** As pastas com programas de todos os plugins instalados (vão pro PATH). */
    public static List<File> pastasInstaladas() {
        List<File> l = new ArrayList<>();
        for (Plugin p : TODOS) {
            if (!p.instalado()) continue;
            for (String sub : p.pastasDeProgramas) {
                File d = sub.equals(".") ? p.pasta() : new File(p.pasta(), sub);
                if (d.isDirectory()) l.add(d);
            }
        }
        return l;
    }

    // =====================================================================
    //  INSTALAR / REMOVER
    // =====================================================================

    public static void instalar(Plugin p, Progresso prog) throws Exception {
        File pasta = p.pasta();
        apagar(pasta);
        pasta.mkdirs();
        try {
            prog.mostrar("Procurando a versão mais nova de " + p.nome + "...", -1);
            String url = p.descobridor.url(HTTP);
            if (p.arquivoUnico != null) {
                baixar(HTTP, url, new File(pasta, p.arquivoUnico), prog);
            } else if (p.instalador != null) {
                File exe = new File(pasta.getParentFile(), p.id + "-instalador.exe");
                baixar(HTTP, url, exe, prog);
                if (prog.cancelado()) throw new IOException("cancelado");
                prog.mostrar("Instalando " + p.nome + " (pode aparecer uma janela do Windows pedindo permissão)...", -1);
                rodarInstalador(p, exe);
                Files.deleteIfExists(exe.toPath());
            } else if (!url.equals("script")) {
                File zip = new File(pasta.getParentFile(), p.id + ".zip");
                baixar(HTTP, url, zip, prog);
                if (prog.cancelado()) throw new IOException("cancelado");
                prog.mostrar("Extraindo...", -1);
                extrairZip(zip, pasta);
                Files.deleteIfExists(zip.toPath());
            }
            if (p.depois != null) p.depois.rodar(p, HTTP, prog);
            Files.write(new File(pasta, ".instalado").toPath(), url.getBytes(StandardCharsets.UTF_8));
            prog.mostrar(p.nome + " instalado!", 100);
        } catch (Exception e) {
            apagar(pasta);   // não deixa plugin pela metade
            throw e;
        }
    }

    public static void remover(Plugin p) {
        apagar(p.pasta());
    }

    // =====================================================================
    //  AJUDANTES DE INTERNET E ARQUIVOS
    // =====================================================================

    static String texto(HttpClient http, String url) throws Exception {
        HttpResponse<String> r = http.send(pedido(url).GET().build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (r.statusCode() != 200) throw new IOException("o site respondeu " + r.statusCode() + " (" + url + ")");
        return r.body();
    }

    static boolean existe(HttpClient http, String url) {
        try {
            HttpResponse<Void> r = http.send(pedido(url).method("HEAD", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.discarding());
            return r.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static HttpRequest.Builder pedido(String url) {
        return HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(2)).header("User-Agent", App.NOME + "/" + App.VERSAO);
    }

    static void baixar(HttpClient http, String url, File destino, Progresso prog) throws Exception {
        HttpResponse<InputStream> r = http.send(pedido(url).timeout(Duration.ofMinutes(60)).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
        if (r.statusCode() != 200) throw new IOException("o download respondeu " + r.statusCode());
        long total = r.headers().firstValueAsLong("content-length").orElse(-1);
        try (InputStream in = r.body(); OutputStream out = Files.newOutputStream(destino.toPath())) {
            byte[] buf = new byte[1 << 16];
            long baixado = 0;
            int n;
            long ultimaAtualizacao = 0;
            while ((n = in.read(buf)) != -1) {
                if (prog.cancelado()) throw new IOException("cancelado");
                out.write(buf, 0, n);
                baixado += n;
                if (System.currentTimeMillis() - ultimaAtualizacao > 200) {
                    ultimaAtualizacao = System.currentTimeMillis();
                    String mb = String.format("%.1f", baixado / 1048576.0);
                    if (total > 0) prog.mostrar("Baixando " + destino.getName() + ": " + mb + " de " + String.format("%.1f", total / 1048576.0) + " MB", (int) (baixado * 100 / total));
                    else prog.mostrar("Baixando " + destino.getName() + ": " + mb + " MB", -1);
                }
            }
        }
    }

    /** Extrai o .zip. Se tudo estiver dentro de UMA pasta (ex.: node-v22-win-x64/), tira essa pasta do caminho. */
    static void extrairZip(File zip, File destino) throws IOException {
        String prefixo = prefixoComum(zip);
        Path raiz = destino.getCanonicalFile().toPath();
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip.toPath()))) {
            ZipEntry e;
            while ((e = in.getNextEntry()) != null) {
                String nome = e.getName().replace('\\', '/');
                if (prefixo != null) {
                    if (!nome.startsWith(prefixo)) continue;
                    nome = nome.substring(prefixo.length());
                }
                if (nome.isEmpty()) continue;
                Path alvo = raiz.resolve(nome).normalize();
                if (!alvo.startsWith(raiz)) throw new IOException("zip com caminho perigoso: " + e.getName());   // proteção "zip slip"
                if (e.isDirectory()) {
                    Files.createDirectories(alvo);
                } else {
                    Files.createDirectories(alvo.getParent());
                    Files.copy(in, alvo, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static String prefixoComum(File zip) throws IOException {
        Set<String> primeiros = new HashSet<>();
        boolean temArquivoNaRaiz = false;
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip.toPath()))) {
            ZipEntry e;
            while ((e = in.getNextEntry()) != null) {
                String nome = e.getName().replace('\\', '/');
                int barra = nome.indexOf('/');
                if (barra < 0) { temArquivoNaRaiz = true; break; }
                primeiros.add(nome.substring(0, barra + 1));
                if (primeiros.size() > 1) break;
            }
        }
        return !temArquivoNaRaiz && primeiros.size() == 1 ? primeiros.iterator().next() : null;
    }

    /** Roda o instalador em modo silencioso, apontando pra pasta do plugin. */
    static void rodarInstalador(Plugin p, File exe) throws Exception {
        String args = p.instalador.replace("{pasta}", p.pasta().getAbsolutePath());
        if (p.viaBat) {
            // NSIS quer:  setup.exe /S /D=C:\caminho com espaço   (sem aspas!)
            File bat = new File(p.pasta().getParentFile(), p.id + "-instalar.bat");
            String conteudo = "@echo off\r\nchcp 65001 >nul\r\n\"" + exe.getAbsolutePath() + "\" " + args + "\r\n";
            Files.write(bat.toPath(), conteudo.getBytes(StandardCharsets.UTF_8));
            rodarAgora(p.pasta().getParentFile(), "cmd.exe", "/c", bat.getAbsolutePath());
            Files.deleteIfExists(bat.toPath());
        } else {
            List<String> cmd = new ArrayList<>();
            cmd.add(exe.getAbsolutePath());
            for (String a : args.split(" (?=[-/])")) cmd.add(a);   // separa nos " /" e " -"
            rodarAgora(p.pasta().getParentFile(), cmd.toArray(new String[0]));
        }
    }

    /** Pega o link de um arquivo da versão mais nova de um projeto no GitHub. */
    static String github(HttpClient http, String repo, String regexDoNome) throws Exception {
        Map<?, ?> rel = (Map<?, ?>) Json.ler(texto(http, "https://api.github.com/repos/" + repo + "/releases/latest"));
        String melhor = null;
        for (Object o : (List<?>) rel.get("assets")) {
            Map<?, ?> a = (Map<?, ?>) o;
            String nome = String.valueOf(a.get("name"));
            if (nome.matches(regexDoNome) && (melhor == null || nome.compareTo(melhor) > 0)) melhor = nome;
        }
        if (melhor == null) throw new IOException("não achei o arquivo no GitHub (" + repo + ")");
        for (Object o : (List<?>) rel.get("assets")) {
            Map<?, ?> a = (Map<?, ?>) o;
            if (melhor.equals(a.get("name"))) return String.valueOf(a.get("browser_download_url"));
        }
        throw new IOException("não achei o link no GitHub");
    }

    /** Acha a versão mais nova numa página/listagem (ex.: "octave-(9.2.0)-w64.zip"). */
    static String maiorVersao(String pagina, String regexComGrupo) throws IOException {
        Matcher m = Pattern.compile(regexComGrupo).matcher(pagina);
        String melhor = null;
        while (m.find()) if (melhor == null || compararVersao(m.group(1), melhor) > 0) melhor = m.group(1);
        if (melhor == null) throw new IOException("não achei a versão");
        return melhor;
    }

    static void rodarAgora(File pasta, String... comando) throws Exception {
        Process p = new ProcessBuilder(comando).directory(pasta).redirectErrorStream(true).start();
        byte[] saida = p.getInputStream().readAllBytes();
        if (p.waitFor() != 0) throw new IOException("o passo final da instalação falhou: " + new String(saida, StandardCharsets.UTF_8).trim());
    }

    static void apagar(File f) {
        if (!f.exists()) return;
        try (java.util.stream.Stream<Path> s = Files.walk(f.toPath())) {
            s.sorted(Comparator.reverseOrder()).forEach(x -> x.toFile().delete());
        } catch (IOException ignorado) { }
    }

    static String semExtensao(String n) {
        int p = n.lastIndexOf('.');
        return p > 0 ? n.substring(0, p) : n;
    }

    /** Compara "1.10.2" com "1.9" do jeito certo (número por número). */
    static int compararVersao(String a, String b) {
        String[] x = a.replaceAll("[^0-9.]", "").split("\\."), y = b.replaceAll("[^0-9.]", "").split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int p = i < x.length && !x[i].isEmpty() ? Integer.parseInt(x[i]) : 0;
            int q = i < y.length && !y[i].isEmpty() ? Integer.parseInt(y[i]) : 0;
            if (p != q) return Integer.compare(p, q);
        }
        return 0;
    }
}
