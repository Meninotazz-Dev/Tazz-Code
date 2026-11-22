package tazzcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * REDIS SIMULADO: um banco "chave = valor" bem rápido (o Redis de verdade guarda tudo na memória também!).
 * Cada linha é um comando, do jeito do redis-cli:
 *   SET nome "João"        GET nome         DEL nome      INCR pontos
 *   LPUSH fila a b c       LRANGE fila 0 -1                (listas)
 *   SADD tags java python  SMEMBERS tags                   (conjuntos)
 *   HSET aluno:1 nome João idade 12   HGETALL aluno:1      (hash = objetinho)
 *   ZADD ranking 100 João 80 Maria    ZRANGE ranking 0 -1 WITHSCORES   (ranking!)
 *   KEYS *   EXPIRE nome 10   TTL nome   FLUSHALL
 */
public class RedisSimulado implements MotorBanco {

    private final Map<String, Object> dados = new LinkedHashMap<>();
    private final Map<String, Long> expira = new HashMap<>();   // chave -> quando expira (ms)
    private Consumer<String> msg = s -> { };

    @Override
    public void executar(String codigo, Consumer<String> mensagens, Consumer<Sql.Resultado> resultados) {
        this.msg = mensagens;
        for (String linha : codigo.split("\\r?\\n")) {
            String l = linha.trim();
            if (l.isEmpty() || l.startsWith("#") || l.startsWith("//") || l.startsWith("--")) continue;
            try {
                List<String> partes = partir(l);
                if (partes.isEmpty()) continue;
                mensagens.accept(executarComando(partes));
            } catch (RuntimeException e) {
                mensagens.accept("ERRO: (error) " + e.getMessage());
            }
        }
    }

    @Override public void limpar() { dados.clear(); expira.clear(); }

    @Override
    public List<String[]> itens() {
        limparExpiradas();
        List<String[]> l = new ArrayList<>();
        for (Map.Entry<String, Object> e : dados.entrySet()) l.add(new String[]{e.getKey(), tipo(e.getValue())});
        return l;
    }

    @Override
    public String comandoParaVer(String item) {
        Object v = dados.get(item);
        String k = item.contains(" ") ? "\"" + item + "\"" : item;
        if (v instanceof List) return "LRANGE " + k + " 0 -1";
        if (v instanceof Set) return "SMEMBERS " + k;
        if (v instanceof TreeMap) return "ZRANGE " + k + " 0 -1 WITHSCORES";
        if (v instanceof Map) return "HGETALL " + k;
        return "GET " + k;
    }

    @Override public String tituloItens() { return "CHAVES"; }
    @Override public String prompt() { return "127.0.0.1:6379>"; }

    // =====================================================================

