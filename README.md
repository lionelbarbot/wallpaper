# WallPapier - Gestionnaire de Fonds d'Écran Android

Une application Android pour gérer et changer automatiquement les fonds d'écran avec des fonctionnalités de récurrence, d'édition d'images et de gestes tactiles.

## Fonctionnalités

### Gestion des Répertoires
- Créer, modifier et supprimer des répertoires d'images
- Définir des règles de récurrence par sélection de jours (lundi à dimanche)
- Choisir les écrans ciblés (verrouillage, accueil, ou les deux)
- Activation automatique selon les jours configurés
- Intervalle de rotation configurable (en minutes)
- Changement automatique au déverrouillage de l'appareil

### Gestion des Images
- Ajouter des images depuis la galerie
- Éditer les images (zoom, crop, rotation) avec l'éditeur intégré
- Les images éditées écrasent les originales
- Affichage en grille des images d'un répertoire

### Rotation Automatique
- Changement automatique des fonds d'écran à intervalle configurable
- Utilise WorkManager pour les tâches en arrière-plan
- Respecte les règles de récurrence des répertoires

### Gestes Tactiles
- **Double tap** sur l'écran d'accueil : passe à l'image suivante du répertoire actif
- Nécessite l'activation du service d'accessibilité dans les paramètres système

## Architecture Technique

- **Langage** : Kotlin
- **UI** : Jetpack Compose
- **Architecture** : MVVM avec ViewModel et StateFlow
- **Base de données** : Room Database
- **Chargement d'images** : Coil
- **Édition d'images** : Composant Compose custom
- **Tâches en arrière-plan** : WorkManager
- **API Fonds d'écran** : WallpaperManager
- **Détection de gestes** : AccessibilityService

## Prérequis

- Android Studio Hedgehog ou plus récent
- JDK 17
- Android SDK avec API 35 (Android 15) minimum

## Installation

1. Cloner le repository
```bash
git clone <repository-url>
cd wallpaper
```

2. Ouvrir le projet dans Android Studio

3. Synchroniser les dépendances Gradle

4. Compiler et installer sur un appareil ou émulateur
```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Utilisation

### Première Utilisation

1. **Permissions** : L'application demandera automatiquement la permission de lecture des images au premier lancement

2. **Service d'Accessibilité** : Pour utiliser les gestes tactiles :
   - Aller dans Paramètres > Accessibilité
   - Trouver "Wallpaper" dans la liste des services
   - Activer le service

### Créer un Répertoire

1. Cliquer sur le bouton "+" sur l'écran d'accueil
2. Entrer un nom pour le répertoire
3. Sélectionner les jours de récurrence (lundi à dimanche)
4. Choisir les écrans ciblés (verrouillage, accueil, ou les deux)
5. Optionnellement configurer l'intervalle de rotation (en minutes)
6. Optionnellement activer le changement au déverrouillage
7. Enregistrer

### Ajouter des Images

1. Ouvrir un répertoire
2. Cliquer sur le bouton "+"
3. Sélectionner une image depuis la galerie
4. L'image sera automatiquement copiée dans le répertoire de l'application

### Éditer une Image

1. Cliquer sur une image dans un répertoire
2. L'éditeur d'image s'ouvrira automatiquement
3. Ajuster le zoom, le crop et la taille
4. Sauvegarder (l'image originale sera écrasée)

## Permissions Requises

1. **READ_MEDIA_IMAGES** (Android 13+)
   - Pour lire les images de la galerie

2. **SET_WALLPAPER**
   - Pour changer les fonds d'écran (permission normale, accordée automatiquement)

3. **BIND_ACCESSIBILITY_SERVICE**
   - Pour détecter les gestes tactiles (nécessite activation manuelle dans les paramètres système)

## Notes Techniques

- **Scoped Storage** : L'application utilise Scoped Storage pour créer et gérer les répertoires d'images dans son espace privé
- **WorkManager** : Les tâches périodiques utilisent WorkManager avec des contraintes pour optimiser la batterie
- **AccessibilityService** : Le service de gestes nécessite une activation manuelle pour des raisons de sécurité
- **Éditeur d'images** : Composant Compose natif avec support des gestes (zoom, pan, rotation) et recadrage

## Licence

[À définir]
