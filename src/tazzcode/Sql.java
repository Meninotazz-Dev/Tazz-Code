package tazzcode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * O BANCO DE DADOS SIMULADO (de mentirinha!). Ele entende SQL de verdade, mas guarda tudo
 * na memória do TazzCode: quando você fecha o programa, os dados somem.
 *
 * Funciona assim:
 *   1) o "tokenizador" quebra o texto em pedacinhos (palavras, números, textos, símbolos)
 *   2) o "parser" lê os pedacinhos e entende o comando (SELECT, INSERT...)
 *   3) o comando é executado nas tabelas, que são listas de linhas na memória
 *
 * Entende: CREATE/DROP/ALTER/TRUNCATE TABLE, INSERT, SELECT (WHERE, JOIN, GROUP BY, HAVING,
 * ORDER BY, LIMIT/TOP, DISTINCT, COUNT/SUM/AVG/MIN/MAX, CASE), UPDATE, DELETE, SHOW TABLES, DESCRIBE.
 * T-SQL: PRINT, DECLARE @x, SET @x, IF/ELSE, WHILE, BEGIN/END, GO.
 * PL/SQL: DECLARE, BEGIN/END, :=, IF/ELSIF/ELSE, FOR..LOOP, WHILE..LOOP, DBMS_OUTPUT.PUT_LINE, DUAL.
 */
public class Sql implements MotorBanco {

    // Listas usadas pelas cores e pelo autocompletar
    public static final String PALAVRAS = "SELECT FROM WHERE INSERT INTO VALUES UPDATE SET DELETE CREATE TABLE DROP "
            + "ALTER ADD COLUMN TRUNCATE AND OR NOT NULL IS IN LIKE BETWEEN AS ORDER BY GROUP HAVING LIMIT OFFSET ASC "
            + "DESC DISTINCT JOIN INNER LEFT RIGHT OUTER ON PRIMARY KEY FOREIGN REFERENCES AUTO_INCREMENT DEFAULT "
            + "UNIQUE IF EXISTS CASE WHEN THEN ELSE END SHOW TABLES DESCRIBE DATABASE USE TRUE FALSE ALL";
    public static final String FUNCOES = "COUNT SUM AVG MIN MAX UPPER LOWER LENGTH ROUND ABS CONCAT SUBSTRING SUBSTR "
            + "TRIM NOW REPLACE COALESCE IFNULL FLOOR CEIL SQRT POWER MOD RAND CAST";
    public static final String TIPOS = "INT INTEGER BIGINT SMALLINT DECIMAL NUMERIC FLOAT REAL DOUBLE VARCHAR CHAR TEXT "
            + "DATE DATETIME TIMESTAMP BOOLEAN BOOL SERIAL";

    /** Erro "amigável" do banco (a mensagem aparece pra pessoa). */
    public static class Erro extends RuntimeException {
        Erro(String msg) { super(msg); }
    }

    // =====================================================================
    //  AS TABELAS
    // =====================================================================

    public static class Coluna {
        public final String nome;
        public final String tipo;
        boolean chave, autoIncremento, naoNulo, unica;
        Expr padrao;
        Coluna(String nome, String tipo) { this.nome = nome; this.tipo = tipo; }
        boolean ehNumero() {
            String t = tipo.toUpperCase();
            return t.contains("INT") || t.contains("DEC") || t.contains("NUM") || t.contains("FLOAT")
                    || t.contains("REAL") || t.contains("DOUBLE") || t.equals("SERIAL") || t.equals("BIT");
        }
    }

    public static class Tabela {
        public final String nome;
        public final List<Coluna> colunas = new ArrayList<>();
        public final List<Object[]> linhas = new ArrayList<>();
        long proximoId = 1;
        Tabela(String nome) { this.nome = nome; }
        int indice(String coluna) {
            for (int i = 0; i < colunas.size(); i++) if (colunas.get(i).nome.equalsIgnoreCase(coluna)) return i;
            return -1;
        }
    }

    /** O resultado de um SELECT (vira uma tabelinha na janela). */
    public static class Resultado {
        public final String titulo;
        public final List<String> colunas;
        public final List<Object[]> linhas;
        Resultado(String titulo, List<String> colunas, List<Object[]> linhas) {
            this.titulo = titulo;
            this.colunas = colunas;
            this.linhas = linhas;
        }
    }

    private final Map<String, Tabela> tabelas = new LinkedHashMap<>();
    private final Map<String, Object> variaveis = new LinkedHashMap<>();
    private String dialeto = "SQL";
    private Consumer<String> mensagens = s -> { };
    private Consumer<Resultado> resultados = r -> { };
    private int passos;   // protege contra laço infinito (WHILE que nunca acaba)

    // ---------- MotorBanco ----------
    private String dialetoPadrao = "SQL";

    public Sql() { }

    public Sql(String dialeto) { this.dialetoPadrao = dialeto; }

    @Override
    public void executar(String codigo, Consumer<String> mensagens, Consumer<Resultado> resultados) {
        executar(codigo, dialetoPadrao, mensagens, resultados);
    }

    @Override
    public List<String[]> itens() {
        List<String[]> l = new ArrayList<>();
        for (Tabela t : tabelas.values()) l.add(new String[]{t.nome, String.valueOf(t.linhas.size())});
        return l;
    }

    @Override public String comandoParaVer(String item) { return "SELECT * FROM " + item + ";"; }
    @Override public String tituloItens() { return "TABELAS"; }
    @Override public String prompt() { return dialetoPadrao.equals("CQL") ? "cqlsh>" : dialetoPadrao.equals("SURREAL") ? "surreal>" : "SQL>"; }

    public List<Tabela> getTabelas() {
        return new ArrayList<>(tabelas.values());
    }

    public void limpar() {
        tabelas.clear();
        variaveis.clear();
    }

