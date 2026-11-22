package tazzcode;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;

/**
 * DESTAQUE DE SINTAXE: pinta o código com cores (palavras-chave, textos, números, comentários...).
 *
 * Como funciona: a gente lê o texto letra por letra, e quando reconhece um "pedaço"
 * (um comentário, um texto entre aspas, uma palavra-chave...) pinta aquele pedaço.
 *
 * As listas de palavras de cada linguagem ficam no Catalogo.
 */
public class Destaque {

    private final StyledDocument doc;
    private String texto;
    private final SimpleAttributeSet normal = estilo(Tema.TEXTO, false, false);

    public Destaque(StyledDocument doc) {
        this.doc = doc;
    }

    /** Repinta o documento inteiro de acordo com a linguagem. */
    public void pintar(Linguagem l) {
        try {
            texto = doc.getText(0, doc.getLength());
        } catch (Exception e) {
            return;
        }
        doc.setCharacterAttributes(0, texto.length(), normal, true);
        if (l == null || l == Catalogo.TEXTO) return;   // texto puro: sem cores

        switch (l.estilo) {
            case HTML: pintarHtml(); break;
            case CSS:  pintarCss(); break;
            default:
                if (l.modo == Linguagem.Modo.BRAINFUCK) pintarBrainfuck();
                else pintarGenerico(l);
        }
    }

    // =====================================================================
    //  O pintor que serve pra (quase) todas as linguagens: ele usa as listas
    //  de palavras, funções, tipos, comentários e aspas que estão no Catalogo.
    // =====================================================================

    private void pintarGenerico(Linguagem l) {
        boolean temClasses = l.palavras.contains("class") && !l.ignoraMaiusculas;
        int i = 0, n = texto.length();
        while (i < n) {
            char c = texto.charAt(i);

            // comentário de bloco: /* ... */, {- ... -}, (* ... *)...
            String[] bloco = blocoEm(l, i);
            if (bloco != null) {
                int fim = texto.indexOf(bloco[1], i + bloco[0].length());
                fim = fim < 0 ? n : fim + bloco[1].length();
                pinta(i, fim, Tema.COD_COMENTARIO, false, true);
                i = fim;
                continue;
            }
            // comentário de linha: //, #, --, ;, rem...
            if (comentarioLinhaEm(l, i)) {
                int fim = fimDaLinha(i);
                pinta(i, fim, Tema.COD_COMENTARIO, false, true);
                i = fim;
                continue;
            }
            // "texto" (e """texto grande""" do Python, Kotlin, Swift...)
            if (l.aspas.indexOf(c) >= 0) {
                String triplo = "" + c + c + c;
                int fim;
                if (texto.startsWith(triplo, i)) {
                    fim = texto.indexOf(triplo, i + 3);
                    fim = fim < 0 ? n : fim + 3;
                } else {
                    fim = fimDoTexto(i, c);
                }
                pinta(i, fim, Tema.COD_TEXTO, false, false);
                i = fim;
                continue;
            }
            // $variavel, @variavel, %variavel%
            if (l.prefixosVariavel.indexOf(c) >= 0 && i + 1 < n && Character.isLetter(texto.charAt(i + 1))) {
                int fim = i + 1;
                while (fim < n && (Character.isLetterOrDigit(texto.charAt(fim)) || texto.charAt(fim) == '_')) fim++;
                if (c == '%' && fim < n && texto.charAt(fim) == '%') fim++;
                pinta(i, fim, Tema.COD_TIPO, false, false);
                i = fim;
                continue;
            }
            // @Anotacao (Java, Python, Kotlin...)
            if (c == '@' && i + 1 < n && Character.isLetter(texto.charAt(i + 1)) && l.letrasExtras.indexOf('@') < 0) {
                int fim = fimDaPalavra(i + 1);
                pinta(i, fim, Tema.COD_ANOTACAO, false, false);
                i = fim;
                continue;
            }
            if (Character.isDigit(c)) {
                int fim = fimDoNumero(i);
                pinta(i, fim, Tema.COD_NUMERO, false, false);
                i = fim;
                continue;
            }
            if (Character.isLetter(c) || c == '_' || ((c == '\\' || c == '$' || c == '@' || c == '#') && l.letrasExtras.indexOf(c) >= 0)) {
                int fim = i + 1;
                while (fim < n && (Character.isLetterOrDigit(texto.charAt(fim)) || texto.charAt(fim) == '_'
                        || l.letrasExtras.indexOf(texto.charAt(fim)) >= 0)) fim++;
                while (fim > i + 1 && ".:".indexOf(texto.charAt(fim - 1)) >= 0) fim--;   // "x." no fim não conta
                String palavra = l.normalizar(texto.substring(i, fim));
                if (l.palavras.contains(palavra)) {
                    pinta(i, fim, Tema.COD_PALAVRA, false, false);
                } else if (l.funcoes.contains(palavra) || proximoNaoEspaco(fim) == '(') {
                    pinta(i, fim, Tema.COD_METODO, false, false);
                } else if (l.tipos.contains(palavra) || (temClasses && Character.isUpperCase(c))) {
                    pinta(i, fim, Tema.COD_TIPO, false, false);
                }
                i = fim;
                continue;
            }
            i++;
        }
    }

