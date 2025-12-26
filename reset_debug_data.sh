#!/bin/bash

# Script pour réinitialiser les données de débogage
# Supprime les SharedPreferences pour forcer la recréation du répertoire de test

echo "🔄 Réinitialisation des données de débogage..."

# Vérifier qu'un appareil est connecté
ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
ADB="$ANDROID_HOME/platform-tools/adb"

if [ ! -f "$ADB" ]; then
    echo "❌ ADB non trouvé"
    exit 1
fi

DEVICES=$("$ADB" devices | grep -v "List" | grep "device$" | wc -l | tr -d ' ')

if [ "$DEVICES" -eq 0 ]; then
    echo "❌ Aucun appareil/émulateur connecté"
    exit 1
fi

echo "📱 Suppression des SharedPreferences..."
"$ADB" shell run-as com.wallpaper rm -f /data/data/com.wallpaper/shared_prefs/wallpaper_prefs.xml

echo "🗑️  Suppression de la base de données..."
"$ADB" shell run-as com.wallpaper rm -rf /data/data/com.wallpaper/databases/wallpaper_database*

echo "✅ Données réinitialisées !"
echo "   Au prochain lancement de l'application, le répertoire de test sera recréé."

