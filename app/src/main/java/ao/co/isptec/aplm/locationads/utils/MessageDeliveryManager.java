package ao.co.isptec.aplm.locationads.utils;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.models.UserProfile;

/**
 * Gerenciador de entrega de mensagens
 *
 * Implementa toda a lógica conforme especificação:
 * 1. Filtra mensagens baseado em local do usuário
 * 2. Aplica políticas (whitelist/blacklist)
 * 3. Verifica janela de tempo
 * 4. Gerencia recebimento explícito/implícito
 */
public class MessageDeliveryManager {

    private static final String TAG = "MessageDeliveryManager";

    private static MessageDeliveryManager instance;
    private Context context;
    private MessageReceiptManager receiptManager;

    private MessageDeliveryManager(Context context) {
        this.context = context.getApplicationContext();
        this.receiptManager = MessageReceiptManager.getInstance(context);
    }

    public static synchronized MessageDeliveryManager getInstance(Context context) {
        if (instance == null) {
            instance = new MessageDeliveryManager(context);
        }
        return instance;
    }

    /**
     * Filtra mensagens que o usuário pode ver
     *
     * Conforme especificação:
     * - Mensagens recebidas explicitamente sempre aparecem
     * - Outras mensagens só aparecem se:
     *   1. Usuário está no local correto (se skipLocationCheck = false)
     *   2. Dentro da janela de tempo
     *   3. Perfil corresponde à política
     *
     * @param allMessages Todas as mensagens disponíveis
     * @param userProfile Perfil do usuário
     * @param currentLocalId Local atual do usuário
     * @param skipLocationCheck Se true, ignora verificação de local
     * @return Lista de mensagens que o usuário pode ver
     */
    public List<Ads> filterMessagesForUser(List<Ads> allMessages,
                                           UserProfile userProfile,
                                           int currentLocalId,
                                           boolean skipLocationCheck) {
        Log.d(TAG, "═══════════════════════════════════");
        Log.d(TAG, "🔍 Filtrando mensagens para usuário");
        Log.d(TAG, "   Local atual: " + currentLocalId);
        Log.d(TAG, "   Total de mensagens: " + allMessages.size());

        List<Ads> visibleMessages = new ArrayList<>();
        int explicitlyReceivedCount = 0;
        int policyAllowedCount = 0;
        int deniedCount = 0;

        for (Ads message : allMessages) {
            Integer messageId = message.getId();
            if (messageId == null) continue;

            // REGRA 1: Mensagens explicitamente recebidas SEMPRE aparecem
            if (receiptManager.wasExplicitlyReceived(messageId)) {
                visibleMessages.add(message);
                explicitlyReceivedCount++;
                Log.d(TAG, "   ✅ Msg " + messageId + ": Explicitamente recebida");
                continue;
            }

            // REGRA 2: Outras mensagens devem passar pela avaliação de política
            if (PolicyEvaluator.canReceiveMessage(message, userProfile, currentLocalId, skipLocationCheck)) {
                visibleMessages.add(message);
                policyAllowedCount++;
                Log.d(TAG, "   ✅ Msg " + messageId + ": Política permite");
            } else {
                deniedCount++;
                Log.d(TAG, "   ❌ Msg " + messageId + ": Política nega");
            }
        }

        Log.d(TAG, "───────────────────────────────────");
        Log.d(TAG, "📊 RESUMO:");
        Log.d(TAG, "   Total analisadas: " + allMessages.size());
        Log.d(TAG, "   ✅ Explicitamente recebidas: " + explicitlyReceivedCount);
        Log.d(TAG, "   ✅ Permitidas por política: " + policyAllowedCount);
        Log.d(TAG, "   ❌ Negadas: " + deniedCount);
        Log.d(TAG, "   📥 TOTAL VISÍVEIS: " + visibleMessages.size());
        Log.d(TAG, "═══════════════════════════════════");

        return visibleMessages;
    }

    /**
     * Marca mensagem como lida/recebida pelo usuário
     *
     * Deve ser chamado quando usuário:
     * - Clica para ler a mensagem
     * - Aceita explicitamente receber a notificação
     *
     * A partir deste momento, a mensagem fica disponível permanentemente
     */
    public void receiveMessage(int messageId) {
        receiptManager.markAsReceived(messageId);
        Log.d(TAG, "✅ Usuário recebeu mensagem " + messageId + " explicitamente");
    }

    /**
     * Verifica se mensagem foi explicitamente recebida
     */
    public boolean wasMessageReceived(int messageId) {
        return receiptManager.wasExplicitlyReceived(messageId);
    }

    /**
     * Verifica se usuário PODE receber uma mensagem específica agora
     * (sem considerar recebimento explícito anterior)
     */
    public boolean canReceiveMessageNow(Ads message, UserProfile userProfile, int currentLocalId) {
        return PolicyEvaluator.canReceiveMessage(message, userProfile, currentLocalId, false);
    }

    /**
     * Obtém mensagens disponíveis para notificação
     *
     * São mensagens que:
     * 1. Usuário PODE receber (política permite)
     * 2. Ainda NÃO recebeu explicitamente
     *
     * Estas são as mensagens para mostrar na notificação push
     */
    public List<Ads> getMessagesForNotification(List<Ads> allMessages,
                                                UserProfile userProfile,
                                                int currentLocalId) {
        List<Ads> notificationMessages = new ArrayList<>();

        for (Ads message : allMessages) {
            Integer messageId = message.getId();
            if (messageId == null) continue;

            // Se já recebeu, não notificar novamente
            if (receiptManager.wasExplicitlyReceived(messageId)) {
                continue;
            }

            // Se política permite, incluir na notificação
            if (PolicyEvaluator.canReceiveMessage(message, userProfile, currentLocalId, false)) {
                notificationMessages.add(message);
            }
        }

        Log.d(TAG, "🔔 Mensagens para notificação: " + notificationMessages.size());
        return notificationMessages;
    }

    /**
     * Remove mensagem (por exemplo, quando proprietário deleta)
     */
    public void removeMessage(int messageId) {
        receiptManager.removeReceived(messageId);
        Log.d(TAG, "🗑️ Mensagem " + messageId + " removida");
    }

    /**
     * Obtém estatísticas
     */
    public String getStats() {
        return "Mensagens recebidas: " + receiptManager.getTotalReceived();
    }

    /**
     * Limpa histórico de recebimento
     */
    public void clearReceiptHistory() {
        receiptManager.clearAll();
        Log.d(TAG, "🗑️ Histórico de recebimento limpo");
    }
}