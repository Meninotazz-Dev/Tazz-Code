package tazzcode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Ajudantes pra descobrir a linguagem de um arquivo e montar o comando de rodar.
 * As linguagens em si ficam no Catalogo.
 */
public class Linguagens {

    /** Nome bonito da linguagem, pra mostrar na barra de baixo. */
    public static String nome(File arquivo) {
        return Catalogo.porArquivo(arquivo).nome;
    }

    public static String extensao(File arquivo) {
        String n = arquivo.getName();
        int ponto = n.lastIndexOf('.');
        return ponto < 0 ? "" : n.substring(ponto + 1).toLowerCase();
    }

    /**
     * Troca os {apelidos} do comando pelos caminhos de verdade e separa os pedaços.
     * Ex.: "gcc {arquivo} -o {saida}" vira [gcc, C:\...\teste.c, -o, C:\...\compilados\teste.exe]
     */
    public static List<List<String>> passos(Linguagem l, File arquivo) {
        Linguagem.Alternativa alt = l.escolhida();
        return passos(alt != null && !alt.comandos.isEmpty() ? alt.comandos : l.comandos, arquivo);
    }

    /** {todos} = todos os arquivos da pasta com a mesma extensão (ex.: todos os .java). */
    public static List<List<String>> passos(List<String> comandos, File arquivo) {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        String nome = arquivo.getName();
        int ponto = nome.lastIndexOf('.');
        String semExt = ponto > 0 ? nome.substring(0, ponto) : nome;
        File compilados = Pastas.compilados().toFile();
        compilados.mkdirs();
        String saidaBase = new File(compilados, semExt).getAbsolutePath();

        List<List<String>> resultado = new ArrayList<>();
        for (String passo : comandos) {
            List<String> partes = new ArrayList<>();
            for (String pedaco : passo.trim().split("\\s+")) {
                if (pedaco.equals("{todos}")) {
                    String ext = extensao(arquivo);
                    File[] irmaos = arquivo.getParentFile().listFiles((d, n) -> n.toLowerCase().endsWith("." + ext));
                    if (irmaos != null) for (File f : irmaos) partes.add(f.getAbsolutePath());
                    continue;
                }
                partes.add(pedaco
                        .replace("{arquivo}", arquivo.getAbsolutePath())
                        .replace("{nome}", semExt)
                        .replace("{pasta}", arquivo.getParentFile().getAbsolutePath())
                        .replace("{saidaPasta}", compilados.getAbsolutePath())
                        .replace("{saidaBase}", saidaBase)
                        .replace("{saida}", saidaBase + (windows ? ".exe" : "")));
            }
            resultado.add(partes);
        }
        return resultado;
    }
}
