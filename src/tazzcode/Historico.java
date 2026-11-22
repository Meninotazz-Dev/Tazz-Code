package tazzcode;

import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * DESFAZER / REFAZER (Ctrl+Z / Ctrl+Y).
 *
 * Jeito simples: a gente guarda "fotos" do texto inteiro.
 * Enquanto você digita sem parar, tudo vira uma foto só (assim o Ctrl+Z
 * desfaz uma palavra/pedaço, e não letra por letra).
 *
 * Por que não usar o UndoManager do Swing? Porque ele se confunde com as
 * cores do código e dava o erro "Stale view" quando você apertava Ctrl+Z.
 */
public class Historico {

    private static final int MAX_FOTOS = 300;

    /** Uma "foto" do texto e de onde o cursor estava. */
    private static class Foto {
        final String texto;
        final int cursor;
        Foto(String texto, int cursor) { this.texto = texto; this.cursor = cursor; }
    }

    private final EditorCodigo editor;
    private final Deque<Foto> desfazer = new ArrayDeque<>();
    private final Deque<Foto> refazer = new ArrayDeque<>();
    private Foto ultima;              // como o texto estava na última foto
    private boolean digitando;        // está no meio de um "grupo" de digitação?
    private boolean restaurando;      // true enquanto a gente mesmo está trocando o texto
    private final Timer fimDoGrupo;

    public Historico(EditorCodigo editor) {
        this.editor = editor;
        this.ultima = new Foto(editor.texto(), 0);

        // Depois de 0,7s sem digitar, fecha o grupo.
        fimDoGrupo = new Timer(700, e -> fecharGrupo());
        fimDoGrupo.setRepeats(false);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { mudou(); }
            public void removeUpdate(DocumentEvent e)  { mudou(); }
            public void changedUpdate(DocumentEvent e) { }   // mudança de cor: ignora
        });
    }

    /** Chame depois de carregar o arquivo, pra ele não "desfazer" o arquivo inteiro. */
    public void reiniciar() {
        desfazer.clear();
        refazer.clear();
        digitando = false;
        ultima = new Foto(editor.texto(), 0);
    }

    private void mudou() {
        if (restaurando) return;
        if (!digitando) {
            guardar(desfazer, ultima);   // a foto de ANTES de começar a digitar
            refazer.clear();
            digitando = true;
        }
        fimDoGrupo.restart();
    }

    private void fecharGrupo() {
        fimDoGrupo.stop();
        digitando = false;
        ultima = new Foto(editor.texto(), editor.getCaretPosition());
    }

    public void desfazer() {
        if (digitando) fecharGrupo();
        if (desfazer.isEmpty()) return;
        guardar(refazer, new Foto(editor.texto(), editor.getCaretPosition()));
        restaurar(desfazer.pop());
    }

    public void refazer() {
        if (digitando) fecharGrupo();
        if (refazer.isEmpty()) return;
        guardar(desfazer, new Foto(editor.texto(), editor.getCaretPosition()));
        restaurar(refazer.pop());
    }

    private void guardar(Deque<Foto> pilha, Foto f) {
        pilha.push(f);
        while (pilha.size() > MAX_FOTOS) pilha.removeLast();
    }

    private void restaurar(Foto f) {
        restaurando = true;
        try {
            Document doc = editor.getDocument();
            doc.remove(0, doc.getLength());
            doc.insertString(0, f.texto, null);
            editor.setCaretPosition(Math.min(f.cursor, doc.getLength()));
        } catch (BadLocationException ignorado) {
        } finally {
            restaurando = false;
        }
        ultima = f;
        editor.pintarAgora();
    }
}