    /** Começa um comentário de bloco aqui? Devolve {abre, fecha} ou null. */
    private String[] blocoEm(Linguagem l, int i) {
        for (String[] b : l.comentarioBloco) {
            if (texto.startsWith(b[0], i)) {
                // "=begin" do Ruby só vale no começo da linha
                if (Character.isLetter(b[0].charAt(b[0].length() - 1)) && i > 0 && texto.charAt(i - 1) != '\n') continue;
                return b;
            }
        }
        return null;
    }

    private boolean comentarioLinhaEm(Linguagem l, int i) {
        for (String marca : l.comentarioLinha) {
            boolean palavra = Character.isLetter(marca.charAt(0)) || marca.equals("*");
            if (palavra) {
                // "rem", "REM" e "*" só valem no começo da linha (depois de espaços)
                if (!inicioDeLinha(i)) continue;
                if (!texto.regionMatches(true, i, marca, 0, marca.length())) continue;
                int depois = i + marca.length();
                if (Character.isLetter(marca.charAt(0)) && depois < texto.length() && !Character.isWhitespace(texto.charAt(depois))) continue;
                return true;
            }
            if (texto.startsWith(marca, i)) {
                // "#" seguido de "include"/"define" no C não é comentário (C usa //)
                return true;
            }
        }
        return false;
    }

    private boolean inicioDeLinha(int i) {
        int j = i - 1;
        while (j >= 0 && (texto.charAt(j) == ' ' || texto.charAt(j) == '\t')) j--;
        return j < 0 || texto.charAt(j) == '\n';
    }

