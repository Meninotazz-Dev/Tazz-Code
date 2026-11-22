package tazzcode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * O VERIFICADOR DE HTML: lê o seu HTML e acha tags erradas, tipo:
 *   <DOCTYPE html>        -> falta o "!"  (o certo é <!DOCTYPE html>)
 *   </doctype>            -> o DOCTYPE não tem fechamento
 *   </br>                 -> <br> não fecha
 *   <div> sem </div>      -> faltou fechar
 *   </span> sem <span>    -> fechou o que não abriu
 *   <b><i></b></i>        -> tags trocadas (uma dentro da outra do jeito errado)
 *   <dvi>                 -> tag que não existe (quis dizer <div>?)
 *   id="x" repetido       -> cada id tem que ser único
 *
 * Como funciona: a gente anda pelo texto achando as tags e usa uma PILHA.
 * Quando abre <div>, empilha. Quando fecha </div>, desempilha. Se não bater, é erro!
 */
public class VerificadorHtml {

    public enum Gravidade { ERRO, AVISO, DICA }

    /** Um problema achado no HTML. */
    public static class Problema {
        public final int inicio, fim;
        public final String mensagem;
        public final Gravidade gravidade;
        // Correção rápida: trocar o texto entre [corrigeInicio, corrigeFim] por "corrigePor".
        public int corrigeInicio = -1, corrigeFim = -1;
        public String corrigePor, comoCorrige;

        Problema(int inicio, int fim, Gravidade g, String mensagem) {
            this.inicio = inicio;
            this.fim = Math.max(fim, inicio + 1);
            this.gravidade = g;
            this.mensagem = mensagem;
        }

        Problema correcao(int ini, int fim, String por, String como) {
            corrigeInicio = ini;
            corrigeFim = fim;
            corrigePor = por;
            comoCorrige = como;
            return this;
        }

        public boolean temCorrecao() { return corrigeInicio >= 0; }
    }

