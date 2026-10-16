package br.com.pocketarcade.util;

import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/** Ajustes de janela comuns às seis telas. */
public final class JanelaUtil {

    private JanelaUtil() {
    }

    /**
     * Desenha atrás das barras do sistema (padrão do Android 15+) e compensa com padding,
     * incluindo o teclado, para que nada fique escondido em telas pequenas.
     */
    public static void prepararTela(ComponentActivity activity, View raiz) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));

        int esquerda = raiz.getPaddingLeft();
        int topo = raiz.getPaddingTop();
        int direita = raiz.getPaddingRight();
        int base = raiz.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());
            view.setPadding(esquerda + barras.left, topo + barras.top,
                    direita + barras.right, base + barras.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    public static void esconderTeclado(ComponentActivity activity, View foco) {
        WindowCompat.getInsetsController(activity.getWindow(), foco)
                .hide(WindowInsetsCompat.Type.ime());
    }

    /** Tecla de ação do teclado virtual (Concluído/Ir) ou Enter de um teclado físico. */
    public static boolean ehTeclaDeConfirmacao(int acao, KeyEvent evento) {
        if (evento != null) {
            return evento.getKeyCode() == KeyEvent.KEYCODE_ENTER;
        }
        return acao == EditorInfo.IME_ACTION_DONE || acao == EditorInfo.IME_ACTION_GO;
    }

    /** O Enter físico chega duas vezes (ao descer e ao subir a tecla); só a primeira deve agir. */
    public static boolean deveAgir(KeyEvent evento) {
        return evento == null || evento.getAction() == KeyEvent.ACTION_DOWN;
    }
}
