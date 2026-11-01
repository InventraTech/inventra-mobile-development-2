# Inventra Mobile

Aplicativo Android em Kotlin + Jetpack Compose para gestão de estoque, requisições, fornecedores e alertas em cozinhas industriais. Consome a [Inventra API](https://github.com/InventraTech/inventra-api-2).

- Login por e-mail e senha ou com Google, com renovação automática do token e volta ao login quando a sessão expira
- Escolha da cozinha num mapa, criação de cozinha e pedido de entrada, com aprovação por um supervisor
- Estoque por cozinha com lotes, validade, entrada, baixa e edição de produto
- Leitura de código de barras pela câmera (CameraX + ML Kit), com consulta ao catálogo e aviso de produto não cadastrado
- Requisições de compra, transferência e consumo, com histórico, detalhe, edição de itens, envio e aprovação
- Fornecedores, alertas (estoque baixo e validade) e notificações no celular mesmo com o app fechado
- Navegação e telas inicial por papel: `SUPERVISOR`, `ESTOQUISTA` e `COMPRADOR`
- Tema claro, escuro ou do sistema, e design system próprio em atomic design

## Sumário

- [Stack](#stack)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Rodando](#rodando)
- [Funcionalidades por papel](#funcionalidades-por-papel)
- [API consumida](#api-consumida)
- [Arquitetura](#arquitetura)
- [Estrutura](#estrutura)
- [Qualidade e testes](#qualidade-e-testes)
- [CI/CD](#cicd)
- [Fluxo de contribuição](#fluxo-de-contribuição)
- [Limitações conhecidas](#limitações-conhecidas)
- [Time](#time)

## Stack

| Tecnologia                    | Versão               |
|:------------------------------|:---------------------|
| Kotlin                        | 2.2.10               |
| Android Gradle Plugin         | 9.3.1                |
| JDK (build)                   | 21                   |
| `minSdk` / `targetSdk`        | 30 / 36 (`compileSdk` 37.1) |
| Jetpack Compose (BOM)         | 2026.02.01           |
| Material 3                    | via BOM              |
| Navigation 3                  | 1.1.7                |
| Lifecycle / ViewModel         | 2.11.0               |
| Retrofit                      | 3.0.0                |
| OkHttp                        | 5.1.0                |
| kotlinx.serialization         | 1.9.0                |
| DataStore Preferences         | 1.1.7                |
| Coil                          | 3.4.0                |
| WorkManager                   | 2.12.0               |
| CameraX                       | 1.7.0-alpha02        |
| ML Kit Barcode Scanning       | 17.3.0               |
| Maps Compose / Play Services Location | 8.3.0 / 21.3.0 |
| Firebase Auth + Credentials Manager + Google ID | 24.2.0 / 1.6.0 / 1.2.0 |
| ktlint / detekt / Kover       | 14.2.0 / 1.23.8 / 0.9.11 |

As versões ficam em [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Pré-requisitos

- **Android Studio** recente (o projeto usa AGP 9) ou apenas a linha de comando com o Android SDK instalado.
- **JDK 21**. O `gradle-daemon-jvm.properties` pede essa versão, e o Gradle baixa o toolchain sozinho quando não encontra.
- **Dispositivo ou emulador com Android 11 (API 30) ou superior**. Para testar a câmera e o mapa, prefira um aparelho real ou um emulador com Google Play Services.
- **Chave do Google Maps** (Maps SDK for Android) para o mapa de escolha da cozinha.
- Acesso a uma instância da [Inventra API](https://github.com/InventraTech/inventra-api-2), a de produção ou uma local.

## Configuração

Os valores abaixo ficam **fora do Git**. O `app/build.gradle.kts` lê cada um do `local.properties` (na raiz do projeto) ou, se não estiver lá, da variável de ambiente de mesmo nome.

| Chave          | Obrigatória | Para quê                                                                 | Padrão |
|:---------------|:------------|:-------------------------------------------------------------------------|:-------|
| `MAPS_API_KEY` | Para o mapa | Vira o `manifestPlaceholder` da chave do Google Maps                     | vazio (o mapa não carrega) |
| `API_BASE_URL` | Não         | URL base da API, **terminada em `/`** e incluindo `/api/`                | `https://ms-inventra-api.onrender.com/api/` |

**`local.properties` de exemplo:**

```properties
MAPS_API_KEY=sua_chave_do_google_maps
API_BASE_URL=http://10.0.2.2:8080/api/
```

> `10.0.2.2` é o `localhost` da máquina vista de dentro do emulador Android. Com `API_BASE_URL` começando em `http://`, o build habilita tráfego em texto puro (`usesCleartextTraffic`) automaticamente. Com `https://` ele fica desligado.

Notas sobre o Firebase e o login com Google:

- O [`app/google-services.json`](app/google-services.json) do projeto Firebase está versionado. O plugin `google-services` gera a partir dele o `default_web_client_id` usado pelo `GoogleAuthClient`.
- O login com Google só funciona se a API tiver a chave de conta de serviço do Firebase configurada. Sem ela, `POST /api/auth/google` responde `503` e o app continua funcionando com e-mail e senha.
- Em builds assinadas com outro keystore, cadastre o SHA-1 do certificado no console do Firebase.

## Rodando

**Android Studio:** abra a pasta do projeto, aguarde a sincronização do Gradle, escolha o dispositivo e execute o módulo `app`.

**Linha de comando (Windows PowerShell):**

```powershell
.\gradlew.bat installDebug
```

**Linha de comando (Linux / macOS):**

```bash
./gradlew installDebug
```

Isso compila e instala o APK debug no dispositivo conectado. Para só gerar o APK, em `app/build/outputs/apk/debug/`:

```bash
./gradlew assembleDebug
```

Na primeira execução o Android pede as permissões de **câmera** (leitura de código de barras), **localização** (cozinhas próximas no mapa) e **notificações** (Android 13+).

## Funcionalidades por papel

A barra inferior e a tela inicial mudam conforme o `accessType` do usuário (`bottomDestinationsFor` e `homeScreenFor`):

| Papel         | Abas                                          | Tela inicial   |
|:--------------|:----------------------------------------------|:---------------|
| `SUPERVISOR`  | Início, Estoque, Fornecedores, Requisições, Perfil | Início    |
| `ESTOQUISTA`  | Início, Estoque, Fornecedores, Perfil         | Início         |
| `COMPRADOR`   | Fornecedores, Requisições, Perfil             | Requisições    |

| Área              | O que o app faz                                                                                          |
|:------------------|:---------------------------------------------------------------------------------------------------------|
| Autenticação      | Login, cadastro e login com Google. Token renovado pelo `TokenAuthenticator`; sessão expirada leva ao login |
| Cozinha           | Descobrir cozinhas no mapa, criar a própria, pedir entrada, acompanhar o pedido e ver os dados da cozinha |
| Início            | Resumo da cozinha e alertas prioritários                                                                 |
| Estoque           | Lista com filtros, detalhe com lotes, cadastro e edição de produto, entrada de estoque e baixa            |
| Leitura           | Câmera lê o código de barras, busca o produto e abre a entrada ou a baixa. Produto desconhecido gera aviso ao supervisor |
| Fornecedores      | Lista com filtros e detalhe com os produtos de cada fornecedor                                           |
| Requisições       | Lista por status, histórico, detalhe, editor de itens, envio para análise, aprovação, rejeição e cancelamento |
| Notificações      | Tela de alertas agrupados, marcar como lido e verificação em segundo plano a cada 15 minutos (WorkManager) |
| Perfil            | Dados da conta, foto, gestão de membros (supervisor aprova, recusa e desativa) e preferências            |
| Preferências      | Tema claro, escuro ou do sistema, e liga ou desliga os avisos de alerta no celular                       |

## API consumida

O app fala com a API REST por Retrofit + kotlinx.serialization. As interfaces ficam em [`data/remote/api/`](app/src/main/java/com/inventraoficial/inventra/data/remote/api/) e a URL base vem de `API_BASE_URL`.

| Interface             | Base path                    | Uso no app                                                      |
|:----------------------|:-----------------------------|:----------------------------------------------------------------|
| `AuthApi`             | `auth`                       | Login, cadastro, Google, refresh e logout                       |
| `UserApi`             | `users`                      | Usuário logado, foto e desativação de membro                    |
| `KitchenApi`          | `kitchens`                   | Descobrir, detalhar, criar e editar cozinha                     |
| `KitchenAccessApi`    | `kitchen-access-requests`    | Pedir entrada, listar os próprios pedidos, aprovar e rejeitar   |
| `ProductApi`          | `products`, `units`, `categories` | Produto, consulta por código de barras, unidades e categorias |
| `StockApi`            | `stock-batches`, `stock-movements`, `kitchens/{id}/stock` | Estoque da cozinha, baixa, vencendo e estoque baixo, movimentações |
| `SupplierApi`         | `suppliers`                  | Lista e detalhe de fornecedores                                 |
| `RequisitionApi`      | `requisitions`               | Requisições, itens e transições de status                       |
| `AlertApi`            | `alerts`                     | Alertas, marcar como lido e produto não cadastrado              |

Todas as chamadas, menos as de autenticação, levam o header `Authorization: Bearer <token>` colocado pelo `AuthInterceptor`. Quando a API responde `401`, o `TokenAuthenticator` tenta renovar o token uma vez; se não conseguir, a sessão é encerrada e o usuário volta ao login. Erros da API (`application/problem+json`) viram mensagens de tela por meio do `ApiCall`.

A referência dos endpoints, com payloads e regras de autorização, está na [documentação da API](https://github.com/InventraTech/inventra-api-2/blob/main/docs/docs.md).

## Arquitetura

Arquitetura em camadas, com fluxo de dados unidirecional:

```
Composable (Screen)  →  ViewModel (StateFlow<UiState>)  →  Repository (interface)  →  Retrofit API / DataStore
        ▲                                                          │
        └──────────────────── UiState imutável ◄───────────────────┘
```

- **UI:** Jetpack Compose. Cada tela tem `Screen`, `UiState` e `ViewModel`; o estado vem como `StateFlow` e os eventos voltam ao ViewModel por funções.
- **Repositórios:** cada um é uma interface com uma implementação `Network*Repository`. Nos testes entram `Fake*Repository`, sem rede.
- **Injeção de dependência manual:** o `AppContainer` (criado na `InventraApplication`) monta OkHttp, Retrofit, repositórios, sessão e verificadores de segundo plano, sem Hilt ou Dagger.
- **Sessão:** o `SessionManager` guarda tokens e usuário no DataStore e avisa a navegação quando a sessão expira.
- **Navegação:** Navigation 3 com um `Navigator` próprio, que mantém a pilha de telas ao girar o aparelho (salva por `NavigatorSaver`) e escolhe abas e tela inicial pelo papel.
- **Segundo plano:** o `AlertCheckWorker` consulta pedidos de entrada e alertas novos a cada 15 minutos (o mínimo do Android, que pode atrasar em economia de bateria) e dispara a notificação do sistema.
- **Design system:** componentes em `core/designsystem`, separados em `atoms`, `molecules` e `organisms`, sobre o tema em `ui/theme`.

## Estrutura

```
app/src/main/java/com/inventraoficial/inventra/
├── InventraApplication.kt      # cria o AppContainer e agenda o worker
├── MainActivity.kt
├── core/
│   └── designsystem/           # atoms, molecules, organisms (componentes Inventra*)
├── data/
│   ├── local/                  # DataStore (sessão e preferências), localização, compressão de imagem
│   ├── remote/
│   │   ├── api/                # interfaces Retrofit
│   │   ├── dto/                # modelos de request/response por domínio
│   │   ├── auth/google/        # GoogleAuthClient (Credentials Manager + Firebase)
│   │   └── interceptor/        # AuthInterceptor e TokenAuthenticator
│   ├── repository/             # interfaces e implementações Network*
│   └── session/                # SessionManager
├── di/                         # AppContainer
├── feature/
│   ├── assistant/              # chat (apenas interface por enquanto)
│   ├── auth/                   # login, cadastro, Google
│   ├── cozinha/                # selecionar, criar, pedido enviado, informações
│   ├── home/
│   ├── notifications/          # tela e verificação em segundo plano
│   ├── profile/                # dados, foto, membros, preferências
│   ├── requirements/           # requisições: lista, histórico, detalhe, editor
│   ├── scan/                   # leitura de código de barras
│   ├── stock/                  # lista, detalhe, edição, cadastro, entrada, baixa
│   └── suppliers/              # lista e detalhe
└── ui/
    ├── navigation/             # Navigator, destinos por papel, SetupNavigation
    └── theme/                  # cores, tipografia e tema

app/src/test/                   # testes unitários (JVM)
app/config/detekt/detekt.yml    # regras do detekt
```

## Qualidade e testes

O projeto usa **ktlint** (estilo), **detekt** (análise estática, com `detekt-baseline.xml`), **Android Lint** e **Kover** (cobertura). Os testes ficam em `app/src/test` e rodam na JVM, sem emulador:

- ViewModels de cada fluxo, com `Fake*Repository` e `kotlinx-coroutines-test`
- Repositórios de rede contra um `MockWebServer`
- `AuthInterceptor`, `TokenAuthenticator`, `ApiCall` e `SessionManager`
- `Navigator`, `NavigatorSaver` e as abas por papel
- Verificadores de segundo plano (`NewAlertsChecker`, `AccessRequestChecker`)

O mesmo conjunto que a CI executa:

```bash
./gradlew --continue ktlintCheck detekt lintDebug test koverXmlReport assembleDebug
```

Para corrigir o estilo automaticamente:

```bash
./gradlew ktlintFormat
```

O relatório de cobertura (Kover) exclui funções `@Composable`, `@Preview`, `BuildConfig` e as classes geradas do Compose. Os relatórios ficam em `app/build/reports/`.

## CI/CD

Os workflows deste repositório só chamam os **workflows reutilizáveis** da organização, mantidos em [`InventraTech/.github`](https://github.com/InventraTech/.github/tree/main/.github/workflows).

| Workflow                                                   | Quando roda                     | O que faz |
|:-----------------------------------------------------------|:--------------------------------|:----------|
| [`ci.yaml`](.github/workflows/ci.yaml) → `ci-android.yaml` | PR e push na `main`             | ktlint, detekt, Android Lint, testes, cobertura (Kover) e APK debug. Em PR, o APK debug e os relatórios ficam anexados à execução (7 e 14 dias) |
| [`cd.yaml`](.github/workflows/cd.yaml) → `cd-android.yaml` | Tag `vX.Y.Z`                    | Valida a tag, roda os testes, gera o APK de release, assina com o `apksigner` e publica uma GitHub Release com o APK e o `SHA256SUMS.txt` |

### Como publicar uma versão

1. Faça merge na `main` por PR. O CD recusa tags que apontam para fora da `main`.
2. Crie e envie a tag:

```bash
git tag v1.2.0
git push origin v1.2.0
```

3. O workflow calcula a versão da tag: `versionName = 1.2.0` e `versionCode = X*10000 + Y*100 + Z` (aqui, `10200`). Não é preciso editar o `build.gradle.kts` à mão.
4. O APK assinado aparece na página de **Releases**, com as notas geradas automaticamente.

### Secrets necessários

Cadastre em *Settings → Secrets and variables → Actions*:

| Secret                       | Para quê                                             |
|:-----------------------------|:-----------------------------------------------------|
| `ANDROID_KEYSTORE_BASE64`    | Keystore de release em base64                        |
| `ANDROID_KEYSTORE_PASSWORD`  | Senha do keystore                                    |
| `ANDROID_KEY_ALIAS`          | Alias da chave                                       |
| `ANDROID_KEY_PASSWORD`       | Senha da chave                                       |
| `MAPS_API_KEY`               | Chave do Google Maps embutida no APK                 |

Para gerar o valor do keystore em base64:

```bash
base64 -w0 release.keystore
```

Sem os secrets de assinatura o CD falha de propósito, em vez de publicar um APK sem assinatura. O APK de release usa a `API_BASE_URL` padrão do `build.gradle.kts`, porque o `cd.yaml` não passa o parâmetro `api-base-url`.

## Fluxo de contribuição

- Trabalhe numa branch a partir da `main` e abra uma **Pull Request**. A `main` é protegida por regra da organização (PR com aprovação, histórico linear e merge por squash).
- A PR usa o [template da organização](https://github.com/InventraTech/.github/blob/main/PULL_REQUEST_TEMPLATE.md): descrição, tipo de mudança, como testar, checklist e evidências.
- Mensagens de commit seguem o padrão de *Conventional Commits* já usado no histórico, por exemplo `feat(baixa): confirmação antes de dar baixa` e `fix(campos): texto dos campos sem cortar`.
- Antes de abrir a PR, rode localmente `ktlintCheck`, `detekt` e `test` (veja [Qualidade e testes](#qualidade-e-testes)). A CI roda os mesmos passos na PR; só faça o merge com a CI verde.
- Nunca versione chaves: `local.properties`, keystores e secrets ficam fora do repositório.

## Limitações conhecidas

- **Assistente (chat):** a tela existe, mas ainda não envia mensagens a nenhum serviço. O `ChatViewModel` só limpa o campo de texto. A integração com a IA da Inventra (`inventra-artificial-intelligence-2`) ainda não foi feita.
- **Build de release sem otimização:** o `release` está com `optimization.enable = false`, ou seja, sem minificação nem ofuscação.
- **Testes instrumentados:** existe só o `ExampleInstrumentedTest`. Os fluxos de tela são cobertos pelos testes de ViewModel, não por testes de interface.
- **Notificações em segundo plano:** o Android executa o `WorkManager` no mínimo a cada 15 minutos e pode atrasar a verificação em modo de economia de bateria, então o aviso não é instantâneo.
- **Backup do Android:** `allowBackup` está ligado, com regras em `res/xml/backup_rules.xml` e `data_extraction_rules.xml`.

## Time

Parte do projeto interdisciplinar **Inventra**, da [InventraTech](https://github.com/InventraTech) (frente de Desenvolvimento Mobile). Veja os demais repositórios e o time completo no [perfil da organização](https://github.com/InventraTech).

| Frente                 | Quem                                                    |
|:-----------------------|:--------------------------------------------------------|
| Desenvolvimento Mobile | [Felipe Kogake](https://github.com/FelipeKogake)        |
| CI/CD e implantação    | Operações Ágeis (workflows reutilizáveis em [`.github`](https://github.com/InventraTech/.github)) |
