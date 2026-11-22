package tazzcode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * O CATÁLOGO: a lista com TODAS as linguagens que o TazzCode conhece.
 *
 * Elas são cadastradas em 3 arquivos:
 *   CatalogoPrincipais.java -> posições 1 a 25 da lista
 *   CatalogoEspeciais.java  -> posições 26 a 50 da lista
 *   CatalogoExtras.java     -> mais 50 linguagens (51 a 100) + HTML, CSS e .bat
 *   CatalogoOneCompiler.java -> o que faltava do OneCompiler (bancos, Deno/Bun, J, Icon...)
 *
 * DESAFIO: cadastre uma linguagem nova! Copie uma parecida e mude o que precisar.
 */
public class Catalogo {

    private static final List<Linguagem> TODAS = new ArrayList<>();
    private static final Map<String, Linguagem> POR_EXTENSAO = new HashMap<>();
    private static final Map<String, Linguagem> POR_NOME_DE_ARQUIVO = new HashMap<>();

    /** Uma linguagem vazia pra arquivos que a gente não conhece (texto puro). */
    public static final Linguagem TEXTO = new Linguagem("Texto", "txt").modo(Linguagem.Modo.SO_EDITAR)
            .precisa("É só texto: não tem como rodar.");

    static {
        CatalogoPrincipais.cadastrar();
        CatalogoEspeciais.cadastrar();
        CatalogoExtras.cadastrar();
        CatalogoOneCompiler.cadastrar();
        TODAS.sort((a, b) -> a.nome.compareToIgnoreCase(b.nome));
    }

    /** Usado pelos arquivos do catálogo pra cadastrar. */
    static Linguagem nova(String categoria, String nome, String... extensoes) {
        Linguagem l = new Linguagem(nome, extensoes).categoria(categoria);
        TODAS.add(l);
        for (String e : extensoes) POR_EXTENSAO.putIfAbsent(e.toLowerCase(), l);
        return l;
    }

    /** Arquivos que são reconhecidos pelo NOME inteiro (ex.: "Makefile"). */
    static void nomeDeArquivo(String nome, Linguagem l) {
        POR_NOME_DE_ARQUIVO.put(nome.toLowerCase(), l);
    }

    public static List<Linguagem> todas() {
        return Collections.unmodifiableList(TODAS);
    }

    public static Linguagem porExtensao(String extensao) {
        return POR_EXTENSAO.getOrDefault(extensao.toLowerCase(), TEXTO);
    }

    public static Linguagem porArquivo(File f) {
        if (f == null) return TEXTO;
        Linguagem pelaNome = POR_NOME_DE_ARQUIVO.get(f.getName().toLowerCase());
        if (pelaNome != null) return pelaNome;
        return porExtensao(Linguagens.extensao(f));
    }

    public static Linguagem porNome(String nome) {
        for (Linguagem l : TODAS) if (l.nome.equalsIgnoreCase(nome)) return l;
        return TEXTO;
    }
}
