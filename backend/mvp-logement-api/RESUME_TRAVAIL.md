# Résumé du travail réalisé – Backend MVP Logement

Document pour aider au compte rendu / rédaction du projet. Il décrit tout ce qui a été mis en place côté backend.
 
---

## 1. Contexte du projet

**MVP Logement** (Altern’Up) est une application d’**échange de logements** entre particuliers : un utilisateur propose son logement pour des dates données et indique la ville où il souhaite aller ; un autre peut proposer l’inverse. Quand les deux offres correspondent (même ville souhaitée, dates qui se chevauchent), une demande d’échange peut être créée, acceptée ou refusée. Une **conversation** (messagerie) est ouverte après acceptation.

Le backend est une **API REST** en **Java (Spring Boot 4)** avec **PostgreSQL**, **JWT** pour l’authentification, **Flyway** pour les migrations, **Docker** pour le déploiement, et des **tests unitaires** sur les parties métier importantes.
 
---

## 2. Stack technique

| Technologie        | Rôle                                      |
|--------------------|-------------------------------------------|
| Java 25            | Langage                                   |
| Spring Boot 4.0.1  | Framework (Web, JPA, Security)            |
| Spring Data JPA    | Accès données, repositories              |
| PostgreSQL         | Base de données                           |
| Flyway             | Migrations SQL versionnées                |
| JWT (jjwt 0.12.5)  | Tokens d’authentification                 |
| Maven              | Build                                     |
| Docker / Compose   | Conteneurisation (API + base)             |
| JUnit 5 + Mockito  | Tests unitaires                           |
 
---

## 3. Structure fonctionnelle (modules)

### 3.1 Authentification (`auth`)

- **Inscription** : `POST /auth/register` (email, mot de passe) → création utilisateur (mot de passe hashé avec BCrypt).
- **Connexion** : `POST /auth/login` (email, password) → renvoie un **JWT** (subject = userId, claim `email`).
- **Filtre JWT** : chaque requête (hors `/auth/**`) peut envoyer `Authorization: Bearer <token>` ; le filtre parse le token et met l’email en “principal” Spring Security.
- **Sécurité** : sessions stateless, CSRF désactivé pour une API, endpoints publics = `/auth/**`, GET `/logements/**` et `/error` ; le reste est protégé (authentifié).

### 3.2 Utilisateurs (`user`)

- Entité **User** : id, email, passwordHash, createdAt.
- **UserRepository** (JPA) pour recherche par email, utilisé à l’auth et pour lier logements / échanges / messages.

### 3.3 Logements (`logement`)

- **Entité** : id, owner (User), title, city, description, createdAt.
- **API** :
    - `GET /logements` : liste tous les logements (public).
    - `GET /logements/{id}` : détail d’un logement (public).
    - `POST /logements` : créer un logement (JWT, propriétaire = utilisateur connecté).
    - `PUT /logements/{id}` : modifier (réservé au propriétaire).
    - `DELETE /logements/{id}` : supprimer (réservé au propriétaire).
- **LogementBuilder** (patron Builder) : construction d’un `Logement` à partir de `CreateLogementRequest` ou `UpdateLogementRequest` pour centraliser la logique de création/mise à jour.
- **LogementService** : vérification propriétaire (email), appel au builder, sauvegarde / mise à jour / suppression.

### 3.4 Périodes d’échange (`exchange` – ExchangePeriod)

- **Entité ExchangePeriod** : id, logement, wantCity (ville souhaitée), startDate, endDate, status (OPEN/CLOSED), createdAt.
- **API** :
    - `POST /exchange-periods` : créer une période (propriétaire du logement uniquement).
    - `GET /exchange-periods/me` : mes périodes (JWT).
    - `GET /exchange-periods/{id}/matches` : périodes “compatibles” avec la période donnée (même logique que le matching côté demande d’échange).
- **Règles** : dates start ≤ end ; seul le propriétaire du logement peut créer une période.

### 3.5 Demandes d’échange (`exchange` – ExchangeRequest)

- **Entité ExchangeRequest** : fromPeriod, toPeriod, status (PENDING/ACCEPTED/REJECTED), createdAt, updatedAt.
- **Règles métier** :
    - Création : l’utilisateur doit être propriétaire du logement de **fromPeriod** ; il ne peut pas envoyer une demande à lui-même (toPeriod) ; les deux périodes doivent être OPEN ; les deux périodes doivent **matcher** (ville de l’une = ville souhaitée de l’autre, et dates qui se chevauchent) ; pas de doublon (même couple from/to).
    - Acceptation : seul le propriétaire du logement de **toPeriod** peut accepter ; après acceptation, une **Conversation** est créée et les deux périodes passent en CLOSED.
    - Rejet : seul le destinataire (toPeriod) peut rejeter.
