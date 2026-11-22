package tazzcode;

import java.util.ArrayList;
import java.util.List;

/**
 * MODELOS DE ARQUIVO: o que já vem escrito quando você cria um arquivo novo
 * (igual ao IntelliJ, que já cria o "public class Nome { }").
 *
 * Cada linguagem tem o seu modelo ("Olá, mundo!") cadastrado no Catalogo.
 */
public class Modelos {

    public static final String CLASSE_JAVA = "Classe Java";
    public static final String CLASSE_JAVA_MAIN = "Classe Java com main";
    public static final String TEXTO = "Texto";
    public static final String OUTRO = "Outro (eu digito a extensão)";

    /** Os tipos que aparecem na janela "Novo arquivo". */
    public static String[] tipos() {
        List<String> l = new ArrayList<>();
        l.add(CLASSE_JAVA);
        l.add(CLASSE_JAVA_MAIN);
        for (Linguagem lg : Catalogo.todas()) {
            if (!lg.nome.equals("Java")) l.add(lg.nome);
        }
        l.add(TEXTO);
        l.add(OUTRO);
        return l.toArray(new String[0]);
    }

    /** Qual extensão cada tipo usa. */
    public static String extensao(String tipo) {
        if (tipo.equals(CLASSE_JAVA) || tipo.equals(CLASSE_JAVA_MAIN)) return "java";
        if (tipo.equals(TEXTO)) return "txt";
        if (tipo.equals(OUTRO)) return "";
        return Catalogo.porNome(tipo).extensaoPrincipal();
    }

    /** O texto que já vem no arquivo novo. "nome" é o nome sem a extensão. */
    public static String conteudo(String tipo, String nome) {
        if (tipo.equals(CLASSE_JAVA)) return "public class " + nome + " {\n    \n}\n";
        if (tipo.equals(CLASSE_JAVA_MAIN)) return Catalogo.porNome("Java").modelo.replace("{nome}", nome);
        if (tipo.equals(TEXTO) || tipo.equals(OUTRO)) return "";
        return Catalogo.porNome(tipo).modelo.replace("{nome}", nome);
    }
}
