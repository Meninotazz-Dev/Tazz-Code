package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A JANELA DO BANCO DE DADOS SIMULADO.
 *
 *   esquerda: as tabelas que existem (clique numa pra ver o que tem dentro)
 *   meio:     os resultados dos SELECTs (cada um numa aba)
 *   embaixo:  as mensagens (tabela criada, linhas inseridas, erros...) e uma caixinha
 *             pra digitar comandos SQL na hora
 *
 * Cada arquivo .sql tem o SEU banco, que fica guardado na memória enquanto o TazzCode estiver aberto.
 */
public class JanelaBanco extends JFrame {

    private static final Map<String, JanelaBanco> ABERTAS = new HashMap<>();

    private final MotorBanco banco;
    private final File arquivo;
    private final JList<String> listaTabelas = new JList<>();
    private final JTabbedPane abasResultado = new JTabbedPane();
    private final JTextPane log = new JTextPane();
    private final JTextField comandoRapido = new JTextField();

    /** Abre (ou reaproveita) a janela do banco desse arquivo e roda o código. */
    public static void rodar(File arquivo, String codigo, Linguagem linguagem) {
        Linguagem.Alternativa alt = linguagem.escolhida();
        String nomeBanco = alt != null ? alt.nome : linguagem.nome;
        String motor = alt != null ? alt.motor : linguagem.motorBanco;
        String dialeto = alt != null ? alt.dialeto : linguagem.dialetoSql;
        String chave = arquivo.getAbsolutePath() + "|" + nomeBanco;
        JanelaBanco j = ABERTAS.get(chave);
        if (j == null || !j.isDisplayable()) {
            MotorBanco m = motor.equals("MONGO") ? new MongoSimulado()
                    : motor.equals("REDIS") ? new RedisSimulado() : new Sql(dialeto);
            j = new JanelaBanco(arquivo, nomeBanco, m);
            ABERTAS.put(chave, j);
        }
        j.setVisible(true);
        j.toFront();
        j.executar(codigo, true);
    }

