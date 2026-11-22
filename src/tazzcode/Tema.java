package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * As cores e fontes do editor.
 *
 * Tem vários TEMAS (Meia-noite, Grafite, Oceano, Claro). O tema e as fontes
 * são escolhidos em Arquivo > Configurações e ficam salvos no config.properties.
 * As cores são carregadas UMA vez quando o TazzCode abre (trocar o tema = reiniciar).
 */
public class Tema {

    // ----- cores da interface (preenchidas pelo tema escolhido) -----
    public static Color FUNDO;          // fundo do editor
    public static Color FUNDO_PAINEL;   // menus, barras, painéis
    public static Color FUNDO_CONSOLE;
    public static Color TEXTO;
    public static Color TEXTO_FRACO;
    public static Color AZUL;           // a cor de destaque (botões principais)
    public static Color AZUL_CLARO;
    public static Color LINHA_ATUAL;    // linha do cursor e botões comuns
    public static Color SELECAO;
    public static Color BORDA;          // linhas finas que separam as partes
    public static Color ERRO;
    public static Color SUCESSO;
    public static Color PERIGO;         // botão "Parar"
    public static boolean CLARO;        // o tema é claro?

    // ----- cores do código -----
    public static Color COD_PALAVRA;    // public, class, if...
    public static Color COD_TEXTO;      // "textos entre aspas"
    public static Color COD_NUMERO;     // 123
    public static Color COD_COMENTARIO; // // comentários
    public static Color COD_METODO;     // println(
    public static Color COD_TIPO;       // String, Scanner
    public static Color COD_ANOTACAO;   // @Override
    public static Color COD_TAG;        // <div>
    public static Color COD_ATRIBUTO;   // class="..."

    public static Font FONTE_UI;

    /** Os temas que existem (o nome aparece nas Configurações). */
    public static final String[] TEMAS = {"Meia-noite", "Grafite", "Oceano", "Claro"};

    static {
        carregar();
    }

    /** Lê o tema e a fonte escolhidos e preenche as cores. */
    public static void carregar() {
        String tema = Config.texto("tema", "Meia-noite");
        int[] c;   // fundo, painel, console, texto, fraco, destaque, destaque claro, linha, seleção, borda
        switch (tema) {
            case "Grafite": c = new int[]{0x1E1F22, 0x2B2D30, 0x19191C, 0xDFE1E5, 0x8C8F96, 0x3574F0, 0x6EA8FE, 0x26282E, 0x214283, 0x393B40}; break;
            case "Oceano":  c = new int[]{0x0F1E26, 0x0B171D, 0x09131A, 0xE2F4FA, 0x7FA3B3, 0x0098D4, 0x5FD2FF, 0x16303B, 0x1D5470, 0x1C3542}; break;
            case "Claro":   c = new int[]{0xFFFFFF, 0xF3F5F9, 0xF7F8FA, 0x1F2430, 0x687185, 0x2F6FEB, 0x1F5FCC, 0xE9EEF7, 0xC9DCFF, 0xDCE1EA}; break;
            default:        c = new int[]{0x1A2131, 0x131927, 0x0F1420, 0xE6EDF7, 0x8391AD, 0x3B82F6, 0x7CC4FF, 0x222B3F, 0x2C4A7C, 0x263048};
        }
        FUNDO = new Color(c[0]); FUNDO_PAINEL = new Color(c[1]); FUNDO_CONSOLE = new Color(c[2]);
        TEXTO = new Color(c[3]); TEXTO_FRACO = new Color(c[4]); AZUL = new Color(c[5]); AZUL_CLARO = new Color(c[6]);
        LINHA_ATUAL = new Color(c[7]); SELECAO = new Color(c[8]); BORDA = new Color(c[9]);
        CLARO = tema.equals("Claro");

        if (CLARO) {   // cores do código pra fundo claro (parecidas com o IntelliJ claro)
            ERRO = new Color(0xD03838); SUCESSO = new Color(0x1E8E4E); PERIGO = new Color(0xD64545);
            COD_PALAVRA = new Color(0x0033B3); COD_TEXTO = new Color(0x067D17); COD_NUMERO = new Color(0x1750EB);
            COD_COMENTARIO = new Color(0x8C8C8C); COD_METODO = new Color(0x00627A); COD_TIPO = new Color(0x2B5CA8);
            COD_ANOTACAO = new Color(0x9E880D); COD_TAG = new Color(0x0033B3); COD_ATRIBUTO = new Color(0x174AD4);
        } else {       // cores do código pra fundo escuro (parecidas com o IntelliJ escuro)
            ERRO = new Color(0xFF7A7A); SUCESSO = new Color(0x7CFFB2); PERIGO = new Color(0xB8404E);
            COD_PALAVRA = new Color(0xCF8E6D); COD_TEXTO = new Color(0x6AAB73); COD_NUMERO = new Color(0x2AACB8);
            COD_COMENTARIO = new Color(0x7A7E85); COD_METODO = new Color(0x56A8F5); COD_TIPO = new Color(0x7CC4FF);
            COD_ANOTACAO = new Color(0xB3AE60); COD_TAG = new Color(0xD5B778); COD_ATRIBUTO = new Color(0xBABABA);
        }
        FONTE_UI = new Font(Config.texto("fonte.ui", "SansSerif"), Font.PLAIN, Config.numero("fonte.ui.tamanho", 13));
    }

