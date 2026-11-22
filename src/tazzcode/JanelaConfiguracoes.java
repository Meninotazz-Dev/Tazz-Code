package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListCellRenderer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A janela de CONFIGURAÇÕES (Arquivo > Configurações, ou Ctrl+vírgula).
 *
 * Do lado esquerdo, as seções; do lado direito, as opções da seção escolhida.
 * Nada muda até clicar em "Salvar". Fontes e opções do editor mudam na hora;
 * o TEMA (cores) precisa reiniciar o TazzCode.
 */
public class JanelaConfiguracoes extends JDialog {

    private final Runnable aoSalvar;
    private final JFrame dona;

    // ----- os controles (um pra cada configuração) -----
    private final JComboBox<String> tema = new JComboBox<>(Tema.TEMAS);
    private final JComboBox<String> fonteUI = new JComboBox<>();
    private final JComboBox<Integer> tamanhoUI = numeros(11, 20);
    private final JComboBox<String> fonteCodigo = new JComboBox<>();
    private final JComboBox<Integer> tamanhoCodigo = numeros(10, 32);
    private final JComboBox<Integer> tamanhoTab = new JComboBox<>(new Integer[]{2, 4, 8});
    private final JCheckBox fecharPares = caixinha("Fechar sozinho ( ), [ ], { }, aspas e tags do HTML");
    private final JCheckBox autocompletar = caixinha("Mostrar sugestões enquanto digito (Ctrl+Espaço sempre funciona)");
    private final JCheckBox numerosLinha = caixinha("Mostrar os números das linhas");
    private final JCheckBox destacarLinha = caixinha("Destacar a linha onde está o cursor");
    private final JCheckBox quebrarLinha = caixinha("Quebrar linhas compridas (em vez da barra de rolagem de lado)");
    private final JCheckBox salvarSozinho = caixinha("Salvar sozinho a cada 30 segundos");
    private final JComboBox<Integer> tamanhoConsole = numeros(10, 28);
    private final JCheckBox limparConsole = caixinha("Limpar o console antes de rodar de novo");
    private final JCheckBox mostrarHelper = caixinha("Mostrar o TazzHelper quando o TazzCode abrir");
    private final JCheckBox modoProfessor = caixinha("Modo professor: dicas em vez de respostas prontas");
    private final JTextArea previa = new JTextArea(
            "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Olá, TazzCode!\");\n    }\n}");
    private final JLabel previaUI = new JLabel("Assim fica o texto dos menus e botões.");

