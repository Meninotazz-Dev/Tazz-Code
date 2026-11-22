import java.util.Scanner;

public class Ola {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Olá! Qual é o seu nome?");
        String nome = sc.nextLine();
        System.out.println("Prazer, " + nome + "! Esse código rodou dentro do TazzCode.");
    }
}
