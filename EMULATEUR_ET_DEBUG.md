# Guide d'Installation et Débogage avec Émulateur Android

Ce guide explique comment installer un émulateur Android et déboguer l'application WallPapier en direct.

## Prérequis

### 1. Installer Android Studio

1. Télécharger Android Studio depuis : https://developer.android.com/studio
2. Installer Android Studio sur macOS
3. Lancer Android Studio et suivre l'assistant de configuration initial

### 2. Installer le SDK Android

Dans Android Studio :
1. Ouvrir **Preferences** (⌘,)
2. Aller dans **Appearance & Behavior > System Settings > Android SDK**
3. Dans l'onglet **SDK Platforms**, installer :
   - Android 14.0 (API 34) - pour targetSdk
   - Android 8.0 (API 26) - pour minSdk
4. Dans l'onglet **SDK Tools**, s'assurer que sont installés :
   - Android SDK Build-Tools
   - Android Emulator
   - Android SDK Platform-Tools (contient ADB)
   - Intel x86 Emulator Accelerator (HAXM installer) - pour macOS avec Intel
   - Apple Silicon support (si Mac M1/M2/M3)

## Configuration de l'Émulateur

### Option 1 : Via Android Studio (Recommandé)

1. Ouvrir Android Studio
2. Cliquer sur **Device Manager** (icône de téléphone dans la barre latérale)
3. Cliquer sur **Create Device**
4. Choisir un appareil (ex: **Pixel 6** ou **Pixel 7**)
5. Cliquer sur **Next**
6. Choisir une image système :
   - **API 34** (Android 14) - recommandé pour correspondre au targetSdk
   - Télécharger si nécessaire
7. Cliquer sur **Next** puis **Finish**

### Option 2 : Via Ligne de Commande

```bash
# Définir les variables d'environnement (ajouter dans ~/.zshrc)
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/tools/bin

# Recharger le shell
source ~/.zshrc

# Lister les images système disponibles
sdkmanager --list | grep "system-images"

# Installer une image système (ex: API 34)
sdkmanager "system-images;android-34;google_apis;x86_64"

# Créer un AVD (Android Virtual Device)
avdmanager create avd -n WallPapier_Emulator -k "system-images;android-34;google_apis;x86_64" -d "pixel_6"
```

## Lancer l'Émulateur

### Via Android Studio

1. Ouvrir **Device Manager**
2. Cliquer sur le bouton **Play** (▶) à côté de l'émulateur créé

### Via Ligne de Commande

```bash
# Lister les AVD disponibles
emulator -list-avds

# Lancer l'émulateur
emulator -avd WallPapier_Emulator
```

## Ouvrir le Projet dans Android Studio

1. Ouvrir Android Studio
2. **File > Open**
3. Sélectionner le dossier `/Users/yapla/gitlab/wallpaper`
4. Attendre la synchronisation Gradle

## Déboguer l'Application

### Mode Debug Standard

1. Dans Android Studio, sélectionner l'émulateur dans la liste déroulante des appareils
2. Cliquer sur le bouton **Debug** (🐛) ou appuyer sur **⇧⌘D**
3. L'application sera installée et lancée en mode debug

### Débogage en Direct (Live Debugging)

1. **Lancer l'application en mode debug** (voir ci-dessus)
2. **Placer des breakpoints** :
   - Cliquer dans la marge à gauche du numéro de ligne pour ajouter un breakpoint (point rouge)
   - Exemple : dans `MainActivity.kt` ligne 45
3. **Utiliser les outils de débogage** :
   - **Variables** : voir les valeurs des variables en temps réel
   - **Call Stack** : voir la pile d'appels
   - **Evaluate Expression** : évaluer des expressions Kotlin pendant l'exécution
4. **Hot Reload avec Compose** :
   - Modifier le code Compose
   - Appuyer sur **⌘⇧R** pour recompiler et recharger

### Logcat pour le Debugging

1. Ouvrir l'onglet **Logcat** en bas de l'écran
2. Filtrer par :
   - Tag : `WallPapier` ou `MainActivity`
   - Niveau : Debug, Info, Warn, Error
3. Ajouter des logs dans le code :
```kotlin
import android.util.Log

Log.d("WallPapier", "Message de debug")
Log.i("WallPapier", "Message d'information")
Log.e("WallPapier", "Message d'erreur", exception)
```

## Scripts Utiles

Des scripts sont disponibles dans le projet pour faciliter le développement :

- `run_emulator.sh` : Lance l'émulateur
- `build_and_install.sh` : Compile et installe l'APK sur l'émulateur
- `debug.sh` : Lance l'application en mode debug

## Vérifier que l'Émulateur est Connecté

```bash
# Vérifier les appareils connectés
adb devices

# Devrait afficher quelque chose comme :
# List of devices attached
# emulator-5554    device
```

## Permissions sur l'Émulateur

L'émulateur permet de tester les permissions :
- **Stockage** : Accordée automatiquement sur l'émulateur
- **Accessibilité** : Nécessite activation manuelle dans Paramètres > Accessibilité
- **Fond d'écran** : Fonctionne normalement sur l'émulateur

## Problèmes Courants

### L'émulateur ne démarre pas

1. Vérifier que HAXM est installé (Mac Intel) ou utiliser ARM64 images (Mac Apple Silicon)
2. Vérifier les ressources système (RAM, CPU)
3. Redémarrer Android Studio

### ADB ne trouve pas l'émulateur

```bash
# Redémarrer le serveur ADB
adb kill-server
adb start-server
adb devices
```

### L'application ne se lance pas

1. Vérifier que l'émulateur est bien sélectionné dans Android Studio
2. Vérifier les logs dans Logcat pour les erreurs
3. Nettoyer et reconstruire le projet : **Build > Clean Project** puis **Build > Rebuild Project**

## Performance de l'Émulateur

Pour améliorer les performances :
1. Allouer plus de RAM à l'émulateur (dans AVD Manager > Edit > Show Advanced Settings)
2. Activer l'accélération matérielle
3. Utiliser des images système x86_64 plutôt que ARM (si Mac Intel)

## Débogage du Service d'Accessibilité

Pour tester le service d'accessibilité sur l'émulateur :
1. Lancer l'application
2. Aller dans **Paramètres Android > Accessibilité**
3. Trouver **WallPapier** dans la liste
4. Activer le service
5. Retourner à l'application pour voir l'état mis à jour

## Ressources

- [Documentation Android Emulator](https://developer.android.com/studio/run/emulator)
- [Débogage Android](https://developer.android.com/studio/debug)
- [Logcat](https://developer.android.com/studio/debug/am-logcat)