    public static Font fonteCodigo() {
        return new Font(Config.texto("fonte.codigo", Font.MONOSPACED), Font.PLAIN, Config.numero("fonte.tamanho", 16));
    }

    public static Font fonteConsole() {
        return new Font(Config.texto("fonte.codigo", Font.MONOSPACED), Font.PLAIN, Config.numero("fonte.console.tamanho", 14));
    }

    /** Todas as fontes instaladas no computador (pra escolher nas Configurações). */
    public static String[] fontesInstaladas() {
        String[] todas = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String[] base = {"SansSerif", "Serif", "Monospaced", "Dialog"};
        Map<String, Boolean> lista = new LinkedHashMap<>();
        for (String b : base) lista.put(b, true);
        Arrays.stream(todas).forEach(f -> lista.put(f, true));
        return lista.keySet().toArray(new String[0]);
    }

    /** Uma cor um pouco mais clara (ou mais escura, no tema claro): pra "passar o mouse em cima". */
    public static Color realce(Color c, float quanto) {
        if (CLARO && c.equals(LINHA_ATUAL)) quanto = -quanto;
        int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
        if (quanto >= 0) return new Color(Math.min(255, (int) (r + (255 - r) * quanto)), Math.min(255, (int) (g + (255 - g) * quanto)), Math.min(255, (int) (b + (255 - b) * quanto)));
        quanto = -quanto;
        return new Color((int) (r * (1 - quanto)), (int) (g * (1 - quanto)), (int) (b * (1 - quanto)));
    }

    /** A cor do texto que fica bem em cima de um fundo (branco no escuro, escuro no claro). */
    public static Color textoSobre(Color fundo) {
        double luz = (0.299 * fundo.getRed() + 0.587 * fundo.getGreen() + 0.114 * fundo.getBlue()) / 255;
        return luz > 0.62 ? new Color(0x1F2430) : Color.WHITE;
    }

    /** Um botão moderno (cantos arredondados, muda de cor ao passar o mouse). */
    public static JButton botao(String texto, Color cor) {
        return new BotaoModerno(texto, null, cor);
    }

    /** Um botão moderno com ícone. */
    public static JButton botao(String texto, Icon icone, Color cor) {
        return new BotaoModerno(texto, icone, cor);
    }

    /** Uma borda fininha arredondada, pra caixas de texto. */
    public static Border bordaCampo() {
        return BorderFactory.createCompoundBorder(new BordaRedonda(BORDA, 8), BorderFactory.createEmptyBorder(5, 8, 5, 8));
    }

