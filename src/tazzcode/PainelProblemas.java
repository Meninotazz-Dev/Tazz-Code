package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * A aba "PROBLEMAS" (ao lado do Console): a lista de erros do arquivo aberto.
 * Dois cliques num problema levam até ele. "Corrigir" arruma sozinho (quando dá).
 */
public class PainelProblemas extends JPanel {

    private final JList<VerificadorHtml.Problema> lista = new JList<>();
    private final JLabel resumo = new JLabel();
    private final JButton corrigir = Tema.botao("Corrigir esse", Tema.AZUL);
    private final JButton corrigirTudo = Tema.botao("Corrigir tudo", Tema.LINHA_ATUAL);
    private EditorCodigo editor;
    private Runnable aoMudarQuantidade = () -> { };

    public PainelProblemas() {
        super(new BorderLayout());
        setBackground(Tema.FUNDO_CONSOLE);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Tema.FUNDO_PAINEL);
        topo.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 6));
        resumo.setForeground(Tema.TEXTO_FRACO);
        resumo.setFont(Tema.FONTE_UI);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botoes.setOpaque(false);
        botoes.add(corrigir);
        botoes.add(corrigirTudo);
        topo.add(resumo, BorderLayout.WEST);
        topo.add(botoes, BorderLayout.EAST);
        add(topo, BorderLayout.NORTH);

        lista.setBackground(Tema.FUNDO_CONSOLE);
        lista.setSelectionBackground(Tema.SELECAO);
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                VerificadorHtml.Problema p = (VerificadorHtml.Problema) v;
                String cor = p.gravidade == VerificadorHtml.Gravidade.ERRO ? "#FF7A7A"
                        : p.gravidade == VerificadorHtml.Gravidade.AVISO ? "#E5C07B" : "#7CC4FF";
                String tipo = p.gravidade == VerificadorHtml.Gravidade.ERRO ? "ERRO"
                        : p.gravidade == VerificadorHtml.Gravidade.AVISO ? "AVISO" : "DICA";
                int linha = editor == null ? 0 : editor.linhaDe(Math.min(p.inicio, editor.getDocument().getLength())) + 1;
                String html = "<html><font color='" + cor + "'><b>" + tipo + "</b></font>&nbsp; <font color='#7D8BA6'>linha "
                        + linha + "</font>&nbsp; <font color='#E8F1FF'>" + escapar(p.mensagem) + "</font>"
                        + (p.temCorrecao() ? " <font color='#7CFFB2'>[pode corrigir]</font>" : "") + "</html>";
                JLabel c = (JLabel) super.getListCellRendererComponent(l, html, i, sel, false);
                c.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
                c.setFont(Tema.FONTE_UI);
                c.setBackground(sel ? Tema.SELECAO : Tema.FUNDO_CONSOLE);
                return c;
            }
        });
        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) irPara(lista.getSelectedValue());
            }
        });
        JScrollPane rol = new JScrollPane(lista);
        rol.setBorder(BorderFactory.createEmptyBorder());
        add(rol, BorderLayout.CENTER);

        corrigir.addActionListener(e -> {
            VerificadorHtml.Problema p = lista.getSelectedValue();
            if (p != null && editor != null) editor.corrigir(p);
        });
        corrigirTudo.addActionListener(e -> {
            if (editor != null) editor.corrigirTudo();
        });
        atualizar();
    }

    /** Mostra os problemas desse editor (troca quando você muda de aba). */
    public void mostrar(EditorCodigo e) {
        editor = e;
        atualizar();
    }

    public void setAoMudarQuantidade(Runnable r) { aoMudarQuantidade = r; }

    public int quantidade() {
        if (editor == null) return 0;
        int n = 0;
        for (VerificadorHtml.Problema p : editor.getProblemas()) if (p.gravidade != VerificadorHtml.Gravidade.DICA) n++;
        return n;
    }

    public void atualizar() {
        List<VerificadorHtml.Problema> ps = editor == null ? java.util.Collections.emptyList() : editor.getProblemas();
        lista.setListData(ps.toArray(new VerificadorHtml.Problema[0]));
        long erros = ps.stream().filter(p -> p.gravidade == VerificadorHtml.Gravidade.ERRO).count();
        long avisos = ps.stream().filter(p -> p.gravidade == VerificadorHtml.Gravidade.AVISO).count();
        boolean html = editor != null && editor.getLinguagem().estilo == Linguagem.Estilo.HTML;
        resumo.setText(!html ? "O verificador funciona em arquivos HTML."
                : ps.isEmpty() ? "Nenhum problema! Seu HTML está certinho."
                : erros + " erro(s), " + avisos + " aviso(s). Passe o mouse na ondinha ou aperte Alt+Enter em cima dela.");
        corrigirTudo.setEnabled(ps.stream().anyMatch(VerificadorHtml.Problema::temCorrecao));
        aoMudarQuantidade.run();
    }

    private void irPara(VerificadorHtml.Problema p) {
        if (p == null || editor == null) return;
        int pos = Math.min(p.inicio, editor.getDocument().getLength());
        editor.requestFocusInWindow();
        editor.select(pos, Math.min(p.fim, editor.getDocument().getLength()));
    }

    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
