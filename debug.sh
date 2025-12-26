#!/bin/bash

# Script pour lancer l'application en mode debug avec logs
# Usage: ./debug.sh

ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$ANDROID_HOME/platform-tools/adb"

echo "🐛 Mode Debug - WallPapier"
echo ""

# Vérifier qu'un appareil est connecté
DEVICES=$("$ADB" devices | grep -v "List" | grep "device$" | wc -l | tr -d ' ')

if [ "$DEVICES" -eq 0 ]; then
    echo "❌ Aucun appareil/émulateur connecté"
    echo "Lancez un émulateur avec: ./run_emulator.sh"
    exit 1
fi

echo "📱 Appareils connectés:"
"$ADB" devices
echo ""

# Compiler et installer en mode debug
echo "🔨 Compilation et installation..."
./build_and_install.sh debug

if [ $? -ne 0 ]; then
    exit 1
fi

echo ""
echo "📊 Affichage des logs (Ctrl+C pour arrêter)..."
echo "   Filtre: WallPapier, MainActivity, GestureService"
echo ""

# Afficher les logs avec filtres
"$ADB" logcat -c  # Nettoyer les logs
"$ADB" logcat | grep -E "WallPapier|MainActivity|GestureService|AndroidRuntime"