    /** Roda um script inteiro. Mensagens e resultados chegam pelos "ouvintes". */
    public void executar(String script, String dialeto, Consumer<String> mensagens, Consumer<Resultado> resultados) {
        this.dialeto = dialeto;
        this.mensagens = mensagens;
        this.resultados = resultados;
        this.passos = 0;
        variaveis.clear();

        // "GO" (SQL Server) e "/" (Oracle) sozinhos na linha separam comandos, igual ao ";".
        String preparado = Pattern.compile("^\\s*(GO|/)\\s*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
                .matcher(script).replaceAll(";");
        List<Token> tokens = new Tokenizador(preparado).tudo();
        Leitor leitor = new Leitor(tokens);
        leitor.script = preparado;
        while (!leitor.fim()) {
            if (leitor.aceita(";")) continue;
            int inicio = leitor.pos;
            try {
                leitor.comando(true);
            } catch (Erro e) {
                mensagens.accept("ERRO: " + e.getMessage());
                // pula o resto do comando com erro e continua no próximo
                boolean jaTerminou = leitor.pos > inicio && leitor.t.get(leitor.pos - 1).eh(";");
                if (!jaTerminou) {
                    leitor.pos = Math.max(leitor.pos, inicio + 1);
                    leitor.pularAte(";");
                }
            }
        }
    }

    // =====================================================================
    //  1) TOKENIZADOR
    // =====================================================================

    enum Tipo { PALAVRA, NUMERO, TEXTO, SIMBOLO, VARIAVEL, FIM }

    static class Token {
        final Tipo tipo;
        final String texto;
        int ini;   // onde o token começa no texto (usado pra ler JSON no meio do SQL)
        Token(Tipo tipo, String texto) { this.tipo = tipo; this.texto = texto; }
        boolean eh(String s) {
            return (tipo == Tipo.PALAVRA || tipo == Tipo.SIMBOLO) && texto.equalsIgnoreCase(s);
        }
        @Override public String toString() { return texto; }
    }

    static class Tokenizador {
        private final String s;
        private int i;
        Tokenizador(String s) { this.s = s; }

        List<Token> tudo() {
            List<Token> lista = new ArrayList<>();
            while (true) {
                pularEspacos();
                if (i >= s.length()) break;
                int tamanhoAntes = lista.size();
                int comeco = i;
                lerUm(lista);
                for (int k = tamanhoAntes; k < lista.size(); k++) lista.get(k).ini = comeco;
            }
            Token fim = new Token(Tipo.FIM, "");
            fim.ini = s.length();
            lista.add(fim);
            return lista;
        }

        private void lerUm(List<Token> lista) {
            {
                char c = s.charAt(i);
                if (Character.isLetter(c) || c == '_') {
                    int ini = i;
                    while (i < s.length() && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_' || s.charAt(i) == '$')) i++;
                    lista.add(new Token(Tipo.PALAVRA, s.substring(ini, i)));
                } else if (Character.isDigit(c) || (c == '.' && i + 1 < s.length() && Character.isDigit(s.charAt(i + 1)))) {
                    int ini = i;
                    while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
                    if (i + 1 < s.length() && s.charAt(i) == '.' && Character.isDigit(s.charAt(i + 1))) {
                        i++;
                        while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
                    } else if (i < s.length() && s.charAt(i) == '.' && !(i + 1 < s.length() && s.charAt(i + 1) == '.')) {
                        i++;
                    }
                    lista.add(new Token(Tipo.NUMERO, s.substring(ini, i)));
                } else if (c == '\'' || c == '"') {
                    lista.add(new Token(Tipo.TEXTO, lerTexto(c)));
                } else if (c == '`' || c == '[') {
                    char fecha = c == '[' ? ']' : '`';
                    int fim = s.indexOf(fecha, i + 1);
                    if (fim < 0) throw new Erro("faltou fechar o " + c);
                    lista.add(new Token(Tipo.PALAVRA, s.substring(i + 1, fim)));
                    i = fim + 1;
                } else if (c == '@' && i + 1 < s.length() && (Character.isLetter(s.charAt(i + 1)) || s.charAt(i + 1) == '_')) {
                    int ini = i++;
                    while (i < s.length() && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_')) i++;
                    lista.add(new Token(Tipo.VARIAVEL, s.substring(ini, i).toLowerCase()));
                } else {
                    String dois = i + 1 < s.length() ? s.substring(i, i + 2) : "";
                    if (dois.equals("<=") || dois.equals(">=") || dois.equals("<>") || dois.equals("!=")
                            || dois.equals("||") || dois.equals(":=") || dois.equals("..") || dois.equals("::")) {
                        lista.add(new Token(Tipo.SIMBOLO, dois));
                        i += 2;
                    } else {
                        lista.add(new Token(Tipo.SIMBOLO, String.valueOf(c)));
                        i++;
                    }
                }
            }
        }

        private void pularEspacos() {
            while (i < s.length()) {
                char c = s.charAt(i);
                if (Character.isWhitespace(c)) {
                    i++;
                } else if (s.startsWith("--", i) || c == '#') {
                    while (i < s.length() && s.charAt(i) != '\n') i++;
                } else if (s.startsWith("/*", i)) {
                    int fim = s.indexOf("*/", i + 2);
                    i = fim < 0 ? s.length() : fim + 2;
                } else {
                    break;
                }
            }
        }

        private String lerTexto(char aspas) {
            StringBuilder sb = new StringBuilder();
            i++;
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == aspas) {
                    if (i + 1 < s.length() && s.charAt(i + 1) == aspas) {   // '' vira '
                        sb.append(aspas);
                        i += 2;
                        continue;
                    }
                    i++;
                    return sb.toString();
                }
                sb.append(c);
                i++;
            }
            throw new Erro("faltou fechar um texto com " + aspas);
        }
    }

    // =====================================================================
    //  2) EXPRESSÕES (contas, comparações, funções...)
    // =====================================================================

    /** Onde uma expressão é calculada: a linha atual (e o grupo, se tiver GROUP BY). */
    static class Contexto {
        final Map<String, Object> linha;
        final List<Map<String, Object>> grupo;
        Contexto(Map<String, Object> linha, List<Map<String, Object>> grupo) {
            this.linha = linha;
            this.grupo = grupo;
        }
    }

    interface Expr {
        Object valor(Contexto c);
        default boolean temAgregacao() { return false; }
        default String nome() { return "?"; }
    }

    // =====================================================================
    //  3) O LEITOR (parser) + execução
    // =====================================================================

    class Leitor {
        final List<Token> t;
        int pos;
        String script = "";

        Leitor(List<Token> t) { this.t = t; }

        // ---------- ajudantes ----------
        Token atual()        { return t.get(pos); }
        Token proximo(int n) { return t.get(Math.min(pos + n, t.size() - 1)); }
        boolean fim()        { return atual().tipo == Tipo.FIM; }
        boolean ve(String s) { return atual().eh(s); }

        boolean aceita(String s) {
            if (ve(s)) { pos++; return true; }
            return false;
        }

        void espera(String s) {
            if (!aceita(s)) throw new Erro("esperava \"" + s + "\" mas achei \"" + (fim() ? "o fim" : atual().texto) + "\"");
        }

        String nome() {
            Token tk = atual();
            if (tk.tipo != Tipo.PALAVRA && tk.tipo != Tipo.TEXTO) throw new Erro("esperava um nome mas achei \"" + tk.texto + "\"");
            pos++;
            return tk.texto;
        }

        void pularAte(String s) {
            while (!fim() && !ve(s)) pos++;
            aceita(s);
        }

        void fimDoComando() {
            if (!aceita(";") && !fim() && !ve("END") && !ve("ELSE") && !ve("ELSIF")) {
                // No T-SQL o ";" é opcional: se o próximo token começa outro comando, tudo bem.
                if (!comecaComando()) throw new Erro("não entendi \"" + atual().texto + "\" (faltou um ; ?)");
            }
        }

        boolean comecaComando() {
            for (String c : new String[]{"SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP", "ALTER", "TRUNCATE",
                    "SHOW", "DESCRIBE", "DESC", "USE", "PRINT", "DECLARE", "SET", "IF", "WHILE", "BEGIN", "FOR", "WITH"}) {
                if (ve(c)) return true;
            }
            return false;
        }

        // ---------- COMANDOS ----------

        void comando(boolean exec) {
            if (++passos > 200000) throw new Erro("o programa rodou demais (laço infinito?) e foi parado");
            // ----- SurrealDB (SurrealQL) -----
            if (dialeto.equals("SURREAL")) {
                if (ve("CREATE") && !proximo(1).eh("TABLE") && !proximo(1).eh("DATABASE")) { pos++; surrealCriar(exec); return; }
                if (aceita("DEFINE")) {
                    if (aceita("TABLE")) { String n = nome(); while (!fim() && !ve(";")) pos++; fimDoComando(); if (exec) { tabelas.putIfAbsent(n.toLowerCase(), new Tabela(n)); mensagens.accept("Tabela \"" + n + "\" definida."); } return; }
                    while (!fim() && !ve(";")) pos++; fimDoComando(); if (exec) mensagens.accept("DEFINE aceito (simulado)."); return;
                }
                if (aceita("INFO")) { while (!fim() && !ve(";")) pos++; fimDoComando(); if (exec) mostrarTabelas(); return; }
            }
            // ----- Cassandra (CQL) -----
            if (ve("CREATE") && (proximo(1).eh("KEYSPACE") || proximo(1).eh("INDEX") || proximo(1).eh("TYPE"))) {
                pos += 2; aceita("IF"); aceita("NOT"); aceita("EXISTS");
                String n = nome();
                while (!fim() && !ve(";")) pos++;
                fimDoComando();
                if (exec) mensagens.accept("\"" + n + "\" criado (simulado).");
                return;
            }
            if (ve("SHOW") && (proximo(1).eh("DATABASES") || proximo(1).eh("SCHEMAS"))) {
                pos += 2; fimDoComando();
                if (exec) resultados.accept(new Resultado("SHOW DATABASES", java.util.Arrays.asList("banco"), new ArrayList<>(java.util.Collections.singletonList(new Object[]{"tazzcode (simulado)"}))));
                return;
            }
            if (ve("SELECT")) { Resultado r = select(exec); if (exec) resultados.accept(r); fimDoComando(); }
            else if (aceita("INSERT")) insert(exec);
            else if (aceita("UPDATE")) update(exec);
            else if (aceita("DELETE")) delete(exec);
            else if (aceita("CREATE")) create(exec);
            else if (aceita("DROP")) drop(exec);
            else if (aceita("ALTER")) alter(exec);
            else if (aceita("TRUNCATE")) truncate(exec);
            else if (aceita("SHOW")) { espera("TABLES"); if (exec) mostrarTabelas(); fimDoComando(); }
            else if (aceita("DESCRIBE") || aceita("DESC")) { String n = nome(); if (exec) descrever(n); fimDoComando(); }
            else if (aceita("USE")) { String n = nome(); if (exec) mensagens.accept("Usando o banco \"" + n + "\" (simulado)."); fimDoComando(); }
            else if (aceita("PRINT")) { Expr e = expressao(); if (exec) mensagens.accept(texto(e.valor(vazio()))); fimDoComando(); }
            else if (ve("DECLARE")) declare(exec);
            else if (aceita("SET")) setVariavel(exec);
            else if (ve("BEGIN")) bloco(exec);
            else if (ve("IF")) se(exec);
            else if (ve("WHILE")) enquanto(exec);
            else if (ve("FOR")) para(exec);
            else if (ve("DBMS_OUTPUT")) putLine(exec);
            else if (atual().tipo == Tipo.PALAVRA && proximo(1).eh(":=")) atribuicao(exec);
            else if (atual().tipo == Tipo.PALAVRA && atual().texto.equalsIgnoreCase("NULL")) { pos++; fimDoComando(); }
            else throw new Erro("não conheço o comando \"" + atual().texto + "\"");
        }

        // ----- CREATE -----
        void create(boolean exec) {
            if (aceita("DATABASE") || aceita("SCHEMA")) {
                String n = nome();
                if (exec) mensagens.accept("Banco \"" + n + "\" criado (simulado).");
                fimDoComando();
                return;
            }
            aceita("OR"); aceita("REPLACE");
            espera("TABLE");
            boolean seNaoExiste = false;
            if (aceita("IF")) { espera("NOT"); espera("EXISTS"); seNaoExiste = true; }
            String nomeTabela = nome();
            Tabela tab = new Tabela(nomeTabela);
            espera("(");
            do {
                if (aceita("PRIMARY")) {
                    espera("KEY"); espera("(");
                    int nivel = 1;   // aceita PRIMARY KEY (a, b) e o jeito do Cassandra: PRIMARY KEY ((a), b)
                    List<String> chaves = new ArrayList<>();
                    while (!fim() && nivel > 0) {
                        if (aceita("(")) nivel++;
                        else if (aceita(")")) nivel--;
                        else if (aceita(",")) { }
                        else chaves.add(nome());
                    }
                    if (chaves.size() == 1) { int i = tab.indice(chaves.get(0)); if (i >= 0) tab.colunas.get(i).chave = true; }
                } else if (ve("FOREIGN") || ve("CONSTRAINT") || ve("UNIQUE") || ve("CHECK") || ve("INDEX") || ve("KEY")) {
                    pularRestricao();
                } else {
                    tab.colunas.add(definicaoColuna());
                }
            } while (aceita(","));
            espera(")");
            while (!fim() && !ve(";")) pos++;   // ignora coisas tipo ENGINE=InnoDB
            fimDoComando();
            if (!exec) return;
            if (tabelas.containsKey(nomeTabela.toLowerCase())) {
                if (seNaoExiste) { mensagens.accept("A tabela \"" + nomeTabela + "\" já existia."); return; }
                throw new Erro("a tabela \"" + nomeTabela + "\" já existe");
            }
            tabelas.put(nomeTabela.toLowerCase(), tab);
            mensagens.accept("Tabela \"" + nomeTabela + "\" criada com " + tab.colunas.size() + " colunas.");
        }

        void pularRestricao() {
            int nivel = 0;
            while (!fim()) {
                if (ve("(")) nivel++;
                if (ve(")")) { if (nivel == 0) return; nivel--; }
                if (ve(",") && nivel == 0) return;
                pos++;
            }
        }

        Coluna definicaoColuna() {
            String nomeCol = nome();
            String tipo = atual().tipo == Tipo.PALAVRA ? nome() : "TEXT";
            if (aceita("(")) { while (!fim() && !aceita(")")) pos++; }   // VARCHAR(50), DECIMAL(10,2)
            Coluna col = new Coluna(nomeCol, tipo);
            if (tipo.equalsIgnoreCase("SERIAL")) col.autoIncremento = true;
            while (!fim() && !ve(",") && !ve(")")) {
                if (aceita("PRIMARY")) { espera("KEY"); col.chave = true; }
                else if (aceita("AUTO_INCREMENT") || aceita("AUTOINCREMENT")) col.autoIncremento = true;
                else if (aceita("IDENTITY")) { col.autoIncremento = true; if (aceita("(")) { while (!fim() && !aceita(")")) pos++; } }
                else if (aceita("NOT")) { espera("NULL"); col.naoNulo = true; }
                else if (aceita("UNIQUE")) col.unica = true;
                else if (aceita("DEFAULT")) col.padrao = primario();
                else if (aceita("GENERATED")) { while (!fim() && !ve(",") && !ve(")")) { if (aceita("IDENTITY")) col.autoIncremento = true; else pos++; } }
                else if (aceita("REFERENCES")) { nome(); if (aceita("(")) { while (!fim() && !aceita(")")) pos++; } }
                else if (aceita("(")) { while (!fim() && !aceita(")")) pos++; }
                else pos++;
            }
            return col;
        }

        // ----- SurrealDB: CREATE pessoa SET nome = 'x' / CREATE pessoa:joao CONTENT { ... } -----
        @SuppressWarnings("unchecked")
        void surrealCriar(boolean exec) {
            String n = nome();
            String id = null;
            if (aceita(":")) id = atual().tipo == Tipo.NUMERO ? atual().texto : nome();
            if (atual().tipo == Tipo.NUMERO && id != null) pos++;
            Map<String, Object> doc = new LinkedHashMap<>();
            if (aceita("CONTENT")) {
                Object json = lerJson();
                if (!(json instanceof Map)) throw new Erro("depois do CONTENT vem um objeto { ... }");
                doc = (Map<String, Object>) json;
            } else if (aceita("SET")) {
                do {
                    String c = nome();
                    espera("=");
                    Expr e = expressao();
                    if (exec) doc.put(c, e.valor(vazio()));
                } while (aceita(","));
            }
            while (!fim() && !ve(";")) pos++;   // RETURN ... etc.
            fimDoComando();
            if (exec) surrealGravar(n, id, doc);
        }

        /** Lê um JSON que está no meio do código (ex.: CONTENT { nome: 'x' }). */
        Object lerJson() {
            int ini = atual().ini;
            Object[] lido;
            try {
                lido = Json.lerPedaco(script, ini);
            } catch (Json.Erro e) {
                throw new Erro("JSON com erro: " + e.getMessage());
            }
            int fimJson = (Integer) lido[1];
            while (!fim() && atual().ini < fimJson) pos++;
            return lido[0];
        }

        void surrealGravar(String n, String id, Map<String, Object> doc) {
            Tabela tab = tabelas.computeIfAbsent(n.toLowerCase(), k -> new Tabela(n));
            garantirColuna(tab, "id");
            String idFinal = n + ":" + (id != null ? id : doc.containsKey("id") ? String.valueOf(doc.get("id"))
                    : java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20));
            doc.remove("id");
            for (String c : doc.keySet()) garantirColuna(tab, c);
            Object[] linha = new Object[tab.colunas.size()];
            linha[tab.indice("id")] = idFinal;
            for (Map.Entry<String, Object> e : doc.entrySet()) {
                Object v = e.getValue();
                if (v instanceof Map || v instanceof List) v = Json.escrever(v);
                linha[tab.indice(e.getKey())] = v;
            }
            for (Object[] outra : tab.linhas) if (idFinal.equals(outra[tab.indice("id")])) throw new Erro("já existe o registro " + idFinal);
            tab.linhas.add(linha);
            List<String> nomes = new ArrayList<>();
            for (Coluna c : tab.colunas) nomes.add(c.nome);
            List<Object[]> uma = new ArrayList<>();
            uma.add(linha.clone());
            resultados.accept(new Resultado("CREATE " + n, nomes, uma));
        }

        // ----- DROP / ALTER / TRUNCATE -----
        void drop(boolean exec) {
            if (aceita("DATABASE") || aceita("SCHEMA")) { nome(); fimDoComando(); if (exec) { tabelas.clear(); mensagens.accept("Banco apagado (simulado)."); } return; }
            espera("TABLE");
            boolean seExiste = false;
            if (aceita("IF")) { espera("EXISTS"); seExiste = true; }
            String n = nome();
            fimDoComando();
            if (!exec) return;
            if (tabelas.remove(n.toLowerCase()) == null) {
                if (!seExiste) throw new Erro("a tabela \"" + n + "\" não existe");
            } else {
                mensagens.accept("Tabela \"" + n + "\" apagada.");
            }
        }

        void alter(boolean exec) {
            espera("TABLE");
            String n = nome();
            Tabela tab = exec ? tabela(n) : null;
            if (aceita("ADD")) {
                aceita("COLUMN");
                Coluna c = definicaoColuna();
                fimDoComando();
                if (!exec) return;
                tab.colunas.add(c);
                for (int i = 0; i < tab.linhas.size(); i++) {
                    Object[] nova = java.util.Arrays.copyOf(tab.linhas.get(i), tab.colunas.size());
                    nova[nova.length - 1] = c.padrao == null ? null : c.padrao.valor(vazio());
                    tab.linhas.set(i, nova);
                }
                mensagens.accept("Coluna \"" + c.nome + "\" adicionada em \"" + n + "\".");
            } else if (aceita("DROP")) {
                aceita("COLUMN");
                String col = nome();
                fimDoComando();
                if (!exec) return;
                int idx = tab.indice(col);
                if (idx < 0) throw new Erro("a coluna \"" + col + "\" não existe");
                tab.colunas.remove(idx);
                for (int i = 0; i < tab.linhas.size(); i++) {
                    List<Object> l = new ArrayList<>(java.util.Arrays.asList(tab.linhas.get(i)));
                    l.remove(idx);
                    tab.linhas.set(i, l.toArray());
                }
                mensagens.accept("Coluna \"" + col + "\" removida.");
            } else {
                throw new Erro("no ALTER TABLE eu só sei ADD e DROP COLUMN");
            }
        }

        void truncate(boolean exec) {
            espera("TABLE");
            String n = nome();
            fimDoComando();
            if (!exec) return;
            Tabela tab = tabela(n);
            tab.linhas.clear();
            tab.proximoId = 1;
            mensagens.accept("Tabela \"" + n + "\" esvaziada.");
        }

        // ----- INSERT -----
        @SuppressWarnings("unchecked")
        void insert(boolean exec) {
            espera("INTO");
            String n = nome();
            List<String> cols = new ArrayList<>();
            if (ve("(") ) {
                pos++;
                do { cols.add(nome()); } while (aceita(","));
                espera(")");
            }
            List<List<Object>> valores = new ArrayList<>();
            if (aceita("VALUES")) {
                do {
                    espera("(");
                    List<Expr> exprs = new ArrayList<>();
                    do { exprs.add(expressao()); } while (aceita(","));
                    espera(")");
                    if (exec) {
                        List<Object> l = new ArrayList<>();
                        for (Expr e : exprs) l.add(e.valor(vazio()));
                        valores.add(l);
                    }
                } while (aceita(","));
            } else if (ve("SELECT")) {
                Resultado r = select(exec);
                if (exec) for (Object[] linha : r.linhas) valores.add(new ArrayList<>(java.util.Arrays.asList(linha)));
            } else if (dialeto.equals("SURREAL") && (ve("{") || ve("["))) {
                // SurrealDB: INSERT INTO pessoa { nome: 'x' }  ou  [ {...}, {...} ]
                Object json = lerJson();
                List<Object> docs = json instanceof List ? (List<Object>) json : java.util.Collections.singletonList(json);
                fimDoComando();
                if (exec) for (Object d : docs) surrealGravar(n, null, (Map<String, Object>) d);
                return;
            } else {
                throw new Erro("depois do INSERT INTO tabela vem VALUES (...) ou um SELECT");
            }
            // Coisas que vêm depois e a gente ignora: IF NOT EXISTS, USING TTL, RETURNING, ON CONFLICT...
            if (ve("IF") || ve("USING") || ve("RETURNING") || ve("ON")) { while (!fim() && !ve(";")) pos++; }
            fimDoComando();
            if (!exec) return;

            if (dialeto.equals("SURREAL")) {
                tabelas.putIfAbsent(n.toLowerCase(), new Tabela(n));
                for (String c : cols) garantirColuna(tabela(n), c);
            }
            Tabela tab = tabela(n);
            if (cols.isEmpty()) for (Coluna c : tab.colunas) if (!(c.autoIncremento && valores.stream().allMatch(v -> v.size() < tab.colunas.size()))) cols.add(c.nome);
            int inseridas = 0;
            for (List<Object> v : valores) {
                if (v.size() != cols.size()) throw new Erro("são " + cols.size() + " colunas mas vieram " + v.size() + " valores");
                Object[] linha = new Object[tab.colunas.size()];
                boolean[] preenchida = new boolean[linha.length];
                for (int i = 0; i < cols.size(); i++) {
                    int idx = tab.indice(cols.get(i));
                    if (idx < 0) throw new Erro("a tabela \"" + n + "\" não tem a coluna \"" + cols.get(i) + "\"");
                    linha[idx] = converter(v.get(i), tab.colunas.get(idx));
                    preenchida[idx] = true;
                }
                completarLinha(tab, linha, preenchida);
                tab.linhas.add(linha);
                inseridas++;
            }
            mensagens.accept(inseridas + (inseridas == 1 ? " linha inserida" : " linhas inseridas") + " em \"" + n + "\".");
        }

        void completarLinha(Tabela tab, Object[] linha, boolean[] preenchida) {
            for (int i = 0; i < linha.length; i++) {
                Coluna c = tab.colunas.get(i);
                boolean autoNumero = c.autoIncremento || (c.chave && c.tipo.equalsIgnoreCase("INTEGER"));   // SQLite
                if (!preenchida[i] || (linha[i] == null && autoNumero)) {
                    if (autoNumero) linha[i] = tab.proximoId;
                    else if (c.padrao != null) linha[i] = converter(c.padrao.valor(vazio()), c);
                }
                if (autoNumero && linha[i] instanceof Number) {
                    tab.proximoId = Math.max(tab.proximoId, ((Number) linha[i]).longValue() + 1);
                }
                if (linha[i] == null && (c.naoNulo || c.chave)) throw new Erro("a coluna \"" + c.nome + "\" não pode ficar vazia (NULL)");
                if ((c.chave || c.unica) && linha[i] != null) {
                    for (Object[] outra : tab.linhas) {
                        if (outra != linha && iguais(outra[i], linha[i])) {
                            throw new Erro("já existe uma linha com " + c.nome + " = " + texto(linha[i]) + " (valor repetido)");
                        }
                    }
                }
            }
        }

        // ----- UPDATE -----
        void update(boolean exec) {
            String n = nome();
            String apelido = apelidoOpcional();
            espera("SET");
            List<String> cols = new ArrayList<>();
            List<Expr> exprs = new ArrayList<>();
            do {
                String c = nome();
                if (aceita(".")) c = nome();
                cols.add(c);
                espera("=");
                exprs.add(expressao());
            } while (aceita(","));
            Expr onde = aceita("WHERE") ? expressao() : null;
            fimDoComando();
            if (!exec) return;
            Tabela tab = tabela(n);
            if (dialeto.equals("SURREAL")) for (String c : cols) garantirColuna(tab, c);
            int mudadas = 0;
            for (Object[] linha : tab.linhas) {
                Contexto ctx = new Contexto(mapaDaLinha(tab, linha, apelido), null);
                if (onde != null && !verdade(onde.valor(ctx))) continue;
                Object[] antes = linha.clone();
                for (int i = 0; i < cols.size(); i++) {
                    int idx = tab.indice(cols.get(i));
                    if (idx < 0) throw new Erro("a tabela não tem a coluna \"" + cols.get(i) + "\"");
                    linha[idx] = converter(exprs.get(i).valor(ctx), tab.colunas.get(idx));
                }
                try {
                    boolean[] tudo = new boolean[linha.length];
                    java.util.Arrays.fill(tudo, true);
                    completarLinha(tab, linha, tudo);
                } catch (Erro e) {
                    System.arraycopy(antes, 0, linha, 0, linha.length);
                    throw e;
                }
                mudadas++;
            }
            mensagens.accept(mudadas + (mudadas == 1 ? " linha alterada" : " linhas alteradas") + " em \"" + n + "\".");
        }

        // ----- DELETE -----
        void delete(boolean exec) {
            aceita("FROM");
            String n = nome();
            String apelido = apelidoOpcional();
            Expr onde = aceita("WHERE") ? expressao() : null;
            fimDoComando();
            if (!exec) return;
            Tabela tab = tabela(n);
            int antes = tab.linhas.size();
            tab.linhas.removeIf(l -> onde == null || verdade(onde.valor(new Contexto(mapaDaLinha(tab, l, apelido), null))));
            int apagadas = antes - tab.linhas.size();
            mensagens.accept(apagadas + (apagadas == 1 ? " linha apagada" : " linhas apagadas") + " de \"" + n + "\".");
        }

        String apelidoOpcional() {
            if (aceita("AS")) return nome();
            if (atual().tipo == Tipo.PALAVRA && !ve("SET") && !ve("WHERE") && !ve("JOIN") && !ve("INNER")
                    && !ve("LEFT") && !ve("RIGHT") && !ve("ON") && !ve("ORDER") && !ve("GROUP") && !ve("LIMIT")
                    && !ve("HAVING") && !ve("OFFSET") && !ve("CROSS") && !ve("UNION") && !ve("END")) {
                return nome();
            }
            return null;
        }

        // ----- SELECT -----
        class ItemSelect {
            Expr expr;
            String apelido;
            String tabelaEstrela;   // "t.*" ou "*" (vazio)
        }

        Resultado select(boolean exec) {
            espera("SELECT");
            boolean distinto = aceita("DISTINCT");
            aceita("ALL");
            Expr top = null;
            if (aceita("TOP")) { boolean par = aceita("("); top = primario(); if (par) espera(")"); }
            if (ve("FIRST") && proximo(1).tipo == Tipo.NUMERO) { pos++; top = primario(); }   // Firebird: SELECT FIRST 5

            List<ItemSelect> itens = new ArrayList<>();
            do {
                ItemSelect it = new ItemSelect();
                if (aceita("*")) {
                    it.tabelaEstrela = "";
                } else if (atual().tipo == Tipo.PALAVRA && proximo(1).eh(".") && proximo(2).eh("*")) {
                    it.tabelaEstrela = nome().toLowerCase();
                    pos += 2;
                } else {
                    it.expr = expressao();
                    if (aceita("AS")) it.apelido = nome();
                    else if (atual().tipo == Tipo.PALAVRA && !ve("FROM") && !ve("WHERE") && !ve("ORDER") && !ve("GROUP")
                            && !ve("LIMIT") && !ve("INTO") && !ve("UNION") && !ve("HAVING") && !ve("END") && !comecaComando()) {
                        it.apelido = nome();
                    } else if (atual().tipo == Tipo.TEXTO) {
                        it.apelido = nome();
                    }
                }
                itens.add(it);
            } while (aceita(","));

            // FROM com JOINs
            List<Map<String, Object>> linhas = new ArrayList<>();
            List<String> colunasDasTabelas = new ArrayList<>();   // pra expandir o *
            Map<String, List<String>> colunasPorApelido = new LinkedHashMap<>();
            if (aceita("FROM")) {
                linhas = fonte(exec, colunasDasTabelas, colunasPorApelido);
                while (true) {
                    boolean esquerda = false;
                    if (aceita("INNER")) { espera("JOIN"); }
                    else if (aceita("LEFT")) { aceita("OUTER"); espera("JOIN"); esquerda = true; }
                    else if (aceita("CROSS")) { espera("JOIN"); }
                    else if (aceita(",")) { /* produto */ }
                    else if (aceita("JOIN")) { }
                    else break;
                    List<String> colsDireita = new ArrayList<>();
                    List<Map<String, Object>> direita = fonte(exec, colsDireita, colunasPorApelido);
                    colunasDasTabelas.addAll(colsDireita);
                    Expr on = aceita("ON") ? expressao() : null;
                    if (exec) linhas = juntar(linhas, direita, on, esquerda, colsDireita);
                }
            } else {
                linhas.add(new LinkedHashMap<>());   // SELECT 1+1 (sem tabela)
            }

            Expr onde = aceita("WHERE") ? expressao() : null;
            List<Expr> grupoPor = new ArrayList<>();
            if (aceita("GROUP")) { espera("BY"); do { grupoPor.add(expressao()); } while (aceita(",")); }
            Expr tendo = aceita("HAVING") ? expressao() : null;
            List<Expr> ordem = new ArrayList<>();
            List<Boolean> decrescente = new ArrayList<>();
            if (aceita("ORDER")) {
                espera("BY");
                do {
                    ordem.add(expressao());
                    if (aceita("DESC")) decrescente.add(true); else { aceita("ASC"); decrescente.add(false); }
                } while (aceita(","));
            }
            Expr limite = null, pular = null;
            if (aceita("LIMIT")) { limite = primario(); if (aceita(",")) { pular = limite; limite = primario(); } }
            if (aceita("OFFSET")) { pular = primario(); aceita("ROWS"); aceita("ROW"); }
            if (aceita("FETCH")) { aceita("FIRST"); aceita("NEXT"); limite = primario(); aceita("ROWS"); aceita("ROW"); aceita("ONLY"); }
            if (aceita("ALLOW")) aceita("FILTERING");   // Cassandra

            if (!exec) return null;

            // WHERE
            List<Map<String, Object>> filtradas = new ArrayList<>();
            for (Map<String, Object> l : linhas) {
                if (onde == null || verdade(onde.valor(new Contexto(l, null)))) filtradas.add(l);
            }

            // Nomes das colunas do resultado
            List<String> nomes = new ArrayList<>();
            for (ItemSelect it : itens) {
                if (it.tabelaEstrela != null) {
                    List<String> cs = it.tabelaEstrela.isEmpty() ? colunasDasTabelas : colunasPorApelido.get(it.tabelaEstrela);
                    if (cs == null) throw new Erro("não achei a tabela \"" + it.tabelaEstrela + "\" no FROM");
                    for (String c : cs) nomes.add(c.substring(c.indexOf('.') + 1));
                } else {
                    nomes.add(it.apelido != null ? it.apelido : it.expr.nome());
                }
            }

            // Agrupar (GROUP BY ou COUNT/SUM sem GROUP BY)
            boolean temAgregacao = itens.stream().anyMatch(it -> it.expr != null && it.expr.temAgregacao())
                    || (tendo != null) || !grupoPor.isEmpty();
            List<Contexto> contextos = new ArrayList<>();
            if (temAgregacao) {
                Map<List<Object>, List<Map<String, Object>>> grupos = new LinkedHashMap<>();
                for (Map<String, Object> l : filtradas) {
                    List<Object> chave = new ArrayList<>();
                    for (Expr g : grupoPor) chave.add(chaveGrupo(g.valor(new Contexto(l, null))));
                    grupos.computeIfAbsent(chave, k -> new ArrayList<>()).add(l);
                }
                if (grupos.isEmpty() && grupoPor.isEmpty()) grupos.put(new ArrayList<>(), new ArrayList<>());
                for (List<Map<String, Object>> g : grupos.values()) {
                    Contexto ctx = new Contexto(g.isEmpty() ? new LinkedHashMap<>() : g.get(0), g);
                    if (tendo == null || verdade(tendo.valor(ctx))) contextos.add(ctx);
                }
            } else {
                for (Map<String, Object> l : filtradas) contextos.add(new Contexto(l, null));
            }

            // Calcula as colunas de cada linha
            List<Object[]> saida = new ArrayList<>();
            List<Contexto> origem = new ArrayList<>();
            for (Contexto ctx : contextos) {
                List<Object> valores = new ArrayList<>();
                for (ItemSelect it : itens) {
                    if (it.tabelaEstrela != null) {
                        List<String> cs = it.tabelaEstrela.isEmpty() ? colunasDasTabelas : colunasPorApelido.get(it.tabelaEstrela);
                        for (String c : cs) valores.add(ctx.linha.get(c));
                    } else {
                        valores.add(it.expr.valor(ctx));
                    }
                }
                saida.add(valores.toArray());
                origem.add(ctx);
            }

            // DISTINCT
            if (distinto) {
                Set<List<Object>> vistos = new LinkedHashSet<>();
                List<Object[]> unicos = new ArrayList<>();
                List<Contexto> origemUnicos = new ArrayList<>();
                for (int i = 0; i < saida.size(); i++) {
                    List<Object> chave = new ArrayList<>();
                    for (Object o : saida.get(i)) chave.add(chaveGrupo(o));
                    if (vistos.add(chave)) { unicos.add(saida.get(i)); origemUnicos.add(origem.get(i)); }
                }
                saida = unicos;
                origem = origemUnicos;
            }

            // ORDER BY (pode usar o apelido da coluna, o número da coluna ou qualquer conta)
            if (!ordem.isEmpty()) {
                List<Integer> idx = new ArrayList<>();
                for (int i = 0; i < saida.size(); i++) idx.add(i);
                final List<Object[]> s = saida;
                final List<Contexto> o = origem;
                idx.sort((a, b) -> {
                    for (int k = 0; k < ordem.size(); k++) {
                        Object va = valorOrdem(ordem.get(k), s.get(a), o.get(a), nomes);
                        Object vb = valorOrdem(ordem.get(k), s.get(b), o.get(b), nomes);
                        int cmp = comparar(va, vb);
                        if (cmp != 0) return decrescente.get(k) ? -cmp : cmp;
                    }
                    return 0;
                });
                List<Object[]> ordenada = new ArrayList<>();
                for (int i : idx) ordenada.add(saida.get(i));
                saida = ordenada;
            }

            // OFFSET / LIMIT / TOP
            int inicio = pular == null ? 0 : (int) numero(pular.valor(vazio())).longValue();
            int max = Integer.MAX_VALUE;
            if (limite != null) max = (int) numero(limite.valor(vazio())).longValue();
            if (top != null) max = Math.min(max, (int) numero(top.valor(vazio())).longValue());
            List<Object[]> finais = new ArrayList<>();
            for (int i = inicio; i < saida.size() && finais.size() < max; i++) finais.add(saida.get(i));

            return new Resultado("SELECT", nomes, finais);
        }

        Object valorOrdem(Expr e, Object[] linhaSaida, Contexto ctx, List<String> nomes) {
            if (e instanceof Literal && ((Literal) e).v instanceof Long) {
                int n = (int) (long) (Long) ((Literal) e).v;
                if (n >= 1 && n <= linhaSaida.length) return linhaSaida[n - 1];
            }
            if (e instanceof RefColuna && ((RefColuna) e).tabela == null) {
                String nome = ((RefColuna) e).coluna;
                for (int i = 0; i < nomes.size(); i++) {
                    if (nomes.get(i).equalsIgnoreCase(nome) && !ctx.linha.containsKey(nome.toLowerCase())) return linhaSaida[i];
                }
            }
            return e.valor(ctx);
        }

        /** Lê "tabela [apelido]" ou "(SELECT ...) apelido" e devolve as linhas. */
        List<Map<String, Object>> fonte(boolean exec, List<String> colunas, Map<String, List<String>> porApelido) {
            if (ve("(")) {
                pos++;
                Resultado sub = select(exec);
                espera(")");
                String apelido = apelidoOpcional();
                if (apelido == null) apelido = "sub";
                List<Map<String, Object>> linhas = new ArrayList<>();
                if (!exec) return linhas;
                List<String> cs = new ArrayList<>();
                for (String c : sub.colunas) cs.add(apelido.toLowerCase() + "." + c.toLowerCase());
                colunas.addAll(cs);
                porApelido.put(apelido.toLowerCase(), cs);
                for (Object[] l : sub.linhas) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    for (int i = 0; i < sub.colunas.size(); i++) {
                        m.put(cs.get(i), l[i]);
                        m.putIfAbsent(sub.colunas.get(i).toLowerCase(), l[i]);
                    }
                    linhas.add(m);
                }
                return linhas;
            }
            String n = nome();
            String apelido = apelidoOpcional();
            List<Map<String, Object>> linhas = new ArrayList<>();
            if (n.equalsIgnoreCase("dual")) { linhas.add(new LinkedHashMap<>()); return linhas; }
            if (!exec) return linhas;
            Tabela tab = tabela(n);
            String a = (apelido != null ? apelido : n).toLowerCase();
            List<String> cs = new ArrayList<>();
            for (Coluna c : tab.colunas) cs.add(a + "." + c.nome.toLowerCase());
            colunas.addAll(cs);
            porApelido.put(a, cs);
            porApelido.putIfAbsent(n.toLowerCase(), cs);
            for (Object[] l : tab.linhas) linhas.add(mapaDaLinha(tab, l, apelido));
            return linhas;
        }

