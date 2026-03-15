# Guide détaillé : tester l’API avec Swagger

Ce guide explique pas à pas comment tester tous les endpoints de l’API via **Swagger UI**.

---

## 1. Accéder à Swagger UI

1. Démarre l’application (par exemple `.\mvnw spring-boot:run` ou `docker compose up`).
2. Ouvre ton navigateur et va sur :  
   **http://localhost:8080/swagger-ui.html**
3. Tu dois voir la page Swagger avec les groupes d’endpoints (auth-controller, logement-controller, etc.).

---

## 2. Tester sans être connecté (endpoints publics)

### 2.1 Inscription – `POST /auth/register`

1. Clique sur **auth-controller** pour déplier.
2. Clique sur **POST /auth/register** → **Try it out**.
3. Dans le corps de la requête (Request body), remplace le JSON par exemple :

```json
{
  "email": "alice@test.com",
  "password": "motdepasse123"
}
```

- **email** : une adresse valide.
- **password** : entre 8 et 72 caractères.

4. Clique sur **Execute**.
5. En **Response body** tu dois avoir un code **201** et un JSON du type : `{"id": 1, "email": "alice@test.com"}`.

Tu peux refaire la même chose avec un deuxième utilisateur (ex. `bob@test.com`) pour tester les échanges plus tard.

### 2.2 Connexion – `POST /auth/login`

1. Ouvre **POST /auth/login** → **Try it out**.
2. Body exemple :

```json
{
  "email": "alice@test.com",
  "password": "motdepasse123"
}
```

