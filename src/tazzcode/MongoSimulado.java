package tazzcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * MONGODB SIMULADO: um banco de DOCUMENTOS. Em vez de tabelas com colunas fixas,
 * ele tem COLEÇÕES de documentos JSON (cada documento pode ter campos diferentes!).
 *
 * Entende (do jeito do "mongosh"):
 *   use escola / show collections / show dbs
 *   db.alunos.insertOne({ nome: 'João', idade: 12 })
 *   db.alunos.insertMany([{...}, {...}])
 *   db.alunos.find({ idade: { $gte: 12 } }, { nome: 1 }).sort({ nome: 1 }).limit(5)
 *   db.alunos.findOne(...)  countDocuments(...)  distinct('campo')
 *   db.alunos.updateOne/updateMany(filtro, { $set: {...}, $inc: {...}, $push: {...} })
 *   db.alunos.deleteOne/deleteMany(filtro)   db.alunos.drop()
 *   db.alunos.aggregate([{ $match }, { $group }, { $sort }, { $limit }, { $project }])
 *   print(...)
 */
public class MongoSimulado implements MotorBanco {

    private final Map<String, List<Map<String, Object>>> colecoes = new LinkedHashMap<>();
    private final Random sorte = new Random();
    private String banco = "test";
    private Consumer<String> msg = s -> { };
    private Consumer<Sql.Resultado> res = r -> { };

    // ---------- leitor ----------
    private String s;
    private int i;

    @Override
    public void executar(String codigo, Consumer<String> mensagens, Consumer<Sql.Resultado> resultados) {
        this.msg = mensagens;
        this.res = resultados;
        this.s = codigo;
        this.i = 0;
        while (true) {
            espacos();
            if (i >= s.length()) break;
            if (s.charAt(i) == ';') { i++; continue; }
            int inicio = i;
            try {
                comando();
            } catch (RuntimeException e) {
                mensagens.accept("ERRO: " + e.getMessage());
                int fimLinha = s.indexOf('\n', Math.max(i, inicio + 1));
                i = fimLinha < 0 ? s.length() : fimLinha + 1;   // pula pra próxima linha
            }
        }
    }

    @Override public void limpar() { colecoes.clear(); }