        List<Map<String, Object>> juntar(List<Map<String, Object>> esquerda, List<Map<String, Object>> direita,
                                         Expr on, boolean leftJoin, List<String> colsDireita) {
            List<Map<String, Object>> resultado = new ArrayList<>();
            for (Map<String, Object> e : esquerda) {
                boolean achou = false;
                for (Map<String, Object> d : direita) {
                    Map<String, Object> junto = juntarMapas(e, d);
                    if (on == null || verdade(on.valor(new Contexto(junto, null)))) {
                        resultado.add(junto);
                        achou = true;
                    }
                }
                if (!achou && leftJoin) {
                    Map<String, Object> vazio = new LinkedHashMap<>();
                    for (String c : colsDireita) { vazio.put(c, null); vazio.putIfAbsent(c.substring(c.indexOf('.') + 1), null); }
                    resultado.add(juntarMapas(e, vazio));
                }
            }
            return resultado;
        }

        Map<String, Object> juntarMapas(Map<String, Object> a, Map<String, Object> b) {
            Map<String, Object> m = new LinkedHashMap<>(a);
            for (Map.Entry<String, Object> en : b.entrySet()) {
                if (en.getKey().contains(".")) m.put(en.getKey(), en.getValue());
                else if (m.containsKey(en.getKey()) && !a.containsKey("?" + en.getKey())) m.put("?" + en.getKey(), Boolean.TRUE); // nome repetido
                else m.putIfAbsent(en.getKey(), en.getValue());
            }
            return m;
        }

