package tazzcode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * JSON feito à mão (sem biblioteca).
 *
 *   ler(texto)      -> transforma texto JSON em Map (objeto), List (lista), String, Long, Double, Boolean ou null
 *   escrever(valor) -> faz o caminho contrário
 *
 * Ele é "bonzinho": também aceita o jeito do JavaScript/MongoDB, tipo
 *   { nome: 'João', idade: 12, }   (chave sem aspas, aspas simples, vírgula sobrando)
 *   /jo/i                          (expressão regular, vira um Pattern)
 *   ObjectId("..."), new Date()    (viram texto)
 */
public class Json {

    public static class Erro extends RuntimeException {
        Erro(String msg) { super(msg); }
    }

    private final String s;
    private int i;

    private Json(String s) { this.s = s; }

    public static Object ler(String texto) {
        Json j = new Json(texto);
        Object v = j.valor();
        j.espacos();
        if (j.i < j.s.length()) throw new Erro("sobrou texto depois do JSON: \"" + j.s.substring(j.i, Math.min(j.s.length(), j.i + 15)) + "\"");
        return v;
    }

    /** Lê um valor e diz até onde leu (pra quem lê pedaços de um texto maior). */
    public static Object[] lerPedaco(String texto, int de) {
        Json j = new Json(texto);
        j.i = de;
        Object v = j.valor();
        return new Object[]{v, j.i};
    }

    // ---------- leitura ----------