    @SuppressWarnings("unchecked")
    private String executarComando(List<String> p) {
        limparExpiradas();
        String cmd = p.get(0).toUpperCase();
        List<String> a = p.subList(1, p.size());
        switch (cmd) {
            case "PING": return a.isEmpty() ? "PONG" : aspas(a.get(0));
            case "ECHO": return aspas(arg(a, 0));
            case "SET": {
                dados.put(arg(a, 0), arg(a, 1));
                expira.remove(a.get(0));
                for (int k = 2; k < a.size() - 1; k++) {
                    if (a.get(k).equalsIgnoreCase("EX")) expira.put(a.get(0), System.currentTimeMillis() + Long.parseLong(a.get(k + 1)) * 1000);
                    if (a.get(k).equalsIgnoreCase("PX")) expira.put(a.get(0), System.currentTimeMillis() + Long.parseLong(a.get(k + 1)));
                }
                return "OK";
            }
            case "SETNX": if (dados.containsKey(arg(a, 0))) return "(integer) 0"; dados.put(a.get(0), arg(a, 1)); return "(integer) 1";
            case "GET": { Object v = dados.get(arg(a, 0)); if (v == null) return "(nil)"; return aspas(texto(v, "GET")); }
            case "MSET": for (int k = 0; k + 1 < a.size(); k += 2) dados.put(a.get(k), a.get(k + 1)); return "OK";
            case "MGET": { List<String> r = new ArrayList<>(); for (String k : a) { Object v = dados.get(k); r.add(v instanceof String ? aspas((String) v) : "(nil)"); } return numerada(r); }
            case "DEL": case "UNLINK": { int n = 0; for (String k : a) if (dados.remove(k) != null) { n++; expira.remove(k); } return "(integer) " + n; }
            case "EXISTS": { int n = 0; for (String k : a) if (dados.containsKey(k)) n++; return "(integer) " + n; }
            case "INCR": return "(integer) " + somar(arg(a, 0), 1);
            case "DECR": return "(integer) " + somar(arg(a, 0), -1);
            case "INCRBY": return "(integer) " + somar(arg(a, 0), Long.parseLong(arg(a, 1)));
            case "DECRBY": return "(integer) " + somar(arg(a, 0), -Long.parseLong(arg(a, 1)));
            case "APPEND": { String v = (dados.containsKey(arg(a, 0)) ? texto(dados.get(a.get(0)), "APPEND") : "") + arg(a, 1); dados.put(a.get(0), v); return "(integer) " + v.length(); }
            case "STRLEN": { Object v = dados.get(arg(a, 0)); return "(integer) " + (v == null ? 0 : texto(v, "STRLEN").length()); }
            case "KEYS": {
                Pattern pt = Pattern.compile(arg(a, 0).replace(".", "\\.").replace("*", ".*").replace("?", "."));
                List<String> r = new ArrayList<>();
                for (String k : dados.keySet()) if (pt.matcher(k).matches()) r.add(aspas(k));
                return numerada(r);
            }
            case "TYPE": { Object v = dados.get(arg(a, 0)); return v == null ? "none" : tipo(v); }
            case "RENAME": { Object v = dados.remove(arg(a, 0)); if (v == null) throw new RuntimeException("ERR no such key"); dados.put(arg(a, 1), v); return "OK"; }
            case "EXPIRE": if (!dados.containsKey(arg(a, 0))) return "(integer) 0"; expira.put(a.get(0), System.currentTimeMillis() + Long.parseLong(arg(a, 1)) * 1000); return "(integer) 1";
            case "TTL": { if (!dados.containsKey(arg(a, 0))) return "(integer) -2"; Long t = expira.get(a.get(0)); return "(integer) " + (t == null ? -1 : Math.max(0, (t - System.currentTimeMillis() + 999) / 1000)); }
            case "PERSIST": return "(integer) " + (expira.remove(arg(a, 0)) != null ? 1 : 0);
            case "DBSIZE": return "(integer) " + dados.size();
            case "FLUSHALL": case "FLUSHDB": limpar(); return "OK";
            case "SELECT": return "OK";
            case "INFO": return "# Server\nredis_version:7 (simulado pelo TazzCode)\n# Keyspace\nkeys=" + dados.size();

            // ----- listas -----
            case "LPUSH": case "RPUSH": {
                LinkedList<String> l = (LinkedList<String>) pegar(arg(a, 0), LinkedList.class, true);
                for (String v : a.subList(1, a.size())) { if (cmd.equals("LPUSH")) l.addFirst(v); else l.addLast(v); }
                return "(integer) " + l.size();
            }
            case "LPOP": case "RPOP": {
                LinkedList<String> l = (LinkedList<String>) pegar(arg(a, 0), LinkedList.class, false);
                if (l == null || l.isEmpty()) return "(nil)";
                String v = cmd.equals("LPOP") ? l.removeFirst() : l.removeLast();
                if (l.isEmpty()) dados.remove(a.get(0));
                return aspas(v);
            }
            case "LLEN": { LinkedList<String> l = (LinkedList<String>) pegar(arg(a, 0), LinkedList.class, false); return "(integer) " + (l == null ? 0 : l.size()); }
            case "LRANGE": {
                LinkedList<String> l = (LinkedList<String>) pegar(arg(a, 0), LinkedList.class, false);
                if (l == null) return "(empty array)";
                int[] f = faixa(l.size(), arg(a, 1), arg(a, 2));
                List<String> r = new ArrayList<>();
                for (int k = f[0]; k <= f[1]; k++) r.add(aspas(l.get(k)));
                return numerada(r);
            }
            case "LINDEX": {
                LinkedList<String> l = (LinkedList<String>) pegar(arg(a, 0), LinkedList.class, false);
                if (l == null) return "(nil)";
                int k = Integer.parseInt(arg(a, 1));
                if (k < 0) k += l.size();
                return k >= 0 && k < l.size() ? aspas(l.get(k)) : "(nil)";
            }

            // ----- conjuntos -----
            case "SADD": { Set<String> st = (Set<String>) pegar(arg(a, 0), LinkedHashSet.class, true); int n = 0; for (String v : a.subList(1, a.size())) if (st.add(v)) n++; return "(integer) " + n; }
            case "SREM": { Set<String> st = (Set<String>) pegar(arg(a, 0), LinkedHashSet.class, false); int n = 0; if (st != null) for (String v : a.subList(1, a.size())) if (st.remove(v)) n++; return "(integer) " + n; }
            case "SMEMBERS": { Set<String> st = (Set<String>) pegar(arg(a, 0), LinkedHashSet.class, false); List<String> r = new ArrayList<>(); if (st != null) for (String v : st) r.add(aspas(v)); return numerada(r); }
            case "SISMEMBER": { Set<String> st = (Set<String>) pegar(arg(a, 0), LinkedHashSet.class, false); return "(integer) " + (st != null && st.contains(arg(a, 1)) ? 1 : 0); }
            case "SCARD": { Set<String> st = (Set<String>) pegar(arg(a, 0), LinkedHashSet.class, false); return "(integer) " + (st == null ? 0 : st.size()); }

            // ----- hashes -----
            case "HSET": case "HMSET": {
                Map<String, String> h = (Map<String, String>) pegar(arg(a, 0), LinkedHashMap.class, true);
                int n = 0;
                for (int k = 1; k + 1 < a.size(); k += 2) if (h.put(a.get(k), a.get(k + 1)) == null) n++;
                return cmd.equals("HMSET") ? "OK" : "(integer) " + n;
            }
            case "HGET": { Map<String, String> h = (Map<String, String>) pegar(arg(a, 0), LinkedHashMap.class, false); String v = h == null ? null : h.get(arg(a, 1)); return v == null ? "(nil)" : aspas(v); }
            case "HGETALL": {
                Map<String, String> h = (Map<String, String>) pegar(arg(a, 0), LinkedHashMap.class, false);
                List<String> r = new ArrayList<>();
                if (h != null) for (Map.Entry<String, String> e : h.entrySet()) { r.add(aspas(e.getKey())); r.add(aspas(e.getValue())); }
                return numerada(r);
            }
            case "HDEL": { Map<String, String> h = (Map<String, String>) pegar(arg(a, 0), LinkedHashMap.class, false); int n = 0; if (h != null) for (String c : a.subList(1, a.size())) if (h.remove(c) != null) n++; return "(integer) " + n; }
            case "HKEYS": case "HVALS": {
                Map<String, String> h = (Map<String, String>) pegar(arg(a, 0), LinkedHashMap.class, false);
                List<String> r = new ArrayList<>();
                if (h != null) for (String v : cmd.equals("HKEYS") ? h.keySet() : h.values()) r.add(aspas(v));
                return numerada(r);
            }
            case "HLEN": { Map<String, String> h = (Map<String, String>) pegar(arg(a, 0), LinkedHashMap.class, false); return "(integer) " + (h == null ? 0 : h.size()); }
            case "HEXISTS": { Map<String, String> h = (Map<String, String>) pegar(arg(a, 0), LinkedHashMap.class, false); return "(integer) " + (h != null && h.containsKey(arg(a, 1)) ? 1 : 0); }
            case "HINCRBY": {
                Map<String, String> h = (Map<String, String>) pegar(arg(a, 0), LinkedHashMap.class, true);
                long v = Long.parseLong(h.getOrDefault(arg(a, 1), "0")) + Long.parseLong(arg(a, 2));
                h.put(a.get(1), String.valueOf(v));
                return "(integer) " + v;
            }

            // ----- sorted sets (rankings) -----
            case "ZADD": {
                TreeMap<String, Double> z = (TreeMap<String, Double>) pegar(arg(a, 0), TreeMap.class, true);
                int n = 0;
                for (int k = 1; k + 1 < a.size(); k += 2) if (z.put(a.get(k + 1), Double.parseDouble(a.get(k))) == null) n++;
                return "(integer) " + n;
            }
            case "ZINCRBY": {
                TreeMap<String, Double> z = (TreeMap<String, Double>) pegar(arg(a, 0), TreeMap.class, true);
                double v = z.getOrDefault(arg(a, 2), 0.0) + Double.parseDouble(arg(a, 1));
                z.put(a.get(2), v);
                return aspas(numero(v));
            }
            case "ZSCORE": { TreeMap<String, Double> z = (TreeMap<String, Double>) pegar(arg(a, 0), TreeMap.class, false); Double v = z == null ? null : z.get(arg(a, 1)); return v == null ? "(nil)" : aspas(numero(v)); }
            case "ZCARD": { TreeMap<String, Double> z = (TreeMap<String, Double>) pegar(arg(a, 0), TreeMap.class, false); return "(integer) " + (z == null ? 0 : z.size()); }
            case "ZREM": { TreeMap<String, Double> z = (TreeMap<String, Double>) pegar(arg(a, 0), TreeMap.class, false); int n = 0; if (z != null) for (String m : a.subList(1, a.size())) if (z.remove(m) != null) n++; return "(integer) " + n; }
            case "ZRANGE": case "ZREVRANGE": {
                TreeMap<String, Double> z = (TreeMap<String, Double>) pegar(arg(a, 0), TreeMap.class, false);
                if (z == null) return "(empty array)";
                List<Map.Entry<String, Double>> ord = ordenado(z, cmd.equals("ZREVRANGE") || a.stream().anyMatch(x -> x.equalsIgnoreCase("REV")));
                int[] f = faixa(ord.size(), arg(a, 1), arg(a, 2));
                boolean comNota = a.stream().anyMatch(x -> x.equalsIgnoreCase("WITHSCORES"));
                List<String> r = new ArrayList<>();
                for (int k = f[0]; k <= f[1]; k++) { r.add(aspas(ord.get(k).getKey())); if (comNota) r.add(aspas(numero(ord.get(k).getValue()))); }
                return numerada(r);
            }
            case "ZRANK": case "ZREVRANK": {
                TreeMap<String, Double> z = (TreeMap<String, Double>) pegar(arg(a, 0), TreeMap.class, false);
                if (z == null || !z.containsKey(arg(a, 1))) return "(nil)";
                List<Map.Entry<String, Double>> ord = ordenado(z, cmd.equals("ZREVRANK"));
                for (int k = 0; k < ord.size(); k++) if (ord.get(k).getKey().equals(a.get(1))) return "(integer) " + k;
                return "(nil)";
            }
            default: throw new RuntimeException("ERR comando desconhecido '" + p.get(0) + "'");
        }
    }

