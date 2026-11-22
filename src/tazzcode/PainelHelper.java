package tazzcode;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * O TAZZHELPER: o assistente que fica do lado direito do editor.
 *
 *   - COM chave da API: conversa com o Claude (a IA), que vê o seu código e o console.
 *   - SEM chave: o AjudanteOffline explica erros comuns.
 *
 * "Modo professor" (ligado no começo): ele dá DICAS e explica, mas não entrega o exercício pronto.
 */
public class PainelHelper extends JPanel {

    /** O que o editor passa pro TazzHelper saber o que está acontecendo. */
    public interface Contexto {
        String nomeArquivo();
        String linguagem();
        String codigo();
        String console();
        List<VerificadorHtml.Problema> problemas();
    }

    private final JTextPane chat = new JTextPane();
    private final JTextArea entrada = new JTextArea(3, 20);
    private final JLabel status = new JLabel();
    private final JButton parar = Tema.botao("Parar", new Color(0x8A2F3B));
    private final ClienteClaude cliente = new ClienteClaude();
    private final List<ClienteClaude.Mensagem> conversa = new ArrayList<>();
    private final Contexto contexto;
    private boolean respondendo;

    // a resposta que está chegando agora (pra redesenhar enquanto chega)
    private int inicioResposta;
    private final StringBuilder resposta = new StringBuilder();
    private final javax.swing.Timer redesenhar;