3. **Execute**.
4. En **Response body** (code 200), récupère le champ **token**. Exemple :

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresInSeconds": 3600
}
```

**Copie tout le contenu du champ `token`** (sans les guillemets) : tu en auras besoin pour les endpoints protégés.

### 2.3 Liste des logements – `GET /logements`

1. **GET /logements** → **Try it out** → **Execute**.
2. Sans token, tu obtiens une liste (souvent vide au début) avec code 200.

### 2.4 Détail d’un logement – `GET /logements/{id}`

1. **GET /logements/{id}** → **Try it out**.
2. Saisis un **id** (ex. `1` si tu as déjà créé un logement).
3. **Execute** → 200 + détail du logement, ou 404 si l’id n’existe pas.

---

## 3. Activer le JWT dans Swagger (Authorize)

Pour appeler les endpoints protégés, il faut envoyer le token à chaque requête. Swagger peut le faire pour toi.

1. En haut à droite de la page Swagger, clique sur le bouton **Authorize** (cadenas).
2. Dans le champ **Value** à côté de **bearerAuth**, colle **uniquement le token** (la longue chaîne reçue dans la réponse de `/auth/login`).
   - Ne mets **pas** le mot "Bearer " devant : Swagger l’ajoute tout seul.
   - Exemple de valeur : `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZW1haWwiOi...`
3. Clique sur **Authorize**, puis **Close**.

Toutes les requêtes que tu lanceras ensuite depuis cette page enverront ce token dans l’en-tête `Authorization: Bearer <token>`.

Pour te connecter en tant qu’autre utilisateur : **Authorize** à nouveau et colle le token obtenu avec `POST /auth/login` pour cet utilisateur.

---

## 4. Tester les endpoints protégés (avec JWT)

Une fois **Authorize** rempli avec le token d’un utilisateur (ex. Alice), tu peux enchaîner les scénarios suivants.

### 4.1 Créer un logement – `POST /logements`

1. **POST /logements** → **Try it out**.
2. Body exemple :

```json
{
  "title": "Studio centre-ville",
  "city": "Bordeaux",
  "description": "Proche tram, calme"
}
```

3. **Execute** → 201 et le logement créé (avec `id`, `ownerId`, etc.). Note l’**id** du logement (ex. `1`) pour la suite.

### 4.2 Modifier un logement – `PUT /logements/{id}`

1. **PUT /logements/{id}** → **Try it out**.
2. **id** : celui de ton logement (ex. `1`).
3. Body exemple :

```json
{
  "title": "Studio rénové centre-ville",
  "city": "Bordeaux",
  "description": "Proche tram, calme, neuf"
}
```

4. **Execute** → 200 avec le logement mis à jour (seul le propriétaire peut modifier).

### 4.3 Créer une période d’échange – `POST /exchange-periods`

1. **POST /exchange-periods** → **Try it out**.
2. Body (remplace `logementId` par l’id de ton logement, et les dates par des dates valides) :

```json
{
  "logementId": 1,
  "wantCity": "Paris",
  "startDate": "2025-07-01",
  "endDate": "2025-07-15"
}
```

- **logementId** : id d’un logement dont tu es le propriétaire.
- **wantCity** : ville où tu veux aller.
- **startDate** / **endDate** : au format `AAAA-MM-JJ`, avec `startDate` ≤ `endDate`.

3. **Execute** → 201. Note l’**id** de la période (ex. `1`).

### 4.4 Mes périodes – `GET /exchange-periods/me`

1. **GET /exchange-periods/me** → **Try it out** → **Execute**.
2. Réponse 200 : liste des périodes des logements que tu possèdes.

### 4.5 Périodes compatibles (matches) – `GET /exchange-periods/{id}/matches`

1. **GET /exchange-periods/{id}/matches** → **Try it out**.
2. **id** : l’id d’**une de tes** périodes (tu dois en être le propriétaire).
3. **Execute** → 200 : liste des autres périodes “compatibles” (ville croisée + dates qui se chevauchent).

### 4.6 Créer une demande d’échange – `POST /exchange-requests`

1. **POST /exchange-requests** → **Try it out**.
2. Body :

```json
{
  "fromPeriodId": 1,
  "toPeriodId": 2
}
```

- **fromPeriodId** : une période dont **tu** es le propriétaire (ton logement).
- **toPeriodId** : une période d’**un autre** utilisateur, compatible avec la première (même ville souhaitée, dates qui se chevauchent).

3. **Execute** → 201 si tout est valide (pas de doublon, périodes OPEN, etc.). Note l’**id** de la demande (ex. `1`).

### 4.7 Boîte de réception / envoyés – `GET /exchange-requests/inbox` et `GET /exchange-requests/outbox`

1. **GET /exchange-requests/inbox** → **Try it out** → **Execute** : demandes reçues (l’autre veut échanger avec toi).
2. **GET /exchange-requests/outbox** → **Try it out** → **Execute** : demandes que tu as envoyées.

### 4.8 Accepter une demande – `POST /exchange-requests/{id}/accept`

1. **POST /exchange-requests/{id}/accept** → **Try it out**.
2. **id** : l’id d’une demande **reçue** (inbox), donc où **toi** tu es le propriétaire du logement de la période **to**.
3. **Execute** → 200. Une **conversation** est créée automatiquement ; les deux périodes passent en CLOSED.

### 4.9 Rejeter une demande – `POST /exchange-requests/{id}/reject`

1. **POST /exchange-requests/{id}/reject** → **Try it out**.
2. **id** : id d’une demande reçue (inbox).
3. **Execute** → 200.

### 4.10 Mes conversations – `GET /conversations/me`

1. **GET /conversations/me** → **Try it out** → **Execute**.
2. Réponse 200 : liste des conversations (liées aux échanges acceptés). Note l’**id** d’une conversation pour les messages.

### 4.11 Détail d’une conversation – `GET /conversations/{id}`

1. **GET /conversations/{id}** → **Try it out**.
2. **id** : id d’une conversation où tu participes.
3. **Execute** → 200.

### 4.12 Liste des messages – `GET /conversations/{id}/messages`

1. **GET /conversations/{id}/messages** → **Try it out**.
2. **id** : id de la conversation.
3. **Execute** → 200 : liste des messages (souvent vide au début).

### 4.13 Envoyer un message – `POST /conversations/{id}/messages`

1. **POST /conversations/{id}/messages** → **Try it out**.
2. **id** : id de la conversation.
3. Body :

```json
{
  "content": "Bonjour, on se fait l’échange la semaine du 7 juillet ?"
}
```

4. **Execute** → 200 : le message est créé et renvoyé dans la réponse.

### 4.14 Supprimer un logement – `DELETE /logements/{id}`

1. **DELETE /logements/{id}** → **Try it out**.
2. **id** : id d’un logement dont tu es le propriétaire.
3. **Execute** → 204 (No Content) si tout s’est bien passé.

---

## 5. Scénario complet (de A à Z)

Pour tout enchaîner dans l’ordre avec **deux utilisateurs** :

1. **Sans token**  
   - `POST /auth/register` : Alice (alice@test.com).  
   - `POST /auth/register` : Bob (bob@test.com).

2. **Token Alice**  
   - `POST /auth/login` (alice) → copier le **token**.  
   - **Authorize** → coller le token.  
   - `POST /logements` : logement à Bordeaux (ex. "Studio Bordeaux"). Noter **logement id** (ex. 1).  
   - `POST /exchange-periods` : logementId=1, wantCity=Paris, 2025-07-01 / 2025-07-15. Noter **période id** (ex. 1).

3. **Token Bob**  
   - `POST /auth/login` (bob) → copier le **token**.  
   - **Authorize** → coller le token de Bob.  
   - `POST /logements` : logement à Paris (ex. "Appart Paris"). Noter **logement id** (ex. 2).  
   - `POST /exchange-periods` : logementId=2, wantCity=Bordeaux, 2025-07-05 / 2025-07-12. Noter **période id** (ex. 2).

4. **Bob envoie une demande à Alice**  
   - Toujours avec le token Bob :  
     `POST /exchange-requests` → fromPeriodId=2, toPeriodId=1.  
   - Noter l’**id** de la demande (ex. 1).

5. **Alice accepte**  
   - **Authorize** avec le token d’Alice.  
   - `POST /exchange-requests/1/accept`.

6. **Conversation et message**  
   - Avec Alice ou Bob : `GET /conversations/me` → noter l’**id** de la conversation (ex. 1).  
   - `POST /conversations/1/messages` → `{"content": "Super, on se fait l’échange !"}`.  
   - `GET /conversations/1/messages` pour voir le message.

---

## 6. Erreurs courantes

| Code / comportement | Cause probable |
|---------------------|----------------|
| **401 Unauthorized** | Token manquant, expiré ou invalide. Refais un `POST /auth/login` et **Authorize** à nouveau. |
| **403 Forbidden** | Tu n’es pas le propriétaire (ex. modifier un logement d’un autre, accepter une demande qui ne t’est pas destinée). |
| **404 Not Found** | Id inexistant (logement, période, demande, conversation). |
| **400 Bad Request** | Données invalides (ex. fromPeriodId = toPeriodId, périodes non compatibles, doublon de demande, dates incohérentes). Vérifier le message d’erreur dans la réponse. |

En cas de **400** ou **404**, regarde toujours le **Response body** dans Swagger : le message indique en général la raison (ex. "Periods are not compatible", "Request already exists").

---

## 7. Récap des URLs utiles

- **Swagger UI** : http://localhost:8080/swagger-ui.html  
- **OpenAPI JSON** : http://localhost:8080/v3/api-docs  

Tu peux partager ce guide à ton binôme pour qu’il teste l’API de bout en bout avec Swagger.