    // ---------- ajudantes ----------

    private void limparExpiradas() {
        long agora = System.currentTimeMillis();
        expira.entrySet().removeIf(e -> {
            if (e.getValue() <= agora) { dados.remove(e.getKey()); return true; }
            return false;
        });
    }

    private Object pegar(String chave, Class<?> tipo, boolean criar) {
        Object v = dados.get(chave);
        if (v == null) {
            if (!criar) return null;
            try { v = tipo.getDeclaredConstructor().newInstance(); } catch (Exception e) { throw new RuntimeException(e); }
            dados.put(chave, v);
        }
        if (!tipo.isInstance(v)) throw new RuntimeException("WRONGTYPE essa chave guarda outro tipo de valor (" + tipo(v) + ")");
        return v;
    }

    private long somar(String chave, long quanto) {
        Object v = dados.getOrDefault(chave, "0");
        long n;
        try { n = Long.parseLong(texto(v, "INCR")); } catch (NumberFormatException e) { throw new RuntimeException("ERR o valor não é um número inteiro"); }
        n += quanto;
        dados.put(chave, String.valueOf(n));
        return n;
    }

    private String texto(Object v, String cmd) {
        if (!(v instanceof String)) throw new RuntimeException("WRONGTYPE " + cmd + " só funciona com texto, e essa chave é " + tipo(v));
        return (String) v;
    }

