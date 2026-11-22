package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * O TUTORIAL DE INSTALAÇÃO: um passo a passo pra instalar a linguagem no PC.
 *
 *   1) o jeito mais fácil: o PLUGIN do TazzCode (não instala nada no Windows)
 *   2) pelo WINGET (o "instalador de programas" que já vem no Windows 10 e 11)
 *   3) pelo SITE oficial, passo a passo
 *   4) TESTAR se deu certo
 */
public class JanelaTutorial extends JDialog {

    /** Os nomes no winget (o "catálogo de programas" do Windows). */
    private static final Map<String, String> WINGET = new HashMap<>();
    /** O comando que mostra a versão (pra testar se instalou). */
    private static final Map<String, String> TESTE = new HashMap<>();
    /** No Linux (Ubuntu): o pacote do apt de cada linguagem. */
    private static final Map<String, String> APT = new HashMap<>();
    /** Fedora (dnf), openSUSE (zypper) e Arch/Manjaro (pacman): o nome do pacote em cada um. */
    private static final Map<String, String> DNF = new HashMap<>();
    private static final Map<String, String> ZYPPER = new HashMap<>();
    private static final Map<String, String> PACMAN = new HashMap<>();
    /** No Linux: as que não têm no apt mas têm na Snap Store. */
    private static final Map<String, String> SNAP = new HashMap<>();
    /** Linguagens que só existem no Windows. */
    private static final java.util.Set<String> SO_WINDOWS = new java.util.HashSet<>(java.util.Arrays.asList(
            "AutoHotkey", "AutoIt", "Batch (Windows)", "VBScript"));

