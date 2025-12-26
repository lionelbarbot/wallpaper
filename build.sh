#!/bin/bash

# Script de build pour WallPapier
# Configure automatiquement JAVA_HOME et lance la compilation

set -e

# Configuration Java depuis Android Studio
ANDROID_STUDIO_APP="/Applications/Android Studio.app"
if [ -d "$ANDROID_STUDIO_APP" ]; then
    export JAVA_HOME="$ANDROID_STUDIO_APP/Contents/jbr/Contents/Home"
    export PATH="$JAVA_HOME/bin:$PATH"
else
    echo "⚠️  Android Studio non trouvé, utilisation de Java système"
fi

echo "🔧 Configuration:"
echo "   JAVA_HOME: $JAVA_HOME"
echo "   Java version:"
$JAVA_HOME/bin/java -version 2>&1 | head -3
echo ""

cd "$(dirname "$0")"

echo "🔨 Compilation de l'application..."
./gradlew assembleDebug "$@"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ BUILD SUCCESSFUL!"
    echo "📦 APK généré: app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "❌ BUILD FAILED"
    exit 1
fi

