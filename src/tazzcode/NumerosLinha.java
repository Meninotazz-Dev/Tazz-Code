package tazzcode;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

/**
 * A coluna da esquerda que mostra 1, 2, 3... ao lado de cada linha.
 * Ela fica "colada" na área de texto (como cabeçalho de linha do JScrollPane).
 */
public class NumerosLinha extends JComponent {

    private final EditorCodigo texto;

    public NumerosLinha(EditorCodigo texto) {
        this.texto = texto;
        setOpaque(true);
        setBackground(Tema.FUNDO_PAINEL);

        // Sempre que o texto mudar, redesenha os números.
        texto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { atualizar(); }
            public void removeUpdate(DocumentEvent e)  { atualizar(); }
            public void changedUpdate(DocumentEvent e) { }
        });
        texto.addCaretListener(e -> repaint());
    }

    private int linhas() {
        return texto.getDocument().getDefaultRootElement().getElementCount();
    }

    private void atualizar() {
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        int digitos = String.valueOf(Math.max(linhas(), 99)).length();
        FontMetrics fm = getFontMetrics(texto.getFont());
        int largura = fm.charWidth('0') * digitos + 22;
        return new Dimension(largura, texto.getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle area = g.getClipBounds();
        g.setColor(getBackground());
        g.fillRect(area.x, area.y, area.width, area.height);

        g.setFont(texto.getFont());
        FontMetrics fm = g.getFontMetrics();
        Element raiz = texto.getDocument().getDefaultRootElement();
        int linhaCursor = texto.linhaDe(texto.getCaretPosition());

        // Descobre quais linhas aparecem na tela e desenha só elas.
        int primeira = raiz.getElementIndex(texto.viewToModel2D(new java.awt.Point(0, area.y)));
        int ultima = raiz.getElementIndex(texto.viewToModel2D(new java.awt.Point(0, area.y + area.height)));

        for (int i = primeira; i <= ultima; i++) {
            try {
                Rectangle2D r = texto.modelToView2D(raiz.getElement(i).getStartOffset());
                if (r == null) continue;
                String num = String.valueOf(i + 1);
                int x = getWidth() - fm.stringWidth(num) - 12;
                int y = (int) r.getY() + fm.getAscent();
                g.setColor(i == linhaCursor ? Tema.AZUL_CLARO : Tema.TEXTO_FRACO);
                g.drawString(num, x, y);
            } catch (Exception ignorado) { }
        }
    }
}
