# MVP Logement – API Backend

API REST du projet **MVP Logement (Altern'Up)** : plateforme d’échange de logements entre particuliers. Les utilisateurs publient des logements et des périodes d’échange (dates + ville souhaitée) ; lorsqu’une période correspond à une autre (même ville, dates qui se chevauchent), une demande d’échange peut être créée, acceptée ou refusée. Une conversation (messagerie) est ouverte après acceptation.

## Technologies

- **Java 25** – **Spring Boot 4** (Web, Data JPA, Security)
- **PostgreSQL** – Base de données
- **Flyway** – Migrations SQL
- **JWT** – Authentification (jjwt)
- **Maven** – Build
- **Docker** – Déploiement (API + base)

## Prérequis

- **Lancer en local** : JDK 25, Maven, PostgreSQL (base `alternup`, user/mot de passe configurés dans `application.properties` ou variables d’environnement).
- **Lancer avec Docker** : Docker et Docker Compose uniquement.

## Lancer l’API

### Avec Docker (recommandé pour un rendu / démo)

À la **racine du dépôt** (parent de `backend/`) :

```bash
docker compose up --build
```

- API : http://localhost:8080
- PostgreSQL : localhost:5433 (base `alternup`)

Arrêter et supprimer les volumes :

```bash
docker compose down -v
```

### En local (sans Docker)

1. Démarrer PostgreSQL et créer la base `alternup` si besoin.
2. Depuis `backend/mvp-logement-api` :

```bash
# Windows (PowerShell)
.\mvnw spring-boot:run
 
# Linux / macOS
./mvnw spring-boot:run
```

Configurer si nécessaire l’URL JDBC, le user et le mot de passe (fichier `src/main/resources/application.properties` ou variables d’environnement).

## Tests

Depuis `backend/mvp-logement-api` :

```bash
.\mvnw test
```

Les tests couvrent notamment :

- **LogementBuilder** – Construction d’entités à partir des DTOs
- **DefaultMatchStrategy** – Règles de compatibilité entre périodes d’échange
- **LogementService** – Création, mise à jour, suppression (avec mocks)

## Structure des principaux packages

| Package | Rôle |
|---------|------|
| `auth` | Inscription, connexion, JWT, filtre d’authentification |
| `user` | Entité utilisateur et repository |
| `logement` | Logements (CRUD), Builder |
| `exchange` | Périodes d’échange, demandes d’échange, stratégie de matching |
| `messaging` | Conversations et messages, événement “message envoyé” |
| `config` | Sécurité (SecurityConfig) |
| `common` | Exceptions (NotFound, Forbidden, BadRequest) et handlers |

## API – Endpoints principaux

- **Auth** : `POST /auth/register`, `POST /auth/login`
- **Logements** : `GET /logements`, `GET /logements/{id}` (public) ; `POST`, `PUT`, `DELETE /logements[/{id}]` (JWT, propriétaire)
- **Périodes d’échange** : `POST /exchange-periods`, `GET /exchange-periods/me`, `GET /exchange-periods/{id}/matches` (JWT)
- **Demandes d’échange** : `POST /exchange-requests`, `GET /exchange-requests/inbox`, `GET /exchange-requests/outbox`, `POST /exchange-requests/{id}/accept`, `POST /exchange-requests/{id}/reject` (JWT)
- **Conversations / messages** : `GET /conversations/me`, `GET /conversations/{id}`, `GET|POST /conversations/{id}/messages` (JWT, participant)

Les routes `/auth/**` et `GET /logements/**` sont publiques ; les autres nécessitent un en-tête `Authorization: Bearer <token>` (token renvoyé par `/auth/login`).

## Patrons de conception

- **Builder** : `LogementBuilder` pour la construction des logements (create/update).
- **Strategy** : `MatchStrategy` / `DefaultMatchStrategy` pour la compatibilité des périodes d’échange.
- **Observer** : `MessageSentEvent` + `MessageNotificationListener` après envoi d’un message (extensible pour notifications).

## Documentation pour la rédaction du projet

Le fichier **`RESUME_TRAVAIL.md`** dans ce même dossier contient un **résumé détaillé** du travail réalisé (fonctionnalités, règles métier, BDD, patrons, tests, Docker). Il est destiné à aider à la rédaction du rapport ou de la documentation (par exemple pour le binôme en charge de cette partie).