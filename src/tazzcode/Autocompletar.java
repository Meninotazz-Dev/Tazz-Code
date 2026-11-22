package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AUTOCOMPLETAR: a listinha que aparece enquanto você digita (igual ao IntelliJ).
 * Digite "sout" e aperte ENTER ou TAB -> vira System.out.println();
 *
 * Nos textos dos atalhos, "$|" marca onde o cursor vai ficar depois de completar.
 *
 * Os atalhos de cada linguagem ficam no Catalogo (procure ".atalho(").
 * DESAFIO: crie os seus próprios atalhos lá!
 */
public class Autocompletar {

    /** Uma sugestão da lista. */
    static class Sugestao {
        final String rotulo;   // o que aparece na lista (e o que você digita)
        final String dica;     // explicação em cinza
        final String inserir;  // o que vai pro código

        Sugestao(String rotulo, String dica, String inserir) {
            this.rotulo = rotulo;
            this.dica = dica;
            this.inserir = inserir;
        }
    }

    private static final Pattern PALAVRA = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{2,}");

    private final EditorCodigo editor;
    private JWindow janela;
    private final JList<Sugestao> lista = new JList<>();
    private int inicioPalavra;

    public Autocompletar(EditorCodigo editor) {
        this.editor = editor;
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setBackground(Tema.FUNDO_PAINEL);
        lista.setForeground(Tema.TEXTO);
        lista.setSelectionBackground(Tema.SELECAO);
        lista.setSelectionForeground(Tema.TEXTO);
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                Sugestao s = (Sugestao) v;
                String html = "<html><b>" + escapar(s.rotulo) + "</b>&nbsp;&nbsp;<font color='#7D8BA6'>"
                        + escapar(s.dica) + "</font></html>";
                JLabel c = (JLabel) super.getListCellRendererComponent(l, html, i, sel, foco);
                c.setFont(editor.getFont().deriveFont(13f));
                c.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 12));
                return c;
            }
        });
        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) aceitar();
            }
        });
    }

    // =====================================================================
    //  mostrar / esconder
    // =====================================================================

    public boolean visivel() {
        return janela != null && janela.isVisible();
    }

    public void esconder() {
        if (janela != null) janela.setVisible(false);
    }

    /** Olha a palavra antes do cursor e mostra (ou esconde) as sugestões. */
    public void atualizar(boolean forcar) {
        String texto = editor.texto();   // getText() no Windows vem com \r\n e bagunça as posições
        int cursor = editor.getCaretPosition();
        int i = cursor;
        Linguagem lg = editor.getLinguagem();
        boolean html = lg.estilo == Linguagem.Estilo.HTML;
        while (i > 0 && (Character.isJavaIdentifierPart(texto.charAt(i - 1))
                || (html && texto.charAt(i - 1) == '!')
                || (lg.estilo == Linguagem.Estilo.CSS && texto.charAt(i - 1) == '-'))) i--;
        String prefixo = texto.substring(i, cursor);
        inicioPalavra = i;

        if (!forcar && !Ajustes.autocompletar()) {   // desligado nas Configurações (Ctrl+Espaço ainda funciona)
            esconder();
            return;
        }
        if (prefixo.isEmpty() && !forcar) {
            esconder();
            return;
        }
        // Depois de "class", "interface", "enum"... você está INVENTANDO um nome: não tem o que sugerir.
        // (Era isso que fazia "public class Main" + Enter virar o atalho "main"!)
        if (!forcar && !html && depoisDeDeclaracao(texto, i)) {
            esconder();
            return;
        }
        // Com letra maiúscula, a sugestão tem que bater certinho (Main não é main).
        boolean exato = !prefixo.equals(prefixo.toLowerCase());

        List<Sugestao> achadas = new ArrayList<>();
        Set<String> jaTem = new LinkedHashSet<>();
        // No HTML: logo depois de "<" a gente só completa o NOME da tag (o "<" já foi digitado).
        boolean depoisDoMenor = html && i > 0 && texto.charAt(i - 1) == '<';
        boolean temPagina = texto.toLowerCase().contains("<html") || texto.toLowerCase().contains("<!doctype");
        for (Sugestao s : base(depoisDoMenor, temPagina, prefixo)) {
            boolean bate = exato ? s.rotulo.startsWith(prefixo) : s.rotulo.toLowerCase().startsWith(prefixo.toLowerCase());
            if (bate && jaTem.add(s.rotulo.toLowerCase() + "|" + s.inserir)) {
                achadas.add(s);
                jaTem.add(s.rotulo);
            }
        }
        // Palavras que já existem no próprio arquivo (nomes de variáveis, métodos...)
        if (!prefixo.isEmpty()) {
            Matcher m = PALAVRA.matcher(texto);
            while (m.find() && achadas.size() < 40) {
                String p = m.group();
                boolean ehAPropriaPalavra = m.start() == inicioPalavra;
                if (!ehAPropriaPalavra && p.startsWith(prefixo) && !p.equals(prefixo) && jaTem.add(p)) {
                    achadas.add(new Sugestao(p, "do arquivo", p));
                }
            }
        }

        // Se a única sugestão é exatamente o que já foi digitado, não precisa mostrar.
        if (achadas.isEmpty() || (achadas.size() == 1 && achadas.get(0).inserir.equals(prefixo))) {
            esconder();
            return;
        }
        mostrar(achadas.subList(0, Math.min(12, achadas.size())));
    }

    private static final java.util.Set<String> DECLARA = new java.util.HashSet<>(java.util.Arrays.asList(
            "class", "interface", "enum", "record", "struct", "trait", "def", "function", "fun", "fn", "func", "namespace", "package", "module"));

    /** A palavra antes desta é "class", "def", "function"...? */
    private static boolean depoisDeDeclaracao(String texto, int inicio) {
        int j = inicio;
        while (j > 0 && (texto.charAt(j - 1) == ' ' || texto.charAt(j - 1) == '\t')) j--;
        if (j == inicio) return false;   // não tinha espaço antes: não é "class Nome"
        int k = j;
        while (k > 0 && Character.isJavaIdentifierPart(texto.charAt(k - 1))) k--;
        return DECLARA.contains(texto.substring(k, j));
    }

    private void mostrar(List<Sugestao> sugestoes) {
        if (janela == null) {
            Window dona = SwingUtilities.getWindowAncestor(editor);
            janela = new JWindow(dona);
            janela.setFocusableWindowState(false);   // o foco continua no editor
            JScrollPane rolagem = new JScrollPane(lista);
            rolagem.setBorder(BorderFactory.createLineBorder(Tema.AZUL));
            janela.add(rolagem);
        }
        lista.setListData(sugestoes.toArray(new Sugestao[0]));
        lista.setSelectedIndex(0);
        lista.setVisibleRowCount(Math.min(8, sugestoes.size()));
        janela.pack();
        janela.setSize(Math.max(320, janela.getWidth()), janela.getHeight());

        try {
            Rectangle2D r = editor.modelToView2D(inicioPalavra);
            Point p = new Point((int) r.getX(), (int) (r.getY() + r.getHeight()) + 2);
            SwingUtilities.convertPointToScreen(p, editor);
            janela.setLocation(p);
        } catch (Exception ignorado) { }
        janela.setVisible(true);
    }

    public void mover(int direcao) {
        int n = lista.getModel().getSize();
        int i = (lista.getSelectedIndex() + direcao + n) % n;
        lista.setSelectedIndex(i);
        lista.ensureIndexIsVisible(i);
    }

    /** Coloca a sugestão escolhida no código. */
    public void aceitar() {
        Sugestao s = lista.getSelectedValue();
        esconder();
        if (s == null) return;
        // Se o texto mudou enquanto a lista estava aberta (ex.: Ctrl+Z), não faz nada.
        if (inicioPalavra > editor.getCaretPosition() || inicioPalavra > editor.getDocument().getLength()) return;

        String recuo = editor.recuoDaLinha(inicioPalavra);
        String texto = s.inserir.replace("\n", "\n" + recuo);
        int marca = texto.indexOf("$|");
        texto = texto.replace("$|", "");

        editor.select(inicioPalavra, editor.getCaretPosition());
        editor.inserindoSozinho = true;
        editor.replaceSelection(texto);
        editor.inserindoSozinho = false;
        if (marca >= 0) editor.setCaretPosition(inicioPalavra + marca);
        editor.requestFocusInWindow();
    }

    // =====================================================================
    //  as listas de atalhos de cada linguagem
    // =====================================================================

    private static final String[] TAGS = {"div", "p", "h1", "h2", "h3", "span", "button", "ul", "ol", "li",
            "section", "header", "footer", "nav", "main", "article", "strong", "em", "label", "form", "table",
            "tr", "td", "th", "head", "body", "title", "style", "script", "textarea", "select", "option", "canvas"};
    private static final String[] TAGS_SOZINHAS = {"img", "br", "hr", "input", "meta", "link"};

    /**
     * As sugestões de cada linguagem: os atalhos (sout, main, fori...) + as palavras,
     * funções e tipos que estão cadastrados no Catalogo.
     */
    private List<Sugestao> base(boolean depoisDoMenor, boolean temPagina, String prefixo) {
        List<Sugestao> l = new ArrayList<>();
        Linguagem lg = editor.getLinguagem();

        if (lg.estilo == Linguagem.Estilo.HTML) {
            if (depoisDoMenor) {
                // Depois de "<": só o nome da tag, e fecha ela.
                l.add(new Sugestao("!DOCTYPE", "<!DOCTYPE html>", "!DOCTYPE html>$|"));
                for (String t : TAGS) l.add(new Sugestao(t, "<" + t + "></" + t + ">", t + ">$|</" + t + ">"));
                l.add(new Sugestao("a", "link", "a href=\"$|\"></a>"));
                l.add(new Sugestao("img", "imagem", "img src=\"$|\" alt=\"\">"));
                l.add(new Sugestao("input", "caixa de texto", "input type=\"text\" id=\"$|\">"));
                l.add(new Sugestao("br", "quebra de linha", "br>$|"));
                l.add(new Sugestao("hr", "linha", "hr>$|"));
                l.add(new Sugestao("meta", "meta", "meta $|>"));
                l.add(new Sugestao("link", "ligar o CSS", "link rel=\"stylesheet\" href=\"$|style.css\">"));
            } else {
                // A página inteira (com DOCTYPE) SÓ aparece se o arquivo ainda não tem uma página.
                if (!temPagina) {
                    l.add(new Sugestao("!", "página HTML completa",
                            "<!DOCTYPE html>\n<html lang=\"pt-br\">\n<head>\n    <meta charset=\"UTF-8\">\n"
                                    + "    <title>$|</title>\n    <link rel=\"stylesheet\" href=\"style.css\">\n</head>\n"
                                    + "<body>\n    \n    <script src=\"script.js\"></script>\n</body>\n</html>"));
                }
                for (String t : TAGS) l.add(new Sugestao(t, "<" + t + ">", "<" + t + ">$|</" + t + ">"));
                l.add(new Sugestao("a", "link", "<a href=\"$|\"></a>"));
                l.add(new Sugestao("img", "imagem", "<img src=\"$|\" alt=\"\">"));
                l.add(new Sugestao("input", "caixa de texto", "<input type=\"text\" id=\"$|\">"));
                l.add(new Sugestao("br", "quebra de linha", "<br>$|"));
                l.add(new Sugestao("link", "ligar o CSS", "<link rel=\"stylesheet\" href=\"$|style.css\">"));
                l.add(new Sugestao("script", "ligar o JS", "<script src=\"$|script.js\"></script>"));
            }
            return l;
        }

        if (lg.estilo == Linguagem.Estilo.CSS) {
            for (String p : new String[]{"color", "background-color", "background", "font-size", "font-family",
                    "font-weight", "margin", "padding", "width", "height", "border", "border-radius",
                    "display", "text-align", "justify-content", "align-items", "gap", "position",
                    "top", "left", "right", "bottom", "cursor", "box-shadow", "transition", "opacity", "flex-direction"}) {
                l.add(new Sugestao(p, "propriedade CSS", p + ": $|;"));
            }
            l.add(new Sugestao("flex", "display: flex", "display: flex;$|"));
            return l;
        }

        // Atalhos (sout, main, fori...). No Java, "class" usa o nome do arquivo.
        for (Linguagem.Atalho a : lg.atalhos) l.add(new Sugestao(a.rotulo, a.dica, a.inserir));
        if (lg.nome.equals("Java")) {
            String classe = editor.getNomeClasse();
            l.add(new Sugestao("class", "classe " + classe, "public class " + classe + " {\n    $|\n}"));
        }
        // Palavras, funções e tipos. Se a linguagem não liga pra maiúsculas e você digitou
        // em minúsculas, a sugestão vem em minúsculas também (select / SELECT).
        boolean minusculas = lg.ignoraMaiusculas && !prefixo.isEmpty() && prefixo.equals(prefixo.toLowerCase());
        for (java.util.Map.Entry<String, String> e : lg.vocabulario.entrySet()) {
            String p = e.getKey();
            if (!Character.isLetter(p.charAt(0)) && p.charAt(0) != '_' && p.charAt(0) != '$' && p.charAt(0) != '\\') continue;
            if (lg.ignoraMaiusculas) p = minusculas ? p.toLowerCase() : p.toUpperCase();
            l.add(new Sugestao(p, e.getValue(), p));
        }
        return l;
    }

    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
