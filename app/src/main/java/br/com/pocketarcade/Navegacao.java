package br.com.pocketarcade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Chaves dos extras e as duas formas de sair de uma tela:
 * finish() volta para a tela anterior que já está na pilha;
 * encerrarSessao() volta direto para a Tela 1 sem criar outra instância dela.
 */
public final class Navegacao {

    public static final String EXTRA_APELIDO = "br.com.pocketarcade.extra.APELIDO";
    public static final String EXTRA_ULTIMO_MODULO = "br.com.pocketarcade.extra.ULTIMO_MODULO";

    public static final int APELIDO_MINIMO = 3;

    private Navegacao() {
    }

    /** Intent explícita da Central para um desafio, levando o apelido. */
    public static Intent intentDesafio(Context origem, Class<? extends Activity> destino, String apelido) {
        Intent intent = new Intent(origem, destino);
        intent.putExtra(EXTRA_APELIDO, apelido);
        return intent;
    }

    /** Valida o apelido recebido no destino. Devolve null se o extra faltar ou for curto demais. */
    public static String lerApelido(Intent intent) {
        if (intent == null) {
            return null;
        }
        String apelido = intent.getStringExtra(EXTRA_APELIDO);
        if (apelido == null) {
            return null;
        }
        apelido = apelido.trim();
        return apelido.length() >= APELIDO_MINIMO ? apelido : null;
    }

    /**
     * Encerrar sessão: CLEAR_TOP fecha todas as Activities acima da Tela 1 e SINGLE_TOP
     * entrega esta Intent à instância existente em onNewIntent(), em vez de criar outra.
     */
    public static void encerrarSessao(Activity origem, String ultimoModulo) {
        Intent intent = new Intent(origem, ApresentacaoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_ULTIMO_MODULO, ultimoModulo);
        origem.startActivity(intent);
    }
}
