package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A JANELA PRINCIPAL do editor:
 *   - menus e barra de botões em cima
 *   - árvore do projeto na esquerda
 *   - abas com os arquivos abertos no meio
 *   - console embaixo
 *   - barra de status no rodapé (linguagem e posição do cursor)
 */
public class JanelaEditor extends JFrame implements PainelProjeto.Acoes {

    private final File projeto;     // null = sem projeto (só arquivos soltos)
    private final JTabbedPane abas = new JTabbedPane();
    private final Console console = new Console();
    private final PainelProblemas painelProblemas = new PainelProblemas();
    private final JTabbedPane abasBaixo = new JTabbedPane();
    private final PainelProjeto painelProjeto = new PainelProjeto(this);
    private final JSplitPane divisaoVertical;
    private final JSplitPane divisaoLateral;
    private final JSplitPane divisaoHelper;
    private final PainelHelper helper;
    private final JLabel statusLinguagem = new JLabel();
    private final JLabel statusPosicao = new JLabel();
    private final JMenu menuRecentes = new JMenu("Abrir recente");
    private final JComboBox<Linguagem.Alternativa> rodarCom = new JComboBox<>();
    private final JLabel rotuloRodarCom = new JLabel("  com: ");
    private boolean atualizandoRodarCom;

    public JanelaEditor(File projeto) {
        super(App.NOME);
        this.projeto = projeto;
        setIconImage(Icone.carregar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(Config.numero("janela.largura", 1200), Config.numero("janela.altura", 760));
        setLocationRelativeTo(null);

        abas.setFont(Tema.FONTE_UI);
        abas.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        abas.addChangeListener(e -> {
            atualizarStatus();
            pintarTitulosDasAbas();
            painelProblemas.mostrar(aba() == null ? null : aba().getTexto());
        });

        // Embaixo: Console e Problemas, em abas
        abasBaixo.setFont(Tema.FONTE_UI);
        abasBaixo.addTab("Console", console);
        abasBaixo.addTab("Problemas", painelProblemas);
        painelProblemas.setAoMudarQuantidade(() -> {
            int n = painelProblemas.quantidade();
            abasBaixo.setTitleAt(1, n == 0 ? "Problemas" : "Problemas (" + n + ")");
        });
        divisaoVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, abas, abasBaixo);
        divisaoVertical.setResizeWeight(0.72);
        divisaoVertical.setDividerSize(5);
        divisaoVertical.setBorder(null);

        divisaoLateral = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelProjeto, divisaoVertical);
        divisaoLateral.setDividerLocation(240);
        divisaoLateral.setDividerSize(5);
        divisaoLateral.setDividerLocation(240);
        divisaoLateral.setBorder(null);