    public PainelHelper(Contexto contexto) {
        super(new BorderLayout());
        this.contexto = contexto;
        setBackground(Tema.FUNDO_PAINEL);
        setPreferredSize(new Dimension(360, 0));

        // ----- topo -----
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.FUNDO_PAINEL);
        topo.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 6));
        JLabel titulo = new JLabel("◆ TazzHelper");
        titulo.setForeground(Tema.AZUL_CLARO);
        titulo.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD, 15f));
        JButton config = Tema.botao("Configurar", Tema.LINHA_ATUAL);
        config.addActionListener(e -> configurar());
        topo.add(titulo, BorderLayout.WEST);
        topo.add(config, BorderLayout.EAST);
        status.setForeground(Tema.TEXTO_FRACO);
        status.setFont(Tema.FONTE_UI.deriveFont(11f));
        topo.add(status, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        // ----- conversa -----
        chat.setEditable(false);
        chat.setBackground(Tema.FUNDO);
        chat.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane rol = new JScrollPane(chat);
        rol.setBorder(BorderFactory.createEmptyBorder());
        add(rol, BorderLayout.CENTER);

        // ----- botões rápidos + caixa de pergunta -----
        JPanel baixo = new JPanel(new BorderLayout(0, 6));
        baixo.setBackground(Tema.FUNDO_PAINEL);
        baixo.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        JPanel rapidos = new JPanel(new GridLayout(2, 2, 6, 6));
        rapidos.setOpaque(false);
        JButton explicarErro = Tema.botao("Explicar o erro", Tema.AZUL);
        JButton explicarCodigo = Tema.botao("Explicar meu código", Tema.LINHA_ATUAL);
        JButton dica = Tema.botao("Me dá uma dica", Tema.LINHA_ATUAL);
        JButton nova = Tema.botao("Nova conversa", Tema.LINHA_ATUAL);
        explicarErro.addActionListener(e -> explicarErro());
        explicarCodigo.addActionListener(e -> enviar("Explica o que o meu código faz, parte por parte, de um jeito simples."));
        dica.addActionListener(e -> enviar("Olhando o meu código, me dá UMA dica do próximo passo ou de algo que dá pra melhorar (sem fazer por mim)."));
        nova.addActionListener(e -> novaConversa());
        rapidos.add(explicarErro);
        rapidos.add(explicarCodigo);
        rapidos.add(dica);
        rapidos.add(nova);
        baixo.add(rapidos, BorderLayout.NORTH);

        entrada.setLineWrap(true);
        entrada.setWrapStyleWord(true);
        entrada.setBackground(Tema.FUNDO);
        entrada.setForeground(Tema.TEXTO);
        entrada.setCaretColor(Tema.AZUL_CLARO);
        entrada.setFont(Tema.FONTE_UI.deriveFont(14f));
        entrada.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        // ENTER manda; Shift+ENTER pula linha
        entrada.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "mandar");
        entrada.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");
        entrada.getActionMap().put("mandar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String t = entrada.getText().trim();
                if (!t.isEmpty() && !respondendo) {
                    entrada.setText("");
                    enviar(t);
                }
            }
        });
        JScrollPane rolEntrada = new JScrollPane(entrada);
        rolEntrada.setBorder(BorderFactory.createLineBorder(Tema.LINHA_ATUAL));
        baixo.add(rolEntrada, BorderLayout.CENTER);
        JPanel linhaParar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        linhaParar.setOpaque(false);
        JLabel dicaEnter = new JLabel("ENTER manda  •  Shift+ENTER pula linha   ");
        dicaEnter.setForeground(Tema.TEXTO_FRACO);
        dicaEnter.setFont(Tema.FONTE_UI.deriveFont(11f));
        parar.setVisible(false);
        parar.addActionListener(e -> {
            cliente.cancelar();
            ClienteLocal.cancelar();
        });
        linhaParar.add(dicaEnter);
        linhaParar.add(parar);
        baixo.add(linhaParar, BorderLayout.SOUTH);
        add(baixo, BorderLayout.SOUTH);

        redesenhar = new javax.swing.Timer(60, e -> redesenharResposta());
        redesenhar.setRepeats(false);

        atualizarStatus();
        boasVindas();
    }

    // =====================================================================
    //  CONVERSA
    // =====================================================================

    private boolean temChave() {
        return !Config.texto("helper.chave", "").isEmpty();
    }

    /** Qual cérebro está ligado: "local" (IA do TazzCode), "claude" (internet) ou "simples". */
    private String motor() {
        String m = Config.texto("helper.motor", "local");
        if (m.equals("claude") && !temChave()) return "local";
        return m;
    }

    private void atualizarStatus() {
        String m = motor();
        String texto = m.equals("claude") ? "Cérebro: Claude pela internet (" + Config.texto("helper.modelo", ClienteClaude.MODELOS[0]) + ")"
                : m.equals("simples") ? "Cérebro: simples (só explica erros comuns)"
                : "Cérebro: IA do TazzCode (no seu PC, sem internet)";
        status.setText(texto + (modoProfessor() ? " • modo professor" : ""));
    }

    private boolean modoProfessor() {
        return Config.texto("helper.professor", "sim").equals("sim");
    }

    private void boasVindas() {
        escreverMensagem("TazzHelper", "Oi! Eu sou o **TazzHelper**, a IA do " + App.NOME + ". "
                + (motor().equals("simples") ? "Rode o seu código (F5) e, se der erro, clique em **Explicar o erro**."
                : "Pode me perguntar sobre programação, pedir pra explicar o seu código ou o erro!")
                + (modoProfessor() ? "\n\nEstou no **modo professor**: eu dou dicas e explico, mas quem resolve é você." : ""), false);
    }

    private void novaConversa() {
        cliente.cancelar();
        conversa.clear();
        historicoLocal.clear();
        chat.setText("");
        boasVindas();
    }

    public void explicarErro() {
        if (motor().equals("simples")) {
            escreverMensagem("Você", "Explica o erro?", true);
            escreverMensagem("TazzHelper", AjudanteOffline.explicar(contexto.console(), contexto.problemas(), contexto.nomeArquivo()), false);
            return;
        }
        enviar("Deu erro (ou não funcionou como eu queria). Me explica o que aconteceu, olhando o console e o meu código.");
    }

    public void enviar(String pergunta) {
        if (respondendo) return;
        escreverMensagem("Você", pergunta, true);
        if (motor().equals("simples")) {
            escreverMensagem("TazzHelper", AjudanteOffline.responder(pergunta, contexto.console(), contexto.problemas(), contexto.nomeArquivo()), false);
            return;
        }
        if (motor().equals("local")) {
            enviarLocal(pergunta);
            return;
        }
        // A pergunta vai junto com o "contexto": o arquivo aberto, o console e os problemas.
        conversa.add(new ClienteClaude.Mensagem("user", montarContexto() + "\n\nPERGUNTA DO JOÃO:\n" + pergunta));
        while (conversa.size() > 20) conversa.remove(0);
        while (!conversa.isEmpty() && !conversa.get(0).papel.equals("user")) conversa.remove(0);

        respondendo = true;
        parar.setVisible(true);
        resposta.setLength(0);
        escreverCabecalho("TazzHelper");
        inicioResposta = chat.getDocument().getLength();
        inserir("…\n", estilo(Tema.TEXTO_FRACO, false, false, false));

        cliente.perguntar(Config.texto("helper.chave", ""), Config.texto("helper.modelo", ClienteClaude.MODELOS[0]),
                instrucoes(), new ArrayList<>(conversa),
                pedaco -> SwingUtilities.invokeLater(() -> {
                    resposta.append(pedaco);
                    if (!redesenhar.isRunning()) redesenhar.start();
                }),
                erro -> SwingUtilities.invokeLater(() -> {
                    respondendo = false;
                    parar.setVisible(false);
                    if (erro != null && resposta.length() == 0) resposta.append("Ops: ").append(erro);
                    else if (erro != null) resposta.append("\n\n(").append(erro).append(")");
                    redesenharResposta();
                    if (erro == null) conversa.add(new ClienteClaude.Mensagem("assistant", resposta.toString()));
                    else if (!conversa.isEmpty()) conversa.remove(conversa.size() - 1);   // tira a pergunta que falhou
                }));
    }

    private final List<ClienteClaude.Mensagem> historicoLocal = new ArrayList<>();

    /** Pergunta pra IA própria do TazzCode (Python, no PC). */
    private void enviarLocal(String pergunta) {
        java.util.Map<String, Object> ctx = new java.util.LinkedHashMap<>();
        ctx.put("arquivo", contexto.nomeArquivo());
        ctx.put("linguagem", contexto.linguagem());
        ctx.put("codigo", contexto.codigo());
        ctx.put("console", contexto.console());
        List<Object> ps = new ArrayList<>();
        if (contexto.problemas() != null) for (VerificadorHtml.Problema p : contexto.problemas()) {
            if (p.gravidade != VerificadorHtml.Gravidade.DICA) ps.add(p.mensagem);
        }
        ctx.put("problemas", ps);
        ctx.put("professor", modoProfessor());

        respondendo = true;
        parar.setVisible(true);
        resposta.setLength(0);
        escreverCabecalho("TazzHelper");
        inicioResposta = chat.getDocument().getLength();
        inserir("…\n", estilo(Tema.TEXTO_FRACO, false, false, false));
        status.setText("Ligando a IA do TazzCode...");

        ClienteLocal.perguntar(pergunta, ctx, new ArrayList<>(historicoLocal),
                pedaco -> SwingUtilities.invokeLater(() -> {
                    resposta.append(pedaco);
                    if (!redesenhar.isRunning()) redesenhar.start();
                }),
                aviso -> SwingUtilities.invokeLater(() -> status.setText(aviso)),
                quem -> SwingUtilities.invokeLater(() -> {
                    respondendo = false;
                    parar.setVisible(false);
                    atualizarStatus();
                    if (quem.startsWith("ERRO: ")) {
                        String erro = quem.substring(6);
                        if (erro.equals("SEM_PYTHON")) {
                            resposta.setLength(0);
                            resposta.append("A IA do TazzCode é feita em **Python**, e não achei o Python no seu PC.\n\n"
                                    + "Instale o **plugin do Python** (menu **Plugins**) e pergunte de novo!\n\n"
                                    + "Enquanto isso, olha o que o cérebro simples sabe:\n\n")
                                    .append(AjudanteOffline.responder(pergunta, contexto.console(), contexto.problemas(), contexto.nomeArquivo()));
                        } else if (resposta.length() == 0) {
                            resposta.append("Ops: ").append(erro);
                        }
                    } else {
                        if (quem.equals("mini-ia")) resposta.append("\n\n_(resposta da mini IA: ela é pequena e pode errar!)_");
                        historicoLocal.add(new ClienteClaude.Mensagem("user", pergunta));
                        historicoLocal.add(new ClienteClaude.Mensagem("assistant", resposta.toString()));
                        while (historicoLocal.size() > 12) historicoLocal.remove(0);
                    }
                    redesenharResposta();
                }));
    }

    /** As instruções pra IA (o "system prompt"). */
    private String instrucoes() {
        return "Você é o TazzHelper, o assistente de programação que mora dentro do TazzCode, um editor de código "
                + "que o João (um estudante jovem que está aprendendo Java, HTML, CSS e outras linguagens) criou com ajuda do Claude. "
                + "Responda sempre em português do Brasil, de um jeito simples, animado e curto, como um bom professor. "
                + "Use exemplos pequenos. Explique o PORQUÊ das coisas. Quando falar de um erro, diga a linha e o motivo. "
                + "Formate com **negrito**, `código` e blocos ``` quando ajudar. "
                + (modoProfessor()
                    ? "MODO PROFESSOR (ligado): o João pediu para NÃO receber respostas prontas de exercícios. "
                    + "Dê dicas, faça perguntas que guiem o raciocínio e explique conceitos; mostre no máximo trechos pequenos "
                    + "(1 a 3 linhas) de código, nunca o programa ou o exercício resolvido inteiro. "
                    : "MODO DIRETO: pode mostrar o código completo quando pedirem, mas explique o que ele faz. ")
                + "Se a pergunta não tiver nada a ver com programação ou estudos, responda com gentileza e traga a conversa de volta pro código. "
                + "O TazzCode tem: F5 roda, console embaixo, aba Problemas (verificador de HTML com Alt+Enter), bancos de dados SIMULADOS "
                + "(SQL, MongoDB, Redis...), modelos de projeto (React, Flask...), e o TazzHelper (você).";
    }

    private String montarContexto() {
        StringBuilder sb = new StringBuilder("[O QUE ESTÁ ABERTO NO TAZZCODE]\n");
        sb.append("Arquivo: ").append(contexto.nomeArquivo() == null ? "(sem nome)" : contexto.nomeArquivo())
                .append(" | Linguagem: ").append(contexto.linguagem()).append("\n");
        String codigo = contexto.codigo();
        if (codigo != null && !codigo.isEmpty()) {
            String[] linhas = codigo.split("\n", -1);
            StringBuilder numerado = new StringBuilder();
            for (int k = 0; k < linhas.length && numerado.length() < 14000; k++) numerado.append(k + 1).append(": ").append(linhas[k]).append("\n");
            sb.append("Código (com número das linhas):\n```\n").append(numerado).append("```\n");
        }
        String c = contexto.console();
        if (c != null && !c.trim().isEmpty()) {
            if (c.length() > 4000) c = "..." + c.substring(c.length() - 4000);
            sb.append("Console (última execução):\n```\n").append(c).append("\n```\n");
        }
        List<VerificadorHtml.Problema> ps = contexto.problemas();
        if (ps != null && !ps.isEmpty()) {
            sb.append("Problemas que o verificador achou:\n");
            for (VerificadorHtml.Problema p : ps) sb.append("- ").append(p.gravidade).append(": ").append(p.mensagem).append("\n");
        }
        return sb.toString();
    }

    // =====================================================================
    //  DESENHAR AS MENSAGENS (com um "markdown" simples: **negrito**, `código`, ```blocos```)
    // =====================================================================

    private void escreverMensagem(String quem, String texto, boolean usuario) {
        escreverCabecalho(quem);
        inserirMarkdown(texto, usuario);
        inserir("\n", estilo(Tema.TEXTO, false, false, false));
    }

    private void escreverCabecalho(String quem) {
        boolean usuario = quem.equals("Você");
        if (chat.getDocument().getLength() > 0) inserir("\n", estilo(Tema.TEXTO, false, false, false));
        inserir((usuario ? "● " : "◆ ") + quem + "\n", estilo(usuario ? Tema.SUCESSO : Tema.AZUL_CLARO, true, false, false));
    }

    private void redesenharResposta() {
        StyledDocument doc = chat.getStyledDocument();
        try {
            doc.remove(inicioResposta, doc.getLength() - inicioResposta);
        } catch (BadLocationException ignorado) { }
        inserirMarkdown(resposta.length() == 0 ? "…" : resposta.toString(), false);
        inserir("\n", estilo(Tema.TEXTO, false, false, false));
    }

    private void inserirMarkdown(String texto, boolean usuario) {
        Color normal = usuario ? Tema.TEXTO_FRACO.brighter() : Tema.TEXTO;
        boolean dentroBloco = false;
        for (String linha : texto.split("\n", -1)) {
            if (linha.trim().startsWith("```")) {
                dentroBloco = !dentroBloco;
                continue;
            }
            if (dentroBloco) {
                inserir(linha + "\n", estilo(Tema.AZUL_CLARO, false, false, true));
                continue;
            }
            String l = linha;
            if (l.startsWith("### ") || l.startsWith("## ") || l.startsWith("# ")) {
                inserir(l.replaceFirst("^#+ ", "") + "\n", estilo(Tema.AZUL_CLARO, true, false, false));
                continue;
            }
            if (l.startsWith("- ") || l.startsWith("* ")) l = "• " + l.substring(2);
            if (l.length() > 2 && l.startsWith("_") && l.endsWith("_")) {   // _texto_ = itálico cinza
                inserir(l.substring(1, l.length() - 1) + "\n", estilo(Tema.TEXTO_FRACO, false, true, false));
                continue;
            }
            // **negrito** e `código`
            int k = 0;
            while (k < l.length()) {
                if (l.startsWith("**", k)) {
                    int f = l.indexOf("**", k + 2);
                    if (f > k) { inserir(l.substring(k + 2, f), estilo(Tema.TEXTO, true, false, false)); k = f + 2; continue; }
                }
                if (l.charAt(k) == '`') {
                    int f = l.indexOf('`', k + 1);
                    if (f > k) { inserir(l.substring(k + 1, f), estilo(Tema.AZUL_CLARO, false, false, true)); k = f + 1; continue; }
                }
                int prox = proximoEspecial(l, k + 1);
                inserir(l.substring(k, prox), estilo(normal, false, usuario, false));
                k = prox;
            }
            inserir("\n", estilo(normal, false, false, false));
        }
        chat.setCaretPosition(chat.getDocument().getLength());
    }

    private static int proximoEspecial(String l, int de) {
        int a = l.indexOf("**", de), b = l.indexOf('`', de);
        int m = l.length();
        if (a >= 0) m = Math.min(m, a);
        if (b >= 0) m = Math.min(m, b);
        return m;
    }

    private void inserir(String s, SimpleAttributeSet estilo) {
        try {
            chat.getStyledDocument().insertString(chat.getDocument().getLength(), s, estilo);
        } catch (BadLocationException ignorado) { }
    }

    private static SimpleAttributeSet estilo(Color cor, boolean negrito, boolean italico, boolean codigo) {
        SimpleAttributeSet s = new SimpleAttributeSet();
        StyleConstants.setForeground(s, cor);
        StyleConstants.setBold(s, negrito);
        StyleConstants.setItalic(s, italico);
        StyleConstants.setFontFamily(s, codigo ? Font.MONOSPACED : "SansSerif");
        StyleConstants.setFontSize(s, codigo ? 13 : 14);
        if (codigo) StyleConstants.setBackground(s, Tema.FUNDO_CONSOLE);
        return s;
    }

    // =====================================================================
    //  CONFIGURAR (chave, modelo, modo professor)
    // =====================================================================

    private void configurar() {
        String[] nomes = {"IA do TazzCode (no seu PC, grátis, sem internet)", "Claude pela internet (precisa de chave paga)", "Simples (só explica erros comuns)"};
        String[] codigos = {"local", "claude", "simples"};
        JComboBox<String> cerebro = new JComboBox<>(nomes);
        cerebro.setSelectedIndex(Arrays.asList(codigos).indexOf(Config.texto("helper.motor", "local")) < 0 ? 0
                : Arrays.asList(codigos).indexOf(Config.texto("helper.motor", "local")));
        JPasswordField chave = new JPasswordField(Config.texto("helper.chave", ""), 32);
        JComboBox<String> modelo = new JComboBox<>(ClienteClaude.MODELOS);
        modelo.setSelectedItem(Config.texto("helper.modelo", ClienteClaude.MODELOS[0]));
        JCheckBox professor = new JCheckBox("Modo professor (dá dicas em vez de respostas prontas)", modoProfessor());

        java.util.Map<?, ?> saude = ClienteLocal.saude();
        boolean miniInstalada = new File(ClienteLocal.pastaDaIA(), "instalado.json").isFile()
                || (saude != null && Boolean.TRUE.equals(saude.get("miniIA")));
        JButton instalarMini = Tema.botao(miniInstalada ? "Reinstalar a mini IA" : "Instalar a mini IA (~280 MB)", Tema.AZUL);
        instalarMini.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(instalarMini).dispose();
            instalarMiniIA();
        });

        Object[] campos = {
                "<html><b>Cérebro do TazzHelper</b></html>", cerebro,
                "<html><b>IA do TazzCode</b>: feita em Python, roda no seu PC. Tem 2 partes:<br>"
                        + "• o <b>especialista</b> (já vem pronto): erros, conceitos, explicar código, dicas<br>"
                        + "• a <b>mini IA</b> (Gemma 3 270M, opcional): conversa sobre qualquer coisa, mas pode errar<br>"
                        + "Mini IA: " + (miniInstalada ? "<font color='green'><b>instalada</b></font>" : "<b>não instalada</b>")
                        + " &nbsp; (pasta: " + ClienteLocal.pastaDaIA() + ")</html>",
                instalarMini,
                " ",
                "<html><b>Chave da API da Anthropic</b> (só pro cérebro \"Claude\": é paga, peça pra um adulto)</html>", chave,
                "<html><b>Modelo do Claude</b></html>", modelo,
                professor
        };
        int r = JOptionPane.showConfirmDialog(this, campos, "Configurar o TazzHelper", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        Config.colocar("helper.motor", codigos[cerebro.getSelectedIndex()]);
        Config.colocar("helper.chave", new String(chave.getPassword()).trim());
        Config.colocar("helper.modelo", (String) modelo.getSelectedItem());
        Config.colocar("helper.professor", professor.isSelected() ? "sim" : "nao");
        atualizarStatus();
        if (codigos[cerebro.getSelectedIndex()].equals("claude") && !temChave()) {
            escreverMensagem("TazzHelper", "Pra usar o Claude precisa da chave. Por enquanto fico com a IA do TazzCode.", false);
        } else {
            escreverMensagem("TazzHelper", "Pronto! " + status.getText(), false);
        }
    }

    /** Baixa a mini IA (llama.cpp + Gemma) mostrando o progresso aqui no chat. */
    private void instalarMiniIA() {
        escreverMensagem("TazzHelper", "Vou baixar a **mini IA** (Gemma 3 270M + llama.cpp, ~280 MB). Pode demorar uns minutos...", false);
        status.setText("Instalando a mini IA...");
        ClienteLocal.instalarMiniIA(
                linha -> SwingUtilities.invokeLater(() -> {
                    if (linha.startsWith("PROGRESSO ")) {
                        String[] p = linha.split(" ", 3);
                        status.setText(p.length > 2 ? p[2] + " (" + p[1] + "%)" : linha);
                    }
                }),
                erro -> SwingUtilities.invokeLater(() -> {
                    atualizarStatus();
                    if (erro == null) escreverMensagem("TazzHelper", "**Mini IA instalada!** Agora eu também converso sobre qualquer assunto (mas lembre: ela é pequena e pode errar).", false);
                    else if (erro.equals("SEM_PYTHON")) escreverMensagem("TazzHelper", "Preciso do **Python** pra instalar. Instale o plugin do Python (menu **Plugins**) e tente de novo.", false);
                    else escreverMensagem("TazzHelper", "Não deu pra instalar: " + erro + "\n\nTem internet? Tente de novo em **Configurar**.", false);
                }));
    }
}
