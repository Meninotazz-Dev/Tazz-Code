package tazzcode;

import static tazzcode.Catalogo.nova;

/**
 * Linguagens 1 a 25 da lista (as mais usadas no mundo).
 *
 * Nos comandos de rodar:
 *   {arquivo} = o arquivo inteiro (C:\...\Main.java)
 *   {nome}    = o nome sem extensão (Main)
 *   {pasta}   = a pasta do arquivo
 *   {saida}   = o programa compilado (fica em C:\Users\SeuNome\TazzCode\compilados)
 *   {saidaBase} = o mesmo, mas sem o ".exe"
 */
class CatalogoPrincipais {

    private static final String TOP = "1 a 10 - As mais usadas";
    private static final String ALTA = "11 a 25 - Alta demanda";

    static void cadastrar() {

        // 1. PYTHON ------------------------------------------------------------
        nova(TOP, "Python", "py", "pyw")
            .linha("#").aspas("\"'")
            .palavras("False None True and as assert async await break class continue def del elif else except "
                    + "finally for from global if import in is lambda nonlocal not or pass raise return try while with yield match case self")
            .funcoes("print input len range str int float bool list dict set tuple type isinstance open sum min max abs "
                    + "round sorted reversed enumerate zip map filter any all chr ord format help dir id hash iter next "
                    + "super getattr setattr hasattr append extend insert remove pop keys values items split join strip "
                    + "replace upper lower startswith endswith find count")
            .tipos("Exception ValueError TypeError KeyError IndexError")
            .modelo("print(\"Olá, mundo!\")\n")
            .rodar("python -u {arquivo}")
            .precisa("Python: https://www.python.org/downloads (marque \"Add python.exe to PATH\")")
            .atalho("print", "mostra na tela", "print($|)")
            .atalho("input", "lê do teclado", "input(\"$|\")")
            .atalho("def", "cria função", "def $|():\n    ")
            .atalho("if", "se...", "if $|:\n    ")
            .atalho("elif", "senão se...", "elif $|:\n    ")
            .atalho("else", "senão", "else:\n    $|")
            .atalho("for", "para cada...", "for i in range($|):\n    ")
            .atalho("while", "enquanto...", "while $|:\n    ")
            .atalho("try", "tenta", "try:\n    $|\nexcept Exception as e:\n    print(e)")
            .atalho("class", "cria classe", "class $|:\n    def __init__(self):\n        pass")
            .atalho("main", "se for o arquivo principal", "if __name__ == \"__main__\":\n    $|");

        // 2. JAVASCRIPT ----------------------------------------------------------
        nova(TOP, "JavaScript", "js", "mjs", "cjs")
            .linha("//").bloco("/*", "*/").aspas("\"'`").variaveis("$")
            .palavras("break case catch class const continue debugger default delete do else export extends false finally "
                    + "for function if import in instanceof let new null return super switch this throw true try typeof "
                    + "undefined var void while with yield async await of static get set")
            .funcoes("console log error warn alert prompt confirm parseInt parseFloat isNaN setTimeout setInterval "
                    + "clearInterval fetch require push pop shift unshift map filter reduce forEach find includes indexOf "
                    + "join split slice splice toUpperCase toLowerCase trim keys values entries stringify parse "
                    + "getElementById querySelector querySelectorAll addEventListener createElement appendChild random floor ceil")
            .tipos("Math JSON Array Object String Number Boolean Date Promise Map Set document window")
            .modelo("console.log(\"Olá, mundo!\");\n")
            .rodar("node {arquivo}")
            .precisa("Node.js: https://nodejs.org")
            .atalho("log", "console.log", "console.log($|);")
            .atalho("function", "cria função", "function $|() {\n    \n}")
            .atalho("arrow", "função seta", "const $| = () => {\n    \n};")
            .atalho("fori", "for com contador", "for (let i = 0; i < $|; i++) {\n    \n}")
            .atalho("forof", "para cada item", "for (const item of $|) {\n    \n}")
            .atalho("if", "se...", "if ($|) {\n    \n}")
            .atalho("getById", "pega elemento", "document.getElementById(\"$|\")")
            .atalho("click", "quando clicar", "addEventListener(\"click\", () => {\n    $|\n});");

        // 3. TYPESCRIPT ----------------------------------------------------------
        nova(TOP, "TypeScript", "ts", "tsx")
            .linha("//").bloco("/*", "*/").aspas("\"'`")
            .palavras("abstract as async await break case catch class const continue declare default delete do else enum "
                    + "export extends false finally for from function if implements import in instanceof interface keyof "
                    + "let module namespace new null private protected public readonly return static super switch this "
                    + "throw true try type typeof undefined var void while yield of")
            .funcoes("console log error map filter reduce forEach find push pop includes join split parseInt parseFloat "
                    + "setTimeout fetch stringify parse keys values")
            .tipos("string number boolean any unknown never void object Array Record Partial Promise Map Set Date Math JSON")
            .modelo("const mensagem: string = \"Olá, mundo!\";\nconsole.log(mensagem);\n")
            .rodar("npx -y tsx {arquivo}")
            .precisa("Node.js (o TypeScript vem pelo npx): https://nodejs.org")
            .atalho("log", "console.log", "console.log($|);")
            .atalho("function", "função com tipos", "function $|(): void {\n    \n}")
            .atalho("interface", "cria interface", "interface $| {\n    \n}")
            .atalho("type", "cria tipo", "type $| = {\n    \n};")
            .atalho("fori", "for com contador", "for (let i = 0; i < $|; i++) {\n    \n}")
            .atalho("if", "se...", "if ($|) {\n    \n}");

        // 4. JAVA ----------------------------------------------------------------
        nova(TOP, "Java", "java")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("abstract assert boolean break byte case catch char class const continue default do double else "
                    + "enum extends final finally float for if implements import instanceof int interface long native new "
                    + "package private protected public return short static super switch synchronized this throw throws "
                    + "try void volatile while var true false null record")
            .funcoes("println print printf format nextLine nextInt nextDouble next hasNext length charAt substring equals "
                    + "equalsIgnoreCase toUpperCase toLowerCase trim split contains indexOf add get set remove size isEmpty "
                    + "parseInt parseDouble valueOf toString random sqrt pow abs max min sleep close")
            .tipos("String Scanner System Math Integer Double Boolean Character ArrayList List HashMap Map Random "
                    + "Thread Exception Object StringBuilder Arrays")
            .modelo("public class {nome} {\n    public static void main(String[] args) {\n        System.out.println(\"Olá, mundo!\");\n    }\n}\n")
            .rodar("java -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 {arquivo}")
            .precisa("JDK 21: https://adoptium.net (marque \"Add to PATH\")")
            .atalho("sout", "imprime uma linha", "System.out.println($|);")
            .atalho("souf", "imprime formatado", "System.out.printf(\"$|%n\");")
            .atalho("main", "método main", "public static void main(String[] args) {\n    $|\n}")
            .atalho("psvm", "método main", "public static void main(String[] args) {\n    $|\n}")
            .atalho("fori", "for com contador", "for (int i = 0; i < $|; i++) {\n    \n}")
            .atalho("if", "se...", "if ($|) {\n    \n}")
            .atalho("ifelse", "se... senão", "if ($|) {\n    \n} else {\n    \n}")
            .atalho("while", "enquanto...", "while ($|) {\n    \n}")
            .atalho("dowhile", "faça... enquanto", "do {\n    $|\n} while ();")
            .atalho("switch", "escolha", "switch ($|) {\n    case 1:\n        break;\n    default:\n        break;\n}")
            .atalho("try", "tenta / se der erro", "try {\n    $|\n} catch (Exception e) {\n    e.printStackTrace();\n}")
            .atalho("Scanner", "ler do teclado", "Scanner sc = new Scanner(System.in);$|")
            .atalho("import Scanner", "importar Scanner", "import java.util.Scanner;$|");

        // 5. C# -----------------------------------------------------------------
        nova(TOP, "C#", "cs")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("abstract as base bool break byte case catch char checked class const continue decimal default "
                    + "delegate do double else enum event explicit extern false finally fixed float for foreach goto if "
                    + "implicit in int interface internal is lock long namespace new null object operator out override "
                    + "params private protected public readonly ref return sbyte sealed short sizeof static string struct "
                    + "switch this throw true try typeof uint ulong unchecked unsafe ushort using var virtual void while "
                    + "async await get set record")
            .funcoes("WriteLine Write ReadLine ReadKey Parse TryParse ToString Add Remove Contains Count Length "
                    + "Substring Split Join Replace ToUpper ToLower Trim Next Sqrt Pow Abs Max Min")
            .tipos("Console Math String Int32 List Dictionary Random DateTime Exception Task Array")
            .modelo("Console.WriteLine(\"Olá, mundo!\");\n")
            .rodar("dotnet run {arquivo}")
            .precisa(".NET 10 SDK (roda arquivo .cs direto): https://dotnet.microsoft.com/download")
            .atalho("cw", "Console.WriteLine", "Console.WriteLine($|);")
            .atalho("cr", "Console.ReadLine", "Console.ReadLine()$|")
            .atalho("for", "for com contador", "for (int i = 0; i < $|; i++)\n{\n    \n}")
            .atalho("foreach", "para cada", "foreach (var item in $|)\n{\n    \n}")
            .atalho("if", "se...", "if ($|)\n{\n    \n}")
            .atalho("class", "cria classe", "class $|\n{\n    \n}");

        // 6. C++ ----------------------------------------------------------------
        nova(TOP, "C++", "cpp", "cc", "cxx", "hpp", "hh")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("alignas alignof and auto bool break case catch char class const constexpr continue decltype "
                    + "default delete do double else enum explicit export extern false float for friend goto if inline "
                    + "int long mutable namespace new noexcept not nullptr operator or private protected public return "
                    + "short signed sizeof static struct switch template this throw true try typedef typename union "
                    + "unsigned using virtual void volatile while include define")
            .funcoes("cout cin endl printf scanf getline push_back pop_back size begin end sort find insert erase "
                    + "to_string stoi stod abs sqrt pow max min swap make_shared make_unique")
            .tipos("std string vector map set pair array unordered_map shared_ptr unique_ptr")
            .modelo("#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << \"Olá, mundo!\" << endl;\n    return 0;\n}\n")
            .rodar("g++ {arquivo} -o {saida} && {saida}")
            .precisa("Compilador g++ (MinGW-w64 / MSYS2): https://www.msys2.org")
            .atalho("cout", "imprime", "cout << $| << endl;")
            .atalho("cin", "lê do teclado", "cin >> $|;")
            .atalho("main", "função main", "int main() {\n    $|\n    return 0;\n}")
            .atalho("for", "for com contador", "for (int i = 0; i < $|; i++) {\n    \n}")
            .atalho("if", "se...", "if ($|) {\n    \n}")
            .atalho("include", "#include", "#include <$|>");

        // 7. C ------------------------------------------------------------------
        nova(TOP, "C", "c", "h")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("auto break case char const continue default do double else enum extern float for goto if inline "
                    + "int long register restrict return short signed sizeof static struct switch typedef union unsigned "
                    + "void volatile while include define NULL true false bool")
            .funcoes("printf scanf puts gets fgets putchar getchar malloc calloc realloc free strlen strcpy strcat strcmp "
                    + "memcpy memset fopen fclose fprintf fscanf rand srand time exit abs sqrt pow")
            .tipos("FILE size_t int8_t int32_t uint8_t uint32_t")
            .modelo("#include <stdio.h>\n\nint main(void) {\n    printf(\"Olá, mundo!\\n\");\n    return 0;\n}\n")
            .rodar("gcc {arquivo} -o {saida} && {saida}")
            .precisa("Compilador gcc (MinGW-w64 / MSYS2): https://www.msys2.org")
            .atalho("printf", "imprime", "printf(\"$|\\n\");")
            .atalho("scanf", "lê do teclado", "scanf(\"%d\", &$|);")
            .atalho("main", "função main", "int main(void) {\n    $|\n    return 0;\n}")
            .atalho("for", "for com contador", "for (int i = 0; i < $|; i++) {\n    \n}")
            .atalho("if", "se...", "if ($|) {\n    \n}")
            .atalho("include", "#include", "#include <$|.h>");

        // 8. GO -----------------------------------------------------------------
        nova(TOP, "Go", "go")
            .linha("//").bloco("/*", "*/").aspas("\"'`")
            .palavras("break case chan const continue default defer else fallthrough for func go goto if import interface "
                    + "map package range return select struct switch type var true false nil iota")
            .funcoes("Println Printf Print Sprintf Scan Scanln append len cap make new panic recover copy delete close "
                    + "Atoi Itoa Sleep Now Split Join Contains ToUpper ToLower")
            .tipos("int int64 float64 string bool byte rune error fmt strings strconv time os math")
            .modelo("package main\n\nimport \"fmt\"\n\nfunc main() {\n    fmt.Println(\"Olá, mundo!\")\n}\n")
            .rodar("go run {arquivo}")
            .precisa("Go: https://go.dev/dl")
            .atalho("pl", "fmt.Println", "fmt.Println($|)")
            .atalho("func", "cria função", "func $|() {\n    \n}")
            .atalho("for", "for com contador", "for i := 0; i < $|; i++ {\n    \n}")
            .atalho("if", "se...", "if $| {\n    \n}")
            .atalho("iferr", "se deu erro", "if err != nil {\n    $|\n}");

        // 9. RUST ---------------------------------------------------------------
        nova(TOP, "Rust", "rs")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("as async await break const continue crate dyn else enum extern false fn for if impl in let loop "
                    + "match mod move mut pub ref return self Self static struct super trait true type unsafe use where while")
            .funcoes("println print format vec panic assert assert_eq unwrap expect push pop len iter map collect "
                    + "clone to_string parse read_line trim is_empty contains Some None Ok Err")
            .tipos("i8 i16 i32 i64 u8 u16 u32 u64 f32 f64 usize isize bool char str String Vec Option Result Box HashMap")
            .modelo("fn main() {\n    println!(\"Olá, mundo!\");\n}\n")
            .rodar("rustc {arquivo} -o {saida} && {saida}")
            .precisa("Rust: https://rustup.rs")
            .atalho("println", "imprime", "println!(\"{}\", $|);")
            .atalho("fn", "cria função", "fn $|() {\n    \n}")
            .atalho("for", "para cada", "for i in 0..$| {\n    \n}")
            .atalho("if", "se...", "if $| {\n    \n}")
            .atalho("match", "escolha", "match $| {\n    _ => {}\n}")
            .atalho("let", "variável", "let $| = ;");

        // 10. SQL ---------------------------------------------------------------
        nova(TOP, "SQL", "sql")
            .semCaixa().linha("--").bloco("/*", "*/").aspas("'\"")
            .palavras(Sql.PALAVRAS)
            .funcoes(Sql.FUNCOES)
            .tipos(Sql.TIPOS)
            .banco("SQL")
            .modelo("-- Banco de dados SIMULADO do TazzCode (não é um banco de verdade!)\n\n"
                    + "CREATE TABLE alunos (\n    id INT PRIMARY KEY AUTO_INCREMENT,\n    nome VARCHAR(50),\n    idade INT\n);\n\n"
                    + "INSERT INTO alunos (nome, idade) VALUES ('João', 12), ('Maria', 13), ('Pedro', 11);\n\n"
                    + "SELECT * FROM alunos WHERE idade >= 12 ORDER BY nome;\n")
            .precisa("Nada! O TazzCode tem um banco simulado embutido.")
            .atalho("select", "consulta", "SELECT * FROM $|;")
            .atalho("insert", "insere", "INSERT INTO $| () VALUES ();")
            .atalho("create", "cria tabela", "CREATE TABLE $| (\n    id INT PRIMARY KEY AUTO_INCREMENT,\n    nome VARCHAR(50)\n);")
            .atalho("update", "altera", "UPDATE $| SET  WHERE ;")
            .atalho("delete", "apaga", "DELETE FROM $| WHERE ;");

        // 11. PHP ---------------------------------------------------------------
        nova(ALTA, "PHP", "php")
            .linha("//", "#").bloco("/*", "*/").aspas("\"'").variaveis("$")
            .palavras("abstract and array as break callable case catch class clone const continue declare default do echo "
                    + "else elseif empty enddeclare endfor endforeach endif endswitch endwhile extends final finally fn for "
                    + "foreach function global if implements include include_once instanceof interface isset list match "
                    + "namespace new or print private protected public require require_once return static switch throw "
                    + "trait try unset use var while true false null")
            .funcoes("echo print_r var_dump strlen count array_push array_pop explode implode str_replace strtoupper "
                    + "strtolower trim json_encode json_decode isset empty date time rand sort in_array array_keys "
                    + "array_map readline fgets intval floatval number_format")
            .modelo("<?php\n\necho \"Olá, mundo!\\n\";\n")
            .rodar("php {arquivo}")
            .precisa("PHP: https://windows.php.net/download (ou o XAMPP)")
            .atalho("echo", "mostra", "echo $|;")
            .atalho("php", "abre o PHP", "<?php\n\n$|")
            .atalho("function", "cria função", "function $|() {\n    \n}")
            .atalho("foreach", "para cada", "foreach ($| as $item) {\n    \n}")
            .atalho("if", "se...", "if ($|) {\n    \n}");

        // 12. SWIFT -------------------------------------------------------------
        nova(ALTA, "Swift", "swift")
            .linha("//").bloco("/*", "*/").aspas("\"")
            .palavras("associatedtype class deinit enum extension fileprivate func import init inout internal let open "
                    + "operator private protocol public rethrows static struct subscript typealias var break case continue "
                    + "default defer do else fallthrough for guard if in repeat return switch where while as catch false "
                    + "is nil self Self super throw throws true try async await")
            .funcoes("print readLine count append remove insert contains map filter reduce sorted min max abs")
            .tipos("Int Double Float String Bool Character Array Dictionary Set Optional")
            .modelo("print(\"Olá, mundo!\")\n")
            .rodar("swift {arquivo}")
            .precisa("Swift: https://www.swift.org/install/windows")
            .atalho("print", "mostra", "print($|)")
            .atalho("func", "cria função", "func $|() {\n    \n}")
            .atalho("for", "para cada", "for i in 0..<$| {\n    \n}")
            .atalho("if", "se...", "if $| {\n    \n}")
            .atalho("guard", "guarda", "guard $| else { return }");

        // 13. KOTLIN ------------------------------------------------------------
        nova(ALTA, "Kotlin", "kt")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("as break class continue do else false for fun if in interface is null object package return super "
                    + "this throw true try typealias typeof val var when while by catch constructor data enum finally "
                    + "init lateinit open override private protected public sealed suspend companion import")
            .funcoes("println print readLine readln listOf mutableListOf mapOf mutableMapOf setOf arrayOf repeat "
                    + "toInt toDouble toString forEach map filter first last size")
            .tipos("Int Long Double Float String Boolean Char Unit Any List Map Set Array")
            .modelo("fun main() {\n    println(\"Olá, mundo!\")\n}\n")
            .rodar("kotlinc {arquivo} -include-runtime -d {saidaBase}.jar && java -jar {saidaBase}.jar")
            .precisa("Kotlin (kotlinc): https://kotlinlang.org/docs/command-line.html")
            .atalho("println", "mostra", "println($|)")
            .atalho("fun", "cria função", "fun $|() {\n    \n}")
            .atalho("main", "função main", "fun main() {\n    $|\n}")
            .atalho("for", "para cada", "for (i in 0 until $|) {\n    \n}")
            .atalho("when", "escolha", "when ($|) {\n    else -> {}\n}");

        // 14. R -----------------------------------------------------------------
        nova(ALTA, "R", "r")
            .linha("#").aspas("\"'")
            .palavras("if else repeat while function for in next break TRUE FALSE NULL Inf NaN NA library require return")
            .funcoes("print cat paste paste0 c list vector matrix data.frame length sum mean median sd min max seq rep "
                    + "plot hist barplot read.csv write.csv head tail summary str nrow ncol apply sapply lapply round "
                    + "readline as.numeric as.integer as.character")
            .modelo("print(\"Olá, mundo!\")\nnumeros <- c(1, 2, 3, 4, 5)\ncat(\"Média:\", mean(numeros), \"\\n\")\n")
            .rodar("Rscript {arquivo}")
            .precisa("R: https://cran.r-project.org")
            .atalho("print", "mostra", "print($|)")
            .atalho("function", "cria função", "$| <- function() {\n    \n}")
            .atalho("for", "para cada", "for (i in 1:$|) {\n    \n}")
            .atalho("if", "se...", "if ($|) {\n    \n}");

        // 15. DART --------------------------------------------------------------
        nova(ALTA, "Dart", "dart")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("abstract as assert async await break case catch class const continue default do else enum export "
                    + "extends extension external factory false final finally for get if implements import in is late "
                    + "library mixin new null on operator part required rethrow return set static super switch sync this "
                    + "throw true try typedef var void while with yield")
            .funcoes("print add remove contains length forEach map where toList toString parse readLineSync")
            .tipos("int double num String bool List Map Set dynamic Object Future Stream")
            .modelo("void main() {\n  print('Olá, mundo!');\n}\n")
            .rodar("dart run {arquivo}")
            .precisa("Dart: https://dart.dev/get-dart")
            .atalho("print", "mostra", "print($|);")
            .atalho("main", "função main", "void main() {\n  $|\n}")
            .atalho("for", "for com contador", "for (var i = 0; i < $|; i++) {\n  \n}")
            .atalho("if", "se...", "if ($|) {\n  \n}");

        // 16. RUBY --------------------------------------------------------------
        nova(ALTA, "Ruby", "rb")
            .linha("#").bloco("=begin", "=end").aspas("\"'").variaveis("@$")
            .palavras("BEGIN END alias and begin break case class def defined? do else elsif end ensure false for if in "
                    + "module next nil not or redo rescue retry return self super then true undef unless until when while yield")
            .funcoes("puts print p gets chomp to_i to_s to_f each map select reject times upto downto length size push "
                    + "pop include? sort reverse join split upcase downcase require attr_accessor")
            .modelo("puts \"Olá, mundo!\"\n")
            .rodar("ruby {arquivo}")
            .precisa("Ruby: https://rubyinstaller.org")
            .atalho("puts", "mostra", "puts $|")
            .atalho("def", "cria método", "def $|\n  \nend")
            .atalho("each", "para cada", "$|.each do |item|\n  \nend")
            .atalho("if", "se...", "if $|\n  \nend")
            .atalho("class", "cria classe", "class $|\n  def initialize\n  end\nend");

        // 17. MATLAB ------------------------------------------------------------
        nova(ALTA, "MATLAB", "m")
            .linha("%").bloco("%{", "%}").aspas("'\"")
            .palavras("break case catch classdef continue else elseif end for function global if otherwise parfor "
                    + "persistent return spmd switch try while true false")
            .funcoes("disp fprintf printf input zeros ones eye rand randn size length numel sum mean max min abs sqrt "
                    + "plot xlabel ylabel title figure hold linspace sin cos exp log round floor num2str str2num")
            .modelo("disp('Olá, mundo!');\nx = 1:5;\nfprintf('Soma: %d\\n', sum(x));\n")
            .rodar("octave-cli {arquivo}")
            .precisa("GNU Octave (grátis e compatível com MATLAB): https://octave.org/download")
            .atalho("disp", "mostra", "disp($|);")
            .atalho("function", "cria função", "function $| ()\n    \nend")
            .atalho("for", "para cada", "for i = 1:$|\n    \nend")
            .atalho("if", "se...", "if $|\n    \nend");

        // 18. SHELL / BASH ------------------------------------------------------
        nova(ALTA, "Shell / Bash", "sh", "bash")
            .linha("#").aspas("\"'").variaveis("$")
            .palavras("if then else elif fi case esac for select while until do done in function time return exit "
                    + "break continue local export readonly declare")
            .funcoes("echo printf read cd ls pwd mkdir rm cp mv cat grep sed awk sort uniq wc head tail chmod source "
                    + "test sleep date curl")
            .modelo("#!/bin/bash\n\necho \"Olá, mundo!\"\n")
            .rodar("bash {arquivo}")
            .precisa("Bash (vem com o Git for Windows ou WSL): https://git-scm.com")
            .atalho("echo", "mostra", "echo \"$|\"")
            .atalho("if", "se...", "if [ $| ]; then\n    \nfi")
            .atalho("for", "para cada", "for i in $|; do\n    \ndone")
            .atalho("function", "cria função", "$|() {\n    \n}");

        // 19. POWERSHELL --------------------------------------------------------
        nova(ALTA, "PowerShell", "ps1", "psm1")
            .semCaixa().linha("#").bloco("<#", "#>").aspas("\"'").variaveis("$")
            .palavras("begin break catch class continue data do dynamicparam else elseif end exit filter finally for "
                    + "foreach from function if in param process return switch throw trap try until using var while")
            .funcoes("Write-Host Write-Output Read-Host Get-ChildItem Get-Content Set-Content Get-Process Get-Date "
                    + "Start-Sleep New-Item Remove-Item Copy-Item Test-Path Select-Object Where-Object ForEach-Object Sort-Object")
            .letras("-")
            .modelo("Write-Host \"Olá, mundo!\"\n")
            .rodar("powershell -ExecutionPolicy Bypass -File {arquivo}")
            .precisa("Já vem no Windows!")
            .atalho("wh", "Write-Host", "Write-Host \"$|\"")
            .atalho("if", "se...", "if ($|) {\n    \n}")
            .atalho("foreach", "para cada", "foreach ($item in $|) {\n    \n}")
            .atalho("function", "cria função", "function $| {\n    \n}");

        // 20. ASSEMBLY ----------------------------------------------------------
        nova(ALTA, "Assembly (NASM)", "asm", "s", "nasm")
            .semCaixa().linha(";").aspas("\"'")
            .palavras("section global extern db dw dd dq resb resw resd equ times bits default rel")
            .funcoes("mov add sub mul imul div idiv inc dec cmp jmp je jne jg jl jge jle jz jnz call ret push pop lea "
                    + "and or xor not shl shr syscall int nop test loop")
            .tipos("rax rbx rcx rdx rsi rdi rsp rbp r8 r9 r10 r11 eax ebx ecx edx esi edi esp ebp al bl cl dl")
            .modelo("; Olá, mundo! em Assembly x64 para Windows (NASM + gcc)\n"
                    + "default rel\nglobal main\nextern printf\n\nsection .data\n    msg db \"Ola, mundo!\", 10, 0\n\n"
                    + "section .text\nmain:\n    sub rsp, 40\n    lea rcx, [msg]\n    call printf\n    add rsp, 40\n    xor eax, eax\n    ret\n")
            .rodar("nasm -f win64 {arquivo} -o {saidaBase}.obj && gcc {saidaBase}.obj -o {saida} && {saida}")
            .precisa("NASM (https://www.nasm.us) + gcc (https://www.msys2.org)")
            .atalho("mov", "copia valor", "mov $|, ")
            .atalho("section", "seção", "section .$|");

        // 21. SCALA -------------------------------------------------------------
        nova(ALTA, "Scala", "scala", "sc")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("abstract case catch class def do else enum extends false final finally for forSome given if "
                    + "implicit import lazy match new null object override package private protected return sealed "
                    + "super then this throw trait true try type using val var while with yield")
            .funcoes("println print readLine map filter foreach reduce fold sum size length head tail toList toString")
            .tipos("Int Long Double String Boolean Unit Any List Map Set Option Some None Array Seq")
            .modelo("@main def ola(): Unit =\n  println(\"Olá, mundo!\")\n")
            .rodar("scala run {arquivo}")
            .precisa("Scala 3 (comando scala): https://www.scala-lang.org/download")
            .atalho("println", "mostra", "println($|)")
            .atalho("def", "cria função", "def $|(): Unit =\n  ")
            .atalho("main", "função main", "@main def principal(): Unit =\n  $|")
            .atalho("for", "para cada", "for i <- 0 until $| do\n  ");

        // 22. LUA ---------------------------------------------------------------
        nova(ALTA, "Lua", "lua")
            .linha("--").bloco("--[[", "]]").aspas("\"'")
            .palavras("and break do else elseif end false for function goto if in local nil not or repeat return then "
                    + "true until while")
            .funcoes("print io.write io.read tostring tonumber type pairs ipairs table.insert table.remove table.concat "
                    + "string.format string.sub string.len string.upper string.lower math.random math.floor math.max "
                    + "math.min require error pcall setmetatable")
            .letras(".")
            .modelo("print(\"Olá, mundo!\")\n")
            .rodar("lua {arquivo}")
            .precisa("Lua: https://luabinaries.sourceforge.net (ou \"winget install DEVCOM.Lua\")")
            .atalho("print", "mostra", "print($|)")
            .atalho("function", "cria função", "function $|()\n    \nend")
            .atalho("for", "for com contador", "for i = 1, $| do\n    \nend")
            .atalho("forpairs", "para cada", "for chave, valor in pairs($|) do\n    \nend")
            .atalho("if", "se...", "if $| then\n    \nend")
            .atalho("local", "variável local", "local $| = ");

        // 23. PERL --------------------------------------------------------------
        nova(ALTA, "Perl", "pl", "pm")
            .linha("#").aspas("\"'").variaveis("$@%")
            .palavras("if elsif else unless while until for foreach do last next redo return sub my our local use "
                    + "require package and or not eq ne lt gt le ge")
            .funcoes("print printf say chomp chop length substr index join split push pop shift unshift sort reverse "
                    + "keys values exists delete defined die warn open close lc uc sprintf")
            .modelo("use strict;\nuse warnings;\n\nprint \"Olá, mundo!\\n\";\n")
            .rodar("perl {arquivo}")
            .precisa("Strawberry Perl: https://strawberryperl.com")
            .atalho("print", "mostra", "print \"$|\\n\";")
            .atalho("sub", "cria função", "sub $| {\n    \n}")
            .atalho("foreach", "para cada", "foreach my $item ($|) {\n    \n}")
            .atalho("if", "se...", "if ($|) {\n    \n}");

        // 24. VISUAL BASIC .NET -------------------------------------------------
        nova(ALTA, "Visual Basic .NET", "vb")
            .semCaixa().linha("'").aspas("\"")
            .palavras("AddHandler AndAlso As ByRef ByVal Call Case Catch Class Const Continue Dim Do Each Else ElseIf End "
                    + "Enum Exit False Finally For Function Get Handles If Implements Imports In Inherits Interface Is "
                    + "Loop Me Module MyBase New Next Not Nothing Of On Or OrElse Private Property Protected Public "
                    + "ReadOnly Return Select Set Shared Static Step Structure Sub Then Throw To True Try Until While With And Mod")
            .funcoes("WriteLine Write ReadLine MsgBox InputBox CInt CStr CDbl Len Mid Left Right UCase LCase Trim Format")
            .tipos("Integer Long Double Single String Boolean Char Date Object Console Math List")
            .modelo("Module {nome}\n    Sub Main()\n        Console.WriteLine(\"Olá, mundo!\")\n    End Sub\nEnd Module\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("VB.NET precisa de um projeto .NET: no terminal, \"dotnet new console -lang VB\" e depois \"dotnet run\".")
            .atalho("cw", "Console.WriteLine", "Console.WriteLine($|)")
            .atalho("sub", "cria Sub", "Sub $|()\n    \nEnd Sub")
            .atalho("for", "para", "For i = 1 To $|\n    \nNext")
            .atalho("if", "se...", "If $| Then\n    \nEnd If");

        // 25. DELPHI / OBJECT PASCAL --------------------------------------------
        nova(ALTA, "Delphi / Object Pascal", "pas", "dpr", "pp", "lpr")
            .semCaixa().linha("//").bloco("{", "}").bloco("(*", "*)").aspas("'")
            .palavras("and array as begin case class const constructor destructor div do downto else end except "
                    + "exports file finally for function goto if implementation in inherited initialization interface "
                    + "is label library mod nil not object of or packed procedure program property raise record repeat "
                    + "set shl shr then to try type unit until uses var while with xor true false")
            .funcoes("writeln write readln read length copy pos inc dec ord chr succ pred trunc round abs sqr sqrt "
                    + "random randomize inttostr strtoint")
            .tipos("integer real double string char boolean byte word longint")
            .modelo("program {nome};\n\nbegin\n  writeln('Olá, mundo!');\nend.\n")
            .rodar("fpc -o{saida} {arquivo} && {saida}")
            .precisa("Free Pascal: https://www.freepascal.org/download.html")
            .atalho("writeln", "mostra", "writeln($|);")
            .atalho("begin", "bloco", "begin\n  $|\nend;")
            .atalho("for", "para", "for i := 1 to $| do\nbegin\n  \nend;")
            .atalho("if", "se...", "if $| then\nbegin\n  \nend;")
            .atalho("procedure", "cria procedimento", "procedure $|;\nbegin\n  \nend;");
    }
}
