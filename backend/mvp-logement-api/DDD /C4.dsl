workspace "EduSwitchLog - C4 Backend" "C4 Model for EduSwitchLog backend aligned with DDD (BC1..BC6)" {

  model {

    /****************************************************************
     * ETAPE 1 — ACTEURS (People)
     ****************************************************************/
    student = person "Etudiant" "Publie des annonces, gère son rythme et échange via la messagerie."
    admin   = person "Administrateur" "Modère les contenus et traite les signalements."

    /****************************************************************
     * ETAPE 2 — SYSTEMES EXTERNES (C1)
     ****************************************************************/
    extObjectStorage = softwareSystem "Object Storage (S3)" "Stockage d'objets pour médias (photos annonces/messages) via URLs pré-signées." {
      tags "External"
    }

    extEmail = softwareSystem "Email Transactionnel" "Envoi d'emails (vérification email, notifications transactionnelles)." {
      tags "External"
    }

    extGeocoding = softwareSystem "Geocoding System" "Géocodage / normalisation d'adresse (option distance/rayon)." {
      tags "External"
    }

    extBroker = softwareSystem "Message Broker" "Bus/queue asynchrone (events, jobs, projections, notifications)." {
      tags "External"
    }

    extPush = softwareSystem "Push Provider" "Notifications push mobiles (FCM/APNS)." {
      tags "External"
    }

    /****************************************************************
     * ETAPE 3 — SYSTEME PRINCIPAL (C1)
     ****************************************************************/
    eduswitchlog = softwareSystem "EduSwitchLog" "Plateforme d'échange de logements étudiants basée sur la compatibilité de rythmes et la mise en relation." {

      /**************************************************************
       * ETAPE 4 — CONTAINERS INTERNES (C2)
       **************************************************************/
      mobileApp = container "EduSwitchLog Mobile App" "Application mobile iOS/Android : UI, authentification, annonces, messagerie." "Flutter / React Native"

      database = container "EduSwitchLog Database" "Stockage transactionnel (comptes, profils, annonces, conversations, messages, signalements...)." "PostgreSQL" {
        tags "Database"
      }

      /**************************************************************
       * ETAPE 5 — BACKEND API (C2) + COMPOSANTS (C3) ALIGNES DDD
       **************************************************************/
      backendApi = container "EduSwitchLog Backend API" "API sécurisée exposant les capacités métier par Bounded Context (IAM, Profil, Annonces, Matching, Messagerie, Modération)." "REST API (Spring Boot / NestJS)" {

        /************************************************************
         * BC1 — Identité & Accès (IAM) : Agrégat Compte
         ************************************************************/
        group "BC1 — Identité & Accès (IAM)" {
          authController = component "AuthController" "Endpoints auth (register/login/refresh/logout) + vérification email." "REST Controller" {
            tags "BC1"
          }
          authenticationService = component "AuthenticationService" "Validation credentials, émission/validation JWT access/refresh, rotation refresh." "Service" {
            tags "BC1"
          }
          registrationService = component "RegistrationService" "Création Compte (hash password), émission event AccountCreated." "Service" {
            tags "BC1"
          }
          emailVerificationService = component "EmailVerificationService" "Vérification email via token (TTL, anti-rejeu, consommation)." "Service" {
            tags "BC1"
          }
          accountRepository = component "AccountRepository" "Persistance Compte (IAM) : email unique, statut, sécurité." "Repository" {
            tags "BC1"
          }
        }

        /************************************************************
         * BC2 — Profil Étudiant : Agrégat Étudiant (référence CompteId)
         ************************************************************/
        group "BC2 — Profil Étudiant" {
          studentController = component "StudentController" "Endpoints profil étudiant (lecture/maj) et suppression (RGPD)." "REST Controller" {
            tags "BC2"
          }
          studentProfileService = component "StudentProfileService" "Mise à jour profil, statut (actif/suspendu), anonymisation/soft-delete." "Service" {
            tags "BC2"
          }
          studentRepository = component "StudentRepository" "Persistance Étudiant (Profil) : CompteId, identité, adresse, préférences." "Repository" {
            tags "BC2"
          }
        }

        /************************************************************
         * BC3 — Annonces : Agrégat Annonce + ACL Médias + Géocodage
         ************************************************************/
        group "BC3 — Annonces" {
          listingController = component "ListingController" "Endpoints annonces (CRUD, publication/archivage) + lecture." "REST Controller" {
            tags "BC3"
          }
          listingService = component "ListingService" "Règles métier Annonce (invariants publication, owner actif, statut)." "Service" {
            tags "BC3"
          }
          listingRepository = component "ListingRepository" "Persistance Annonce (transactionnel)." "Repository" {
            tags "BC3"
          }

          listingQueryService = component "ListingQueryService" "Recherche/filtrage d'annonces (ville, prix, critères) sans scoring métier." "Service" {
            tags "BC3"
          }

          mediaUploadService = component "MediaUploadService" "Génération URLs pré-signées PUT + validation MIME/size." "Service" {
            tags "BC3"
          }
          mediaAccessService = component "MediaAccessService" "Génération URLs pré-signées GET + contrôles d'accès." "Service" {
            tags "BC3"
          }

          geocodingClient = component "GeocodingClient" "Client HTTP géocodage/normalisation adresse." "Adapter" {
            tags "BC3"
          }
        }

        /************************************************************
         * BC4 — Matching : Service de domaine + Projection (Read Model)
         ************************************************************/
        group "BC4 — Matching" {
          matchingService = component "MatchingService" "Calcul compatibilité (rythme, contraintes, score) à partir des annonces." "Domain Service" {
            tags "BC4"
          }
          matchProjectionUpdater = component "MatchProjectionUpdater" "Consumer d'events AnnoncePubliée/AnnonceModifiée => met à jour la projection." "Worker/Consumer" {
            tags "BC4"
          }
          matchReadRepository = component "MatchReadRepository" "Stockage/accès modèle de lecture Match (candidats, score, état)." "Repository" {
            tags "BC4"
          }
        }

        /************************************************************
         * BC5 — Messagerie : Agrégat Conversation + Entité Message
         ************************************************************/
        group "BC5 — Messagerie" {
          conversationController = component "ConversationController" "Endpoints conversations (création, liste, fermeture, détails)." "REST Controller" {
            tags "BC5"
          }
          conversationService = component "ConversationService" "Règles Conversation (participants, fermeture, lien annonce)." "Service" {
            tags "BC5"
          }
          conversationRepository = component "ConversationRepository" "Persistance Conversation (agrégat racine)." "Repository" {
            tags "BC5"
          }

          messageController = component "MessageController" "Endpoints messages (envoi, suppression logique, médias)." "REST Controller" {
            tags "BC5"
          }
          messageService = component "MessageService" "Envoi message, contrôles d'accès, émission event MessageEnvoyé." "Service" {
            tags "BC5"
          }
          messageRepository = component "MessageRepository" "Persistance Message (entité rattachée Conversation)." "Repository" {
            tags "BC5"
          }
        }

        /************************************************************
         * BC6 — Modération : Agrégat Signalement + décisions
         ************************************************************/
        group "BC6 — Modération" {
          moderationController = component "ModerationController" "Endpoints admin (liste signalements, revue, décisions)." "REST Controller" {
            tags "BC6"
          }
          moderationService = component "ModerationService" "Workflow modération : analyser signalement, appliquer décision (suspendre/archiver/fermer)." "Service" {
            tags "BC6"
          }
          reportRepository = component "ReportRepository" "Persistance Signalement (audit, statut, cible)." "Repository" {
            tags "BC6"
          }
        }

        /************************************************************
         * Orchestration / Intégration (events + notifications)
         ************************************************************/
        group "Orchestration & Intégration" {
          notificationOrchestrator = component "NotificationOrchestrator" "Orchestration événementielle : routage events vers email/push/broker." "Service" {
            tags "Integration"
          }
          emailDispatcher = component "EmailDispatcher" "Adaptateur email (templates + provider)." "Adapter" {
            tags "Integration"
          }
          pushDispatcher = component "PushDispatcher" "Adaptateur push (FCM/APNS)." "Adapter" {
            tags "Integration"
          }
          eventPublisher = component "EventPublisher" "Publication d'événements de domaine (bus/queue) + garanties delivery." "Adapter" {
            tags "Integration"
          }
        }

        /***********************
         * C3 — Relations internes (alignées DDD)
         ***********************/

        /***** BC1 IAM *****/
        authController -> registrationService "POST /auth/register"
        authController -> authenticationService "POST /auth/login, POST /auth/refresh"
        authController -> emailVerificationService "POST /auth/verify-email"

        registrationService -> accountRepository "Persist Compte"
        authenticationService -> accountRepository "Load Compte + validate"
        emailVerificationService -> accountRepository "Mark email verified"

        registrationService -> eventPublisher "Publish DomainEvent: AccountCreated"
        eventPublisher -> notificationOrchestrator "Route AccountCreated"
        notificationOrchestrator -> emailDispatcher "Send email verification"

        /***** BC2 Profil *****/
        studentController -> studentProfileService "PATCH /students/me, DELETE /students/me"
        studentProfileService -> studentRepository "Persist Étudiant (soft delete/anonymisation)"

        /***** BC3 Annonces *****/
        listingController -> listingService "CRUD + publish/archive"
        listingController -> listingQueryService "GET /listings (filters)"
        listingService -> listingRepository "Persist Annonce"
        listingQueryService -> geocodingClient "Optionnel : normalisation adresse"

        listingService -> eventPublisher "Publish DomainEvent: AnnoncePubliée / AnnonceModifiée"
        eventPublisher -> matchProjectionUpdater "Deliver events to Matching"

        /***** BC4 Matching *****/
        matchProjectionUpdater -> matchingService "Compute score/compat"
        matchProjectionUpdater -> matchReadRepository "Upsert projection"

        /***** BC5 Messagerie *****/
        conversationController -> conversationService "POST /conversations, GET /conversations, POST /conversations/{id}/close"
        conversationService -> conversationRepository "Persist Conversation"

        messageController -> messageService "POST /messages"
        messageService -> messageRepository "Persist Message"
        messageService -> eventPublisher "Publish DomainEvent: MessageEnvoyé"
        eventPublisher -> notificationOrchestrator "Route MessageEnvoyé"
        notificationOrchestrator -> pushDispatcher "Send push notification (optionnel)"
        notificationOrchestrator -> emailDispatcher "Send email notification (optionnel)"

        /***** BC6 Modération *****/
        moderationController -> moderationService "Admin moderation endpoints"
        moderationService -> reportRepository "Persist Signalement + audit"
        moderationService -> eventPublisher "Publish DomainEvent: SignalementTraité (optionnel)"

        /***** Repositories -> DB *****/
        accountRepository -> database "SQL (PostgreSQL)"
        studentRepository -> database "SQL (PostgreSQL)"
        listingRepository -> database "SQL (PostgreSQL)"
        conversationRepository -> database "SQL (PostgreSQL)"
        messageRepository -> database "SQL (PostgreSQL)"
        matchReadRepository -> database "SQL (PostgreSQL) ou store dédié"
        reportRepository -> database "SQL (PostgreSQL)"

        /***********************
         * C3 — Intégrations externes (ACL / adaptateurs)
         ***********************/
        mediaUploadService -> extObjectStorage "HTTPS: pre-signed URL (PUT)"
        mediaAccessService -> extObjectStorage "HTTPS: pre-signed URL (GET)"
        emailDispatcher -> extEmail "SMTP/HTTPS: send transactional email"
        geocodingClient -> extGeocoding "HTTPS: geocode/normalize address"
        eventPublisher -> extBroker "AMQP/SQS: publish events/jobs"
        pushDispatcher -> extPush "HTTPS: send push notifications"
      }

      /**************************************************************
       * ETAPE 6 — RELATIONS C2
       **************************************************************/
      student -> mobileApp "Utilise l'app (annonces, matching, messagerie)"
      mobileApp -> backendApi "HTTPS REST/JSON + Bearer JWT"
      admin -> backendApi "HTTPS Admin/Moderation endpoints"
    }

    /****************************************************************
     * ETAPE 7 — RELATIONS C1
     ****************************************************************/
    eduswitchlog -> extObjectStorage "Stores media via pre-signed URLs"
    eduswitchlog -> extEmail "Sends verification/transactional emails"
    eduswitchlog -> extGeocoding "Geocodes and normalizes addresses"
    eduswitchlog -> extBroker "Publishes/consumes async jobs/events"
    eduswitchlog -> extPush "Sends push notifications"
  }

  views {

    systemContext eduswitchlog "C1-SystemContext" "C1 - System Context (EduSwitchLog + externes)" {
      include student
      include admin
      include eduswitchlog
      include extObjectStorage
      include extEmail
      include extGeocoding
      include extBroker
      include extPush
      autolayout lr
    }

    container eduswitchlog "C2-Containers" "C2 - Containers (internes + dépendances externes)" {
      include student
      include admin
      include mobileApp
      include backendApi
      include database
      include extObjectStorage
      include extEmail
      include extGeocoding
      include extBroker
      include extPush
      autolayout lr
    }

    component backendApi "C3-BackendComponents" "C3 - Backend Components (aligné DDD : BC1..BC6)" {
      include *
      autolayout lr
    }

    styles {

      element "Person" {
        shape person
        background "white"
        color "black"
      }

      element "Software System" {
        background "darkblue"
        color "white"
      }

      element "Container" {
        background "steelblue"
        color "white"
      }

      element "Component" {
        shape component
        background "gold"
        color "black"
      }

      element "Database" {
        shape cylinder
        background "teal"
        color "white"
      }

      element "External" {
        background "lightgrey"
        color "black"
        stroke "black"
        opacity 85
      }

    }
  }
}
