package tazzcode;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * Carrega o ícone do programa.
 *
 * QUER COLOCAR O SEU ÍCONE? Salve uma imagem PNG (quadrada, tipo 256x256)
 * com o nome  recursos/icone.png  e compile de novo. Pronto!
 * Se não tiver imagem, o editor desenha um ícone simples sozinho.
 */
public class Icone {

    public static Image carregar() {
        // 1) Tenta pegar de dentro do .jar
        try (InputStream in = Icone.class.getResourceAsStream("/icone.png")) {
            if (in != null) return ImageIO.read(in);
        } catch (Exception ignorado) { }

        // 2) Tenta pegar da pasta recursos (quando roda pelo .bat)
        try {
            File f = new File("recursos/icone.png");
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception ignorado) { }

        // 3) Desenha um ícone provisório: "</>" num quadrado azul
        return desenharPadrao();
    }

    private static Image desenharPadrao() {
        int t = 64;
        BufferedImage img = new BufferedImage(t, t, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Tema.AZUL);
        g.fillRoundRect(2, 2, t - 4, t - 4, 16, 16);
        g.setColor(Tema.AZUL_CLARO);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(2, 2, t - 5, t - 5, 16, 16);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        String s = "</>";
        int w = g.getFontMetrics().stringWidth(s);
        g.drawString(s, (t - w) / 2, 40);
        g.dispose();
        return img;
    }
}