    static {
        DNF.put("Python", "python3");
        DNF.put("JavaScript", "nodejs npm");
        DNF.put("TypeScript", "nodejs npm");
        DNF.put("Java", "java-21-openjdk-devel");
        DNF.put("JShell (Java interativo)", "java-21-openjdk-devel");
        DNF.put("C", "gcc make");
        DNF.put("C++", "gcc-c++ make");
        DNF.put("C#", "dotnet-sdk-8.0");
        DNF.put("Go", "golang");
        DNF.put("Rust", "rust cargo");
        DNF.put("Ruby", "ruby");
        DNF.put("PHP", "php-cli");
        DNF.put("Lua", "lua");
        DNF.put("Perl", "perl");
        DNF.put("Haskell", "ghc");
        DNF.put("Kotlin", "kotlin");
        DNF.put("Scala", "scala");
        DNF.put("R", "R");
        DNF.put("Fortran", "gcc-gfortran");
        DNF.put("Assembly (NASM)", "nasm");
        DNF.put("Erlang", "erlang");
        DNF.put("Elixir", "elixir");
        DNF.put("OCaml", "ocaml");
        DNF.put("Clojure", "clojure");
        DNF.put("Common Lisp", "sbcl");
        DNF.put("Racket", "racket");
        DNF.put("Prolog", "pl");
        DNF.put("Nim", "nim");
        DNF.put("Zig", "zig");
        DNF.put("Julia", "julia");
        DNF.put("Dart", "dart");
        DNF.put("Tcl", "tcl");
        DNF.put("AWK", "gawk");
        DNF.put("Shell / Bash", "bash");
        DNF.put("Zsh", "zsh");
        DNF.put("Fish", "fish");
        DNF.put("Makefile", "make");
        DNF.put("CMake", "cmake");
        DNF.put("MATLAB", "octave");
        DNF.put("Groovy", "groovy");
        DNF.put("COBOL", "gnucobol");
        DNF.put("Ada", "gcc-gnat");
        DNF.put("Delphi / Object Pascal", "fpc");
        DNF.put("Vala", "vala");
        DNF.put("Verilog", "iverilog");
        DNF.put("LaTeX", "texlive-scheme-basic");
        ZYPPER.put("Python", "python3");
        ZYPPER.put("JavaScript", "nodejs npm");
        ZYPPER.put("TypeScript", "nodejs npm");
        ZYPPER.put("Java", "java-21-openjdk-devel");
        ZYPPER.put("JShell (Java interativo)", "java-21-openjdk-devel");
        ZYPPER.put("C", "gcc make");
        ZYPPER.put("C++", "gcc-c++ make");
        ZYPPER.put("Go", "go");
        ZYPPER.put("Rust", "rust cargo");
        ZYPPER.put("Ruby", "ruby");
        ZYPPER.put("PHP", "php8");
        ZYPPER.put("Lua", "lua54");
        ZYPPER.put("Perl", "perl");
        ZYPPER.put("Haskell", "ghc");
        ZYPPER.put("Scala", "scala");
        ZYPPER.put("R", "R-base");
        ZYPPER.put("Fortran", "gcc-fortran");
        ZYPPER.put("Assembly (NASM)", "nasm");
        ZYPPER.put("Erlang", "erlang");
        ZYPPER.put("Elixir", "elixir");
        ZYPPER.put("OCaml", "ocaml");
        ZYPPER.put("Common Lisp", "sbcl");
        ZYPPER.put("Racket", "racket");
        ZYPPER.put("Prolog", "swipl");
        ZYPPER.put("Nim", "nim");
        ZYPPER.put("Zig", "zig");
        ZYPPER.put("Julia", "julia");
        ZYPPER.put("Tcl", "tcl");
        ZYPPER.put("AWK", "gawk");
        ZYPPER.put("Shell / Bash", "bash");
        ZYPPER.put("Zsh", "zsh");
        ZYPPER.put("Fish", "fish");
        ZYPPER.put("Makefile", "make");
        ZYPPER.put("CMake", "cmake");
        ZYPPER.put("MATLAB", "octave");
        ZYPPER.put("Groovy", "groovy");
        ZYPPER.put("Ada", "gcc-ada");
        ZYPPER.put("Delphi / Object Pascal", "fpc");
        ZYPPER.put("Vala", "vala");
        PACMAN.put("Python", "python");
        PACMAN.put("JavaScript", "nodejs npm");
        PACMAN.put("TypeScript", "nodejs npm");
        PACMAN.put("Java", "jdk21-openjdk");
        PACMAN.put("JShell (Java interativo)", "jdk21-openjdk");
        PACMAN.put("C", "gcc make");
        PACMAN.put("C++", "gcc make");
        PACMAN.put("C#", "dotnet-sdk");
        PACMAN.put("F#", "dotnet-sdk");
        PACMAN.put("Go", "go");
        PACMAN.put("Rust", "rust");
        PACMAN.put("Ruby", "ruby");
        PACMAN.put("PHP", "php");
        PACMAN.put("Lua", "lua");
        PACMAN.put("Perl", "perl");
        PACMAN.put("Haskell", "ghc");
        PACMAN.put("Kotlin", "kotlin");
        PACMAN.put("Scala", "scala");
        PACMAN.put("R", "r");
        PACMAN.put("Fortran", "gcc-fortran");
        PACMAN.put("Assembly (NASM)", "nasm");
        PACMAN.put("Erlang", "erlang");
        PACMAN.put("Elixir", "elixir");
        PACMAN.put("OCaml", "ocaml");
        PACMAN.put("Clojure", "clojure");
        PACMAN.put("Common Lisp", "sbcl");
        PACMAN.put("Racket", "racket");
        PACMAN.put("Prolog", "swi-prolog");
        PACMAN.put("Nim", "nim");
        PACMAN.put("Zig", "zig");
        PACMAN.put("Julia", "julia");
        PACMAN.put("Dart", "dart");
        PACMAN.put("Tcl", "tcl");
        PACMAN.put("AWK", "gawk");
        PACMAN.put("Shell / Bash", "bash");
        PACMAN.put("Zsh", "zsh");
        PACMAN.put("Fish", "fish");
        PACMAN.put("Makefile", "make");
        PACMAN.put("CMake", "cmake");
        PACMAN.put("MATLAB", "octave");
        PACMAN.put("Groovy", "groovy");
        PACMAN.put("Crystal", "crystal");
        PACMAN.put("Ada", "gcc-ada");
        PACMAN.put("Delphi / Object Pascal", "fpc");
        PACMAN.put("Vala", "vala");
        PACMAN.put("Verilog", "iverilog");
        PACMAN.put("LaTeX", "texlive-basic");
        PACMAN.put("Raku", "rakudo");
        PACMAN.put("Smalltalk", "gnu-smalltalk");
        PACMAN.put("VHDL", "ghdl");
        PACMAN.put("Haxe", "haxe");
        PACMAN.put("Scheme", "chez-scheme");
        PACMAN.put("Forth", "gforth");
        PACMAN.put("Nix", "nix");
        PACMAN.put("Dockerfile", "docker");
        APT.put("Ada", "gnat");
        APT.put("Assembly (NASM)", "nasm");
        APT.put("AWK", "gawk");
        APT.put("C", "build-essential");
        APT.put("C++", "build-essential");
        APT.put("C#", "dotnet-sdk-8.0");
        APT.put("F#", "dotnet-sdk-8.0");
        APT.put("Clojure", "clojure");
        APT.put("CMake", "cmake");
        APT.put("COBOL", "gnucobol");
        APT.put("Common Lisp", "sbcl");
        APT.put("Delphi / Object Pascal", "fpc");
        APT.put("Elixir", "elixir");
        APT.put("Erlang", "erlang");
        APT.put("Fish", "fish");
        APT.put("Forth", "gforth");
        APT.put("Fortran", "gfortran");
        APT.put("Go", "golang-go");
        APT.put("Groovy", "groovy");
        APT.put("Haskell", "ghc");
        APT.put("Haxe", "haxe");
        APT.put("Java", "openjdk-21-jdk");
        APT.put("JShell (Java interativo)", "openjdk-21-jdk");
        APT.put("JavaScript", "nodejs npm");
        APT.put("TypeScript", "nodejs npm");
        APT.put("CoffeeScript", "nodejs npm");
        APT.put("EJS (Embedded JavaScript)", "nodejs npm");
        APT.put("LaTeX", "texlive-latex-base");
        APT.put("Lua", "lua5.4");
        APT.put("Makefile", "make");
        APT.put("MATLAB", "octave");
        APT.put("Nim", "nim");
        APT.put("Objective-C", "clang gobjc");
        APT.put("OCaml", "ocaml");
        APT.put("Perl", "perl");
        APT.put("PHP", "php-cli");
        APT.put("Prolog", "swi-prolog");
        APT.put("Python", "python3");
        APT.put("R", "r-base");
        APT.put("Racket", "racket");
        APT.put("Raku", "rakudo");
        APT.put("Ruby", "ruby");
        APT.put("Rust", "rustc cargo");
        APT.put("Scala", "scala");
        APT.put("Scheme", "chezscheme");
        APT.put("Shell / Bash", "bash");
        APT.put("Smalltalk", "gnu-smalltalk");
        APT.put("Tcl", "tcl");
        APT.put("Vala", "valac");
        APT.put("Verilog", "iverilog");
        APT.put("VHDL", "ghdl");
        APT.put("Zsh", "zsh");
        APT.put("Dockerfile", "docker.io");
        APT.put("Nix", "nix-bin");
        SNAP.put("Kotlin", "kotlin --classic");
        SNAP.put("Zig", "zig --classic --beta");
        SNAP.put("Julia", "julia --classic");
        SNAP.put("PowerShell", "powershell --classic");
        WINGET.put("Python", "Python.Python.3.12");
        WINGET.put("JavaScript", "OpenJS.NodeJS.LTS");
        WINGET.put("TypeScript", "OpenJS.NodeJS.LTS");
        WINGET.put("CoffeeScript", "OpenJS.NodeJS.LTS");
        WINGET.put("EJS (Embedded JavaScript)", "OpenJS.NodeJS.LTS");
        WINGET.put("Java", "EclipseAdoptium.Temurin.21.JDK");
        WINGET.put("JShell (Java interativo)", "EclipseAdoptium.Temurin.21.JDK");
        WINGET.put("Go", "GoLang.Go");
        WINGET.put("Rust", "Rustlang.Rustup");
        WINGET.put("C#", "Microsoft.DotNet.SDK.8");
        WINGET.put("F#", "Microsoft.DotNet.SDK.8");
        WINGET.put("Visual Basic .NET", "Microsoft.DotNet.SDK.8");
        WINGET.put("C", "BrechtSanders.WinLibs.POSIX.UCRT");
        WINGET.put("C++", "BrechtSanders.WinLibs.POSIX.UCRT");
        WINGET.put("Fortran", "BrechtSanders.WinLibs.POSIX.UCRT");
        WINGET.put("Ruby", "RubyInstallerTeam.RubyWithDevKit.3.3");
        WINGET.put("Lua", "DEVCOM.Lua");
        WINGET.put("Julia", "Julialang.Julia");
        WINGET.put("Zig", "zig.zig");
        WINGET.put("R", "RProject.R");
        WINGET.put("MATLAB", "GNU.Octave");
        WINGET.put("Perl", "StrawberryPerl.StrawberryPerl");
        WINGET.put("Shell / Bash", "Git.Git");
        WINGET.put("AWK", "Git.Git");
        WINGET.put("Erlang", "Erlang.ErlangOTP");
        WINGET.put("Racket", "Racket.Racket");
        WINGET.put("Prolog", "SWI-Prolog.SWI-Prolog");
        WINGET.put("Nim", "nim.nim");
        WINGET.put("Dockerfile", "Docker.DockerDesktop");
        WINGET.put("CMake", "Kitware.CMake");
        WINGET.put("Terraform (HCL)", "Hashicorp.Terraform");
        WINGET.put("AutoHotkey", "AutoHotkey.AutoHotkey");
        WINGET.put("Deno", "DenoLand.Deno");

        TESTE.put("python", "python --version");
        TESTE.put("node", "node --version");
        TESTE.put("npx", "node --version");
        TESTE.put("java", "java -version");
        TESTE.put("go", "go version");
        TESTE.put("gcc", "gcc --version");
        TESTE.put("g++", "g++ --version");
        TESTE.put("dotnet", "dotnet --version");
        TESTE.put("rustc", "rustc --version");
        TESTE.put("lua", "lua -v");
        TESTE.put("zig", "zig version");
        TESTE.put("kotlinc", "kotlinc -version");
        TESTE.put("swipl", "swipl --version");
        TESTE.put("octave-cli", "octave-cli --version");
        TESTE.put("Rscript", "Rscript --version");
        TESTE.put("scala", "scala --version");
        TESTE.put("php", "php --version");
    }