    public JanelaConfiguracoes(JFrame dona, Runnable aoSalvar) {
        super(dona, "Configurações", true);
        this.dona = dona;
        this.aoSalvar = aoSalvar;
        setSize(820, 560);
        setMinimumSize(new Dimension(640, 440));
        setLocationRelativeTo(dona);

        // ----- as seções -----
        Map<String, JComponent> secoes = new LinkedHashMap<>();
        secoes.put("Aparência", secaoAparencia());
        secoes.put("Editor", secaoEditor());
        secoes.put("Console", secaoConsole());
        secoes.put("TazzHelper", secaoHelper());
        secoes.put("Pastas", secaoPastas());

        CardLayout cartas = new CardLayout();
        JPanel direita = new JPanel(cartas);
        direita.setBackground(Tema.FUNDO);
        for (Map.Entry<String, JComponent> e : secoes.entrySet()) {
            JScrollPane rola = new JScrollPane(e.getValue(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            rola.setBorder(null);
            rola.getVerticalScrollBar().setUnitIncrement(16);
            direita.add(rola, e.getKey());
        }

        JList<String> menu = new JList<>(secoes.keySet().toArray(new String[0]));
        menu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menu.setBackground(Tema.FUNDO_PAINEL);
        menu.setFixedCellHeight(Tema.FONTE_UI.getSize() + 22);
        menu.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                super.getListCellRendererComponent(l, v, i, sel, false);
                setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 10));
                setFont(Tema.FONTE_UI.deriveFont(sel ? Font.BOLD : Font.PLAIN, Tema.FONTE_UI.getSize2D() + 1));
                setBackground(sel ? Tema.SELECAO : Tema.FUNDO_PAINEL);
                setForeground(sel ? (Tema.CLARO ? Tema.TEXTO : Color.WHITE) : Tema.TEXTO);
                return this;
            }
        });
        menu.addListSelectionListener(e -> {
            if (menu.getSelectedValue() != null) cartas.show(direita, menu.getSelectedValue());
        });
        menu.setSelectedIndex(0);
        JPanel esquerda = new JPanel(new BorderLayout());
        esquerda.setBackground(Tema.FUNDO_PAINEL);
        esquerda.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Tema.BORDA), BorderFactory.createEmptyBorder(12, 0, 12, 0)));
        JLabel titulo = new JLabel("Configurações", Icones.engrenagem(), JLabel.LEFT);
        titulo.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD, Tema.FONTE_UI.getSize2D() + 3));
        titulo.setForeground(Tema.TEXTO);
        titulo.setIconTextGap(8);
        titulo.setBorder(BorderFactory.createEmptyBorder(2, 16, 14, 16));
        esquerda.add(titulo, BorderLayout.NORTH);
        esquerda.add(menu, BorderLayout.CENTER);
        esquerda.setPreferredSize(new Dimension(190, 10));

        // ----- botões de baixo -----
        JButton restaurar = Tema.botao("Restaurar o padrão", Tema.LINHA_ATUAL);
        JButton cancelar = Tema.botao("Cancelar", Tema.LINHA_ATUAL);
        JButton salvar = Tema.botao("Salvar", Tema.AZUL);
        restaurar.addActionListener(e -> preencher(true));
        cancelar.addActionListener(e -> dispose());
        salvar.addActionListener(e -> salvar());
        JPanel baixo = new JPanel(new BorderLayout());
        baixo.setBackground(Tema.FUNDO_PAINEL);
        baixo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDA), BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JPanel bDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bDireita.setOpaque(false);
        bDireita.add(cancelar);
        bDireita.add(salvar);
        JPanel bEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bEsquerda.setOpaque(false);
        bEsquerda.add(restaurar);
        baixo.add(bEsquerda, BorderLayout.WEST);
        baixo.add(bDireita, BorderLayout.EAST);

        JPanel tudo = new JPanel(new BorderLayout());
        tudo.add(esquerda, BorderLayout.WEST);
        tudo.add(direita, BorderLayout.CENTER);
        tudo.add(baixo, BorderLayout.SOUTH);
        setContentPane(tudo);
        getRootPane().setDefaultButton(salvar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        preencher(false);
    }

    // =====================================================================
    //  AS SEÇÕES
    // =====================================================================

    private JComponent secaoAparencia() {
        String[] todas = Tema.fontesInstaladas();
        for (String f : todas) fonteUI.addItem(f);
        // Fontes de código: as "monoespaçadas" conhecidas primeiro (todas as letras com a mesma largura)
        List<String> codigo = new ArrayList<>();
        List<String> instaladas = Arrays.asList(todas);
        for (String f : new String[]{"Monospaced", "Consolas", "JetBrains Mono", "Cascadia Code", "Cascadia Mono", "Fira Code",
                "Source Code Pro", "Ubuntu Mono", "DejaVu Sans Mono", "Courier New", "Lucida Console"}) {
            if (f.equals("Monospaced") || instaladas.contains(f)) codigo.add(f);
        }
        for (String f : todas) if (!codigo.contains(f)) codigo.add(f);
        for (String f : codigo) fonteCodigo.addItem(f);

        previa.setEditable(false);
        previa.setBackground(Tema.FUNDO_CONSOLE);
        previa.setForeground(Tema.TEXTO);
        previa.setBorder(BorderFactory.createCompoundBorder(new Tema.BordaRedonda(Tema.BORDA, 10), BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        previaUI.setForeground(Tema.TEXTO_FRACO);
        Runnable atualizar = () -> {
            previa.setFont(new Font((String) fonteCodigo.getSelectedItem(), Font.PLAIN, (Integer) tamanhoCodigo.getSelectedItem()));
            previaUI.setFont(new Font((String) fonteUI.getSelectedItem(), Font.PLAIN, (Integer) tamanhoUI.getSelectedItem()));
        };
        for (JComboBox<?> c : List.of(fonteUI, tamanhoUI, fonteCodigo, tamanhoCodigo)) c.addActionListener(e -> atualizar.run());
        tema.setRenderer(new DefaultListCellRenderer() {   // cada tema com as bolinhas das cores dele
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                super.getListCellRendererComponent(l, v, i, sel, false);
                setIcon(amostraTema(String.valueOf(v)));
                setIconTextGap(8);
                setOpaque(i >= 0);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                setBackground(sel ? Tema.SELECAO : Tema.FUNDO);
                setForeground(Tema.TEXTO);
                return this;
            }
        });

        Formulario f = new Formulario("Aparência", "As cores e as letras do TazzCode.");
        f.linha("Tema", tema, "Trocar o tema reinicia o TazzCode (ele pergunta antes e reabre os seus arquivos).");
        f.linha("Fonte da interface", fonteUI, null);
        f.linha("Tamanho da interface", tamanhoUI, null);
        f.cheio(previaUI);
        f.linha("Fonte do código", fonteCodigo, "Dica: fontes \"monoespaçadas\" (todas as letras com a mesma largura) são as melhores pra código.");
        f.linha("Tamanho do código", tamanhoCodigo, "Também dá pra mudar com Ctrl+= e Ctrl+- no editor.");
        f.cheio(previa);
        return f.pronto();
    }

    private JComponent secaoEditor() {
        Formulario f = new Formulario("Editor", "Como a área de escrever código se comporta.");
        f.linha("Tamanho do TAB", tamanhoTab, "Quantos espaços o TAB coloca (e o recuo depois do Enter).");
        f.cheio(fecharPares);
        f.cheio(autocompletar);
        f.cheio(numerosLinha);
        f.cheio(destacarLinha);
        f.cheio(quebrarLinha);
        f.cheio(salvarSozinho);
        return f.pronto();
    }

    private JComponent secaoConsole() {
        Formulario f = new Formulario("Console", "O painel de baixo, onde o seu programa roda.");
        f.linha("Tamanho da letra", tamanhoConsole, null);
        f.cheio(limparConsole);
        return f.pronto();
    }

    private JComponent secaoHelper() {
        Formulario f = new Formulario("TazzHelper", "A IA que ajuda com os erros e explica o código.");
        f.cheio(mostrarHelper);
        f.cheio(modoProfessor);
        f.texto("O cérebro do TazzHelper (IA do TazzCode, Claude pela internet ou simples) e a mini IA "
                + "se configuram no botão Configurar, lá no painel do TazzHelper.");
        return f.pronto();
    }

    private JComponent secaoPastas() {
        Formulario f = new Formulario("Pastas", "Onde o TazzCode guarda cada coisa no seu computador.");
        f.pasta("Seus projetos", Pastas.documentos().toFile());
        f.pasta("Configurações", Pastas.dados().toFile());
        f.pasta("Plugins e mini IA", Pastas.compartilhada().toFile());
        f.pasta("Programa", Pastas.instalacao().toFile());
        f.texto(App.NOME + " " + App.VERSAO);
        return f.pronto();
    }

    // =====================================================================
    //  LER E SALVAR
    // =====================================================================

    /** Coloca nos controles o que está salvo (ou o padrão, se "padrao" for true). */
    private void preencher(boolean padrao) {
        java.util.function.BiFunction<String, String, String> t = (k, d) -> padrao ? d : Config.texto(k, d);
        java.util.function.BiFunction<String, Integer, Integer> n = (k, d) -> padrao ? d : Config.numero(k, d);
        java.util.function.BiFunction<String, Boolean, Boolean> b = (k, d) -> padrao ? d : Config.texto(k, d ? "sim" : "nao").equals("sim");

        tema.setSelectedItem(t.apply("tema", "Meia-noite"));
        escolher(fonteUI, t.apply("fonte.ui", "SansSerif"));
        tamanhoUI.setSelectedItem(n.apply("fonte.ui.tamanho", 13));
        escolher(fonteCodigo, t.apply("fonte.codigo", "Monospaced"));
        tamanhoCodigo.setSelectedItem(n.apply("fonte.tamanho", 16));
        tamanhoTab.setSelectedItem(n.apply("editor.tab", 4));
        fecharPares.setSelected(b.apply("editor.fecharPares", true));
        autocompletar.setSelected(b.apply("editor.autocompletar", true));
        numerosLinha.setSelected(b.apply("editor.numeros", true));
        destacarLinha.setSelected(b.apply("editor.linhaAtual", true));
        quebrarLinha.setSelected(b.apply("editor.quebrar", false));
        salvarSozinho.setSelected(b.apply("editor.salvarSozinho", false));
        tamanhoConsole.setSelectedItem(n.apply("fonte.console.tamanho", 14));
        limparConsole.setSelected(b.apply("console.limpar", true));
        mostrarHelper.setSelected(b.apply("helper.visivel", true));
        modoProfessor.setSelected(b.apply("helper.professor", true));
    }

    private static void escolher(JComboBox<String> c, String valor) {
        for (int i = 0; i < c.getItemCount(); i++) {
            if (c.getItemAt(i).equalsIgnoreCase(valor)) { c.setSelectedIndex(i); return; }
        }
        c.setSelectedIndex(0);
    }

    private void salvar() {
        String temaAntes = Config.texto("tema", "Meia-noite");
        String uiAntes = Config.texto("fonte.ui", "SansSerif") + Config.numero("fonte.ui.tamanho", 13);

        Config.colocar("tema", tema.getSelectedItem());
        Config.colocar("fonte.ui", fonteUI.getSelectedItem());
        Config.colocar("fonte.ui.tamanho", tamanhoUI.getSelectedItem());
        Config.colocar("fonte.codigo", fonteCodigo.getSelectedItem());
        Config.colocar("fonte.tamanho", tamanhoCodigo.getSelectedItem());
        Config.colocar("editor.tab", tamanhoTab.getSelectedItem());
        Config.colocar("editor.fecharPares", fecharPares.isSelected() ? "sim" : "nao");
        Config.colocar("editor.autocompletar", autocompletar.isSelected() ? "sim" : "nao");
        Config.colocar("editor.numeros", numerosLinha.isSelected() ? "sim" : "nao");
        Config.colocar("editor.linhaAtual", destacarLinha.isSelected() ? "sim" : "nao");
        Config.colocar("editor.quebrar", quebrarLinha.isSelected() ? "sim" : "nao");
        Config.colocar("editor.salvarSozinho", salvarSozinho.isSelected() ? "sim" : "nao");
        Config.colocar("fonte.console.tamanho", tamanhoConsole.getSelectedItem());
        Config.colocar("console.limpar", limparConsole.isSelected() ? "sim" : "nao");
        Config.colocar("helper.visivel", mostrarHelper.isSelected() ? "sim" : "nao");
        Config.colocar("helper.professor", modoProfessor.isSelected() ? "sim" : "nao");

        dispose();
        aoSalvar.run();   // fontes do código, console e opções do editor: na hora

        boolean precisaReiniciar = !temaAntes.equals(tema.getSelectedItem())
                || !uiAntes.equals(fonteUI.getSelectedItem() + "" + tamanhoUI.getSelectedItem());
        if (precisaReiniciar && dona instanceof JanelaEditor) {
            Object[] opcoes = {"Reiniciar agora", "Depois"};
            int r = javax.swing.JOptionPane.showOptionDialog(dona,
                    "O tema e a fonte da interface mudam quando o " + App.NOME + " reinicia.\n"
                            + "Reiniciar agora? (ele pede pra salvar o que precisar e reabre os seus arquivos)",
                    "Configurações", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
            if (r == 0) ((JanelaEditor) dona).reiniciar();
        } else if (precisaReiniciar && dona instanceof JanelaInicial) {
            ((JanelaInicial) dona).recarregar();   // a tela inicial não tem nada pra salvar: troca na hora
        }
    }

    // =====================================================================
    //  PEÇAS
    // =====================================================================

    private static JComboBox<Integer> numeros(int de, int ate) {
        Integer[] n = new Integer[ate - de + 1];
        for (int i = 0; i < n.length; i++) n[i] = de + i;
        return new JComboBox<>(n);
    }

    /** Uma caixinha de marcar, com o visual do tema. */
    static JCheckBox caixinha(String texto) {
        JCheckBox c = new JCheckBox(texto);
        c.setOpaque(false);
        c.setForeground(Tema.TEXTO);
        c.setFont(Tema.FONTE_UI);
        c.setFocusPainted(false);
        c.setIconTextGap(10);
        c.setIcon(marca(false));
        c.setSelectedIcon(marca(true));
        c.setRolloverIcon(marca(false));
        c.setRolloverSelectedIcon(marca(true));
        c.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        c.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return c;
    }

    private static Icon marca(boolean marcada) {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (marcada) {
                    g2.setColor(Tema.AZUL);
                    g2.fillRoundRect(x, y, 18, 18, 6, 6);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawPolyline(new int[]{x + 4, x + 8, x + 14}, new int[]{y + 9, y + 13, y + 5}, 3);
                } else {
                    g2.setColor(Tema.FUNDO);
                    g2.fillRoundRect(x, y, 18, 18, 6, 6);
                    g2.setColor(Tema.TEXTO_FRACO);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(x, y, 17, 17, 6, 6);
                }
                g2.dispose();
            }
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 18; }
        };
    }

    /** As bolinhas com as cores de cada tema (na lista de temas). */
    private static Icon amostraTema(String nome) {
        int[] cores;
        switch (nome) {
            case "Grafite": cores = new int[]{0x1E1F22, 0x3574F0, 0xCF8E6D}; break;
            case "Oceano":  cores = new int[]{0x0F1E26, 0x0098D4, 0x6AAB73}; break;
            case "Claro":   cores = new int[]{0xFFFFFF, 0x2F6FEB, 0x0033B3}; break;
            default:        cores = new int[]{0x1A2131, 0x3B82F6, 0x7CC4FF};
        }
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 0; i < cores.length; i++) {
                    g2.setColor(new Color(cores[i]));
                    g2.fillOval(x + i * 11, y, 14, 14);
                    g2.setColor(new Color(0x80808080, true));
                    g2.drawOval(x + i * 11, y, 14, 14);
                }
                g2.dispose();
            }
            public int getIconWidth() { return 36; }
            public int getIconHeight() { return 14; }
        };
    }

    /** Monta uma seção: título, explicação e as linhas "rótulo: controle". */
    private static class Formulario {
        private final JPanel p = new JPanel(new GridBagLayout());
        private int linha = 0;

        Formulario(String titulo, String explicacao) {
            p.setBackground(Tema.FUNDO);
            p.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
            JLabel t = new JLabel(titulo);
            t.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD, Tema.FONTE_UI.getSize2D() + 9));
            t.setForeground(Tema.TEXTO);
            cheio(t);
            JLabel e = new JLabel(explicacao);
            e.setForeground(Tema.TEXTO_FRACO);
            e.setBorder(BorderFactory.createEmptyBorder(2, 0, 16, 0));
            cheio(e);
        }

        void linha(String rotulo, JComponent controle, String dica) {
            JLabel r = new JLabel(rotulo);
            r.setForeground(Tema.TEXTO);
            r.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = linha; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(6, 0, 6, 16);
            p.add(r, c);
            c = new GridBagConstraints();
            c.gridx = 1; c.gridy = linha++; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(6, 0, 6, 0);
            controle.setPreferredSize(new Dimension(Math.max(120, controle instanceof JComboBox && ((JComboBox<?>) controle).getItemAt(0) instanceof Integer ? 90 : 260),
                    Tema.FONTE_UI.getSize() + 18));
            p.add(controle, c);
            if (dica != null) {
                JLabel d = new JLabel("<html><div style='width:330px'>" + dica + "</div></html>");
                d.setForeground(Tema.TEXTO_FRACO);
                d.setFont(Tema.FONTE_UI.deriveFont(Tema.FONTE_UI.getSize2D() - 1));
                c = new GridBagConstraints();
                c.gridx = 1; c.gridy = linha++; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(0, 0, 8, 0);
                c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
                p.add(d, c);
            }
        }

        void cheio(JComponent comp) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = linha++; c.gridwidth = 2; c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(2, 0, 2, 0);
            p.add(comp, c);
        }

        void texto(String t) {
            JLabel l = new JLabel("<html><div style='width:460px'>" + t + "</div></html>");
            l.setForeground(Tema.TEXTO_FRACO);
            l.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
            cheio(l);
        }

        void pasta(String nome, File pasta) {
            JPanel linhaP = new JPanel(new BorderLayout(12, 0));
            linhaP.setOpaque(false);
            linhaP.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
            JLabel n = new JLabel("<html><b>" + nome + "</b><br><span style='color:#" + Integer.toHexString(Tema.TEXTO_FRACO.getRGB() & 0xFFFFFF)
                    + "'>" + pasta.getAbsolutePath() + "</span></html>");
            n.setForeground(Tema.TEXTO);
            JButton abrir = Tema.botao("Abrir", Icones.pasta(), Tema.LINHA_ATUAL);
            abrir.addActionListener(e -> {
                try { Desktop.getDesktop().open(pasta); } catch (Exception ignorado) { }
            });
            linhaP.add(n, BorderLayout.CENTER);
            linhaP.add(abrir, BorderLayout.EAST);
            cheio(linhaP);
        }

        JComponent pronto() {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = linha++; c.weighty = 1; c.fill = GridBagConstraints.VERTICAL;
            p.add(Box.createVerticalGlue(), c);
            return p;
        }
    }
}
