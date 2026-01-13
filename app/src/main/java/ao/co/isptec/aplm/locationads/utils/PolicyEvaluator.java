package ao.co.isptec.aplm.locationads.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.models.UserProfile;

/**
 * Avaliador de políticas de mensagens
 *
 * Implementa a lógica conforme especificação:
 * 1. Verifica se usuário está no local correto
 * 2. Verifica se está dentro da janela de tempo
 * 3. Avalia restrições de perfil (whitelist/blacklist)
 */
public class PolicyEvaluator {

    private static final String TAG = "PolicyEvaluator";

    /**
     * Verifica se usuário pode receber a mensagem
     *
     * @param ad Anúncio/Mensagem
     * @param userProfile Perfil do usuário
     * @param currentLocalId Local atual do usuário
     * @param skipLocationCheck Se true, ignora verificação de local
     * @return true se pode receber, false caso contrário
     */
    public static boolean canReceiveMessage(Ads ad, UserProfile userProfile, int currentLocalId, boolean skipLocationCheck) {
        Log.d(TAG, "═══════════════════════════════════");
        Log.d(TAG, "🔍 Avaliando mensagem: " + ad.getTitulo());

        // 1. Verificar se usuário está no local correto (se não skip)
        if (!skipLocationCheck && ad.getLocalId() != currentLocalId) {
            Log.d(TAG, "   ❌ Local incorreto (msg:" + ad.getLocalId() + " != user:" + currentLocalId + ")");
            return false;
        }
        Log.d(TAG, "   ✅ Local correto: " + currentLocalId + (skipLocationCheck ? " (ignorando)" : ""));

        // 2. Verificar janela de tempo
        if (!isWithinTimeWindow(ad.getHoraInicio(), ad.getHoraFim())) {
            Log.d(TAG, "   ❌ Fora da janela de tempo");
            return false;
        }
        Log.d(TAG, "   ✅ Dentro da janela de tempo");

        // 3. Avaliar política de restrições
        String policy = ad.getPolicy();
        Map<String, Object> restricoes = ad.getRestricoes();

        if (policy == null) {
            Log.d(TAG, "   ⚠️ Sem política definida - permitindo");
            return true;
        }

        boolean matchesRestrictions = matchesProfile(userProfile, restricoes);

        if ("WHITELIST".equalsIgnoreCase(policy)) {
            // WHITELIST: Só recebe se corresponder às restrições
            // Se não há restrições, todos recebem
            if (restricoes == null || restricoes.isEmpty()) {
                Log.d(TAG, "   ✅ WHITELIST sem restrições - todos recebem");
                return true;
            }

            if (matchesRestrictions) {
                Log.d(TAG, "   ✅ WHITELIST - usuário corresponde às restrições");
                return true;
            } else {
                Log.d(TAG, "   ❌ WHITELIST - usuário NÃO corresponde às restrições");
                return false;
            }

        } else if ("BLACKLIST".equalsIgnoreCase(policy)) {
            // BLACKLIST: Todos recebem EXCETO quem corresponde
            if (matchesRestrictions) {
                Log.d(TAG, "   ❌ BLACKLIST - usuário está bloqueado");
                return false;
            } else {
                Log.d(TAG, "   ✅ BLACKLIST - usuário não está bloqueado");
                return true;
            }
        }

        // Se não tem política ou é desconhecida, permitir
        Log.d(TAG, "   ⚠️ Política desconhecida: " + policy + " - permitindo");
        return true;
    }

