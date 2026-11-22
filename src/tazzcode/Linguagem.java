package tazzcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * UMA LINGUAGEM: tudo o que o editor sabe sobre ela.
 *   - nome e extensões (.py, .java...)
 *   - como são os comentários e os textos
 *   - palavras-chave, funções e tipos (pras cores e pro autocompletar)
 *   - atalhos do autocompletar
 *   - o modelo de arquivo novo (o "Olá, mundo!")
 *   - o comando pra rodar e o que precisa instalar
 *
 * As linguagens são cadastradas no Catalogo (CatalogoPrincipais, CatalogoEspeciais e CatalogoExtras).
 */
public class Linguagem {

    /** Como o editor roda essa linguagem quando você aperta F5. */
    public enum Modo {
        CONSOLE,      // roda um comando e mostra no console
        NAVEGADOR,    // abre o arquivo no navegador (HTML, SVG...)
        LINK,         // abre um site (ex.: Scratch)
        BANCO,        // abre a janela do banco de dados simulado
        BRAINFUCK,    // interpretador feito aqui dentro do TazzCode
        SO_EDITAR     // só dá pra editar aqui (explica como rodar)
    }

    /** Como pintar o código. */
    public enum Estilo { GENERICO, HTML, CSS }

    /** Um atalho do autocompletar ($| = onde o cursor fica). */
    public static class Atalho {
        public final String rotulo, dica, inserir;
        Atalho(String rotulo, String dica, String inserir) {
            this.rotulo = rotulo;
            this.dica = dica;
            this.inserir = inserir;
        }
    }

    /**
     * Um jeito de rodar a linguagem. Ex.: JavaScript pode rodar com Node.js, Deno ou Bun;
     * SQL pode simular MySQL, PostgreSQL, SQLite... Você escolhe no "Rodar com" da barra.
     */
    public static class Alternativa {
        public final String nome;
        public final List<String> comandos = new ArrayList<>();
        public String precisa = "";
        public String motor = "SQL";        // modo BANCO: SQL, MONGO ou REDIS
        public String dialeto = "SQL";      // modo BANCO: SQL, PLSQL, TSQL, CQL, SURREAL
        Alternativa(String nome) { this.nome = nome; }
        @Override public String toString() { return nome; }
    }

    public final String nome;
    public final List<String> extensoes;
    public final List<Alternativa> alternativas = new ArrayList<>();
    public String motorBanco = "SQL";
    public List<String> comentarioLinha = new ArrayList<>();
    public List<String[]> comentarioBloco = new ArrayList<>();
    public String aspas = "\"'";
    public String prefixosVariavel = "";      // ex.: "$" no PHP, "$@%" no Perl
    public String letrasExtras = "";          // letras a mais nos nomes (ex.: "-" no Lisp)
    public boolean ignoraMaiusculas = false;  // SQL, Pascal, VB... (IF = if)
    public Set<String> palavras = new HashSet<>();
    public Set<String> funcoes = new HashSet<>();
    public Set<String> tipos = new HashSet<>();
    /** As palavras do jeito que foram escritas (pro autocompletar), com uma dica de cada. */
    public java.util.Map<String, String> vocabulario = new java.util.LinkedHashMap<>();
    public List<Atalho> atalhos = new ArrayList<>();
    public String modelo = "";                // {nome} = nome do arquivo sem extensão
    public List<String> comandos = new ArrayList<>();   // cada um é um passo (ex.: compilar, depois rodar)
    public String precisa = "";               // o que instalar pra rodar
    public String link = "";                  // modo LINK
    public String dialetoSql = "SQL";         // modo BANCO: SQL, PLSQL ou TSQL
    public Modo modo = Modo.CONSOLE;
    public Estilo estilo = Estilo.GENERICO;
    public String categoria = "";

    public Linguagem(String nome, String... extensoes) {
        this.nome = nome;
        this.extensoes = Arrays.asList(extensoes);
    }

    // ---------- jeitinho de cadastrar em uma linha só (cada método devolve "this") ----------

