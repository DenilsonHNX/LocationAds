package ao.co.isptec.aplm.locationads.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.network.models.PerfilKeyValue;

public class PropertyReadOnlyAdapter extends RecyclerView.Adapter<PropertyReadOnlyAdapter.ViewHolder> {

    private List<PerfilKeyValue> properties;

    public PropertyReadOnlyAdapter(List<PerfilKeyValue> properties) {
        this.properties = properties;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_property_readonly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PerfilKeyValue property = properties.get(position);
        holder.bind(property);
    }

    @Override
    public int getItemCount() {
        return properties != null ? properties.size() : 0;
    }

    public void updateProperties(List<PerfilKeyValue> newProperties) {
        this.properties = newProperties;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtPropertyKey;
        private TextView txtPropertyValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPropertyKey = itemView.findViewById(R.id.txtPropertyKey);
            txtPropertyValue = itemView.findViewById(R.id.txtPropertyValue);
        }

        public void bind(PerfilKeyValue property) {
            txtPropertyKey.setText(property.getKey());
            txtPropertyValue.setText(property.getValue());
        }
    }
}