        // TazzHelper do lado direito
        helper = new PainelHelper(new PainelHelper.Contexto() {
            public String nomeArquivo() { return aba() == null || aba().getArquivo() == null ? null : aba().getArquivo().getName(); }
            public String linguagem() { return aba() == null ? "Texto" : aba().getTexto().getLinguagem().nome; }
            public String codigo() { return aba() == null ? "" : aba().getTexto().texto(); }
            public String console() { return console.textoDaSaida(); }
            public java.util.List<VerificadorHtml.Problema> problemas() { return aba() == null ? null : aba().getTexto().getProblemas(); }
        });
        divisaoHelper = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, divisaoLateral, helper);
        divisaoHelper.setResizeWeight(1.0);
        Tema.escurecer(divisaoHelper);
        divisaoHelper.setDividerSize(5);
        helper.setVisible(Config.texto("helper.visivel", "sim").equals("sim"));
        console.setAoFaltarPrograma(this::faltouPrograma);
        console.setAoExplicar(() -> {
            mostrarHelper();
            helper.explicarErro();
        });

        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.setBackground(Tema.FUNDO_PAINEL);
        conteudo.add(criarBarraBotoes(), BorderLayout.NORTH);
        conteudo.add(divisaoHelper, BorderLayout.CENTER);
        conteudo.add(criarBarraStatus(), BorderLayout.SOUTH);
        setContentPane(conteudo);
        JMenuBar menus = criarMenus();
        Tema.escurecer(menus);
        setJMenuBar(menus);
        Tema.escurecer(divisaoVertical);
        Tema.escurecer(divisaoLateral);
        divisaoVertical.setDividerSize(5);
        divisaoLateral.setDividerSize(5);
        divisaoLateral.setDividerLocation(240);

        if (projeto != null) {
            painelProjeto.setRaiz(projeto);
            Config.adicionarProjeto(projeto.getAbsolutePath());
            Config.colocar("ultima.pasta", projeto.getAbsolutePath());
        } else {
            painelProjeto.setVisible(false);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sair();
            }
        });

        novaAbaVazia();

        // Configurações > Salvar sozinho: a cada 30 segundos salva o que mudou (só arquivos que já têm nome)
        javax.swing.Timer autoSalvar = new javax.swing.Timer(30_000, e -> {
            if (!Ajustes.salvarSozinho()) return;
            for (AbaEditor a : todasAbas()) {
                if (a.isModificado() && a.getArquivo() != null) {
                    try { a.salvarEm(a.getArquivo()); } catch (IOException ignorado) { }
                }
            }
        });
        autoSalvar.start();
    }

    // =====================================================================
    //  MENUS
    // =====================================================================

    private JMenuBar criarMenus() {
        JMenuBar barra = new JMenuBar();

        JMenu arquivo = menu("Arquivo");
        arquivo.add(item("Novo arquivo...", KeyEvent.VK_N, 0, () -> novoArquivo(pastaPadrao())));
        arquivo.add(item("Novo sem título", KeyEvent.VK_N, InputEvent.SHIFT_DOWN_MASK, this::novaAbaVazia));
        arquivo.add(item("Abrir arquivo...", KeyEvent.VK_O, 0, this::escolherEAbrir));
        arquivo.add(item("Abrir pasta como projeto...", 0, 0, this::abrirOutroProjeto));
        arquivo.add(menuRecentes);
        atualizarRecentes();
        arquivo.addSeparator();
        arquivo.add(item("Salvar", KeyEvent.VK_S, 0, this::salvar));
        arquivo.add(item("Salvar como...", KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK, this::salvarComo));
        arquivo.add(item("Salvar tudo", KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK, this::salvarTudo));
        arquivo.addSeparator();
        arquivo.add(item("Fechar aba", KeyEvent.VK_W, 0, () -> fecharAba(aba())));
        arquivo.add(item("Fechar projeto (tela inicial)", 0, 0, this::voltarTelaInicial));
        arquivo.add(item("Abrir pasta dos meus projetos", 0, 0,
                () -> abrirNoExplorador(Pastas.documentos().toFile())));
        arquivo.add(item("Abrir pasta das configurações", 0, 0,
                () -> abrirNoExplorador(Pastas.dados().toFile())));
        arquivo.addSeparator();
        arquivo.add(item("Configurações...", KeyEvent.VK_COMMA, 0, this::abrirConfiguracoes));
        arquivo.addSeparator();
        arquivo.add(item("Sair", 0, 0, this::sair));
        barra.add(arquivo);

        JMenu editar = menu("Editar");
        editar.add(item("Desfazer", KeyEvent.VK_Z, 0, () -> { if (aba() != null) aba().desfazer(); }));
        editar.add(item("Refazer", KeyEvent.VK_Y, 0, () -> { if (aba() != null) aba().refazer(); }));
        editar.addSeparator();
        editar.add(item("Selecionar tudo", KeyEvent.VK_A, 0, () -> { if (aba() != null) aba().getTexto().selectAll(); }));
        JMenuItem dica = new JMenuItem("Ctrl+/ comenta  •  Ctrl+D duplica  •  Ctrl+Espaço sugestões");
        dica.setEnabled(false);
        editar.addSeparator();
        editar.add(dica);
        barra.add(editar);

        JMenu exibir = menu("Exibir");
        exibir.add(item("Aumentar fonte", KeyEvent.VK_EQUALS, 0, () -> mudarFonte(+2)));
        exibir.add(item("Diminuir fonte", KeyEvent.VK_MINUS, 0, () -> mudarFonte(-2)));
        exibir.addSeparator();
        JMenuItem arvore = item("Mostrar/esconder projeto", 0, 0, this::alternarProjeto);
        arvore.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK));
        exibir.add(arvore);
        exibir.add(item("Mostrar/esconder console", KeyEvent.VK_J, 0, this::alternarConsole));
        exibir.add(item("Mostrar/esconder o TazzHelper", KeyEvent.VK_H, InputEvent.SHIFT_DOWN_MASK, this::alternarHelper));
        barra.add(exibir);

        JMenu executar = menu("Executar");
        JMenuItem rodar = item("Rodar (o projeto, se tiver comando; senão o arquivo)", 0, 0, this::rodar);
        rodar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        executar.add(rodar);
        executar.add(item("Rodar só este arquivo", KeyEvent.VK_F5, 0, this::rodarArquivo));
        executar.add(item("Configurar o comando do projeto...", 0, 0, this::configurarProjeto));
        JMenuItem parar = item("Parar", 0, 0, console::parar);
        parar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.SHIFT_DOWN_MASK));
        executar.add(parar);
        executar.addSeparator();
        JMenuItem terminal = item("Abrir terminal", 0, 0, this::abrirTerminal);
        terminal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.ALT_DOWN_MASK));
        executar.add(terminal);
        barra.add(executar);

        JMenu plugins = menu("Plugins");
        plugins.add(item("Gerenciar plugins (linguagens portáteis)...", 0, 0, () -> {
            if (!Executaveis.WINDOWS) {
                JOptionPane.showMessageDialog(this, "No Linux não precisa de plugin: as linguagens se instalam pelo terminal,\n"
                        + "com um comando só (ex.: sudo apt install python3).\n\n"
                        + "Abra Plugins > Tutorial de instalação e escolha a linguagem: ele mostra o comando certo.",
                        "Plugins no Linux", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            new JanelaPlugins(this).setVisible(true);
        }));
        plugins.add(item("Tutorial de instalação no PC...", 0, 0, () -> abrirTutorial(aba() != null && aba().getArquivo() != null
                ? Catalogo.porArquivo(aba().getArquivo()) : Catalogo.porNome("Python"))));
        plugins.add(item("Abrir a pasta dos plugins", 0, 0, () -> {
            java.io.File d = Pastas.plugins().toFile();
            d.mkdirs();
            abrirNoExplorador(d);
        }));
        barra.add(plugins);

        JMenu ajuda = menu("Ajuda");
        ajuda.add(item("Por que o antivírus desconfia?", 0, 0, this::explicarAntivirus));
        ajuda.addSeparator();
        ajuda.add(item("Sobre", 0, 0, () -> JOptionPane.showMessageDialog(this,
                App.NOME + " versão " + App.VERSAO + "\nFeito pelo João, em Java!\n\n"
                        + "Programa:        " + Pastas.instalacao() + "\n"
                        + "Configurações:   " + Pastas.dados() + "\n"
                        + "Seus projetos:   " + Pastas.documentos() + "\n"
                        + "Plugins e IA:    " + Pastas.compartilhada(),
                "Sobre", JOptionPane.INFORMATION_MESSAGE)));
        barra.add(ajuda);

        return barra;
    }

    /** Explica o aviso do antivírus e como evitar (com a ajuda de um adulto). */
    private void explicarAntivirus() {
        String pasta = Pastas.compilados().toString();
        JTextArea t = new JTextArea(
                "Quando você roda um programa em C, C++, Go, Rust..., o " + App.NOME + " cria um .exe NOVINHO.\n"
              + "Os antivírus (Avast, Windows Defender, Norton...) desconfiam de todo .exe que nunca viram:\n"
              + "eles não sabem quem fez, então analisam por alguns segundos antes de deixar abrir.\n\n"
              + "Isso NÃO quer dizer que tem vírus. O programa foi feito por você, a partir do seu código.\n"
              + "Os programas grandes (Chrome, jogos...) não passam por isso porque são \"assinados\"\n"
              + "(um certificado digital pago que diz quem fez o programa).\n\n"
              + "COMO EVITAR (peça pra um adulto, porque mexe na segurança do PC):\n"
              + "Coloque a pasta dos compilados como \"exceção\" no antivírus:\n\n"
              + "    " + pasta + "\n\n"
              + "• Avast: Menu > Configurações > Geral > Exceções > Adicionar exceção\n"
              + "• Windows Defender: Segurança do Windows > Proteção contra vírus e ameaças >\n"
              + "  Gerenciar configurações > Exclusões > Adicionar exclusão > Pasta\n\n"
              + "Só coloque como exceção essa pasta, NUNCA a pasta Downloads ou o disco C: inteiro!\n"
              + "Se você mandar o seu .exe pra outra pessoa, o antivírus dela também vai desconfiar:\n"
              + "avise ela que foi você que fez.");
        t.setEditable(false);
        t.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        t.setBackground(Tema.FUNDO_PAINEL);
        t.setForeground(Tema.TEXTO);
        t.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        Object[] botoes = {"Copiar o caminho da pasta", "Abrir a pasta", "Fechar"};
        int r = JOptionPane.showOptionDialog(this, t, "Por que o antivírus desconfia?", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, botoes, botoes[2]);
        if (r == 0) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(pasta), null);
        } else if (r == 1) {
            try { Desktop.getDesktop().open(Pastas.compilados().toFile()); } catch (Exception ignorado) { }
        }
    }

    private JMenu menu(String nome) {
        JMenu m = new JMenu(nome);
        m.setForeground(Tema.TEXTO);
        return m;
    }

    /** Cria um item de menu. tecla = atalho com Ctrl (0 = sem atalho). */
    private JMenuItem item(String nome, int tecla, int extra, Runnable acao) {
        JMenuItem i = new JMenuItem(nome);
        if (tecla != 0) {
            i.setAccelerator(KeyStroke.getKeyStroke(tecla, InputEvent.CTRL_DOWN_MASK | extra));
        }
        i.addActionListener(e -> acao.run());
        return i;
    }

    private void atualizarRecentes() {
        menuRecentes.removeAll();
        List<String> lista = Config.recentes();
        if (lista.isEmpty()) {
            JMenuItem vazio = new JMenuItem("(nenhum)");
            vazio.setEnabled(false);
            menuRecentes.add(vazio);
        }
        for (String caminho : lista) {
            menuRecentes.add(item(caminho, 0, 0, () -> abrirArquivo(new File(caminho))));
        }
    }

    // =====================================================================
    //  BARRAS
    // =====================================================================

    private JPanel criarBarraBotoes() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Tema.FUNDO_PAINEL);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDA),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JButton novo = Tema.botao("Novo", Icones.novo(), Tema.LINHA_ATUAL);
        JButton abrir = Tema.botao("Abrir", Icones.abrir(), Tema.LINHA_ATUAL);
        JButton salvar = Tema.botao("Salvar", Icones.salvar(), Tema.LINHA_ATUAL);
        JButton rodar = Tema.botao("Rodar", Icones.rodar(), Tema.AZUL);
        JButton parar = Tema.botao("Parar", Icones.parar(), Tema.PERIGO);
        JButton terminal = Tema.botao("Terminal", Icones.terminal(), Tema.LINHA_ATUAL);
        JButton botaoHelper = Tema.botao("TazzHelper", Icones.helper(), new Color(0x5B5BD6));
        JButton ajustes = Tema.botao("", Icones.engrenagem(), Tema.LINHA_ATUAL);
        ajustes.setBorder(BorderFactory.createEmptyBorder(6, 9, 6, 9));

        novo.setToolTipText("Novo arquivo (Ctrl+N)");
        abrir.setToolTipText("Abrir arquivo (Ctrl+O)");
        salvar.setToolTipText("Salvar (Ctrl+S)");
        rodar.setToolTipText("Rodar o código (F5)");
        parar.setToolTipText("Parar o programa que está rodando");
        terminal.setToolTipText("Abrir um terminal na pasta");
        botaoHelper.setToolTipText("Mostrar ou esconder o TazzHelper");
        ajustes.setToolTipText("Configurações (Ctrl+,)");

        novo.addActionListener(e -> novoArquivo(pastaPadrao()));
        abrir.addActionListener(e -> escolherEAbrir());
        salvar.addActionListener(e -> salvar());
        rodar.addActionListener(e -> rodar());
        parar.addActionListener(e -> console.parar());
        terminal.addActionListener(e -> abrirTerminal());
        botaoHelper.addActionListener(e -> alternarHelper());
        ajustes.addActionListener(e -> abrirConfiguracoes());

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        esquerda.setOpaque(false);
        esquerda.add(novo);
        esquerda.add(abrir);
        esquerda.add(salvar);
        esquerda.add(separador());
        esquerda.add(rodar);
        rotuloRodarCom.setForeground(Tema.TEXTO_FRACO);
        rotuloRodarCom.setFont(Tema.FONTE_UI);
        rodarCom.setFont(Tema.FONTE_UI);
        rodarCom.setToolTipText("Rodar com... (ex.: Node.js, Deno ou Bun; MySQL ou PostgreSQL)");
        rodarCom.addActionListener(e -> {
            if (atualizandoRodarCom || aba() == null || aba().getArquivo() == null) return;
            Linguagem.Alternativa alt = (Linguagem.Alternativa) rodarCom.getSelectedItem();
            if (alt != null) Catalogo.porArquivo(aba().getArquivo()).escolher(alt.nome);
        });
        esquerda.add(rotuloRodarCom);
        esquerda.add(rodarCom);
        esquerda.add(parar);
        barra.add(esquerda, BorderLayout.WEST);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        direita.setOpaque(false);
        if (projeto != null) {
            JLabel nomeProjeto = new JLabel(projeto.getName(), Icones.pasta(), JLabel.LEFT);
            nomeProjeto.setForeground(Tema.TEXTO_FRACO);
            nomeProjeto.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD));
            nomeProjeto.setIconTextGap(6);
            nomeProjeto.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
            direita.add(nomeProjeto);
        }
        direita.add(terminal);
        direita.add(botaoHelper);
        direita.add(ajustes);
        barra.add(direita, BorderLayout.EAST);
        return barra;
    }

    /** Uma linha fininha em pé, pra separar grupos de botões. */
    private static JComponent separador() {
        JPanel s = new JPanel();
        s.setBackground(Tema.BORDA);
        s.setPreferredSize(new java.awt.Dimension(1, 22));
        JPanel volta = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        volta.setOpaque(false);
        volta.add(s);
        return volta;
    }

    private JPanel criarBarraStatus() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Tema.FUNDO_PAINEL);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDA),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        for (JLabel l : new JLabel[]{statusLinguagem, statusPosicao}) {
            l.setForeground(Tema.TEXTO_FRACO);
            l.setFont(Tema.FONTE_UI.deriveFont(Tema.FONTE_UI.getSize2D() - 1f));
        }
        statusLinguagem.setIcon(new javax.swing.Icon() {   // uma bolinha na cor de destaque
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Tema.AZUL);
                g2.fillOval(x, y + 2, 8, 8);
                g2.dispose();
            }
            public int getIconWidth() { return 8; }
            public int getIconHeight() { return 12; }
        });
        statusLinguagem.setIconTextGap(7);
        barra.add(statusLinguagem, BorderLayout.WEST);
        barra.add(statusPosicao, BorderLayout.EAST);
        return barra;
    }

    private void atualizarStatus() {
        AbaEditor a = aba();
        String nomeProjeto = projeto == null ? "" : " [" + projeto.getName() + "]";
        if (a == null) {
            statusLinguagem.setText(" ");
            statusPosicao.setText(" ");
            setTitle(App.NOME + nomeProjeto);
            return;
        }
        statusLinguagem.setText(a.getArquivo() == null ? "Texto" : Linguagens.nome(a.getArquivo()));
        atualizarRodarCom(a.getArquivo());
        statusPosicao.setText(a.posicaoCursor());
        setTitle(a.titulo() + nomeProjeto + " - " + App.NOME);
    }

    /** Mostra o "com: [Node.js ▼]" só quando a linguagem tem mais de um jeito de rodar. */
    private void atualizarRodarCom(File f) {
        Linguagem l = f == null ? Catalogo.TEXTO : Catalogo.porArquivo(f);
        atualizandoRodarCom = true;
        rodarCom.removeAllItems();
        for (Linguagem.Alternativa alt : l.alternativas) rodarCom.addItem(alt);
        Linguagem.Alternativa atual = l.escolhida();
        if (atual != null) rodarCom.setSelectedItem(atual);
        boolean mostrar = l.alternativas.size() > 1;
        rodarCom.setVisible(mostrar);
        rotuloRodarCom.setVisible(mostrar);
        atualizandoRodarCom = false;
    }

    // =====================================================================
    //  ABAS (cada aba tem o nome + um "x" pra fechar)
    // =====================================================================

    private AbaEditor aba() {
        return (AbaEditor) abas.getSelectedComponent();
    }

    private List<AbaEditor> todasAbas() {
        List<AbaEditor> l = new ArrayList<>();
        for (int i = 0; i < abas.getTabCount(); i++) l.add((AbaEditor) abas.getComponentAt(i));
        return l;
    }

    private void adicionarAba(AbaEditor a) {
        abas.addTab(a.titulo(), a);
        abas.setTabComponentAt(abas.indexOfComponent(a), cabecalhoDaAba(a));
        abas.setSelectedComponent(a);
        a.setAoMudar(() -> {
            pintarTitulosDasAbas();
            atualizarStatus();
        });
        a.getTexto().addCaretListener(e -> atualizarStatus());
        a.getTexto().setAoVerificar(() -> {
            if (aba() == a) painelProblemas.mostrar(a.getTexto());
        });
        a.getTexto().verificar();
        SwingUtilities.invokeLater(() -> a.getTexto().requestFocusInWindow());
        pintarTitulosDasAbas();
        atualizarStatus();
    }

    /** O "cabeçalho" da aba: o nome do arquivo e o botãozinho de fechar. */
    private JPanel cabecalhoDaAba(AbaEditor a) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel nome = new JLabel(a.titulo());
        nome.setFont(Tema.FONTE_UI);
        JLabel fechar = new JLabel("×");
        fechar.setFont(new Font("SansSerif", Font.BOLD, 15));
        fechar.setForeground(Tema.TEXTO_FRACO);
        fechar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fechar.setToolTipText("Fechar (Ctrl+W)");
        fechar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { fecharAba(a); }
            @Override public void mouseEntered(MouseEvent e) { fechar.setForeground(Tema.ERRO); }
            @Override public void mouseExited(MouseEvent e)  { fechar.setForeground(Tema.TEXTO_FRACO); }
        });
        p.add(nome);
        p.add(fechar);
        return p;
    }

    /** Atualiza os nomes das abas e deixa a aba escolhida em destaque. */
    private void pintarTitulosDasAbas() {
        for (int i = 0; i < abas.getTabCount(); i++) {
            AbaEditor a = (AbaEditor) abas.getComponentAt(i);
            JPanel cab = (JPanel) abas.getTabComponentAt(i);
            if (cab == null) continue;
            JLabel nome = (JLabel) cab.getComponent(0);
            nome.setText(a.titulo());
            boolean escolhida = i == abas.getSelectedIndex();
            nome.setForeground(escolhida ? Tema.TEXTO : Tema.TEXTO_FRACO);
            nome.setFont(Tema.FONTE_UI.deriveFont(escolhida ? Font.BOLD : Font.PLAIN));
            cab.setBorder(BorderFactory.createCompoundBorder(   // risquinho colorido embaixo da aba escolhida
                    BorderFactory.createMatteBorder(0, 0, 2, 0, escolhida ? Tema.AZUL : new Color(0, 0, 0, 0)),
                    BorderFactory.createEmptyBorder(2, 0, 3, 0)));
        }
    }

    private void novaAbaVazia() {
        adicionarAba(new AbaEditor(null));
    }

    // =====================================================================
    //  ARQUIVOS
    // =====================================================================

    /** A pasta onde arquivos novos vão ser criados. */
    private File pastaPadrao() {
        if (projeto != null) return projeto;
        AbaEditor a = aba();
        if (a != null && a.getArquivo() != null) return a.getArquivo().getParentFile();
        return null;
    }

    private void escolherEAbrir() {
        JFileChooser escolha = new JFileChooser(pastaPadrao() != null ? pastaPadrao()
                : new File(Config.texto("ultima.pasta", System.getProperty("user.home"))));
        if (escolha.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            abrirArquivo(escolha.getSelectedFile());
        }
    }

    @Override
    public void abrir(File arquivo) {
        abrirArquivo(arquivo);
    }

    public void abrirArquivo(File f) {
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "Esse arquivo não existe mais:\n" + f);
            return;
        }
        for (AbaEditor a : todasAbas()) {           // já está aberto? só vai pra aba dele
            if (f.equals(a.getArquivo())) {
                abas.setSelectedComponent(a);
                return;
            }
        }
        // Se a única aba é uma "Sem título" vazia, troca ela pelo arquivo.
        AbaEditor atual = aba();
        if (abas.getTabCount() == 1 && atual.getArquivo() == null
                && !atual.isModificado() && atual.getTexto().getText().isEmpty()) {
            abas.remove(atual);
        }
        adicionarAba(new AbaEditor(f));
        lembrar(f);
    }

    private void lembrar(File f) {
        Config.adicionarRecente(f.getAbsolutePath());
        Config.colocar("ultima.pasta", f.getParent());
        atualizarRecentes();
    }

    /** Janela "Novo arquivo": nome + tipo (Classe Java, Python, HTML...). */
    @Override
    public void novoArquivo(File pasta) {
        if (pasta == null) {
            JFileChooser e = new JFileChooser(Config.texto("ultima.pasta", System.getProperty("user.home")));
            e.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            e.setDialogTitle("Em qual pasta criar o arquivo?");
            if (e.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            pasta = e.getSelectedFile();
        }

        JTextField campoNome = new JTextField();
        JComboBox<String> tipo = new JComboBox<>(Modelos.tipos());
        tipo.setMaximumRowCount(18);
        // Já deixa escolhida a linguagem do arquivo que está aberto (fica mais rápido).
        AbaEditor atual = aba();
        if (atual != null && atual.getArquivo() != null) {
            Linguagem lg = Catalogo.porArquivo(atual.getArquivo());
            if (lg != Catalogo.TEXTO && !lg.nome.equals("Java")) tipo.setSelectedItem(lg.nome);
        }
        Object[] campos = {"Tipo:", tipo, "Nome (sem extensão):", campoNome, "Pasta: " + pasta};
        SwingUtilities.invokeLater(campoNome::requestFocusInWindow);
        int r = JOptionPane.showConfirmDialog(this, campos, "Novo arquivo", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) return;
        String t = (String) tipo.getSelectedItem();
        String ext = Modelos.extensao(t);

        String semExtensao = nome.contains(".") ? nome.substring(0, nome.lastIndexOf('.')) : nome;
        String nomeArquivo = nome.contains(".") || ext.isEmpty() ? nome : nome + "." + ext;
        if (t.startsWith("Classe Java") || Modelos.conteudo(t, "\u0000").contains("\u0000")) semExtensao = semExtensao.replaceAll("[^A-Za-z0-9_]", "");

        File f = new File(pasta, nomeArquivo);
        if (f.exists()) {
            JOptionPane.showMessageDialog(this, "Já existe um arquivo com esse nome.");
            return;
        }
        try {
            String conteudo = Modelos.conteudo(t, semExtensao).replace("\n", System.lineSeparator());
            Files.write(f.toPath(), conteudo.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Não consegui criar: " + e.getMessage());
            return;
        }
        painelProjeto.atualizar();
        abrirArquivo(f);
    }

    @Override
    public void novaPasta(File pasta) {
        String nome = JOptionPane.showInputDialog(this, "Nome da pasta nova:", "Nova pasta", JOptionPane.PLAIN_MESSAGE);
        if (nome == null || nome.trim().isEmpty()) return;
        new File(pasta, nome.trim()).mkdirs();
        painelProjeto.atualizar();
    }

    @Override
    public void renomear(File arquivo) {
        String nome = (String) JOptionPane.showInputDialog(this, "Novo nome:", "Renomear",
                JOptionPane.PLAIN_MESSAGE, null, null, arquivo.getName());
        if (nome == null || nome.trim().isEmpty() || nome.equals(arquivo.getName())) return;
        File novo = new File(arquivo.getParentFile(), nome.trim());
        if (novo.exists() || !arquivo.renameTo(novo)) {
            JOptionPane.showMessageDialog(this, "Não consegui renomear (já existe esse nome?).");
            return;
        }
        // Abas abertas desse arquivo (ou de arquivos dentro dessa pasta) passam a apontar pro nome novo.
        String antigo = arquivo.getAbsolutePath();
        for (AbaEditor a : todasAbas()) {
            if (a.getArquivo() == null) continue;
            String c = a.getArquivo().getAbsolutePath();
            if (c.equals(antigo) || c.startsWith(antigo + File.separator)) {
                a.trocarArquivo(new File(novo.getAbsolutePath() + c.substring(antigo.length())));
            }
        }
        painelProjeto.atualizar();
    }

    @Override
    public void excluir(File arquivo) {
        int r = JOptionPane.showConfirmDialog(this,
                "Mandar \"" + arquivo.getName() + "\" para a Lixeira?", "Excluir", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        boolean foi = false;
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MOVE_TO_TRASH)) {
                foi = Desktop.getDesktop().moveToTrash(arquivo);
            }
        } catch (Exception ignorado) { }
        if (!foi) {
            JOptionPane.showMessageDialog(this, "Não consegui mandar pra Lixeira.");
            return;
        }
        String caminho = arquivo.getAbsolutePath();
        for (AbaEditor a : todasAbas()) {   // fecha as abas do que foi excluído
            if (a.getArquivo() != null && a.getArquivo().getAbsolutePath().startsWith(caminho)) {
                abas.remove(a);
            }
        }
        if (abas.getTabCount() == 0) novaAbaVazia();
        painelProjeto.atualizar();
    }

    @Override
    public void abrirNoExplorador(File arquivo) {
        try {
            if (arquivo.isFile() && System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("explorer.exe", "/select,", arquivo.getAbsolutePath()).start();
            } else {
                Desktop.getDesktop().open(arquivo.isDirectory() ? arquivo : arquivo.getParentFile());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fica em:\n" + arquivo.getAbsolutePath());
        }
    }

    // ----- salvar -----

    /** Salva a aba atual. Retorna true se salvou. */
    private boolean salvar() {
        return salvarAba(aba());
    }

    private boolean salvarAba(AbaEditor a) {
        if (a == null) return false;
        if (a.getArquivo() == null) return salvarComo(a);
        return gravar(a, a.getArquivo());
    }

    private void salvarTudo() {
        for (AbaEditor a : todasAbas()) {
            if (a.isModificado()) salvarAba(a);
        }
    }

    private boolean salvarComo() {
        return salvarComo(aba());
    }

    private boolean salvarComo(AbaEditor a) {
        if (a == null) return false;
        abas.setSelectedComponent(a);
        File inicio = pastaPadrao() != null ? pastaPadrao()
                : new File(Config.texto("ultima.pasta", System.getProperty("user.home")));
        JFileChooser escolha = new JFileChooser(inicio);
        escolha.setDialogTitle("Salvar como (não esqueça a extensão: .java, .py, .html...)");
        if (escolha.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return false;
        File destino = escolha.getSelectedFile();
        if (destino.exists() && !destino.equals(a.getArquivo())) {
            int r = JOptionPane.showConfirmDialog(this, destino.getName() + " já existe. Substituir?",
                    "Substituir?", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return false;
        }
        return gravar(a, destino);
    }

    private boolean gravar(AbaEditor a, File destino) {
        try {
            a.salvarEm(destino);
            lembrar(destino);
            painelProjeto.atualizar();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Não consegui salvar:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ----- fechar -----

    /** Pergunta se quer salvar antes de fechar. Retorna false se a pessoa cancelou. */
    private boolean podeFechar(AbaEditor a) {
        if (!a.isModificado()) return true;
        abas.setSelectedComponent(a);
        int r = JOptionPane.showConfirmDialog(this,
                "Salvar as mudanças em \"" + a.titulo().replace("● ", "") + "\"?",
                "Arquivo não salvo", JOptionPane.YES_NO_CANCEL_OPTION);
        if (r == JOptionPane.CANCEL_OPTION || r == JOptionPane.CLOSED_OPTION) return false;
        if (r == JOptionPane.YES_OPTION) return salvarAba(a);
        return true;
    }

    private void fecharAba(AbaEditor a) {
        if (a != null && podeFechar(a)) {
            abas.remove(a);
            if (abas.getTabCount() == 0) novaAbaVazia();
        }
    }

    private boolean fecharTudo() {
        for (AbaEditor a : todasAbas()) {
            if (!podeFechar(a)) return false;
        }
        console.parar();
        Config.colocar("janela.largura", getWidth());
        Config.colocar("janela.altura", getHeight());
        return true;
    }

    private void voltarTelaInicial() {
        if (!fecharTudo()) return;
        dispose();
        new JanelaInicial().setVisible(true);
    }

    private void abrirOutroProjeto() {
        JFileChooser e = new JFileChooser(Config.texto("ultima.pasta", System.getProperty("user.home")));
        e.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        e.setDialogTitle("Escolha a pasta do projeto");
        if (e.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        if (!fecharTudo()) return;
        dispose();
        new JanelaEditor(e.getSelectedFile()).setVisible(true);
    }

    /** Abre a janela de Configurações. */
    private void abrirConfiguracoes() {
        new JanelaConfiguracoes(this, this::aplicarConfiguracoes).setVisible(true);
    }

    /** Aplica na hora o que dá pra aplicar sem reiniciar (fontes, editor, console). */
    private void aplicarConfiguracoes() {
        for (AbaEditor a : todasAbas()) a.aplicarConfiguracoes();
        console.aplicarConfiguracoes();
        pintarTitulosDasAbas();
    }

    /** Fecha tudo (pedindo pra salvar) e abre o TazzCode de novo, com os mesmos arquivos. */
    void reiniciar() {
        List<String> abertos = new ArrayList<>();
        for (AbaEditor a : todasAbas()) if (a.getArquivo() != null) abertos.add(a.getArquivo().getAbsolutePath());
        if (!fecharTudo()) return;
        Reiniciar.agora(projeto, abertos);
    }

    private void sair() {
        if (!fecharTudo()) return;
        dispose();
        System.exit(0);
    }

    // =====================================================================
    //  RODAR CÓDIGO E TERMINAL
    // =====================================================================

    /** F5: se o projeto tem um comando (arquivo .tazzcode), roda o projeto; senão, o arquivo aberto. */
    private void rodar() {
        Projeto p = Projeto.ler(projeto);
        if (p != null) rodarProjeto(p);
        else rodarArquivo();
    }

    private void rodarProjeto(Projeto p) {
        salvarTudo();
        mostrarConsole();
        rodarComando(p.rodar, p.precisa, () -> {
            if (!p.url.isEmpty()) abrirDepois(p.url);
        });
    }

    /**
     * Roda um comando do projeto (pode ter vários passos com &&).
     * Passos especiais: "navegador:arquivo.html" e "servidor:arquivo.html" abrem no navegador.
     */
    public void rodarComando(String comando, String precisa, Runnable depois) {
        java.util.List<String> passos = new ArrayList<>();
        String abrirNoFim = null;
        for (String passo : comando.split("&&")) {
            String t = passo.trim();
            if (t.startsWith("navegador:") || t.startsWith("servidor:")) abrirNoFim = t;
            else if (!t.isEmpty()) passos.add(t);
        }
        final String fim = abrirNoFim;
        Runnable tudoCerto = () -> {
            if (fim != null) {
                String arquivo = fim.substring(fim.indexOf(':') + 1);
                try {
                    if (fim.startsWith("servidor:")) abrirNoNavegador(java.net.URI.create(ServidorLocal.endereco(projeto, arquivo)));
                    else abrirNoNavegador(new File(projeto, arquivo).toURI());
                } catch (IOException e) {
                    console.escrever("Não consegui ligar o servidor: " + e.getMessage() + "\n", Tema.ERRO);
                }
            }
            if (depois != null) depois.run();
        };
        if (passos.isEmpty()) {
            console.limpar();
            tudoCerto.run();
            return;
        }
        File base = projeto != null ? projeto : (aba() != null && aba().getArquivo() != null ? aba().getArquivo().getParentFile() : new File("."));
        File principal = aba() != null && aba().getArquivo() != null ? aba().getArquivo() : new File(base, "main");
        console.rodarPassos(Linguagens.passos(passos, principal), base, precisa, tudoCerto);
    }

    /** Servidores demoram um pouquinho pra ligar: abre o navegador depois de 4 segundos. */
    private void abrirDepois(String url) {
        console.escrever("(abrindo " + url + " no navegador daqui a pouco...)\n", Tema.TEXTO_FRACO);
        javax.swing.Timer t = new javax.swing.Timer(4000, e -> abrirNoNavegador(java.net.URI.create(url)));
        t.setRepeats(false);
        t.start();
    }

    private void configurarProjeto() {
        if (projeto == null) {
            JOptionPane.showMessageDialog(this, "Abra uma pasta como projeto primeiro.");
            return;
        }
        Projeto p = Projeto.ler(projeto);
        if (p == null) p = new Projeto();
        JTextField cmd = new JTextField(p.rodar, 40);
        JTextField url = new JTextField(p.url, 40);
        Object[] campos = {"Comando do F5 (vários passos: use &&). Ex.: python app.py", cmd,
                "Abrir no navegador depois (opcional). Ex.: http://localhost:5000", url,
                "Deixe o comando vazio pra voltar a rodar só o arquivo aberto."};
        if (JOptionPane.showConfirmDialog(this, campos, "Comando do projeto", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        p.rodar = cmd.getText().trim();
        p.url = url.getText().trim();
        try {
            if (p.rodar.isEmpty()) Files.deleteIfExists(new File(projeto, Projeto.ARQUIVO).toPath());
            else p.salvar(projeto);
            painelProjeto.atualizar();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Não consegui salvar: " + e.getMessage());
        }
    }

    private void rodarArquivo() {
        AbaEditor a = aba();
        if (a == null) return;

        // Precisa estar salvo pra rodar (o "java"/"python" lê o arquivo do disco).
        if (a.getArquivo() == null || a.isModificado()) {
            if (!salvar()) return;
        }
        File f = a.getArquivo();
        Linguagem l = Catalogo.porArquivo(f);
        mostrarConsole();

        switch (l.modo) {
            case NAVEGADOR:
                abrirNoNavegador(f.toURI());
                break;
            case LINK:
                console.limpar();
                console.escrever(l.precisa + "\n", Tema.AZUL_CLARO);
                abrirNoNavegador(java.net.URI.create(l.link));
                break;
            case BANCO:
                console.limpar();
                console.escrever("Abrindo o banco de dados SIMULADO (" + l.nome + ") numa janela...\n", Tema.AZUL_CLARO);
                JanelaBanco.rodar(f, a.getTexto().texto(), l);
                break;
            case BRAINFUCK:
                console.rodarInterno("Brainfuck (interpretador do " + App.NOME + ")", new Brainfuck(a.getTexto().texto()));
                break;
            case SO_EDITAR:
                console.limpar();
                console.escrever(l.nome + ": " + l.precisa + "\n", Tema.AZUL_CLARO);
                break;
            default:
                if (l.comandos.isEmpty()) {
                    console.limpar();
                    console.escrever("Ainda não sei rodar arquivos \"" + l.nome + "\" (" + f.getName() + ").\n", Tema.ERRO);
                    return;
                }
                Linguagem.Alternativa alt = l.escolhida();
                console.rodarPassos(Linguagens.passos(l, f), f.getParentFile(), alt != null ? alt.precisa : l.precisa);
        }
    }

    private void abrirNoNavegador(java.net.URI endereco) {
        try {
            Desktop.getDesktop().browse(endereco);
            console.escrever("\nAbrindo no navegador: " + endereco + "\n", Tema.AZUL_CLARO);
        } catch (Exception e) {
            console.escrever("\nNão consegui abrir o navegador: " + e.getMessage() + "\n", Tema.ERRO);
        }
    }

    private void abrirTerminal() {
        File pasta = pastaPadrao();
        if (pasta == null) pasta = new File(System.getProperty("user.home"));
        mostrarConsole();
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        console.rodar(windows ? Arrays.asList("cmd.exe", "/k", "chcp 65001 >nul")
                : Arrays.asList("bash", "-i"), pasta);
    }

    // =====================================================================
    //  EXIBIR
    // =====================================================================

    private void mudarFonte(int delta) {
        int tamanho = Math.max(10, Math.min(40, Config.numero("fonte.tamanho", 16) + delta));
        Config.colocar("fonte.tamanho", tamanho);
        for (AbaEditor a : todasAbas()) a.atualizarFonte();
    }

    private void alternarProjeto() {
        if (projeto == null) {
            JOptionPane.showMessageDialog(this, "Abra uma pasta como projeto pra ver a árvore\n(Arquivo → Abrir pasta como projeto).");
            return;
        }
        painelProjeto.setVisible(!painelProjeto.isVisible());
        if (painelProjeto.isVisible()) divisaoLateral.setDividerLocation(240);
        divisaoLateral.revalidate();
    }

    /** Abre o tutorial de instalação de uma linguagem. */
    public void abrirTutorial(Linguagem l) {
        new JanelaTutorial(this, l, cmd -> {
            mostrarConsole();
            rodarComando(cmd, "", null);
        }).setVisible(true);
    }

    /** Quando o F5 não acha o programa: oferece o plugin ou o tutorial. */
    private void faltouPrograma(String programa) {
        Plugins.Plugin p = Plugins.quemTem(programa);
        Linguagem l = aba() != null && aba().getArquivo() != null ? Catalogo.porArquivo(aba().getArquivo()) : Catalogo.TEXTO;
        String[] opcoes = p != null ? new String[]{"Instalar o plugin " + p.nome, "Ver o tutorial (instalar no PC)", "Agora não"}
                : new String[]{"Ver o tutorial (instalar no PC)", "Agora não"};
        String msg = "O programa \"" + programa + "\" não está instalado neste computador.\n\n"
                + (p != null ? "Dá pra instalar o PLUGIN " + p.nome + " (" + p.tamanho + "): ele roda sem instalar nada no Windows.\n"
                + "Ou você pode seguir o tutorial pra instalar no PC." : "Siga o tutorial pra instalar no PC.");
        int r = JOptionPane.showOptionDialog(this, msg, "Falta instalar", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
        if (p != null && r == 0) {
            JanelaPlugins j = new JanelaPlugins(this);
            j.setVisible(true);
            j.instalar(p);
        } else if ((p != null && r == 1) || (p == null && r == 0)) {
            abrirTutorial(l);
        }
    }

    private void alternarHelper() {
        if (helper.isVisible()) {
            helper.setVisible(false);
            Config.colocar("helper.visivel", "nao");
        } else {
            mostrarHelper();
        }
    }

    private void mostrarHelper() {
        if (helper.isVisible()) return;
        helper.setVisible(true);
        Config.colocar("helper.visivel", "sim");
        divisaoHelper.setDividerLocation(divisaoHelper.getWidth() - 380);
    }

    private void alternarConsole() {
        if (abasBaixo.isVisible()) {
            abasBaixo.setVisible(false);
        } else {
            mostrarConsole();
        }
    }

    private void mostrarConsole() {
        abasBaixo.setSelectedComponent(console);
        if (!abasBaixo.isVisible()) {
            abasBaixo.setVisible(true);
            divisaoVertical.setDividerLocation(0.7);
        }
    }
}