    private final Linguagem linguagem;
    private final Consumer<String> rodarNoTerminal;
    private final JLabel resultadoTeste = new JLabel(" ");

    public JanelaTutorial(JFrame dona, Linguagem inicial, Consumer<String> rodarNoTerminal) {
        super(dona, "Tutorial de instalação", false);
        this.linguagem = inicial;
        this.rodarNoTerminal = rodarNoTerminal;
        setSize(720, 640);
        setLocationRelativeTo(dona);

        JPanel tudo = new JPanel(new BorderLayout());
        tudo.setBackground(Tema.FUNDO);

        // escolher a linguagem
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topo.setBackground(Tema.FUNDO_PAINEL);
        JLabel rot = new JLabel("Linguagem:");
        rot.setForeground(Tema.TEXTO);
        JComboBox<Linguagem> escolha = new JComboBox<>(Catalogo.todas().toArray(new Linguagem[0]));
        escolha.setMaximumRowCount(18);
        escolha.setSelectedItem(inicial);
        escolha.addActionListener(e -> {
            dispose();
            new JanelaTutorial(dona, (Linguagem) escolha.getSelectedItem(), rodarNoTerminal).setVisible(true);
        });
        topo.add(rot);
        topo.add(escolha);
        tudo.add(topo, BorderLayout.NORTH);

        JPanel passos = new JPanel();
        passos.setLayout(new BoxLayout(passos, BoxLayout.Y_AXIS));
        passos.setBackground(Tema.FUNDO);
        passos.setBorder(BorderFactory.createEmptyBorder(14, 18, 18, 18));
        montar(passos);
        JScrollPane rol = new JScrollPane(passos);
        rol.setBorder(BorderFactory.createEmptyBorder());
        rol.getVerticalScrollBar().setUnitIncrement(16);
        tudo.add(rol, BorderLayout.CENTER);
        setContentPane(tudo);
    }