    @Override
    public List<String[]> itens() {
        List<String[]> l = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> e : colecoes.entrySet()) l.add(new String[]{e.getKey(), String.valueOf(e.getValue().size())});
        return l;
    }

    @Override public String comandoParaVer(String item) { return "db." + item + ".find()"; }
    @Override public String tituloItens() { return "COLEÇÕES"; }
    @Override public String prompt() { return banco + ">"; }

    // =====================================================================
    //  COMANDOS
    // =====================================================================

    private void comando() {
        if (palavraAqui("use")) { i += 3; banco = palavra(); msg.accept("switched to db " + banco); return; }
        if (palavraAqui("show")) {
            i += 4; espacos();
            String o = palavra();
            if (o.equals("collections") || o.equals("tables")) {
                if (colecoes.isEmpty()) msg.accept("(nenhuma coleção ainda)");
                for (String c : colecoes.keySet()) msg.accept(c);
            } else {
                msg.accept(banco + "  (simulado)");
            }
            return;
        }
        if (palavraAqui("print") || palavraAqui("printjson") || s.startsWith("console.log", i)) {
            palavraComPontos();
            espera('(');
            List<Object> args = argumentos();
            StringBuilder sb = new StringBuilder();
            for (Object a : args) sb.append(a instanceof String ? a : Json.bonito(a)).append(' ');
            msg.accept(sb.toString().trim());
            return;
        }
        if (palavraAqui("var") || palavraAqui("let") || palavraAqui("const")) {
            throw new RuntimeException("variáveis (var/let/const) ainda não funcionam no Mongo simulado. Use db.colecao... direto.");
        }
        if (!s.startsWith("db.", i)) throw new RuntimeException("não entendi: comece com db.colecao.algumaCoisa(...)");
        i += 3;
        String nomeOuMetodo = palavra();
        espacos();
        if (i < s.length() && s.charAt(i) == '(') {                 // db.metodo()
            i++;
            List<Object> args = argumentos();
            metodoDoBanco(nomeOuMetodo, args);
            return;
        }
        String colecao = nomeOuMetodo;
        espera('.');
        String metodo = palavra();
        espera('(');
        List<Object> args = argumentos();
        metodoDaColecao(colecao, metodo, args);
    }

    private void metodoDoBanco(String m, List<Object> args) {
        switch (m) {
            case "getCollectionNames": msg.accept(Json.escrever(new ArrayList<>(colecoes.keySet()))); break;
            case "createCollection": colecoes.putIfAbsent(texto(args, 0), new ArrayList<>()); msg.accept("{ ok: 1 }"); break;
            case "dropDatabase": colecoes.clear(); msg.accept("{ ok: 1, dropped: '" + banco + "' }"); break;
            case "getName": msg.accept(banco); break;
            case "stats": msg.accept("{ db: '" + banco + "', collections: " + colecoes.size() + " }"); break;
            default: throw new RuntimeException("não conheço db." + m + "()");
        }
    }

    @SuppressWarnings("unchecked")
    private void metodoDaColecao(String nome, String metodo, List<Object> args) {
        List<Map<String, Object>> col = colecoes.computeIfAbsent(nome, k -> new ArrayList<>());
        Map<String, Object> filtro = args.isEmpty() || !(args.get(0) instanceof Map) ? new LinkedHashMap<>() : objeto(args.get(0));
        switch (metodo) {
            case "insertOne": case "insert": case "save": {
                if (!args.isEmpty() && args.get(0) instanceof List) { inserirVarios(col, (List<Object>) args.get(0)); break; }
                Map<String, Object> doc = copia(objeto(args.get(0)));
                Object id = inserir(col, doc);
                msg.accept("{ acknowledged: true, insertedId: " + Json.escrever(id) + " }");
                break;
            }
            case "insertMany": inserirVarios(col, (List<Object>) args.get(0)); break;
            case "find": case "findOne": {
                Map<String, Object> projecao = args.size() > 1 ? objeto(args.get(1)) : null;
                List<Map<String, Object>> achados = new ArrayList<>();
                for (Map<String, Object> d : col) if (bate(d, filtro)) achados.add(d);
                // .sort() .limit() .skip() .count() .pretty() .toArray()
                Map<String, Object> ordem = null;
                int limite = metodo.equals("findOne") ? 1 : Integer.MAX_VALUE, pular = 0;
                boolean contar = false;
                while (true) {
                    espacos();
                    if (i >= s.length() || s.charAt(i) != '.') break;
                    i++;
                    String m = palavra();
                    espera('(');
                    List<Object> a = argumentos();
                    switch (m) {
                        case "sort": ordem = objeto(a.get(0)); break;
                        case "limit": limite = (int) Sql.numero(a.get(0)).longValue(); if (limite == 0) limite = Integer.MAX_VALUE; break;
                        case "skip": pular = (int) Sql.numero(a.get(0)).longValue(); break;
                        case "count": case "itcount": contar = true; break;
                        default: break; // pretty(), toArray()...
                    }
                }
                if (ordem != null) ordenar(achados, ordem);
                List<Map<String, Object>> fatia = new ArrayList<>();
                for (int k = pular; k < achados.size() && fatia.size() < limite; k++) fatia.add(projetar(achados.get(k), projecao));
                if (contar) { msg.accept(String.valueOf(fatia.size())); break; }
                if (metodo.equals("findOne") && fatia.isEmpty()) { msg.accept("null"); break; }
                mostrar("db." + nome + "." + metodo + "()", fatia);
                break;
            }
            case "countDocuments": case "count": case "estimatedDocumentCount": {
                int n = 0;
                for (Map<String, Object> d : col) if (bate(d, filtro)) n++;
                msg.accept(String.valueOf(n));
                break;
            }
            case "distinct": {
                String campo = texto(args, 0);
                Map<String, Object> f = args.size() > 1 ? objeto(args.get(1)) : new LinkedHashMap<>();
                Set<Object> vistos = new LinkedHashSet<>();
                for (Map<String, Object> d : col) if (bate(d, f)) { Object v = pegar(d, campo); if (v != null) vistos.add(v); }
                msg.accept(Json.escrever(new ArrayList<>(vistos)));
                break;
            }
            case "updateOne": case "updateMany": case "replaceOne": {
                Map<String, Object> mudanca = objeto(args.get(1));
                boolean upsert = args.size() > 2 && Boolean.TRUE.equals(objeto(args.get(2)).get("upsert"));
                int achados = 0, mudados = 0;
                for (Map<String, Object> d : col) {
                    if (!bate(d, filtro)) continue;
                    achados++;
                    String antes = Json.escrever(d);
                    if (metodo.equals("replaceOne")) { Object id = d.get("_id"); d.clear(); d.put("_id", id); d.putAll(copia(mudanca)); }
                    else aplicarUpdate(d, mudanca);
                    if (!antes.equals(Json.escrever(d))) mudados++;
                    if (!metodo.equals("updateMany")) break;
                }
                if (achados == 0 && upsert) {
                    Map<String, Object> novo = new LinkedHashMap<>();
                    for (Map.Entry<String, Object> e : filtro.entrySet()) if (!e.getKey().startsWith("$") && !(e.getValue() instanceof Map)) novo.put(e.getKey(), e.getValue());
                    aplicarUpdate(novo, mudanca);
                    Object id = inserir(col, novo);
                    msg.accept("{ acknowledged: true, matchedCount: 0, modifiedCount: 0, upsertedId: " + Json.escrever(id) + " }");
                    break;
                }
                msg.accept("{ acknowledged: true, matchedCount: " + achados + ", modifiedCount: " + mudados + " }");
                break;
            }
            case "deleteOne": case "deleteMany": case "remove": {
                int apagados = 0;
                for (int k = 0; k < col.size(); k++) {
                    if (bate(col.get(k), filtro)) {
                        col.remove(k--);
                        apagados++;
                        if (metodo.equals("deleteOne")) break;
                    }
                }
                msg.accept("{ acknowledged: true, deletedCount: " + apagados + " }");
                break;
            }
            case "drop": colecoes.remove(nome); msg.accept("true"); break;
            case "aggregate": mostrar("db." + nome + ".aggregate()", agregar(col, (List<Object>) args.get(0))); break;
            case "createIndex": msg.accept("'" + String.join("_", objeto(args.get(0)).keySet()) + "_1' (índice simulado)"); break;
            default: throw new RuntimeException("não conheço o método " + metodo + "()");
        }
    }

    private void inserirVarios(List<Map<String, Object>> col, List<Object> docs) {
        List<Object> ids = new ArrayList<>();
        for (Object d : docs) ids.add(inserir(col, copia(objeto(d))));
        msg.accept("{ acknowledged: true, insertedIds: " + Json.escrever(ids) + " }");
    }

    private Object inserir(List<Map<String, Object>> col, Map<String, Object> doc) {
        if (!doc.containsKey("_id")) {
            Map<String, Object> comId = new LinkedHashMap<>();
            comId.put("_id", novoId());
            comId.putAll(doc);
            doc = comId;
        }
        for (Map<String, Object> d : col) {
            if (Sql.comparar(d.get("_id"), doc.get("_id")) == 0) throw new RuntimeException("E11000 chave duplicada: _id " + Json.escrever(doc.get("_id")));
        }
        col.add(doc);
        return doc.get("_id");
    }

    private String novoId() {
        StringBuilder sb = new StringBuilder(Long.toHexString(System.currentTimeMillis() / 1000));
        while (sb.length() < 24) sb.append(Integer.toHexString(sorte.nextInt(16)));
        return sb.toString();
    }

    // =====================================================================
    //  FILTROS ({ idade: { $gte: 12 }, nome: /jo/i, $or: [...] })
    // =====================================================================

    @SuppressWarnings("unchecked")
    private boolean bate(Map<String, Object> doc, Map<String, Object> filtro) {
        for (Map.Entry<String, Object> e : filtro.entrySet()) {
            String k = e.getKey();
            Object cond = e.getValue();
            switch (k) {
                case "$and": for (Object f : (List<Object>) cond) if (!bate(doc, objeto(f))) return false; continue;
                case "$or": { boolean algum = false; for (Object f : (List<Object>) cond) if (bate(doc, objeto(f))) { algum = true; break; } if (!algum) return false; continue; }
                case "$nor": for (Object f : (List<Object>) cond) if (bate(doc, objeto(f))) return false; continue;
                default: break;
            }
            Object valor = pegar(doc, k);
            boolean existe = existe(doc, k);
            if (cond instanceof Map && !((Map<String, Object>) cond).isEmpty() && ((Map<String, Object>) cond).keySet().iterator().next().startsWith("$")) {
                if (!operadores(valor, existe, (Map<String, Object>) cond)) return false;
            } else if (!igual(valor, cond)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean operadores(Object v, boolean existe, Map<String, Object> ops) {
        for (Map.Entry<String, Object> op : ops.entrySet()) {
            Object x = op.getValue();
            boolean ok;
            switch (op.getKey()) {
                case "$eq": ok = igual(v, x); break;
                case "$ne": ok = !igual(v, x); break;
                case "$gt": ok = v != null && x != null && compararVal(v, x) > 0; break;
                case "$gte": ok = v != null && x != null && compararVal(v, x) >= 0; break;
                case "$lt": ok = v != null && x != null && compararVal(v, x) < 0; break;
                case "$lte": ok = v != null && x != null && compararVal(v, x) <= 0; break;
                case "$in": ok = false; for (Object y : (List<Object>) x) if (igual(v, y)) { ok = true; break; } break;
                case "$nin": ok = true; for (Object y : (List<Object>) x) if (igual(v, y)) { ok = false; break; } break;
                case "$exists": ok = existe == Sql.verdade(x); break;
                case "$regex": {
                    String opcoes = ops.get("$options") == null ? "" : ops.get("$options").toString();
                    Pattern p = x instanceof Pattern ? (Pattern) x : Pattern.compile(x.toString(), opcoes.contains("i") ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0);
                    ok = v != null && p.matcher(Sql.texto(v)).find();
                    break;
                }
                case "$options": ok = true; break;
                case "$size": ok = v instanceof List && ((List<Object>) v).size() == Sql.numero(x).intValue(); break;
                case "$not": ok = !operadores(v, existe, objeto(x)); break;
                case "$all": ok = v instanceof List && ((List<Object>) v).containsAll((List<Object>) x); break;
                default: throw new RuntimeException("não conheço o operador " + op.getKey());
            }
            if (!ok) return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean igual(Object v, Object cond) {
        if (cond instanceof Pattern) return v != null && ((Pattern) cond).matcher(Sql.texto(v)).find();
        if (v instanceof List && !(cond instanceof List)) {          // campo é lista: basta conter
            for (Object item : (List<Object>) v) if (igual(item, cond)) return true;
            return false;
        }
        if (v == null || cond == null) return v == null && cond == null;
        if (v instanceof Map || v instanceof List) return Json.escrever(v).equals(Json.escrever(cond));
        return compararVal(v, cond) == 0 && (v instanceof Number) == (cond instanceof Number);
    }

    private int compararVal(Object a, Object b) {
        if (a instanceof String && b instanceof String) return ((String) a).compareTo((String) b);   // Mongo diferencia maiúsculas
        return Sql.comparar(a, b);
    }

    /** Pega "endereco.cidade" dentro do documento. */
    @SuppressWarnings("unchecked")
    private Object pegar(Map<String, Object> doc, String caminho) {
        Object atual = doc;
        for (String parte : caminho.split("\\.")) {
            if (atual instanceof Map) atual = ((Map<String, Object>) atual).get(parte);
            else if (atual instanceof List && parte.matches("\\d+")) {
                List<Object> l = (List<Object>) atual;
                int k = Integer.parseInt(parte);
                atual = k < l.size() ? l.get(k) : null;
            } else return null;
        }
        return atual;
    }

    @SuppressWarnings("unchecked")
    private boolean existe(Map<String, Object> doc, String caminho) {
        String[] partes = caminho.split("\\.");
        Object atual = doc;
        for (String parte : partes) {
            if (!(atual instanceof Map) || !((Map<String, Object>) atual).containsKey(parte)) return false;
            atual = ((Map<String, Object>) atual).get(parte);
        }
        return true;
    }

    // =====================================================================
    //  UPDATE ($set, $inc, $push...)
    // =====================================================================

    @SuppressWarnings("unchecked")
    private void aplicarUpdate(Map<String, Object> doc, Map<String, Object> mudanca) {
        boolean temOperador = mudanca.keySet().stream().anyMatch(k -> k.startsWith("$"));
        if (!temOperador) {   // updateOne(filtro, { nome: 'x' }) sem $set: o Mongo novo dá erro
            throw new RuntimeException("no update, use operadores: { $set: { campo: valor } }");
        }
        for (Map.Entry<String, Object> op : mudanca.entrySet()) {
            Map<String, Object> campos = objeto(op.getValue());
            for (Map.Entry<String, Object> c : campos.entrySet()) {
                String k = c.getKey();
                Object v = c.getValue();
                Object atual = pegar(doc, k);
                switch (op.getKey()) {
                    case "$set": colocar(doc, k, copiaValor(v)); break;
                    case "$unset": tirar(doc, k); break;
                    case "$inc": colocar(doc, k, atual == null ? v : somar(atual, v)); break;
                    case "$mul": colocar(doc, k, atual == null ? 0L : multiplicar(atual, v)); break;
                    case "$min": if (atual == null || Sql.comparar(v, atual) < 0) colocar(doc, k, v); break;
                    case "$max": if (atual == null || Sql.comparar(v, atual) > 0) colocar(doc, k, v); break;
                    case "$rename": { Object val = pegar(doc, k); tirar(doc, k); colocar(doc, v.toString(), val); break; }
                    case "$push": case "$addToSet": {
                        List<Object> l = atual instanceof List ? (List<Object>) atual : new ArrayList<>();
                        List<Object> novos = v instanceof Map && ((Map<String, Object>) v).containsKey("$each")
                                ? (List<Object>) ((Map<String, Object>) v).get("$each") : Collections.singletonList(v);
                        for (Object n : novos) {
                            boolean jaTem = false;
                            for (Object x : l) if (igual(x, n)) jaTem = true;
                            if (op.getKey().equals("$push") || !jaTem) l.add(copiaValor(n));
                        }
                        colocar(doc, k, l);
                        break;
                    }
                    case "$pull": if (atual instanceof List) ((List<Object>) atual).removeIf(x -> v instanceof Map ? x instanceof Map && bate((Map<String, Object>) x, (Map<String, Object>) v) : igual(x, v)); break;
                    case "$pop": if (atual instanceof List && !((List<Object>) atual).isEmpty()) { List<Object> l = (List<Object>) atual; l.remove(Sql.numero(v).intValue() < 0 ? 0 : l.size() - 1); } break;
                    default: throw new RuntimeException("não conheço o operador " + op.getKey());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void colocar(Map<String, Object> doc, String caminho, Object valor) {
        String[] partes = caminho.split("\\.");
        Map<String, Object> atual = doc;
        for (int k = 0; k < partes.length - 1; k++) {
            Object prox = atual.get(partes[k]);
            if (!(prox instanceof Map)) { prox = new LinkedHashMap<String, Object>(); atual.put(partes[k], prox); }
            atual = (Map<String, Object>) prox;
        }
        atual.put(partes[partes.length - 1], valor);
    }

    @SuppressWarnings("unchecked")
    private void tirar(Map<String, Object> doc, String caminho) {
        String[] partes = caminho.split("\\.");
        Object atual = doc;
        for (int k = 0; k < partes.length - 1; k++) {
            if (!(atual instanceof Map)) return;
            atual = ((Map<String, Object>) atual).get(partes[k]);
        }
        if (atual instanceof Map) ((Map<String, Object>) atual).remove(partes[partes.length - 1]);
    }

    private Object somar(Object a, Object b) {
        Number x = Sql.numero(a), y = Sql.numero(b);
        if (x instanceof Long && y instanceof Long) return x.longValue() + y.longValue();
        return x.doubleValue() + y.doubleValue();
    }

    private Object multiplicar(Object a, Object b) {
        Number x = Sql.numero(a), y = Sql.numero(b);
        if (x instanceof Long && y instanceof Long) return x.longValue() * y.longValue();
        return x.doubleValue() * y.doubleValue();
    }

    // =====================================================================
    //  ORDENAR, PROJETAR e AGREGAR
    // =====================================================================

    private void ordenar(List<Map<String, Object>> docs, Map<String, Object> ordem) {
        docs.sort((a, b) -> {
            for (Map.Entry<String, Object> e : ordem.entrySet()) {
                int c = Sql.comparar(pegar(a, e.getKey()), pegar(b, e.getKey()));
                if (c != 0) return Sql.numero(e.getValue()).intValue() < 0 ? -c : c;
            }
            return 0;
        });
    }

    private Map<String, Object> projetar(Map<String, Object> doc, Map<String, Object> proj) {
        if (proj == null || proj.isEmpty()) return doc;
        boolean incluir = proj.entrySet().stream().anyMatch(e -> !e.getKey().equals("_id") && Sql.verdade(e.getValue()));
        Map<String, Object> r = new LinkedHashMap<>();
        if (incluir) {
            if (!proj.containsKey("_id") || Sql.verdade(proj.get("_id"))) r.put("_id", doc.get("_id"));
            for (Map.Entry<String, Object> e : proj.entrySet()) {
                if (e.getKey().equals("_id")) continue;
                if (e.getValue() instanceof String && e.getValue().toString().startsWith("$")) r.put(e.getKey(), pegar(doc, e.getValue().toString().substring(1)));
                else if (Sql.verdade(e.getValue()) && existe(doc, e.getKey())) r.put(e.getKey(), pegar(doc, e.getKey()));
            }
        } else {
            r.putAll(doc);
            for (String k : proj.keySet()) r.remove(k);
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> agregar(List<Map<String, Object>> col, List<Object> etapas) {
        List<Map<String, Object>> docs = new ArrayList<>(col);
        for (Object etapaObj : etapas) {
            Map<String, Object> etapa = objeto(etapaObj);
            String tipo = etapa.keySet().iterator().next();
            Object arg = etapa.get(tipo);
            switch (tipo) {
                case "$match": { List<Map<String, Object>> f = new ArrayList<>(); for (Map<String, Object> d : docs) if (bate(d, objeto(arg))) f.add(d); docs = f; break; }
                case "$sort": ordenar(docs, objeto(arg)); break;
                case "$limit": docs = new ArrayList<>(docs.subList(0, Math.min(docs.size(), Sql.numero(arg).intValue()))); break;
                case "$skip": docs = new ArrayList<>(docs.subList(Math.min(docs.size(), Sql.numero(arg).intValue()), docs.size())); break;
                case "$project": { List<Map<String, Object>> p = new ArrayList<>(); for (Map<String, Object> d : docs) p.add(projetar(d, objeto(arg))); docs = p; break; }
                case "$count": { Map<String, Object> m = new LinkedHashMap<>(); m.put(arg.toString(), (long) docs.size()); docs = new ArrayList<>(Collections.singletonList(m)); break; }
                case "$group": docs = agrupar(docs, objeto(arg)); break;
                case "$unwind": {
                    String campo = arg.toString().replace("$", "");
                    List<Map<String, Object>> u = new ArrayList<>();
                    for (Map<String, Object> d : docs) {
                        Object v = pegar(d, campo);
                        if (v instanceof List) for (Object item : (List<Object>) v) { Map<String, Object> c = new LinkedHashMap<>(d); c.put(campo, item); u.add(c); }
                    }
                    docs = u;
                    break;
                }
                default: throw new RuntimeException("não conheço a etapa " + tipo);
            }
        }
        return docs;
    }

    private List<Map<String, Object>> agrupar(List<Map<String, Object>> docs, Map<String, Object> spec) {
        Object idExpr = spec.get("_id");
        Map<String, List<Map<String, Object>>> grupos = new LinkedHashMap<>();
        Map<String, Object> chaves = new LinkedHashMap<>();
        for (Map<String, Object> d : docs) {
            Object chave = valorDe(d, idExpr);
            String k = Json.escrever(chave);
            grupos.computeIfAbsent(k, x -> new ArrayList<>()).add(d);
            chaves.putIfAbsent(k, chave);
        }
        List<Map<String, Object>> saida = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> g : grupos.entrySet()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("_id", chaves.get(g.getKey()));
            for (Map.Entry<String, Object> campo : spec.entrySet()) {
                if (campo.getKey().equals("_id")) continue;
                Map<String, Object> acc = objeto(campo.getValue());
                String op = acc.keySet().iterator().next();
                Object expr = acc.get(op);
                Object total = null;
                List<Object> valores = new ArrayList<>();
                for (Map<String, Object> d : g.getValue()) valores.add(valorDe(d, expr));
                switch (op) {
                    case "$sum": total = 0L; for (Object v : valores) if (v instanceof Number) total = somar(total, v); break;
                    case "$avg": { double s = 0; int n = 0; for (Object v : valores) if (v instanceof Number) { s += ((Number) v).doubleValue(); n++; } total = n == 0 ? null : s / n; break; }
                    case "$min": for (Object v : valores) if (v != null && (total == null || Sql.comparar(v, total) < 0)) total = v; break;
                    case "$max": for (Object v : valores) if (v != null && (total == null || Sql.comparar(v, total) > 0)) total = v; break;
                    case "$first": total = valores.isEmpty() ? null : valores.get(0); break;
                    case "$last": total = valores.isEmpty() ? null : valores.get(valores.size() - 1); break;
                    case "$push": total = valores; break;
                    case "$addToSet": total = new ArrayList<>(new LinkedHashSet<>(valores)); break;
                    case "$count": total = (long) valores.size(); break;
                    default: throw new RuntimeException("não conheço o acumulador " + op);
                }
                r.put(campo.getKey(), total);
            }
            saida.add(r);
        }
        return saida;
    }

    /** "$campo" vira o valor do campo; o resto é valor fixo (ex.: { $sum: 1 }). */
    @SuppressWarnings("unchecked")
    private Object valorDe(Map<String, Object> d, Object expr) {
        if (expr instanceof String && ((String) expr).startsWith("$")) return pegar(d, ((String) expr).substring(1));
        if (expr instanceof Map) {
            Map<String, Object> r = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : ((Map<String, Object>) expr).entrySet()) r.put(e.getKey(), valorDe(d, e.getValue()));
            return r;
        }
        return expr;
    }

    // =====================================================================
    //  MOSTRAR RESULTADOS (vira tabela: cada campo vira uma coluna)
    // =====================================================================

    private void mostrar(String titulo, List<Map<String, Object>> docs) {
        List<String> colunas = new ArrayList<>();
        for (Map<String, Object> d : docs) for (String k : d.keySet()) if (!colunas.contains(k)) colunas.add(k);
        List<Object[]> linhas = new ArrayList<>();
        for (Map<String, Object> d : docs) {
            Object[] l = new Object[colunas.size()];
            for (int k = 0; k < l.length; k++) {
                Object v = d.get(colunas.get(k));
                l[k] = (v instanceof Map || v instanceof List) ? Json.escrever(v) : (d.containsKey(colunas.get(k)) ? v : "");
            }
            linhas.add(l);
        }
        if (colunas.isEmpty()) colunas.add("(nenhum documento)");
        res.accept(new Sql.Resultado(titulo, colunas, linhas));
    }

    // =====================================================================
    //  AJUDANTES DO LEITOR
    // =====================================================================

    private List<Object> argumentos() {
        List<Object> args = new ArrayList<>();
        espacos();
        if (i < s.length() && s.charAt(i) == ')') { i++; return args; }
        while (true) {
            Object[] lido;
            try {
                lido = Json.lerPedaco(s, i);
            } catch (Json.Erro e) {
                throw new RuntimeException("erro no JSON: " + e.getMessage());
            }
            args.add(lido[0]);
            i = (Integer) lido[1];
            espacos();
            if (i < s.length() && s.charAt(i) == ',') { i++; continue; }
            espera(')');
            return args;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> objeto(Object o) {
        if (o == null) return new LinkedHashMap<>();
        if (!(o instanceof Map)) throw new RuntimeException("esperava um objeto { ... } mas veio " + Json.escrever(o));
        return (Map<String, Object>) o;
    }

    private String texto(List<Object> args, int k) {
        if (args.size() <= k) throw new RuntimeException("faltou um argumento");
        return String.valueOf(args.get(k));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> copia(Map<String, Object> m) {
        return (Map<String, Object>) copiaValor(m);
    }

    @SuppressWarnings("unchecked")
    private Object copiaValor(Object v) {
        if (v instanceof Map) {
            Map<String, Object> c = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : ((Map<String, Object>) v).entrySet()) c.put(e.getKey(), copiaValor(e.getValue()));
            return c;
        }
        if (v instanceof List) {
            List<Object> c = new ArrayList<>();
            for (Object x : (List<Object>) v) c.add(copiaValor(x));
            return c;
        }
        return v;
    }

    private void espacos() {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) i++;
            else if (s.startsWith("//", i)) { while (i < s.length() && s.charAt(i) != '\n') i++; }
            else if (s.startsWith("/*", i)) { int f = s.indexOf("*/", i + 2); i = f < 0 ? s.length() : f + 2; }
            else break;
        }
    }

    private boolean palavraAqui(String p) {
        return s.startsWith(p, i) && (i + p.length() >= s.length() || !Character.isLetterOrDigit(s.charAt(i + p.length())));
    }

    private String palavra() {
        espacos();
        int ini = i;
        while (i < s.length() && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_' || s.charAt(i) == '$')) i++;
        if (ini == i) throw new RuntimeException("esperava um nome aqui");
        return s.substring(ini, i);
    }

    private void palavraComPontos() {
        while (i < s.length() && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '.')) i++;
    }

    private void espera(char c) {
        espacos();
        if (i >= s.length() || s.charAt(i) != c) throw new RuntimeException("esperava \"" + c + "\"");
        i++;
    }
}
