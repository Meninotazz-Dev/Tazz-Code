package tazzcode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * O TazzHelper SEM INTERNET (sem chave da API).
 * Ele não "pensa" igual a IA: ele conhece uma lista de erros comuns e explica cada um.
 *
 * Funciona assim: cada REGRA tem um "padrão" (um pedaço de texto que aparece no erro)
 * e uma explicação. Se o padrão aparece no console, a explicação vai pra resposta.
 *
 * DESAFIO: ensine um erro novo pro TazzHelper! Copie uma regra lá embaixo e mude.
 */
public class AjudanteOffline {

    private static class Regra {
        final Pattern padrao;
        final String explicacao;   // $1, $2... = pedaços que o padrão capturou
        Regra(String padrao, String explicacao) {
            this.padrao = Pattern.compile(padrao, Pattern.CASE_INSENSITIVE);
            this.explicacao = explicacao;
        }
    }

    private static final List<Regra> REGRAS = new ArrayList<>();

    static {
        // ----- Java -----
        r("cannot find symbol[\\s\\S]*?symbol:\\s+(?:variable|method|class)\\s+(\\w+)",
          "**O Java não conhece `$1`.** Pode ser: nome escrito errado (maiúscula/minúscula conta!), variável criada dentro de outro bloco `{ }`, ou faltou um `import`.");
        r("cannot find symbol", "**O Java não achou um nome** (variável, método ou classe). Confira se escreveu igualzinho, com maiúsculas e minúsculas.");
        r("';' expected", "**Faltou um ponto e vírgula `;`** no fim de uma linha. O número depois do `.java:` diz a linha.");
        r("class (\\w+) is public, should be declared in a file named (\\S+)",
          "**O nome da classe tem que ser igual ao nome do arquivo.** A classe é `$1`, então o arquivo tem que se chamar `$2`.");
        r("incompatible types: (.+?) cannot be converted to (\\S+)",
          "**Tipos diferentes:** você tentou guardar um `$1` onde só cabe `$2`. Ex.: texto (`String`) num `int`. Use `Integer.parseInt(...)` pra converter texto em número.");
        r("missing return statement", "**Faltou o `return`.** O método promete devolver um valor, mas em algum caminho ele termina sem `return`.");
        r("reached end of file while parsing", "**Faltou fechar uma chave `}`.** Conte as `{` e as `}`: tem que ter o mesmo tanto.");
        r("unclosed string literal", "**Um texto ficou sem fechar as aspas.** Todo `\"` que abre precisa de outro `\"` fechando, na mesma linha.");
        r("non-static (?:variable|method) (\\w+).*cannot be referenced from a static context",
          "**`$1` não é `static`, mas o `main` é.** Ou coloque `static` em `$1`, ou crie um objeto (`new MinhaClasse()`) e use por ele.");
        r("NullPointerException", "**NullPointerException:** você usou um objeto que está vazio (`null`). Veja a linha do erro e confira se a variável recebeu um valor (um `new ...`) antes.");
        r("ArrayIndexOutOfBoundsException.*?Index (-?\\d+) out of bounds for length (\\d+)",
          "**Posição fora da lista:** você pediu a posição $1, mas a lista tem $2 itens (as posições vão de 0 até $2 menos 1).");
        r("StringIndexOutOfBoundsException", "**Posição fora do texto:** você pediu uma letra numa posição que o texto não tem. Lembre: a primeira letra é a posição 0.");
        r("InputMismatchException", "**O Scanner esperava um número e veio texto** (ou o contrário). Digite do jeito que o programa pede, ou use `nextLine()` e converta.");
        r("NumberFormatException: For input string: \"(.*?)\"", "**\"$1\" não é um número**, então não deu pra converter. Confira o que foi digitado.");
        r("ArithmeticException: / by zero", "**Divisão por zero!** Nenhum número pode ser dividido por 0. Confira o valor do divisor antes de dividir.");
        r("error: compilation failed", "O Java **nem chegou a rodar**: tem erro de escrita no código. Os erros aparecem logo acima, com o número da linha.");

        // ----- Python -----
        r("IndentationError", "**Erro de recuo (espaços no começo da linha).** No Python, o que está dentro de um `if`, `for` ou `def` tem que ter 4 espaços a mais, todos iguais.");
        r("SyntaxError: (.+)", "**Erro de escrita (sintaxe):** $1. Confira parênteses, aspas e os dois pontos `:` no fim de `if`, `for`, `def`...");
        r("NameError: name '(\\w+)' is not defined", "**O Python não conhece `$1`.** Talvez você escreveu errado, ou usou antes de criar (`$1 = ...`).");
        r("ModuleNotFoundError: No module named '([\\w.]+)'", "**Falta instalar a biblioteca `$1`.** Abra o Terminal (botão `>_ Terminal`) e digite: `python -m pip install $1`");
        r("TypeError: can only concatenate str \\(not \"(\\w+)\"\\) to str", "**Não dá pra juntar texto com $1 usando `+`.** Converta com `str(...)` ou use f-string: `f\"Idade: {idade}\"`.");
        r("TypeError: (.+)", "**Tipo errado:** $1. Você usou um valor de um jeito que ele não aceita (ex.: somar texto com número).");
        r("ZeroDivisionError", "**Divisão por zero!** Confira o número que está embaixo da divisão.");
        r("IndexError: list index out of range", "**Posição fora da lista.** A lista é menor do que você pensou. Lembre que começa no 0.");
        r("KeyError: (.+)", "**A chave $1 não existe no dicionário.** Use `.get(...)` ou confira se ela foi colocada antes.");
        r("ValueError: invalid literal for int\\(\\) with base 10: '(.*?)'", "**\"$1\" não é um número inteiro**, então o `int()` não conseguiu converter.");
        r("AttributeError: '(\\w+)' object has no attribute '(\\w+)'", "**O tipo `$1` não tem `$2`.** Confira o nome (talvez é outro método).");

        // ----- JavaScript / Node -----
        r("ReferenceError: (\\w+) is not defined", "**O JavaScript não conhece `$1`.** Escreveu certo? Criou com `let`/`const` antes de usar?");
        r("TypeError: Cannot read propert(?:y|ies) of (undefined|null)", "**Você usou algo que está `$2`.** Ex.: `pessoa.nome` quando `pessoa` ainda não existe.");
        r("Cannot find module '(.+?)'", "**Falta instalar o pacote `$1`.** No Terminal: `npm install $1`");
        r("SyntaxError: Unexpected token", "**Erro de escrita:** tem um símbolo fora do lugar (chave, parêntese ou vírgula sobrando/faltando).");

        // ----- C / C++ -----
        r("undefined reference to `?(\\w+)", "**O compilador não achou a função `$1`.** Escreveu o nome certo? Se for de biblioteca, faltou ligar ela (ex.: `-lm`).");
        r("expected ';' before", "**Faltou um `;`** antes do ponto que o erro mostra.");
        r("implicit declaration of function '(\\w+)'", "**Faltou o `#include` da função `$1`** (ou ela está escrita errado).");

        // ----- instalação / sistema -----
        r("não está instalado|is not recognized as an internal or external command|command not found",
          "**O programa dessa linguagem não está instalado** (ou o Windows não sabe onde ele está). O console mostrou o site pra baixar. Depois de instalar, feche e abra o TazzCode.");
        r("Address already in use|EADDRINUSE", "**A porta já está sendo usada:** o servidor já está rodando (talvez em outra janela). Aperte Parar e rode de novo.");

        // ----- bancos simulados -----
        r("ERRO: a tabela \"(\\w+)\" não existe", "**A tabela `$1` não existe ainda.** Crie com `CREATE TABLE $1 (...)` antes de usar.");
        r("ERRO: não existe a coluna \"(.+?)\"", "**A coluna `$1` não existe.** Use `DESCRIBE tabela` pra ver as colunas que existem.");
        r("ERRO: .*valor repetido", "**Valor repetido numa coluna que não pode repetir** (PRIMARY KEY ou UNIQUE). Use outro valor.");
    }

