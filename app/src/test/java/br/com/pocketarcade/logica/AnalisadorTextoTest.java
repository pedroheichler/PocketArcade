package br.com.pocketarcade.logica;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AnalisadorTextoTest {

    @Test
    public void exemploDoSlide_Ana() {
        ResultadoAnalise r = AnalisadorTexto.analisar("Ana");
        assertEquals(3, r.getCaracteresSemEspacos());
        assertEquals(1, r.getPalavras());
        assertEquals(2, r.getVogais());
        assertEquals("anA", r.getTextoInvertido());
        assertTrue(r.isPalindromo());
    }

    @Test
    public void palindromoIgnoraEspacosEMaiusculas() {
        assertTrue(AnalisadorTexto.ehPalindromo("A sacada da casa"));
        assertTrue(AnalisadorTexto.ehPalindromo("Roma me tem amor"));
    }

    @Test
    public void palindromoIgnoraAcentosEPontuacao() {
        assertTrue(AnalisadorTexto.ehPalindromo("Socorram-me, subi no ônibus em Marrocos!"));
    }

    @Test
    public void textoComumNaoEhPalindromo() {
        assertFalse(AnalisadorTexto.ehPalindromo("Pocket Arcade"));
        assertFalse(AnalisadorTexto.ehPalindromo("!!!"));
    }

    @Test
    public void contaPalavrasComEspacosRepetidos() {
        assertEquals(3, AnalisadorTexto.contarPalavras("  um   dois\ttrês  "));
        assertEquals(0, AnalisadorTexto.contarPalavras("   "));
    }

    @Test
    public void contaCaracteresSemEspacos() {
        assertEquals(13, AnalisadorTexto.contarCaracteresSemEspacos("A sacada da casa"));
    }

    @Test
    public void contaVogaisAcentuadas() {
        // Ágata (Á, a, a) + ébrio (é, i, o) + Ônix (Ô, i) = 8 vogais
        assertEquals(8, AnalisadorTexto.contarVogais("Ágata ébrio Ônix"));
        assertEquals(0, AnalisadorTexto.contarVogais("xyz 123"));
    }

    @Test
    public void inverteTextoPreservandoMaiusculas() {
        assertEquals("edacrA", AnalisadorTexto.inverter("Arcade"));
    }

    @Test
    public void validaMinimoDeTresCaracteresSemContarEspacos() {
        assertTrue(AnalisadorTexto.temTamanhoMinimo("Ana"));
        assertFalse(AnalisadorTexto.temTamanhoMinimo("a b"));
        assertFalse(AnalisadorTexto.temTamanhoMinimo(""));
    }
}
