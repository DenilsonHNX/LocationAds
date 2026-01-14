# 📍 LocationAds

Aplicação Android para gerenciamento de anúncios baseados em localização. Permite criar, visualizar e gerenciar anúncios que são exibidos para utilizadores em locais específicos.

---

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Funcionalidades](#-funcionalidades)
- [Arquitetura](#-arquitetura)
- [Configuração](#-configuração)
- [API Backend](#-api-backend)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Modelos de Dados](#-modelos-de-dados)
- [Serviços](#-serviços)
- [Permissões](#-permissões)
- [Build e Instalação](#-build-e-instalação)

---

## 🎯 Visão Geral

O **LocationAds** é uma aplicação Android que conecta utilizadores a anúncios relevantes com base na sua localização geográfica. Empresas e indivíduos podem criar locais e associar anúncios a esses locais, que serão exibidos para outros utilizadores quando estiverem nas proximidades.

### Tecnologias Utilizadas

| Componente | Tecnologia |
|------------|------------|
| **Plataforma** | Android (SDK 29-36) |
| **Linguagem** | Java 17 |
| **Build** | Gradle 8.13.1 (Kotlin DSL) |
| **Mapas** | Google Maps SDK 19.2.0 |
| **Networking** | Retrofit 2.9.0 + OkHttp |
| **Push Notifications** | Firebase Cloud Messaging |
| **Backend** | NestJS + Prisma + PostgreSQL |

---

## ✨ Funcionalidades

### 🔐 Autenticação
- **Registo** de novos utilizadores com nome, email e password
- **Login** com email e password
- **Gestão de sessão** com tokens JWT armazenados em SharedPreferences
- **Logout** seguro

### 📍 Gestão de Locais
- **Criar locais** baseados em:
  - Coordenadas GPS (latitude, longitude, raio)
  - Redes WiFi (lista de SSIDs)
- **Visualizar** todos os locais no mapa ou lista
- **Apagar** locais criados pelo próprio utilizador
- Os locais são visíveis para **todos** os utilizadores

### 📢 Gestão de Anúncios
- **Criar anúncios** associados a locais
- **Visualizar** anúncios em lista ou detalhes
- **Filtrar** por whitelist/blacklist
- **Apagar** anúncios criados pelo próprio utilizador
- **Partilhar** anúncios com outros

### 🔔 Notificações Push
- **Firebase Cloud Messaging (FCM)** para notificações em tempo real
- **Polling Fallback** automático quando FCM não está disponível
- Notificações de novos anúncios em locais próximos

### 📊 Rastreamento de Localização
- **Serviço em background** para tracking contínuo
- Atualização de localização a cada 30 segundos
- Notificação de foreground para Android 8.0+

### 👤 Perfil do Utilizador
- Visualizar e editar dados do perfil
- Ver estatísticas (anúncios criados, locais, etc.)

---

## 🏗 Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                        PRESENTATION                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐│
│  │MainActivity│ │LoginActivity│ │ViewAds │ │ AddAds/AddLocal ││
│  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         ADAPTERS                             │
│  ┌──────────────────┐         ┌──────────────────┐          │
│  │  AnunciosAdapter │         │   LocaisAdapter  │          │
│  └──────────────────┘         └──────────────────┘          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         NETWORK                              │
│  ┌──────────┐    ┌──────────┐    ┌──────────────────┐       │
│  │ApiClient │───▶│ApiService│───▶│ Backend (Render) │       │
│  └──────────┘    └──────────┘    └──────────────────┘       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         SERVICES                             │
│  ┌─────────────────────┐  ┌─────────────────┐  ┌──────────┐ │
│  │LocationTrackingService│  │NotificationPoller│  │FCMService│ │
│  └─────────────────────┘  └─────────────────┘  └──────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## ⚙️ Configuração

### 1. Pré-requisitos

```bash
# Android SDK
SDK 29 (mínimo)
SDK 36 (compilação/target)

# Java
JDK 17 ou superior

# Gradle
8.13.1 (incluído via wrapper)
```

### 2. Configurar Google Maps

1. Obter uma API Key no [Google Cloud Console](https://console.cloud.google.com/)
2. Ativar "Maps SDK for Android"
3. Adicionar a key em `app/src/main/res/values/google_maps_api.xml`:

```xml
<resources>
    <string name="google_maps_key">SUA_API_KEY_AQUI</string>
</resources>
```

### 3. Configurar Firebase

1. Criar projeto no [Firebase Console](https://console.firebase.google.com/)
2. Adicionar app Android com package `ao.co.isptec.aplm.locationads`
3. Baixar `google-services.json` e colocar em `app/`
4. Ativar Cloud Messaging

### 4. Variáveis de Ambiente

O backend está configurado em:
```java
// ApiClient.java
private static final String BASE_URL = "https://backend-aplm-1.onrender.com/";
```

---

## 🌐 API Backend

### Base URL
```
https://backend-aplm-1.onrender.com
```

### Endpoints Disponíveis

#### AUTH - Autenticação
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/auth/register` | Registrar novo usuário |
| POST | `/auth/login` | Fazer login e receber token JWT |
| POST | `/auth/forgot-password` | Alterar senha do usuário |
| GET | `/auth/profile` | Obter perfil do usuário autenticado |
| POST | `/auth/update-fcm-token` | Atualizar token FCM do usuário |
| POST | `/auth/usuarios/{userId}/fcm-token` | Salvar token FCM (compatível Android) |

#### HEALTH - Verificação de Saúde
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/health` | Verificar se o servidor está funcionando |

#### LOCATION - Gestão de Locais
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/locais` | Criar novo local (GPS ou WiFi) |
| GET | `/locais` | Listar todos os locais |
| GET | `/locais/{id}` | Obter local por ID |
| DELETE | `/locais/{id}` | Apagar local (apenas criador) |

#### MESSAGES - Gestão de Anúncios
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/messages` | Criar novo anúncio |
| GET | `/messages` | Listar anúncios (com filtros opcionais) |
| GET | `/messages/my-messages` | Listar anúncios do utilizador autenticado |
| GET | `/messages/all-public` | Listar TODOS os anúncios públicos |
| GET | `/messages/debug` | Debug: Ver informações sobre anúncios |
| GET | `/messages/notifications` | Listar notificações do utilizador |
| GET | `/messages/saved` | Listar anúncios salvos nos favoritos |
| GET | `/messages/whitelist` | Anúncios onde o usuário está na whitelist |
| GET | `/messages/blacklist` | Anúncios onde o usuário está na blacklist |
| GET | `/messages/similar` | Anúncios similares baseados no perfil |
| GET | `/messages/{id}` | Obter anúncio por ID |
| POST | `/messages/{id}/save` | Salvar anúncio nos favoritos |
| DELETE | `/messages/{id}/save` | Remover anúncio dos favoritos |
| DELETE | `/messages/{id}` | Apagar anúncio (apenas autor) |

#### MENSAGENS-TRANSITO - Sistema de Mulas
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/mensagens-transito/assign/{anuncioId}/{mulaId}` | Atribuir anúncio a uma mula |
| GET | `/mensagens-transito/mula` | Mensagens que a mula está transportando |
| PUT | `/mensagens-transito/deliver/{transitoId}` | Marcar mensagem como entregue |
| GET | `/mensagens-transito/local/{localId}` | Mensagens disponíveis para entrega |

#### PERFIL - Gestão de Perfil do Utilizador
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/usuarios/{userId}/perfil` | Obter perfil do usuário |
| POST | `/usuarios/{userId}/perfil` | Adicionar par chave-valor ao perfil |
| PUT | `/usuarios/{userId}/perfil` | Atualizar perfil completo |
| DELETE | `/usuarios/{userId}/perfil/{chave}` | Remover par chave-valor |

#### PERFIL PUBLIC - Chaves Públicas
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/perfil/chaves` | Listar todas as chaves públicas do sistema |

---

## 📁 Estrutura do Projeto

```
app/src/main/java/ao/co/isptec/aplm/locationads/
├── MainActivity.java              # Activity principal com mapa e listas
├── LoginActivity.java             # Tela de login
├── RegisterActivity.java          # Tela de registo
├── ViewAds.java                   # Visualizar detalhes de anúncio
├── AddAds.java                    # Criar novo anúncio
├── AddLocal.java                  # Criar novo local
├── ProfileActivity.java           # Perfil do utilizador
├── AboutActivity.java             # Sobre a aplicação
│
├── adapter/
│   ├── AnunciosAdapter.java       # Adapter para lista de anúncios
│   └── LocaisAdapter.java         # Adapter para lista de locais
│
├── network/
│   ├── interfaces/
│   │   └── ApiService.java        # Interface Retrofit com endpoints
│   ├── models/
│   │   ├── Ads.java               # Modelo de anúncio
│   │   ├── Local.java             # Modelo de local
│   │   ├── User.java              # Modelo de utilizador
│   │   ├── Notificacao.java       # Modelo de notificação
│   │   ├── LocationUpdate.java    # Modelo de atualização de localização
│   │   └── MensagemTransito.java  # Modelo de mensagem em trânsito (mulas)
│   └── singleton/
│       ├── ApiClient.java         # Cliente Retrofit singleton
│       └── ProfileManager.java    # Gestor de perfil
│
└── service/
    ├── FCMService.java            # Serviço Firebase Cloud Messaging
    ├── NotificationPoller.java    # Polling fallback para notificações
    ├── NotificationManager.java   # Gestor central de notificações
    └── LocationTrackingService.java # Serviço de rastreamento GPS
```

---

## 📊 Modelos de Dados

### User (Utilizador)
```java
{
    "id": Integer,
    "nome": String,
    "email": String,
    "password": String  // Apenas no registo
}
```

### Local
```java
{
    "id": Integer,
    "userId": Integer,      // ID do criador
    "nome": String,
    "tipo": String,         // "gps" ou "wifi"
    "latitude": Double,
    "longitude": Double,
    "raio": Integer,        // Raio em metros
    "wifiIds": List<String> // Lista de SSIDs (se tipo=wifi)
}
```

### Ads (Anúncio/Mensagem)
```java
{
    "id": Integer,
    "autorId": Integer,     // ID do criador
    "localId": Integer,     // Local associado
    "titulo": String,
    "conteudo": String,
    "dataCriacao": String,
    "dataExpiracao": String,
    "imagemUrl": String
}
```

---

## 🔧 Serviços

### LocationTrackingService
Serviço de foreground que rastreia a localização do utilizador em background.

```java
// Iniciar o serviço
Intent intent = new Intent(context, LocationTrackingService.class);
ContextCompat.startForegroundService(context, intent);

// Parar o serviço
stopService(new Intent(context, LocationTrackingService.class));
```

**Características:**
- Atualiza localização a cada 30 segundos
- Envia atualizações para o backend via `POST /locais/update-location`
- Mostra notificação persistente (required para foreground service)

### FCMService
Recebe notificações push do Firebase.

```java
// Token é registado automaticamente
// Notificações são exibidas via NotificationManager
```

### NotificationPoller
Fallback quando FCM não está disponível.

```java
// Inicia polling a cada 60 segundos
NotificationPoller.getInstance(context).startPolling();

// Para o polling
NotificationPoller.getInstance(context).stopPolling();
```

### NotificationManager
Gestor central que combina FCM + Polling.

```java
NotificationManager manager = NotificationManager.getInstance(context);
manager.initialize();  // Inicia FCM e configura fallback
manager.shutdown();    // Para todos os serviços
```

---

## 🔒 Permissões

```xml
<!-- Localização -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Rede -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

<!-- Notificações -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />

<!-- Serviços em Background -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

---

## 🔨 Build e Instalação

### Compilar o Projeto

```bash
# Compilar apenas
./gradlew compileDebugJavaWithJavac

# Compilar e verificar
./gradlew assembleDebug
```

### Gerar APK

```bash
# APK Debug
./gradlew assembleDebug
# Resultado: app/build/outputs/apk/debug/app-debug.apk

# APK Release (requer keystore configurada)
./gradlew assembleRelease
```

### Instalar no Dispositivo

```bash
# Via ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Ou via Gradle
./gradlew installDebug
```

---

## 🧪 Testes

### Executar Testes Unitários

```bash
./gradlew test
```

### Executar Testes Instrumentados

```bash
./gradlew connectedAndroidTest
```

---

## 🚀 Funcionalidades Futuras (Sistema Mulas)

O backend já suporta um sistema descentralizado de entrega de mensagens ("mulas") para áreas sem conectividade:

### Endpoints Disponíveis
```
POST /mensagens-transito          # Criar mensagem em trânsito
GET  /mensagens-transito/destino  # Mensagens para entregar
POST /mensagens-transito/{id}/entregar  # Confirmar entrega
```

### Como Funciona
1. Utilizador A cria mensagem para local sem cobertura
2. Mensagem fica em "trânsito"
3. Utilizador B (mula) passa pelo local de origem
4. Mula carrega mensagem
5. Mula passa pelo local de destino
6. Mensagem é entregue

---

## 📝 Changelog

### v1.0.0 (Janeiro 2026)
- ✅ Autenticação (login, registo, logout)
- ✅ CRUD de Locais (GPS e WiFi)
- ✅ CRUD de Anúncios
- ✅ Visualização em mapa e lista
- ✅ Notificações Push (FCM + Polling)
- ✅ Rastreamento de localização em background
- ✅ Apagar anúncios/locais (apenas criador)
- ✅ Visualizar locais de todos os utilizadores

---

## 👥 Equipa

Desenvolvido para a disciplina de **APLM** no **ISPTEC**.

---

## 📄 Licença

Este projeto é para fins educacionais.
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
