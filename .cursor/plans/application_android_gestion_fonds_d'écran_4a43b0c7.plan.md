---
name: Application Android Gestion Fonds d'Écran
overview: Développer une application Android native en Kotlin permettant de gérer des répertoires d'images avec dates d'activation, édition d'images intégrée, et changement automatique/manuel des fonds d'écran via des gestes tactiles.
todos:
  - id: setup_project
    content: Initialiser le projet Android avec Gradle, configurer les dépendances (Compose, Room, Coil, uCrop, WorkManager) et définir les permissions dans AndroidManifest.xml
    status: completed
  - id: data_layer
    content: Créer les entités Room (WallpaperFolder, WallpaperImage), les DAO et la base de données avec migrations
    status: completed
    dependencies:
      - setup_project
  - id: repository_layer
    content: Implémenter les repositories pour la gestion des répertoires et images, incluant la logique pour déterminer le répertoire actif selon les règles de récurrence
    status: completed
    dependencies:
      - data_layer
  - id: ui_screens
    content: "Développer les écrans Compose : HomeScreen (liste répertoires), FolderDetailScreen (liste images), EditFolderScreen (création/édition), ImageEditorScreen (édition avec uCrop), SettingsScreen (configuration intervalle)"
    status: completed
    dependencies:
      - repository_layer
  - id: wallpaper_service
    content: Implémenter WallpaperService pour appliquer les fonds d'écran sur verrouillage/accueil avec WallpaperManager API
    status: completed
    dependencies:
      - repository_layer
  - id: gesture_service
    content: Créer GestureService (AccessibilityService) pour détecter les double taps (image suivante) et quadruple taps (répertoire suivant) sur l'écran d'accueil
    status: completed
    dependencies:
      - wallpaper_service
  - id: worker_rotation
    content: Implémenter WallpaperWorker avec WorkManager pour la rotation automatique des images à intervalle configurable
    status: completed
    dependencies:
      - wallpaper_service
  - id: permissions_handling
    content: Gérer les permissions runtime (READ_MEDIA_IMAGES, SET_WALLPAPER) et la configuration Scoped Storage pour créer/accéder aux répertoires
    status: completed
    dependencies:
      - ui_screens
  - id: navigation
    content: Configurer la navigation entre les écrans avec Jetpack Navigation Compose
    status: completed
    dependencies:
      - ui_screens
  - id: recurrence_logic
    content: Implémenter la logique de calcul des dates d'activation avec récurrence (quotidienne, hebdomadaire, mensuelle) et détermination du répertoire actif
    status: completed
    dependencies:
      - repository_layer
---

# Plan de Développement - Application Android Gestion de Fonds d'Écran

## Architecture et Technologies

### Stack Technique

- **Langage** : Kotlin
- **UI Framework** : Jetpack Compose (compatible API 26+)
- **Architecture** : MVVM avec ViewModel et StateFlow
- **Base de données** : Room Database pour stocker les configurations
- **Gestion des images** : Coil pour le chargement, uCrop pour l'édition
- **Tâches en arrière-plan** : WorkManager pour la rotation automatique
- **Permissions** : Scoped Storage (Android 10+) avec MediaStore API
- **Gestion des fonds d'écran** : WallpaperManager API
- **Détection des gestes** : AccessibilityService pour capturer les taps

### Structure du Projet

```javascript
app/
├── src/main/
│   ├── java/com/wallpaper/
│   │   ├── data/
│   │   │   ├── database/          # Room entities et DAO
│   │   │   ├── repository/        # Repositories pour les données
│   │   │   └── model/             # Modèles de données
│   │   ├── domain/
│   │   │   └── usecase/           # Cas d'usage métier
│   │   ├── ui/
│   │   │   ├── theme/             # Thème Compose
│   │   │   ├── screens/           # Écrans principaux
│   │   │   └── components/        # Composants réutilisables
│   │   ├── service/
│   │   │   ├── WallpaperService.kt      # Service pour changer les fonds
│   │   │   ├── GestureService.kt        # Service pour détecter les taps
│   │   │   └── WallpaperWorker.kt       # Worker pour rotation auto
│   │   └── MainActivity.kt
│   └── res/
│       ├── values/                # Strings, colors, themes
│       └── xml/                   # Configurations (permissions, etc.)
```



## Fonctionnalités Principales

### 1. Gestion des Répertoires

- **Création** : Créer des répertoires dans le stockage de l'app (Scoped Storage)
- **Liste** : Afficher tous les répertoires avec leur statut (actif/inactif)
- **Édition** : Modifier le nom, les dates d'activation (récurrence), et les écrans ciblés
- **Suppression** : Supprimer un répertoire et ses images

### 2. Gestion des Images