    private void montar(JPanel p) {
        Linguagem l = linguagem;
        titulo(p, "Como instalar " + l.nome + " no PC", 20);
        String comando = l.comandos.isEmpty() ? "" : l.comandos.get(0).split("\\s+")[0];
        Linguagem.Alternativa alt = l.escolhida();
        if (alt != null && !alt.comandos.isEmpty()) comando = alt.comandos.get(0).split("\\s+")[0];
        if (comando.startsWith("{")) comando = "";

        // Linguagens que não instalam (rodam em outro programa ou são simuladas)
        if (l.modo == Linguagem.Modo.BANCO || l.modo == Linguagem.Modo.BRAINFUCK) {
            texto(p, "<b>Não precisa instalar nada!</b> O " + l.nome + " roda dentro do próprio TazzCode (simulado).");
            return;
        }
        if (l.modo == Linguagem.Modo.SO_EDITAR || l.modo == Linguagem.Modo.LINK || l.modo == Linguagem.Modo.NAVEGADOR) {
            texto(p, l.precisa.isEmpty() ? "Essa linguagem não precisa ser instalada." : l.precisa);
            botaoSite(p, l.precisa);
            return;
        }
        if (!comando.isEmpty() && Executaveis.noPc(comando)) {
            texto(p, "<font color='#7CFFB2'><b>Boa notícia: o \"" + comando + "\" JÁ está instalado no seu PC!</b></font> É só apertar F5.");
        }

        if (!Executaveis.WINDOWS) {
            montarLinux(p, l, comando, alt);
            return;
        }

        // 1) PLUGIN
        List<Plugins.Plugin> plugins = Plugins.paraLinguagem(l.nome);
        if (!plugins.isEmpty()) {
            secao(p, "Jeito 1 (o mais fácil): PLUGIN do TazzCode");
            for (Plugins.Plugin pl : plugins) {
                texto(p, "<b>" + pl.nome + "</b> (" + pl.tamanho + "): " + pl.descricao
                        + "<br>O plugin fica só dentro da pasta do TazzCode: <b>não mexe no Windows</b> e dá pra apagar quando quiser."
                        + (pl.instalado() ? "<br><font color='#7CFFB2'><b>Já está instalado!</b></font>" : ""));
                if (!pl.instalado()) botao(p, "Instalar o plugin " + pl.nome, Tema.AZUL, () -> {
                    JanelaPlugins j = new JanelaPlugins((JFrame) getOwner());
                    j.setVisible(true);
                    j.instalar(pl);
                    dispose();
                });
            }
        }

        // 2) WINGET
        String id = WINGET.get(l.nome);
        if (id != null) {
            secao(p, "Jeito " + (plugins.isEmpty() ? "1" : "2") + ": pelo winget (já vem no Windows 10 e 11)");
            texto(p, "O <b>winget</b> é o \"instalador de programas\" do Windows. Ele baixa e instala sozinho, já configurando tudo.<br>"
                    + "<b>1.</b> Clique no botão (ou abra o Terminal do Windows e digite o comando abaixo).<br>"
                    + "<b>2.</b> Se aparecer uma pergunta, digite <b>Y</b> e aperte ENTER. Se o Windows pedir permissão, clique em <b>Sim</b>.<br>"
                    + "<b>3.</b> Quando terminar, <b>feche e abra o TazzCode de novo</b> (pra ele enxergar o programa novo).");
            String cmd = "winget install -e --id " + id + " --accept-source-agreements --accept-package-agreements";
            codigo(p, "winget install -e --id " + id);
            botao(p, "Rodar no terminal do TazzCode", Tema.LINHA_ATUAL, () -> rodarNoTerminal.accept(cmd));
            texto(p, "<font color='#7D8BA6'>Se o winget disser que não achou, use o Jeito pelo site aqui embaixo.</font>");
        }

        // 3) SITE
        int numero = (plugins.isEmpty() ? 1 : 2) + (id != null ? 1 : 0);
        secao(p, "Jeito " + numero + ": pelo site oficial");
        texto(p, "<b>1.</b> Abra o site (botão abaixo).<br>"
                + "<b>2.</b> Baixe a versão pra <b>Windows 64-bit</b> (normalmente um arquivo <b>.exe</b> ou <b>.msi</b>).<br>"
                + "<b>3.</b> Abra o arquivo baixado e vá clicando em <b>Next/Próximo</b>.<br>"
                + "<b>4.</b> MUITO IMPORTANTE: se aparecer uma opção tipo <b>\"Add to PATH\"</b> ou <b>\"Adicionar ao PATH\"</b>, <b>MARQUE ela</b>! "
                + "(É ela que deixa o TazzCode achar o programa.)<br>"
                + "<b>5.</b> Termine a instalação e <b>feche e abra o TazzCode de novo</b>.");
        texto(p, "<font color='#7D8BA6'>" + (alt != null && !alt.precisa.isEmpty() ? alt.precisa : l.precisa) + "</font>");
        botaoSite(p, alt != null && !alt.precisa.isEmpty() ? alt.precisa : l.precisa);

        // 4) TESTAR
        if (!comando.isEmpty()) {
            secao(p, "Testar se deu certo");
            String teste = TESTE.getOrDefault(comando, comando + " --version");
            codigo(p, teste);
            botao(p, "Testar agora", Tema.AZUL, () -> testar(teste));
            resultadoTeste.setFont(Tema.FONTE_UI.deriveFont(14f));
            resultadoTeste.setAlignmentX(LEFT_ALIGNMENT);
            p.add(resultadoTeste);
        }
    }

