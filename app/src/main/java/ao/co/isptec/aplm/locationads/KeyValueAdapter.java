package ao.co.isptec.aplm.locationads;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyValueAdapter extends RecyclerView.Adapter<KeyValueAdapter.ViewHolder> {

    private Map<String, String> map;

    public KeyValueAdapter(Map<String, String> map) {
        this.map = map;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_key_value, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<String> keys = new ArrayList<>(map.keySet());
        String key = keys.get(position);
        String value = map.get(key);

        holder.key.setText(key);
        holder.value.setText(value);

        holder.btnRemove.setOnClickListener(v -> {
            map.remove(key);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return map.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView key, value;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            key = itemView.findViewById(R.id.tvKey);
            value = itemView.findViewById(R.id.tvValue);
            btnRemove = itemView.findViewById(R.id.btnRemovePair);
        }
    }

    // Caso queira atualizar o mapa externamente depois de criado o adapter
    public void atualizarMapa(Map<String, String> novoMapa) {
        this.map = novoMapa;
        notifyDataSetChanged();
    }
}