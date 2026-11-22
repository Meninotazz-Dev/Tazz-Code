package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * O visual das caixas de escolher (JComboBox): arredondada, sem o "botão" antigo,
 * com uma setinha desenhada. O Tema coloca ele como padrão no Swing.
 */
public class CaixaEscolha extends BasicComboBoxUI {

    public static ComponentUI createUI(JComponent c) {
        return new CaixaEscolha();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.setOpaque(false);
        c.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 4));
        comboBox.setBackground(Tema.LINHA_ATUAL);
        comboBox.setForeground(Tema.TEXTO);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int i, boolean sel, boolean foco) {
                super.getListCellRendererComponent(lista, valor, i, sel, foco);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                setOpaque(i >= 0);   // na caixa fechada fica transparente (o fundo é o arredondado)
                setBackground(sel ? Tema.SELECAO : Tema.FUNDO);
                setForeground(Tema.TEXTO);
                return this;
            }
        });
    }

    @Override
    protected JButton createArrowButton() {
        JButton b = new JButton() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Tema.TEXTO_FRACO);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.drawPolyline(new int[]{cx - 4, cx, cx + 4}, new int[]{cy - 2, cy + 2, cy - 2}, 3);
                g2.dispose();
            }
        };
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setContentAreaFilled(false);
        b.setFocusable(false);
        return b;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(comboBox.getBackground());
        g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
        g2.setColor(comboBox.hasFocus() ? Tema.AZUL : Tema.BORDA);
        g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);
        g2.dispose();
        super.paint(g, c);
    }

    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean foco) {
        // nada: o fundo arredondado já foi pintado
    }
}
