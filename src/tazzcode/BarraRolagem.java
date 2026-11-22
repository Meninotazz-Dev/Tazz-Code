package tazzcode;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Barra de rolagem fininha e escura, no estilo do editor
 * (sem as setinhas nas pontas, igual ao IntelliJ e ao VS Code).
 */
public class BarraRolagem extends BasicScrollBarUI {

    /** O Swing chama isso pra criar a barra (por isso tem que ser "public static"). */
    public static ComponentUI createUI(JComponent c) {
        return new BarraRolagem();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return semBotao();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return semBotao();
    }

    private JButton semBotao() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        g.setColor(Tema.FUNDO);
        g.fillRect(r.x, r.y, r.width, r.height);
    }

    @Override
    protected void paintThumb(Graphics g0, JComponent c, Rectangle r) {
        if (r.isEmpty()) return;
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(isThumbRollover() ? Tema.SELECAO : Tema.LINHA_ATUAL.brighter());
        g.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
        g.dispose();
    }
}
