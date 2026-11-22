package tazzcode;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.function.Consumer;

/**
 * Ícones desenhados em código (sem arquivos de imagem): eles ficam lisinhos
 * em qualquer tamanho e pegam a cor do texto do botão.
 *
 * Cada ícone é desenhado num quadrado de 16x16 "pontinhos".
 */
public class Icones {

    /** Um ícone que desenha com uma função. */
    private static Icon icone(Consumer<Graphics2D> desenho) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                Color cor = c == null ? Tema.TEXTO : c.getForeground();
                g2.setColor(cor);
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                desenho.accept(g2);
                g2.dispose();
            }
            @Override public int getIconWidth()  { return 16; }
            @Override public int getIconHeight() { return 16; }
        };
    }

    public static Icon rodar() {
        return icone(g -> {
            Path2D p = new Path2D.Float();
            p.moveTo(4.5, 2.5); p.lineTo(13, 8); p.lineTo(4.5, 13.5); p.closePath();
            g.fill(p);
        });
    }

    public static Icon parar() {
        return icone(g -> g.fillRoundRect(3, 3, 10, 10, 3, 3));
    }

    public static Icon novo() {
        return icone(g -> {
            g.drawRoundRect(3, 1, 10, 14, 3, 3);
            g.drawLine(8, 5, 8, 11);
            g.drawLine(5, 8, 11, 8);
        });
    }

    public static Icon abrir() {
        return icone(g -> {
            Path2D p = new Path2D.Float();
            p.moveTo(1.5, 4); p.lineTo(6, 4); p.lineTo(7.5, 5.5); p.lineTo(14.5, 5.5);
            p.lineTo(14.5, 13.5); p.lineTo(1.5, 13.5); p.closePath();
            g.draw(p);
        });
    }

    public static Icon salvar() {
        return icone(g -> {
            g.drawRoundRect(2, 2, 12, 12, 3, 3);
            g.drawRect(5, 2, 6, 4);
            g.fillRoundRect(5, 9, 6, 3, 1, 1);
        });
    }

    public static Icon terminal() {
        return icone(g -> {
            g.drawRoundRect(1, 2, 14, 12, 3, 3);
            g.drawPolyline(new int[]{4, 7, 4}, new int[]{6, 8, 10}, 3);
            g.drawLine(8, 11, 12, 11);
        });
    }

    public static Icon helper() {   // o "losango" do TazzHelper, com brilho
        return icone(g -> {
            Path2D p = new Path2D.Float();
            p.moveTo(8, 1); p.lineTo(10, 6); p.lineTo(15, 8); p.lineTo(10, 10);
            p.lineTo(8, 15); p.lineTo(6, 10); p.lineTo(1, 8); p.lineTo(6, 6); p.closePath();
            g.fill(p);
        });
    }

    public static Icon engrenagem() {
        return icone(g -> {
            for (int i = 0; i < 8; i++) {
                double a = Math.PI / 4 * i;
                g.drawLine((int) Math.round(8 + Math.cos(a) * 5.2), (int) Math.round(8 + Math.sin(a) * 5.2),
                        (int) Math.round(8 + Math.cos(a) * 7), (int) Math.round(8 + Math.sin(a) * 7));
            }
            g.drawOval(3, 3, 10, 10);
            g.drawOval(6, 6, 4, 4);
        });
    }

    public static Icon pasta() {
        return abrir();
    }

    public static Icon voltar() {
        return icone(g -> {
            g.drawPolyline(new int[]{7, 3, 7}, new int[]{4, 8, 12}, 3);
            g.drawLine(3, 8, 13, 8);
        });
    }
}
