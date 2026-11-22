package tazzcode;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Uma ABA do editor: um arquivo aberto, com a área de código e os números das linhas.
 */
public class AbaEditor extends JPanel {

    private final EditorCodigo texto = new EditorCodigo();
    private final Historico historico;
    private File arquivo;          // null = arquivo novo, ainda sem nome
    private boolean modificado;
    private final JScrollPane rolagem;
    private final NumerosLinha numeros;
    private Runnable aoMudar = () -> { };   // avisa a janela pra atualizar o título da aba

    public AbaEditor(File arquivo) {
        super(new BorderLayout());
        this.arquivo = arquivo;

        rolagem = new JScrollPane(texto);
        numeros = new NumerosLinha(texto);
        if (Ajustes.numerosLinha()) rolagem.setRowHeaderView(numeros);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.getViewport().setBackground(Tema.FUNDO);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        rolagem.getHorizontalScrollBar().setUnitIncrement(16);
        add(rolagem, BorderLayout.CENTER);

        if (arquivo != null) carregar();
        atualizarLinguagem();
        historico = new Historico(texto);   // Ctrl+Z / Ctrl+Y
        texto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { marcarModificado(); }
            public void removeUpdate(DocumentEvent e)  { marcarModificado(); }
            public void changedUpdate(DocumentEvent e) { }
        });
    }

    private void atualizarLinguagem() {
        if (arquivo == null) texto.setExtensao("", "Main");
        else texto.setExtensao(Linguagens.extensao(arquivo), arquivo.getName());
    }

    // ---------- arquivo ----------

    private void carregar() {
        try {
            String conteudo = new String(Files.readAllBytes(arquivo.toPath()), StandardCharsets.UTF_8);
            if (conteudo.startsWith("﻿")) conteudo = conteudo.substring(1);   // tira o "BOM" invisível
            // Arruma qualquer quebra de linha estranha (\r\n, \r\r\n, \r sozinho) pra \n.
            texto.setText(conteudo.replace("\r\n", "\n").replace("\r", "").replace("\t", Ajustes.recuo()));
            texto.setCaretPosition(0);
            modificado = false;
        } catch (IOException e) {
            texto.setText("// Não consegui abrir: " + e.getMessage());
        }
    }

    /** Lê o arquivo do disco de novo (quando ele foi renomeado ou mudou lá fora). */
    public void trocarArquivo(File novo) {
        arquivo = novo;
        atualizarLinguagem();
        aoMudar.run();
    }

    public void salvarEm(File destino) throws IOException {
        // O documento guarda as quebras de linha como \n. No Windows a gente salva com \r\n.
        // (Antes usava getText(), que JÁ devolvia \r\n no Windows, e aí ficava \r\r\n: linhas dobradas!)
        String quebra = System.lineSeparator();
        String conteudo = texto.texto().replace("\n", quebra);
        Files.write(destino.toPath(), conteudo.getBytes(StandardCharsets.UTF_8));
        boolean mudouNome = !destino.equals(arquivo);
        arquivo = destino;
        modificado = false;
        if (mudouNome) atualizarLinguagem();
        aoMudar.run();
    }

    private void marcarModificado() {
        if (!modificado) {
            modificado = true;
            aoMudar.run();
        }
    }

    // ---------- coisas que a janela usa ----------

    public String titulo() {
        String nome = arquivo == null ? "Sem título" : arquivo.getName();
        return modificado ? "● " + nome : nome;
    }

    public File getArquivo()            { return arquivo; }
    public boolean isModificado()       { return modificado; }
    public EditorCodigo getTexto()      { return texto; }
    public void setAoMudar(Runnable r)  { this.aoMudar = r; }

    public void desfazer() { historico.desfazer(); }
    public void refazer()  { historico.refazer(); }

    /** Aplica as Configurações (fonte, números de linha, quebra de linha) sem reabrir. */
    public void aplicarConfiguracoes() {
        texto.setFont(Tema.fonteCodigo());
        rolagem.setRowHeaderView(Ajustes.numerosLinha() ? numeros : null);
        numeros.revalidate();
        texto.revalidate();
        revalidate();
        repaint();
    }

    public void atualizarFonte() {
        texto.setFont(Tema.fonteCodigo());
        revalidate();
        repaint();
    }

    /** "Linha 3, Coluna 10" — pra barra de baixo. */
    public String posicaoCursor() {
        int pos = texto.getCaretPosition();
        int linha = texto.linhaDe(pos);
        int coluna = pos - texto.inicioDaLinha(linha);
        return "Linha " + (linha + 1) + ", Coluna " + (coluna + 1);
    }
}
