package br.com.pocketarcade.logica;

import java.util.Random;

/**
 * Placar de uma partida melhor de três de Pedra, Papel e Tesoura.
 * Empates não somam pontos; vence quem chegar primeiro a duas vitórias.
 */
public final class PartidaArena {

    public static final int VITORIAS_PARA_VENCER = 2;

    private final Random sorteador;
    private int pontosJogador;
    private int pontosDispositivo;
    private int rodadasJogadas;

    public PartidaArena(Random sorteador) {
        this.sorteador = sorteador;
    }

    public static ResultadoRodada comparar(Jogada jogador, Jogada dispositivo) {
        if (jogador == dispositivo) {
            return ResultadoRodada.EMPATE;
        }
        return jogador.venceDe(dispositivo) ? ResultadoRodada.VITORIA : ResultadoRodada.DERROTA;
    }

    public Jogada sortearJogadaDispositivo() {
        Jogada[] jogadas = Jogada.values();
        return jogadas[sorteador.nextInt(jogadas.length)];
    }

    public ResultadoRodada jogar(Jogada jogador, Jogada dispositivo) {
        if (terminou()) {
            throw new IllegalStateException("A partida já terminou; comece uma nova.");
        }
        rodadasJogadas++;
        ResultadoRodada resultado = comparar(jogador, dispositivo);
        if (resultado == ResultadoRodada.VITORIA) {
            pontosJogador++;
        } else if (resultado == ResultadoRodada.DERROTA) {
            pontosDispositivo++;
        }
        return resultado;
    }

    public boolean terminou() {
        return pontosJogador >= VITORIAS_PARA_VENCER || pontosDispositivo >= VITORIAS_PARA_VENCER;
    }

    public boolean jogadorVenceu() {
        return pontosJogador >= VITORIAS_PARA_VENCER;
    }

    public void reiniciar() {
        pontosJogador = 0;
        pontosDispositivo = 0;
        rodadasJogadas = 0;
    }

    public int getPontosJogador() {
        return pontosJogador;
    }

    public int getPontosDispositivo() {
        return pontosDispositivo;
    }

    public int getRodadasJogadas() {
        return rodadasJogadas;
    }
}