    private JanelaBanco(File arquivo, String nomeBanco, MotorBanco motor) {
        super(nomeBanco + " SIMULADO - " + arquivo.getName());
        this.arquivo = arquivo;
        this.banco = motor;
        setIconImage(Icone.carregar());
        setSize(1000, 650);
        setLocationByPlatform(true);

        JPanel tudo = new JPanel(new BorderLayout());
        tudo.setBackground(Tema.FUNDO);

        // ----- aviso em cima -----
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.FUNDO_PAINEL);
        topo.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JLabel aviso = new JLabel(nomeBanco + " de mentirinha: fica só na memória do TazzCode, não é o banco real.");
        aviso.setForeground(Tema.AZUL_CLARO);
        aviso.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD));
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botoes.setOpaque(false);
        JButton limparLog = Tema.botao("Limpar mensagens", Tema.LINHA_ATUAL);
        JButton apagarTudo = Tema.botao("Apagar o banco", new Color(0x8A2F3B));
        limparLog.addActionListener(e -> log.setText(""));
        apagarTudo.addActionListener(e -> {
            banco.limpar();
            abasResultado.removeAll();
            atualizarTabelas();
            escrever("O banco foi apagado. Tudo vazio de novo!\n", Tema.ERRO);
        });
        botoes.add(limparLog);
        botoes.add(apagarTudo);
        topo.add(aviso, BorderLayout.WEST);
        topo.add(botoes, BorderLayout.EAST);
        tudo.add(topo, BorderLayout.NORTH);

        // ----- lista de tabelas -----
        listaTabelas.setBackground(Tema.FUNDO_PAINEL);
        listaTabelas.setForeground(Tema.TEXTO);
        listaTabelas.setSelectionBackground(Tema.SELECAO);
        listaTabelas.setFont(Tema.FONTE_UI.deriveFont(14f));
        listaTabelas.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                JLabel c = (JLabel) super.getListCellRendererComponent(l, v, i, sel, false);
                c.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                c.setBackground(sel ? Tema.SELECAO : Tema.FUNDO_PAINEL);
                c.setForeground(Tema.TEXTO);
                return c;
            }
        });
        listaTabelas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String v = listaTabelas.getSelectedValue();
                if (v != null) {
                    String c = banco.comandoParaVer(v.substring(0, v.lastIndexOf("  (")));
                    escrever("\n" + banco.prompt() + " " + c + "\n", Tema.AZUL_CLARO);
                    executar(c, false);
                }
            }
        });
        JPanel esquerda = new JPanel(new BorderLayout());
        esquerda.setBackground(Tema.FUNDO_PAINEL);
        JLabel tituloTabelas = new JLabel(banco.tituloItens());
        tituloTabelas.setForeground(Tema.AZUL_CLARO);
        tituloTabelas.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD));
        tituloTabelas.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        esquerda.add(tituloTabelas, BorderLayout.NORTH);
        JScrollPane rolTabelas = new JScrollPane(listaTabelas);
        rolTabelas.setBorder(BorderFactory.createEmptyBorder());
        esquerda.add(rolTabelas, BorderLayout.CENTER);

        // ----- resultados -----
        abasResultado.setFont(Tema.FONTE_UI);
        abasResultado.setForeground(Tema.TEXTO);
        abasResultado.setBackground(Tema.FUNDO_PAINEL);

        // ----- mensagens + comando rápido -----
        log.setEditable(false);
        log.setBackground(Tema.FUNDO_CONSOLE);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        log.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JScrollPane rolLog = new JScrollPane(log);
        rolLog.setBorder(BorderFactory.createEmptyBorder());

        comandoRapido.setBackground(Tema.FUNDO);
        comandoRapido.setForeground(Tema.TEXTO);
        comandoRapido.setCaretColor(Tema.AZUL_CLARO);
        comandoRapido.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        comandoRapido.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        comandoRapido.setToolTipText("Digite um comando e aperte ENTER");
        comandoRapido.addActionListener(e -> {
            String c = comandoRapido.getText().trim();
            if (c.isEmpty()) return;
            comandoRapido.setText("");
            escrever("\n" + banco.prompt() + " " + c + "\n", Tema.AZUL_CLARO);
            executar(c, false);
        });
        JLabel prompt = new JLabel("  " + banco.prompt() + " ");
        prompt.setForeground(Tema.AZUL_CLARO);
        prompt.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        JPanel linhaComando = new JPanel(new BorderLayout());
        linhaComando.setBackground(Tema.FUNDO);
        linhaComando.add(prompt, BorderLayout.WEST);
        linhaComando.add(comandoRapido, BorderLayout.CENTER);

        JPanel baixo = new JPanel(new BorderLayout());
        baixo.add(rolLog, BorderLayout.CENTER);
        baixo.add(linhaComando, BorderLayout.SOUTH);

        JSplitPane meio = new JSplitPane(JSplitPane.VERTICAL_SPLIT, abasResultado, baixo);
        meio.setResizeWeight(0.62);
        JSplitPane lados = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, esquerda, meio);
        Tema.escurecer(meio);
        Tema.escurecer(lados);
        meio.setDividerSize(5);
        lados.setDividerSize(5);
        lados.setDividerLocation(200);
        meio.setDividerLocation(380);
        tudo.add(lados, BorderLayout.CENTER);
        setContentPane(tudo);
    }

    /** Roda os comandos. Se "doArquivo", limpa as abas antigas antes. */
    private void executar(String codigo, boolean doArquivo) {
        if (doArquivo) {
            // Cada vez que você roda o arquivo, o banco começa do zero (senão o CREATE TABLE
            // daria "tabela já existe" na 2ª vez). A caixinha SQL> continua no mesmo banco.
            banco.limpar();
            abasResultado.removeAll();
            escrever("\n=== Rodando " + arquivo.getName() + " ===\n", Tema.AZUL_CLARO);
        }
        int[] contador = {abasResultado.getTabCount()};
        banco.executar(codigo,
                msg -> escrever(msg + "\n", msg.startsWith("ERRO") ? Tema.ERRO : Tema.TEXTO),
                r -> {
                    contador[0]++;
                    String titulo = contador[0] + ". " + r.titulo + " (" + r.linhas.size() + ")";
                    abasResultado.addTab(titulo, tabelaDoResultado(r));
                    abasResultado.setSelectedIndex(abasResultado.getTabCount() - 1);
                    escrever(r.linhas.size() + (r.linhas.size() == 1 ? " linha" : " linhas") + " no resultado.\n", Tema.SUCESSO);
                });
        atualizarTabelas();
    }

    private JScrollPane tabelaDoResultado(Sql.Resultado r) {
        Object[][] dados = new Object[r.linhas.size()][];
        for (int i = 0; i < dados.length; i++) {
            Object[] l = r.linhas.get(i);
            dados[i] = new Object[l.length];
            for (int j = 0; j < l.length; j++) dados[i][j] = Sql.texto(l[j]);
        }
        DefaultTableModel modelo = new DefaultTableModel(dados, r.colunas.toArray()) {
            @Override public boolean isCellEditable(int l, int c) { return false; }
        };
        JTable tabela = new JTable(modelo);
        tabela.setBackground(Tema.FUNDO);
        tabela.setForeground(Tema.TEXTO);
        tabela.setGridColor(Tema.LINHA_ATUAL);
        tabela.setSelectionBackground(Tema.SELECAO);
        tabela.setSelectionForeground(Tema.TEXTO);
        tabela.setRowHeight(24);
        tabela.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        tabela.getTableHeader().setBackground(Tema.FUNDO_PAINEL);
        tabela.getTableHeader().setForeground(Tema.AZUL_CLARO);
        tabela.getTableHeader().setFont(Tema.FONTE_UI.deriveFont(Font.BOLD));
        DefaultTableCellRenderer celula = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int l, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, l, c);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setBackground(sel ? Tema.SELECAO : (l % 2 == 0 ? Tema.FUNDO : Tema.FUNDO_PAINEL));
                setForeground("NULL".equals(v) ? Tema.TEXTO_FRACO : Tema.TEXTO);
                return this;
            }
        };
        tabela.setDefaultRenderer(Object.class, celula);
        JScrollPane rol = new JScrollPane(tabela);
        rol.getViewport().setBackground(Tema.FUNDO);
        rol.setBorder(BorderFactory.createEmptyBorder());
        return rol;
    }

    private void atualizarTabelas() {
        java.util.List<String> nomes = new java.util.ArrayList<>();
        for (String[] t : banco.itens()) nomes.add(t[0] + "  (" + t[1] + ")");
        listaTabelas.setListData(nomes.toArray(new String[0]));
    }

    private void escrever(String s, Color cor) {
        StyledDocument doc = log.getStyledDocument();
        SimpleAttributeSet estilo = new SimpleAttributeSet();
        StyleConstants.setForeground(estilo, cor);
        try {
            doc.insertString(doc.getLength(), s, estilo);
        } catch (Exception ignorado) { }
        log.setCaretPosition(doc.getLength());
    }
}
