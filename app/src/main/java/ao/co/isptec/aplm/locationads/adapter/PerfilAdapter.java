package ao.co.isptec.aplm.locationads.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.network.models.PerfilKeyValue;
import java.util.List;

public class PerfilAdapter extends RecyclerView.Adapter<PerfilAdapter.ViewHolder> {

    private List<PerfilKeyValue> perfilList;
    private Context context;
    private OnPropertyActionListener listener;

    public interface OnPropertyActionListener {
        void onEditProperty(PerfilKeyValue property, int position);
        void onDeleteProperty(PerfilKeyValue property, int position);
    }

    public PerfilAdapter(List<PerfilKeyValue> perfilList, Context context,
                         OnPropertyActionListener listener) {
        this.perfilList = perfilList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_perfil_property, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PerfilKeyValue property = perfilList.get(position);

        holder.textKey.setText(property.getKey());
        holder.textValue.setText(property.getValue());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProperty(property, position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteProperty(property, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return perfilList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textKey, textValue;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            textKey = itemView.findViewById(R.id.textKey);
            textValue = itemView.findViewById(R.id.textValue);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}