package ao.co.isptec.aplm.locationads.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.ViewAds;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import okhttp3.ResponseBody; // ✅ ESTE É O CORRETO!
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnunciosAdapter extends RecyclerView.Adapter<AnunciosAdapter.ViewHolder> {

    private static final String TAG = "AnunciosAdapter";

    private Context context;
    private List<Ads> anuncios;
    private ApiService apiService;
    private OnItemClickListener listener;
    private Set<Integer> savedAdsIds;

    public interface OnItemClickListener {
        void onItemClick(Ads ads);
        void onSaveClick(Ads ads, boolean isSaved);
    }

    public AnunciosAdapter(Context context, List<Ads> anuncios) {
        this.context = context;
        this.anuncios = anuncios;
        this.apiService = ApiClient.getInstance(context).getApiService();
        this.savedAdsIds = new HashSet<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSavedAdsIds(Set<Integer> savedIds) {
        this.savedAdsIds = savedIds;
        notifyDataSetChanged();
    }

    public void addSavedAdId(int id) {
        savedAdsIds.add(id);
    }

    public void removeSavedAdId(int id) {
        savedAdsIds.remove(id);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anuncio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ads anuncio = anuncios.get(position);

        // Título
        if (anuncio.getTitulo() != null && !anuncio.getTitulo().isEmpty()) {
            holder.textTitulo.setText(anuncio.getTitulo());
        } else {
            holder.textTitulo.setText("Sem título");
        }

        // Conteúdo
        if (anuncio.getConteudo() != null && !anuncio.getConteudo().isEmpty()) {
            holder.textConteudo.setText(anuncio.getConteudo());
        } else {
            holder.textConteudo.setText("Sem descrição");
        }

        // Localização (se disponível)
        if (anuncio.getLocal() != null && anuncio.getLocal().getNome() != null) {
            holder.textLocalizacao.setText(anuncio.getLocal().getNome());
        } else {
            holder.textLocalizacao.setText("Local ID: " + anuncio.getLocalId());
        }

        // Período
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
        return anuncios != null ? anuncios.size() : 0;
    }

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

    public void updateData(List<Ads> novosAnuncios) {
        this.anuncios = novosAnuncios;
        notifyDataSetChanged();
    }

    private void handleSaveClick(Ads ads, ImageButton btnSalvar, int position) {
        int adId = ads.getId();
        boolean isSaved = savedAdsIds.contains(adId);

        Log.d(TAG, "🔖 Clique - ID: " + adId + ", Salvo: " + isSaved);

        if (isSaved) {
            unsaveAd(ads, btnSalvar, position);
        } else {
            saveAd(ads, btnSalvar, position);
        }
    }

    private void saveAd(Ads ads, ImageButton btnSalvar, int position) {
        int adId = ads.getId();
        Log.d(TAG, "💾 Salvando: " + ads.getTitulo() + " (ID: " + adId + ")");

        // ✅ CORRETO: Callback<ResponseBody>
        apiService.saveMessage(adId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Salvo com sucesso!");

                    savedAdsIds.add(adId);
                    updateSaveButton(btnSalvar, true);

                    if (listener != null) {
                        listener.onSaveClick(ads, true);
                    }

                    Toast.makeText(context, "Anúncio salvo!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "❌ Erro ao salvar: " + response.code());
                    Toast.makeText(context, "Erro ao salvar anúncio", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Falha: " + t.getMessage());
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unsaveAd(Ads ads, ImageButton btnSalvar, int position) {
        int adId = ads.getId();
        Log.d(TAG, "🗑️ Removendo: " + ads.getTitulo() + " (ID: " + adId + ")");

        // ✅ CORRETO: Callback<ResponseBody>
        apiService.unsaveMessage(adId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Removido com sucesso!");

                    savedAdsIds.remove(adId);

                    if (listener != null) {
                        listener.onSaveClick(ads, false);
                    }

                    Toast.makeText(context, "Anúncio removido dos salvos", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "❌ Erro ao remover: " + response.code());
                    Toast.makeText(context, "Erro ao remover anúncio", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Falha: " + t.getMessage());
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSaveButton(ImageButton button, boolean isSaved) {
        if (isSaved) {
            button.setImageResource(R.drawable.ic_bookmark);
        } else {
            button.setImageResource(R.drawable.ic_bookmark_border);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView textTitulo;
        TextView textConteudo;
        TextView textLocalizacao;
        TextView textPeriodo;
        TextView textPolicy;
        ImageButton btnSalvar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textConteudo = itemView.findViewById(R.id.textConteudo);
            textLocalizacao = itemView.findViewById(R.id.textLocalizacao);
            textPeriodo = itemView.findViewById(R.id.textPeriodo);
            textPolicy = itemView.findViewById(R.id.textPolicy);
            btnSalvar = itemView.findViewById(R.id.btnSalvar);
        }
    }
}