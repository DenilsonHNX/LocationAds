package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LinearLayout containerAnuncios;
    private Map<String, String> perfilUsuario = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.home_activity);

        containerAnuncios = findViewById(R.id.containerAnuncios);

        // Recuperar perfil do usuário
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String perfilJson = prefs.getString("perfil_usuario", "{}");
        try {
            JSONObject jsonObject = new JSONObject(perfilJson);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                perfilUsuario.put(key, jsonObject.getString(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mostrarAnunciosFiltrados();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageView btnToPerfil = findViewById(R.id.btnToPerfil);
        btnToPerfil.setOnClickListener(v -> {
            Intent i = new Intent(this, PerfilAccount.class);
            startActivity(i);
        });

        ImageView btnToList = findViewById(R.id.btnToList);
        btnToList.setOnClickListener(v -> {
            Intent i = new Intent(this, ListMenu.class);
            startActivity(i);
        });

        ImageButton btnToIdea = findViewById(R.id.btnToIdea);
        btnToIdea.setOnClickListener(v -> {
            Intent i = new Intent(this, AboutApp.class);
            startActivity(i);
        });

        ImageView btnToAddAds = findViewById(R.id.btnToAddAds);
        btnToAddAds.setOnClickListener(v -> {
            Intent i = new Intent(this, AddAds.class);
            startActivity(i);
        });
    }

    private void mostrarAnunciosFiltrados() {
        List<AdMessage> todasMensagens = AdMessage.carregarMensagens();
        containerAnuncios.removeAllViews();

        for (AdMessage msg : todasMensagens) {
            boolean permitido = podeVerMensagem(perfilUsuario, msg.getWhitelist(), false) &&
                    podeVerMensagem(perfilUsuario, msg.getBlacklist(), true);
            if (permitido) {
                View anuncioView = getLayoutInflater().inflate(R.layout.item_anuncio, containerAnuncios, false);

                TextView title = anuncioView.findViewById(R.id.txtTituloAnuncio);
                TextView location = anuncioView.findViewById(R.id.txtLocalAnuncio);
                TextView desc = anuncioView.findViewById(R.id.txtDescricaoAnuncio);

                title.setText(msg.getConteudo());
                location.setText(msg.getLocal());
                desc.setText("Publicado por: " + msg.getAutor());

                containerAnuncios.addView(anuncioView);
            }
        }
    }

    private boolean podeVerMensagem(Map<String, String> perfilUsuario, Map<String, String> restricao, boolean isBlacklist) {
        if (restricao == null || restricao.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, String> regra : restricao.entrySet()) {
            String chave = regra.getKey();
            String valor = regra.getValue();
            if (isBlacklist) {
                if (perfilUsuario.containsKey(chave) && perfilUsuario.get(chave).equalsIgnoreCase(valor)) {
                    return false;
                }
            } else {
                if (!perfilUsuario.containsKey(chave) || !perfilUsuario.get(chave).equalsIgnoreCase(valor)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));
    }
}