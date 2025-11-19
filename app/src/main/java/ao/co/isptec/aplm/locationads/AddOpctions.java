package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

public class AddOpctions extends DialogFragment {

    public interface AddOptionsListener {
        void onAddLocalSelected();
        void onAddAdsSelected();
    }

    private AddOptionsListener listener;

    public void setListener(AddOptionsListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_add_opctions, container, false);

        Button btnToAddLocal = view.findViewById(R.id.btnToAddLocal);
        Button btnToAddAds = view.findViewById(R.id.btnToAddAds);

        btnToAddLocal.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddLocalSelected();
            }
            dismiss();
        });

        btnToAddAds.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddAdsSelected();
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null){
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}