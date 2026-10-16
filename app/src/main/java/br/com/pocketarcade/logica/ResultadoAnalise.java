package br.com.pocketarcade.logica;

/** As cinco medidas que o Scanner de Mensagens mostra para um texto. */
public final class ResultadoAnalise {

    private final int caracteresSemEspacos;
    private final int palavras;
    private final int vogais;
    private final String textoInvertido;
    private final boolean palindromo;

    public ResultadoAnalise(int caracteresSemEspacos, int palavras, int vogais,
                            String textoInvertido, boolean palindromo) {
        this.caracteresSemEspacos = caracteresSemEspacos;
        this.palavras = palavras;
        this.vogais = vogais;
        this.textoInvertido = textoInvertido;
        this.palindromo = palindromo;
    }

    public int getCaracteresSemEspacos() {
        return caracteresSemEspacos;
    }

    public int getPalavras() {
        return palavras;
    }

    public int getVogais() {
        return vogais;
    }

    public String getTextoInvertido() {
        return textoInvertido;
    }

    public boolean isPalindromo() {
        return palindromo;
    }
}
