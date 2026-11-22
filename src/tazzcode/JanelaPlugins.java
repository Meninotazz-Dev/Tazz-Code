package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;

/**
 * A JANELA DE PLUGINS: lista as linguagens portáteis que dá pra baixar, mostra quais já
 * estão instaladas e tem os botões Instalar / Remover / Tutorial.
 */
public class JanelaPlugins extends JDialog {

    private final JList<Plugins.Plugin> lista = new JList<>(Plugins.todos().toArray(new Plugins.Plugin[0]));
    private final JLabel detalhes = new JLabel();
    private final JButton instalar = Tema.botao("Instalar", Tema.AZUL);
    private final JButton remover = Tema.botao("Remover", new Color(0x8A2F3B));
    private final JButton abrirPasta = Tema.botao("Abrir pasta", Tema.LINHA_ATUAL);
    private final JButton cancelar = Tema.botao("Cancelar download", new Color(0x8A2F3B));
    private final JProgressBar barra = new JProgressBar(0, 100);
    private final JLabel status = new JLabel(" ");
    private volatile boolean cancelado;
    private volatile boolean trabalhando;

    public JanelaPlugins(JFrame dona) {
        super(dona, "Plugins do " + App.NOME, false);
        setSize(820, 520);
        setLocationRelativeTo(dona);

        JPanel tudo = new JPanel(new BorderLayout());
        tudo.setBackground(Tema.FUNDO);

        JLabel topo = new JLabel("<html><b>Plugins = linguagens portáteis.</b> O TazzCode baixa e guarda na pasta dele: "
                + "roda SEM instalar nada no Windows. Se a linguagem já estiver instalada no PC, o TazzCode usa a do PC.</html>");
        topo.setForeground(Tema.TEXTO);
        topo.setFont(Tema.FONTE_UI);
        topo.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        tudo.add(topo, BorderLayout.NORTH);

        lista.setBackground(Tema.FUNDO_PAINEL);
        lista.setSelectionBackground(Tema.SELECAO);
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foco) {
                Plugins.Plugin p = (Plugins.Plugin) v;
                String estado = p.instalado() ? "<font color='#7CFFB2'>instalado</font>"
                        : algumNoPc(p) ? "<font color='#7CC4FF'>já tem no PC</font>" : "<font color='#7D8BA6'>" + p.tamanho + "</font>";
                JLabel c = (JLabel) super.getListCellRendererComponent(l, "<html><font color='#E8F1FF'><b>" + p.nome + "</b></font><br>" + estado + "</html>", i, sel, false);
                c.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                c.setBackground(sel ? Tema.SELECAO : Tema.FUNDO_PAINEL);
                return c;
            }
        });
        lista.addListSelectionListener(e -> atualizar());
        JScrollPane rol = new JScrollPane(lista);
        rol.setPreferredSize(new java.awt.Dimension(260, 0));
        rol.setBorder(BorderFactory.createEmptyBorder());
        tudo.add(rol, BorderLayout.WEST);

        JPanel direita = new JPanel(new BorderLayout());
        direita.setBackground(Tema.FUNDO);
        direita.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        detalhes.setForeground(Tema.TEXTO);
        detalhes.setFont(Tema.FONTE_UI.deriveFont(14f));
        detalhes.setVerticalAlignment(JLabel.TOP);
        direita.add(detalhes, BorderLayout.CENTER);

        JPanel baixo = new JPanel(new BorderLayout(0, 6));
        baixo.setOpaque(false);
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botoes.setOpaque(false);
        JButton tutorial = Tema.botao("Tutorial (instalar no PC)", Tema.LINHA_ATUAL);
        botoes.add(instalar);
        botoes.add(remover);
        botoes.add(abrirPasta);
        botoes.add(tutorial);
        botoes.add(cancelar);
        cancelar.setVisible(false);
        baixo.add(botoes, BorderLayout.NORTH);
        barra.setStringPainted(true);
        barra.setVisible(false);
        baixo.add(barra, BorderLayout.CENTER);
        status.setForeground(Tema.TEXTO_FRACO);
        status.setFont(Tema.FONTE_UI);
        baixo.add(status, BorderLayout.SOUTH);
        direita.add(baixo, BorderLayout.SOUTH);
        tudo.add(direita, BorderLayout.CENTER);
        setContentPane(tudo);

        instalar.addActionListener(e -> instalar(lista.getSelectedValue()));
        remover.addActionListener(e -> {
            Plugins.Plugin p = lista.getSelectedValue();
            if (p != null && JOptionPane.showConfirmDialog(this, "Apagar o plugin " + p.nome + "?", "Remover", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                Plugins.remover(p);
                atualizar();
                lista.repaint();
            }
        });
        abrirPasta.addActionListener(e -> {
            Plugins.Plugin p = lista.getSelectedValue();
            try { if (p != null) Desktop.getDesktop().open(p.pasta()); } catch (Exception ignorado) { }
        });
        tutorial.addActionListener(e -> {
            Plugins.Plugin p = lista.getSelectedValue();
            if (p == null) return;
            Linguagem l = Catalogo.porNome(p.linguagens.get(0));
            JFrame f = (JFrame) getOwner();
            if (f instanceof JanelaEditor) ((JanelaEditor) f).abrirTutorial(l);
        });
        cancelar.addActionListener(e -> cancelado = true);
        lista.setSelectedIndex(0);
    }

    private static boolean algumNoPc(Plugins.Plugin p) {
        return Executaveis.noPc(p.comandos.get(0));
    }

    private void atualizar() {
        Plugins.Plugin p = lista.getSelectedValue();
        if (p == null) return;
        StringBuilder sb = new StringBuilder("<html><div style='width:470px'><font size='+1' color='#7CC4FF'><b>" + p.nome + "</b></font><br><br>");
        sb.append(p.descricao).append("<br><br>");
        sb.append("<b>Tamanho do download:</b> ").append(p.tamanho).append("<br>");
        sb.append("<b>Faz rodar:</b> ").append(String.join(", ", p.linguagens)).append("<br>");
        sb.append("<b>Programas:</b> ").append(String.join(", ", p.comandos)).append("<br><br>");
        if (!p.depende.isEmpty()) {
            Plugins.Plugin d = Plugins.porId(p.depende);
            sb.append("<font color='#E5C07B'>Precisa também de: ").append(d.nome).append(" (se não tiver no PC).</font><br><br>");
        }
        if (p.instalado()) sb.append("<font color='#7CFFB2'><b>Instalado</b> em ").append(p.pasta()).append("</font>");
        else if (algumNoPc(p)) sb.append("<font color='#7CC4FF'><b>Você já tem isso instalado no PC</b>, então não precisa do plugin (mas pode baixar mesmo assim).</font>");
        else sb.append("Não instalado. Precisa de internet pra baixar.");
        detalhes.setText(sb.append("</div></html>").toString());
        instalar.setEnabled(!trabalhando);
        instalar.setText(p.instalado() ? "Reinstalar (atualizar)" : "Instalar");
        remover.setEnabled(p.instalado() && !trabalhando);
        abrirPasta.setEnabled(p.instalado());
    }

    /** Baixa e instala, mostrando a barrinha de progresso. */
    public void instalar(Plugins.Plugin p) {
        if (p == null || trabalhando) return;
        lista.setSelectedValue(p, true);
        trabalhando = true;
        cancelado = false;
        barra.setVisible(true);
        barra.setIndeterminate(true);
        cancelar.setVisible(true);
        atualizar();
        new Thread(() -> {
            String erro = null;
            try {
                Plugins.Plugin dep = p.depende.isEmpty() ? null : Plugins.porId(p.depende);
                if (dep != null && !dep.instalado() && !algumNoPc(dep)) Plugins.instalar(dep, progresso());
                Plugins.instalar(p, progresso());
            } catch (Exception e) {
                erro = cancelado ? "Cancelado." : e.getMessage();
            }
            String fim = erro;
            SwingUtilities.invokeLater(() -> {
                trabalhando = false;
                cancelar.setVisible(false);
                barra.setVisible(false);
                if (fim == null) {
                    status.setForeground(Tema.SUCESSO);
                    status.setText(p.nome + " instalado! Pode apertar F5.");
                } else {
                    status.setForeground(Tema.ERRO);
                    status.setText("<html>Não deu: " + fim + "<br>Sem internet? Tente de novo, ou use o Tutorial pra instalar no PC.</html>");
                }
                atualizar();
                lista.repaint();
            });
        }).start();
    }

    private Plugins.Progresso progresso() {
        return new Plugins.Progresso() {
            public void mostrar(String texto, int porcento) {
                SwingUtilities.invokeLater(() -> {
                    status.setForeground(Tema.TEXTO_FRACO);
                    status.setText(texto);
                    barra.setIndeterminate(porcento < 0);
                    if (porcento >= 0) barra.setValue(porcento);
                });
            }
            public boolean cancelado() { return cancelado; }
        };
    }
}
