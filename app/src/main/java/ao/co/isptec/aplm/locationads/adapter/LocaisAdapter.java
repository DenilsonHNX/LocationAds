package ao.co.isptec.aplm.locationads.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.network.models.Local;

public class LocaisAdapter extends RecyclerView.Adapter<LocaisAdapter.LocalViewHolder> {
    private List<Local> locais;
    public LocaisAdapter(List<Local> locais) { this.locais = locais; }

    @Override
    public LocalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_local, parent, false);
        return new LocalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LocalViewHolder holder, int position) {
        holder.txtNome.setText(locais.get(position).getNome());
        // Adicione outros campos se desejar
    }

    @Override
    public int getItemCount() { return locais.size(); }

    public static class LocalViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        public LocalViewHolder(View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
        }


    }

    public void updateData(List<Local> novosLocais) {
        this.locais.clear();
        this.locais.addAll(novosLocais);
        notifyDataSetChanged();
    }


}