        // ----- T-SQL e PL/SQL: variáveis e blocos -----
        void declare(boolean exec) {
            espera("DECLARE");
            if (atual().tipo == Tipo.VARIAVEL) {             // T-SQL: DECLARE @x INT = 5, @y INT
                do {
                    String v = atual().texto; pos++;
                    aceita("AS");
                    if (atual().tipo == Tipo.PALAVRA) { nome(); if (aceita("(")) { while (!fim() && !aceita(")")) pos++; } }
                    Object valor = null;
                    if (aceita("=")) { Expr e = expressao(); if (exec) valor = e.valor(vazio()); }
                    if (exec) variaveis.put(v, valor);
                } while (aceita(","));
                fimDoComando();
                return;
            }
            // PL/SQL: DECLARE x NUMBER := 5; ... BEGIN ... END;
            while (!fim() && !ve("BEGIN")) {
                String v = nome().toLowerCase();
                aceita("CONSTANT");
                if (atual().tipo == Tipo.PALAVRA && !ve(":=")) { nome(); if (aceita("(")) { while (!fim() && !aceita(")")) pos++; } }
                Object valor = null;
                if (aceita(":=") || aceita("DEFAULT")) { Expr e = expressao(); if (exec) valor = e.valor(vazio()); }
                espera(";");
                if (exec) variaveis.put(v, valor);
            }
            bloco(exec);
        }

