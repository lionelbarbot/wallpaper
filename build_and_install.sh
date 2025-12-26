#!/bin/bash

# Script pour compiler et installer l'APK sur l'émulateur/appareil connecté
# Usage: ./build_and_install.sh [debug|release]

BUILD_TYPE="${1:-debug}"
ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$ANDROID_HOME/platform-tools/adb"

echo "🔨 Compilation de l'application en mode $BUILD_TYPE..."

# Compiler l'APK
if [ "$BUILD_TYPE" = "release" ]; then
    ./gradlew assembleRelease
    APK_PATH="app/build/outputs/apk/release/app-release.apk"
else
    ./gradlew assembleDebug
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
fi

if [ ! -f "$APK_PATH" ]; then
    echo "❌ Erreur lors de la compilation"
    exit 1
fi

echo "✅ Compilation réussie: $APK_PATH"

# Vérifier qu'un appareil est connecté
if [ ! -f "$ADB" ]; then
    echo "❌ ADB non trouvé à: $ADB"
    echo "Veuillez installer Android SDK Platform-Tools"
    exit 1
fi

DEVICES=$("$ADB" devices | grep -v "List" | grep "device$" | wc -l | tr -d ' ')

if [ "$DEVICES" -eq 0 ]; then
    echo "❌ Aucun appareil/émulateur connecté"
    echo "Connectez un appareil ou lancez un émulateur avec: ./run_emulator.sh"
    exit 1
fi

echo "📱 Appareils connectés: $DEVICES"
"$ADB" devices

# Désinstaller l'ancienne version si elle existe
echo "🗑️  Désinstallation de l'ancienne version..."
"$ADB" uninstall com.wallpaper 2>/dev/null

# Installer la nouvelle version
echo "📦 Installation de la nouvelle version..."
"$ADB" install "$APK_PATH"

if [ $? -eq 0 ]; then
    echo "✅ Installation réussie!"
    echo "🚀 Lancement de l'application..."
    "$ADB" shell am start -n com.wallpaper/.MainActivity
else
    echo "❌ Erreur lors de l'installation"
    exit 1
fi

