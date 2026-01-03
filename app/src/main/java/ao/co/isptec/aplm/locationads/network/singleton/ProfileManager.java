package ao.co.isptec.aplm.locationads.network.singleton;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
<<<<<<< HEAD
import ao.co.isptec.aplm.locationads.network.models.LoginResponse;
=======
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31
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

    // Carregar perfil do cache
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

    // Salvar perfil no cache
    private void saveCachedProfile() {
        try {
            String profileJson = gson.toJson(currentProfile);
            prefs.edit().putString(KEY_PROFILE, profileJson).apply();
            Log.d(TAG, "Perfil salvo no cache");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar perfil no cache", e);
        }
    }

    // Obter perfil atual
    public UserProfile getCurrentProfile() {
        if (currentProfile == null) {
            currentProfile = new UserProfile();
        }
        return currentProfile;
    }

    // Adicionar propriedade
    public void addProperty(String key, String value, ProfileCallback callback) {
<<<<<<< HEAD


        PerfilKeyValue property = new PerfilKeyValue(key, value);
        String token = TokenManager.getInstance(context).getToken();
        String userId = TokenManager.getInstance(context).getUserId();


        apiService.addProfileProperty("Bearer " + token, property, userId)
=======
        PerfilKeyValue property = new PerfilKeyValue(key, value);
        String token = TokenManager.getInstance(context).getToken();

        apiService.addProfileProperty("Bearer " + token, property)
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            currentProfile.addProperty(key, value);
                            saveCachedProfile();
                            Log.d(TAG, "Propriedade adicionada: " + key + "=" + value);
                            if (callback != null) callback.onSuccess();
                        } else {
                            Log.e(TAG, "Erro ao adicionar propriedade: " + response.code());
                            if (callback != null) callback.onError("Erro: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.e(TAG, "Falha na requisição", t);
                        // Salvar localmente mesmo se falhar
                        currentProfile.addProperty(key, value);
                        saveCachedProfile();
                        if (callback != null) callback.onSuccess();
                    }
                });
    }

    // Remover propriedade
    public void removeProperty(String key, ProfileCallback callback) {
        String token = TokenManager.getInstance(context).getToken();

        apiService.removeProfileProperty("Bearer " + token, key)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            currentProfile.removeProperty(key);
                            saveCachedProfile();
                            Log.d(TAG, "Propriedade removida: " + key);
                            if (callback != null) callback.onSuccess();
                        } else {
                            Log.e(TAG, "Erro ao remover propriedade: " + response.code());
                            if (callback != null) callback.onError("Erro: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.e(TAG, "Falha na requisição", t);
                        // Remover localmente mesmo se falhar
                        currentProfile.removeProperty(key);
                        saveCachedProfile();
                        if (callback != null) callback.onSuccess();
                    }
                });
    }

    // Carregar perfil do servidor
    public void loadProfileFromServer(ProfileLoadCallback callback) {
        String token = TokenManager.getInstance(context).getToken();

        apiService.getUserProfile("Bearer " + token)
                .enqueue(new Callback<UserProfile>() {
                    @Override
                    public void onResponse(@NonNull Call<UserProfile> call,
                                           @NonNull Response<UserProfile> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            currentProfile = response.body();
                            saveCachedProfile();
                            Log.d(TAG, "Perfil carregado do servidor");
                            if (callback != null) callback.onSuccess(currentProfile);
                        } else {
                            Log.e(TAG, "Erro ao carregar perfil: " + response.code());
                            if (callback != null) callback.onError("Erro: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {
                        Log.e(TAG, "Falha na requisição", t);
                        // Retornar perfil em cache
                        if (callback != null) callback.onSuccess(currentProfile);
                    }
                });
    }

    // Obter chaves públicas
    public void getPublicKeys(PublicKeysCallback callback) {
        String token = TokenManager.getInstance(context).getToken();

        apiService.getPublicKeys("Bearer " + token)
                .enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<String>> call,
                                           @NonNull Response<List<String>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<String> keys = response.body();
                            cachePublicKeys(keys);
                            Log.d(TAG, "Chaves públicas carregadas: " + keys.size());
                            if (callback != null) callback.onSuccess(keys);
                        } else {
                            Log.e(TAG, "Erro ao carregar chaves: " + response.code());
                            // Retornar cache
                            if (callback != null) callback.onSuccess(getCachedPublicKeys());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                        Log.e(TAG, "Falha na requisição", t);
                        // Retornar cache
                        if (callback != null) callback.onSuccess(getCachedPublicKeys());
                    }
                });
    }

    // Cache de chaves públicas
    private void cachePublicKeys(List<String> keys) {
        try {
            String keysJson = gson.toJson(keys);
            prefs.edit().putString(KEY_PUBLIC_KEYS, keysJson).apply();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao cachear chaves públicas", e);
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

    // Obter todas as propriedades
    public List<PerfilKeyValue> getAllProperties() {
        return currentProfile.getAllPropertiesAsList();
    }

    // Limpar perfil
    public void clearProfile() {
        currentProfile.clearAllProperties();
        saveCachedProfile();
    }

    // ✅ MÉTODO QUE FALTAVA - Salvar perfil
    /**
     * Salva o perfil atual no cache local
     * Este método é chamado quando o usuário clica em "Salvar" na tela de edição
     */
    public void saveProfile() {
        saveCachedProfile();
        Log.d(TAG, "Perfil salvo manualmente pelo usuário");
    }

    // ✅ MÉTODO ADICIONAL - Sincronizar perfil com servidor
    /**
     * Sincroniza o perfil local com o servidor
     * Envia todas as propriedades para o backend
     */
    public void syncProfileWithServer(ProfileCallback callback) {
        String token = TokenManager.getInstance(context).getToken();

        apiService.updateUserProfile("Bearer " + token, currentProfile)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Perfil sincronizado com servidor");
                            if (callback != null) callback.onSuccess();
                        } else {
                            Log.e(TAG, "Erro ao sincronizar perfil: " + response.code());
                            if (callback != null) callback.onError("Erro: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.e(TAG, "Falha ao sincronizar perfil", t);
                        if (callback != null) callback.onError("Falha na conexão: " + t.getMessage());
                    }
                });
    }

    // ✅ MÉTODO ADICIONAL - Atualizar dados básicos do perfil
    /**
     * Atualiza o username do perfil atual
     */
    public void setUsername(String username) {
        if (currentProfile != null) {
            currentProfile.setUsername(username);
            saveCachedProfile();
        }
    }

    // ✅ MÉTODO ADICIONAL - Obter username
    /**
     * Retorna o username do perfil atual
     */
    public String getUsername() {
        if (currentProfile != null) {
            return currentProfile.getUsername();
        }
        return "";
    }
}