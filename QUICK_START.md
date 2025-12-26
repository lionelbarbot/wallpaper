# Guide de Démarrage Rapide - WallPapier

## ✅ Compilation Réussie !

L'application a été compilée avec succès. Voici comment la lancer sur un émulateur.

## Méthode 1 : Avec Android Studio (Recommandé)

1. **Ouvrir le projet dans Android Studio**
   ```bash
   open -a "Android Studio" /Users/yapla/gitlab/wallpaper
   ```

2. **Créer un émulateur** (si pas déjà fait)
   - Device Manager > Create Device
   - Choisir Pixel 6 ou Pixel 7
   - Image système : API 34 (Android 14)
   - Cliquer sur Finish

3. **Lancer l'émulateur**
   - Device Manager > Cliquer sur ▶ à côté de l'émulateur

4. **Lancer l'application**
   - Sélectionner l'émulateur dans la liste déroulante
   - Cliquer sur le bouton Run (▶) ou Debug (🐛)

## Méthode 2 : Via Ligne de Commande

### 1. Compiler l'application

```bash
cd /Users/yapla/gitlab/wallpaper
./build.sh
```

### 2. Lancer un émulateur

```bash
# Option A : Via Android Studio (ouvrir Device Manager et cliquer sur ▶)
# Option B : Via ligne de commande
./run_emulator.sh
```

### 3. Installer l'APK sur l'émulateur

```bash
./build_and_install.sh debug
```

### 4. Voir les logs en temps réel

```bash
./debug.sh
```

## Vérifier que l'émulateur est connecté

```bash
# Vérifier les appareils connectés
$HOME/Library/Android/sdk/platform-tools/adb devices

# Devrait afficher :
# List of devices attached
# emulator-5554    device
```

## Problèmes Courants

### "ADB non trouvé"
```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### "Aucun appareil connecté"
1. Vérifier que l'émulateur est lancé
2. Attendre quelques secondes que l'émulateur démarre complètement
3. Relancer `adb devices`

### "Permission denied" sur les scripts
```bash
chmod +x build.sh build_and_install.sh run_emulator.sh debug.sh
```

## Fichiers Générés

- **APK Debug** : `app/build/outputs/apk/debug/app-debug.apk`
- **APK Release** : `app/build/outputs/apk/release/app-release.apk` (après `./build.sh release`)

## Prochaines Étapes

1. ✅ Compiler l'application → **FAIT**
2. Lancer un émulateur Android
3. Installer l'APK sur l'émulateur
4. Tester l'application
5. Activer le service d'accessibilité dans les paramètres pour tester les gestes

## Commandes Utiles

```bash
# Compiler uniquement
./build.sh

# Compiler et installer
./build_and_install.sh debug

# Lancer l'émulateur
./run_emulator.sh

# Voir les logs
./debug.sh

# Nettoyer le projet
./gradlew clean

# Reconstruire complètement
./gradlew clean build
```

