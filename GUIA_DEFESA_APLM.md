# 🎓 Guia de Preparação para Defesa - APLM (Android)

Este documento foi criado para ajudar na defesa do projeto **LocationAds**. Ele cobre os conceitos teóricos exigidos na cadeira de Aplicações Móveis, explica a arquitetura do projeto e antecipa perguntas que professores rígidos costumam fazer.

---

## 🏗️ 1. Arquitetura e Estrutura do Projeto

O projeto segue um padrão modular com separação de responsabilidades:

- **Activities**: Responsáveis pela UI e interação com o utilizador (`MainActivity`, `LoginActivity`).
- **Services**: Componentes que correm em background sem interface (`LocationTrackingService`, `FCMService`).
- **Adapters**: Pontes entre as fontes de dados e as Views (`AnunciosAdapter`, `LocaisAdapter`).
- **Network**: Camada de persistência remota usando o padrão **Singleton** (`ApiClient`) e **Interface** (`ApiService`).
- **Models**: POJOs (Plain Old Java Objects) para mapeamento de dados JSON (`Ads`, `Local`).

### Onde está cada coisa?
- **UI (Layouts)**: `app/src/main/res/layout/`
- **Lógica de Rede**: `ao.co.isptec.aplm.locationads.network`
- **Background**: `ao.co.isptec.aplm.locationads.service`
- **Permissões**: `AndroidManifest.xml`

---

## 🚀 2. Conceitos Técnicos Fundamentais

### A. API Levels (minSdk vs targetSdk)
- **minSdk = 29 (Android 10)**: Significa que a aplicação só corre em dispositivos com Android 10 ou superior. Escolhemos 29 porque é onde as permissões de localização em background tornaram-se mais rigorosas.
- **targetSdk = 36**: A aplicação está optimizada para as versões mais recentes do Android, garantindo segurança e performance de ponta.

### B. Networking: Retrofit vs AsyncTask
- **AsyncTask (Deprecated)**: Não usamos `AsyncTask` porque é obsoleta e causa fugas de memória (memory leaks) se a Activity for destruída durante a execução.
- **Retrofit**: Usamos Retrofit 2.9.0. É uma biblioteca Type-Safe que abstrai o HTTP.
- **Assincronismo**: As chamadas são feitas via `.enqueue()`, que executa a requisição numa **Worker Thread** e devolve o resultado na **Main Thread** (UI Thread) para evitar que a aplicação "congele" (ANR - Application Not Responding).

### C. Background & Foreground Services
- **LocationTrackingService**: É um **Foreground Service**.
  - **Porquê via Foreground?** Desde o Android 8.0, serviços em background podem ser mortos pelo sistema para poupar bateria. Um Foreground Service mostra uma notificação persistente, indicando ao utilizador que a app está ativa e ao sistema que não deve terminá-la.
  - **FusedLocationProvider**: Usamos esta API da Google em vez do nativo `LocationManager` porque é mais eficiente, combinando GPS, Wi-Fi e sensores.

### D. Notificações (Push vs Polling)
Implementamos uma estratégia robusta:
1. **Push (FCM)**: O servidor envia uma mensagem quando há algo novo. É eficiente em termos de bateria.
2. **Polling (NotificationPoller)**: Como fallback, a app verifica manualmente a cada 60s se há novidades, caso o serviço FCM do dispositivo falhe ou não esteja disponível.

### E. Adaptabilidade (Layouts)
- Usamos **ConstraintLayout** na maioria dos layouts. Isso permite que a interface seja fluída e se ajuste a diferentes tamanhos de ecrã sem aninhamento excessivo de Views (o que melhora a performance de renderização).
- **RecyclerView**: Usa o padrão **ViewHolder**. Em vez de criar centenas de Views para uma lista, ele reaproveita as Views que saem do ecrã para mostrar novos dados.

