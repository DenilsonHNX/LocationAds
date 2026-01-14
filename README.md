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

#### Autenticação
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/auth/register` | Registar novo utilizador |
| POST | `/auth/login` | Login |
| GET | `/auth/me` | Obter dados do utilizador autenticado |

#### Mensagens (Anúncios)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/messages` | Listar todas as mensagens |
| POST | `/messages` | Criar nova mensagem |
| GET | `/messages/{id}` | Obter mensagem por ID |
| DELETE | `/messages/{id}` | Apagar mensagem |
| GET | `/messages/whitelist` | Mensagens na whitelist |
| GET | `/messages/blacklist` | Mensagens na blacklist |

#### Locais
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/locais` | Listar todos os locais |
| POST | `/locais` | Criar novo local |
| GET | `/locais/{id}` | Obter local por ID |
| DELETE | `/locais/{id}` | Apagar local |
| GET | `/locais/user` | Locais do utilizador |

#### Perfil
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/perfil` | Obter perfil |
| PUT | `/perfil` | Atualizar perfil |

#### Notificações
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/messages/notifications` | Listar notificações |
| POST | `/messages/notifications/register` | Registar token FCM |

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
