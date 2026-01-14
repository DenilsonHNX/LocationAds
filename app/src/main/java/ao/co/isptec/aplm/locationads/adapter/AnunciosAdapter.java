package ao.co.isptec.aplm.locationads.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.ViewAds;
import ao.co.isptec.aplm.locationads.network.models.Ads;

public class AnunciosAdapter extends RecyclerView.Adapter<AnunciosAdapter.ViewHolder> {

    private static final String TAG = "AnunciosAdapter";
    private Context context;
    private List<Ads> anuncios;

    public AnunciosAdapter(Context context, List<Ads> anuncios) {
        this.context = context;
        this.anuncios = anuncios;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_anuncio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ads anuncio = anuncios.get(position);

        // Título
        holder.textTitulo.setText(anuncio.getTitulo());

        // Conteúdo
        holder.textConteudo.setText(anuncio.getConteudo());

        // Localização (se disponível)
        if (anuncio.getLocal() != null && anuncio.getLocal().getNome() != null) {
            holder.textLocalizacao.setText(anuncio.getLocal().getNome());
        } else {
            holder.textLocalizacao.setText("Local ID: " + anuncio.getLocalId());
        }

        // Período de validade
        String periodo = formatarPeriodo(anuncio.getHoraInicio(), anuncio.getHoraFim());
        holder.textPeriodo.setText(periodo);

        // Política (badge)
        String policy = anuncio.getPolicy();
        if (policy != null) {
            holder.textPolicy.setText(policy.toUpperCase());
            if ("whitelist".equalsIgnoreCase(policy)) {
                holder.textPolicy.setBackgroundResource(R.drawable.badge_whitelist);
            } else if ("blacklist".equalsIgnoreCase(policy)) {
                holder.textPolicy.setBackgroundResource(R.drawable.badge_blacklist);
            } else {
                holder.textPolicy.setBackgroundResource(R.drawable.badge_background);
            }
        }

        // Click para abrir detalhes
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewAds.class);
            intent.putExtra("ad_id", anuncio.getId());
            intent.putExtra("autor_id", anuncio.getAutorId());
            intent.putExtra("is_saved", anuncio.isSalvo());
            intent.putExtra("title", anuncio.getTitulo());
            intent.putExtra("description", anuncio.getConteudo());
            
            if (anuncio.getLocal() != null) {
                intent.putExtra("location", anuncio.getLocal().getNome());
            }
            
            if (anuncio.getCriadoEm() != null && anuncio.getCriadoEm().length() >= 10) {
                intent.putExtra("date", anuncio.getCriadoEm().substring(0, 10));
            }
            
            if (anuncio.getAutor() != null) {
                intent.putExtra("author", anuncio.getAutor().getUsername());
            }
            
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return anuncios.size();
    }

    /**
     * Formatar período de validade
     */
    private String formatarPeriodo(String inicio, String fim) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm", Locale.getDefault());

            Date dataInicio = inputFormat.parse(inicio);
            Date dataFim = inputFormat.parse(fim);

            if (dataInicio != null && dataFim != null) {
                return outputFormat.format(dataInicio) + " até " + outputFormat.format(dataFim);
            }

        } catch (Exception e) {
            // Se falhar, tentar sem milissegundos
            try {
                SimpleDateFormat inputFormat2 = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm", Locale.getDefault());

                Date dataInicio = inputFormat2.parse(inicio);
                Date dataFim = inputFormat2.parse(fim);

                if (dataInicio != null && dataFim != null) {
                    return outputFormat.format(dataInicio) + " até " + outputFormat.format(dataFim);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return "Período indisponível";
    }

    /**
     * Atualizar lista de anúncios
     */
    public void updateData(List<Ads> novosAnuncios) {
        this.anuncios = novosAnuncios;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView textTitulo;
        TextView textConteudo;
        TextView textLocalizacao;
        TextView textPeriodo;
        TextView textPolicy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textConteudo = itemView.findViewById(R.id.textConteudo);
            textLocalizacao = itemView.findViewById(R.id.textLocalizacao);
            textPeriodo = itemView.findViewById(R.id.textPeriodo);
            textPolicy = itemView.findViewById(R.id.textPolicy);
        }
    }
}