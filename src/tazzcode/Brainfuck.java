package tazzcode;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Um INTERPRETADOR DE BRAINFUCK feito aqui dentro do TazzCode!
 *
 * O Brainfuck tem uma fita com 30.000 "casinhas" (bytes), todas começando em 0,
 * e um ponteiro que aponta pra uma delas. Só existem 8 comandos:
 *   >  anda o ponteiro pra direita      <  anda pra esquerda
 *   +  soma 1 na casinha                -  tira 1 da casinha
 *   .  mostra a casinha como letra      ,  lê uma letra do teclado
 *   [  se a casinha for 0, pula pro ]   ]  se não for 0, volta pro [
 * Qualquer outra letra é comentário.
 */
public class Brainfuck implements Console.Interno {

    private final String codigo;

    public Brainfuck(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public void executar(java.util.function.Consumer<String> escrever,
                         java.util.function.Supplier<String> lerLinha,
                         java.util.function.BooleanSupplier foiParado) {
        // 1) Acha os pares de [ ] antes de começar
        int[] par = new int[codigo.length()];
        Deque<Integer> pilha = new ArrayDeque<>();
        for (int i = 0; i < codigo.length(); i++) {
            if (codigo.charAt(i) == '[') pilha.push(i);
            else if (codigo.charAt(i) == ']') {
                if (pilha.isEmpty()) throw new RuntimeException("tem um ] sem [ (posição " + i + ")");
                int abre = pilha.pop();
                par[abre] = i;
                par[i] = abre;
            }
        }
        if (!pilha.isEmpty()) throw new RuntimeException("tem um [ sem ] (posição " + pilha.peek() + ")");

        // 2) Roda
        byte[] fita = new byte[30000];
        int p = 0;
        String entrada = "";
        StringBuilder saida = new StringBuilder();
        long passos = 0;
        for (int i = 0; i < codigo.length(); i++) {
            if ((++passos & 0xFFFF) == 0) {
                if (foiParado.getAsBoolean()) return;
                if (saida.length() > 0) { escrever.accept(saida.toString()); saida.setLength(0); }
            }
            switch (codigo.charAt(i)) {
                case '>': p = (p + 1) % fita.length; break;
                case '<': p = (p - 1 + fita.length) % fita.length; break;
                case '+': fita[p]++; break;
                case '-': fita[p]--; break;
                case '.':
                    saida.append((char) (fita[p] & 0xFF));
                    if (fita[p] == '\n') { escrever.accept(saida.toString()); saida.setLength(0); }
                    break;
                case ',':
                    if (saida.length() > 0) { escrever.accept(saida.toString()); saida.setLength(0); }
                    if (entrada.isEmpty()) {
                        escrever.accept("[digite na caixinha de baixo e aperte ENTER]\n");
                        String l = lerLinha.get();
                        if (l == null) return;
                        entrada = l + "\n";
                    }
                    fita[p] = (byte) entrada.charAt(0);
                    entrada = entrada.substring(1);
                    break;
                case '[': if (fita[p] == 0) i = par[i]; break;
                case ']': if (fita[p] != 0) i = par[i]; break;
                default: break; // comentário
            }
        }
        if (saida.length() > 0) escrever.accept(saida.toString());
    }
}
