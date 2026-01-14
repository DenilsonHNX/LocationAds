package ao.co.isptec.aplm.locationads.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocaisAdapter extends RecyclerView.Adapter<LocaisAdapter.LocalViewHolder> {

    private static final String TAG = "LocaisAdapter";
    
    private List<Local> locais;
    private Context context;
    private int currentUserId = -1;
    private ApiService apiService;
    private OnLocalClickListener clickListener;

    public interface OnLocalClickListener {
        void onLocalClick(Local local);
        void onLocalDeleted(Local local);
    }

    public LocaisAdapter(List<Local> locais) {
        this.locais = locais;
    }

    public LocaisAdapter(Context context, List<Local> locais, OnLocalClickListener listener) {
        this.context = context;
        this.locais = locais;
        this.clickListener = listener;
        
        if (context != null) {
            this.apiService = ApiClient.getInstance(context).getApiService();
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            this.currentUserId = prefs.getInt("userId", -1);
        }
    }

    @NonNull
    @Override
    public LocalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
            apiService = ApiClient.getInstance(context).getApiService();
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            currentUserId = prefs.getInt("userId", -1);
        }
        
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_local, parent, false);
        return new LocalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalViewHolder holder, int position) {
        Local local = locais.get(position);
        
        holder.txtNome.setText(local.getNome());
        
        // Mostrar tipo do local
        if (holder.txtTipo != null) {
            String tipo = local.getTipo();
            if ("gps".equalsIgnoreCase(tipo)) {
                holder.txtTipo.setText("📍 GPS");
            } else if ("wifi".equalsIgnoreCase(tipo)) {
                holder.txtTipo.setText("📶 WiFi");
            } else {
                holder.txtTipo.setText(tipo != null ? tipo : "");
            }
        }
        
        // Mostrar coordenadas ou WiFi IDs
        if (holder.txtDetalhes != null) {
            if (local.getLatitude() != null && local.getLongitude() != null) {
                holder.txtDetalhes.setText(String.format("%.4f, %.4f", 
                        local.getLatitude(), local.getLongitude()));
            } else if (local.getWifiIds() != null && !local.getWifiIds().isEmpty()) {
                holder.txtDetalhes.setText(local.getWifiIds().size() + " redes WiFi");
            } else {
                holder.txtDetalhes.setText("");
            }
        }
        
        // Botão de apagar - só mostra se for o criador
        if (holder.btnDelete != null) {
            Integer localUserId = local.getUserId();
            boolean isCreator = currentUserId > 0 && localUserId != null && localUserId.equals(currentUserId);
            
            if (isCreator) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(v -> confirmDeleteLocal(local, position));
            } else {
                holder.btnDelete.setVisibility(View.GONE);
            }
        }
        
        // Click no item
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onLocalClick(local);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locais != null ? locais.size() : 0;
    }

    /**
     * Confirma antes de apagar o local
     */
    private void confirmDeleteLocal(Local local, int position) {
        if (context == null) return;
        
        new AlertDialog.Builder(context)
                .setTitle("Apagar Local")
                .setMessage("Tem certeza que deseja apagar o local \"" + local.getNome() + "\"?\n\nTodos os anúncios associados também serão afetados.")
                .setPositiveButton("Apagar", (dialog, which) -> deleteLocal(local, position))
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    /**
     * Apaga o local
     */
    private void deleteLocal(Local local, int position) {
        if (local.getId() == null || local.getId() <= 0) {
            Toast.makeText(context, "Erro: ID do local inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Apagando local ID: " + local.getId());

        apiService.removeLocalById(local.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    // Remover da lista
                    locais.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, locais.size());
                    
                    Toast.makeText(context, "Local apagado com sucesso", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✅ Local apagado");
                    
                    if (clickListener != null) {
                        clickListener.onLocalDeleted(local);
                    }
                } else {
                    Log.e(TAG, "Erro ao apagar: " + response.code());
                    if (response.code() == 403) {
                        Toast.makeText(context, "Sem permissão para apagar este local", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 400) {
                        Toast.makeText(context, "Não é possível apagar: local tem anúncios associados", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Erro ao apagar local", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha ao apagar local", t);
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateData(List<Local> novosLocais) {
        this.locais.clear();
        if (novosLocais != null) {
            this.locais.addAll(novosLocais);
        }
        notifyDataSetChanged();
    }

    public static class LocalViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        TextView txtTipo;
        TextView txtDetalhes;
        ImageButton btnDelete;

        public LocalViewHolder(View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtTipo = itemView.findViewById(R.id.txtTipo);
            txtDetalhes = itemView.findViewById(R.id.txtDetalhes);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
