package ao.co.isptec.aplm.locationads.adapter;

import android.content.Context;
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
        // Nota: Você precisará buscar o nome do local pela localId
        holder.textLocalizacao.setText("Local ID: " + anuncio.getLocalId());

        // Período de validade
        String periodo = formatarPeriodo(anuncio.getHoraInicio(), anuncio.getHoraFim());
        holder.textPeriodo.setText(periodo);

        // Política (badge)
        holder.textPolicy.setText(anuncio.getPolicy());
        if ("WHITELIST".equals(anuncio.getPolicy())) {
            holder.textPolicy.setBackgroundResource(R.drawable.badge_whitelist);
        } else {
            holder.textPolicy.setBackgroundResource(R.drawable.badge_blacklist);
        }
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