#!/bin/bash

# Script pour générer les icônes Android à partir de wallpapier-logo-fullsize.png
# Utilise sips (outil natif macOS) pour redimensionner les images

SOURCE_IMAGE="wallpapier-logo-fullsize.png"
BASE_DIR="app/src/main/res"

# Vérifier que l'image source existe
if [ ! -f "$SOURCE_IMAGE" ]; then
    echo "Erreur: $SOURCE_IMAGE introuvable!"
    exit 1
fi

echo "Génération des icônes normales..."

# Générer les icônes normales
generate_icon() {
    local folder=$1
    local size=$2
    local output_dir="$BASE_DIR/$folder"
    
    # Créer le dossier s'il n'existe pas
    mkdir -p "$output_dir"
    
    # Redimensionner l'image avec sips
    sips -z $size $size "$SOURCE_IMAGE" --out "$output_dir/ic_launcher.png" > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "✓ Créé: $output_dir/ic_launcher.png ($size x $size)"
    else
        echo "✗ Erreur lors de la création de $output_dir/ic_launcher.png"
    fi
}

# Générer toutes les tailles
generate_icon "mipmap-mdpi" 48
generate_icon "mipmap-hdpi" 72
generate_icon "mipmap-xhdpi" 96
generate_icon "mipmap-xxhdpi" 144
generate_icon "mipmap-xxxhdpi" 192

echo ""
echo "Génération des icônes round..."

# Générer les icônes round (copie de l'icône normale pour l'instant)
generate_round_icon() {
    local folder=$1
    local output_dir="$BASE_DIR/$folder"
    
    # Copier l'icône normale comme icône round
    if [ -f "$output_dir/ic_launcher.png" ]; then
        cp "$output_dir/ic_launcher.png" "$output_dir/ic_launcher_round.png"
        echo "✓ Créé: $output_dir/ic_launcher_round.png"
    fi
}

generate_round_icon "mipmap-mdpi"
generate_round_icon "mipmap-hdpi"
generate_round_icon "mipmap-xhdpi"
generate_round_icon "mipmap-xxhdpi"
generate_round_icon "mipmap-xxxhdpi"

echo ""
echo "✓ Toutes les icônes ont été générées!"