        void setVariavel(boolean exec) {
            if (atual().tipo != Tipo.VARIAVEL) throw new Erro("SET precisa de uma variável, tipo SET @x = 1");
            String v = atual().texto; pos++;
            espera("=");
            Expr e = expressao();
            fimDoComando();
            if (exec) variaveis.put(v, e.valor(vazio()));
        }

        void atribuicao(boolean exec) {
            String v = nome().toLowerCase();
            espera(":=");
            Expr e = expressao();
            fimDoComando();
            if (exec) {
                if (!variaveis.containsKey(v)) throw new Erro("a variável \"" + v + "\" não foi declarada");
                variaveis.put(v, e.valor(vazio()));
            }
        }

        void putLine(boolean exec) {
            nome(); espera("."); String f = nome();
            if (!f.equalsIgnoreCase("PUT_LINE") && !f.equalsIgnoreCase("PUT")) throw new Erro("só conheço DBMS_OUTPUT.PUT_LINE");
            espera("(");
            Expr e = expressao();
            espera(")");
            fimDoComando();
            if (exec) mensagens.accept(texto(e.valor(vazio())));
        }

        void bloco(boolean exec) {
            espera("BEGIN");
            aceita("TRAN"); aceita("TRANSACTION");
            while (!fim() && !ve("END") && !ve("EXCEPTION")) {
                if (aceita(";")) continue;
                comando(exec);
            }
            if (aceita("EXCEPTION")) { while (!fim() && !ve("END")) pos++; }
            espera("END");
            if (atual().tipo == Tipo.PALAVRA && !comecaComando() && !ve("ELSE") && !ve("END")) pos++;   // END nome_do_bloco
            aceita(";");
        }

