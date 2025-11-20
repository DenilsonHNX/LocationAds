package ao.co.isptec.aplm.locationads;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ao.co.isptec.aplm.locationads.adapter.LocaisAdapter;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private LinearLayout containerAnuncios;
    private Map<String, String> perfilUsuario = new HashMap<>();
    private LocaisAdapter locaisAdapter;
    private RecyclerView listaLocais;

    ApiService apiService = ApiClient.getInstance().getApiService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.home_activity);

        // inicialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        containerAnuncios = findViewById(R.id.containerAnuncios);

        listaLocais = findViewById(R.id.listaLocais);


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
            AddOpctions dialog = new AddOpctions();
            dialog.setListener(new AddOpctions.AddOptionsListener() {
                @Override
                public void onAddLocalSelected() {
                    Intent intent = new Intent(MainActivity.this, AddLocal.class);
                    startActivity(intent);
                }
                @Override
                public void onAddAdsSelected() {
                    Intent intent = new Intent(MainActivity.this, AddAds.class);
                    startActivity(intent);
                }
            });
            dialog.show(getSupportFragmentManager(), "AddOptionsDialog");
        });
        View map = findViewById(R.id.map);

        Button btnMapa = findViewById(R.id.btnMapa);
        Button btnLocais = findViewById(R.id.btnLocais);
        View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        RecyclerView listaLocais = findViewById(R.id.listaLocais);

        btnMapa.setOnClickListener(v -> {
            if (mapView != null) mapView.setVisibility(View.VISIBLE);
            listaLocais.setVisibility(View.GONE);
        });

        btnLocais.setOnClickListener(v -> {
            if (mapView != null) mapView.setVisibility(View.GONE);
            listaLocais.setVisibility(View.VISIBLE);
            buscarTodosLocais();
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Aqui você pode pedir permissão ao usuário se ainda não tiver
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Minha localização"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    } else {
                        // Localização não disponível, pode usar fallback (ex: um local fixo)
                    }
                });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão dada, pode tentar obter localização novamente
                if (mMap != null) {
                    onMapReady(mMap);
                }
            } else {
                Toast.makeText(this, "Permissão de localização é necessária para mostrar sua posição no mapa", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void buscarTodosLocais() {
        apiService.getAllLocals()
                .enqueue(new retrofit2.Callback<List<Local>>() {
                    @Override
                    public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            locaisAdapter.updateData(response.body());
                        } else {
                            Toast.makeText(MainActivity.this, "Nenhum local encontrado", Toast.LENGTH_SHORT).show();
                            locaisAdapter.updateData(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Local>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Erro ao buscar locais", Toast.LENGTH_SHORT).show();
                        locaisAdapter.updateData(new ArrayList<>());
                    }
                });
    }



    private void atualizarListaLocais(List<Local> locais) {
        if (locaisAdapter == null) {
            locaisAdapter = new LocaisAdapter(locais);
            listaLocais.setAdapter(locaisAdapter);
            listaLocais.setLayoutManager(new LinearLayoutManager(this));
        } else {
            locaisAdapter = new LocaisAdapter(locais);
            listaLocais.setAdapter(locaisAdapter);
        }
    }



}