    /** Tags que NÃO têm fechamento. */
    static final Set<String> SEM_FECHAR = new HashSet<>(Arrays.asList(
            "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "source", "track", "wbr", "param"));

    /** Tags que o navegador fecha sozinho (então esquecer de fechar é só aviso). */
    static final Set<String> FECHA_SOZINHA = new HashSet<>(Arrays.asList(
            "p", "li", "dt", "dd", "tr", "td", "th", "option", "thead", "tbody", "tfoot", "colgroup", "optgroup",
            "html", "head", "body", "rt", "rp"));

    /** Todas as tags do HTML que a gente conhece. */
    static final Set<String> CONHECIDAS = new HashSet<>(Arrays.asList(
            "a", "abbr", "address", "area", "article", "aside", "audio", "b", "base", "bdi", "bdo", "blockquote",
            "body", "br", "button", "canvas", "caption", "cite", "code", "col", "colgroup", "data", "datalist", "dd",
            "del", "details", "dfn", "dialog", "div", "dl", "dt", "em", "embed", "fieldset", "figcaption", "figure",
            "footer", "form", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html", "i",
            "iframe", "img", "input", "ins", "kbd", "label", "legend", "li", "link", "main", "map", "mark", "menu",
            "meta", "meter", "nav", "noscript", "object", "ol", "optgroup", "option", "output", "p", "param",
            "picture", "pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script", "search", "section",
            "select", "slot", "small", "source", "span", "strong", "style", "sub", "summary", "sup", "table", "tbody",
            "td", "template", "textarea", "tfoot", "th", "thead", "time", "title", "tr", "track", "u", "ul", "var",
            "video", "wbr", "svg", "path", "circle", "rect", "line", "polyline", "polygon", "g", "text", "ellipse",
            "defs", "use", "math", "center", "font", "marquee"));

    private static final Pattern ID = Pattern.compile("\\bid\\s*=\\s*([\"'])(.*?)\\1", Pattern.CASE_INSENSITIVE);

    /** Uma tag aberta que está na pilha. */
    private static class Aberta {
        final String nome;
        final int inicio, fim;
        Aberta(String nome, int inicio, int fim) { this.nome = nome; this.inicio = inicio; this.fim = fim; }
    }

    public static List<Problema> verificar(String t) {
        List<Problema> problemas = new ArrayList<>();
        Deque<Aberta> pilha = new ArrayDeque<>();
        Map<String, Integer> ids = new HashMap<>();
        boolean achouConteudo = false;   // pra saber se o DOCTYPE está no começo
        boolean temDoctype = false;
        int i = 0, n = t.length();

        while (i < n) {
            if (t.startsWith("<!--", i)) {                                     // comentário
                int fim = t.indexOf("-->", i + 4);
                if (fim < 0) {
                    problemas.add(new Problema(i, i + 4, Gravidade.ERRO, "Comentário <!-- sem fechar. Feche com -->")
                            .correcao(n, n, "-->", "fechar o comentário no fim"));
                    break;
                }
                i = fim + 3;
                continue;
            }
            char c = t.charAt(i);
            char prox = i + 1 < n ? t.charAt(i + 1) : ' ';
            boolean pareceTag = c == '<' && (Character.isLetter(prox) || prox == '/' || prox == '!' || prox == '?');
            if (!pareceTag) {
                if (!Character.isWhitespace(c)) achouConteudo = true;
                i++;
                continue;
            }
            int fimTag = acharFimDaTag(t, i);
            if (fimTag < 0) {                                                  // < sem >
                int fimLinha = t.indexOf('\n', i);
                if (fimLinha < 0) fimLinha = n;
                int outra = t.indexOf('<', i + 1);
                if (outra >= 0 && outra < fimLinha) fimLinha = outra;
                problemas.add(new Problema(i, fimLinha, Gravidade.ERRO, "Essa tag não fecha com \">\".")
                        .correcao(fimLinha, fimLinha, ">", "colocar o \">\""));
                i++;
                continue;
            }
            String dentro = t.substring(i + 1, fimTag);   // o que está entre < e >
            int depois = fimTag + 1;

            // ----- DOCTYPE -----
            String semExcl = dentro.startsWith("!") ? dentro.substring(1) : dentro;
            if (semExcl.toLowerCase().startsWith("doctype")) {
                temDoctype = true;
                if (!dentro.startsWith("!")) {
                    problemas.add(new Problema(i, depois, Gravidade.ERRO, "Faltou o \"!\": o certo é <!DOCTYPE html>")
                            .correcao(i, depois, "<!DOCTYPE html>", "trocar por <!DOCTYPE html>"));
                } else if (!semExcl.trim().equalsIgnoreCase("doctype html")) {
                    problemas.add(new Problema(i, depois, Gravidade.AVISO, "Hoje em dia o DOCTYPE é só <!DOCTYPE html>")
                            .correcao(i, depois, "<!DOCTYPE html>", "trocar por <!DOCTYPE html>"));
                }
                if (achouConteudo) {
                    problemas.add(new Problema(i, depois, Gravidade.AVISO, "O <!DOCTYPE html> tem que ser a PRIMEIRA coisa do arquivo."));
                }
                i = depois;
                continue;
            }
            if (dentro.startsWith("/") && dentro.substring(1).trim().replace("!", "").toLowerCase().startsWith("doctype")) {
                problemas.add(new Problema(i, depois, Gravidade.ERRO, "O DOCTYPE não tem fechamento: apague esse " + t.substring(i, depois))
                        .correcao(i, depois, "", "apagar " + t.substring(i, depois)));
                i = depois;
                continue;
            }
            if (dentro.startsWith("!") || dentro.startsWith("?")) { i = depois; continue; }   // <!...> e <?xml ?>

            achouConteudo = true;
            boolean fechando = dentro.startsWith("/");
            String corpo = fechando ? dentro.substring(1) : dentro;
            Matcher m = Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9-]*)").matcher(corpo);
            if (!m.find()) {
                problemas.add(new Problema(i, depois, Gravidade.ERRO, "Tag sem nome. Depois do \"<\" vem o nome da tag, tipo <div>."));
                i = depois;
                continue;
            }
            String nome = m.group(1).toLowerCase();
            int iniNome = i + 1 + (fechando ? 1 : 0) + m.start(1);
            int fimNome = iniNome + m.group(1).length();
            if (fechando && m.start(1) > 0) {
                problemas.add(new Problema(i, depois, Gravidade.ERRO, "Não pode ter espaço depois do \"</\".")
                        .correcao(i, depois, "</" + m.group(1) + ">", "tirar o espaço"));
            }

            // ----- tag desconhecida -----
            if (!CONHECIDAS.contains(nome) && !nome.contains("-")) {
                String parecida = maisParecida(nome);
                Problema p = new Problema(iniNome, fimNome, Gravidade.AVISO,
                        "A tag <" + nome + "> não existe no HTML." + (parecida != null ? " Você quis dizer <" + parecida + ">?" : ""));
                if (parecida != null) p.correcao(iniNome, fimNome, parecida, "trocar por " + parecida);
                problemas.add(p);
            }

            // ----- aspas sem fechar dentro da tag -----
            if (!fechando && (contar(dentro, '"') % 2 != 0 || contar(dentro, '\'') % 2 != 0 && !dentro.contains("\""))) {
                problemas.add(new Problema(i, depois, Gravidade.ERRO, "Tem uma aspa (\") abrindo sem fechar dentro dessa tag."));
            }

            // ----- id repetido -----
            if (!fechando) {
                Matcher mid = ID.matcher(dentro);
                while (mid.find()) {
                    String id = mid.group(2);
                    int pos = i + 1 + mid.start(2);
                    if (ids.containsKey(id)) {
                        problemas.add(new Problema(pos, pos + id.length(), Gravidade.AVISO,
                                "O id \"" + id + "\" já foi usado antes. Cada id tem que ser único (use class pra repetir)."));
                    } else {
                        ids.put(id, pos);
                    }
                }
                if (nome.equals("img") && !dentro.toLowerCase().matches("(?s).*\\balt\\s*=.*")) {
                    problemas.add(new Problema(iniNome, fimNome, Gravidade.DICA,
                            "Dica: coloque alt=\"descrição\" no <img> (ajuda quem não enxerga a imagem).")
                            .correcao(fimNome, fimNome, " alt=\"\"", "adicionar alt=\"\""));
                }
            }

            // ----- abrir / fechar (a pilha) -----
            boolean autoFechada = dentro.trim().endsWith("/");
            if (fechando) {
                if (SEM_FECHAR.contains(nome)) {
                    problemas.add(new Problema(i, depois, Gravidade.ERRO, "<" + nome + "> não tem fechamento: apague o </" + nome + ">")
                            .correcao(i, depois, "", "apagar </" + nome + ">"));
                } else if (!fecharNaPilha(pilha, nome, i, problemas)) {
                    problemas.add(new Problema(i, depois, Gravidade.ERRO, "Fechou </" + nome + "> mas não tem nenhum <" + nome + "> aberto.")
                            .correcao(i, depois, "", "apagar </" + nome + ">"));
                }
            } else if (!SEM_FECHAR.contains(nome) && !autoFechada) {
                pilha.push(new Aberta(nome, i, depois));
                if (nome.equals("script") || nome.equals("style") || nome.equals("textarea") || nome.equals("title")) {
                    // Lá dentro é código/texto, não HTML: pula até o fechamento.
                    int fecha = indexOfIgnoreCase(t, "</" + nome, depois);
                    i = fecha < 0 ? n : fecha;
                    continue;
                }
            }
            i = depois;
        }

        // ----- o que ficou aberto no fim -----
        while (!pilha.isEmpty()) {
            Aberta a = pilha.pop();
            if (FECHA_SOZINHA.contains(a.nome)) continue;
            Gravidade g = Gravidade.ERRO;
            problemas.add(new Problema(a.inicio, a.fim, g, "Faltou fechar o <" + a.nome + ">. Coloque </" + a.nome + "> no lugar certo.")
                    .correcao(n, n, (t.endsWith("\n") || t.isEmpty() ? "" : "\n") + "</" + a.nome + ">", "colocar </" + a.nome + "> no fim"));
        }

        if (temDoctype) {
            String minus = t.toLowerCase();
            if (!minus.contains("<title")) problemas.add(new Problema(0, 1, Gravidade.DICA, "Dica: a página não tem <title> (o nome que aparece na aba do navegador)."));
            if (!minus.contains("charset")) problemas.add(new Problema(0, 1, Gravidade.DICA, "Dica: coloque <meta charset=\"UTF-8\"> no <head> pra os acentos aparecerem certinho."));
        }
        problemas.sort((a, b) -> Integer.compare(a.inicio, b.inicio));
        return problemas;
    }