    private static String tipo(Object v) {
        if (v instanceof LinkedList) return "list";
        if (v instanceof Set) return "set";
        if (v instanceof TreeMap) return "zset";
        if (v instanceof Map) return "hash";
        return "string";
    }

    private static List<Map.Entry<String, Double>> ordenado(TreeMap<String, Double> z, boolean reverso) {
        List<Map.Entry<String, Double>> l = new ArrayList<>(z.entrySet());
        l.sort((x, y) -> { int c = Double.compare(x.getValue(), y.getValue()); return c != 0 ? c : x.getKey().compareTo(y.getKey()); });
        if (reverso) java.util.Collections.reverse(l);
        return l;
    }

    private static int[] faixa(int tamanho, String ini, String fim) {
        int a = Integer.parseInt(ini), b = Integer.parseInt(fim);
        if (a < 0) a += tamanho;
        if (b < 0) b += tamanho;
        return new int[]{Math.max(0, a), Math.min(tamanho - 1, b)};
    }

    private static String numerada(List<String> itens) {
        if (itens.isEmpty()) return "(empty array)";
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < itens.size(); k++) sb.append(k == 0 ? "" : "\n").append(k + 1).append(") ").append(itens.get(k));
        return sb.toString();
    }

    private static String numero(double d) {
        return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d);
    }

    private static String aspas(String s) {
        return "\"" + s + "\"";
    }

    private static String arg(List<String> a, int k) {
        if (k >= a.size()) throw new RuntimeException("ERR faltam argumentos nesse comando");
        return a.get(k);
    }

    /** Separa a linha em pedaços, respeitando "textos com espaço". */
    static List<String> partir(String linha) {
        List<String> r = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        char aspas = 0;
        boolean temAlgo = false;
        for (char c : linha.toCharArray()) {
            if (aspas != 0) {
                if (c == aspas) aspas = 0; else atual.append(c);
            } else if (c == '"' || c == '\'') {
                aspas = c;
                temAlgo = true;
            } else if (Character.isWhitespace(c)) {
                if (temAlgo || atual.length() > 0) { r.add(atual.toString()); atual.setLength(0); temAlgo = false; }
            } else {
                atual.append(c);
            }
        }
        if (temAlgo || atual.length() > 0) r.add(atual.toString());
        return r;
    }
}