### F. Persistência de Dados (SharedPreferences)
- Nem tudo precisa de uma Base de Dados SQLite. Para dados simples como o **Token JWT** e o **ID do utilizador**, usamos `SharedPreferences`.
- É um sistema de armazenamento chave-valor persistente no ficheiro XML privado da aplicação.
- *Local de uso*: `ProfileManager.java` e `LoginActivity.java`.

---

## 💻 3. Trechos de Código para Explicar

### Chamada à API (Retrofit)
```java
// Local: ApiService.java e MainActivity.java
apiService.login(request).enqueue(new Callback<LoginResponse>() {
    @Override
    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        if (response.isSuccessful()) {
            // Sucesso! Atualizar UI
        }
    }
    @Override
    public void onFailure(Call<LoginResponse> call, Throwable t) {
        // Erro de rede
    }
});
```
*Explicação*: O `.enqueue()` cria uma thread separada. O código dentro de `onResponse` volta automaticamente para a thread principal do Android, permitindo atualizar a interface com segurança.

### Iniciar Serviço em Background
```java
// Local: LocationTrackingService.java -> onStartCommand
Notification notification = createNotification();
startForeground(NOTIFICATION_ID, notification);
```
*Explicação*: Este comando transforma o serviço num **Foreground Service**. Sem isto, o sistema mataria o rastreamento de GPS após alguns minutos de ecrã desligado.

---

## ❓ 4. Questões Prováveis na Defesa (Q&A)

**P: Por que é que a tua aplicación usa permissões 'Dangerous'?**
*R:* O projeto baseia-se em localização (`ACCESS_FINE_LOCATION`). Para proteger a privacidade do utilizador, estas permissões devem ser pedidas em **Runtime** (tempo de execução) e não apenas declaradas no Manifest.

**P: O que acontece se o utilizador trocar de ecrã enquanto os dados estão a ser carregados da internet?**
*R:* Usamos o Retrofit com callbacks. No entanto, em produção, deveríamos tratar o cancelamento (Call.cancel()) no `onDestroy` para evitar fugas de memória. (Neste projeto, o Singleton ApiClient ajuda a centralizar a gestão).

**P: Como garantiste que a lista de anúncios não consome toda a memória do telemóvel?**
*R:* Através do **RecyclerView**. Ele não carrega todos os anúncios na memória de uma vez; ele apenas infla as Views visíveis e recicla-as conforme o scroll é feito.

**P: O que é o Android Manifest?**
*R:* É o ficheiro de configuração principal. Ele declara todos os componentes da aplicação (Activities, Services, Receivers), permissões exigidas e a Activity que inicia primeiro (`Intent Intent.ACTION_MAIN`).

**P: Como é feita a comunicação entre o seu Serviço e o Servidor?**
*R:* O Serviço (`LocationTrackingService`) captura a latitude/longitude a cada 30 segundos e envia um `POST` para o endpoint `/messages/update-location`. O servidor então decide se deve enviar uma notificação baseada nessa nova posição.

**P: Por que usar o padrão Singleton para o ApiClient?**
*R:* Para garantir que temos apenas **uma instância** do cliente HTTP (OkHttp) e do Retrofit em toda a aplicação. Isso economiza recursos de memória e evita abrir múltiplas conexões de rede desnecessárias.

---

## 🎨 5. Dicas Extras para a Defesa
- **Paciência**: Se o professor perguntar por que não usaste Kotlin, diz que o foco foi dominar a base sólida do Android em Java para entender melhor o funcionamento do SDK.
- **Debug**: Se algo falhar na demonstração, abre o **Logcat** no Android Studio. Professores valorizam quem sabe ler logs de erro.
- **Contexto**: Lembra-te que `Activity` é um `Context`, mas um `Service` também é. O Context é a interface global para informações do ambiente da aplicação.

---

### Links e Caminhos Úteis:
- **Lógica de Localização**: `LocationTrackingService.java`
- **Configuração de Rede**: `ApiService.java`
- **Gestão de Sessão**: `SharedPreferences` (usado no `ProfileManager.java`)
