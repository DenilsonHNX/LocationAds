package ao.co.isptec.aplm.locationads.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ao.co.isptec.aplm.locationads.AdMessage;
import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.ViewAds;

/**
 * Adapter para lista de anúncios no RecyclerView
 */
public class AnunciosAdapter extends RecyclerView.Adapter<AnunciosAdapter.AnuncioViewHolder> {

    private Context context;
    private List<AdMessage> anuncios;

    public AnunciosAdapter(Context context, List<AdMessage> anuncios) {
        this.context = context;
        this.anuncios = anuncios;
    }

    @NonNull
    @Override
    public AnuncioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anuncio, parent, false);
        return new AnuncioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnuncioViewHolder holder, int position) {
        AdMessage anuncio = anuncios.get(position);

        // Definir dados
        holder.txtTitulo.setText(anuncio.getConteudo());
        holder.txtLocal.setText(anuncio.getLocal());
        holder.txtDescricao.setText("Publicado por: " + anuncio.getAutor());

        // Click listener para abrir detalhes
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewAds.class);
            intent.putExtra("title", anuncio.getConteudo());
            intent.putExtra("location", anuncio.getLocal());
            intent.putExtra("description", "Publicado por: " + anuncio.getAutor());
            intent.putExtra("author", anuncio.getAutor());
            // Adicione mais dados conforme necessário
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return anuncios.size();
    }

    /**
     * Atualiza a lista de anúncios
     */
    public void updateData(List<AdMessage> newAnuncios) {
        this.anuncios = newAnuncios;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder para os itens do RecyclerView
     */
    static class AnuncioViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;
        TextView txtLocal;
        TextView txtDescricao;
        ImageView imgAnuncio;

        public AnuncioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloAnuncio);
            txtLocal = itemView.findViewById(R.id.txtLocalAnuncio);
            txtDescricao = itemView.findViewById(R.id.txtDescricaoAnuncio);
            imgAnuncio = itemView.findViewById(R.id.imgAnuncio);
        }
    }
}