package ao.co.isptec.aplm.locationads;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.card.MaterialCardView;

public class AddOptionsDialog extends DialogFragment {

    private static final String TAG = "AddOptionsDialog";

    public interface AddOptionsListener {
        void onAddLocalSelected();
        void onAddAdsSelected();
    }

    private AddOptionsListener listener;

    public void setListener(AddOptionsListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Remove o título padrão do dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_add_opctions, container, false);

        initViews(view);
        setupListeners(view);

        return view;
    }

    private void initViews(View view) {
        // Views já estão no layout
    }

    private void setupListeners(View view) {
        ImageButton btnClose = view.findViewById(R.id.btnClose);
        MaterialCardView cardAddLocal = view.findViewById(R.id.cardAddLocal);
        MaterialCardView cardAddAd = view.findViewById(R.id.cardAddAd);

        // Botão fechar
        btnClose.setOnClickListener(v -> dismiss());

        // Card Adicionar Local
        cardAddLocal.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddLocalSelected();
            }
            dismiss();
        });

        // Card Adicionar Anúncio
        cardAddAd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddAdsSelected();
            }
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // Define o tamanho do dialog
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);

            // Define o fundo transparente para mostrar os cantos arredondados
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}