package br.com.pocketarcade.randomizador;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import br.com.pocketarcade.R;
import br.com.pocketarcade.databinding.ItemPalavraBinding;

/**
 * O Adapter fornece as views dos itens; o ViewHolder guarda as referências (via View Binding)
 * para que cada view seja reutilizada quando o item sai da tela.
 */
public class ItemSorteioAdapter extends RecyclerView.Adapter<ItemSorteioAdapter.ItemViewHolder> {

    /** Avisa a Activity quando um item recebe clique longo (exclusão). */
    public interface AoSegurarItem {
        void aoSegurar(int posicao);
    }

    private final List<ItemSorteio> itens;
    private final List<String> categorias;
    private final int[] coresCategorias;
    private final AoSegurarItem aoSegurar;
    private ItemSorteio destacado;

    public ItemSorteioAdapter(List<ItemSorteio> itens, String[] categorias, int[] coresCategorias,
                              AoSegurarItem aoSegurar) {
        this.itens = itens;
        this.categorias = Arrays.asList(categorias);
        this.coresCategorias = coresCategorias;
        this.aoSegurar = aoSegurar;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPalavraBinding binding = ItemPalavraBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding);
        binding.getRoot().setOnLongClickListener(v -> {
            int posicao = holder.getBindingAdapterPosition();
            if (posicao != RecyclerView.NO_POSITION) {
                aoSegurar.aoSegurar(posicao);
            }
            return true;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemSorteio item = itens.get(position);
        ItemPalavraBinding b = holder.binding;
        int cor = corDaCategoria(item.getCategoria());

        b.textoPalavra.setText(item.getPalavra());
        b.textoCategoria.setText(item.getCategoria());
        b.textoCategoria.setTextColor(cor);
        b.indicadorCategoria.setBackgroundTintList(ColorStateList.valueOf(cor));
        b.getRoot().setContentDescription(b.getRoot().getContext()
                .getString(R.string.cd_item, item.getPalavra(), item.getCategoria()));

        boolean emDestaque = item == destacado;
        int corBorda = emDestaque
                ? ContextCompat.getColor(b.getRoot().getContext(), R.color.neon_amarelo)
                : ContextCompat.getColor(b.getRoot().getContext(), R.color.arcade_borda);
        b.getRoot().setStrokeColor(corBorda);
        b.getRoot().setStrokeWidth(b.getRoot().getResources().getDimensionPixelSize(
                emDestaque ? R.dimen.borda_neon : R.dimen.borda_painel));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    /** Destaca o item sorteado e tira o destaque do anterior. */
    public void destacar(ItemSorteio item) {
        int anterior = destacado == null ? -1 : itens.indexOf(destacado);
        destacado = item;
        if (anterior >= 0) {
            notifyItemChanged(anterior);
        }
        int atual = item == null ? -1 : itens.indexOf(item);
        if (atual >= 0) {
            notifyItemChanged(atual);
        }
    }

    private int corDaCategoria(String categoria) {
        int indice = categorias.indexOf(categoria);
        return indice >= 0 ? coresCategorias[indice] : coresCategorias[0];
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        final ItemPalavraBinding binding;

        ItemViewHolder(ItemPalavraBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
