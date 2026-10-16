package br.com.pocketarcade.logica;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Regras do Scanner de Mensagens. Classe pura (sem Android), por isso é testada com JUnit.
 */
public final class AnalisadorTexto {

    public static final int MINIMO_CARACTERES = 3;

    private static final String VOGAIS = "aeiou";

    private AnalisadorTexto() {
    }

    public static ResultadoAnalise analisar(String texto) {
        return new ResultadoAnalise(
                contarCaracteresSemEspacos(texto),
                contarPalavras(texto),
                contarVogais(texto),
                inverter(texto),
                ehPalindromo(texto));
    }

    /** Pelo menos três caracteres visíveis: espaços não contam. */
    public static boolean temTamanhoMinimo(String texto) {
        return contarCaracteresSemEspacos(texto) >= MINIMO_CARACTERES;
    }

    public static int contarCaracteresSemEspacos(String texto) {
        return (int) texto.codePoints()
                .filter(c -> !Character.isWhitespace(c))
                .count();
    }

    public static int contarPalavras(String texto) {
        String limpo = texto.trim();
        if (limpo.isEmpty()) {
            return 0;
        }
        return limpo.split("\\s+").length;
    }

    /** Conta a, e, i, o, u, inclusive acentuadas (á, ê, õ...). */
    public static int contarVogais(String texto) {
        String semAcentos = removerAcentos(texto).toLowerCase(Locale.ROOT);
        int total = 0;
        for (int i = 0; i < semAcentos.length(); i++) {
            if (VOGAIS.indexOf(semAcentos.charAt(i)) >= 0) {
                total++;
            }
        }
        return total;
    }

    public static String inverter(String texto) {
        return new StringBuilder(texto).reverse().toString();
    }

    /**
     * Compara o texto com o seu inverso ignorando espaços e maiúsculas/minúsculas,
     * como pede o enunciado ("Ana", "A sacada da casa"). Acentos e pontuação também
     * são ignorados para aceitar frases como "Socorram-me, subi no ônibus em Marrocos".
     */
    public static boolean ehPalindromo(String texto) {
        String normalizado = removerAcentos(texto)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
        if (normalizado.isEmpty()) {
            return false;
        }
        return normalizado.equals(inverter(normalizado));
    }

    private static String removerAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