        void se(boolean exec) {
            espera("IF");
            Expr cond = expressao();
            if (aceita("THEN")) {                                      // PL/SQL
                boolean feito = false;
                boolean v = exec && verdade(cond.valor(vazio()));
                comandosAte(v, "ELSIF", "ELSE", "END");
                feito = v;
                while (aceita("ELSIF")) {
                    Expr c2 = expressao();
                    espera("THEN");
                    boolean v2 = exec && !feito && verdade(c2.valor(vazio()));
                    comandosAte(v2, "ELSIF", "ELSE", "END");
                    feito |= v2;
                }
                if (aceita("ELSE")) comandosAte(exec && !feito, "END");
                espera("END"); espera("IF"); aceita(";");
            } else {                                                   // T-SQL
                boolean v = exec && verdade(cond.valor(vazio()));
                comando(v);
                if (aceita("ELSE")) comando(exec && !v);
            }
        }

        void comandosAte(boolean exec, String... paradas) {
            while (!fim()) {
                for (String p : paradas) if (ve(p)) return;
                if (aceita(";")) continue;
                comando(exec);
            }
        }

        void enquanto(boolean exec) {
            espera("WHILE");
            int inicioCond = pos;
            Expr cond = expressao();
            if (aceita("LOOP")) {                                      // PL/SQL
                int corpo = pos;
                while (exec && verdade(cond.valor(vazio()))) {
                    pos = corpo;
                    comandosAte(true, "END");
                    pos = inicioCond;
                    cond = expressao();
                    espera("LOOP");
                }
                pos = corpo;
                comandosAte(false, "END");
                espera("END"); espera("LOOP"); aceita(";");
            } else {                                                   // T-SQL
                int corpo = pos;
                while (exec && verdade(cond.valor(vazio()))) {
                    pos = corpo;
                    comando(true);
                }
                pos = corpo;
                comando(false);
            }
        }

        void para(boolean exec) {                                       // PL/SQL: FOR i IN 1..10 LOOP ... END LOOP;
            espera("FOR");
            String v = nome().toLowerCase();
            espera("IN");
            boolean reverso = aceita("REVERSE");
            Expr de = expressao();
            espera("..");
            Expr ate = expressao();
            espera("LOOP");
            int corpo = pos;
            if (exec) {
                long a = numero(de.valor(vazio())).longValue();
                long b = numero(ate.valor(vazio())).longValue();
                Object antigo = variaveis.get(v);
                for (long i = reverso ? b : a; reverso ? i >= a : i <= b; i += reverso ? -1 : 1) {
                    variaveis.put(v, i);
                    pos = corpo;
                    comandosAte(true, "END");
                    if (++passos > 200000) throw new Erro("o laço rodou demais e foi parado");
                }
                if (antigo == null) variaveis.remove(v); else variaveis.put(v, antigo);
            }
            pos = corpo;
            comandosAte(false, "END");
            espera("END"); espera("LOOP"); aceita(";");
        }

        // ---------- EXPRESSÕES (do mais "fraco" pro mais "forte") ----------

        Expr expressao() { return ou(); }

        Expr ou() {
            Expr e = e_();
            while (aceita("OR")) { Expr a = e, b = e_(); e = binaria("OR", a, b, c -> verdade(a.valor(c)) || verdade(b.valor(c))); }
            return e;
        }

        Expr e_() {
            Expr e = nao();
            while (aceita("AND")) { Expr a = e, b = nao(); e = binaria("AND", a, b, c -> verdade(a.valor(c)) && verdade(b.valor(c))); }
            return e;
        }

        Expr nao() {
            if (aceita("NOT")) { Expr a = nao(); return binaria("NOT", a, a, c -> !verdade(a.valor(c))); }
            if (aceita("EXISTS")) {
                espera("(");
                int ini = pos;
                select(false);
                int fimSub = pos;
                espera(")");
                return new Expr() {
                    public Object valor(Contexto c) {
                        int volta = pos; pos = ini;
                        Resultado r = select(true);
                        pos = volta;
                        return !r.linhas.isEmpty();
                    }
                    public String nome() { return "EXISTS"; }
                };
            }
            return comparacao();
        }

        Expr comparacao() {
            Expr e = soma();
            while (true) {
                if (ve("=") || ve("<>") || ve("!=") || ve("<") || ve(">") || ve("<=") || ve(">=")) {
                    String op = atual().texto; pos++;
                    Expr a = e, b = soma();
                    e = binaria(op, a, b, c -> {
                        Object x = a.valor(c), y = b.valor(c);
                        if (x == null || y == null) return null;
                        int cmp = comparar(x, y);
                        switch (op) {
                            case "=": return cmp == 0;
                            case "<>": case "!=": return cmp != 0;
                            case "<": return cmp < 0;
                            case ">": return cmp > 0;
                            case "<=": return cmp <= 0;
                            default: return cmp >= 0;
                        }
                    });
                    continue;
                }
                boolean negado = false;
                int volta = pos;
                if (aceita("NOT")) negado = true;
                if (aceita("LIKE")) {
                    Expr a = e, b = soma();
                    boolean n = negado;
                    e = binaria("LIKE", a, b, c -> {
                        Object x = a.valor(c), y = b.valor(c);
                        if (x == null || y == null) return null;
                        return n != como(texto(x), texto(y));
                    });
                } else if (aceita("IN")) {
                    espera("(");
                    Expr a = e;
                    boolean n = negado;
                    if (ve("SELECT")) {
                        int ini = pos;
                        select(false);
                        espera(")");
                        e = new Expr() {
                            public Object valor(Contexto c) {
                                int v = pos; pos = ini;
                                Resultado r = select(true);
                                pos = v;
                                Object x = a.valor(c);
                                for (Object[] l : r.linhas) if (iguais(x, l[0])) return !n;
                                return n;
                            }
                        };
                    } else {
                        List<Expr> lista = new ArrayList<>();
                        do { lista.add(expressao()); } while (aceita(","));
                        espera(")");
                        e = binaria("IN", a, a, c -> {
                            Object x = a.valor(c);
                            for (Expr item : lista) if (iguais(x, item.valor(c))) return !n;
                            return n;
                        });
                    }
                } else if (aceita("BETWEEN")) {
                    Expr a = e, de = soma();
                    espera("AND");
                    Expr ate = soma();
                    boolean n = negado;
                    e = binaria("BETWEEN", a, a, c -> {
                        Object x = a.valor(c);
                        if (x == null) return null;
                        return n != (comparar(x, de.valor(c)) >= 0 && comparar(x, ate.valor(c)) <= 0);
                    });
                } else if (!negado && aceita("IS")) {
                    boolean n = aceita("NOT");
                    espera("NULL");
                    Expr a = e;
                    e = binaria("IS NULL", a, a, c -> n != (a.valor(c) == null));
                } else {
                    pos = volta;
                    return e;
                }
            }
        }

        Expr soma() {
            Expr e = produto();
            while (ve("+") || ve("-") || ve("||")) {
                String op = atual().texto; pos++;
                Expr a = e, b = produto();
                e = binaria(op, a, b, c -> {
                    Object x = a.valor(c), y = b.valor(c);
                    if (op.equals("||")) return (x == null ? "" : texto(x)) + (y == null ? "" : texto(y));
                    if (x == null || y == null) return null;
                    if (op.equals("+") && (x instanceof String || y instanceof String)) {
                        if (dialeto.equals("TSQL") || !(ehNumerico(x) && ehNumerico(y))) return texto(x) + texto(y);
                    }
                    return conta(op, x, y);
                });
            }
            return e;
        }

        Expr produto() {
            Expr e = unario();
            while (ve("*") || ve("/") || ve("%")) {
                String op = atual().texto; pos++;
                Expr a = e, b = unario();
                e = binaria(op, a, b, c -> {
                    Object x = a.valor(c), y = b.valor(c);
                    if (x == null || y == null) return null;
                    return conta(op, x, y);
                });
            }
            return e;
        }

