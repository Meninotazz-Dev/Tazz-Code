package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * O PAINEL DO PROJETO (a "árvore" da esquerda, igual à do IntelliJ).
 * Mostra as pastas e arquivos do projeto. Dois cliques abrem o arquivo,
 * e o botão direito abre o menu (novo arquivo, nova pasta, renomear, excluir...).
 */
public class PainelProjeto extends JPanel {

    /** Pastas que não aparecem na árvore (são coisas internas). */
    private static final Set<String> ESCONDIDAS = new HashSet<>(Arrays.asList(
            ".git", ".idea", "out", "node_modules", "__pycache__", ".vscode", Projeto.ARQUIVO, "target", ".venv"));

    /** O que o painel pede pra janela fazer. */
    public interface Acoes {
        void abrir(File arquivo);
        void novoArquivo(File pasta);
        void novaPasta(File pasta);
        void renomear(File arquivo);
        void excluir(File arquivo);
        void abrirNoExplorador(File arquivo);
    }

    private final JTree arvore = new JTree();
    private final JLabel titulo = new JLabel();
    private File raiz;
    private final Acoes acoes;

    public PainelProjeto(Acoes acoes) {
        super(new BorderLayout());
        this.acoes = acoes;
        setBackground(Tema.FUNDO_PAINEL);

        titulo.setForeground(Tema.AZUL_CLARO);
        titulo.setFont(Tema.FONTE_UI.deriveFont(Font.BOLD));
        titulo.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        add(titulo, BorderLayout.NORTH);

        arvore.setBackground(Tema.FUNDO_PAINEL);
        arvore.setRootVisible(true);
        arvore.setShowsRootHandles(true);
        arvore.setRowHeight(22);
        arvore.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        arvore.setCellRenderer(new Desenhista());

        arvore.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    File f = arquivoNaPosicao(e);
                    if (f != null && f.isFile()) acoes.abrir(f);
                }
            }
            @Override
            public void mousePressed(MouseEvent e)  { if (e.isPopupTrigger()) menu(e); }
            @Override
            public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) menu(e); }
        });

        JScrollPane rolagem = new JScrollPane(arvore);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        add(rolagem, BorderLayout.CENTER);
    }

    // =====================================================================
    //  montar a árvore
    // =====================================================================

    public void setRaiz(File pasta) {
        this.raiz = pasta;
        titulo.setText("PROJETO: " + pasta.getName());
        atualizar();
    }

    public File getRaiz() {
        return raiz;
    }

    /** Lê as pastas de novo, mantendo abertas as que estavam abertas. */
    public void atualizar() {
        if (raiz == null) return;
        Set<String> abertas = new HashSet<>();
        Enumeration<TreePath> e = arvore.getExpandedDescendants(new TreePath(arvore.getModel().getRoot()));
        if (e != null) {
            while (e.hasMoreElements()) {
                Object no = ((DefaultMutableTreeNode) e.nextElement().getLastPathComponent()).getUserObject();
                if (no instanceof Item) abertas.add(((Item) no).arquivo.getAbsolutePath());
            }
        }

        DefaultMutableTreeNode noRaiz = montar(raiz);
        arvore.setModel(new DefaultTreeModel(noRaiz));

        arvore.expandRow(0);
        for (int i = 0; i < arvore.getRowCount(); i++) {
            Object no = ((DefaultMutableTreeNode) arvore.getPathForRow(i).getLastPathComponent()).getUserObject();
            if (no instanceof Item && abertas.contains(((Item) no).arquivo.getAbsolutePath())) arvore.expandRow(i);
        }
    }

    private DefaultMutableTreeNode montar(File pasta) {
        DefaultMutableTreeNode no = new DefaultMutableTreeNode(new Item(pasta));
        File[] filhos = pasta.listFiles();
        if (filhos == null) return no;

        List<File> pastas = new ArrayList<>();
        List<File> arquivos = new ArrayList<>();
        for (File f : filhos) {
            if (f.isHidden() || ESCONDIDAS.contains(f.getName())) continue;
            (f.isDirectory() ? pastas : arquivos).add(f);
        }
        pastas.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        arquivos.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (File p : pastas) no.add(montar(p));
        for (File a : arquivos) no.add(new DefaultMutableTreeNode(new Item(a)));
        return no;
    }

    private File arquivoNaPosicao(MouseEvent e) {
        TreePath caminho = arvore.getPathForLocation(e.getX(), e.getY());
        if (caminho == null) return null;
        arvore.setSelectionPath(caminho);
        Object no = ((DefaultMutableTreeNode) caminho.getLastPathComponent()).getUserObject();
        return no instanceof Item ? ((Item) no).arquivo : null;
    }

    // =====================================================================
    //  menu do botão direito
    // =====================================================================

    private void menu(MouseEvent e) {
        File f = arquivoNaPosicao(e);
        if (f == null) f = raiz;
        File alvo = f;
        File pasta = f.isDirectory() ? f : f.getParentFile();

        JPopupMenu m = new JPopupMenu();
        if (alvo.isFile()) m.add(item("Abrir", () -> acoes.abrir(alvo)));
        m.add(item("Novo arquivo...", () -> acoes.novoArquivo(pasta)));
        m.add(item("Nova pasta...", () -> acoes.novaPasta(pasta)));
        m.addSeparator();
        if (!alvo.equals(raiz)) {
            m.add(item("Renomear...", () -> acoes.renomear(alvo)));
            m.add(item("Excluir", () -> acoes.excluir(alvo)));
            m.addSeparator();
        }
        m.add(item("Mostrar no Explorador de Arquivos", () -> acoes.abrirNoExplorador(alvo)));
        m.add(item("Atualizar", this::atualizar));
        m.show(arvore, e.getX(), e.getY());
    }

    private JMenuItem item(String nome, Runnable r) {
        JMenuItem i = new JMenuItem(nome);
        i.addActionListener(ev -> r.run());
        return i;
    }

    // =====================================================================
    //  como cada item aparece (nome + iconezinho colorido)
    // =====================================================================

    /** Um arquivo ou pasta na árvore (mostra só o nome, não o caminho inteiro). */
    static class Item {
        final File arquivo;
        Item(File arquivo) { this.arquivo = arquivo; }
        @Override public String toString() { return arquivo.getName(); }
    }

    private class Desenhista extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree t, Object valor, boolean sel, boolean aberto,
                                                      boolean folha, int linha, boolean foco) {
            super.getTreeCellRendererComponent(t, valor, sel, aberto, folha, linha, foco);
            setForeground(Tema.TEXTO);
            setBackgroundNonSelectionColor(Tema.FUNDO_PAINEL);
            setBackgroundSelectionColor(Tema.SELECAO);
            setBorderSelectionColor(Tema.SELECAO);
            setFont(Tema.FONTE_UI);
            Object no = ((DefaultMutableTreeNode) valor).getUserObject();
            if (no instanceof Item) setIcon(iconePara(((Item) no).arquivo));
            return this;
        }
    }

    /** Um quadradinho colorido com uma letra, tipo um ícone simples. */
    private static Icon iconePara(File f) {
        if (f.isDirectory()) return new IconeLetra("", new Color(0x7CC4FF), true);
        Linguagem l = Catalogo.porArquivo(f);
        switch (l.nome) {
            case "Java":       return new IconeLetra("J", new Color(0xE76F00), false);
            case "Python":     return new IconeLetra("P", new Color(0x3776AB), false);
            case "HTML":       return new IconeLetra("H", new Color(0xE34F26), false);
            case "CSS":        return new IconeLetra("C", new Color(0x2965F1), false);
            case "JavaScript": return new IconeLetra("J", new Color(0xC9A900), false);
            case "TypeScript": return new IconeLetra("T", new Color(0x3178C6), false);
            case "SQL":        return new IconeLetra("S", new Color(0x00758F), false);
            case "Texto":      return new IconeLetra("", new Color(0x4D5A70), false);
            default: {
                // As outras: a 1ª letra do nome, com uma cor que depende do nome.
                float matiz = (Math.abs(l.nome.hashCode()) % 360) / 360f;
                String letra = l.nome.substring(0, 1).toUpperCase();
                if (!Character.isLetter(letra.charAt(0))) letra = "#";
                return new IconeLetra(letra, Color.getHSBColor(matiz, 0.55f, 0.75f), false);
            }
        }
    }

    private static class IconeLetra implements Icon {
        private final String letra;
        private final Color cor;
        private final boolean pasta;

        IconeLetra(String letra, Color cor, boolean pasta) {
            this.letra = letra;
            this.cor = cor;
            this.pasta = pasta;
        }

        public int getIconWidth()  { return 16; }
        public int getIconHeight() { return 16; }

        public void paintIcon(Component c, Graphics g0, int x, int y) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(cor);
            if (pasta) {
                g.fillRoundRect(x, y + 2, 7, 4, 2, 2);        // a "orelhinha" da pasta
                g.fillRoundRect(x, y + 4, 15, 10, 3, 3);
            } else {
                g.fillRoundRect(x + 1, y + 1, 14, 14, 4, 4);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 10));
                int w = g.getFontMetrics().stringWidth(letra);
                g.drawString(letra, x + 8 - w / 2, y + 12);
            }
            g.dispose();
        }
    }

    /** Usado pela janela pra saber se um arquivo está dentro do projeto. */
    public boolean contem(File f) {
        return raiz != null && f != null && f.getAbsolutePath().startsWith(raiz.getAbsolutePath());
    }
}
