#!/usr/bin/env python3
"""
Script pour générer les icônes Android à partir de wallpapier-logo-fullsize.png
Nécessite Pillow: pip install Pillow
"""

from PIL import Image
import os

# Tailles d'icônes pour Android (en dp, converties en px pour mdpi)
sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192,
}

# Tailles pour les icônes round (mêmes tailles)
round_sizes = sizes.copy()

def create_icon(source_path, output_dir, size, is_round=False):
    """Crée une icône redimensionnée"""
    try:
        # Créer le dossier de sortie s'il n'existe pas
        os.makedirs(output_dir, exist_ok=True)
        
        # Ouvrir l'image source
        img = Image.open(source_path)
        
        # Convertir en RGBA si nécessaire
        if img.mode != 'RGBA':
            img = img.convert('RGBA')
        
        # Redimensionner en gardant les proportions
        img.thumbnail((size, size), Image.Resampling.LANCZOS)
        
        # Créer une nouvelle image avec la taille exacte et fond transparent
        new_img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        
        # Centrer l'image redimensionnée
        x_offset = (size - img.width) // 2
        y_offset = (size - img.height) // 2
        new_img.paste(img, (x_offset, y_offset), img)
        
        # Pour les icônes round, créer un masque circulaire
        if is_round:
            mask = Image.new('L', (size, size), 0)
            from PIL import ImageDraw
            draw = ImageDraw.Draw(mask)
            draw.ellipse((0, 0, size, size), fill=255)
            new_img.putalpha(mask)
        
        # Sauvegarder
        filename = 'ic_launcher_round.png' if is_round else 'ic_launcher.png'
        output_path = os.path.join(output_dir, filename)
        new_img.save(output_path, 'PNG')
        print(f"✓ Créé: {output_path}")
        
    except Exception as e:
        print(f"✗ Erreur pour {output_dir}: {e}")

def main():
    source_image = 'wallpapier-logo-fullsize.png'
    
    if not os.path.exists(source_image):
        print(f"Erreur: {source_image} introuvable!")
        return
    
    base_dir = 'app/src/main/res'
    
    # Générer les icônes normales
    print("Génération des icônes normales...")
    for folder, size in sizes.items():
        output_dir = os.path.join(base_dir, folder)
        create_icon(source_image, output_dir, size, is_round=False)
    
    # Générer les icônes round
    print("\nGénération des icônes round...")
    for folder, size in round_sizes.items():
        output_dir = os.path.join(base_dir, folder)
        create_icon(source_image, output_dir, size, is_round=True)
    
    print("\n✓ Toutes les icônes ont été générées!")

if __name__ == '__main__':
    main()

