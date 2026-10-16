package br.com.pocketarcade;

import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.com.pocketarcade.databinding.ActivityRandomizadorBinding;
import br.com.pocketarcade.randomizador.ItemSorteio;
import br.com.pocketarcade.randomizador.ItemSorteioAdapter;
import br.com.pocketarcade.util.JanelaUtil;

/** Tela 6: cadastro de palavras por categoria e sorteio com RecyclerView. */
public class RandomizadorActivity extends AppCompatActivity {

    private static final String TAG = "PocketArcade";
    private static final String TELA = "Tela 6 · Randomizador";

    private static final String ESTADO_PALAVRAS = "palavras";
    private static final String ESTADO_CATEGORIAS = "categorias";
    private static final String ESTADO_GRADE = "em_grade";

    private ActivityRandomizadorBinding binding;
    private final ArrayList<ItemSorteio> itens = new ArrayList<>();
    private final Random random = new Random();
    private ItemSorteioAdapter adapter;
    private String[] categorias;
    private boolean emGrade;

    private MediaPlayer somSorteio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TELA + " → onCreate()");
        binding = ActivityRandomizadorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        JanelaUtil.prepararTela(this, binding.getRoot());

        String apelido = Navegacao.lerApelido(getIntent());
        if (apelido == null) {
            apelido = getString(R.string.jogador_padrao);
        }
        binding.textoSubtitulo.setText(getString(R.string.rand_subtitulo, apelido));

        binding.cabecalho.textoTitulo.setText(R.string.modulo_randomizador);
        binding.cabecalho.botaoVoltar.setOnClickListener(v -> finish());
        binding.cabecalho.botaoEncerrar.setOnClickListener(v ->
                Navegacao.encerrarSessao(this, getString(R.string.modulo_randomizador)));

        categorias = getResources().getStringArray(R.array.categorias);
        if (savedInstanceState == null || !restaurarItens(savedInstanceState)) {
            carregarExemplos();
        }
        emGrade = savedInstanceState != null && savedInstanceState.getBoolean(ESTADO_GRADE);

        adapter = new ItemSorteioAdapter(itens, categorias, lerCoresCategorias(), this::removerItem);
        binding.listaItens.setAdapter(adapter);
        aplicarLayoutManager();

        ArrayAdapter<CharSequence> adapterCategorias = ArrayAdapter.createFromResource(
                this, R.array.categorias, R.layout.item_spinner);
        adapterCategorias.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinnerCategoria.setAdapter(adapterCategorias);
        binding.spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int posicao, long id) {
                atualizarContagem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        somSorteio = MediaPlayer.create(this, R.raw.som_sorteio);

        binding.botaoAdicionar.setOnClickListener(v -> adicionar());
        binding.botaoSortear.setOnClickListener(v -> sortear());
        binding.botaoAlternarLayout.setOnClickListener(v -> alternarLayout());
        binding.campoValor.setOnEditorActionListener((v, acao, evento) -> {
            if (!JanelaUtil.ehTeclaDeConfirmacao(acao, evento)) {
                return false;
            }
            if (JanelaUtil.deveAgir(evento)) {
                adicionar();
            }
            return true;
        });
        binding.campoValor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int inicio, int quantidade, int depois) {
            }

            @Override
            public void onTextChanged(CharSequence s, int inicio, int antes, int quantidade) {
                binding.layoutValor.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        atualizarContagem();
        atualizarListaVazia();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, TELA + " → onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, TELA + " → onPause(): pausando som");
        if (somSorteio != null && somSorteio.isPlaying()) {
            somSorteio.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, TELA + " → onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, TELA + " → onDestroy(): liberando MediaPlayer");
        if (somSorteio != null) {
            somSorteio.release();
            somSorteio = null;
        }
    }

    /** Mantém os itens cadastrados se a Activity for recriada pelo sistema. */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> palavras = new ArrayList<>();
        ArrayList<String> categoriasItens = new ArrayList<>();
        for (ItemSorteio item : itens) {
            palavras.add(item.getPalavra());
            categoriasItens.add(item.getCategoria());
        }
        outState.putStringArrayList(ESTADO_PALAVRAS, palavras);
        outState.putStringArrayList(ESTADO_CATEGORIAS, categoriasItens);
        outState.putBoolean(ESTADO_GRADE, emGrade);
    }

    private void adicionar() {
        String valor = binding.campoValor.getText() == null
                ? "" : binding.campoValor.getText().toString().trim();
        if (valor.isEmpty()) {
            mostrarErro(getString(R.string.erro_valor_vazio));
            return;
        }
        String categoria = categoriaSelecionada();
        for (ItemSorteio item : itens) {
            if (item.equivaleA(valor, categoria)) {
                mostrarErro(getString(R.string.erro_valor_repetido, valor, categoria));
                return;
            }
        }

        itens.add(new ItemSorteio(valor, categoria));
        int posicao = itens.size() - 1;
        adapter.notifyItemInserted(posicao);
        binding.listaItens.smoothScrollToPosition(posicao);

        binding.campoValor.setText("");
        atualizarContagem();
        atualizarListaVazia();
    }

    /** Clique longo em um item: remove da lista e avisa o Adapter. */
    private void removerItem(int posicao) {
        ItemSorteio removido = itens.remove(posicao);
        adapter.notifyItemRemoved(posicao);
        Toast.makeText(this, getString(R.string.rand_removido, removido.getPalavra()),
                Toast.LENGTH_SHORT).show();
        atualizarContagem();
        atualizarListaVazia();
    }

    /** Sorteia apenas entre os itens da categoria selecionada no Spinner. */
    private void sortear() {
        String categoria = categoriaSelecionada();
        List<ItemSorteio> candidatos = new ArrayList<>();
        for (ItemSorteio item : itens) {
            if (item.getCategoria().equals(categoria)) {
                candidatos.add(item);
            }
        }
        if (candidatos.isEmpty()) {
            Toast.makeText(this, getString(R.string.erro_categoria_vazia, categoria),
                    Toast.LENGTH_LONG).show();
            binding.cardSorteio.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremer));
            return;
        }

        ItemSorteio sorteado = candidatos.get(random.nextInt(candidatos.size()));
        Log.d(TAG, TELA + ": sorteado \"" + sorteado.getPalavra() + "\" em " + categoria);
        binding.textoCategoriaSorteada.setText(getString(R.string.rand_sorteado_em, categoria));
        binding.textoSorteado.setText(sorteado.getPalavra());
        animarSorteio();
        tocarSom();

        adapter.destacar(sorteado);
        binding.listaItens.smoothScrollToPosition(itens.indexOf(sorteado));
    }

    /** O dado gira duas voltas e a palavra sorteada surge com zoom. */
    private void animarSorteio() {
        int duracao = getResources().getInteger(R.integer.duracao_longa);
        binding.imagemDado.animate()
                .rotationBy(720f)
                .setDuration(duracao)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null);

        binding.textoSorteado.animate().cancel();
        binding.textoSorteado.setPivotX(0f);
        binding.textoSorteado.setAlpha(0f);
        binding.textoSorteado.setScaleX(0.4f);
        binding.textoSorteado.setScaleY(0.4f);
        binding.textoSorteado.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duracao)
                .setInterpolator(new OvershootInterpolator(2f))
                .setListener(null);
    }

    private void tocarSom() {
        if (somSorteio == null) {
            return;
        }
        if (somSorteio.isPlaying()) {
            somSorteio.pause();
        }
        somSorteio.seekTo(0);
        somSorteio.start();
    }

    /** Alterna entre LinearLayoutManager (lista) e GridLayoutManager (grade). */
    private void alternarLayout() {
        emGrade = !emGrade;
        aplicarLayoutManager();
    }

    private void aplicarLayoutManager() {
        if (emGrade) {
            binding.listaItens.setLayoutManager(new GridLayoutManager(this,
                    getResources().getInteger(R.integer.colunas_grade)));
            binding.botaoAlternarLayout.setText(R.string.rand_ver_lista);
            binding.botaoAlternarLayout.setIconResource(R.drawable.ic_lista);
        } else {
            binding.listaItens.setLayoutManager(new LinearLayoutManager(this));
            binding.botaoAlternarLayout.setText(R.string.rand_ver_grade);
            binding.botaoAlternarLayout.setIconResource(R.drawable.ic_grade);
        }
    }

    private String categoriaSelecionada() {
        int posicao = binding.spinnerCategoria.getSelectedItemPosition();
        return categorias[Math.max(posicao, 0)];
    }

    private void atualizarContagem() {
        String categoria = categoriaSelecionada();
        int total = 0;
        for (ItemSorteio item : itens) {
            if (item.getCategoria().equals(categoria)) {
                total++;
            }
        }
        if (total == 0) {
            binding.textoContagem.setText(getString(R.string.rand_contagem_zero, categoria));
        } else {
            binding.textoContagem.setText(getResources().getQuantityString(
                    R.plurals.rand_contagem, total, total, categoria));
        }
    }

    private void atualizarListaVazia() {
        binding.textoListaVazia.setVisibility(itens.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarErro(String mensagem) {
        binding.layoutValor.setError(mensagem);
        binding.layoutValor.startAnimation(AnimationUtils.loadAnimation(this, R.anim.tremer));
    }

    private void carregarExemplos() {
        TypedArray exemplos = getResources().obtainTypedArray(R.array.exemplos_por_categoria);
        for (int i = 0; i < exemplos.length() && i < categorias.length; i++) {
            int lista = exemplos.getResourceId(i, 0);
            for (String palavra : getResources().getStringArray(lista)) {
                itens.add(new ItemSorteio(palavra, categorias[i]));
            }
        }
        exemplos.recycle();
    }

    private boolean restaurarItens(Bundle estado) {
        ArrayList<String> palavras = estado.getStringArrayList(ESTADO_PALAVRAS);
        ArrayList<String> categoriasItens = estado.getStringArrayList(ESTADO_CATEGORIAS);
        if (palavras == null || categoriasItens == null || palavras.size() != categoriasItens.size()) {
            return false;
        }
        for (int i = 0; i < palavras.size(); i++) {
            itens.add(new ItemSorteio(palavras.get(i), categoriasItens.get(i)));
        }
        return true;
    }

    private int[] lerCoresCategorias() {
        TypedArray cores = getResources().obtainTypedArray(R.array.cores_categorias);
        int[] resultado = new int[cores.length()];
        for (int i = 0; i < resultado.length; i++) {
            resultado[i] = cores.getColor(i, 0);
        }
        cores.recycle();
        return resultado;
    }
}