    private void pintarBrainfuck() {
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if ("+-".indexOf(c) >= 0) pinta(i, i + 1, Tema.COD_NUMERO, true, false);
            else if ("<>".indexOf(c) >= 0) pinta(i, i + 1, Tema.COD_PALAVRA, true, false);
            else if ("[]".indexOf(c) >= 0) pinta(i, i + 1, Tema.COD_TAG, true, false);
            else if (".,".indexOf(c) >= 0) pinta(i, i + 1, Tema.COD_METODO, true, false);
            else pinta(i, i + 1, Tema.COD_COMENTARIO, false, true);
        }
    }

    // =====================================================================
    //  HTML
    // =====================================================================

    private void pintarHtml() {
        int i = 0, n = texto.length();
        while (i < n) {
            if (texto.startsWith("<!--", i)) {
                int fim = texto.indexOf("-->", i + 4);
                fim = fim < 0 ? n : fim + 3;
                pinta(i, fim, Tema.COD_COMENTARIO, false, true);
                i = fim;
            } else if (texto.charAt(i) == '<') {
                // <nome-da-tag atributo="valor">
                int fimTag = texto.indexOf('>', i);
                if (fimTag < 0) fimTag = n - 1;
                int j = i + 1;
                if (j < n && (texto.charAt(j) == '/' || texto.charAt(j) == '!')) j++;
                int fimNome = j;
                while (fimNome < n && (Character.isLetterOrDigit(texto.charAt(fimNome)) || texto.charAt(fimNome) == '-')) fimNome++;
                pinta(i, fimNome, Tema.COD_TAG, true, false);

                int k = fimNome;
                while (k < fimTag) {
                    char c = texto.charAt(k);
                    if (c == '"' || c == '\'') {
                        int fim = Math.min(fimDoTexto(k, c), fimTag);
                        pinta(k, fim, Tema.COD_TEXTO, false, false);
                        k = fim;
                    } else if (Character.isLetter(c)) {
                        int fim = k;
                        while (fim < fimTag && (Character.isLetterOrDigit(texto.charAt(fim)) || texto.charAt(fim) == '-')) fim++;
                        pinta(k, fim, Tema.COD_ATRIBUTO, false, false);
                        k = fim;
                    } else {
                        k++;
                    }
                }
                if (fimTag < n) pinta(fimTag, fimTag + 1, Tema.COD_TAG, true, false);
                i = fimTag + 1;
            } else {
                i++;
            }
        }
    }

    // =====================================================================
    //  CSS
    // =====================================================================

    private void pintarCss() {
        int i = 0, n = texto.length();
        boolean dentroDasChaves = false;
        while (i < n) {
            char c = texto.charAt(i);
            if (c == '/' && i + 1 < n && texto.charAt(i + 1) == '*') {
                int fim = texto.indexOf("*/", i + 2);
                fim = fim < 0 ? n : fim + 2;
                pinta(i, fim, Tema.COD_COMENTARIO, false, true);
                i = fim;
            } else if (c == '{') {
                dentroDasChaves = true;
                i++;
            } else if (c == '}') {
                dentroDasChaves = false;
                i++;
            } else if (c == '"' || c == '\'') {
                int fim = fimDoTexto(i, c);
                pinta(i, fim, Tema.COD_TEXTO, false, false);
                i = fim;
            } else if (!dentroDasChaves && !Character.isWhitespace(c)) {
                // seletor: body, .classe, #id
                int fim = i;
                while (fim < n && texto.charAt(fim) != '{' && texto.charAt(fim) != ',' && texto.charAt(fim) != '\n') fim++;
                pinta(i, fim, Tema.COD_TAG, false, false);
                i = Math.max(fim, i + 1);
            } else if (dentroDasChaves && (Character.isLetter(c) || c == '-')) {
                int fim = i;
                while (fim < n && (Character.isLetterOrDigit(texto.charAt(fim)) || texto.charAt(fim) == '-')) fim++;
                if (proximoNaoEspaco(fim) == ':') pinta(i, fim, Tema.COD_PALAVRA, false, false); // propriedade
                i = fim;
            } else if (dentroDasChaves && (Character.isDigit(c) || c == '#')) {
                int fim = i + 1;
                while (fim < n && (Character.isLetterOrDigit(texto.charAt(fim)) || texto.charAt(fim) == '.' || texto.charAt(fim) == '%')) fim++;
                pinta(i, fim, Tema.COD_NUMERO, false, false);
                i = fim;
            } else {
                i++;
            }
        }
    }

    // =====================================================================
    //  ajudantes
    // =====================================================================

    private int fimDaLinha(int i) {
        int f = texto.indexOf('\n', i);
        return f < 0 ? texto.length() : f;
    }

    private int fimDoTexto(int i, char aspas) {
        int j = i + 1;
        while (j < texto.length()) {
            char c = texto.charAt(j);
            if (c == '\\') { j += 2; continue; }
            if (c == aspas) return j + 1;
            if (c == '\n' && aspas != '`') return j;   // texto não fechado: para na linha
            j++;
        }
        return texto.length();
    }

    private int fimDaPalavra(int i) {
        int j = i;
        while (j < texto.length() && Character.isJavaIdentifierPart(texto.charAt(j))) j++;
        return j;
    }

    private int fimDoNumero(int i) {
        int j = i;
        while (j < texto.length() && (Character.isLetterOrDigit(texto.charAt(j)) || texto.charAt(j) == '.' || texto.charAt(j) == '_')) j++;
        return j;
    }

    private char proximoNaoEspaco(int i) {
        while (i < texto.length() && texto.charAt(i) == ' ') i++;
        return i < texto.length() ? texto.charAt(i) : 0;
    }

    private void pinta(int inicio, int fim, Color cor, boolean negrito, boolean italico) {
        if (fim > inicio) doc.setCharacterAttributes(inicio, fim - inicio, estilo(cor, negrito, italico), false);
    }

    private static SimpleAttributeSet estilo(Color cor, boolean negrito, boolean italico) {
        SimpleAttributeSet s = new SimpleAttributeSet();
        StyleConstants.setForeground(s, cor);
        StyleConstants.setBold(s, negrito);
        StyleConstants.setItalic(s, italico);
        return s;
    }

}
