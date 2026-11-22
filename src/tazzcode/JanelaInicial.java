package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A TELA INICIAL (igual à "Welcome" do IntelliJ):
 * do lado esquerdo os botões, do lado direito a lista de projetos.
 */
public class JanelaInicial extends JFrame {

    private final JList<File> lista = new JList<>();

    private final JPanel cartasLista = new JPanel(new java.awt.CardLayout());

    private static String hex(java.awt.Color c) {
        return String.format("#%06X", c.getRGB() & 0xFFFFFF);
    }

    /** Abre a tela inicial de novo com o tema/fonte novos (aqui não tem nada pra salvar). */
    void recarregar() {
        Tema.carregar();
        Tema.aplicarNoSwing();
        dispose();
        new JanelaInicial().setVisible(true);
    }

    public JanelaInicial() {
        super("Bem-vindo ao " + App.NOME);
        setIconImage(Icone.carregar());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 500);
        setLocationRelativeTo(null);

        JPanel tudo = new JPanel(new BorderLayout());
        tudo.setBackground(Tema.FUNDO);

        // ----- lado esquerdo -----
        JPanel esquerda = new JPanel();
        esquerda.setLayout(new BoxLayout(esquerda, BoxLayout.Y_AXIS));
        esquerda.setBackground(Tema.FUNDO_PAINEL);
        esquerda.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));
        esquerda.setPreferredSize(new Dimension(250, 0));

        JLabel nome = new JLabel(App.NOME);
        nome.setIcon(new javax.swing.ImageIcon(Icone.carregar().getScaledInstance(48, 48, java.awt.Image.SCALE_SMOOTH)));
        nome.setForeground(Tema.TEXTO);
        nome.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD, Tema.FONTE_UI.getSize2D() + 13));
        nome.setIconTextGap(12);
        JLabel versao = new JLabel("versão " + App.VERSAO);
        versao.setForeground(Tema.TEXTO_FRACO);
        versao.setFont(Tema.FONTE_UI);
        nome.setAlignmentX(LEFT_ALIGNMENT);
        versao.setAlignmentX(LEFT_ALIGNMENT);
        esquerda.add(nome);
        esquerda.add(versao);
        esquerda.add(Box.createVerticalStrut(28));

        JPanel botoes = new JPanel(new GridLayout(0, 1, 0, 8));
        botoes.setOpaque(false);
        botoes.setAlignmentX(LEFT_ALIGNMENT);
        int alturaBotao = Tema.FONTE_UI.getSize() + 22;
        botoes.setMaximumSize(new Dimension(220, alturaBotao * 4 + 24));
        JButton novo = Tema.botao("Novo projeto", Icones.novo(), Tema.AZUL);
        JButton abrirPasta = Tema.botao("Abrir pasta como projeto", Icones.abrir(), Tema.LINHA_ATUAL);
        JButton abrirArquivo = Tema.botao("Abrir só um arquivo", Icones.novo(), Tema.LINHA_ATUAL);
        JButton semProjeto = Tema.botao("Continuar sem projeto", Icones.voltar(), Tema.LINHA_ATUAL);
        for (JButton b : new JButton[]{novo, abrirPasta, abrirArquivo, semProjeto}) b.setHorizontalAlignment(JButton.LEFT);
        novo.addActionListener(e -> novoProjeto());
        abrirPasta.addActionListener(e -> escolherPasta());
        abrirArquivo.addActionListener(e -> abrirArquivo());
        semProjeto.addActionListener(e -> abrirEditor(null, null));
        botoes.add(novo);
        botoes.add(abrirPasta);
        botoes.add(abrirArquivo);
        botoes.add(semProjeto);
        esquerda.add(botoes);
        esquerda.add(Box.createVerticalGlue());
        JButton ajustes = Tema.botao("Configurações", Icones.engrenagem(), Tema.FUNDO_PAINEL);
        ajustes.setForeground(Tema.TEXTO_FRACO);
        ajustes.setAlignmentX(LEFT_ALIGNMENT);
        ajustes.addActionListener(e -> new JanelaConfiguracoes(this, () -> { }).setVisible(true));
        esquerda.add(ajustes);
        tudo.add(esquerda, BorderLayout.WEST);

        // ----- lado direito: projetos -----
        JPanel direita = new JPanel(new BorderLayout());
        direita.setBackground(Tema.FUNDO);
        direita.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));
        JLabel titulo = new JLabel("Projetos recentes");
        titulo.setForeground(Tema.TEXTO);
        titulo.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD, Tema.FONTE_UI.getSize2D() + 5));
        titulo.setBorder(BorderFactory.createEmptyBorder(4, 2, 12, 0));
        direita.add(titulo, BorderLayout.NORTH);

        lista.setBackground(Tema.FUNDO);
        lista.setSelectionBackground(Tema.SELECAO);
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                File f = (File) v;
                String html = "<html><b><font color='" + hex(Tema.TEXTO) + "'>" + f.getName() + "</font></b><br>"
                        + "<font color='" + hex(Tema.TEXTO_FRACO) + "'>" + f.getAbsolutePath() + "</font></html>";
                JLabel c = (JLabel) super.getListCellRendererComponent(l, html, i, sel, false);
                c.setIcon(Icones.pasta());
                c.setIconTextGap(12);
                c.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
                c.setFont(Tema.FONTE_UI.deriveFont(Tema.FONTE_UI.getSize2D() + 1));
                c.setForeground(Tema.TEXTO_FRACO);
                c.setBackground(sel ? Tema.SELECAO : Tema.FUNDO);
                return c;
            }
        });
        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && lista.getSelectedValue() != null) {
                    abrirEditor(lista.getSelectedValue(), null);
                }
            }
        });
        JScrollPane rolagem = new JScrollPane(lista);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        JLabel vazio = new JLabel("<html><div style='text-align:center'><b>Nenhum projeto ainda.</b><br><br>"
                + "Clique em <b>Novo projeto</b> pra começar!</div></html>", Icones.pasta(), JLabel.CENTER);
        vazio.setHorizontalTextPosition(JLabel.CENTER);
        vazio.setVerticalTextPosition(JLabel.BOTTOM);
        vazio.setIconTextGap(12);
        vazio.setForeground(Tema.TEXTO_FRACO);
        cartasLista.add(rolagem, "lista");
        cartasLista.add(vazio, "vazio");
        cartasLista.setOpaque(false);
        direita.add(cartasLista, BorderLayout.CENTER);

        JLabel dica = new JLabel("Dois cliques num projeto para abrir.");
        dica.setForeground(Tema.TEXTO_FRACO);
        dica.setFont(Tema.FONTE_UI);
        dica.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        direita.add(dica, BorderLayout.SOUTH);
        tudo.add(direita, BorderLayout.CENTER);

        setContentPane(tudo);
        carregarLista();
    }

    /** Junta os projetos recentes com as pastas que estão em TazzCode\projetos. */
    private void carregarLista() {
        Set<File> projetos = new LinkedHashSet<>();
        for (String c : Config.projetosRecentes()) {
            File f = new File(c);
            if (f.isDirectory()) projetos.add(f);
        }
        File[] daPasta = Pastas.projetos().toFile().listFiles(File::isDirectory);
        if (daPasta != null) for (File f : daPasta) projetos.add(f);
        lista.setListData(new ArrayList<>(projetos).toArray(new File[0]));
        ((java.awt.CardLayout) cartasLista.getLayout()).show(cartasLista, projetos.isEmpty() ? "vazio" : "lista");
    }

    // =====================================================================
    //  ações
    // =====================================================================

    private void novoProjeto() {
        // A lista mistura LINGUAGENS (um arquivo "Olá, mundo!") e MODELOS (frameworks do OneCompiler).
        List<Object> opcoes = new ArrayList<>(java.util.Arrays.asList("Java", "Python", "Site (HTML + CSS + JS)", "Vazio"));
        for (ModelosProjeto.Modelo mo : ModelosProjeto.todos()) opcoes.add(mo);
        for (Linguagem lg : Catalogo.todas()) {
            if (!opcoes.contains(lg.nome) && !lg.modelo.isEmpty() && lg.modo != Linguagem.Modo.LINK) opcoes.add(lg.nome);
        }

        JTextField campoNome = new JTextField("MeuProjeto");
        JTextField busca = new JTextField();
        busca.setToolTipText("Digite pra procurar (ex.: react, flask, mongo...)");
        javax.swing.DefaultListModel<Object> modelo = new javax.swing.DefaultListModel<>();
        opcoes.forEach(modelo::addElement);
        JList<Object> lista = new JList<>(modelo);
        lista.setVisibleRowCount(14);
        lista.setSelectedIndex(0);
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                String texto = (v instanceof ModelosProjeto.Modelo ? "[modelo]  " : "[linguagem]  ") + v;
                return super.getListCellRendererComponent(l, texto, i, sel, foco);
            }
        });
        busca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filtrar() {
                String q = busca.getText().trim().toLowerCase();
                modelo.clear();
                for (Object o : opcoes) if (o.toString().toLowerCase().contains(q)) modelo.addElement(o);
                if (!modelo.isEmpty()) lista.setSelectedIndex(0);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });
        JScrollPane rol = new JScrollPane(lista);
        rol.setPreferredSize(new Dimension(460, 300));
        Object[] campos = {"Nome do projeto:", campoNome, "Começar com (procure aqui):", busca, rol,
                "[modelo] = framework/biblioteca do OneCompiler   [linguagem] = um arquivo Olá, mundo!",
                "Ele vai ficar em: " + Pastas.projetos()};
        int r = JOptionPane.showConfirmDialog(this, campos, "Novo projeto", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION || lista.getSelectedValue() == null) return;
        Object escolhido = lista.getSelectedValue();

        String nome = campoNome.getText().trim().replaceAll("[\\\\/:*?\"<>|]", "");
        if (nome.isEmpty()) return;
        File pasta = Pastas.projetos().resolve(nome).toFile();
        if (pasta.exists()) {
            JOptionPane.showMessageDialog(this, "Já existe um projeto com esse nome.");
            return;
        }
        pasta.mkdirs();

        File primeiro = null;
        ModelosProjeto.Modelo mo = escolhido instanceof ModelosProjeto.Modelo ? (ModelosProjeto.Modelo) escolhido : null;
        try {
            if (mo != null) {
                // Cria os arquivos do modelo e o .tazzcode com o comando do F5
                for (java.util.Map.Entry<String, String> arq : mo.arquivos.entrySet()) {
                    File f = new File(pasta, arq.getKey());
                    f.getParentFile().mkdirs();
                    escrever(f.getParentFile(), f.getName(), arq.getValue());
                }
                Projeto p = new Projeto();
                p.rodar = mo.rodar;
                p.url = mo.url;
                p.precisa = mo.precisa;
                p.modelo = mo.nome;
                p.salvar(pasta);
                if (!mo.abrir.isEmpty()) primeiro = new File(pasta, mo.abrir);
            } else {
                String tipo = escolhido.toString();
                switch (tipo) {
                    case "Java":
                        primeiro = escrever(pasta, "Main.java", Modelos.conteudo("Classe Java com main", "Main"));
                        break;
                    case "Python":
                        primeiro = escrever(pasta, "main.py", Modelos.conteudo("Python", "main"));
                        break;
                    case "Site (HTML + CSS + JS)":
                        primeiro = escrever(pasta, "index.html", Modelos.conteudo("HTML", nome));
                        escrever(pasta, "style.css", Modelos.conteudo("CSS", "style"));
                        escrever(pasta, "script.js", Modelos.conteudo("JavaScript", "script"));
                        break;
                    case "Vazio":
                        break;
                    default: {
                        // Qualquer outra linguagem do catálogo: cria "Main.ext" (ou "main.ext") com o Olá, mundo!
                        Linguagem lg = Catalogo.porNome(tipo);
                        String base = lg.modelo.contains("{nome}") ? "Main" : "main";
                        primeiro = escrever(pasta, base + "." + lg.extensaoPrincipal(), lg.modelo.replace("{nome}", base));
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Não consegui criar os arquivos: " + e.getMessage());
        }
        JanelaEditor j = abrirEditor(pasta, primeiro);
        if (mo != null && !mo.instalar.isEmpty()) {
            // Instala o que o modelo precisa (pip install, npm install...) uma vez só.
            j.rodarComando(mo.instalar, mo.precisa, null);
        }
    }

    private static File escrever(File pasta, String nome, String conteudo) throws IOException {
        File f = new File(pasta, nome);
        Files.write(f.toPath(), conteudo.replace("\n", System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        return f;
    }

    private void escolherPasta() {
        JFileChooser e = new JFileChooser(Config.texto("ultima.pasta", System.getProperty("user.home")));
        e.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        e.setDialogTitle("Escolha a pasta do projeto");
        if (e.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) abrirEditor(e.getSelectedFile(), null);
    }

    private void abrirArquivo() {
        JFileChooser e = new JFileChooser(Config.texto("ultima.pasta", System.getProperty("user.home")));
        if (e.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) abrirEditor(null, e.getSelectedFile());
    }

    private JanelaEditor abrirEditor(File projeto, File arquivo) {
        JanelaEditor j = new JanelaEditor(projeto);
        j.setVisible(true);
        if (arquivo != null && arquivo.exists()) j.abrirArquivo(arquivo);
        dispose();
        return j;
    }
}