        Expr unario() {
            if (aceita("-")) { Expr a = unario(); return binaria("-", a, a, c -> { Object x = a.valor(c); return x == null ? null : conta("-", 0L, x); }); }
            if (aceita("+")) return unario();
            Expr e = primario();
            while (aceita("::")) {                     // PostgreSQL: '5'::int
                String tipo = nome();
                Coluna falsa = new Coluna("x", tipo);
                Expr dentro = e;
                e = new Expr() {
                    public Object valor(Contexto c) { return converter(dentro.valor(c), falsa); }
                    public boolean temAgregacao() { return dentro.temAgregacao(); }
                    public String nome() { return dentro.nome(); }
                };
            }
            return e;
        }

        Expr primario() {
            Token tk = atual();
            if (tk.tipo == Tipo.NUMERO) {
                pos++;
                return new Literal(tk.texto.contains(".") ? (Object) Double.parseDouble(tk.texto) : (Object) Long.parseLong(tk.texto), tk.texto);
            }
            if (tk.tipo == Tipo.TEXTO) { pos++; return new Literal(tk.texto, "'" + tk.texto + "'"); }
            if (tk.tipo == Tipo.VARIAVEL) {
                pos++;
                String v = tk.texto;
                return new Expr() {
                    public Object valor(Contexto c) {
                        if (!variaveis.containsKey(v)) throw new Erro("a variável " + v + " não foi declarada (use DECLARE)");
                        return variaveis.get(v);
                    }
                    public String nome() { return v; }
                };
            }
            if (aceita("(")) {
                if (ve("SELECT")) {      // subconsulta que devolve 1 valor
                    int ini = pos;
                    select(false);
                    espera(")");
                    return new Expr() {
                        public Object valor(Contexto c) {
                            int v = pos; pos = ini;
                            Resultado r = select(true);
                            pos = v;
                            return r.linhas.isEmpty() ? null : r.linhas.get(0)[0];
                        }
                        public String nome() { return "subconsulta"; }
                    };
                }
                Expr e = expressao();
                espera(")");
                return e;
            }
            if (tk.eh("NULL")) { pos++; return new Literal(null, "NULL"); }
            if (tk.eh("TRUE")) { pos++; return new Literal(true, "TRUE"); }
            if (tk.eh("FALSE")) { pos++; return new Literal(false, "FALSE"); }
            if (tk.eh("CASE")) return caso();
            if (tk.eh("CAST")) {
                pos++; espera("(");
                Expr e = expressao();
                espera("AS");
                String tipo = nome();
                if (aceita("(")) { while (!fim() && !aceita(")")) pos++; }
                espera(")");
                Coluna falsa = new Coluna("x", tipo);
                return new Expr() {
                    public Object valor(Contexto c) { return converter(e.valor(c), falsa); }
                    public String nome() { return "CAST"; }
                };
            }
            if (tk.tipo == Tipo.PALAVRA) {
                pos++;
                String n = tk.texto;
                // DBMS_OUTPUT.PUT_LINE dentro de expressão não existe; tabela.coluna sim
                if (ve("(")) return funcao(n);
                if (aceita(".")) {
                    String col = aceita("*") ? "*" : nome();
                    return new RefColuna(n, col);
                }
                if (n.equalsIgnoreCase("CURRENT_DATE") || n.equalsIgnoreCase("SYSDATE") || n.equalsIgnoreCase("CURRENT_TIMESTAMP")) {
                    return funcaoSemArgumento(n);
                }
                return new RefColuna(null, n);
            }
            throw new Erro("não entendi \"" + (fim() ? "o fim do texto" : tk.texto) + "\"");
        }

        Expr caso() {
            espera("CASE");
            Expr base = (!ve("WHEN")) ? expressao() : null;
            List<Expr> quando = new ArrayList<>(), entao = new ArrayList<>();
            while (aceita("WHEN")) {
                quando.add(expressao());
                espera("THEN");
                entao.add(expressao());
            }
            Expr senao = aceita("ELSE") ? expressao() : null;
            espera("END");
            return new Expr() {
                public Object valor(Contexto c) {
                    Object b = base == null ? null : base.valor(c);
                    for (int i = 0; i < quando.size(); i++) {
                        boolean ok = base == null ? verdade(quando.get(i).valor(c)) : iguais(b, quando.get(i).valor(c));
                        if (ok) return entao.get(i).valor(c);
                    }
                    return senao == null ? null : senao.valor(c);
                }
                public boolean temAgregacao() {
                    for (Expr e : entao) if (e.temAgregacao()) return true;
                    for (Expr e : quando) if (e.temAgregacao()) return true;
                    return senao != null && senao.temAgregacao();
                }
                public String nome() { return "CASE"; }
            };
        }

        Expr funcao(String n) {
            espera("(");
            String f = n.toUpperCase(Locale.ROOT);
            boolean distinto = aceita("DISTINCT");
            List<Expr> args = new ArrayList<>();
            boolean estrela = false;
            if (aceita("*")) estrela = true;
            else if (!ve(")")) { do { args.add(expressao()); } while (aceita(",")); }
            espera(")");
            String rotulo = n + "(" + (estrela ? "*" : args.isEmpty() ? "" : args.get(0).nome()) + ")";

            if (f.equals("COUNT") || f.equals("SUM") || f.equals("AVG") || f.equals("MIN") || f.equals("MAX")) {
                boolean est = estrela;
                return new Expr() {
                    public Object valor(Contexto c) {
                        List<Map<String, Object>> linhas = c.grupo != null ? c.grupo : Collections.singletonList(c.linha);
                        List<Object> vals = new ArrayList<>();
                        Set<Object> vistos = new LinkedHashSet<>();
                        for (Map<String, Object> l : linhas) {
                            Object v = est ? 1L : args.get(0).valor(new Contexto(l, null));
                            if (v == null) continue;
                            if (distinto && !vistos.add(chaveGrupo(v))) continue;
                            vals.add(v);
                        }
                        switch (f) {
                            case "COUNT": return (long) vals.size();
                            case "SUM": { if (vals.isEmpty()) return null; Object s = 0L; for (Object v : vals) s = conta("+", s, v); return s; }
                            case "AVG": { if (vals.isEmpty()) return null; double s = 0; for (Object v : vals) s += numero(v).doubleValue(); return s / vals.size(); }
                            case "MIN": { Object m = null; for (Object v : vals) if (m == null || comparar(v, m) < 0) m = v; return m; }
                            default:    { Object m = null; for (Object v : vals) if (m == null || comparar(v, m) > 0) m = v; return m; }
                        }
                    }
                    public boolean temAgregacao() { return true; }
                    public String nome() { return rotulo; }
                };
            }
            return new Expr() {
                public Object valor(Contexto c) {
                    List<Object> v = new ArrayList<>();
                    for (Expr a : args) v.add(a.valor(c));
                    return chamar(f, v);
                }
                public boolean temAgregacao() {
                    for (Expr a : args) if (a.temAgregacao()) return true;
                    return false;
                }
                public String nome() { return rotulo; }
            };
        }

        Expr funcaoSemArgumento(String n) {
            return new Expr() {
                public Object valor(Contexto c) { return chamar(n.toUpperCase(Locale.ROOT), new ArrayList<>()); }
                public String nome() { return n; }
            };
        }