    private static void r(String padrao, String explicacao) {
        REGRAS.add(new Regra(padrao, explicacao));
    }

    /** Lê o que aconteceu (console + problemas) e explica. */
    public static String explicar(String console, List<VerificadorHtml.Problema> problemas, String nomeArquivo) {
        StringBuilder sb = new StringBuilder();
        List<String> ditas = new ArrayList<>();
        for (Regra r : REGRAS) {
            Matcher m = r.padrao.matcher(console == null ? "" : console);
            if (!m.find()) continue;
            String ex = r.explicacao;
            for (int g = 1; g <= m.groupCount(); g++) ex = ex.replace("$" + g, m.group(g) == null ? "" : m.group(g).trim());
            String chave = r.explicacao.substring(0, Math.min(25, r.explicacao.length()));
            if (ditas.contains(chave)) continue;
            // Regras gerais (ex.: "cannot find symbol") só valem se uma específica não explicou
            if (!ditas.isEmpty() && (r.padrao.pattern().equals("cannot find symbol") || r.padrao.pattern().startsWith("TypeError: (.+)")
                    || r.padrao.pattern().startsWith("SyntaxError: (.+)"))) continue;
            ditas.add(chave);
            sb.append("• ").append(ex).append("\n\n");
            if (ditas.size() >= 4) break;
        }
        Matcher linha = Pattern.compile("(?:\\.java|\\.py|\\.js|\\.c|\\.cpp)[:\"]\\s*(?:line\\s*)?(\\d+)").matcher(console == null ? "" : console);
        if (sb.length() > 0 && linha.find()) sb.append("Olhe a **linha ").append(linha.group(1)).append("** do arquivo.\n\n");

        if (problemas != null && !problemas.isEmpty()) {
            int n = 0;
            for (VerificadorHtml.Problema p : problemas) {
                if (p.gravidade == VerificadorHtml.Gravidade.DICA) continue;
                if (n++ == 0) sb.append("No HTML eu vi:\n");
                sb.append("• ").append(p.mensagem).append(p.temCorrecao() ? " (dá pra corrigir com Alt+Enter)" : "").append("\n");
                if (n >= 5) break;
            }
        }
        if (sb.length() == 0) {
            if (console != null && console.contains("[Terminou com código 0")) {
                return "O último programa rodou **sem erro**! Se ele não fez o que você queria, me conte o que esperava (com a chave da API eu entendo perguntas livres).";
            }
            return "Não achei nenhum erro que eu conheço" + (nomeArquivo == null ? "." : " em " + nomeArquivo + ".")
                    + " Rode o código (F5) e, se der erro, clique em **Explicar o erro** de novo.";
        }
        return sb.toString().trim();
    }

    /** Resposta pra uma pergunta livre quando não tem chave da API. */
    public static String responder(String pergunta, String console, List<VerificadorHtml.Problema> problemas, String nomeArquivo) {
        String p = pergunta.toLowerCase();
        if (p.contains("erro") || p.contains("errado") || p.contains("não funciona") || p.contains("nao funciona") || p.contains("bug")) {
            return explicar(console, problemas, nomeArquivo);
        }
        return "Sem internet/chave eu só sei **explicar erros comuns** (clique em *Explicar o erro*).\n\n"
                + "Pra conversar de verdade (perguntar qualquer coisa, pedir dicas, entender o código), "
                + "o TazzHelper precisa de uma **chave da API da Anthropic**: clique em *Configurar*. "
                + "A chave é paga, então peça pra um adulto criar em https://console.anthropic.com";
    }
}
