package br.com.pocketarcade.logica;

/** As três jogadas da Arena e quem vence quem. */
public enum Jogada {
    PEDRA,
    PAPEL,
    TESOURA;

    /** Pedra quebra tesoura, tesoura corta papel, papel embrulha pedra. */
    public boolean venceDe(Jogada outra) {
        switch (this) {
            case PEDRA:
                return outra == TESOURA;
            case PAPEL:
                return outra == PEDRA;
            case TESOURA:
            default:
                return outra == PAPEL;
        }
    }
}
