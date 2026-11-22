package tazzcode;

import static tazzcode.Catalogo.nova;

/**
 * Tudo o que tem no OneCompiler (onecompiler.com/languages) e ainda faltava no TazzCode:
 *   - linguagens novas: J, Icon, ISPC, JShell, EJS
 *   - "Rodar com": Node.js / Deno / Bun, Python 3 / Python 2, Octave / MATLAB, Bash / sh
 *   - os 16 bancos de dados (todos SIMULADOS): MySQL, PostgreSQL, SQLite, MariaDB, Oracle,
 *     SQL Server, IBM Db2, Cassandra, QuestDB, DuckDB, SurrealDB, Firebird, ClickHouse,
 *     MongoDB e Redis
 * (Os frameworks, tipo React, Flask e Spring Boot, viraram MODELOS DE PROJETO: veja ModelosProjeto.java)
 */
class CatalogoOneCompiler {

    private static final String OC = "OneCompiler - linguagens";
    private static final String BD = "OneCompiler - bancos de dados";

    static void cadastrar() {

        // ===== "Rodar com..." nas linguagens que já existiam =====================

        Catalogo.porNome("JavaScript")
            .rodarCom("Node.js", "node {arquivo}", "Node.js: https://nodejs.org")
            .rodarCom("Deno", "deno run -A {arquivo}", "Deno: https://deno.com (no PowerShell: irm https://deno.land/install.ps1 | iex)")
            .rodarCom("Bun", "bun {arquivo}", "Bun: https://bun.sh (no PowerShell: irm bun.sh/install.ps1 | iex)");

        Catalogo.porNome("TypeScript")
            .rodarCom("Node.js (tsx)", "npx -y tsx {arquivo}", "Node.js: https://nodejs.org")
            .rodarCom("Deno", "deno run -A {arquivo}", "Deno: https://deno.com")
            .rodarCom("Bun", "bun {arquivo}", "Bun: https://bun.sh");

        Catalogo.porNome("Python")
            .rodarCom("Python 3", "python -u {arquivo}", "Python: https://www.python.org/downloads (marque \"Add python.exe to PATH\")")
            .rodarCom("Python 2", "py -2 -u {arquivo}", "Python 2 é bem antigo (acabou em 2020). Se quiser mesmo: https://www.python.org/downloads/release/python-2718");

        Catalogo.porNome("MATLAB")
            .rodarCom("Octave", "octave-cli {arquivo}", "GNU Octave (grátis): https://octave.org/download")
            .rodarCom("MATLAB", "matlab -batch {nome}", "MATLAB (pago): https://www.mathworks.com");

        Catalogo.porNome("Shell / Bash")
            .rodarCom("Bash", "bash {arquivo}", "Bash (vem com o Git for Windows): https://git-scm.com")
            .rodarCom("sh (Shell Script)", "sh {arquivo}", "sh (vem com o Git for Windows): https://git-scm.com");

        Catalogo.porNome("Java")
            .rodarCom("Java (arquivo único)", "java -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 {arquivo}",
                    "JDK 21: https://adoptium.net (marque \"Add to PATH\")")
            .rodarCom("Java (compilar a pasta toda)", "javac -encoding UTF-8 -d {saidaPasta}/java_{nome} {todos} && java -Dstdout.encoding=UTF-8 -cp {saidaPasta}/java_{nome} {nome}",
                    "JDK 21: https://adoptium.net. Use esse quando o programa tem VÁRIAS classes em arquivos separados.");

        Catalogo.porNome("Clojure")
            .rodarCom("Clojure CLI", "clojure -M {arquivo}", "Clojure: https://clojure.org/guides/install_clojure")
            .rodarCom("Babashka (bb)", "bb {arquivo}", "Babashka: tem plugin no TazzCode! Ou https://babashka.org");

        // ===== linguagens novas ===============================================

        nova(OC, "JShell (Java interativo)", "jsh")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("int double boolean char long var String if else for while return new class void true false null")
            .funcoes("System.out.println printf")
            .tipos("String Math List Map")
            .letras(".")
            .modelo("// JShell: Java sem precisar de classe nem main!\nint x = 5;\nSystem.out.println(\"x vezes 2 = \" + (x * 2));\n/exit\n")
            .rodar("jshell -q {arquivo}")
            .precisa("Vem junto com o JDK (comando jshell). Termine o arquivo com /exit.")
            .atalho("sout", "imprime", "System.out.println($|);");

        nova(OC, "J", "ijs")
            .linha("NB.").aspas("'")
            .palavras("if. do. else. elseif. end. while. for. select. case. return.")
            .funcoes("echo exit i. # +/ */ >: <:")
            .modelo("NB. J: uma linguagem de matemática com símbolos curtinhos\necho 'Olá, mundo!'\necho +/ 1 2 3 4 5\nexit ''\n")
            .rodar("jconsole {arquivo}")
            .precisa("J: https://code.jsoftware.com/wiki/System/Installation");

        nova(OC, "Icon", "icn")
            .linha("#").aspas("\"'")
            .palavras("procedure end local global static if then else every do while until repeat return suspend fail break next to by")
            .funcoes("write writes read find upto many move tab")
            .modelo("procedure main()\n    write(\"Olá, mundo!\")\n    every i := 1 to 3 do write(i)\nend\n")
            .rodar("icon {arquivo}")
            .precisa("Icon: https://www2.cs.arizona.edu/icon")
            .atalho("write", "mostra", "write($|)");

        nova(OC, "ISPC", "ispc")
            .linha("//").bloco("/*", "*/").aspas("\"")
            .palavras("export uniform varying foreach foreach_tiled programIndex programCount if else for while return void struct")
            .funcoes("print reduce_add")
            .tipos("int float double bool int8 int16 int32 int64")
            .modelo("// ISPC: C que roda em várias \"pistas\" do processador ao mesmo tempo\nexport void dobrar(uniform float valores[], uniform int n) {\n"
                    + "    foreach (i = 0 ... n) {\n        valores[i] = valores[i] * 2;\n    }\n}\n")
            .rodar("ispc {arquivo} -o {saidaBase}.o")
            .precisa("ISPC (Intel): https://ispc.github.io/downloads.html. Ele só COMPILA: quem roda é um programa em C/C++.");

        nova(OC, "EJS (Embedded JavaScript)", "ejs")
            .estilo(Linguagem.Estilo.HTML)
            .modelo("<% const nome = 'João'; %>\n<h1>Olá, <%= nome %>!</h1>\n<ul>\n<% for (let i = 1; i <= 3; i++) { %>\n  <li>Item <%= i %></li>\n<% } %>\n</ul>\n")
            .rodar("npx -y ejs {arquivo}")
            .precisa("Node.js: https://nodejs.org (o EJS vem pelo npx). Ele mostra o HTML que o template gera.");

        // ===== BANCOS DE DADOS (todos simulados) ===============================

        // O SQL (.sql) ganha um "Rodar com" pra escolher qual banco simular.
        Catalogo.porNome("SQL")
            .bancoCom("MySQL", "SQL")
            .bancoCom("MariaDB", "SQL")
            .bancoCom("PostgreSQL", "SQL")
            .bancoCom("SQLite", "SQL")
            .bancoCom("Microsoft SQL Server", "TSQL")
            .bancoCom("Oracle Database", "PLSQL")
            .bancoCom("IBM Db2", "SQL")
            .bancoCom("DuckDB", "SQL")
            .bancoCom("QuestDB", "SQL")
            .bancoCom("Firebird", "SQL")
            .bancoCom("ClickHouse", "SQL");

        nova(BD, "PostgreSQL", "pgsql", "psql")
            .semCaixa().linha("--").bloco("/*", "*/").aspas("'\"")
            .palavras(Sql.PALAVRAS + " RETURNING SERIAL ILIKE")
            .funcoes(Sql.FUNCOES + " GEN_RANDOM_UUID")
            .tipos(Sql.TIPOS + " SERIAL BIGSERIAL JSONB UUID")
            .banco("SQL").bancoCom("PostgreSQL", "SQL")
            .modelo("-- PostgreSQL SIMULADO (não é o banco de verdade!)\nCREATE TABLE jogos (\n    id SERIAL PRIMARY KEY,\n    nome TEXT NOT NULL,\n    nota INT\n);\n\n"
                    + "INSERT INTO jogos (nome, nota) VALUES ('Minecraft', 10), ('BlockBricks', 10), ('Roblox', 9);\n\nSELECT nome, nota FROM jogos ORDER BY nota DESC, nome;\n")
            .precisa("Nada! O TazzCode simula o PostgreSQL.");

        nova(BD, "MySQL / MariaDB", "mysql")
            .semCaixa().linha("--", "#").bloco("/*", "*/").aspas("'\"")
            .palavras(Sql.PALAVRAS).funcoes(Sql.FUNCOES).tipos(Sql.TIPOS)
            .banco("SQL").bancoCom("MySQL", "SQL").bancoCom("MariaDB", "SQL")
            .modelo(Catalogo.porNome("SQL").modelo)
            .precisa("Nada! O TazzCode simula o MySQL.");

        nova(BD, "Cassandra (CQL)", "cql")
            .semCaixa().linha("--", "//").bloco("/*", "*/").aspas("'\"")
            .palavras(Sql.PALAVRAS + " KEYSPACE WITH REPLICATION ALLOW FILTERING TTL USING")
            .funcoes(Sql.FUNCOES + " UUID TOTIMESTAMP")
            .tipos(Sql.TIPOS + " UUID TIMEUUID LIST MAP")
            .banco("CQL").bancoCom("Cassandra", "CQL")
            .modelo("-- Cassandra SIMULADO (não é o banco de verdade!)\nCREATE KEYSPACE escola WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};\n"
                    + "USE escola;\n\nCREATE TABLE alunos (\n    turma text,\n    id uuid,\n    nome text,\n    PRIMARY KEY ((turma), id)\n);\n\n"
                    + "INSERT INTO alunos (turma, id, nome) VALUES ('7A', uuid(), 'João');\nINSERT INTO alunos (turma, id, nome) VALUES ('7A', uuid(), 'Maria');\n\n"
                    + "SELECT * FROM alunos WHERE turma = '7A';\n")
            .precisa("Nada! O TazzCode simula o Cassandra.");

        nova(BD, "SurrealDB (SurrealQL)", "surql")
            .semCaixa().linha("--", "//", "#").bloco("/*", "*/").aspas("'\"")
            .palavras(Sql.PALAVRAS + " CONTENT DEFINE INFO FOR DB RELATE RETURN")
            .funcoes(Sql.FUNCOES).tipos(Sql.TIPOS)
            .banco("SURREAL").bancoCom("SurrealDB", "SURREAL")
            .modelo("-- SurrealDB SIMULADO (não é o banco de verdade!)\n-- Não precisa criar a tabela antes: ela nasce sozinha.\n"
                    + "CREATE jogador SET nome = 'João', nivel = 7;\nCREATE jogador:maria CONTENT { nome: 'Maria', nivel: 9, itens: ['espada', 'escudo'] };\n\n"
                    + "SELECT * FROM jogador WHERE nivel > 5;\nUPDATE jogador SET nivel = nivel + 1 WHERE nome = 'João';\nSELECT nome, nivel FROM jogador;\n")
            .precisa("Nada! O TazzCode simula o SurrealDB.");

        nova(BD, "MongoDB", "mongo", "mongodb")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("db use show true false null")
            .funcoes("insertOne insertMany find findOne updateOne updateMany replaceOne deleteOne deleteMany countDocuments "
                    + "distinct aggregate sort limit skip drop createCollection getCollectionNames print")
            .tipos("$set $inc $push $pull $unset $gt $gte $lt $lte $ne $in $nin $regex $exists $and $or $match $group $sum $avg $project")
            .letras("$")
            .motor("MONGO").bancoCom("MongoDB", "MONGO")
            .modelo("// MongoDB SIMULADO (não é o banco de verdade!)\nuse escola\n\ndb.alunos.insertMany([\n  { nome: 'João', idade: 12, jogos: ['Minecraft', 'Roblox'] },\n"
                    + "  { nome: 'Maria', idade: 13, cidade: 'Palmas' },\n  { nome: 'Pedro', idade: 11 }\n])\n\n"
                    + "db.alunos.find({ idade: { $gte: 12 } }).sort({ nome: 1 })\n\n"
                    + "db.alunos.updateOne({ nome: 'Pedro' }, { $set: { idade: 12 } })\n\n"
                    + "db.alunos.aggregate([\n  { $group: { _id: null, media: { $avg: '$idade' }, total: { $sum: 1 } } }\n])\n")
            .precisa("Nada! O TazzCode simula o MongoDB.")
            .atalho("find", "busca", "db.$|.find({})")
            .atalho("insertOne", "insere 1", "db.$|.insertOne({  })")
            .atalho("set", "$set", "{ $set: { $| } }");

        nova(BD, "Redis", "redis")
            .semCaixa().linha("#", "//").aspas("\"'")
            .palavras("SET GET DEL EXISTS INCR DECR INCRBY DECRBY APPEND MSET MGET KEYS TYPE EXPIRE TTL PERSIST RENAME "
                    + "FLUSHALL DBSIZE PING ECHO LPUSH RPUSH LPOP RPOP LRANGE LLEN LINDEX SADD SREM SMEMBERS SISMEMBER SCARD "
                    + "HSET HGET HGETALL HDEL HKEYS HVALS HLEN HINCRBY ZADD ZRANGE ZREVRANGE ZSCORE ZINCRBY ZCARD ZREM ZRANK WITHSCORES EX")
            .motor("REDIS").bancoCom("Redis", "REDIS")
            .modelo("# Redis SIMULADO (não é o banco de verdade!) - um comando por linha\nSET jogador \"João\"\nGET jogador\n\n"
                    + "INCR moedas\nINCRBY moedas 50\n\nZADD ranking 120 João 95 Maria 80 Pedro\nZREVRANGE ranking 0 -1 WITHSCORES\n\n"
                    + "HSET jogador:1 nome João nivel 7\nHGETALL jogador:1\n\nKEYS *\n")
            .precisa("Nada! O TazzCode simula o Redis.");
    }
}
