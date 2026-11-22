package tazzcode;

/**
 * As CONFIGURAÇÕES do usuário (Arquivo > Configurações), num lugar só.
 * Tudo fica salvo no config.properties, na pasta das configurações.
 */
public class Ajustes {

    private static boolean sim(String chave, boolean padrao) {
        return Config.texto(chave, padrao ? "sim" : "nao").equals("sim");
    }

    // ----- editor -----
    public static int tamanhoTab()          { return Math.max(1, Math.min(8, Config.numero("editor.tab", 4))); }
    public static String recuo()            { return " ".repeat(tamanhoTab()); }
    public static boolean fecharPares()     { return sim("editor.fecharPares", true); }
    public static boolean autocompletar()   { return sim("editor.autocompletar", true); }
    public static boolean numerosLinha()    { return sim("editor.numeros", true); }
    public static boolean destacarLinha()   { return sim("editor.linhaAtual", true); }
    public static boolean quebrarLinha()    { return sim("editor.quebrar", false); }
    public static boolean salvarSozinho()   { return sim("editor.salvarSozinho", false); }

    // ----- console -----
    public static boolean limparConsole()   { return sim("console.limpar", true); }

    // ----- ao abrir -----
    public static boolean mostrarHelper()   { return sim("helper.visivel", true); }
}
