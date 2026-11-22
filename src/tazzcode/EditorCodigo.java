package tazzcode;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * A ÁREA DE ESCREVER CÓDIGO. Ela sabe:
 *   - pintar o código com cores (usa o Destaque)
 *   - fechar sozinha ( ) [ ] { } " " e as tags do HTML
 *   - manter o recuo quando aperta ENTER
 *   - mostrar o autocompletar (usa o Autocompletar)
 *   - Ctrl+/ comenta a linha, Ctrl+D duplica a linha, Shift+TAB tira o recuo
 */
public class EditorCodigo extends JTextPane {

    private static final Set<String> TAGS_SEM_FECHAR = new HashSet<>(Arrays.asList(
            "br", "img", "meta", "link", "input", "hr", "area", "base", "col", "source", "wbr", "embed", "track", "param",
            "doctype"));   // <DOCTYPE html> e <!DOCTYPE html> NUNCA ganham </doctype>

    private String extensao = "";
    private Linguagem linguagem = Catalogo.TEXTO;
    private String nomeClasse = "Main";
    private final Destaque destaque;
    private final Autocompletar auto;
    private final Timer timerCores;
    boolean inserindoSozinho = false;   // true quando o próprio editor está inserindo texto
    private java.util.List<VerificadorHtml.Problema> problemas = new java.util.ArrayList<>();
    private final java.util.List<Object> marcasDeProblema = new java.util.ArrayList<>();
    private Runnable aoVerificar = () -> { };

