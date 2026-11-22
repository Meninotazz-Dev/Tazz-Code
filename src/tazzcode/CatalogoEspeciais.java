package tazzcode;

import static tazzcode.Catalogo.nova;

/**
 * Linguagens 26 a 50 da lista (científicas, especializadas, de hardware e de ensino).
 */
class CatalogoEspeciais {

    private static final String ESP = "26 a 40 - Especializadas e emergentes";
    private static final String NICHO = "41 a 50 - Nichos, hardware e ensino";

    static void cadastrar() {

        // 26. JULIA -------------------------------------------------------------
        nova(ESP, "Julia", "jl")
            .linha("#").bloco("#=", "=#").aspas("\"'")
            .palavras("abstract baremodule begin break catch const continue do else elseif end export false finally for "
                    + "function global if import let local macro module mutable quote return struct true try using while in")
            .funcoes("println print readline parse length push! pop! sum maximum minimum sort collect map filter "
                    + "round floor sqrt abs zeros ones rand string typeof")
            .tipos("Int Int64 Float64 String Bool Char Vector Array Dict Nothing")
            .letras("!")
            .modelo("println(\"Olá, mundo!\")\n")
            .rodar("julia {arquivo}")
            .precisa("Julia: https://julialang.org/downloads")
            .atalho("println", "mostra", "println($|)")
            .atalho("function", "cria função", "function $|()\n    \nend")
            .atalho("for", "para cada", "for i in 1:$|\n    \nend")
            .atalho("if", "se...", "if $|\n    \nend");

        // 27. ZIG ---------------------------------------------------------------
        nova(ESP, "Zig", "zig")
            .linha("//").aspas("\"'")
            .palavras("addrspace align allowzero and anyframe anytype asm async await break callconv catch comptime const "
                    + "continue defer else enum errdefer error export extern fn for if inline noalias nosuspend opaque or "
                    + "orelse packed pub resume return struct suspend switch test threadlocal try union unreachable var "
                    + "volatile while true false null undefined")
            .funcoes("print import alloc free append deinit init")
            .tipos("u8 u16 u32 u64 i8 i16 i32 i64 f32 f64 usize isize bool void anyerror std")
            .modelo("const std = @import(\"std\");\n\npub fn main() void {\n    std.debug.print(\"Olá, mundo!\\n\", .{});\n}\n")
            .rodar("zig run {arquivo}")
            .precisa("Zig: https://ziglang.org/download")
            .atalho("print", "imprime", "std.debug.print(\"$|\\n\", .{});")
            .atalho("fn", "cria função", "fn $|() void {\n    \n}")
            .atalho("while", "enquanto", "while ($|) {\n    \n}");

        // 28. MOJO --------------------------------------------------------------
        nova(ESP, "Mojo", "mojo", "🔥")
            .linha("#").aspas("\"'")
            .palavras("def fn struct trait var let alias if elif else for while in return raises try except with import "
                    + "from inout owned borrowed True False None pass break continue")
            .funcoes("print len range input str int")
            .tipos("Int Float64 String Bool SIMD List")
            .modelo("def main():\n    print(\"Olá, mundo!\")\n")
            .rodar("mojo run {arquivo}")
            .precisa("Mojo (Modular): https://docs.modular.com/mojo (no Windows, use o WSL)")
            .atalho("print", "mostra", "print($|)")
            .atalho("fn", "função rápida", "fn $|():\n    ")
            .atalho("def", "função", "def $|():\n    ");

        // 29. ELIXIR ------------------------------------------------------------
        nova(ESP, "Elixir", "ex", "exs")
            .linha("#").aspas("\"'")
            .palavras("after alias and case catch cond def defmodule defp defstruct do else end false fn for if import in "
                    + "nil not or quote raise receive require rescue true try unless unquote use when with")
            .funcoes("IO.puts IO.inspect IO.gets Enum.map Enum.filter Enum.reduce Enum.each String.upcase String.trim "
                    + "length hd tl spawn send")
            .letras(".?!")
            .modelo("IO.puts(\"Olá, mundo!\")\n")
            .rodar("elixir {arquivo}")
            .precisa("Elixir: https://elixir-lang.org/install.html")
            .atalho("puts", "IO.puts", "IO.puts($|)")
            .atalho("def", "cria função", "def $| do\n  \nend")
            .atalho("defmodule", "cria módulo", "defmodule $| do\n  \nend")
            .atalho("case", "escolha", "case $| do\n  _ -> nil\nend");

        // 30. HASKELL -----------------------------------------------------------
        nova(ESP, "Haskell", "hs")
            .linha("--").bloco("{-", "-}").aspas("\"'")
            .palavras("case class data default deriving do else foreign if import in infix infixl infixr instance let "
                    + "module newtype of then type where True False")
            .funcoes("putStrLn putStr print getLine show read map filter foldr foldl sum product length head tail "
                    + "reverse take drop zip concat elem maximum minimum return mapM_")
            .tipos("Int Integer Double Float String Bool Char Maybe Just Nothing Either IO")
            .modelo("main :: IO ()\nmain = putStrLn \"Olá, mundo!\"\n")
            .rodar("runghc {arquivo}")
            .precisa("GHC (Haskell): https://www.haskell.org/ghcup")
            .atalho("putStrLn", "mostra", "putStrLn $|")
            .atalho("main", "função main", "main :: IO ()\nmain = do\n  $|")
            .atalho("if", "se...", "if $| then  else ");

        // 31. OBJECTIVE-C -------------------------------------------------------
        nova(ESP, "Objective-C", "mm")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("@interface @implementation @end @property @synthesize @protocol @class @selector self super nil "
                    + "YES NO if else for while do return switch case break continue int char float double void id "
                    + "import include static const")
            .funcoes("NSLog printf alloc init release retain autorelease")
            .tipos("NSString NSObject NSArray NSDictionary NSNumber NSInteger BOOL")
            .letras("@")
            .modelo("#import <Foundation/Foundation.h>\n\nint main(int argc, const char * argv[]) {\n    @autoreleasepool {\n        NSLog(@\"Olá, mundo!\");\n    }\n    return 0;\n}\n")
            .rodar("clang {arquivo} -o {saida} -lobjc && {saida}")
            .precisa("Objective-C funciona melhor no Mac (Xcode). No Windows: clang + GNUstep (avançado).")
            .atalho("NSLog", "mostra", "NSLog(@\"$|\");");

        // 32. GROOVY ------------------------------------------------------------
        nova(ESP, "Groovy", "groovy", "gvy")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("abstract as assert boolean break case catch char class def default do double else enum extends "
                    + "false final finally float for if implements import in instanceof int interface long new null "
                    + "package private protected public return short static super switch this throw true try void while")
            .funcoes("println print each collect findAll find inject size times upto")
            .tipos("String Integer List Map")
            .modelo("println \"Olá, mundo!\"\n")
            .rodar("groovy {arquivo}")
            .precisa("Groovy: https://groovy.apache.org/download.html")
            .atalho("println", "mostra", "println $|")
            .atalho("def", "cria função", "def $|() {\n    \n}")
            .atalho("each", "para cada", "$|.each { item ->\n    \n}");

        // 33. VBA ---------------------------------------------------------------
        nova(ESP, "VBA (Office)", "vba", "cls", "frm")
            .semCaixa().linha("'").aspas("\"")
            .palavras("Sub Function End Dim As If Then Else ElseIf For Each Next To Step Do Loop While Wend Until Select "
                    + "Case With Set Let Call Exit Private Public Const True False Nothing And Or Not Mod Option Explicit "
                    + "On Error Resume GoTo ByVal ByRef New Me")
            .funcoes("MsgBox InputBox Debug.Print Range Cells Worksheets Workbooks ActiveSheet Len Mid Left Right UCase "
                    + "LCase Trim CInt CStr CDbl Val Format Now Date")
            .tipos("Integer Long Double String Boolean Variant Object Worksheet Range")
            .letras(".")
            .modelo("Sub Ola()\n    MsgBox \"Olá, mundo!\"\nEnd Sub\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("VBA roda DENTRO do Excel/Word: abra o Excel, aperte Alt+F11, cole o código e aperte F5. "
                    + "Pra rodar fora do Office, use VBScript (.vbs), que é parecido e roda aqui!")
            .atalho("sub", "cria Sub", "Sub $|()\n    \nEnd Sub")
            .atalho("msgbox", "caixa de mensagem", "MsgBox \"$|\"")
            .atalho("for", "para", "For i = 1 To $|\n    \nNext i")
            .atalho("if", "se...", "If $| Then\n    \nEnd If");

        // 34. FORTRAN -----------------------------------------------------------
        nova(ESP, "Fortran", "f90", "f95", "f03", "f", "for")
            .semCaixa().linha("!").aspas("\"'")
            .palavras("program end implicit none integer real double precision complex logical character parameter "
                    + "dimension allocatable if then else elseif endif do enddo while call subroutine function return "
                    + "module use contains stop print write read select case exit cycle")
            .funcoes("abs sqrt sin cos exp log mod size sum maxval minval len trim allocate deallocate")
            .modelo("program {nome}\n  implicit none\n  print *, 'Olá, mundo!'\nend program {nome}\n")
            .rodar("gfortran {arquivo} -o {saida} && {saida}")
            .precisa("gfortran (vem no MSYS2): https://www.msys2.org")
            .atalho("print", "mostra", "print *, $|")
            .atalho("do", "repetição", "do i = 1, $|\n  \nend do")
            .atalho("if", "se...", "if ($|) then\n  \nend if");

        // 35. COBOL -------------------------------------------------------------
        nova(ESP, "COBOL", "cob", "cbl", "cpy")
            .semCaixa().linha("*>").aspas("\"'")
            .palavras("IDENTIFICATION DIVISION PROGRAM-ID ENVIRONMENT DATA WORKING-STORAGE SECTION PROCEDURE PIC PICTURE "
                    + "VALUE DISPLAY ACCEPT MOVE TO ADD SUBTRACT MULTIPLY DIVIDE COMPUTE GIVING IF ELSE END-IF PERFORM "
                    + "UNTIL VARYING FROM BY TIMES END-PERFORM STOP RUN EVALUATE WHEN END-EVALUATE")
            .letras("-")
            .modelo("       IDENTIFICATION DIVISION.\n       PROGRAM-ID. {nome}.\n       PROCEDURE DIVISION.\n"
                    + "           DISPLAY \"Ola, mundo!\".\n           STOP RUN.\n")
            .rodar("cobc -x {arquivo} -o {saida} && {saida}")
            .precisa("GnuCOBOL: https://gnucobol.sourceforge.io (ou pelo MSYS2)")
            .atalho("display", "mostra", "DISPLAY \"$|\".")
            .atalho("perform", "repetição", "PERFORM $| TIMES\n    \nEND-PERFORM");

        // 36. ABAP --------------------------------------------------------------
        nova(ESP, "ABAP (SAP)", "abap")
            .semCaixa().linha("\"", "*").aspas("'`")
            .palavras("REPORT DATA TYPE TYPES WRITE IF ELSE ELSEIF ENDIF LOOP AT ENDLOOP DO ENDDO WHILE ENDWHILE "
                    + "SELECT FROM WHERE INTO TABLE ENDSELECT FORM ENDFORM PERFORM CLASS DEFINITION IMPLEMENTATION "
                    + "METHOD ENDMETHOD ENDCLASS PUBLIC SECTION METHODS CREATE OBJECT CALL VALUE")
            .letras("-")
            .modelo("REPORT {nome}.\n\nWRITE 'Olá, mundo!'.\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("ABAP só roda dentro de um sistema SAP. Pra treinar de graça: SAP BTP Trial (https://account.hanatrial.ondemand.com).")
            .atalho("write", "mostra", "WRITE '$|'.")
            .atalho("data", "variável", "DATA $| TYPE i.");

        // 37. ADA ---------------------------------------------------------------
        nova(ESP, "Ada", "adb", "ads", "ada")
            .semCaixa().linha("--").aspas("\"'")
            .palavras("abort abs abstract accept access aliased all and array at begin body case constant declare delay "
                    + "delta digits do else elsif end entry exception exit for function generic goto if in interface is "
                    + "limited loop mod new not null of or others out overriding package pragma private procedure "
                    + "protected raise range record rem renames requeue return reverse select separate subtype "
                    + "synchronized tagged task terminate then type until use when while with xor")
            .funcoes("Put_Line Put Get Get_Line New_Line")
            .tipos("Integer Float Boolean String Character Natural Positive Ada.Text_IO")
            .letras(".")
            .modelo("with Ada.Text_IO; use Ada.Text_IO;\n\nprocedure {nome} is\nbegin\n   Put_Line (\"Olá, mundo!\");\nend {nome};\n")
            .rodar("gnatmake {arquivo} -D {saidaPasta} -o {saida} && {saida}")
            .precisa("GNAT (Ada): https://www.adacore.com/download ou \"pacman -S mingw-w64-ucrt-x86_64-gcc-ada\" no MSYS2")
            .atalho("put_line", "mostra", "Put_Line (\"$|\");")
            .atalho("for", "para", "for I in 1 .. $| loop\n   \nend loop;")
            .atalho("if", "se...", "if $| then\n   \nend if;");

        // 38. SOLIDITY ----------------------------------------------------------
        nova(ESP, "Solidity", "sol")
            .linha("//").bloco("/*", "*/").aspas("\"'")
            .palavras("pragma solidity contract interface library function modifier event emit struct enum mapping "
                    + "public private internal external view pure payable returns return if else for while do break "
                    + "continue new delete memory storage calldata constructor require revert assert import is override virtual")
            .funcoes("require revert keccak256 transfer send msg.sender msg.value block.timestamp")
            .tipos("address uint uint256 int int256 bool string bytes bytes32")
            .letras(".")
            .modelo("// SPDX-License-Identifier: MIT\npragma solidity ^0.8.0;\n\ncontract {nome} {\n    string public mensagem = \"Olá, mundo!\";\n}\n")
            .rodar("solc --bin {arquivo}")
            .precisa("solc (compilador Solidity): https://docs.soliditylang.org/en/latest/installing-solidity.html. "
                    + "Pra testar fácil, use o Remix no navegador: https://remix.ethereum.org")
            .atalho("function", "cria função", "function $|() public {\n    \n}")
            .atalho("contract", "cria contrato", "contract $| {\n    \n}");

        // 39. F# ----------------------------------------------------------------
        nova(ESP, "F#", "fsx", "fs")
            .linha("//").bloco("(*", "*)").aspas("\"")
            .palavras("abstract and as assert base begin class default delegate do done downcast downto elif else end "
                    + "exception extern false finally for fun function if in inherit inline interface internal lazy let "
                    + "match member module mutable namespace new not null of open or override private public rec return "
                    + "static struct then to true try type upcast use val void when while with yield")
            .funcoes("printfn printf sprintf List.map List.filter List.sum Seq.map Array.map ignore string int float")
            .tipos("int float string bool unit list option")
            .letras(".")
            .modelo("printfn \"Olá, mundo!\"\n")
            .rodar("dotnet fsi {arquivo}")
            .precisa(".NET SDK: https://dotnet.microsoft.com/download")
            .atalho("printfn", "mostra", "printfn \"$|\"")
            .atalho("let", "valor", "let $| = ")
            .atalho("for", "para", "for i in 1 .. $| do\n    ");

        // 40. CLOJURE -----------------------------------------------------------
        nova(ESP, "Clojure", "clj", "cljc", "edn")
            .linha(";").aspas("\"")
            .palavras("def defn defn- defmacro fn let if do when cond case loop recur ns require import quote try catch "
                    + "finally throw nil true false and or not")
            .funcoes("println print str map filter reduce first rest cons conj assoc dissoc get count inc dec range "
                    + "apply vec doseq dotimes read-line")
            .letras("-?!*<>=/")
            .modelo("(println \"Olá, mundo!\")\n")
            .rodar("clojure -M {arquivo}")
            .precisa("Clojure: https://clojure.org/guides/install_clojure")
            .atalho("println", "mostra", "(println $|)")
            .atalho("defn", "cria função", "(defn $| []\n  )")
            .atalho("let", "variáveis", "(let [$|]\n  )");

        // 41. ERLANG ------------------------------------------------------------
        nova(NICHO, "Erlang", "erl", "hrl")
            .linha("%").aspas("\"'")
            .palavras("after and andalso band begin bnot bor bsl bsr bxor case catch cond div end fun if let not of or "
                    + "orelse receive rem try when xor module export")
            .funcoes("io:format io:fwrite spawn send self length hd tl lists:map lists:filter lists:foldl")
            .letras(":")
            .modelo("#!/usr/bin/env escript\n\nmain(_) ->\n    io:format(\"Ola, mundo!~n\").\n")
            .rodar("escript {arquivo}")
            .precisa("Erlang/OTP: https://www.erlang.org/downloads")
            .atalho("format", "mostra", "io:format(\"$|~n\")")
            .atalho("case", "escolha", "case $| of\n    _ -> ok\nend");

        // 42. GLEAM -------------------------------------------------------------
        nova(NICHO, "Gleam", "gleam")
            .linha("//").aspas("\"")
            .palavras("as assert case const external fn if import let opaque pub todo type use panic True False Nil")
            .funcoes("io.println io.debug list.map list.filter string.append int.to_string")
            .tipos("Int Float String Bool List Result Option")
            .letras(".")
            .modelo("import gleam/io\n\npub fn main() {\n  io.println(\"Olá, mundo!\")\n}\n")
            .rodar("gleam run")
            .precisa("Gleam: https://gleam.run/getting-started (precisa de um projeto: \"gleam new nome\" no terminal)")
            .atalho("println", "mostra", "io.println($|)")
            .atalho("fn", "cria função", "pub fn $|() {\n  \n}")
            .atalho("case", "escolha", "case $| {\n  _ -> Nil\n}");

        // 43. VERILOG -----------------------------------------------------------
        nova(NICHO, "Verilog", "v", "sv", "vh")
            .linha("//").bloco("/*", "*/").aspas("\"")
            .palavras("module endmodule input output inout wire reg integer parameter assign always initial begin end "
                    + "if else case endcase for posedge negedge or and not localparam generate endgenerate")
            .funcoes("$display $monitor $finish $time $dumpfile $dumpvars")
            .letras("$")
            .modelo("module {nome};\n  initial begin\n    $display(\"Ola, mundo!\");\n    $finish;\n  end\nendmodule\n")
            .rodar("iverilog -o {saidaBase}.vvp {arquivo} && vvp {saidaBase}.vvp")
            .precisa("Icarus Verilog: https://bleyer.org/icarus")
            .atalho("module", "cria módulo", "module $|();\n  \nendmodule")
            .atalho("always", "bloco always", "always @(posedge clk) begin\n  $|\nend");

        // 44. VHDL --------------------------------------------------------------
        nova(NICHO, "VHDL", "vhd", "vhdl")
            .semCaixa().linha("--").aspas("\"'")
            .palavras("library use entity is port in out inout end architecture of begin signal variable constant "
                    + "process if then elsif else case when others loop for while wait report severity note")
            .tipos("std_logic std_logic_vector integer boolean bit")
            .modelo("entity {nome} is\nend {nome};\n\narchitecture comportamento of {nome} is\nbegin\n  process\n  begin\n"
                    + "    report \"Ola, mundo!\";\n    wait;\n  end process;\nend comportamento;\n")
            .rodar("ghdl -a {arquivo} && ghdl -e {nome} && ghdl -r {nome}")
            .precisa("GHDL: https://github.com/ghdl/ghdl/releases (o nome da entity tem que ser igual ao nome do arquivo)")
            .atalho("process", "processo", "process\nbegin\n  $|\nend process;");

        // 45. SAS ---------------------------------------------------------------
        nova(NICHO, "SAS", "sas")
            .semCaixa().linha("*").bloco("/*", "*/").aspas("\"'")
            .palavras("data set run proc print means freq sort by where if then else do end input datalines output "
                    + "keep drop label format title var class quit")
            .modelo("data _null_;\n    put 'Olá, mundo!';\nrun;\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("SAS é pago, mas tem a versão grátis online: SAS OnDemand for Academics (https://welcome.oda.sas.com).")
            .atalho("data", "passo data", "data $|;\n    \nrun;")
            .atalho("proc", "procedimento", "proc print data=$|;\nrun;");

        // 46. APEX --------------------------------------------------------------
        nova(NICHO, "Apex (Salesforce)", "apex", "trigger")
            .semCaixa().linha("//").bloco("/*", "*/").aspas("'")
            .palavras("public private global class static void return if else for while new insert update delete upsert "
                    + "select from where trigger on before after with sharing without virtual override try catch true false null")
            .funcoes("System.debug Database.insert Limits")
            .tipos("String Integer Decimal Boolean List Map Set Id Account Contact")
            .letras(".")
            .modelo("public class {nome} {\n    public static void ola() {\n        System.debug('Olá, mundo!');\n    }\n}\n")
            .modo(Linguagem.Modo.SO_EDITAR)
            .precisa("Apex roda na nuvem da Salesforce. Crie uma conta grátis em https://developer.salesforce.com/signup.")
            .atalho("debug", "mostra", "System.debug($|);");

        // 47. PL/SQL ------------------------------------------------------------
        nova(NICHO, "PL/SQL (Oracle)", "pls", "plsql", "pks", "pkb")
            .semCaixa().linha("--").bloco("/*", "*/").aspas("'\"")
            .palavras(Sql.PALAVRAS + " DECLARE BEGIN EXCEPTION LOOP EXIT ELSIF PROCEDURE FUNCTION PACKAGE RETURN IS "
                    + "CURSOR OPEN FETCH CLOSE DUAL")
            .funcoes(Sql.FUNCOES + " DBMS_OUTPUT.PUT_LINE NVL SYSDATE TO_CHAR TO_DATE TO_NUMBER")
            .tipos(Sql.TIPOS + " VARCHAR2 NUMBER")
            .letras(".")
            .banco("PLSQL")
            .modelo("-- Banco SIMULADO no estilo Oracle (não é real!)\n\nCREATE TABLE produtos (\n    id NUMBER PRIMARY KEY,\n"
                    + "    nome VARCHAR2(50),\n    preco NUMBER\n);\n\nINSERT INTO produtos VALUES (1, 'Caderno', 15.5);\n"
                    + "INSERT INTO produtos VALUES (2, 'Lápis', 2);\n\nSELECT nome || ' custa ' || preco AS info FROM produtos;\n\n"
                    + "BEGIN\n    DBMS_OUTPUT.PUT_LINE('Olá, mundo!');\nEND;\n/\n")
            .precisa("Nada! O TazzCode tem um banco simulado embutido.")
            .atalho("put_line", "mostra", "DBMS_OUTPUT.PUT_LINE($|);")
            .atalho("begin", "bloco", "BEGIN\n    $|\nEND;\n/")
            .atalho("select", "consulta", "SELECT * FROM $|;");

        // 48. T-SQL -------------------------------------------------------------
        nova(NICHO, "T-SQL (SQL Server)", "tsql")
            .semCaixa().linha("--").bloco("/*", "*/").aspas("'\"").variaveis("@")
            .palavras(Sql.PALAVRAS + " TOP GO PRINT DECLARE SET BEGIN END IDENTITY NVARCHAR")
            .funcoes(Sql.FUNCOES + " GETDATE LEN ISNULL NEWID")
            .tipos(Sql.TIPOS + " NVARCHAR BIT DATETIME")
            .banco("TSQL")
            .modelo("-- Banco SIMULADO no estilo SQL Server (não é real!)\n\nCREATE TABLE jogos (\n    id INT IDENTITY PRIMARY KEY,\n"
                    + "    nome NVARCHAR(50),\n    nota INT\n);\nGO\n\nINSERT INTO jogos (nome, nota) VALUES ('Minecraft', 10), ('Roblox', 9);\n"
                    + "GO\n\nSELECT TOP 1 * FROM jogos ORDER BY nota DESC;\nPRINT 'Olá, mundo!';\n")
            .precisa("Nada! O TazzCode tem um banco simulado embutido.")
            .atalho("print", "mostra", "PRINT '$|';")
            .atalho("select", "consulta", "SELECT TOP 10 * FROM $|;")
            .atalho("go", "separa lotes", "GO");

        // 49. NIM ---------------------------------------------------------------
        nova(NICHO, "Nim", "nim")
            .linha("#").bloco("#[", "]#").aspas("\"'")
            .palavras("addr and as asm bind block break case cast concept const continue converter defer discard "
                    + "distinct div do elif else end enum except export finally for from func if import in include "
                    + "interface is isnot iterator let macro method mixin mod nil not notin object of or out proc ptr "
                    + "raise ref return shl shr static template try tuple type using var when while xor yield true false")
            .funcoes("echo readLine stdin len add del high low inc dec ord chr parseInt")
            .tipos("int float string bool char seq array")
            .modelo("echo \"Olá, mundo!\"\n")
            .rodar("nim r --hints:off {arquivo}")
            .precisa("Nim: https://nim-lang.org/install.html")
            .atalho("echo", "mostra", "echo $|")
            .atalho("proc", "cria função", "proc $|() =\n  ")
            .atalho("for", "para cada", "for i in 0..<$|:\n  ");

        // 50. SCRATCH -----------------------------------------------------------
        nova(NICHO, "Scratch", "sb3", "sb2")
            .link("https://scratch.mit.edu/projects/editor")
            .precisa("O Scratch é de blocos, então abre no site: lá clique em Arquivo > Carregar do seu computador.");
    }
}
