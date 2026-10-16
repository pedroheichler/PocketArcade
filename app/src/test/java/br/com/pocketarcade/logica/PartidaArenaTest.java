package br.com.pocketarcade.logica;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class PartidaArenaTest {

    @Test
    public void regrasClassicas() {
        assertEquals(ResultadoRodada.VITORIA, PartidaArena.comparar(Jogada.PEDRA, Jogada.TESOURA));
        assertEquals(ResultadoRodada.VITORIA, PartidaArena.comparar(Jogada.TESOURA, Jogada.PAPEL));
        assertEquals(ResultadoRodada.VITORIA, PartidaArena.comparar(Jogada.PAPEL, Jogada.PEDRA));
        assertEquals(ResultadoRodada.DERROTA, PartidaArena.comparar(Jogada.TESOURA, Jogada.PEDRA));
        assertEquals(ResultadoRodada.EMPATE, PartidaArena.comparar(Jogada.PAPEL, Jogada.PAPEL));
    }

    @Test
    public void empateNaoSomaPontos() {
        PartidaArena partida = new PartidaArena(new Random(1));
        partida.jogar(Jogada.PEDRA, Jogada.PEDRA);
        assertEquals(0, partida.getPontosJogador());
        assertEquals(0, partida.getPontosDispositivo());
        assertEquals(1, partida.getRodadasJogadas());
        assertFalse(partida.terminou());
    }

    @Test
    public void venceQuemFazDuasVitoriasPrimeiro() {
        PartidaArena partida = new PartidaArena(new Random(1));
        partida.jogar(Jogada.PEDRA, Jogada.TESOURA);
        partida.jogar(Jogada.PEDRA, Jogada.PAPEL);
        assertFalse(partida.terminou());
        partida.jogar(Jogada.PAPEL, Jogada.PEDRA);
        assertTrue(partida.terminou());
        assertTrue(partida.jogadorVenceu());
        assertEquals(2, partida.getPontosJogador());
        assertEquals(1, partida.getPontosDispositivo());
    }

    @Test(expected = IllegalStateException.class)
    public void naoAceitaJogadaDepoisDoFim() {
        PartidaArena partida = new PartidaArena(new Random(1));
        partida.jogar(Jogada.TESOURA, Jogada.PEDRA);
        partida.jogar(Jogada.TESOURA, Jogada.PEDRA);
        partida.jogar(Jogada.PEDRA, Jogada.PEDRA);
    }

    @Test
    public void reiniciarZeraOPlacar() {
        PartidaArena partida = new PartidaArena(new Random(1));
        partida.jogar(Jogada.PEDRA, Jogada.TESOURA);
        partida.jogar(Jogada.PEDRA, Jogada.TESOURA);
        partida.reiniciar();
        assertEquals(0, partida.getPontosJogador());
        assertEquals(0, partida.getRodadasJogadas());
        assertFalse(partida.terminou());
    }

    @Test
    public void sorteioDoDispositivoCobreAsTresJogadas() {
        PartidaArena partida = new PartidaArena(new Random(42));
        boolean[] vistas = new boolean[Jogada.values().length];
        for (int i = 0; i < 200; i++) {
            vistas[partida.sortearJogadaDispositivo().ordinal()] = true;
        }
        for (boolean vista : vistas) {
            assertTrue(vista);
        }
    }
}