- **Ajout** : Sélectionner des images depuis la galerie ou prendre des photos
- **Liste** : Afficher les images d'un répertoire en grille
- **Édition** : Zoom, crop, resize avec uCrop (écrase l'original)
- **Suppression** : Supprimer une image d'un répertoire

### 3. Configuration des Dates d'Activation

- **Récurrence** : Définir des règles de récurrence (quotidienne, hebdomadaire, mensuelle)
- **Plages horaires** : Optionnellement définir des heures d'activation
- **Statut actif** : Déterminer automatiquement quel répertoire est actif selon la date/heure

### 4. Application des Fonds d'Écran

- **Cible** : Écran de verrouillage, écran d'accueil, ou les deux
- **Rotation automatique** : Changer l'image à intervalle configurable
- **Rotation manuelle** : Double tap pour image suivante, quadruple tap pour répertoire suivant

### 5. Service en Arrière-plan

- **WallpaperService** : Service pour appliquer les fonds d'écran
- **GestureService** : AccessibilityService pour détecter les taps sur l'écran d'accueil
- **WallpaperWorker** : Worker périodique pour rotation automatique

## Implémentation Détaillée

### Phase 1 : Configuration du Projet

- Initialiser le projet Android avec Gradle
- Configurer les dépendances (Compose, Room, Coil, uCrop, WorkManager)
- Définir les permissions dans `AndroidManifest.xml`
- Configurer le thème Compose de base

### Phase 2 : Modèle de Données (Room)

- **Entity** : `WallpaperFolder` (id, name, recurrenceRule, targetScreens, isActive)
- **Entity** : `WallpaperImage` (id, folderId, filePath, displayOrder)
- **DAO** : Requêtes pour CRUD sur les entités
- **Database** : Instance Room avec migrations

### Phase 3 : Repository Layer

- `WallpaperFolderRepository` : Gestion des répertoires
- `WallpaperImageRepository` : Gestion des images
- `WallpaperServiceRepository` : Logique métier pour déterminer le répertoire actif

### Phase 4 : UI - Écrans Principaux

- **HomeScreen** : Liste des répertoires avec statut actif
- **FolderDetailScreen** : Liste des images d'un répertoire
- **EditFolderScreen** : Création/édition d'un répertoire (nom, récurrence, écrans)
- **ImageEditorScreen** : Édition d'image avec uCrop
- **SettingsScreen** : Configuration de l'intervalle de rotation automatique

### Phase 5 : Services et Workers

- **WallpaperService** : Utiliser `WallpaperManager` pour appliquer les fonds
- **GestureService** : AccessibilityService pour détecter les taps (nécessite permission spéciale)
- **WallpaperWorker** : Worker périodique qui vérifie le répertoire actif et change l'image

### Phase 6 : Gestion des Permissions

- Demander les permissions au runtime (READ_MEDIA_IMAGES, SET_WALLPAPER)
- Gérer Scoped Storage pour créer/accéder aux répertoires d'images
- Demander la permission d'accessibilité pour GestureService

### Phase 7 : Logique Métier

- Calculer le répertoire actif selon les règles de récurrence
- Gérer la rotation des images dans un répertoire
- Implémenter la logique de double/quadruple tap

## Fichiers Clés à Créer

### Configuration

- `build.gradle.kts` (app) : Dépendances et configuration
- `AndroidManifest.xml` : Permissions et déclarations de services
- `strings.xml` : Ressources de texte

### Modèles de Données

- `data/database/entities/WallpaperFolder.kt`
- `data/database/entities/WallpaperImage.kt`
- `data/database/dao/WallpaperDao.kt`
- `data/database/WallpaperDatabase.kt`

### Repositories

- `data/repository/WallpaperFolderRepository.kt`
- `data/repository/WallpaperImageRepository.kt`

### Services

- `service/WallpaperService.kt`
- `service/GestureService.kt`
- `service/WallpaperWorker.kt`

### UI

- `ui/screens/HomeScreen.kt`
- `ui/screens/FolderDetailScreen.kt`
- `ui/screens/EditFolderScreen.kt`
- `ui/screens/ImageEditorScreen.kt`
- `ui/theme/Color.kt`, `Theme.kt`

### Navigation

- `ui/navigation/NavGraph.kt`

## Permissions Requises

1. `READ_MEDIA_IMAGES` : Lire les images de la galerie
2. `SET_WALLPAPER` : Changer les fonds d'écran
3. `BIND_ACCESSIBILITY_SERVICE` : Pour détecter les taps (permission système)
4. Stockage : Utiliser Scoped Storage pour créer les répertoires

## Points d'Attention

- **Scoped Storage** : Android 10+ limite l'accès aux fichiers. Utiliser MediaStore API ou le répertoire privé de l'app
- **AccessibilityService** : Nécessite une activation manuelle par l'utilisateur dans les paramètres système
- **WorkManager** : Pour les tâches périodiques, utiliser PeriodicWorkRequest avec contraintes
- **Performance** : Optimiser le chargement des images avec Coil et le caching
- **Batterie** : Minimiser l'impact avec des intervalles raisonnables et des optimisations

## Tests à Prévoir