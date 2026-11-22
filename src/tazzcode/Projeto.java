package tazzcode;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

/**
 * As configurações de UM PROJETO, guardadas no arquivo ".tazzcode" dentro da pasta dele.
 *   rodar   = o comando do F5 (ex.: python app.py)
 *   url     = endereço pra abrir no navegador depois de rodar (servidores)
 *   precisa = o que precisa instalar
 *   modelo  = de qual modelo o projeto foi criado
 * Se o projeto não tiver esse arquivo, o F5 roda só o arquivo aberto.
 */
public class Projeto {

    public static final String ARQUIVO = ".tazzcode";

    public String rodar = "", url = "", precisa = "", modelo = "";

    public static Projeto ler(File pasta) {
        if (pasta == null) return null;
        File f = new File(pasta, ARQUIVO);
        if (!f.isFile()) return null;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (Exception e) {
            return null;
        }
        Projeto pr = new Projeto();
        pr.rodar = p.getProperty("rodar", "");
        pr.url = p.getProperty("url", "");
        pr.precisa = p.getProperty("precisa", "");
        pr.modelo = p.getProperty("modelo", "");
        return pr.rodar.isEmpty() ? null : pr;
    }

    public void salvar(File pasta) throws java.io.IOException {
        Properties p = new Properties();
        p.setProperty("rodar", rodar);
        p.setProperty("url", url);
        p.setProperty("precisa", precisa);
        p.setProperty("modelo", modelo);
        try (Writer w = Files.newBufferedWriter(new File(pasta, ARQUIVO).toPath(), StandardCharsets.UTF_8)) {
            p.store(w, "Projeto do TazzCode - \"rodar\" e o comando do F5");
        }
    }
}
