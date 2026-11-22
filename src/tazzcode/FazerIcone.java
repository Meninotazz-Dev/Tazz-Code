package tazzcode;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Transforma o ícone (recursos/icone.png) num arquivo .ico, que é o formato de ícone do Windows.
 * O Empacotar.bat usa isso pra colocar o seu ícone no TazzCode.exe e no instalador.
 *
 *   java -cp out tazzcode.FazerIcone recursos\icone.png dist\icone.ico
 *
 * Um .ico é uma "caixinha" com várias imagens de tamanhos diferentes (16, 32, 48, 256).
 * Desde o Windows Vista, cada imagem pode ser um PNG normal dentro da caixinha.
 */
public class FazerIcone {

    public static void main(String[] args) throws IOException {
        File png = new File(args.length > 0 ? args[0] : "recursos/icone.png");
        File ico = new File(args.length > 1 ? args[1] : "dist/icone.ico");
        Image original = png.isFile() ? ImageIO.read(png) : Icone.carregar();
        if (original == null) original = Icone.carregar();

        int[] tamanhos = {16, 24, 32, 48, 64, 256};
        byte[][] imagens = new byte[tamanhos.length][];
        for (int i = 0; i < tamanhos.length; i++) imagens[i] = pngDoTamanho(original, tamanhos[i]);

        // Cabeçalho (6 bytes) + uma "ficha" de 16 bytes pra cada imagem + as imagens
        int deslocamento = 6 + 16 * tamanhos.length;
        ByteBuffer cab = ByteBuffer.allocate(deslocamento).order(ByteOrder.LITTLE_ENDIAN);
        cab.putShort((short) 0).putShort((short) 1).putShort((short) tamanhos.length);   // 1 = ícone
        for (int i = 0; i < tamanhos.length; i++) {
            int t = tamanhos[i];
            cab.put((byte) (t >= 256 ? 0 : t)).put((byte) (t >= 256 ? 0 : t));   // 0 quer dizer 256
            cab.put((byte) 0).put((byte) 0);                                        // paleta, reservado
            cab.putShort((short) 1).putShort((short) 32);                           // planos, bits por cor
            cab.putInt(imagens[i].length).putInt(deslocamento);
            deslocamento += imagens[i].length;
        }
        if (ico.getParentFile() != null) ico.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(ico)) {
            out.write(cab.array());
            for (byte[] b : imagens) out.write(b);
        }
        System.out.println("Ícone criado: " + ico.getPath() + (png.isFile() ? " (a partir de " + png.getPath() + ")" : " (ícone padrão)"));
    }

    private static byte[] pngDoTamanho(Image img, int t) throws IOException {
        BufferedImage b = new BufferedImage(t, t, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = b.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(img, 0, 0, t, t, null);
        g.dispose();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(b, "png", bytes);
        return bytes.toByteArray();
    }
}
