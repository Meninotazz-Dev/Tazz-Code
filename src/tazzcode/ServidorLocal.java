package tazzcode;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Um SERVIDOR WEB pequenininho, feito com o servidor que já vem no Java.
 * Ele mostra os arquivos de uma pasta no endereço http://localhost:porta/
 *
 * Por que precisa? Alguns códigos (React com JSX, "import" do JavaScript...) não funcionam
 * quando você abre o arquivo direto (file://). Pelo http:// eles funcionam.
 */
public class ServidorLocal {

    private static final Map<String, Integer> PORTAS = new HashMap<>();   // pasta -> porta
    private static final Map<String, HttpServer> SERVIDORES = new HashMap<>();

    /** Liga (se ainda não estiver ligado) e devolve o endereço do arquivo. */
    public static synchronized String endereco(File pasta, String arquivo) throws IOException {
        String chave = pasta.getAbsolutePath();
        if (!SERVIDORES.containsKey(chave)) {
            HttpServer s = null;
            int porta = 5500;
            while (s == null) {
                try {
                    s = HttpServer.create(new InetSocketAddress("127.0.0.1", porta), 0);
                } catch (IOException ocupada) {
                    porta++;
                    if (porta > 5600) throw ocupada;
                }
            }
            File raiz = pasta.getCanonicalFile();
            s.createContext("/", troca -> {
                String caminho = URLDecoder.decode(troca.getRequestURI().getPath(), StandardCharsets.UTF_8);
                File f = new File(raiz, caminho).getCanonicalFile();
                if (f.isDirectory()) f = new File(f, "index.html");
                byte[] corpo;
                int codigo = 200;
                if (!f.getPath().startsWith(raiz.getPath()) || !f.isFile()) {   // não deixa sair da pasta
                    corpo = ("Não achei: " + caminho).getBytes(StandardCharsets.UTF_8);
                    codigo = 404;
                } else {
                    corpo = Files.readAllBytes(f.toPath());
                    troca.getResponseHeaders().set("Content-Type", tipo(f.getName()));
                }
                troca.getResponseHeaders().set("Cache-Control", "no-store");   // sempre a versão mais nova
                troca.sendResponseHeaders(codigo, corpo.length);
                try (OutputStream out = troca.getResponseBody()) {
                    out.write(corpo);
                }
            });
            s.start();
            SERVIDORES.put(chave, s);
            PORTAS.put(chave, porta);
        }
        return "http://localhost:" + PORTAS.get(chave) + "/" + arquivo.replace('\\', '/');
    }

    private static String tipo(String nome) {
        String n = nome.toLowerCase();
        if (n.endsWith(".html") || n.endsWith(".htm")) return "text/html; charset=utf-8";
        if (n.endsWith(".css")) return "text/css; charset=utf-8";
        if (n.endsWith(".js") || n.endsWith(".mjs") || n.endsWith(".jsx")) return "text/javascript; charset=utf-8";
        if (n.endsWith(".json")) return "application/json; charset=utf-8";
        if (n.endsWith(".svg")) return "image/svg+xml";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".gif")) return "image/gif";
        if (n.endsWith(".wasm")) return "application/wasm";
        return "text/plain; charset=utf-8";
    }
}
