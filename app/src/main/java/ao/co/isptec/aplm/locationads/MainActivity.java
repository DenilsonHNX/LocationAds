package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ImageView btnToPerfil = findViewById(R.id.btnToPerfil);
        btnToPerfil.setOnClickListener(v -> {
            Intent i = new Intent(this, PerfilAccount.class);
            startActivity(i);
        });

        ImageButton btnToList = findViewById(R.id.btnToList);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // exemplo: mostrar Luanda
        LatLng luanda = new LatLng(-8.838333, 13.234444);

        mMap.addMarker(new MarkerOptions().position(luanda).title("Luanda"));
    }
}
