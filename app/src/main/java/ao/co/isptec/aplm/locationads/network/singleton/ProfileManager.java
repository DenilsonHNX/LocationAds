package ao.co.isptec.aplm.locationads.network.singleton;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.PerfilKeyValue;
import ao.co.isptec.aplm.locationads.network.models.UserProfile;
import com.google.gson.Gson;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

    private static final String TAG = "ProfileManager";
    private static ProfileManager instance;
    private Context context;
    private ApiService apiService;
    private SharedPreferences prefs;
    private UserProfile currentProfile;
    private Gson gson;

    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String KEY_PROFILE = "user_profile";
    private static final String KEY_PUBLIC_KEYS = "public_keys";

    public interface ProfileCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface ProfileLoadCallback {
        void onSuccess(UserProfile profile);
        void onError(String error);
    }

    public interface PublicKeysCallback {
        void onSuccess(List<String> keys);
        void onError(String error);
    }

    public static synchronized ProfileManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileManager(context.getApplicationContext());
        }
        return instance;
    }

    private ProfileManager(Context context) {
        this.context = context;
        this.apiService = ApiClient.getInstance().getApiService();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        loadCachedProfile();
    }

    private void loadCachedProfile() {
        String profileJson = prefs.getString(KEY_PROFILE, null);
        if (profileJson != null) {
            try {
                currentProfile = gson.fromJson(profileJson, UserProfile.class);
                Log.d(TAG, "Perfil carregado do cache: " + currentProfile.getPropertyCount() + " propriedades");
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar perfil do cache", e);
                currentProfile = new UserProfile();
            }
        } else {
            currentProfile = new UserProfile();
        }
    }

    private void saveCachedProfile() {
        try {
            String profileJson = gson.toJson(currentProfile);
            prefs.edit().putString(KEY_PROFILE, profileJson).apply();
            Log.d(TAG, "Perfil salvo no cache");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar perfil no cache", e);
        }
    }

    public UserProfile getCurrentProfile() {
        if (currentProfile == null) {
            currentProfile = new UserProfile();
        }
        return currentProfile;
    }

    /**
     * ✅ ADICIONAR PROPRIEDADE
     * POST /usuarios/{userId}/perfil
     */
    public void addProperty(String key, String value, ProfileCallback callback) {
        TokenManager tokenManager = TokenManager.getInstance(context);
        String token = tokenManager.getToken();
        int userId = tokenManager.getUserIdFromToken();

        if (token == null || userId == -1) {
            if (callback != null) callback.onError("Token inválido");
            return;
        }

        PerfilKeyValue property = new PerfilKeyValue(key, value);

        Log.d(TAG, "➕ Adicionando propriedade: " + key + " = " + value);

        apiService.addProfileProperty(userId, "Bearer " + token, property)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            currentProfile.addProperty(key, value);
                            saveCachedProfile();
                            Log.d(TAG, "✅ Propriedade adicionada: " + key + "=" + value);
                            if (callback != null) callback.onSuccess();
                        } else {
                            Log.e(TAG, "❌ Erro ao adicionar propriedade: " + response.code());
                            if (callback != null) callback.onError("Erro: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.e(TAG, "❌ Falha ao adicionar propriedade", t);
                        // Salvar localmente mesmo se falhar
                        currentProfile.addProperty(key, value);
                        saveCachedProfile();
                        if (callback != null) callback.onSuccess();
                    }
                });
    }

    /**
     * ✅ REMOVER PROPRIEDADE
     * DELETE /usuarios/{userId}/perfil/{chave}
     */
    public void removeProperty(String key, ProfileCallback callback) {
        TokenManager tokenManager = TokenManager.getInstance(context);
        String token = tokenManager.getToken();
        int userId = tokenManager.getUserIdFromToken();

        if (token == null || userId == -1) {
            if (callback != null) callback.onError("Token inválido");
            return;
        }

        Log.d(TAG, "🗑️ Removendo propriedade: " + key);

        apiService.removeProfileProperty(userId, key, "Bearer " + token)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            currentProfile.removeProperty(key);
                            saveCachedProfile();
                            Log.d(TAG, "✅ Propriedade removida: " + key);
                            if (callback != null) callback.onSuccess();
                        } else {
                            Log.e(TAG, "❌ Erro ao remover propriedade: " + response.code());
                            if (callback != null) callback.onError("Erro: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.e(TAG, "❌ Falha ao remover propriedade", t);
                        // Remover localmente mesmo se falhar
                        currentProfile.removeProperty(key);
                        saveCachedProfile();
                        if (callback != null) callback.onSuccess();
                    }
                });
    }

    /**
     * ✅ EDITAR PROPRIEDADE (DELETE + POST)
     * Opção B: Deleta a antiga e adiciona a nova
     */
    public void updateProperty(String key, String newValue, ProfileCallback callback) {
        Log.d(TAG, "✏️ Editando propriedade: " + key + " -> " + newValue);

        // Passo 1: Remover propriedade antiga
        removeProperty(key, new ProfileCallback() {
            @Override
            public void onSuccess() {
                // Passo 2: Adicionar propriedade com novo valor
                addProperty(key, newValue, new ProfileCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Propriedade atualizada com sucesso");
                        if (callback != null) callback.onSuccess();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Erro ao adicionar nova propriedade: " + error);
                        if (callback != null) callback.onError(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Erro ao remover propriedade antiga: " + error);
                // Tentar adicionar mesmo assim
                addProperty(key, newValue, callback);
            }
        });
    }

    /**
     * Carregar perfil do servidor
     * GET /usuarios/{userId}/perfil
     */
    public void loadProfileFromServer(ProfileLoadCallback callback) {
        TokenManager tokenManager = TokenManager.getInstance(context);
        String token = tokenManager.getToken();
        int userId = tokenManager.getUserIdFromToken();

        if (token == null || userId == -1) {
            if (callback != null) callback.onError("Token inválido");
            return;
        }

        Log.d(TAG, "🔄 Carregando perfil do servidor...");

        apiService.getUserPerfil(userId, "Bearer " + token)
                .enqueue(new Callback<List<PerfilKeyValue>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<PerfilKeyValue>> call,
                                           @NonNull Response<List<PerfilKeyValue>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<PerfilKeyValue> properties = response.body();

                            // Converter lista para UserProfile
                            currentProfile = new UserProfile();
                            for (PerfilKeyValue prop : properties) {
                                currentProfile.addProperty(prop.getKey(), prop.getValue());
                            }

                            saveCachedProfile();
                            Log.d(TAG, "✅ Perfil carregado: " + properties.size() + " propriedades");
                            if (callback != null) callback.onSuccess(currentProfile);
                        } else {
                            Log.e(TAG, "❌ Erro ao carregar perfil: " + response.code());
                            if (callback != null) callback.onError("Erro: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<PerfilKeyValue>> call, @NonNull Throwable t) {
                        Log.e(TAG, "❌ Falha ao carregar perfil", t);
                        // Retornar perfil em cache
                        if (callback != null) callback.onSuccess(currentProfile);
                    }
                });
    }

    /**
     * Obter chaves públicas
     * GET /perfil/chaves
     */
    public void getPublicKeys(PublicKeysCallback callback) {
        String token = TokenManager.getInstance(context).getToken();

        if (token == null) {
            if (callback != null) callback.onError("Token inválido");
            return;
        }

        apiService.getPublicKeys("Bearer " + token)
                .enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<String>> call,
                                           @NonNull Response<List<String>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<String> keys = response.body();
                            cachePublicKeys(keys);
                            Log.d(TAG, "✅ Chaves públicas: " + keys.size());
                            if (callback != null) callback.onSuccess(keys);
                        } else {
                            Log.e(TAG, "❌ Erro ao carregar chaves: " + response.code());
                            if (callback != null) callback.onSuccess(getCachedPublicKeys());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                        Log.e(TAG, "❌ Falha ao carregar chaves", t);
                        if (callback != null) callback.onSuccess(getCachedPublicKeys());
                    }
                });
    }

    private void cachePublicKeys(List<String> keys) {
        try {
            String keysJson = gson.toJson(keys);
            prefs.edit().putString(KEY_PUBLIC_KEYS, keysJson).apply();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao cachear chaves", e);
        }
    }

    private List<String> getCachedPublicKeys() {
        String keysJson = prefs.getString(KEY_PUBLIC_KEYS, null);
        if (keysJson != null) {
            try {
                return gson.fromJson(keysJson, ArrayList.class);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao ler cache de chaves", e);
            }
        }
        return new ArrayList<>();
    }

    public List<PerfilKeyValue> getAllProperties() {
        return currentProfile.getAllPropertiesAsList();
    }

    public void clearProfile() {
        currentProfile.clearAllProperties();
        saveCachedProfile();
    }

    public void saveProfile() {
        saveCachedProfile();
        Log.d(TAG, "Perfil salvo no cache");
    }

    public void setUsername(String username) {
        if (currentProfile != null) {
            currentProfile.setUsername(username);
            saveCachedProfile();
        }
    }

    public String getUsername() {
        if (currentProfile != null) {
            return currentProfile.getUsername();
        }
        return "";
    }
}