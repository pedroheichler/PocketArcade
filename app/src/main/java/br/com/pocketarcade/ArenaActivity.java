package br.com.pocketarcade;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Locale;
import java.util.Random;

import br.com.pocketarcade.databinding.ActivityArenaBinding;
import br.com.pocketarcade.logica.Jogada;
import br.com.pocketarcade.logica.PartidaArena;
import br.com.pocketarcade.logica.ResultadoRodada;
import br.com.pocketarcade.util.JanelaUtil;

/** Tela 4: Pedra, Papel e Tesoura contra o dispositivo, melhor de três. */
public class ArenaActivity extends AppCompatActivity {

    private static final String TAG = "PocketArcade";
    private static final String TELA = "Tela 4 · Arena";

    private ActivityArenaBinding binding;
    private final PartidaArena partida = new PartidaArena(new Random());
    private String apelido;

    // Criados uma única vez em onCreate() e reutilizados a cada rodada.
    private MediaPlayer somVitoria;
    private MediaPlayer somDerrota;
    private MediaPlayer somEmpate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TELA + " → onCreate()");
        binding = ActivityArenaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        JanelaUtil.prepararTela(this, binding.getRoot());

        apelido = Navegacao.lerApelido(getIntent());
        if (apelido == null) {
            apelido = getString(R.string.jogador_padrao);
        }
        binding.textoNomeJogador.setText(apelido);

        binding.cabecalho.textoTitulo.setText(R.string.modulo_arena);
        binding.cabecalho.botaoVoltar.setOnClickListener(v -> finish());
        binding.cabecalho.botaoEncerrar.setOnClickListener(v ->
                Navegacao.encerrarSessao(this, getString(R.string.modulo_arena)));

        somVitoria = MediaPlayer.create(this, R.raw.som_vitoria);
        somDerrota = MediaPlayer.create(this, R.raw.som_derrota);
        somEmpate = MediaPlayer.create(this, R.raw.som_empate);

        binding.botaoPedra.setOnClickListener(v -> jogar(Jogada.PEDRA, v));
        binding.botaoPapel.setOnClickListener(v -> jogar(Jogada.PAPEL, v));
        binding.botaoTesoura.setOnClickListener(v -> jogar(Jogada.TESOURA, v));
        binding.botaoNovaPartida.setOnClickListener(v -> novaPartida());

        atualizarPlacar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, TELA + " → onResume()");
    }

    /** Ao sair da tela o áudio é pausado (pause, não stop) para poder ser reutilizado. */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, TELA + " → onPause(): pausando sons");
        pausar(somVitoria);
        pausar(somDerrota);
        pausar(somEmpate);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, TELA + " → onStop()");
    }

    /** release() libera os recursos nativos e leva cada player ao estado End. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, TELA + " → onDestroy(): liberando MediaPlayers");
        somVitoria = liberar(somVitoria);
        somDerrota = liberar(somDerrota);
        somEmpate = liberar(somEmpate);
    }

    private void jogar(Jogada escolha, View botao) {
        if (partida.terminou()) {
            return;
        }
        pulsar(botao);
        Jogada dispositivo = partida.sortearJogadaDispositivo();
        ResultadoRodada resultado = partida.jogar(escolha, dispositivo);
        Log.d(TAG, TELA + ": " + escolha + " × " + dispositivo + " = " + resultado);

        mostrarEscolha(binding.imagemEscolhaJogador, escolha, -1, R.string.cd_escolha_jogador);
        mostrarEscolha(binding.imagemEscolhaDispositivo, dispositivo, 1, R.string.cd_escolha_dispositivo);
        mostrarResultado(resultado, escolha, dispositivo);
        atualizarPlacar();

        if (resultado == ResultadoRodada.VITORIA) {
            pulsar(binding.textoPontosJogador);
            tocar(somVitoria);
        } else if (resultado == ResultadoRodada.DERROTA) {
            pulsar(binding.textoPontosDispositivo);
            tocar(somDerrota);
        } else {
            tocar(somEmpate);
        }

        if (partida.terminou()) {
            mostrarFimDaPartida();
        }
    }

    /** A escolha entra deslizando e girando: jogador pela esquerda (-1), dispositivo pela direita (1). */
    private void mostrarEscolha(ImageView imagem, Jogada jogada, int lado, int descricao) {
        imagem.setImageResource(icone(jogada));
        imagem.setContentDescription(getString(descricao, nome(jogada)));
        imagem.animate().cancel();
        imagem.setTranslationX(getResources().getDimension(R.dimen.deslocamento_animacao) * lado);
        imagem.setRotation(45f * lado);
        imagem.setAlpha(0f);
        imagem.animate()
                .translationX(0f)
                .rotation(0f)
                .alpha(1f)
                .setDuration(getResources().getInteger(R.integer.duracao_media))
                .setInterpolator(new OvershootInterpolator())
                .setListener(null);
    }

    private void mostrarResultado(ResultadoRodada resultado, Jogada escolha, Jogada dispositivo) {
        int titulo;
        int cor;
        String detalhe;
        switch (resultado) {
            case VITORIA:
                titulo = R.string.arena_vitoria;
                cor = R.color.neon_verde;
                detalhe = getString(explicacao(escolha));
                break;
            case DERROTA:
                titulo = R.string.arena_derrota;
                cor = R.color.erro;
                detalhe = getString(explicacao(dispositivo));
                break;
            case EMPATE:
            default:
                titulo = R.string.arena_empate;
                cor = R.color.neon_amarelo;
                detalhe = getString(R.string.arena_empate_detalhe,
                        nome(escolha).toLowerCase(Locale.forLanguageTag("pt-BR")));
                break;
        }
        binding.textoResultado.setText(titulo);
        binding.textoResultado.setTextColor(ContextCompat.getColor(this, cor));
        binding.textoDetalhe.setText(detalhe);

        binding.textoResultado.animate().cancel();
        binding.textoResultado.setScaleX(0.5f);
        binding.textoResultado.setScaleY(0.5f);
        binding.textoResultado.setAlpha(0f);
        binding.textoResultado.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(getResources().getInteger(R.integer.duracao_longa))
                .setInterpolator(new OvershootInterpolator(2f))
                .setListener(null);
    }

    private void atualizarPlacar() {
        binding.textoPontosJogador.setText(String.valueOf(partida.getPontosJogador()));
        binding.textoPontosDispositivo.setText(String.valueOf(partida.getPontosDispositivo()));
        if (partida.terminou()) {
            binding.textoRodada.setText(getString(R.string.arena_rodada_fim, partida.getRodadasJogadas()));
        } else {
            binding.textoRodada.setText(getString(R.string.arena_rodada, partida.getRodadasJogadas() + 1));
        }
    }

    /** Placar e resultado final ficam nesta mesma Activity. */
    private void mostrarFimDaPartida() {
        boolean venceu = partida.jogadorVenceu();
        binding.textoFinalTitulo.setText(venceu
                ? getString(R.string.arena_fim_vitoria, apelido)
                : getString(R.string.arena_fim_derrota));
        binding.textoFinalTitulo.setTextColor(ContextCompat.getColor(this,
                venceu ? R.color.neon_verde : R.color.erro));
        binding.textoFinalPlacar.setText(getString(R.string.arena_placar_final,
                partida.getPontosJogador(), partida.getPontosDispositivo()));
        habilitarJogadas(false);

        binding.cardFinal.setVisibility(View.VISIBLE);
        binding.cardFinal.setAlpha(0f);
        binding.cardFinal.setScaleX(0.85f);
        binding.cardFinal.setScaleY(0.85f);
        binding.cardFinal.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(getResources().getInteger(R.integer.duracao_longa))
                .setInterpolator(new OvershootInterpolator())
                .setListener(null);
    }

    /** O botão Nova partida reinicia o placar. */
    private void novaPartida() {
        partida.reiniciar();
        habilitarJogadas(true);
        binding.cardFinal.setVisibility(View.GONE);
        binding.imagemEscolhaJogador.setImageResource(R.drawable.ic_duvida);
        binding.imagemEscolhaJogador.setContentDescription(getString(R.string.cd_escolha_vazia));
        binding.imagemEscolhaDispositivo.setImageResource(R.drawable.ic_duvida);
        binding.imagemEscolhaDispositivo.setContentDescription(getString(R.string.cd_escolha_vazia));
        binding.textoResultado.setText(R.string.arena_aguardando);
        binding.textoResultado.setTextColor(ContextCompat.getColor(this, R.color.arcade_texto));
        binding.textoDetalhe.setText(R.string.arena_aguardando_detalhe);
        atualizarPlacar();
        pulsar(binding.cardPlacar);
    }

    private void habilitarJogadas(boolean habilitar) {
        View[] botoes = {binding.botaoPedra, binding.botaoPapel, binding.botaoTesoura};
        for (View botao : botoes) {
            botao.setEnabled(habilitar);
            botao.setAlpha(habilitar ? 1f : 0.4f);
        }
    }

    /** Pulso curto: CycleInterpolator(0.5) cresce e volta ao tamanho original. */
    private void pulsar(View view) {
        view.animate().cancel();
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(getResources().getInteger(R.integer.duracao_curta))
                .setInterpolator(new CycleInterpolator(0.5f))
                .setListener(null);
    }

    /** Reaproveita o player: volta ao início e toca de novo, sem criar outra instância. */
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

    private int icone(Jogada jogada) {
        switch (jogada) {
            case PEDRA:
                return R.drawable.ic_pedra;
            case PAPEL:
                return R.drawable.ic_papel;
            case TESOURA:
            default:
                return R.drawable.ic_tesoura;
        }
    }

    private String nome(Jogada jogada) {
        switch (jogada) {
            case PEDRA:
                return getString(R.string.arena_pedra);
            case PAPEL:
                return getString(R.string.arena_papel);
            case TESOURA:
            default:
                return getString(R.string.arena_tesoura);
        }
    }

    /** Frase que explica por que a jogada vencedora ganhou. */
    private int explicacao(Jogada vencedora) {
        switch (vencedora) {
            case PEDRA:
                return R.string.arena_pedra_vence;
            case PAPEL:
                return R.string.arena_papel_vence;
            case TESOURA:
            default:
                return R.string.arena_tesoura_vence;
        }
    }
}
