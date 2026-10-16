package br.com.pocketarcade;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import br.com.pocketarcade.databinding.ActivityScannerBinding;
import br.com.pocketarcade.logica.AnalisadorTexto;
import br.com.pocketarcade.logica.ResultadoAnalise;
import br.com.pocketarcade.util.JanelaUtil;

/** Tela 3: analisa um texto em cinco medidas. */
public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = "PocketArcade";
    private static final String TELA = "Tela 3 · Scanner";

    private ActivityScannerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TELA + " → onCreate()");
        binding = ActivityScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        JanelaUtil.prepararTela(this, binding.getRoot());

        String apelido = Navegacao.lerApelido(getIntent());
        if (apelido == null) {
            apelido = getString(R.string.jogador_padrao);
        }
        binding.textoSubtitulo.setText(getString(R.string.scanner_subtitulo, apelido));

        binding.cabecalho.textoTitulo.setText(R.string.modulo_scanner);
        binding.cabecalho.botaoVoltar.setOnClickListener(v -> finish());
        binding.cabecalho.botaoEncerrar.setOnClickListener(v ->
                Navegacao.encerrarSessao(this, getString(R.string.modulo_scanner)));

        binding.botaoAnalisar.setOnClickListener(v -> analisar());
        binding.botaoLimpar.setOnClickListener(v -> limpar());
        binding.campoMensagem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int inicio, int quantidade, int depois) {
            }

            @Override
            public void onTextChanged(CharSequence s, int inicio, int antes, int quantidade) {
                binding.layoutMensagem.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, TELA + " → onResume()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, TELA + " → onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, TELA + " → onDestroy()");
    }

    private void analisar() {
        String texto = binding.campoMensagem.getText() == null
                ? "" : binding.campoMensagem.getText().toString().trim();
        if (texto.isEmpty()) {
            mostrarErro(getString(R.string.erro_mensagem_vazia));
            return;
        }
        if (!AnalisadorTexto.temTamanhoMinimo(texto)) {
            mostrarErro(getString(R.string.erro_mensagem_curta, AnalisadorTexto.MINIMO_CARACTERES));
            return;
        }

        binding.layoutMensagem.setError(null);
        JanelaUtil.esconderTeclado(this, binding.campoMensagem);

        ResultadoAnalise resultado = AnalisadorTexto.analisar(texto);
        binding.valorCaracteres.setText(String.valueOf(resultado.getCaracteresSemEspacos()));
        binding.valorPalavras.setText(String.valueOf(resultado.getPalavras()));
        binding.valorVogais.setText(String.valueOf(resultado.getVogais()));
        binding.valorInvertido.setText(resultado.getTextoInvertido());
        binding.valorPalindromo.setText(resultado.isPalindromo() ? R.string.sim : R.string.nao);
        binding.valorPalindromo.setTextColor(ContextCompat.getColor(this,
                resultado.isPalindromo() ? R.color.neon_verde : R.color.erro));

        // Animação XML no painel a cada nova análise.
        binding.painelResultado.animate().cancel();
        binding.painelResultado.setAlpha(1f);
        binding.painelResultado.setVisibility(View.VISIBLE);
        binding.painelResultado.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.resultado_entrada));
    }

    private void limpar() {
        binding.campoMensagem.setText("");
        binding.layoutMensagem.setError(null);
        binding.campoMensagem.requestFocus();
        if (binding.painelResultado.getVisibility() != View.VISIBLE) {
            return;
        }
        // Painel some com fade (ViewPropertyAnimator) e só então sai do layout.
        binding.painelResultado.animate()
                .alpha(0f)
                .setDuration(getResources().getInteger(R.integer.duracao_curta))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.painelResultado.animate().setListener(null);
                        binding.painelResultado.setVisibility(View.GONE);
                        binding.painelResultado.setAlpha(1f);
                    }
                });
    }

    private void mostrarErro(String mensagem) {
        binding.layoutMensagem.setError(mensagem);
        binding.layoutMensagem.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremer));
    }
}