        Expr binaria(String op, Expr a, Expr b, java.util.function.Function<Contexto, Object> calc) {
            return new Expr() {
                public Object valor(Contexto c) { return calc.apply(c); }
                public boolean temAgregacao() { return a.temAgregacao() || b.temAgregacao(); }
                public String nome() { return a == b ? op + " " + a.nome() : a.nome() + " " + op + " " + b.nome(); }
            };
        }
    }

    /** Um valor fixo: 10, 'texto', NULL... */
    static class Literal implements Expr {
        final Object v;
        final String escrito;
        Literal(Object v, String escrito) { this.v = v; this.escrito = escrito; }
        public Object valor(Contexto c) { return v; }
        public String nome() { return escrito; }
    }

    /** O nome de uma coluna (ou de uma variável do PL/SQL). */
    class RefColuna implements Expr {
        final String tabela, coluna;
        RefColuna(String tabela, String coluna) { this.tabela = tabela; this.coluna = coluna; }
        public Object valor(Contexto c) {
            String chave = (tabela == null ? "" : tabela.toLowerCase() + ".") + coluna.toLowerCase();
            if (c.linha.containsKey(chave)) {
                if (tabela == null && c.linha.containsKey("?" + chave)) {
                    throw new Erro("a coluna \"" + coluna + "\" existe em duas tabelas: escreva tabela." + coluna);
                }
                return c.linha.get(chave);
            }
            if (tabela == null && variaveis.containsKey(chave)) return variaveis.get(chave);
            throw new Erro("não existe a coluna \"" + (tabela == null ? "" : tabela + ".") + coluna + "\"");
        }
        public String nome() { return coluna; }
    }

    // =====================================================================
    //  FUNÇÕES (UPPER, LENGTH, ROUND...)
    // =====================================================================

    private static final Random SORTE = new Random();

    Object chamar(String f, List<Object> a) {
        Object x = a.isEmpty() ? null : a.get(0);
        switch (f) {
            case "UPPER": case "UCASE": return x == null ? null : texto(x).toUpperCase();
            case "LOWER": case "LCASE": return x == null ? null : texto(x).toLowerCase();
            case "LENGTH": case "LEN": case "CHAR_LENGTH": return x == null ? null : (long) texto(x).length();
            case "TRIM": case "LTRIM": case "RTRIM": return x == null ? null : texto(x).trim();
            case "CONCAT": { StringBuilder sb = new StringBuilder(); for (Object o : a) if (o != null) sb.append(texto(o)); return sb.toString(); }
            case "SUBSTRING": case "SUBSTR": case "MID": {
                if (x == null) return null;
                String s = texto(x);
                int ini = (int) numero(a.get(1)).longValue() - 1;
                int tam = a.size() > 2 ? (int) numero(a.get(2)).longValue() : s.length();
                ini = Math.max(0, Math.min(ini, s.length()));
                return s.substring(ini, Math.min(s.length(), ini + tam));
            }
            case "REPLACE": return x == null ? null : texto(x).replace(texto(a.get(1)), texto(a.get(2)));
            case "LEFT": return x == null ? null : texto(x).substring(0, Math.min(texto(x).length(), (int) numero(a.get(1)).longValue()));
            case "RIGHT": { if (x == null) return null; String s = texto(x); int n = (int) numero(a.get(1)).longValue(); return s.substring(Math.max(0, s.length() - n)); }
            case "ABS": return x == null ? null : (x instanceof Long ? (Object) Math.abs((Long) x) : (Object) Math.abs(numero(x).doubleValue()));
            case "ROUND": {
                if (x == null) return null;
                int casas = a.size() > 1 ? (int) numero(a.get(1)).longValue() : 0;
                BigDecimal b = new BigDecimal(numero(x).toString()).setScale(casas, java.math.RoundingMode.HALF_UP);
                return casas <= 0 ? (Object) b.longValue() : (Object) b.doubleValue();
            }
            case "FLOOR": return x == null ? null : (long) Math.floor(numero(x).doubleValue());
            case "CEIL": case "CEILING": return x == null ? null : (long) Math.ceil(numero(x).doubleValue());
            case "SQRT": return x == null ? null : Math.sqrt(numero(x).doubleValue());
            case "POWER": case "POW": return Math.pow(numero(x).doubleValue(), numero(a.get(1)).doubleValue());
            case "MOD": return conta("%", x, a.get(1));
            case "RAND": case "RANDOM": return SORTE.nextDouble();
            case "COALESCE": case "IFNULL": case "ISNULL": case "NVL": for (Object o : a) if (o != null) return o; return null;
            case "NOW": case "GETDATE": case "SYSDATE": case "CURRENT_TIMESTAMP": case "SYSDATETIME":
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            case "CURDATE": case "CURRENT_DATE": return LocalDate.now().toString();
            case "YEAR": return x == null ? null : Long.parseLong(texto(x).substring(0, 4));
            case "TO_CHAR": case "STR": return x == null ? null : texto(x);
            case "TO_NUMBER": return x == null ? null : numero(x);
            case "NEWID": case "UUID": case "GEN_RANDOM_UUID": case "UUID_GENERATE_V4": return java.util.UUID.randomUUID().toString();
            case "TOTIMESTAMP": case "TODATE": case "DATE": return x;
            case "STRING::UPPERCASE": return x == null ? null : texto(x).toUpperCase();
            default: throw new Erro("não conheço a função " + f + "()");
        }
    }

    // =====================================================================
    //  AJUDANTES DE VALORES
    // =====================================================================

    private Contexto vazio() {
        return new Contexto(new LinkedHashMap<>(), null);
    }

    Map<String, Object> mapaDaLinha(Tabela tab, Object[] linha, String apelido) {
        Map<String, Object> m = new LinkedHashMap<>();
        String a = (apelido != null ? apelido : tab.nome).toLowerCase();
        for (int i = 0; i < tab.colunas.size(); i++) {
            String c = tab.colunas.get(i).nome.toLowerCase();
            m.put(a + "." + c, linha[i]);
            if (!a.equals(tab.nome.toLowerCase())) m.put(tab.nome.toLowerCase() + "." + c, linha[i]);
            m.put(c, linha[i]);
        }
        return m;
    }

    /** SurrealDB não tem "esquema": se a coluna não existe, a gente cria na hora. */
    void garantirColuna(Tabela tab, String nome) {
        if (tab.indice(nome) >= 0) return;
        Coluna c = new Coluna(nome, "ANY");
        if (nome.equalsIgnoreCase("id")) c.chave = true;
        tab.colunas.add(c);
        for (int i = 0; i < tab.linhas.size(); i++) tab.linhas.set(i, java.util.Arrays.copyOf(tab.linhas.get(i), tab.colunas.size()));
    }

    Tabela tabela(String nome) {
        Tabela t = tabelas.get(nome.toLowerCase());
        if (t == null) throw new Erro("a tabela \"" + nome + "\" não existe (crie com CREATE TABLE)");
        return t;
    }

    void mostrarTabelas() {
        List<Object[]> l = new ArrayList<>();
        for (Tabela t : tabelas.values()) l.add(new Object[]{t.nome, (long) t.linhas.size()});
        resultados.accept(new Resultado("SHOW TABLES", java.util.Arrays.asList("tabela", "linhas"), l));
    }

    void descrever(String nome) {
        Tabela t = tabela(nome);
        List<Object[]> l = new ArrayList<>();
        for (Coluna c : t.colunas) {
            String extras = (c.chave ? "PRIMARY KEY " : "") + (c.autoIncremento ? "AUTO_INCREMENT " : "")
                    + (c.naoNulo ? "NOT NULL " : "") + (c.unica ? "UNIQUE" : "");
            l.add(new Object[]{c.nome, c.tipo.toUpperCase(), extras.trim()});
        }
        resultados.accept(new Resultado("DESCRIBE " + t.nome, java.util.Arrays.asList("coluna", "tipo", "detalhes"), l));
    }

    Object converter(Object v, Coluna c) {
        if (v == null) return null;
        String t = c.tipo.toUpperCase();
        try {
            if (t.contains("INT") || t.equals("SERIAL") || t.equals("BIT")) {
                if (v instanceof Boolean) return ((Boolean) v) ? 1L : 0L;
                return numero(v).longValue();
            }
            if (t.contains("DEC") || t.contains("NUM") || t.contains("FLOAT") || t.contains("REAL") || t.contains("DOUBLE") || t.equals("MONEY")) {
                Number n = numero(v);
                return (n instanceof Long && t.equals("NUMBER")) ? n : (Object) n.doubleValue();
            }
            if (t.startsWith("BOOL")) return verdade(v);
        } catch (Erro e) {
            throw new Erro("o valor " + texto(v) + " não serve pra coluna \"" + c.nome + "\" (" + c.tipo + ")");
        }
        if (t.contains("CHAR") || t.contains("TEXT") || t.contains("DATE") || t.contains("TIME")) return texto(v);
        return v;
    }

    static boolean ehNumerico(Object o) {
        if (o instanceof Number) return true;
        if (o instanceof String) {
            try { Double.parseDouble(((String) o).trim()); return true; } catch (NumberFormatException e) { return false; }
        }
        return false;
    }

    static Number numero(Object o) {
        if (o instanceof Long || o instanceof Double) return (Number) o;
        if (o instanceof Number) return ((Number) o).doubleValue();
        if (o instanceof Boolean) return ((Boolean) o) ? 1L : 0L;
        if (o instanceof String) {
            String s = ((String) o).trim();
            try { return Long.parseLong(s); } catch (NumberFormatException e) { /* tenta decimal */ }
            try { return Double.parseDouble(s); } catch (NumberFormatException e) { /* erro abaixo */ }
        }
        throw new Erro("\"" + texto(o) + "\" não é um número");
    }

    Object conta(String op, Object x, Object y) {
        Number a = numero(x), b = numero(y);
        boolean inteiros = a instanceof Long && b instanceof Long;
        if (inteiros) {
            long p = a.longValue(), q = b.longValue();
            switch (op) {
                case "+": return p + q;
                case "-": return p - q;
                case "*": return p * q;
                case "%": if (q == 0) throw new Erro("divisão por zero"); return p % q;
                case "/":
                    if (q == 0) throw new Erro("divisão por zero");
                    if (dialeto.equals("TSQL") || p % q == 0) return p / q;   // SQL Server: 5/2 = 2
                    return (double) p / q;
            }
        }
        double p = a.doubleValue(), q = b.doubleValue();
        switch (op) {
            case "+": return p + q;
            case "-": return p - q;
            case "*": return p * q;
            case "%": return p % q;
            default: if (q == 0) throw new Erro("divisão por zero"); return p / q;
        }
    }

    static int comparar(Object a, Object b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        if (ehNumerico(a) && ehNumerico(b) && (a instanceof Number || b instanceof Number)) {
            return Double.compare(numero(a).doubleValue(), numero(b).doubleValue());
        }
        if (a instanceof Boolean || b instanceof Boolean) return Boolean.compare(verdade(a), verdade(b));
        return texto(a).compareToIgnoreCase(texto(b));   // 'joão' = 'João'
    }

    static boolean iguais(Object a, Object b) {
        return a != null && b != null && comparar(a, b) == 0;
    }

    static Object chaveGrupo(Object o) {
        if (o == null) return "\u0000NULL";
        if (o instanceof Number) return new BigDecimal(o.toString()).stripTrailingZeros();
        return texto(o).toLowerCase();
    }

    static boolean verdade(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0;
        String s = o.toString();
        return !s.isEmpty() && !s.equals("0") && !s.equalsIgnoreCase("false");
    }

    /** LIKE: % = qualquer coisa, _ = uma letra. Não liga pra maiúsculas. */
    static boolean como(String texto, String padrao) {
        StringBuilder re = new StringBuilder();
        for (char c : padrao.toCharArray()) {
            if (c == '%') re.append(".*");
            else if (c == '_') re.append('.');
            else re.append(Pattern.quote(String.valueOf(c)));
        }
        return Pattern.compile(re.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE)
                .matcher(texto).matches();
    }

    /** Como um valor aparece na tela. */
    public static String texto(Object o) {
        if (o == null) return "NULL";
        if (o instanceof Double) {
            double d = (Double) o;
            if (Double.isNaN(d) || Double.isInfinite(d)) return String.valueOf(d);
            return new BigDecimal(d).setScale(6, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        }
        if (o instanceof Boolean) return ((Boolean) o) ? "TRUE" : "FALSE";
        return o.toString();
    }
}
