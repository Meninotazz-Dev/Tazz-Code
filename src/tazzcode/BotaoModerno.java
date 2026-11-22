package tazzcode;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Um botão moderno: cantos arredondados, sem a "moldura" antiga do Java,
 * fica mais claro quando o mouse passa em cima e mais escuro quando aperta.
 */
public class BotaoModerno extends JButton {

    private Color cor;

    public BotaoModerno(String texto, Icon icone, Color cor) {
        super(texto, icone);
        this.cor = cor;
        setUI(new BasicButtonUI());
        setFont(Tema.FONTE_UI.deriveFont(Font.BOLD));
        setForeground(Tema.textoSobre(cor));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setIconTextGap(7);
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void setBackground(Color c) {   // quem chamava setBackground continua funcionando
        super.setBackground(c);
        if (c != null && !(c instanceof javax.swing.plaf.UIResource)) {   // ignora a cor "padrão" que o Swing tenta colocar
            cor = c;
            setForeground(Tema.textoSobre(c));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        pintarFundo(g, this, cor, 10);
        super.paintComponent(g);
    }

    /** Pinta o fundo arredondado de qualquer botão (usado aqui e no UIPadrao). */
    static void pintarFundo(Graphics g, AbstractButton b, Color cor, int raio) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ButtonModel m = b.getModel();
        Color c = cor == null ? Tema.LINHA_ATUAL : cor;
        if (!b.isEnabled()) c = Tema.realce(c, -0.25f);
        else if (m.isPressed()) c = Tema.realce(c, -0.18f);
        else if (m.isRollover()) c = Tema.realce(c, 0.12f);
        g2.setColor(c);
        g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), raio, raio);
        if (b.isFocusOwner() && b.isFocusable()) {
            g2.setColor(Tema.AZUL_CLARO);
            g2.drawRoundRect(1, 1, b.getWidth() - 3, b.getHeight() - 3, raio, raio);
        }
        g2.dispose();
    }

    /**
     * O visual de TODOS os outros botões do programa (os das caixas de diálogo,
     * por exemplo). O Tema coloca ele como padrão no Swing.
     */
    public static class UIPadrao extends BasicButtonUI {
        public static ComponentUI createUI(JComponent c) {
            return new UIPadrao();
        }

        @Override
        public void installDefaults(AbstractButton b) {
            super.installDefaults(b);
            b.setOpaque(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setRolloverEnabled(true);
            b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
            b.setForeground(Tema.TEXTO);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            if (b.isContentAreaFilled()) pintarFundo(g, b, b.getBackground(), 10);
            super.paint(g, c);
        }
    }
}