    /** Fecha a tag na pilha. As que estavam abertas "por cima" dela ficaram sem fechar. */
    private static boolean fecharNaPilha(Deque<Aberta> pilha, String nome, int posFechamento, List<Problema> problemas) {
        boolean existe = false;
        for (Aberta a : pilha) if (a.nome.equals(nome)) { existe = true; break; }
        if (!existe) return false;
        while (!pilha.isEmpty()) {
            Aberta a = pilha.pop();
            if (a.nome.equals(nome)) return true;
            if (FECHA_SOZINHA.contains(a.nome)) continue;   // <li>, <p>... o navegador fecha sozinho (é permitido)
            Gravidade g = Gravidade.ERRO;
            problemas.add(new Problema(a.inicio, a.fim, g,
                    "O <" + a.nome + "> não foi fechado antes do </" + nome + ">. (Tags fecham na ordem contrária: a última que abre é a primeira que fecha.)")
                    .correcao(posFechamento, posFechamento, "</" + a.nome + ">", "colocar </" + a.nome + "> antes do </" + nome + ">"));
        }
        return true;
    }

    /** Acha o ">" que fecha a tag, pulando o que estiver entre aspas. */
    private static int acharFimDaTag(String t, int ini) {
        char aspas = 0;
        for (int j = ini + 1; j < t.length(); j++) {
            char c = t.charAt(j);
            if (aspas != 0) {
                if (c == aspas) aspas = 0;
                else if (c == '\n') return t.indexOf('>', ini);   // aspa sem fechar: usa o próximo ">"
                continue;
            }
            if (c == '"' || c == '\'') aspas = c;
            else if (c == '>') return j;
            else if (c == '<') return -1;   // abriu outra tag antes de fechar essa
        }
        return -1;
    }

    private static int contar(String s, char c) {
        int k = 0;
        for (char x : s.toCharArray()) if (x == c) k++;
        return k;
    }

    private static int indexOfIgnoreCase(String t, String alvo, int de) {
        return t.toLowerCase().indexOf(alvo.toLowerCase(), de);
    }

    /** A tag conhecida mais parecida (distância de Levenshtein até 2). */
    static String maisParecida(String nome) {
        String melhor = null;
        int menor = 3;
        for (String c : CONHECIDAS) {
            int d = distancia(nome, c);
            if (d < menor || (d == menor && melhor != null && c.length() == nome.length())) {
                menor = d;
                melhor = c;
            }
        }
        return menor <= 2 && nome.length() > 1 ? melhor : null;
    }

    /** Quantas letras precisa mudar pra transformar uma palavra na outra. */
    static int distancia(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) d[i][0] = i;
        for (int j = 0; j <= b.length(); j++) d[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int custo = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + custo);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);   // letras trocadas (dvi -> div)
                }
            }
        }
        return d[a.length()][b.length()];
    }
}