    /** O tutorial no Linux (Ubuntu): apt, snap ou site. */
    private void montarLinux(JPanel p, Linguagem l, String comando, Linguagem.Alternativa alt) {
        if (SO_WINDOWS.contains(l.nome)) {
            texto(p, "<b>" + l.nome + " só existe no Windows.</b> No Linux dá pra editar os arquivos, mas não dá pra rodar.");
            return;
        }
        int numero = 1;
        // Qual "instalador de programas" este Linux usa? (vem do arquivo /etc/os-release)
        String familia = Executaveis.familiaLinux();
        String nomeDistro = Executaveis.nomeLinux();
        String instalador, pacote, cmd;
        switch (familia) {
            case "fedora": instalador = "dnf";    pacote = DNF.get(l.nome);    cmd = "sudo dnf install -y "; break;
            case "suse":   instalador = "zypper"; pacote = ZYPPER.get(l.nome); cmd = "sudo zypper install -y "; break;
            case "arch":   instalador = "pacman"; pacote = PACMAN.get(l.nome); cmd = "sudo pacman -S --needed "; break;
            default:       instalador = "apt";    pacote = APT.get(l.nome);    cmd = "sudo apt install -y ";
        }
        if (pacote != null) {
            secao(p, "Jeito " + numero++ + " (o mais fácil): pelo " + instalador);
            texto(p, "O <b>" + instalador + "</b> é o \"instalador de programas\" do " + nomeDistro + ". Um comando e pronto:<br>"
                    + "<b>1.</b> Clique no botão: abre o Terminal já com o comando.<br>"
                    + "<b>2.</b> Digite a <b>sua senha</b> do Linux (ela não aparece enquanto digita, é normal!) e aperte ENTER.<br>"
                    + "<b>3.</b> Quando terminar, feche o Terminal e aperte F5 no TazzCode.");
            String comandoFinal = cmd + pacote;
            codigo(p, comandoFinal);
            String antes = familia.equals("debian") ? "sudo apt update && " : "";
            botao(p, "Abrir no Terminal", Tema.AZUL, () -> abrirTerminalLinux(antes + comandoFinal));
        } else if (!familia.equals("debian") && APT.get(l.nome) != null) {
            secao(p, "Jeito " + numero++ + ": procurar pelo " + instalador);
            texto(p, "Não sei o nome exato do pacote no " + nomeDistro + ". Procure assim (troque pela linguagem):");
            String busca = familia.equals("arch") ? "pacman -Ss " : familia.equals("suse") ? "zypper search " : "dnf search ";
            codigo(p, busca + l.nome.toLowerCase().split(" ")[0]);
        }
        String snap = SNAP.get(l.nome);
        if (snap != null) {
            secao(p, "Jeito " + numero++ + ": pela Snap Store");
            texto(p, "Também dá pela <b>Snap Store</b> (no Ubuntu já vem; nos outros Linux, instale o \"snapd\" antes).");
            String cmdSnap = "sudo snap install " + snap;
            codigo(p, cmdSnap);
            botao(p, "Abrir no Terminal", Tema.AZUL, () -> abrirTerminalLinux(cmdSnap));
        }
        secao(p, "Jeito " + numero + ": pelo site oficial");
        texto(p, "Abra o site e procure a versão pra <b>Linux</b> (às vezes chamada de <b>x86_64</b> ou <b>.deb</b>). "
                + "Se for um arquivo <b>.deb</b>, é só dar dois cliques que o Ubuntu instala.");
        String precisa = alt != null && !alt.precisa.isEmpty() ? alt.precisa : l.precisa;
        botaoSite(p, precisa);
        if (!comando.isEmpty()) {
            secao(p, "Testar se deu certo");
            String teste = TESTE.getOrDefault(comando, comando + " --version").replace("python --", "python3 --");
            codigo(p, teste);
            botao(p, "Testar agora", Tema.AZUL, () -> testar(teste));
            resultadoTeste.setFont(Tema.FONTE_UI.deriveFont(14f));
            resultadoTeste.setAlignmentX(LEFT_ALIGNMENT);
            p.add(resultadoTeste);
        }
    }

