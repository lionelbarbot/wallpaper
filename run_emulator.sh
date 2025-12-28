#!/bin/bash

# Script pour lancer l'émulateur Android
# Usage: ./run_emulator.sh [nom_avd]

# Définir les chemins Android SDK (ajuster si nécessaire)
ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
EMULATOR="$ANDROID_HOME/emulator/emulator"
ADB="$ANDROID_HOME/platform-tools/adb"

# Vérifier que les outils sont disponibles
if [ ! -f "$EMULATOR" ]; then
    echo "❌ Émulateur Android non trouvé à: $EMULATOR"
    echo "Veuillez installer Android Studio et configurer ANDROID_HOME"
    exit 1
fi

# Nom de l'AVD (par défaut ou argument)
AVD_NAME="${1:-WallPapier_Emulator}"

echo "🔍 Recherche de l'AVD: $AVD_NAME"

# Lister les AVD disponibles
AVAILABLE_AVDS=$("$EMULATOR" -list-avds 2>/dev/null)

if [ -z "$AVAILABLE_AVDS" ]; then
    echo "❌ Aucun AVD trouvé. Veuillez créer un AVD dans Android Studio:"
    echo "   1. Ouvrir Android Studio"
    echo "   2. Device Manager > Create Device"
    echo "   3. Choisir un appareil et une image système (API 35 requis)"
    exit 1
fi

# Vérifier si l'AVD existe
if echo "$AVAILABLE_AVDS" | grep -q "^$AVD_NAME$"; then
    echo "✅ AVD trouvé: $AVD_NAME"
else
    echo "⚠️  AVD '$AVD_NAME' non trouvé."
    echo "AVD disponibles:"
    echo "$AVAILABLE_AVDS"
    echo ""
    read -p "Utiliser le premier AVD disponible? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        AVD_NAME=$(echo "$AVAILABLE_AVDS" | head -n 1)
        echo "✅ Utilisation de: $AVD_NAME"
    else
        exit 1
    fi
fi

# Vérifier si un émulateur est déjà en cours d'exécution
if "$ADB" devices | grep -q "emulator"; then
    echo "⚠️  Un émulateur est déjà en cours d'exécution"
    echo "Appareils connectés:"
    "$ADB" devices
    read -p "Continuer quand même? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 0
    fi
fi

echo "🚀 Lancement de l'émulateur: $AVD_NAME"
echo "   (Fermez cette fenêtre pour arrêter l'émulateur)"

# Lancer l'émulateur
"$EMULATOR" -avd "$AVD_NAME" &

# Attendre que l'émulateur soit prêt
echo "⏳ Attente du démarrage de l'émulateur..."
sleep 5

# Vérifier périodiquement si l'émulateur est prêt
for i in {1..30}; do
    if "$ADB" devices | grep -q "device$"; then
        echo "✅ Émulateur prêt!"
        "$ADB" devices
        exit 0
    fi
    sleep 2
    echo -n "."
done

echo ""
echo "⚠️  L'émulateur prend plus de temps que prévu à démarrer"
echo "Vérifiez manuellement avec: adb devices"

