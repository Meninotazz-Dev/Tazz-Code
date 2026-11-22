package tazzcode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Conversa com o Claude pela internet (API da Anthropic).
 *
 * Como funciona:
 *   1) monta um JSON com a conversa (as mensagens) e as instruções (o "system")
 *   2) manda pra https://api.anthropic.com/v1/messages com a CHAVE da API
 *   3) a resposta chega aos pedacinhos ("streaming"), e cada pedaço aparece na tela na hora
 *
 * A chave da API é paga (cada pergunta custa uns centavos). Ela fica salva no config do TazzCode.
 */
public class ClienteClaude {

    public static final String[] MODELOS = {"claude-haiku-5-5", "claude-sonnet-5-5", "claude-opus-5-5"};
    private static final String ENDERECO = System.getProperty("tazzcode.api", "https://api.anthropic.com") + "/v1/messages";

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    private volatile boolean cancelado;

    /** Uma mensagem da conversa: quem falou ("user" ou "assistant") e o texto. */
    public static class Mensagem {
        public final String papel, texto;
        public Mensagem(String papel, String texto) { this.papel = papel; this.texto = texto; }
    }

    public void cancelar() { cancelado = true; }

    /**
     * Manda a conversa e vai chamando "pedaco" com cada pedacinho da resposta.
     * Roda numa thread separada (não trava a janela). No fim chama "fim" (com null se deu certo, ou o erro).
     */
    public void perguntar(String chave, String modelo, String sistema, List<Mensagem> conversa,
                          Consumer<String> pedaco, Consumer<String> fim) {
        cancelado = false;
        Thread t = new Thread(() -> {
            try {
                Map<String, Object> corpo = new LinkedHashMap<>();
                corpo.put("model", modelo);
                corpo.put("max_tokens", 2000L);
                corpo.put("stream", true);
                corpo.put("system", sistema);
                List<Object> msgs = new ArrayList<>();
                for (Mensagem m : conversa) {
                    Map<String, Object> mm = new LinkedHashMap<>();
                    mm.put("role", m.papel);
                    mm.put("content", m.texto);
                    msgs.add(mm);
                }
                corpo.put("messages", msgs);

                HttpRequest req = HttpRequest.newBuilder(URI.create(ENDERECO))
                        .timeout(Duration.ofMinutes(3))
                        .header("content-type", "application/json")
                        .header("x-api-key", chave)
                        .header("anthropic-version", "2023-06-01")
                        .POST(HttpRequest.BodyPublishers.ofString(Json.escrever(corpo), StandardCharsets.UTF_8))
                        .build();

                HttpResponse<java.io.InputStream> resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
                if (resp.statusCode() != 200) {
                    String erro = new String(resp.body().readAllBytes(), StandardCharsets.UTF_8);
                    fim.accept(explicarErro(resp.statusCode(), erro));
                    return;
                }
                // Streaming: o servidor manda linhas "data: {json}" (Server-Sent Events)
                try (BufferedReader r = new BufferedReader(new InputStreamReader(resp.body(), StandardCharsets.UTF_8))) {
                    String linha;
                    while ((linha = r.readLine()) != null) {
                        if (cancelado) { fim.accept("(parado)"); return; }
                        if (!linha.startsWith("data:")) continue;
                        Object ev = Json.ler(linha.substring(5).trim());
                        if (!(ev instanceof Map)) continue;
                        Map<?, ?> m = (Map<?, ?>) ev;
                        String tipo = String.valueOf(m.get("type"));
                        if (tipo.equals("content_block_delta")) {
                            Map<?, ?> delta = (Map<?, ?>) m.get("delta");
                            if (delta != null && "text_delta".equals(delta.get("type"))) pedaco.accept(String.valueOf(delta.get("text")));
                        } else if (tipo.equals("error")) {
                            Map<?, ?> e = (Map<?, ?>) m.get("error");
                            fim.accept("A IA deu um erro: " + (e == null ? "?" : e.get("message")));
                            return;
                        } else if (tipo.equals("message_stop")) {
                            break;
                        }
                    }
                }
                fim.accept(null);
            } catch (java.net.ConnectException | java.net.http.HttpConnectTimeoutException e) {
                fim.accept("Não consegui conectar na internet. O computador está online?");
            } catch (Exception e) {
                fim.accept("Deu um problema: " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private static String explicarErro(int status, String corpo) {
        String detalhe = "";
        try {
            Object j = Json.ler(corpo);
            if (j instanceof Map && ((Map<?, ?>) j).get("error") instanceof Map) {
                detalhe = String.valueOf(((Map<?, ?>) ((Map<?, ?>) j).get("error")).get("message"));
            }
        } catch (Exception ignorado) { }
        switch (status) {
            case 401: return "A chave da API está errada ou foi apagada. Confira em Configurar (" + detalhe + ")";
            case 402: case 403: return "A conta da API não deixou (falta crédito ou permissão?). " + detalhe;
            case 404: return "Esse modelo não existe ou a conta não tem acesso a ele. Troque o modelo em Configurar. " + detalhe;
            case 429: return "Muitas perguntas seguidas (ou acabou o limite). Espere um pouquinho e tente de novo.";
            case 529: case 503: return "Os servidores da IA estão ocupados agora. Tente de novo daqui a pouco.";
            default: return "Erro " + status + " da API: " + detalhe;
        }
    }
}