- **API** :
    - `POST /exchange-requests` : créer une demande (body : fromPeriodId, toPeriodId).
    - `GET /exchange-requests/inbox` : demandes reçues (moi = owner du logement de toPeriod).
    - `GET /exchange-requests/outbox` : demandes envoyées (moi = owner du logement de fromPeriod).
    - `POST /exchange-requests/{id}/accept` : accepter.
    - `POST /exchange-requests/{id}/reject` : rejeter.

**Stratégie de matching (patron Strategy)**
- Interface **MatchStrategy** : `boolean isMatch(ExchangePeriod from, ExchangePeriod to)`.
- **DefaultMatchStrategy** :
    - Villes “croisées” : ville du logement de `to` = ville souhaitée de `from`, et inversement (avec normalisation casse/espaces).
    - Périodes qui se chevauchent (overlap des dates).
- Injectée dans **ExchangeRequestService** pour vérifier la compatibilité avant création de la demande.

### 3.6 Conversations et messagerie (`messaging`)

- **Conversation** : liée à un **ExchangeRequest** (une conversation par échange accepté).
- **Message** : conversation, sender (User), content, createdAt.
- **API** :
    - `GET /conversations/me` : mes conversations (participant = owner du logement de fromPeriod ou toPeriod de l’échange lié).
    - `GET /conversations/{id}` : détail d’une conversation (si on en fait partie).
    - `GET /conversations/{id}/messages` : liste des messages (participant uniquement).
    - `POST /conversations/{id}/messages` : envoyer un message (body : content) (participant uniquement).
- **Événement domaine (patron Observer)** : après enregistrement d’un message, le **MessageService** publie un **MessageSentEvent** via `ApplicationEventPublisher`. Un **MessageNotificationListener** écoute cet événement (pour l’instant sans action supplémentaire ; prévu pour notifications email, websocket, etc.).

---

## 4. Base de données et migrations (Flyway)

- **V1__init.sql** : tables `users`, `logements`.
- **V2__create_exchange_periods.sql** : table `exchange_periods` (logement_id, want_city, start_date, end_date, status, contraintes, index).
- **V3__create_exchange_requests.sql** : table `exchange_requests` (from_period_id, to_period_id, status, created_at, updated_at).
- **V4__create_messaging.sql** : tables `conversations` (exchange_request_id), `messages` (conversation_id, sender_id, content, created_at).

En production / Docker, `spring.jpa.hibernate.ddl-auto` peut être mis à `update` pour adapter le schéma si besoin (ou `validate` une fois les migrations stabilisées).
 
---

## 5. Patrons de conception utilisés

1. **Builder** : **LogementBuilder** pour construire un `Logement` à partir des DTOs (create/update), utilisé dans **LogementService**.
2. **Strategy** : **MatchStrategy** / **DefaultMatchStrategy** pour la règle de compatibilité entre deux périodes d’échange ; injectée dans **ExchangeRequestService**.
3. **Observer (événements domaine)** : **MessageSentEvent** publié après envoi d’un message ; **MessageNotificationListener** écoute l’événement (extensible pour notifications).
4. **Repository** : Spring Data JPA (LogementRepository, UserRepository, ExchangePeriodRepository, ExchangeRequestRepository, ConversationRepository, MessageRepository).
5. **DTO** : requêtes (CreateLogementRequest, UpdateLogementRequest, CreateExchangePeriodRequest, CreateExchangeRequestRequest, SendMessageRequest, etc.) et réponses (LogementResponse, ExchangePeriodResponse, ExchangeRequestResponse, MessageResponse, ConversationResponse, etc.) pour découpler API et entités.

---

## 6. Gestion des erreurs

- **NotFoundException** : ressource introuvable (404).
- **ForbiddenException** : action non autorisée (ex. modifier un logement dont on n’est pas propriétaire) (403).
- **BadRequestException** : données invalides ou règle métier non respectée (400).
- **GlobalExceptionHandler** / **ApiExceptionHandler** : mapping de ces exceptions en réponses HTTP JSON cohérentes.

---

## 7. Tests unitaires

- **LogementBuilderTest** : construction à partir de CreateLogementRequest, applyUpdate, chaînage.
- **DefaultMatchStrategyTest** : isMatch (villes croisées + overlap), pas de match (villes différentes, dates sans chevauchement), normalisation (casse, espaces), gestion de null.
- **LogementServiceTest** (Mockito) : create (succès, utilisateur non trouvé), update (succès, logement non trouvé, non propriétaire), delete (succès, non trouvé, non propriétaire).
- **MvpLogementApiApplicationTests** : chargement du contexte Spring.

Commande : `.\mvnw test` (Windows, depuis `backend/mvp-logement-api`).
 
---

## 8. Docker

- **Dockerfile** (multi-stage) : build Maven (JDK 25) → image finale JRE 25 avec le JAR, port 8080.
- **docker-compose.yml** (à la racine du repo) :
    - Service **db** : PostgreSQL 16, base `alternup`, user/password (projetrncp), volume persistant.
    - Service **api** : build depuis `backend/mvp-logement-api`, dépend de `db`, variables d’environnement pour l’URL JDBC et `SPRING_JPA_HIBERNATE_DDL_AUTO=update`, port 8080.
