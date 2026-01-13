package ao.co.isptec.aplm.locationads.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Gerenciador de recebimento de mensagens

 * Conforme especificação:
 * - Se usuário RECEBE explicitamente: mensagem fica disponível mesmo após sair do local ou expirar
 * - Se NÃO recebe explicitamente: mensagem não fica disponível após sair ou expirar
 */
public class MessageReceiptManager {

    private static final String TAG = "MessageReceiptManager";
    private static final String PREFS_NAME = "MessageReceipts";
    private static final String KEY_RECEIVED_MESSAGES = "received_messages";

    private static MessageReceiptManager instance;
    private SharedPreferences prefs;
    private Set<Integer> receivedMessageIds;

    private MessageReceiptManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        receivedMessageIds = new HashSet<>();
        loadReceivedMessages();
    }

    public static synchronized MessageReceiptManager getInstance(Context context) {
        if (instance == null) {
            instance = new MessageReceiptManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Carrega mensagens recebidas do armazenamento local
     */
    private void loadReceivedMessages() {
        Set<String> receivedSet = prefs.getStringSet(KEY_RECEIVED_MESSAGES, new HashSet<>());
        for (String id : receivedSet) {
            try {
                receivedMessageIds.add(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Erro ao carregar ID: " + id);
            }
        }
        Log.d(TAG, "📥 Mensagens recebidas carregadas: " + receivedMessageIds.size());
    }

    /**
     * Salva mensagens recebidas
     */
    private void saveReceivedMessages() {
        Set<String> receivedSet = new HashSet<>();
        for (Integer id : receivedMessageIds) {
            receivedSet.add(String.valueOf(id));
        }
        prefs.edit().putStringSet(KEY_RECEIVED_MESSAGES, receivedSet).apply();
        Log.d(TAG, "💾 Mensagens recebidas salvas: " + receivedMessageIds.size());
    }

    /**
     * Marca mensagem como recebida explicitamente pelo usuário
     *
     * Quando usuário clica para ler a mensagem = recebimento explícito
     * A partir deste momento, a mensagem fica disponível permanentemente
     */
    public void markAsReceived(int messageId) {
        if (!receivedMessageIds.contains(messageId)) {
            receivedMessageIds.add(messageId);
            saveReceivedMessages();
            Log.d(TAG, "✅ Mensagem " + messageId + " marcada como recebida");
        }
    }

    /**
     * Verifica se mensagem foi recebida explicitamente
     */
    public boolean wasExplicitlyReceived(int messageId) {
        return receivedMessageIds.contains(messageId);
    }

    /**
     * Remove mensagem dos recebidos (caso usuário delete, por exemplo)
     */
    public void removeReceived(int messageId) {
        if (receivedMessageIds.remove(messageId)) {
            saveReceivedMessages();
            Log.d(TAG, "🗑️ Mensagem " + messageId + " removida dos recebidos");
        }
    }

    /**
     * Limpa todas as mensagens recebidas
     */
    public void clearAll() {
        receivedMessageIds.clear();
        saveReceivedMessages();
        Log.d(TAG, "🗑️ Todas as mensagens recebidas foram limpas");
    }

    /**
     * Obtém total de mensagens recebidas
     */
    public int getTotalReceived() {
        return receivedMessageIds.size();
    }

    /**
     * Obtém IDs das mensagens recebidas
     */
    public Set<Integer> getReceivedMessageIds() {
        return new HashSet<>(receivedMessageIds);
    }
}