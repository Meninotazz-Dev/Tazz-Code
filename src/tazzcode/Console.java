package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * O CONSOLE: o painel de baixo que roda o código e mostra o que ele imprime.
 *
 * Como funciona: o Java usa o ProcessBuilder pra abrir OUTRO programa
 * (tipo "java Teste.java" ou "python teste.py"). A gente fica "escutando"
 * o que esse programa escreve e coloca aqui na tela.
 * E o que você digita na caixinha de baixo é mandado pro programa (pro Scanner, input()...).
 */
public class Console extends JPanel {
    private JLabel seta;

    private final JTextPane saida = new JTextPane();
    private final JTextField entrada = new JTextField();
    private final JLabel status = new JLabel("Pronto");
    private final JPanel botoesTopo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    private volatile Process processo;

    public Console() {
        super(new BorderLayout());
        setBackground(Tema.FUNDO_CONSOLE);

        // ----- barra de cima do console -----
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.FUNDO_PAINEL);
        topo.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 6));
        JLabel titulo = new JLabel("CONSOLE");
        titulo.setForeground(Tema.AZUL_CLARO);
        titulo.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD));
        status.setForeground(Tema.TEXTO_FRACO);
        status.setFont(Tema.FONTE_UI);
        status.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        esquerda.setOpaque(false);
        esquerda.add(titulo);
        esquerda.add(status);
        JButton limpar = Tema.botao("Limpar", Tema.LINHA_ATUAL);
        limpar.addActionListener(e -> limpar());
        botoesTopo.setOpaque(false);
        botoesTopo.add(limpar);
        topo.add(esquerda, BorderLayout.WEST);
        topo.add(botoesTopo, BorderLayout.EAST);
        add(topo, BorderLayout.NORTH);

        // ----- área de saída -----
        saida.setEditable(false);
        saida.setBackground(Tema.FUNDO_CONSOLE);
        saida.setFont(Tema.fonteConsole());
        saida.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JScrollPane rolagem = new JScrollPane(saida);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        add(rolagem, BorderLayout.CENTER);

        // ----- caixinha de digitar (entrada do programa) -----
        entrada.setBackground(Tema.FUNDO);
        entrada.setForeground(Tema.TEXTO);
        entrada.setCaretColor(Tema.AZUL_CLARO);
        entrada.setFont(Tema.fonteConsole());
        entrada.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.LINHA_ATUAL),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        entrada.setToolTipText("Digite aqui e aperte ENTER para mandar para o programa");
        entrada.addActionListener(e -> enviarEntrada());
        seta = new JLabel("  > ");
        seta.setForeground(Tema.AZUL_CLARO);
        seta.setFont(Tema.fonteConsole().deriveFont(Font.BOLD));
        JPanel baixo = new JPanel(new BorderLayout());
        baixo.setBackground(Tema.FUNDO);
        baixo.add(seta, BorderLayout.WEST);
        baixo.add(entrada, BorderLayout.CENTER);
        add(baixo, BorderLayout.SOUTH);

        escrever("Bem-vindo ao " + App.NOME + "! Abra um arquivo e aperte F5 para rodar.\n", Tema.TEXTO_FRACO);
    }

    // ---------- rodar e parar ----------

    /** Um programa que roda AQUI DENTRO do TazzCode (ex.: o interpretador de Brainfuck). */
    public interface Interno {
        void executar(java.util.function.Consumer<String> escrever,
                      java.util.function.Supplier<String> lerLinha,
                      java.util.function.BooleanSupplier foiParado) throws Exception;
    }

    private java.util.function.Consumer<String> aoFaltarPrograma = c -> { };

    /** Quem quer saber quando faltou um programa (a janela oferece o plugin/tutorial). */
    public void setAoFaltarPrograma(java.util.function.Consumer<String> c) { aoFaltarPrograma = c; }

    private volatile boolean ocupado;          // tem alguma coisa rodando?
    private volatile boolean pararPedido;
    private final java.util.concurrent.BlockingQueue<String> linhasDigitadas = new java.util.concurrent.LinkedBlockingQueue<>();
    private Thread trabalho;

    public boolean rodando() {
        return ocupado;
    }

    /** Roda um comando só. */
    public void rodar(List<String> comando, File pasta) {
        rodarPassos(java.util.Collections.singletonList(comando), pasta, "");
    }

    /**
     * Roda vários comandos, um depois do outro (ex.: 1º compilar, 2º rodar).
     * Se um passo der erro, os próximos não rodam.
     */
    public void rodarPassos(List<List<String>> passos, File pasta, String dicaInstalar) {
        rodarPassos(passos, pasta, dicaInstalar, null);
    }

    /** Igual, e quando TODOS os passos derem certo, roda "depois" (na thread da janela). */
    public void rodarPassos(List<List<String>> passos, File pasta, String dicaInstalar, Runnable depois) {
        if (rodando()) parar();
        prepararParaRodar();
        comecar();
        long inicio = System.currentTimeMillis();

        trabalho = new Thread(() -> {
            int codigo = 0;
            if (passos.isEmpty()) naTela("(nada pra rodar)\n", Tema.TEXTO_FRACO);
            for (int i = 0; i < passos.size() && !pararPedido; i++) {
                List<String> comando = passos.get(i);
                String rotulo = (passos.size() > 1 ? "[passo " + (i + 1) + " de " + passos.size() + "] " : "") + "> ";
                naTela(rotulo + String.join(" ", comando) + "\n\n", Tema.AZUL_CLARO);

                // Acha o programa (no PC ou nos plugins). Se não achar, avisa.
                List<String> real = Executaveis.resolver(comando);
                if (real == null) {
                    naTela("Não achei o programa \"" + comando.get(0) + "\": ele não está instalado no PC.\n", Tema.ERRO);
                    Plugins.Plugin plugin = Plugins.quemTem(comando.get(0));
                    if (plugin != null) naTela("Tem PLUGIN pra isso: \"" + plugin.nome + "\" (menu Plugins). Ele roda sem instalar nada no PC!\n", Tema.SUCESSO);
                    if (!dicaInstalar.isEmpty()) naTela("Pra instalar no PC: " + dicaInstalar + "\n", Tema.AZUL_CLARO);
                    naTela("Tem um passo a passo em: Plugins > Tutorial de instalação.\n", Tema.AZUL_CLARO);
                    String faltou = comando.get(0);
                    SwingUtilities.invokeLater(() -> aoFaltarPrograma.accept(faltou));
                    codigo = -1;
                    break;
                }
                avisarAntivirus(real.get(0));
                ProcessBuilder pb = new ProcessBuilder(real);
                pb.directory(pasta);
                pb.environment().put("PYTHONIOENCODING", "utf-8");   // acentos no Python
                Executaveis.prepararAmbiente(pb.environment());       // plugins no PATH
                try {
                    Process p = pb.start();
                    processo = p;
                    Thread a = escutar(p.getInputStream(), Tema.TEXTO);   // saída normal
                    Thread b = escutar(p.getErrorStream(), Tema.ERRO);    // erros em vermelho
                    codigo = p.waitFor();
                    a.join(1000);
                    b.join(1000);
                } catch (IOException e) {
                    naTela("Não consegui rodar \"" + comando.get(0) + "\": ele não está instalado (ou não está no PATH do Windows).\n", Tema.ERRO);
                    if (!dicaInstalar.isEmpty()) naTela("Pra instalar: " + dicaInstalar + "\n", Tema.AZUL_CLARO);
                    codigo = -1;
                } catch (InterruptedException e) {
                    codigo = -1;
                }
                if (codigo != 0) break;
            }
            terminar(codigo, inicio);
            if (codigo == 0 && depois != null && !pararPedido) SwingUtilities.invokeLater(depois);
        });
        trabalho.setDaemon(true);
        trabalho.start();
    }

    /** Roda um programa feito em Java aqui dentro (sem processo de fora). */
    public void rodarInterno(String titulo, Interno programa) {
        if (rodando()) parar();
        prepararParaRodar();
        comecar();
        naTela("> " + titulo + "\n\n", Tema.AZUL_CLARO);
        long inicio = System.currentTimeMillis();
        trabalho = new Thread(() -> {
            int codigo = 0;
            try {
                programa.executar(
                        s -> naTela(s, Tema.TEXTO),
                        () -> {
                            try {
                                while (!pararPedido) {
                                    String l = linhasDigitadas.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                                    if (l != null) return l;
                                }
                            } catch (InterruptedException ignorado) { }
                            return null;
                        },
                        () -> pararPedido);
            } catch (Exception e) {
                naTela("\nErro: " + e.getMessage() + "\n", Tema.ERRO);
                codigo = 1;
            }
            terminar(codigo, inicio);
        });
        trabalho.setDaemon(true);
        trabalho.start();
    }

    private void comecar() {
        ocupado = true;
        pararPedido = false;
        linhasDigitadas.clear();
        status.setText("Rodando...");
        entrada.requestFocusInWindow();
    }

    private void terminar(int codigo, long inicio) {
        double seg = (System.currentTimeMillis() - inicio) / 1000.0;
        processo = null;
        ocupado = false;
        if (pararPedido) return;
        SwingUtilities.invokeLater(() -> {
            Color cor = codigo == 0 ? Tema.SUCESSO : Tema.ERRO;
            escrever(String.format("%n[Terminou com código %d em %.2fs]%n", codigo, seg), cor);
            status.setText(codigo == 0 ? "Terminou" : "Terminou com erro");
        });
    }

    public void parar() {
        if (!rodando()) return;
        pararPedido = true;
        Process p = processo;
        if (p != null) {
            p.descendants().forEach(ProcessHandle::destroyForcibly); // filhos também
            p.destroyForcibly();
        }
        ocupado = false;
        escrever("\n[Parado pelo usuário]\n", Tema.ERRO);
        status.setText("Parado");
    }

    // =====================================================================
    //  SAÍDA SEM TRAVAR
    //
    //  Antes, cada pedacinho que o programa escrevia virava uma tarefa pra janela.
    //  Um programa com "while (true) println" mandava MILHÕES de tarefas, o console
    //  crescia sem parar (centenas de MB) e o TazzCode travava por minutos.
    //  Agora: os pedaços vão pra uma fila, a janela junta tudo e escreve de uma vez
    //  a cada 40 ms, e o console guarda só o final (LIMITE caracteres).
    // =====================================================================

    private static final int LIMITE = 200_000;          // quanto texto o console guarda
    private static final int FILA_MAXIMA = 400_000;     // se a fila encher, o programa espera um pouco
    private final java.util.ArrayDeque<Object[]> fila = new java.util.ArrayDeque<>();
    private int tamanhoFila = 0;
    private boolean cortouComeco = false;
    private final javax.swing.Timer descarregar = new javax.swing.Timer(40, e -> descarregarFila());

    /** Chamado das outras threads: só coloca na fila (rapidinho). */
    private void naTela(String s, Color cor) {
        if (s.isEmpty()) return;
        synchronized (fila) {
            // Programa escrevendo rápido demais? Espera a janela dar conta (sem isso, a memória explode).
            while (tamanhoFila > FILA_MAXIMA && !pararPedido) {
                try { fila.wait(50); } catch (InterruptedException e) { return; }
            }
            Object[] ultimo = fila.peekLast();
            if (ultimo != null && ultimo[1].equals(cor) && ((StringBuilder) ultimo[0]).length() < 64_000) {
                ((StringBuilder) ultimo[0]).append(s);
            } else {
                fila.addLast(new Object[]{new StringBuilder(s), cor});
            }
            tamanhoFila += s.length();
        }
        if (!descarregar.isRunning()) SwingUtilities.invokeLater(descarregar::start);
    }

    /** Na thread da janela: escreve tudo o que está na fila de uma vez. */
    private void descarregarFila() {
        java.util.List<Object[]> pegar = new java.util.ArrayList<>();
        synchronized (fila) {
            pegar.addAll(fila);
            fila.clear();
            tamanhoFila = 0;
            fila.notifyAll();
        }
        if (pegar.isEmpty()) {
            descarregar.stop();
            return;
        }
        StyledDocument doc = saida.getStyledDocument();
        for (Object[] p : pegar) {
            String s = p[0].toString();
            if (s.length() > LIMITE) {   // um pedaço gigante: fica só com o final dele
                s = s.substring(s.length() - LIMITE);
                cortouComeco = true;
            }
            SimpleAttributeSet estilo = new SimpleAttributeSet();
            StyleConstants.setForeground(estilo, (Color) p[1]);
            try { doc.insertString(doc.getLength(), s, estilo); } catch (BadLocationException ignorado) { }
        }
        aparar();
        saida.setCaretPosition(doc.getLength());
    }

    /** Se o console passou do limite, apaga o começo (o mais antigo). */
    private void aparar() {
        StyledDocument doc = saida.getStyledDocument();
        int excesso = doc.getLength() - LIMITE;
        if (excesso <= 0) return;
        try {
            doc.remove(0, excesso + LIMITE / 4);   // apaga um pouco a mais, pra não ficar apagando toda hora
            if (!cortouComeco || !doc.getText(0, Math.min(10, doc.getLength())).startsWith("[...")) {
                SimpleAttributeSet estilo = new SimpleAttributeSet();
                StyleConstants.setForeground(estilo, Tema.TEXTO_FRACO);
                doc.insertString(0, "[... o começo da saída foi apagado pro TazzCode não travar ...]\n", estilo);
            }
            cortouComeco = true;
        } catch (BadLocationException ignorado) { }
    }

    /** Fica lendo o que o programa escreve, numa thread separada (pra janela não travar). */
    private Thread escutar(InputStream fluxo, Color cor) {
        Thread t = new Thread(() -> {
            try (Reader r = new InputStreamReader(fluxo, StandardCharsets.UTF_8)) {
                int n;
                char[] grande = new char[8192];
                while ((n = r.read(grande)) != -1) {
                    naTela(new String(grande, 0, n).replace("\r\n", "\n"), cor);
                }
            } catch (IOException ignorado) { }
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    private void enviarEntrada() {
        String linha = entrada.getText();
        entrada.setText("");
        if (!rodando()) {
            escrever("(nenhum programa rodando)\n", Tema.TEXTO_FRACO);
            return;
        }
        escrever(linha + "\n", Tema.AZUL_CLARO);
        Process p = processo;
        if (p == null) {                 // programa interno (ex.: Brainfuck)
            linhasDigitadas.add(linha);
            return;
        }
        try {
            OutputStream out = p.getOutputStream();
            out.write((linha + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            escrever("Não deu pra mandar: " + e.getMessage() + "\n", Tema.ERRO);
        }
    }

    // ---------- escrever na tela ----------

    /**
     * Programa que acabou de ser compilado (.exe novinho)? Os antivírus (Avast, Windows Defender...)
     * desconfiam de todo .exe que eles nunca viram antes e analisam ele por uns segundos.
     * Isso é normal! Aqui a gente avisa quem está usando, pra ninguém se assustar.
     */
    private void avisarAntivirus(String programa) {
        try {
            java.io.File f = new java.io.File(programa);
            java.io.File compilados = Pastas.compilados().toFile().getCanonicalFile();
            if (!Executaveis.WINDOWS || !f.isFile() || !f.getName().toLowerCase().endsWith(".exe")) return;
            if (!f.getCanonicalPath().startsWith(compilados.getPath())) return;
            if (System.currentTimeMillis() - f.lastModified() > 60_000) return;   // só se acabou de nascer
            naTela("ℹ Programa novinho! Se o antivírus (Avast, Windows Defender...) disser que é \"suspeito\" e analisar ele,\n"
                    + "  é normal: o antivírus desconfia de todo .exe que acabou de ser criado, porque nunca viu ele antes.\n"
                    + "  Esse programa foi feito por VOCÊ, a partir do seu código. É só esperar a análise terminar.\n"
                    + "  (Ajuda > Por que o antivírus desconfia? explica como evitar isso.)\n\n", Tema.TEXTO_FRACO);
        } catch (Exception ignorado) { }
    }

    public void escrever(String s, Color cor) {
        descarregarFila();   // o que estava na fila vem antes (pra não embaralhar a ordem)
        StyledDocument doc = saida.getStyledDocument();
        SimpleAttributeSet estilo = new SimpleAttributeSet();
        StyleConstants.setForeground(estilo, cor);
        try {
            doc.insertString(doc.getLength(), s, estilo);
        } catch (BadLocationException ignorado) { }
        aparar();
        saida.setCaretPosition(doc.getLength());
    }

    /** Tudo o que está escrito no console agora (o TazzHelper lê isso). */
    public String textoDaSaida() {
        try {
            int n = saida.getDocument().getLength(), de = Math.max(0, n - 20_000);   // o TazzHelper só precisa do final
            return saida.getDocument().getText(de, n - de);
        } catch (BadLocationException e) {
            return "";
        }
    }

    /** Liga o botão "Explicar com o TazzHelper" na barra do console. */
    public void setAoExplicar(Runnable r) {
        JButton b = Tema.botao("◆ Explicar com o TazzHelper", Tema.AZUL);
        b.addActionListener(e -> r.run());
        botoesTopo.add(b, 0);
        botoesTopo.revalidate();
    }

    /** Antes de rodar: limpa o console (ou, se desligado nas Configurações, só põe uma linha separando). */
    private void prepararParaRodar() {
        if (Ajustes.limparConsole() || saida.getDocument().getLength() == 0) limpar();
        else escrever("\n────────────────────────────────────────\n\n", Tema.TEXTO_FRACO);
    }

    /** Aplica as Configurações (tamanho da letra do console). */
    public void aplicarConfiguracoes() {
        saida.setFont(Tema.fonteConsole());
        entrada.setFont(Tema.fonteConsole());
        seta.setFont(Tema.fonteConsole().deriveFont(Font.BOLD));
        revalidate();
        repaint();
    }

    public void limpar() {
        synchronized (fila) {
            fila.clear();
            tamanhoFila = 0;
            fila.notifyAll();
        }
        cortouComeco = false;
        saida.setText("");
    }
}
