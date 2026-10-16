package br.com.pocketarcade;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import br.com.pocketarcade.databinding.ActivityApresentacaoBinding;
import br.com.pocketarcade.util.JanelaUtil;

/** Tela 1: o jogador se identifica com um apelido e entra no Pocket Arcade. */
public class ApresentacaoActivity extends AppCompatActivity {

    private static final String TAG = "PocketArcade";
    private static final String TELA = "Tela 1 · Apresentação";

    private ActivityApresentacaoBinding binding;
    private boolean abrindoCentral;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TELA + " → onCreate()");
        binding = ActivityApresentacaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        JanelaUtil.prepararTela(this, binding.getRoot());

        binding.botaoEntrar.setOnClickListener(v -> tentarEntrar());
        binding.campoApelido.setOnEditorActionListener((v, acao, evento) -> {
            if (!JanelaUtil.ehTeclaDeConfirmacao(acao, evento)) {
                return false;
            }
            if (JanelaUtil.deveAgir(evento)) {
                tentarEntrar();
            }
            return true;
        });
        binding.campoApelido.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int inicio, int quantidade, int depois) {
            }

            @Override
            public void onTextChanged(CharSequence s, int inicio, int antes, int quantidade) {
                binding.layoutApelido.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (savedInstanceState == null) {
            animarEntrada();
        }
    }

    /** Recebe o retorno de "Encerrar sessão" (CLEAR_TOP | SINGLE_TOP) sem recriar a tela. */
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(TAG, TELA + " → onNewIntent()");
        mostrarUltimoModulo(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, TELA + " → onResume()");
        abrindoCentral = false;
        binding.botaoEntrar.setScaleX(1f);
        binding.botaoEntrar.setScaleY(1f);
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

    private void tentarEntrar() {
        if (abrindoCentral) {
            return;
        }
        String apelido = textoDoCampo();
        if (apelido.isEmpty()) {
            mostrarErro(getString(R.string.erro_apelido_vazio));
            return;
        }
        if (apelido.length() < Navegacao.APELIDO_MINIMO) {
            mostrarErro(getString(R.string.erro_apelido_curto, Navegacao.APELIDO_MINIMO));
            return;
        }

        binding.layoutApelido.setError(null);
        abrindoCentral = true;
        JanelaUtil.esconderTeclado(this, binding.campoApelido);

        // Pulso com ViewPropertyAnimator: o CycleInterpolator(0.5) leva a escala
        // de 1 a 1.12 e de volta a 1 numa única animação; ao terminar, abre a Central.
        binding.botaoEntrar.animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setStartDelay(0)
                .setDuration(getResources().getInteger(R.integer.duracao_media))
                .setInterpolator(new CycleInterpolator(0.5f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.botaoEntrar.animate().setListener(null);
                        abrirCentral(apelido);
                    }
                });
    }

    /** Intent explícita levando o apelido para a Tela 2. */
    private void abrirCentral(String apelido) {
        Intent intent = new Intent(this, CentralActivity.class);
        intent.putExtra(Navegacao.EXTRA_APELIDO, apelido);
        startActivity(intent);
    }

    private void mostrarUltimoModulo(Intent intent) {
        String modulo = intent.getStringExtra(Navegacao.EXTRA_ULTIMO_MODULO);
        if (modulo == null || modulo.trim().isEmpty()) {
            return;
        }
        binding.textoRetorno.setText(getString(R.string.retorno_ultimo_modulo, modulo));
        binding.textoRetorno.setVisibility(View.VISIBLE);
        binding.textoRetorno.startAnimation(AnimationUtils.loadAnimation(this, R.anim.resultado_entrada));
    }

    private void mostrarErro(String mensagem) {
        binding.layoutApelido.setError(mensagem);
        binding.layoutApelido.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremer));
    }

    private String textoDoCampo() {
        return binding.campoApelido.getText() == null ? "" : binding.campoApelido.getText().toString().trim();
    }

    /** Logo surge com zoom e os blocos sobem em sequência. */
    private void animarEntrada() {
        int duracao = getResources().getInteger(R.integer.duracao_longa);
        float deslocamento = getResources().getDimension(R.dimen.deslocamento_curto);

        binding.imagemLogo.setAlpha(0f);
        binding.imagemLogo.setScaleX(0.6f);
        binding.imagemLogo.setScaleY(0.6f);
        binding.imagemLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duracao)
                .setInterpolator(new OvershootInterpolator());

        View[] blocos = {binding.cardIntegrante, binding.layoutApelido, binding.botaoEntrar};
        for (int i = 0; i < blocos.length; i++) {
            View bloco = blocos[i];
            bloco.setAlpha(0f);
            bloco.setTranslationY(deslocamento);
            bloco.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(120L * (i + 1))
                    .setDuration(duracao)
                    .setInterpolator(new DecelerateInterpolator());
        }
    }
}
