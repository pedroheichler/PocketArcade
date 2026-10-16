package br.com.pocketarcade.logica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Cofre do Código Secreto: quatro dígitos diferentes e no máximo oito tentativas.
 * As pistas seguem a lógica do TERMO (verde, amarelo e cinza por dígito).
 */
public final class CodigoSecreto {

    public static final int TAMANHO = 4;
    public static final int MAX_TENTATIVAS = 8;

    /** Pista de um dígito do palpite. */
    public enum Pista {
        /** Verde: dígito certo na posição certa. */
        CERTA,
        /** Amarelo: o dígito existe, mas em outra posição. */
        POSICAO_ERRADA,
        /** Cinza: o dígito não existe no código. */
        AUSENTE
    }

    /** Motivo pelo qual um palpite foi recusado. */
    public enum ErroPalpite {
        NENHUM,
        VAZIO,
        NAO_NUMERICO,
        TAMANHO_INVALIDO,
        DIGITO_REPETIDO
    }

    private final String codigo;
    private int tentativas;
    private boolean descoberto;

    public CodigoSecreto(String codigo) {
        if (validar(codigo) != ErroPalpite.NENHUM) {
            throw new IllegalArgumentException("Código inválido: " + codigo);
        }
        this.codigo = codigo;
    }

    /** Embaralha os dígitos 0–9 e usa os quatro primeiros: nunca há repetição. */
    public static CodigoSecreto sortear(Random random) {
        List<Integer> digitos = new ArrayList<>();
        for (int d = 0; d <= 9; d++) {
            digitos.add(d);
        }
        Collections.shuffle(digitos, random);
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < TAMANHO; i++) {
            codigo.append(digitos.get(i));
        }
        return new CodigoSecreto(codigo.toString());
    }

    /** Campo não vazio, só números, exatamente quatro e sem dígitos repetidos. */
    public static ErroPalpite validar(String palpite) {
        if (palpite == null || palpite.trim().isEmpty()) {
            return ErroPalpite.VAZIO;
        }
        for (int i = 0; i < palpite.length(); i++) {
            char c = palpite.charAt(i);
            if (c < '0' || c > '9') {
                return ErroPalpite.NAO_NUMERICO;
            }
        }
        if (palpite.length() != TAMANHO) {
            return ErroPalpite.TAMANHO_INVALIDO;
        }
        for (int i = 0; i < palpite.length(); i++) {
            if (palpite.indexOf(palpite.charAt(i)) != i) {
                return ErroPalpite.DIGITO_REPETIDO;
            }
        }
        return ErroPalpite.NENHUM;
    }

    public Pista[] avaliar(String palpite) {
        if (terminou()) {
            throw new IllegalStateException("A partida já terminou.");
        }
        if (validar(palpite) != ErroPalpite.NENHUM) {
            throw new IllegalArgumentException("Palpite inválido: " + palpite);
        }
        tentativas++;
        Pista[] pistas = new Pista[TAMANHO];
        for (int i = 0; i < TAMANHO; i++) {
            char digito = palpite.charAt(i);
            if (codigo.charAt(i) == digito) {
                pistas[i] = Pista.CERTA;
            } else if (codigo.indexOf(digito) >= 0) {
                pistas[i] = Pista.POSICAO_ERRADA;
            } else {
                pistas[i] = Pista.AUSENTE;
            }
        }
        descoberto = contar(pistas, Pista.CERTA) == TAMANHO;
        return pistas;
    }

    public static int contar(Pista[] pistas, Pista tipo) {
        int total = 0;
        for (Pista pista : pistas) {
            if (pista == tipo) {
                total++;
            }
        }
        return total;
    }

    public boolean terminou() {
        return descoberto || tentativas >= MAX_TENTATIVAS;
    }

    public boolean foiDescoberto() {
        return descoberto;
    }

    public int getTentativas() {
        return tentativas;
    }

    public int getTentativasRestantes() {
        return MAX_TENTATIVAS - tentativas;
    }

    public String getCodigo() {
        return codigo;
    }
}