    public Linguagem linha(String... marcas)            { comentarioLinha.addAll(Arrays.asList(marcas)); return this; }
    public Linguagem bloco(String abre, String fecha)   { comentarioBloco.add(new String[]{abre, fecha}); return this; }
    public Linguagem aspas(String a)                    { aspas = a; return this; }
    public Linguagem variaveis(String prefixos)         { prefixosVariavel = prefixos; return this; }
    public Linguagem letras(String extras)              { letrasExtras = extras; return this; }
    public Linguagem semCaixa()                         { ignoraMaiusculas = true; return this; }
    public Linguagem palavras(String lista)             { adicionar(palavras, lista, "palavra-chave"); return this; }
    public Linguagem funcoes(String lista)              { adicionar(funcoes, lista, "função"); return this; }
    public Linguagem tipos(String lista)                { adicionar(tipos, lista, "tipo"); return this; }
    public Linguagem modelo(String m)                   { modelo = m; return this; }
    public Linguagem precisa(String p)                  { precisa = p; return this; }
    public Linguagem modo(Modo m)                       { modo = m; return this; }
    public Linguagem estilo(Estilo e)                   { estilo = e; return this; }
    public Linguagem link(String url)                   { link = url; modo = Modo.LINK; return this; }
    public Linguagem categoria(String c)                { categoria = c; return this; }

    public Linguagem banco(String dialeto) {
        dialetoSql = dialeto;
        modo = Modo.BANCO;
        return this;
    }

    /** Banco que não é SQL: "MONGO" ou "REDIS". */
    public Linguagem motor(String motor) {
        motorBanco = motor;
        modo = Modo.BANCO;
        return this;
    }

    /** Mais um jeito de rodar (o primeiro cadastrado é o padrão). */
    public Linguagem rodarCom(String nomeAlt, String comandoComPassos, String precisaAlt) {
        Alternativa a = new Alternativa(nomeAlt);
        for (String passo : comandoComPassos.split("&&")) a.comandos.add(passo.trim());
        a.precisa = precisaAlt;
        alternativas.add(a);
        if (comandos.isEmpty()) { comandos.addAll(a.comandos); precisa = precisaAlt; }
        return this;
    }

    /** Mais um banco simulado (ex.: "PostgreSQL" com dialeto "SQL"). */
    public Linguagem bancoCom(String nomeAlt, String dialeto) {
        Alternativa a = new Alternativa(nomeAlt);
        a.dialeto = dialeto;
        a.motor = motorBanco;
        a.precisa = "Nada! O TazzCode simula o " + nomeAlt + " (não é o banco de verdade).";
        alternativas.add(a);
        return this;
    }

    /** O jeito de rodar que está escolhido agora (ou null se a linguagem só tem um). */
    public Alternativa escolhida() {
        if (alternativas.isEmpty()) return null;
        String salvo = Config.texto("rodarcom." + nome, "");
        for (Alternativa a : alternativas) if (a.nome.equals(salvo)) return a;
        return alternativas.get(0);
    }

    public void escolher(String nomeAlt) {
        Config.colocar("rodarcom." + nome, nomeAlt);
    }

    /** Cada "&&" separa um passo. Ex.: "gcc {arquivo} -o {saida} && {saida}" */
    public Linguagem rodar(String comandoComPassos) {
        for (String passo : comandoComPassos.split("&&")) comandos.add(passo.trim());
        return this;
    }

    public Linguagem atalho(String rotulo, String dica, String inserir) {
        atalhos.add(new Atalho(rotulo, dica, inserir));
        return this;
    }

    private void adicionar(Set<String> conjunto, String lista, String dica) {
        for (String p : lista.trim().split("\\s+")) {
            if (p.isEmpty()) continue;
            conjunto.add(ignoraMaiusculas ? p.toLowerCase() : p);
            vocabulario.putIfAbsent(p, dica);
        }
    }

    /** As palavras "no jeito certo" de comparar (minúsculas se a linguagem ignora maiúsculas). */
    public String normalizar(String palavra) {
        return ignoraMaiusculas ? palavra.toLowerCase() : palavra;
    }

    public String extensaoPrincipal() {
        return extensoes.isEmpty() ? "" : extensoes.get(0);
    }

    @Override
    public String toString() {
        return nome;
    }
}