    /** Abre o Terminal do Linux rodando o comando (pra poder digitar a senha do sudo). */
    static void abrirTerminalLinux(String comando) {
        String script = comando + "; echo; echo 'Pronto! Pode fechar esta janela.'; read -r _";
        String[][] opcoes = {
                {"x-terminal-emulator", "-e", "bash", "-c", script},
                {"kgx", "--", "bash", "-c", script},
                {"ptyxis", "--", "bash", "-c", script},
                {"xfce4-terminal", "-x", "bash", "-c", script},
                {"mate-terminal", "--", "bash", "-c", script},
                {"tilix", "-e", "bash", "-c", script},
                {"alacritty", "-e", "bash", "-c", script},
                {"gnome-terminal", "--", "bash", "-c", script},
                {"konsole", "-e", "bash", "-c", script},
                {"xterm", "-e", "bash", "-c", script}};
        for (String[] o : opcoes) {
            if (Executaveis.acharPrograma(o[0]) == null) continue;
            try { new ProcessBuilder(o).start(); return; } catch (Exception ignorado) { }
        }
        javax.swing.JOptionPane.showMessageDialog(null, "Não achei o Terminal. Abra ele (Ctrl+Alt+T) e digite:\n\n" + comando);
    }

    /** Roda o comando de versão e mostra se funcionou. */
    private void testar(String teste) {
        resultadoTeste.setForeground(Tema.TEXTO_FRACO);
        resultadoTeste.setText("Testando...");
        new Thread(() -> {
            String msg;
            Color cor;
            try {
                List<String> cmd = Executaveis.resolver(java.util.Arrays.asList(teste.split(" ")));
                if (cmd == null) throw new Exception("não achei o programa");
                ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);
                Executaveis.prepararAmbiente(pb.environment());
                Process pr = pb.start();
                String saida = new String(pr.getInputStream().readAllBytes()).trim();
                pr.waitFor();
                String primeira = saida.split("\\R")[0];
                msg = "<html><b>Funcionou!</b> " + primeira.replace("<", "&lt;") + "</html>";
                cor = Tema.SUCESSO;
            } catch (Exception e) {
                msg = "<html><b>Ainda não achei.</b> Instalou e fechou/abriu o TazzCode? (" + e.getMessage() + ")</html>";
                cor = Tema.ERRO;
            }
            String m = msg;
            Color c = cor;
            javax.swing.SwingUtilities.invokeLater(() -> {
                resultadoTeste.setForeground(c);
                resultadoTeste.setText(m);
            });
        }).start();
    }

    // ---------- pecinhas da tela ----------

    private void titulo(JPanel p, String t, float tamanho) {
        JLabel l = new JLabel(t);
        l.setForeground(Tema.AZUL_CLARO);
        l.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD, tamanho));
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(8));
    }

    private void secao(JPanel p, String t) {
        p.add(Box.createVerticalStrut(14));
        titulo(p, t, 16);
    }

    private void texto(JPanel p, String html) {
        JLabel l = new JLabel("<html><div style='width:540px'>" + html + "</div></html>");
        l.setForeground(Tema.TEXTO);
        l.setFont(Tema.FONTE_UI.deriveFont(13.5f));
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(6));
    }

    private void codigo(JPanel p, String c) {
        JTextField f = new JTextField(c);   // dá pra selecionar e copiar
        f.setEditable(false);
        f.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        f.setBackground(Tema.FUNDO_CONSOLE);
        f.setForeground(Tema.AZUL_CLARO);
        f.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        f.setMaximumSize(new java.awt.Dimension(640, 34));
        f.setAlignmentX(LEFT_ALIGNMENT);
        p.add(f);
        p.add(Box.createVerticalStrut(6));
    }

    private void botao(JPanel p, String t, Color cor, Runnable r) {
        JButton b = Tema.botao(t, cor);
        b.addActionListener(e -> r.run());
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linha.setOpaque(false);
        linha.add(b);
        linha.setAlignmentX(LEFT_ALIGNMENT);
        linha.setMaximumSize(new java.awt.Dimension(700, 40));
        p.add(linha);
        p.add(Box.createVerticalStrut(6));
    }

    private void botaoSite(JPanel p, String precisa) {
        Matcher m = Pattern.compile("https?://[^\\s)]+").matcher(precisa == null ? "" : precisa);
        if (!m.find()) return;
        String url = m.group().replaceAll("[.,;]$", "");
        botao(p, "Abrir o site: " + url, Tema.LINHA_ATUAL, () -> {
            try { Desktop.getDesktop().browse(URI.create(url)); } catch (Exception ignorado) { }
        });
    }
}