    private void espacos() {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) i++;
            else if (s.startsWith("//", i)) { while (i < s.length() && s.charAt(i) != '\n') i++; }
            else if (s.startsWith("/*", i)) { int f = s.indexOf("*/", i + 2); i = f < 0 ? s.length() : f + 2; }
            else break;
        }
    }

    private Object valor() {
        espacos();
        if (i >= s.length()) throw new Erro("o JSON acabou no meio");
        char c = s.charAt(i);
        if (c == '{') return objeto();
        if (c == '[') return lista();
        if (c == '"' || c == '\'') return texto();
        if (c == '/') return regex();
        if (c == '-' || c == '+' || c == '.' || Character.isDigit(c)) return numero();
        String palavra = palavra();
        switch (palavra) {
            case "true": return true;
            case "false": return false;
            case "null": case "undefined": return null;
            case "new": return valor();   // new Date() -> Date()
            default:
                espacos();
                if (i < s.length() && s.charAt(i) == '(') {        // ObjectId("x"), Date(), ISODate("...")
                    i++;
                    espacos();
                    Object arg = null;
                    if (s.charAt(i) != ')') arg = valor();
                    espacos();
                    esperar(')');
                    if (palavra.equals("Date") || palavra.equals("ISODate")) {
                        return arg != null ? arg.toString() : java.time.LocalDateTime.now().withNano(0).toString();
                    }
                    if (palavra.equals("NumberInt") || palavra.equals("NumberLong")) return Long.parseLong(arg.toString());
                    return arg == null ? java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 24) : arg.toString();
                }
                if (palavra.isEmpty()) throw new Erro("não entendi \"" + c + "\" no JSON");
                throw new Erro("não entendi \"" + palavra + "\" (texto precisa de aspas)");
        }
    }

    private Map<String, Object> objeto() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++;   // {
        while (true) {
            espacos();
            if (i < s.length() && s.charAt(i) == '}') { i++; return m; }
            String chave;
            char c = s.charAt(i);
            if (c == '"' || c == '\'') chave = texto();
            else chave = palavra();
            if (chave.isEmpty()) throw new Erro("esperava o nome de uma chave no objeto");
            espacos();
            esperar(':');
            m.put(chave, valor());
            espacos();
            if (i < s.length() && s.charAt(i) == ',') { i++; continue; }
            espacos();
            esperar('}');
            return m;
        }
    }

    private List<Object> lista() {
        List<Object> l = new ArrayList<>();
        i++;   // [
        while (true) {
            espacos();
            if (i < s.length() && s.charAt(i) == ']') { i++; return l; }
            l.add(valor());
            espacos();
            if (i < s.length() && s.charAt(i) == ',') { i++; continue; }
            esperar(']');
            return l;
        }
    }

    private String texto() {
        char aspas = s.charAt(i++);
        StringBuilder sb = new StringBuilder();
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (c == aspas) return sb.toString();
            if (c == '\\' && i < s.length()) {
                char e = s.charAt(i++);
                switch (e) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u':
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                        break;
                    default: sb.append(e);
                }
            } else {
                sb.append(c);
            }
        }
        throw new Erro("texto sem fechar as aspas");
    }

    private Pattern regex() {
        int ini = ++i;
        while (i < s.length() && s.charAt(i) != '/') { if (s.charAt(i) == '\\') i++; i++; }
        String corpo = s.substring(ini, i);
        i++;
        int flags = 0;
        while (i < s.length() && Character.isLetter(s.charAt(i))) {
            if (s.charAt(i) == 'i') flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            if (s.charAt(i) == 'm') flags |= Pattern.MULTILINE;
            i++;
        }
        return Pattern.compile(corpo, flags);
    }

    private Object numero() {
        int ini = i;
        if (s.charAt(i) == '-' || s.charAt(i) == '+') i++;
        while (i < s.length() && (Character.isDigit(s.charAt(i)) || ".eE+-".indexOf(s.charAt(i)) >= 0)) {
            if ((s.charAt(i) == '+' || s.charAt(i) == '-') && !(s.charAt(i - 1) == 'e' || s.charAt(i - 1) == 'E')) break;
            i++;
        }
        String n = s.substring(ini, i);
        if (n.contains(".") || n.contains("e") || n.contains("E")) return Double.parseDouble(n);
        return Long.parseLong(n.replace("+", ""));
    }

    private String palavra() {
        int ini = i;
        while (i < s.length() && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_' || s.charAt(i) == '$' || s.charAt(i) == '.')) i++;
        return s.substring(ini, i);
    }

    private void esperar(char c) {
        espacos();
        if (i >= s.length() || s.charAt(i) != c) {
            throw new Erro("esperava \"" + c + "\" no JSON" + (i < s.length() ? " mas achei \"" + s.charAt(i) + "\"" : ""));
        }
        i++;
    }

    // ---------- escrita ----------

    public static String escrever(Object v) {
        StringBuilder sb = new StringBuilder();
        escrever(v, sb, -1, 0);
        return sb.toString();
    }

    /** Igual, mas com quebras de linha e recuo (bonito de ler). */
    public static String bonito(Object v) {
        StringBuilder sb = new StringBuilder();
        escrever(v, sb, 2, 0);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void escrever(Object v, StringBuilder sb, int recuo, int nivel) {
        if (v == null) { sb.append("null"); return; }
        if (v instanceof String) { aspas((String) v, sb); return; }
        if (v instanceof Pattern) { sb.append('/').append(((Pattern) v).pattern()).append('/'); return; }
        if (v instanceof Double) {
            double d = (Double) v;
            sb.append(d == Math.rint(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15 ? String.valueOf((long) d) + ".0" : String.valueOf(d));
            return;
        }
        if (v instanceof Number || v instanceof Boolean) { sb.append(v); return; }
        if (v instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) v;
            if (m.isEmpty()) { sb.append("{}"); return; }
            sb.append('{');
            boolean primeiro = true;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                if (!primeiro) sb.append(',');
                primeiro = false;
                quebra(sb, recuo, nivel + 1);
                aspas(e.getKey(), sb);
                sb.append(recuo >= 0 ? ": " : ":");
                escrever(e.getValue(), sb, recuo, nivel + 1);
            }
            quebra(sb, recuo, nivel);
            sb.append('}');
            return;
        }
        if (v instanceof List) {
            List<Object> l = (List<Object>) v;
            if (l.isEmpty()) { sb.append("[]"); return; }
            sb.append('[');
            for (int k = 0; k < l.size(); k++) {
                if (k > 0) sb.append(',');
                quebra(sb, recuo, nivel + 1);
                escrever(l.get(k), sb, recuo, nivel + 1);
            }
            quebra(sb, recuo, nivel);
            sb.append(']');
            return;
        }
        aspas(v.toString(), sb);
    }

    private static void quebra(StringBuilder sb, int recuo, int nivel) {
        if (recuo < 0) return;
        sb.append('\n');
        for (int k = 0; k < recuo * nivel; k++) sb.append(' ');
    }

    private static void aspas(String s, StringBuilder sb) {
        sb.append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }
}
