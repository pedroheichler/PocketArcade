package br.com.pocketarcade.randomizador;

/** Classe de modelo do Randomizador: uma palavra e a categoria a que pertence. */
public class ItemSorteio {

    private final String palavra;
    private final String categoria;

    public ItemSorteio(String palavra, String categoria) {
        this.palavra = palavra;
        this.categoria = categoria;
    }

    public String getPalavra() {
        return palavra;
    }

    public String getCategoria() {
        return categoria;
    }

    /** Mesma palavra na mesma categoria, sem diferenciar maiúsculas. */
    public boolean equivaleA(String outraPalavra, String outraCategoria) {
        return categoria.equals(outraCategoria) && palavra.equalsIgnoreCase(outraPalavra);
    }
}