- Lancer : à la racine du projet, `docker compose up --build`. Arrêt + suppression volumes : `docker compose down -v`.

---

## 9. Résumé des endpoints (pour le rapport)

| Méthode | Endpoint | Accès | Description |
|--------|----------|--------|-------------|
| POST   | /auth/register | Public | Inscription |
| POST   | /auth/login    | Public | Connexion (JWT) |
| GET    | /logements    | Public | Liste logements |
| GET    | /logements/{id} | Public | Détail logement |
| POST   | /logements    | JWT   | Créer logement |
| PUT    | /logements/{id} | JWT (owner) | Modifier logement |
| DELETE | /logements/{id} | JWT (owner) | Supprimer logement |
| POST   | /exchange-periods | JWT (owner logement) | Créer période d’échange |
| GET    | /exchange-periods/me | JWT | Mes périodes |
| GET    | /exchange-periods/{id}/matches | JWT | Périodes compatibles |
| POST   | /exchange-requests | JWT | Créer demande d’échange |
| GET    | /exchange-requests/inbox | JWT | Demandes reçues |
| GET    | /exchange-requests/outbox | JWT | Demandes envoyées |
| POST   | /exchange-requests/{id}/accept | JWT (destinataire) | Accepter |
| POST   | /exchange-requests/{id}/reject | JWT (destinataire) | Rejeter |
| GET    | /conversations/me | JWT | Mes conversations |
| GET    | /conversations/{id} | JWT (participant) | Détail conversation |
| GET    | /conversations/{id}/messages | JWT (participant) | Liste messages |
| POST   | /conversations/{id}/messages | JWT (participant) | Envoyer message |

(Un endpoint **/me** ou similaire peut exister pour le profil utilisateur courant selon le code restant.)
 
---

## 10. Points utiles pour la rédaction

- **Architecture** : couche Controller → Service → Repository ; DTO en entrée/sortie ; entités JPA pour la persistance.
- **Sécurité** : authentification stateless JWT ; autorisations basées sur le rôle “propriétaire” ou “participant” selon la ressource.
- **Qualité** : nettoyage des commentaires inutiles, tests unitaires ciblés (builder, stratégie de match, service logement), Docker pour reproductibilité.
- **Évolutivité** : stratégie de match injectable (autres algorithmes possibles), listener sur envoi de message (notifications à brancher plus tard).

## 11. Architecture C4 et approche DDD

Dans le cadre de la conception de l’API **AlternUp**, l’architecture logicielle a été modélisée en s’appuyant sur deux approches complémentaires :

- le **modèle C4** (Context, Container, Component, Code) permettant de représenter l’architecture à différents niveaux d’abstraction ;
- les principes du **Domain-Driven Design (DDD)** afin de structurer le système autour du domaine métier et de ses sous-domaines.

Cette approche permet :

- une **séparation claire des responsabilités** entre les différentes couches du système ;
- une **meilleure lisibilité de l’architecture** pour les développeurs et les parties prenantes ;
- une **évolutivité facilitée** grâce à l’isolation des contextes métier.

---

### Diagramme C4

Le diagramme C4 illustre la structure globale de l’application et la manière dont les différents composants interagissent entre eux.

Il met notamment en évidence :

- les **acteurs externes** (utilisateurs, services tiers) ;
- les **conteneurs applicatifs** (API backend, base de données PostgreSQL, services externes) ;
- les **composants internes** organisés selon les principes du DDD.

![img.png](DDD%20/img.png)

---

### Consultation interactive du diagramme

Afin d’explorer l’architecture de manière interactive, le diagramme C4 peut être visualisé via la plateforme **Structurizr Playground**.

1. Se rendre sur le site :


https://playground.structurizr.com/


2. Copier le contenu du fichier suivant :

[C4.dsl](DDD%20/C4.dsl)


3. Coller ce contenu dans l’éditeur Structurizr afin de générer automatiquement la visualisation du modèle C4.

Cette représentation permet de naviguer entre :

- **Context** : vision globale du système et des acteurs
- **Container** : architecture des services applicatifs
- **Component** : organisation interne des modules
- **Code** : implémentation technique détaillée

---

### Organisation selon le Domain-Driven Design

L’application est structurée selon les principes du **Domain-Driven Design**, permettant d’isoler les responsabilités métier et techniques.

Les principaux domaines fonctionnels identifiés sont :

- **Authentication** : gestion de l’inscription, de la connexion et des tokens JWT
- **Users** : gestion des profils utilisateurs
- **Logements** : gestion des annonces de logements
- **Exchange Requests** : gestion des demandes d’échange
- **Messaging** : gestion des conversations et des messages

Cette organisation permet :

- une **meilleure maintenabilité du code**
- une **évolutivité facilitée**
- une **séparation claire entre logique métier et infrastructure techniq