    /**
     * Pinta as partes "padrão" do Swing (menus, abas, árvore, caixas de diálogo...)
     * com as cores do tema. Precisa ser chamado ANTES de criar as janelas.
     */
    public static void aplicarNoSwing() {
        // Letras lisinhas (antialias) em todo lugar
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        // A fonte da interface em TODOS os componentes
        FontUIResource fonte = new FontUIResource(FONTE_UI);
        for (Object chave : UIManager.getLookAndFeelDefaults().keySet().toArray()) {
            if (chave != null && chave.toString().endsWith(".font")) UIManager.put(chave, fonte);
        }

        cor("TabbedPane.selected", FUNDO);
        cor("TabbedPane.background", FUNDO_PAINEL);
        cor("TabbedPane.unselectedBackground", FUNDO_PAINEL);
        cor("TabbedPane.tabAreaBackground", FUNDO_PAINEL);
        cor("TabbedPane.contentAreaColor", FUNDO);
        cor("TabbedPane.foreground", TEXTO);
        cor("TabbedPane.light", FUNDO_PAINEL);
        cor("TabbedPane.highlight", FUNDO_PAINEL);
        cor("TabbedPane.shadow", FUNDO_PAINEL);
        cor("TabbedPane.darkShadow", FUNDO_PAINEL);
        cor("TabbedPane.focus", FUNDO_PAINEL);
        cor("TabbedPane.borderHightlightColor", FUNDO_PAINEL);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabInsets", new Insets(6, 12, 6, 10));
        UIManager.put("TabbedPane.tabsOverlapBorder", true);

        for (String c : new String[]{"MenuBar", "Menu", "MenuItem", "PopupMenu", "CheckBoxMenuItem", "RadioButtonMenuItem"}) {
            cor(c + ".background", FUNDO_PAINEL);
            cor(c + ".foreground", TEXTO);
            cor(c + ".selectionBackground", SELECAO);
            cor(c + ".selectionForeground", CLARO ? TEXTO : Color.WHITE);
            cor(c + ".acceleratorForeground", TEXTO_FRACO);
            cor(c + ".acceleratorSelectionForeground", TEXTO);
            UIManager.put(c + ".borderPainted", false);
        }
        UIManager.put("MenuItem.margin", new Insets(5, 6, 5, 10));
        UIManager.put("Menu.margin", new Insets(4, 6, 4, 6));
        cor("Separator.foreground", BORDA);
        cor("Separator.background", FUNDO_PAINEL);
        cor("Separator.highlight", FUNDO_PAINEL);
        cor("Separator.shadow", BORDA);
        cor("PopupMenuSeparator.foreground", BORDA);
        cor("PopupMenuSeparator.background", FUNDO_PAINEL);
        UIManager.put("PopupMenu.border", BorderFactory.createCompoundBorder(new BordaRedonda(BORDA, 6), BorderFactory.createEmptyBorder(4, 0, 4, 0)));
        UIManager.put("MenuBar.border", BorderFactory.createEmptyBorder());

        cor("Tree.background", FUNDO_PAINEL);
        cor("Tree.textBackground", FUNDO_PAINEL);
        cor("Tree.foreground", TEXTO);
        cor("Tree.textForeground", TEXTO);
        cor("Tree.selectionBackground", SELECAO);
        cor("Tree.selectionForeground", CLARO ? TEXTO : Color.WHITE);
        cor("Tree.selectionBorderColor", SELECAO);
        cor("Tree.hash", BORDA);
        UIManager.put("Tree.rowHeight", FONTE_UI.getSize() + 12);

        cor("ScrollBar.background", FUNDO);
        cor("ScrollBar.track", FUNDO);
        cor("ScrollBar.thumb", BORDA);
        cor("ScrollBar.thumbHighlight", BORDA);
        cor("ScrollBar.thumbShadow", BORDA);
        cor("ScrollBar.thumbDarkShadow", FUNDO);
        cor("ScrollBar.shadow", FUNDO);
        cor("ScrollBar.highlight", FUNDO);
        cor("ScrollBar.darkShadow", FUNDO);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBarUI", BarraRolagem.class.getName());

        cor("SplitPane.background", FUNDO_PAINEL);
        cor("SplitPane.dividerFocusColor", AZUL);
        cor("SplitPaneDivider.draggingColor", AZUL);
        cor("ScrollPane.background", FUNDO);
        cor("Viewport.background", FUNDO);
        cor("ToolTip.background", FUNDO_PAINEL);
        cor("ToolTip.foreground", TEXTO);
        UIManager.put("ToolTip.border", BorderFactory.createCompoundBorder(new BordaRedonda(BORDA, 6), BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        // Caixas de diálogo, campos, listas, caixas de escolher: tudo no tema
        for (String c : new String[]{"Panel", "OptionPane", "Label", "CheckBox", "RadioButton", "Viewport"}) {
            cor(c + ".background", FUNDO_PAINEL);
            cor(c + ".foreground", TEXTO);
        }
        cor("OptionPane.messageForeground", TEXTO);
        for (String c : new String[]{"TextField", "PasswordField", "FormattedTextField", "TextArea", "EditorPane", "TextPane", "Spinner"}) {
            cor(c + ".background", FUNDO);
            cor(c + ".foreground", TEXTO);
            cor(c + ".caretForeground", AZUL_CLARO);
            cor(c + ".selectionBackground", SELECAO);
            cor(c + ".selectionForeground", TEXTO);
            cor(c + ".inactiveForeground", TEXTO_FRACO);
            cor(c + ".inactiveBackground", FUNDO_PAINEL);
        }
        UIManager.put("TextField.border", bordaCampo());
        UIManager.put("PasswordField.border", bordaCampo());
        UIManager.put("FormattedTextField.border", bordaCampo());
        for (String c : new String[]{"List", "Table", "ComboBox"}) {
            cor(c + ".background", FUNDO);
            cor(c + ".foreground", TEXTO);
            cor(c + ".selectionBackground", SELECAO);
            cor(c + ".selectionForeground", CLARO ? TEXTO : Color.WHITE);
        }
        cor("ComboBox.buttonBackground", FUNDO);
        cor("ComboBox.buttonDarkShadow", FUNDO);
        cor("ComboBox.disabledBackground", FUNDO_PAINEL);
        cor("Table.gridColor", BORDA);
        cor("TableHeader.background", FUNDO_PAINEL);
        cor("TableHeader.foreground", TEXTO);
        cor("ProgressBar.background", FUNDO);
        cor("ProgressBar.foreground", AZUL);
        cor("ProgressBar.selectionForeground", Color.WHITE);
        cor("ProgressBar.selectionBackground", TEXTO);
        UIManager.put("ProgressBar.border", BorderFactory.createEmptyBorder());
        cor("Button.background", LINHA_ATUAL);
        cor("Button.foreground", TEXTO);
        cor("Button.select", SELECAO);
        UIManager.put("Button.focus", new ColorUIResource(FUNDO_PAINEL));
        cor("FileChooser.listViewBackground", FUNDO);
        cor("Slider.background", FUNDO_PAINEL);
        cor("Slider.foreground", AZUL);
        cor("TitledBorder.titleColor", TEXTO);
        UIManager.put("ComboBoxUI", CaixaEscolha.class.getName());
        UIManager.put("ButtonUI", BotaoModerno.UIPadrao.class.getName());
    }

    /** Deixa a barra de menus no tema (o visual padrão do Java pinta ela de branco). */
    public static void escurecer(javax.swing.JMenuBar barra) {
        barra.setUI(new javax.swing.plaf.basic.BasicMenuBarUI());
        barra.setBackground(FUNDO_PAINEL);
        barra.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
    }

    /** Pinta a "barrinha" que divide os painéis com a cor do tema. */
    public static void escurecer(javax.swing.JSplitPane divisao) {
        divisao.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI());
        divisao.setBorder(null);
        javax.swing.plaf.basic.BasicSplitPaneDivider d = ((javax.swing.plaf.basic.BasicSplitPaneUI) divisao.getUI()).getDivider();
        d.setBackground(BORDA);
        d.setBorder(null);
    }

    private static void cor(String chave, Color c) {
        UIManager.put(chave, new ColorUIResource(c));
    }

    // =====================================================================
    //  Uma borda com cantos arredondados (desenhada à mão)
    // =====================================================================
    public static class BordaRedonda extends javax.swing.border.AbstractBorder {
        private final Color cor;
        private final int raio;

        public BordaRedonda(Color cor, int raio) {
            this.cor = cor;
            this.raio = raio;
        }

        @Override
        public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int w, int h) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.hasFocus() && c instanceof javax.swing.text.JTextComponent ? AZUL : cor);
            g2.drawRoundRect(x, y, w - 1, h - 1, raio, raio);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(java.awt.Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }
}