    public EditorCodigo() {
        setFont(Tema.fonteCodigo());
        setBackground(Tema.FUNDO);
        setForeground(Tema.TEXTO);
        setCaretColor(Tema.AZUL_CLARO);
        setSelectionColor(Tema.SELECAO);
        setSelectedTextColor(null);   // mantém as cores do código mesmo selecionado
        setOpaque(false);             // a gente mesmo pinta o fundo (pra destacar a linha atual)
        setMargin(new java.awt.Insets(4, 8, 4, 8));

        destaque = new Destaque(getStyledDocument());
        auto = new Autocompletar(this);

        // Repinta as cores um pouquinho depois de você parar de digitar.
        timerCores = new Timer(120, e -> {
            destaque.pintar(linguagem);
            verificar();
        });
        timerCores.setRepeats(false);

        getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                timerCores.restart();
                if (!inserindoSozinho && e.getLength() == 1) {
                    SwingUtilities.invokeLater(() -> auto.atualizar(false));
                }
            }
            public void removeUpdate(DocumentEvent e) {
                timerCores.restart();
                if (auto.visivel()) SwingUtilities.invokeLater(() -> auto.atualizar(false));
            }
            public void changedUpdate(DocumentEvent e) { }
        });
        addCaretListener(e -> repaint());
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                auto.esconder();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                auto.esconder();
            }
        });

        configurarTeclas();
        setToolTipText("");   // liga as dicas do mouse (mostram o erro em cima da ondinha)
    }

    // =====================================================================
    //  linguagem
    // =====================================================================

    public void setExtensao(String extensao, String nomeArquivo) {
        this.extensao = extensao;
        this.linguagem = Catalogo.porArquivo(new java.io.File(nomeArquivo));
        int ponto = nomeArquivo.lastIndexOf('.');
        this.nomeClasse = ponto > 0 ? nomeArquivo.substring(0, ponto) : nomeArquivo;
        destaque.pintar(linguagem);
    }

    public String getExtensao()   { return extensao; }
    public Linguagem getLinguagem() { return linguagem; }
    public String getNomeClasse() { return nomeClasse; }

    public void pintarAgora() {
        auto.esconder();
        destaque.pintar(linguagem);
        verificar();
    }

    // =====================================================================
    //  PROBLEMAS (por enquanto, o verificador de HTML)
    // =====================================================================

    /** Procura problemas no código e sublinha com uma "ondinha". */
    public void verificar() {
        for (Object m : marcasDeProblema) getHighlighter().removeHighlight(m);
        marcasDeProblema.clear();
        boolean html = ehHtml() && (extensao.equals("html") || extensao.equals("htm"));
        problemas = html ? VerificadorHtml.verificar(texto()) : new java.util.ArrayList<>();
        for (VerificadorHtml.Problema p : problemas) {
            if (p.gravidade == VerificadorHtml.Gravidade.DICA) continue;   // dica não ganha ondinha
            java.awt.Color cor = p.gravidade == VerificadorHtml.Gravidade.ERRO ? Tema.ERRO : new java.awt.Color(0xE5C07B);
            try {
                int fim = Math.min(p.fim, getDocument().getLength());
                if (fim > p.inicio) marcasDeProblema.add(getHighlighter().addHighlight(p.inicio, fim, new Ondinha(cor)));
            } catch (BadLocationException ignorado) { }
        }
        aoVerificar.run();
    }

    public java.util.List<VerificadorHtml.Problema> getProblemas() { return problemas; }
    public void setAoVerificar(Runnable r) { aoVerificar = r; }

    /** Aplica a correção rápida de um problema. */
    public void corrigir(VerificadorHtml.Problema p) {
        if (!p.temCorrecao()) return;
        try {
            int fim = Math.min(p.corrigeFim, getDocument().getLength());
            getDocument().remove(p.corrigeInicio, fim - p.corrigeInicio);
            getDocument().insertString(p.corrigeInicio, p.corrigePor, null);
            setCaretPosition(Math.min(p.corrigeInicio + p.corrigePor.length(), getDocument().getLength()));
        } catch (BadLocationException ignorado) { }
        pintarAgora();
    }

    /** Corrige todos (do fim pro começo, pra uma correção não bagunçar a posição da outra). */
    public int corrigirTudo() {
        int feitas = 0;
        for (int volta = 0; volta < 20; volta++) {
            VerificadorHtml.Problema alvo = null;
            for (VerificadorHtml.Problema p : problemas) {
                if (p.temCorrecao() && p.gravidade != VerificadorHtml.Gravidade.DICA && (alvo == null || p.corrigeInicio > alvo.corrigeInicio)) alvo = p;
            }
            if (alvo == null) break;
            corrigir(alvo);
            feitas++;
        }
        return feitas;
    }

    private VerificadorHtml.Problema problemaEm(int pos) {
        for (VerificadorHtml.Problema p : problemas) if (pos >= p.inicio && pos <= p.fim) return p;
        return null;
    }

    @Override
    public String getToolTipText(java.awt.event.MouseEvent e) {
        VerificadorHtml.Problema p = problemaEm(viewToModel2D(e.getPoint()));
        if (p == null) return null;
        String txt = "<html><b>" + (p.gravidade == VerificadorHtml.Gravidade.ERRO ? "Erro" : "Aviso") + ":</b> "
                + p.mensagem.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        if (p.temCorrecao()) txt += "<br><i>Alt+Enter: " + p.comoCorrige.replace("<", "&lt;").replace(">", "&gt;") + "</i>";
        return txt + "</html>";
    }

    /** A "ondinha" vermelha/amarela embaixo do erro. */
    private static class Ondinha implements javax.swing.text.Highlighter.HighlightPainter {
        private final java.awt.Color cor;
        Ondinha(java.awt.Color cor) { this.cor = cor; }

        @Override
        public void paint(Graphics g, int p0, int p1, java.awt.Shape area, javax.swing.text.JTextComponent c) {
            try {
                Rectangle2D a = c.modelToView2D(p0);
                Rectangle2D b = c.modelToView2D(p1);
                int x0 = (int) a.getX();
                int x1 = (int) (b.getY() == a.getY() ? b.getX() : c.getWidth() - 10);
                int y = (int) (a.getY() + a.getHeight()) - 2;
                g.setColor(cor);
                for (int x = x0; x < x1; x += 4) {
                    g.drawLine(x, y, x + 2, y - 2);
                    g.drawLine(x + 2, y - 2, x + 4, y);
                }
            } catch (BadLocationException ignorado) { }
        }
    }

    private boolean ehCodigo() {
        return linguagem != Catalogo.TEXTO && linguagem.modo != Linguagem.Modo.BRAINFUCK
                && !linguagem.nome.equals("Markdown");
    }

    private boolean ehHtml() {
        return linguagem.estilo == Linguagem.Estilo.HTML;
    }

    // =====================================================================
    //  desenho: sem quebra de linha automática + linha atual destacada
    // =====================================================================

    @Override
    public boolean getScrollableTracksViewportWidth() {
        // false = linhas compridas não quebram (aparece a barra de rolagem de lado)
        if (Ajustes.quebrarLinha()) return true;   // Configurações > quebrar linhas compridas
        return getParent() == null || getUI().getPreferredSize(this).width <= getParent().getSize().width;
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Tema.FUNDO);
        g.fillRect(0, 0, getWidth(), getHeight());
        try {
            Rectangle2D r = modelToView2D(getCaretPosition());
            if (r != null && Ajustes.destacarLinha()) {
                g.setColor(Tema.LINHA_ATUAL);
                g.fillRect(0, (int) r.getY(), getWidth(), (int) r.getHeight());
            }
        } catch (BadLocationException ignorado) { }
        super.paintComponent(g);
    }

    // =====================================================================
    //  ajudantes de texto
    // =====================================================================

    /** O texto do documento, sempre com \n (nunca \r\n), batendo com as posições do cursor. */
    public String texto() {
        try {
            return getDocument().getText(0, getDocument().getLength());
        } catch (BadLocationException e) {
            return "";
        }
    }

    private char charEm(int pos) {
        String t = texto();
        return (pos >= 0 && pos < t.length()) ? t.charAt(pos) : 0;
    }

    public int linhaDe(int pos) {
        return getDocument().getDefaultRootElement().getElementIndex(pos);
    }

    public int inicioDaLinha(int linha) {
        return getDocument().getDefaultRootElement().getElement(linha).getStartOffset();
    }

    public int fimDaLinha(int linha) {
        Element e = getDocument().getDefaultRootElement().getElement(linha);
        return Math.min(e.getEndOffset() - 1, getDocument().getLength());
    }

    /** Os espaços do começo da linha onde está a posição. */
    public String recuoDaLinha(int pos) {
        String t = texto();
        int inicio = inicioDaLinha(linhaDe(pos));
        int i = inicio;
        while (i < t.length() && (t.charAt(i) == ' ' || t.charAt(i) == '\t')) i++;
        return t.substring(inicio, i);
    }

    private void inserir(String s) {
        inserindoSozinho = true;
        replaceSelection(s);
        inserindoSozinho = false;
    }

    // =====================================================================
    //  teclas
    // =====================================================================

    private void configurarTeclas() {
        // fechar pares
        tecla('(', () -> fecharPar("(", ")"));
        tecla('[', () -> fecharPar("[", "]"));
        tecla('{', () -> fecharPar("{", "}"));
        tecla('"', () -> aspas('"'));
        tecla('\'', () -> aspas('\''));
        tecla(')', () -> pularSeIgual(')'));
        tecla(']', () -> pularSeIgual(']'));
        tecla('}', () -> pularSeIgual('}'));
        tecla('>', this::fecharTagHtml);

        sobrescrever("ENTER", () -> {
            if (auto.visivel()) { auto.aceitar(); return true; }
            enterComRecuo();
            return true;
        });
        sobrescrever("TAB", () -> {
            if (auto.visivel()) { auto.aceitar(); return true; }
            inserir(Ajustes.recuo());
            return true;
        });
        sobrescrever("shift TAB", () -> { tirarRecuo(); return true; });
        sobrescrever("BACK_SPACE", this::apagarPar);
        sobrescrever("UP", () -> { if (auto.visivel()) { auto.mover(-1); return true; } return false; });
        sobrescrever("DOWN", () -> { if (auto.visivel()) { auto.mover(+1); return true; } return false; });
        sobrescrever("ESCAPE", () -> { auto.esconder(); return true; });
        sobrescrever("ctrl SPACE", () -> { auto.atualizar(true); return true; });
        sobrescrever("ctrl SLASH", () -> { comentarLinha(); return true; });
        sobrescrever("ctrl D", () -> { duplicarLinha(); return true; });
        sobrescrever("alt ENTER", () -> {
            VerificadorHtml.Problema p = problemaEm(getCaretPosition());
            if (p != null && p.temCorrecao()) corrigir(p);
            return true;
        });
    }

    /** Liga uma tecla de "letra digitada" a uma ação. Se a ação devolver false, digita normal. */
    private void tecla(char c, BooleanSupplier acao) {
        String nome = "tecla_" + (int) c;
        getInputMap().put(KeyStroke.getKeyStroke(c), nome);
        getActionMap().put(nome, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!isEditable()) return;
                if (!acao.getAsBoolean()) inserir(String.valueOf(c));
            }
        });
    }

    /** Troca o que uma tecla faz; se a ação devolver false, faz o que ela fazia antes. */
    private void sobrescrever(String teclaTexto, BooleanSupplier acao) {
        KeyStroke ks = KeyStroke.getKeyStroke(teclaTexto);
        InputMap im = getInputMap();
        Object antigoNome = im.get(ks);
        Action antiga = antigoNome == null ? null : getActionMap().get(antigoNome);
        String nome = "nova_" + teclaTexto;
        im.put(ks, nome);
        getActionMap().put(nome, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!acao.getAsBoolean() && antiga != null) antiga.actionPerformed(e);
            }
        });
    }

    // ----- pares ( ) [ ] { } -----

    private boolean fecharPar(String abre, String fecha) {
        if (!ehCodigo() || !Ajustes.fecharPares()) return false;
        String selecionado = getSelectedText();
        int pos = getSelectionStart();
        if (selecionado != null) {                      // envolve o que está selecionado
            inserir(abre + selecionado + fecha);
            select(pos + 1, pos + 1 + selecionado.length());
            return true;
        }
        inserir(abre + fecha);
        setCaretPosition(pos + 1);
        return true;
    }

    private boolean aspas(char a) {
        if (!ehCodigo() || !Ajustes.fecharPares()) return false;
        int pos = getCaretPosition();
        if (getSelectedText() == null && charEm(pos) == a) {   // já tem a aspa: só pula ela
            setCaretPosition(pos + 1);
            return true;
        }
        if (linguagem.aspas.indexOf(a) < 0 && !ehHtml() && linguagem.estilo != Linguagem.Estilo.CSS) return false;   // ex.: ' no Lisp
        char antes = charEm(pos - 1);
        if (Character.isLetterOrDigit(antes)) return false;    // ex.: don't
        String s = String.valueOf(a);
        return fecharPar(s, s);
    }

    private boolean pularSeIgual(char c) {
        int pos = getCaretPosition();
        if (ehCodigo() && getSelectedText() == null && charEm(pos) == c) {
            setCaretPosition(pos + 1);
            return true;
        }
        return false;
    }

    private boolean apagarPar() {
        int pos = getCaretPosition();
        if (!ehCodigo() || getSelectedText() != null || pos == 0) return false;
        String par = "" + charEm(pos - 1) + charEm(pos);
        if (par.equals("()") || par.equals("[]") || par.equals("{}") || par.equals("\"\"") || par.equals("''")) {
            try {
                getDocument().remove(pos - 1, 2);
                return true;
            } catch (BadLocationException ignorado) { }
        }
        return false;
    }

    // ----- HTML: <div> vira <div></div> -----

    private boolean fecharTagHtml() {
        if (!ehHtml() || !Ajustes.fecharPares()) return false;
        int pos = getCaretPosition();
        String t = texto();
        int abre = t.lastIndexOf('<', pos - 1);
        if (abre < 0 || t.lastIndexOf('>', pos - 1) > abre) return false;
        String dentro = t.substring(abre + 1, pos);
        inserir(">");
        if (dentro.isEmpty() || dentro.startsWith("/") || dentro.startsWith("!") || dentro.startsWith("?") || dentro.endsWith("/")) return true;
        // O nome da tag tem que vir colado no "<" (ex.: <div class="x">).
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([A-Za-z][A-Za-z0-9-]*)").matcher(dentro);
        if (!m.find()) return true;
        String nome = m.group(1).toLowerCase();
        if (TAGS_SEM_FECHAR.contains(nome)) return true;
        int depois = getCaretPosition();
        inserir("</" + nome + ">");
        setCaretPosition(depois);
        return true;
    }

    // ----- ENTER -----

    private void enterComRecuo() {
        int pos = getCaretPosition();
        String recuo = recuoDaLinha(pos);
        String t = texto();

        int i = pos - 1;
        while (i >= 0 && t.charAt(i) == ' ') i--;
        char antes = i >= 0 ? t.charAt(i) : 0;
        char depois = charEm(pos);

        boolean doisPontos = antes == ':' && (linguagem.nome.equals("Python") || linguagem.nome.equals("Mojo")
                || linguagem.nome.equals("Nim") || linguagem.nome.startsWith("GDScript"));
        boolean abriu = antes == '{' || antes == '(' || antes == '[' || doisPontos;
        boolean tagAberta = antes == '>' && depois == '<' && t.startsWith("</", pos);

        if ((abriu && (depois == '}' || depois == ')' || depois == ']')) || tagAberta) {
            // {|}  vira:
            // {
            //     |
            // }
            inserir("\n" + recuo + Ajustes.recuo());
            int cursor = getCaretPosition();
            inserir("\n" + recuo);
            setCaretPosition(cursor);
        } else if (abriu) {
            inserir("\n" + recuo + Ajustes.recuo());
        } else {
            inserir("\n" + recuo);
        }
    }

    // ----- Shift+TAB, Ctrl+/, Ctrl+D -----

    private void tirarRecuo() {
        int linha = linhaDe(getCaretPosition());
        int inicio = inicioDaLinha(linha);
        String t = texto();
        int n = 0;
        while (n < Ajustes.tamanhoTab() && inicio + n < t.length() && t.charAt(inicio + n) == ' ') n++;
        if (n > 0) {
            try {
                getDocument().remove(inicio, n);
            } catch (BadLocationException ignorado) { }
        }
    }

    private void comentarLinha() {
        // Usa o comentário de linha da linguagem (// no Java, # no Python, -- no SQL...)
        if (linguagem.comentarioLinha.isEmpty()) return;   // (HTML e CSS: fica de desafio!)
        String marca = linguagem.comentarioLinha.get(0) + " ";
        int linha = linhaDe(getCaretPosition());
        int inicio = inicioDaLinha(linha);
        int fim = fimDaLinha(linha);
        String conteudo = texto().substring(inicio, fim);
        String semRecuo = conteudo.stripLeading();
        int recuo = conteudo.length() - semRecuo.length();
        try {
            if (semRecuo.startsWith(marca)) {
                getDocument().remove(inicio + recuo, marca.length());
            } else if (semRecuo.startsWith(marca.trim())) {
                getDocument().remove(inicio + recuo, marca.trim().length());
            } else {
                getDocument().insertString(inicio + recuo, marca, null);
            }
        } catch (BadLocationException ignorado) { }
    }

    private void duplicarLinha() {
        int linha = linhaDe(getCaretPosition());
        int inicio = inicioDaLinha(linha);
        int fim = fimDaLinha(linha);
        int coluna = getCaretPosition() - inicio;
        String conteudo = texto().substring(inicio, fim);
        try {
            getDocument().insertString(fim, "\n" + conteudo, null);
            setCaretPosition(fim + 1 + coluna);
        } catch (BadLocationException ignorado) { }
    }

}
