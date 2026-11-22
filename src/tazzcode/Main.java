package tazzcode;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * TazzCode — um editor de código feito em Java (Swing).
 *
 * Mapa do projeto (cada arquivo tem uma tarefa):
 *   Main.java          -> liga o programa e abre a tela inicial
 *   JanelaInicial.java -> a tela de boas-vindas com a lista de projetos
 *   App.java           -> o nome e a versão do programa (mude aqui se trocar o nome!)
 *   Pastas.java        -> diz onde as coisas ficam salvas no computador
 *   Config.java        -> o "mini banco": guarda configurações e arquivos recentes
 *   Linguagem.java     -> tudo o que o editor sabe sobre UMA linguagem
 *   Catalogo*.java     -> as 103 linguagens cadastradas
 *   Linguagens.java    -> monta o comando de rodar de cada arquivo
 *   Sql.java           -> o banco de dados simulado (entende SQL!)
 *   JanelaBanco.java   -> a janela do banco simulado
 *   Brainfuck.java     -> o interpretador de Brainfuck
 *   MongoSimulado.java / RedisSimulado.java -> os outros bancos simulados (MotorBanco.java)
 *   Json.java          -> lê e escreve JSON (sem biblioteca)
 *   VerificadorHtml.java / PainelProblemas.java -> acha erros no HTML
 *   ModelosProjeto.java / Projeto.java -> os modelos (React, Flask...) e o comando do projeto
 *   ServidorLocal.java -> um servidorzinho web pra abrir as páginas
 *   PainelHelper.java / ClienteClaude.java / AjudanteOffline.java -> o TazzHelper
 *   Plugins.java / JanelaPlugins.java -> linguagens portáteis que o TazzCode baixa
 *   Executaveis.java   -> acha os programas (no PC ou nos plugins)
 *   JanelaTutorial.java -> o passo a passo pra instalar cada linguagem no PC
 *   Tema.java          -> cores e fontes do visual
 *   Icone.java         -> carrega o ícone do programa
 *   BarraRolagem.java  -> barras de rolagem escuras e fininhas
 *   NumerosLinha.java  -> a coluna com os números das linhas
 *   Historico.java     -> o desfazer/refazer (Ctrl+Z / Ctrl+Y)
 *   EditorCodigo.java  -> a área de escrever (fecha parênteses, recuo, atalhos)
 *   Destaque.java      -> pinta o código com cores
 *   Autocompletar.java -> a listinha de sugestões (sout, main, fori...)
 *   Modelos.java       -> o que já vem escrito em cada tipo de arquivo novo
 *   PainelProjeto.java -> a árvore de pastas e arquivos do projeto
 *   AbaEditor.java     -> uma aba com um arquivo aberto
 *   Console.java       -> o painel de baixo que roda o código e mostra a saída
 *   JanelaEditor.java  -> a janela principal (menus, abas, console)
 */
public class Main {
    public static void main(String[] args) {
        // Arruma as pastas (quem vinha da versão antiga tem as coisas mudadas de lugar)
        String mudanca = Pastas.migrar();
        Vigia.comecar();   // anota em travamentos.log se a janela travar

        // Se der qualquer erro inesperado, mostra uma janela e salva em erro.log
        // (assim o editor nunca "some" sem explicar o que aconteceu).
        Thread.setDefaultUncaughtExceptionHandler((thread, erro) -> {
            java.io.StringWriter sw = new java.io.StringWriter();
            erro.printStackTrace(new java.io.PrintWriter(sw));
            try {
                java.nio.file.Files.writeString(Pastas.arquivoErro(), sw.toString());
            } catch (Exception ignorado) { }
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Deu um erro no " + App.NOME + ":\n\n" + erro + "\n\n(o detalhe foi salvo em erro.log)",
                    "Erro", javax.swing.JOptionPane.ERROR_MESSAGE);
        });

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                // Se não der, usa o visual padrão mesmo.
            }
            Tema.aplicarNoSwing();

            if (args.length > 0) {
                // Aberto com um arquivo (ex.: arrastar um arquivo em cima do .bat), ou reiniciando: abre direto.
                java.io.File projeto = null;
                java.util.List<String> arquivos = new java.util.ArrayList<>();
                for (String a : args) {
                    if (a.startsWith("--projeto=")) projeto = new java.io.File(a.substring(10));
                    else arquivos.add(a);
                }
                JanelaEditor janela = new JanelaEditor(projeto);
                janela.setVisible(true);
                for (String caminho : arquivos) janela.abrirArquivo(new java.io.File(caminho));
            } else {
                new JanelaInicial().setVisible(true);
            }
            if (mudanca != null) javax.swing.JOptionPane.showMessageDialog(null, mudanca, App.NOME,
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
