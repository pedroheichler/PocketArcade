package br.com.pocketarcade;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import br.com.pocketarcade.databinding.ActivityCentralBinding;
import br.com.pocketarcade.util.JanelaUtil;

/** Tela 2: menu rolável com os quatro painéis de desafio. */
public class CentralActivity extends AppCompatActivity {

    private static final String TAG = "PocketArcade";
    private static final String TELA = "Tela 2 · Central";
    private static final String ESTADO_ULTIMO_MODULO = "ultimo_modulo";

    private ActivityCentralBinding binding;
    private String apelido;
    private String ultimoModulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TELA + " → onCreate()");

        // Validação no destino: sem apelido válido não há sessão; volta para a Tela 1.
        apelido = Navegacao.lerApelido(getIntent());
        if (apelido == null) {
            Toast.makeText(this, R.string.erro_apelido_ausente, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        binding = ActivityCentralBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        JanelaUtil.prepararTela(this, binding.getRoot());

        if (savedInstanceState != null) {
            ultimoModulo = savedInstanceState.getString(ESTADO_ULTIMO_MODULO);
        }
        binding.textoSaudacao.setText(getString(R.string.central_saudacao, apelido));

        binding.botaoAbrirScanner.setOnClickListener(v ->
                abrirDesafio(ScannerActivity.class, R.string.modulo_scanner));
        binding.botaoAbrirArena.setOnClickListener(v ->
                abrirDesafio(ArenaActivity.class, R.string.modulo_arena));
        binding.botaoAbrirCofre.setOnClickListener(v ->
                abrirDesafio(CofreActivity.class, R.string.modulo_cofre));
        binding.botaoAbrirRandomizador.setOnClickListener(v ->
                abrirDesafio(RandomizadorActivity.class, R.string.modulo_randomizador));
        binding.botaoEncerrarSessao.setOnClickListener(v ->
                Navegacao.encerrarSessao(this,
                        ultimoModulo != null ? ultimoModulo : getString(R.string.modulo_central)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, TELA + " → onResume()");
        if (ultimoModulo == null) {
            binding.textoUltimoModulo.setText(R.string.central_ultimo_nenhum);
        } else {
            binding.textoUltimoModulo.setText(getString(R.string.central_ultimo, ultimoModulo));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ESTADO_ULTIMO_MODULO, ultimoModulo);
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

    /** Abre o desafio por Intent explícita. A Central continua na pilha para o finish() voltar a ela. */
    private void abrirDesafio(Class<? extends Activity> destino, int nomeModulo) {
        ultimoModulo = getString(nomeModulo);
        startActivity(Navegacao.intentDesafio(this, destino, apelido));
    }
}
