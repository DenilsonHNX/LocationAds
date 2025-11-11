package ao.co.isptec.aplm.locationads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdMessage {
    private String id;
    private String conteudo;
    private String local;
    private Map<String, String> whitelist;  // Política de chave-valor permitida
    private Map<String, String> blacklist;  // Política de chave-valor bloqueada
    private String autor;
    private long dataCriacao; // timestamp UNIX
    private long validoAte;   // término da validade da mensagem

    public AdMessage() {
        whitelist = new HashMap<>();
        blacklist = new HashMap<>();
    }

    public AdMessage(String id, String conteudo, String local,
                     Map<String, String> whitelist,
                     Map<String, String> blacklist,
                     String autor, long dataCriacao, long validoAte) {
        this.id = id;
        this.conteudo = conteudo;
        this.local = local;
        this.whitelist = (whitelist != null) ? whitelist : new HashMap<>();
        this.blacklist = (blacklist != null) ? blacklist : new HashMap<>();
        this.autor = autor;
        this.dataCriacao = dataCriacao;
        this.validoAte = validoAte;
    }

    // Getters e setters omitidos para brevidade...
    public String getId() { return id; }
    public String getConteudo() { return conteudo; }
    public String getLocal() { return local; }
    public Map<String, String> getWhitelist() { return whitelist; }
    public Map<String, String> getBlacklist() { return blacklist; }
    public String getAutor() { return autor; }
    public long getDataCriacao() { return dataCriacao; }
    public long getValidoAte() { return validoAte; }

    public boolean isValidaAgora() {
        long agora = System.currentTimeMillis();
        return agora >= dataCriacao && agora <= validoAte;
    }

    // Método estático para carregar mensagens de exemplo
    public static List<AdMessage> carregarMensagens() {
        List<AdMessage> lista = new ArrayList<>();

        Map<String, String> wl1 = new HashMap<>();
        wl1.put("profissao", "Estudante");
        AdMessage m1 = new AdMessage(
                "1",
                "Mensagem exclusiva para estudantes!",
                "Largo da Independência",
                wl1,
                null,
                "João",
                System.currentTimeMillis() - 1000 * 60 * 60,
                System.currentTimeMillis() + 1000 * 60 * 60 * 24
        );
        lista.add(m1);

        Map<String, String> bl2 = new HashMap<>();
        bl2.put("profissao", "Trabalhador");
        AdMessage m2 = new AdMessage(
                "2",
                "Mensagem para todos exceto trabalhadores!",
                "Largo da Independência",
                null,
                bl2,
                "Maria",
                System.currentTimeMillis() - 1000 * 60 * 60,
                System.currentTimeMillis() + 1000 * 60 * 60 * 24
        );
        lista.add(m2);

        AdMessage m3 = new AdMessage(
                "3",
                "Mensagem para todos!",
                "Largo da Independência",
                null,
                null,
                "Admin",
                System.currentTimeMillis() - 1000 * 60 * 60,
                System.currentTimeMillis() + 1000 * 60 * 60 * 24
        );
        lista.add(m3);

        return lista;
    }
}
