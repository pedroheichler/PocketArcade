package br.com.pocketarcade.logica;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import br.com.pocketarcade.logica.CodigoSecreto.ErroPalpite;
import br.com.pocketarcade.logica.CodigoSecreto.Pista;

public class CodigoSecretoTest {

    @Test
    public void exemploDoSlide_4271_contra_4219() {
        CodigoSecreto jogo = new CodigoSecreto("4271");
        Pista[] pistas = jogo.avaliar("4219");
        assertArrayEquals(new Pista[]{Pista.CERTA, Pista.CERTA, Pista.POSICAO_ERRADA, Pista.AUSENTE}, pistas);
        assertEquals(1, jogo.getTentativas());
        assertFalse(jogo.terminou());
    }

    @Test
    public void validacoesDoPalpite() {
        assertEquals(ErroPalpite.VAZIO, CodigoSecreto.validar(""));
        assertEquals(ErroPalpite.VAZIO, CodigoSecreto.validar("   "));
        assertEquals(ErroPalpite.VAZIO, CodigoSecreto.validar(null));
        assertEquals(ErroPalpite.NAO_NUMERICO, CodigoSecreto.validar("12a4"));
        assertEquals(ErroPalpite.TAMANHO_INVALIDO, CodigoSecreto.validar("123"));
        assertEquals(ErroPalpite.TAMANHO_INVALIDO, CodigoSecreto.validar("12345"));
        assertEquals(ErroPalpite.DIGITO_REPETIDO, CodigoSecreto.validar("1123"));
        assertEquals(ErroPalpite.NENHUM, CodigoSecreto.validar("0947"));
    }

    @Test
    public void acertarAbreOCofre() {
        CodigoSecreto jogo = new CodigoSecreto("0947");
        jogo.avaliar("1234");
        jogo.avaliar("0947");
        assertTrue(jogo.terminou());
        assertTrue(jogo.foiDescoberto());
        assertEquals(2, jogo.getTentativas());
    }

    @Test
    public void oitoErrosBloqueiamOCofre() {
        CodigoSecreto jogo = new CodigoSecreto("0947");
        for (int i = 0; i < CodigoSecreto.MAX_TENTATIVAS; i++) {
            assertFalse(jogo.terminou());
            jogo.avaliar("1235");
        }
        assertTrue(jogo.terminou());
        assertFalse(jogo.foiDescoberto());
        assertEquals(0, jogo.getTentativasRestantes());
    }

    @Test(expected = IllegalStateException.class)
    public void naoAvaliaDepoisDoFim() {
        CodigoSecreto jogo = new CodigoSecreto("0947");
        jogo.avaliar("0947");
        jogo.avaliar("1234");
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoAvaliaPalpiteInvalido() {
        new CodigoSecreto("0947").avaliar("1123");
    }

    @Test
    public void sorteioGeraQuatroDigitosDiferentes() {
        Random random = new Random(7);
        for (int i = 0; i < 100; i++) {
            String codigo = CodigoSecreto.sortear(random).getCodigo();
            assertEquals(CodigoSecreto.TAMANHO, codigo.length());
            Set<Character> digitos = new HashSet<>();
            for (char c : codigo.toCharArray()) {
                assertTrue(Character.isDigit(c));
                digitos.add(c);
            }
            assertEquals(CodigoSecreto.TAMANHO, digitos.size());
        }
    }

    @Test
    public void contaPistasPorTipo() {
        Pista[] pistas = {Pista.CERTA, Pista.CERTA, Pista.POSICAO_ERRADA, Pista.AUSENTE};
        assertEquals(2, CodigoSecreto.contar(pistas, Pista.CERTA));
        assertEquals(1, CodigoSecreto.contar(pistas, Pista.POSICAO_ERRADA));
        assertEquals(1, CodigoSecreto.contar(pistas, Pista.AUSENTE));
    }
}