    /**
     * Verifica se a mensagem está dentro da janela de tempo
     */
    private static boolean isWithinTimeWindow(String horaInicio, String horaFim) {
        if (horaInicio == null || horaFim == null) {
            Log.d(TAG, "      ⚠️ Sem janela de tempo definida");
            return true; // Sem restrição de tempo
        }

        try {
            Date now = new Date();

            // Tentar formato com milissegundos
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            Date inicio = sdf.parse(horaInicio);
            Date fim = sdf.parse(horaFim);

            if (inicio == null || fim == null) {
                return true;
            }

            boolean isWithin = now.after(inicio) && now.before(fim);

            if (isWithin) {
                Log.d(TAG, "      ✅ Dentro da janela: " + formatDate(inicio) + " a " + formatDate(fim));
            } else {
                Log.d(TAG, "      ❌ Fora da janela: " + formatDate(inicio) + " a " + formatDate(fim));
                Log.d(TAG, "         Agora: " + formatDate(now));
            }

            return isWithin;

        } catch (ParseException e) {
            // Tentar formato sem milissegundos
            try {
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date inicio = sdf2.parse(horaInicio);
                Date fim = sdf2.parse(horaFim);
                Date now = new Date();

                if (inicio == null || fim == null) {
                    return true;
                }

                return now.after(inicio) && now.before(fim);

            } catch (ParseException e2) {
                Log.e(TAG, "      ❌ Erro ao parsear datas: " + e2.getMessage());
                return true; // Em caso de erro, permitir
            }
        }
    }

    /**
     * Verifica se o perfil do usuário corresponde às restrições
     *
     * Exemplo de restrições:
     * - {"Profissao": "Estudante"} → Só estudantes
     * - {"Idade": 18} → Apenas maiores de 18
     * - {"Profissao": "Estudante", "Idade": 18} → Estudantes maiores de 18
     */
    private static boolean matchesProfile(UserProfile userProfile, Map<String, Object> restricoes) {
        if (userProfile == null) {
            Log.d(TAG, "      ⚠️ Perfil do usuário é null");
            return false;
        }

        if (restricoes == null || restricoes.isEmpty()) {
            Log.d(TAG, "      ℹ️ Sem restrições de perfil");
            return true; // Sem restrições = todos correspondem
        }

        Map<String, Object> userProfileData = userProfile.getProfile();

        if (userProfileData == null || userProfileData.isEmpty()) {
            Log.d(TAG, "      ⚠️ Dados de perfil vazios");
            return false;
        }

        Log.d(TAG, "      🔍 Verificando restrições:");

        // Todas as restrições devem ser satisfeitas
        for (Map.Entry<String, Object> restricao : restricoes.entrySet()) {
            String key = restricao.getKey();
            Object requiredValue = restricao.getValue();

            Object userValue = userProfileData.get(key);

            Log.d(TAG, "         • " + key + ": requerido=" + requiredValue + ", user=" + userValue);

            if (userValue == null) {
                Log.d(TAG, "           ❌ Usuário não tem esta propriedade");
                return false;
            }

            // Comparar valores
            if (!valuesMatch(requiredValue, userValue)) {
                Log.d(TAG, "           ❌ Valores não correspondem");
                return false;
            }

            Log.d(TAG, "           ✅ Corresponde");
        }

        Log.d(TAG, "      ✅ Todas as restrições satisfeitas");
        return true;
    }

    /**
     * Compara dois valores com suporte a diferentes tipos
     */
    private static boolean valuesMatch(Object required, Object user) {
        if (required == null || user == null) {
            return false;
        }

        // Conversão para strings para comparação
        String reqStr = required.toString().trim();
        String userStr = user.toString().trim();

        // Comparação case-insensitive
        if (reqStr.equalsIgnoreCase(userStr)) {
            return true;
        }

        // Tentar comparação numérica
        try {
            double reqNum = Double.parseDouble(reqStr);
            double userNum = Double.parseDouble(userStr);

            // Suporte para operadores (>= <= > < ==)
            // Para simplificar, apenas comparação direta por enquanto
            return Math.abs(reqNum - userNum) < 0.001;

        } catch (NumberFormatException e) {
            // Não é número, manter comparação de string
        }

        return false;
    }

    /**
     * Formata data para log
     */
    private static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Valida se a política está bem formada
     */
    public static boolean isValidPolicy(Ads ad) {
        if (ad == null) return false;

        String policy = ad.getPolicy();
        if (policy == null) return true; // Sem política é válido

        // Deve ser WHITELIST ou BLACKLIST
        return "WHITELIST".equalsIgnoreCase(policy) || "BLACKLIST".equalsIgnoreCase(policy);
    }
}