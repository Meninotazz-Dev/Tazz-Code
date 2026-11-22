package tazzcode;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * O "MINI BANCO" do editor.
 * Guarda tudo num arquivo de texto simples (config.properties) na pasta do usuário.
 * Cada linha é "chave=valor", tipo:
 *   fonte.tamanho=16
 *   recente.0=C:\Users\Joao\Desktop\Teste.java
 *
 * Você pode abrir esse arquivo no Bloco de Notas pra ver o que tem lá dentro!
 */
public class Config {

    private static final int MAX_RECENTES = 10;
    private static final Properties dados = new Properties();

    static {
        carregar();
    }

    public static void carregar() {
        if (!Files.exists(Pastas.arquivoConfig())) return;
        try (Reader r = Files.newBufferedReader(Pastas.arquivoConfig(), StandardCharsets.UTF_8)) {
            dados.load(r);
        } catch (IOException e) {
            System.err.println("Não consegui ler a configuração: " + e.getMessage());
        }
    }

    public static void salvar() {
        try (Writer w = Files.newBufferedWriter(Pastas.arquivoConfig(), StandardCharsets.UTF_8)) {
            dados.store(w, App.NOME + " - configuracoes");
        } catch (IOException e) {
            System.err.println("Não consegui salvar a configuração: " + e.getMessage());
        }
    }

    // ---------- valores simples ----------

    public static String texto(String chave, String padrao) {
        return dados.getProperty(chave, padrao);
    }

    public static int numero(String chave, int padrao) {
        try {
            return Integer.parseInt(dados.getProperty(chave));
        } catch (Exception e) {
            return padrao;
        }
    }

    public static void colocar(String chave, Object valor) {
        dados.setProperty(chave, String.valueOf(valor));
        salvar();
    }

    // ---------- arquivos e projetos recentes ----------

    public static List<String> recentes() {
        return lista("recente");
    }

    public static void adicionarRecente(String caminho) {
        adicionar("recente", caminho);
    }

    public static List<String> projetosRecentes() {
        return lista("projeto");
    }

    public static void adicionarProjeto(String caminho) {
        adicionar("projeto", caminho);
    }

    public static void removerProjeto(String caminho) {
        List<String> l = lista("projeto");
        l.remove(caminho);
        gravar("projeto", l);
    }

    private static List<String> lista(String prefixo) {
        List<String> l = new ArrayList<>();
        for (int i = 0; i < MAX_RECENTES; i++) {
            String c = dados.getProperty(prefixo + "." + i);
            if (c != null) l.add(c);
        }
        return l;
    }

    /** Coloca o caminho no topo da lista. */
    private static void adicionar(String prefixo, String caminho) {
        List<String> l = lista(prefixo);
        l.remove(caminho);
        l.add(0, caminho);
        gravar(prefixo, l);
    }

    private static void gravar(String prefixo, List<String> l) {
        for (int i = 0; i < MAX_RECENTES; i++) {
            if (i < l.size()) dados.setProperty(prefixo + "." + i, l.get(i));
            else dados.remove(prefixo + "." + i);
        }
        salvar();
    }
}
