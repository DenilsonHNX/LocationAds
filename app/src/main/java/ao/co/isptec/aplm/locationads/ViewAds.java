package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ViewAds extends AppCompatActivity {

    // Views
    private ImageButton btnVoltar;
    private ImageView adImage;
    private TextView adTitle;
    private TextView adLocation;
    private TextView adsDescription;
    private TextView adDate;
    private TextView adAuthor;
    private MaterialButton btnShare;
    private MaterialButton btnContact;
    private FloatingActionButton fabFavorite;
    private CollapsingToolbarLayout collapsingToolbar;

    // Data
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ads);

        // Inicializar views
        initViews();

        // Configurar listeners
        setupListeners();

        // Carregar dados do anúncio
        loadAdData();
    }

    /**
     * Inicializa todas as views
     */
    private void initViews() {
        btnVoltar = findViewById(R.id.btnVoltar);
        adImage = findViewById(R.id.adImage);
        adTitle = findViewById(R.id.adTitle);
        adLocation = findViewById(R.id.adLocation);
        adsDescription = findViewById(R.id.adsDescription);
        adDate = findViewById(R.id.adDate);
        adAuthor = findViewById(R.id.adAuthor);
        btnShare = findViewById(R.id.btnShare);
        btnContact = findViewById(R.id.btnContact);
        fabFavorite = findViewById(R.id.fabFavorite);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
    }

    /**
     * Configura todos os listeners
     */
    private void setupListeners() {
        // Botão Voltar
        btnVoltar.setOnClickListener(v -> {
            finish(); // Melhor usar finish() em vez de criar nova Intent
        });

        // Botão Compartilhar
        btnShare.setOnClickListener(v -> shareAd());

        // Botão Contato/Mais Informações
        btnContact.setOnClickListener(v -> showContactInfo());

        // FAB Favorito
        fabFavorite.setOnClickListener(v -> toggleFavorite());
    }

    /**
     * Carrega os dados do anúncio
     * Em produção, estes dados viriam de uma Intent ou API
     */
    private void loadAdData() {
        // Receber dados da Intent
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String location = intent.getStringExtra("location");
            String description = intent.getStringExtra("description");
            String date = intent.getStringExtra("date");
            String author = intent.getStringExtra("author");
            int imageResId = intent.getIntExtra("imageResId", R.drawable.bg_placeholder);

            // Definir dados nas views
            if (title != null) {
                adTitle.setText(title);
                collapsingToolbar.setTitle(title);
            }
            if (location != null) adLocation.setText(location);
            if (description != null) adsDescription.setText(description);
            if (date != null) adDate.setText(date);
            if (author != null) adAuthor.setText(author);
            adImage.setImageResource(imageResId);
        }

        // Dados de exemplo (caso não venha nada da Intent)
        if (adTitle.getText().toString().equals("Título do Anúncio")) {
            loadSampleData();
        }
    }

    /**
     * Carrega dados de exemplo
     */
    private void loadSampleData() {
        adTitle.setText("Promoção Especial de Verão");
        collapsingToolbar.setTitle("Promoção Especial");
        adLocation.setText("Talatona, Luanda");
        adsDescription.setText("Aproveite nossa promoção especial de verão! " +
                "Descontos incríveis em todos os produtos. " +
                "Válido até o final do mês. Venha conferir nossas ofertas " +
                "e aproveite os melhores preços da cidade!");
        adDate.setText("17/12/2024");
        adAuthor.setText("LocationAds Store");
    }

    /**
     * Compartilha o anúncio
     */
    private void shareAd() {
        String shareText = adTitle.getText().toString() + "\n" +
                adLocation.getText().toString() + "\n\n" +
                adsDescription.getText().toString();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, adTitle.getText().toString());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        try {
            startActivity(Intent.createChooser(shareIntent, "Compartilhar via"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Nenhum app de compartilhamento encontrado",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Mostra informações de contato
     */
    private void showContactInfo() {
        // Aqui você pode:
        // 1. Abrir um dialog com informações de contato
        // 2. Abrir WhatsApp
        // 3. Fazer uma ligação
        // 4. Enviar email
        // 5. Abrir tela de detalhes da empresa

        Toast.makeText(this,
                "Entrando em contato com " + adAuthor.getText().toString(),
                Toast.LENGTH_SHORT).show();

        // Exemplo: Abrir WhatsApp (descomente se quiser usar)
        // openWhatsApp("244999999999", "Olá, vi seu anúncio no LocationAds!");
    }

    /**
     * Abre WhatsApp com número e mensagem
     */
    private void openWhatsApp(String phoneNumber, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber +
                    "&text=" + android.net.Uri.encode(message);
            intent.setData(android.net.Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp não instalado", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Alterna estado de favorito
     */
    private void toggleFavorite() {
        isFavorite = !isFavorite;

        if (isFavorite) {
            // Adicionar aos favoritos
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
            Toast.makeText(this, "Adicionado aos favoritos", Toast.LENGTH_SHORT).show();

            // Aqui você salvaria no banco de dados ou SharedPreferences
            // saveFavorite();
        } else {
            // Remover dos favoritos
            fabFavorite.setImageResource(R.drawable.ic_favorite_border);
            Toast.makeText(this, "Removido dos favoritos", Toast.LENGTH_SHORT).show();

            // Aqui você removeria do banco de dados
            // removeFavorite();
        }
    }

    /**
     * Salva o anúncio como favorito
     */
    private void saveFavorite() {
        // Implementar salvamento em banco de dados ou SharedPreferences
        // Exemplo com SharedPreferences:
        /*
        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String adId = getIntent().getStringExtra("adId");
        editor.putBoolean("fav_" + adId, true);
        editor.apply();
        */
    }

    /**
     * Remove o anúncio dos favoritos
     */
    private void removeFavorite() {
        // Implementar remoção do banco de dados ou SharedPreferences
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
