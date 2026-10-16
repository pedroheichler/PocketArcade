package br.com.pocketarcade;

import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Random;

import br.com.pocketarcade.databinding.ActivityCofreBinding;
import br.com.pocketarcade.databinding.ItemTentativaBinding;
import br.com.pocketarcade.logica.CodigoSecreto;
import br.com.pocketarcade.logica.CodigoSecreto.ErroPalpite;
import br.com.pocketarcade.logica.CodigoSecreto.Pista;
import br.com.pocketarcade.util.JanelaUtil;

/** Tela 5: descobrir um código de quatro dígitos diferentes em até oito tentativas. */
public class CofreActivity extends AppCompatActivity {

    private static final String TAG = "PocketArcade";
    private static final String TELA = "Tela 5 · Cofre";

    private ActivityCofreBinding binding;
    private final Random random = new Random();
    private CodigoSecreto jogo;
    private String apelido;

    /** Marcado no início da partida e comparado no fim; o tempo não é atualizado continuamente. */
    private long inicioMillis;

    private MediaPlayer somVitoria;
    private MediaPlayer somDerrota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TELA + " → onCreate()");
        binding = ActivityCofreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        JanelaUtil.prepararTela(this, binding.getRoot());

        apelido = Navegacao.lerApelido(getIntent());
        if (apelido == null) {
            apelido = getString(R.string.jogador_padrao);
        }
        binding.textoSubtitulo.setText(getString(R.string.cofre_subtitulo, apelido));

        binding.cabecalho.textoTitulo.setText(R.string.modulo_cofre);
        binding.cabecalho.botaoVoltar.setOnClickListener(v -> finish());
        binding.cabecalho.botaoEncerrar.setOnClickListener(v ->
                Navegacao.encerrarSessao(this, getString(R.string.modulo_cofre)));

        somVitoria = MediaPlayer.create(this, R.raw.som_vitoria);
        somDerrota = MediaPlayer.create(this, R.raw.som_derrota);

        binding.botaoTentar.setOnClickListener(v -> tentar());
        binding.botaoNovaPartida.setOnClickListener(v -> novaPartida());
        binding.campoPalpite.setOnEditorActionListener((v, acao, evento) -> {
            if (!JanelaUtil.ehTeclaDeConfirmacao(acao, evento)) {
                return false;
            }
            if (JanelaUtil.deveAgir(evento)) {
                tentar();
            }
            return true;
        });
        binding.campoPalpite.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int inicio, int quantidade, int depois) {
            }

            @Override
            public void onTextChanged(CharSequence s, int inicio, int antes, int quantidade) {
                binding.layoutPalpite.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        novaPartida();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, TELA + " → onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, TELA + " → onPause(): pausando sons");
        pausar(somVitoria);
        pausar(somDerrota);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, TELA + " → onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, TELA + " → onDestroy(): liberando MediaPlayers");
        somVitoria = liberar(somVitoria);
        somDerrota = liberar(somDerrota);
    }

    private void novaPartida() {
        jogo = CodigoSecreto.sortear(random);
        inicioMillis = System.currentTimeMillis();
        // Facilita a demonstração em vídeo: o código aparece no Logcat.
        Log.d(TAG, TELA + ": código sorteado = " + jogo.getCodigo());

        binding.containerHistorico.removeAllViews();
        binding.textoHistoricoVazio.setVisibility(View.VISIBLE);
        binding.cardConclusao.setVisibility(View.GONE);
        binding.campoPalpite.setText("");
        binding.layoutPalpite.setError(null);
        esconderCodigo();
        habilitarEntrada(true);
        atualizarTentativas();
    }

    private void tentar() {
        if (jogo.terminou()) {
            return;
        }
        String palpite = binding.campoPalpite.getText() == null
                ? "" : binding.campoPalpite.getText().toString().trim();

        // Valida antes de aceitar: não vazio, quatro números e sem repetição.
        ErroPalpite erro = CodigoSecreto.validar(palpite);
        if (erro != ErroPalpite.NENHUM) {
            mostrarErro(mensagemDeErro(erro));
            return;
        }

        Pista[] pistas = jogo.avaliar(palpite);
        adicionarAoHistorico(palpite, pistas);
        binding.campoPalpite.setText("");
        atualizarTentativas();

        if (jogo.terminou()) {
            concluir();
        }
    }

    private String mensagemDeErro(ErroPalpite erro) {
        switch (erro) {
            case VAZIO:
                return getString(R.string.erro_palpite_vazio);
            case NAO_NUMERICO:
                return getString(R.string.erro_palpite_numerico);
            case TAMANHO_INVALIDO:
                return getString(R.string.erro_palpite_tamanho, CodigoSecreto.TAMANHO);
            case DIGITO_REPETIDO:
            default:
                return getString(R.string.erro_palpite_repetido);
        }
    }

    /** Histórico textual: cada tentativa vira uma linha com os dígitos pintados pela pista. */
    private void adicionarAoHistorico(String palpite, Pista[] pistas) {
        binding.textoHistoricoVazio.setVisibility(View.GONE);
        ItemTentativaBinding linha = ItemTentativaBinding.inflate(
                getLayoutInflater(), binding.containerHistorico, false);

        int numero = jogo.getTentativas();
        int certas = CodigoSecreto.contar(pistas, Pista.CERTA);
        int deslocadas = CodigoSecreto.contar(pistas, Pista.POSICAO_ERRADA);
        int ausentes = CodigoSecreto.contar(pistas, Pista.AUSENTE);
        linha.textoNumero.setText(getString(R.string.cofre_numero_tentativa, numero));
        linha.textoResumo.setText(getString(R.string.cofre_resumo, certas, deslocadas, ausentes));
        linha.getRoot().setContentDescription(getString(R.string.cd_tentativa,
                numero, palpite, certas, deslocadas, ausentes));

        TextView[] tiles = {linha.digito1, linha.digito2, linha.digito3, linha.digito4};
        int duracao = getResources().getInteger(R.integer.duracao_curta);
        int atraso = getResources().getInteger(R.integer.atraso_tile);
        for (int i = 0; i < tiles.length; i++) {
            tiles[i].setText(String.valueOf(palpite.charAt(i)));
            tiles[i].setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, corDaPista(pistas[i]))));
            // Os dígitos "viram" um após o outro, como no TERMO.
            tiles[i].setRotationX(90f);
            tiles[i].animate()
                    .rotationX(0f)
                    .setStartDelay((long) atraso * i)
                    .setDuration(duracao)
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(null);
        }

        View raiz = linha.getRoot();
        raiz.setAlpha(0f);
        raiz.setTranslationY(-getResources().getDimension(R.dimen.deslocamento_curto));
        binding.containerHistorico.addView(raiz);
        raiz.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(duracao)
                .setListener(null);
    }

    private int corDaPista(Pista pista) {
        switch (pista) {
            case CERTA:
                return R.color.pista_verde;
            case POSICAO_ERRADA:
                return R.color.pista_amarelo;
            case AUSENTE:
            default:
                return R.color.pista_cinza;
        }
    }

    /** Conclusão na mesma tela: resultado, tentativas e tempo decorrido. */
    private void concluir() {
        long fimMillis = System.currentTimeMillis();
        String tempo = formatarTempo(fimMillis - inicioMillis);
        boolean venceu = jogo.foiDescoberto();
        Log.d(TAG, TELA + ": fim de partida, venceu=" + venceu + ", tempo=" + tempo);

        habilitarEntrada(false);
        JanelaUtil.esconderTeclado(this, binding.campoPalpite);
        revelarCodigo(venceu);

        binding.textoConclusaoTitulo.setText(venceu ? R.string.cofre_vitoria_titulo : R.string.cofre_derrota_titulo);
        binding.textoConclusaoTitulo.setTextColor(ContextCompat.getColor(this,
                venceu ? R.color.neon_verde : R.color.erro));
        binding.textoConclusaoMensagem.setText(venceu
                ? getString(R.string.cofre_vitoria_mensagem, apelido, jogo.getCodigo())
                : getString(R.string.cofre_derrota_mensagem, jogo.getCodigo()));
        binding.textoConclusaoEstatisticas.setText(getString(R.string.cofre_estatisticas,
                jogo.getTentativas(), CodigoSecreto.MAX_TENTATIVAS, tempo));

        int duracao = getResources().getInteger(R.integer.duracao_longa);
        binding.cardConclusao.setVisibility(View.VISIBLE);
        // Com teclado físico, o foco sairia do campo desativado para a seta de voltar.
        binding.botaoNovaPartida.requestFocus();
        binding.cardConclusao.setAlpha(0f);
        if (venceu) {
            // Sucesso: o cartão cresce com overshoot e o cofre dá uma volta completa.
            binding.cardConclusao.setScaleX(0.6f);
            binding.cardConclusao.setScaleY(0.6f);
            binding.cardConclusao.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duracao)
                    .setInterpolator(new OvershootInterpolator())
                    .setListener(null);
            binding.imagemCofre.animate()
                    .rotationBy(360f)
                    .setDuration(duracao)
                    .setListener(null);
            tocar(somVitoria);
        } else {
            // Fracasso: o cofre treme (animação XML) e o cartão surge descendo.
            binding.cardCofre.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremer));
            binding.cardConclusao.setTranslationY(-getResources().getDimension(R.dimen.deslocamento_curto));
            binding.cardConclusao.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(duracao)
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(null);
            tocar(somDerrota);
        }
        binding.rolagemCofre.smoothScrollTo(0, 0);
    }

    private String formatarTempo(long duracaoMillis) {
        long totalSegundos = duracaoMillis / 1000;
        long minutos = totalSegundos / 60;
        long segundos = totalSegundos % 60;
        if (minutos > 0) {
            return getString(R.string.tempo_minutos, minutos, segundos);
        }
        return getString(R.string.tempo_segundos, totalSegundos);
    }

    private TextView[] slots() {
        return new TextView[]{binding.slot1, binding.slot2, binding.slot3, binding.slot4};
    }

    private void esconderCodigo() {
        for (TextView slot : slots()) {
            slot.setText(R.string.cofre_slot_oculto);
            slot.setTextColor(ContextCompat.getColor(this, R.color.arcade_texto));
        }
        binding.containerSlots.setContentDescription(getString(R.string.cd_cofre_oculto));
    }

    private void revelarCodigo(boolean venceu) {
        TextView[] slots = slots();
        int cor = ContextCompat.getColor(this, venceu ? R.color.neon_verde : R.color.erro);
        int atraso = getResources().getInteger(R.integer.atraso_tile);
        for (int i = 0; i < slots.length; i++) {
            slots[i].setText(String.valueOf(jogo.getCodigo().charAt(i)));
            slots[i].setTextColor(cor);
            slots[i].setRotationY(90f);
            slots[i].animate()
                    .rotationY(0f)
                    .setStartDelay((long) atraso * i)
                    .setDuration(getResources().getInteger(R.integer.duracao_curta))
                    .setListener(null);
        }
        binding.containerSlots.setContentDescription(getString(R.string.cd_cofre_revelado, jogo.getCodigo()));
    }

    private void atualizarTentativas() {
        binding.textoTentativas.setText(getString(R.string.cofre_tentativas,
                jogo.getTentativasRestantes(), CodigoSecreto.MAX_TENTATIVAS));
    }

    private void habilitarEntrada(boolean habilitar) {
        binding.layoutPalpite.setEnabled(habilitar);
        binding.campoPalpite.setEnabled(habilitar);
        binding.botaoTentar.setEnabled(habilitar);
    }

    private void mostrarErro(String mensagem) {
        binding.layoutPalpite.setError(mensagem);
        binding.layoutPalpite.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremer));
    }

    private void tocar(MediaPlayer som) {
        if (som == null) {
            return;
        }
        if (som.isPlaying()) {
            som.pause();
        }
        som.seekTo(0);
        som.start();
    }

    private void pausar(MediaPlayer som) {
        if (som != null && som.isPlaying()) {
            som.pause();
        }
    }

    private MediaPlayer liberar(MediaPlayer som) {
        if (som != null) {
            som.release();
        }
        return null;
    }
}
