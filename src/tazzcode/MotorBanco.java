package tazzcode;

import java.util.List;
import java.util.function.Consumer;

/**
 * Um "MOTOR" de banco de dados simulado. Cada tipo de banco tem o seu:
 *   Sql             -> MySQL, PostgreSQL, SQLite, SQL Server, Oracle, Cassandra, SurrealDB...
 *   MongoSimulado   -> MongoDB (documentos JSON)
 *   RedisSimulado   -> Redis (chave = valor)
 * A JanelaBanco funciona com qualquer um deles.
 */
public interface MotorBanco {

    /** Roda o código. Mensagens vão pra "mensagens", tabelas de resultado vão pra "resultados". */
    void executar(String codigo, Consumer<String> mensagens, Consumer<Sql.Resultado> resultados);

    /** Apaga tudo. */
    void limpar();

    /** O que aparece na lista da esquerda: {nome, quantidade}. */
    List<String[]> itens();

    /** O comando que mostra um item quando você clica nele (ex.: SELECT * FROM tabela). */
    String comandoParaVer(String item);

    /** "TABELAS", "COLEÇÕES" ou "CHAVES". */
    String tituloItens();

    /** O que aparece antes da caixinha de comando: "SQL>", "mongo>", "redis>". */
    String prompt();
}
