#!/usr/bin/env node

/**
 * Script pour générer les icônes PWA à partir d'une image source
 * Usage: node generate-icons.js <source-image.png>
 * 
 * Prérequis: npm install --save-dev sharp
 */

const sharp = require('sharp');
const path = require('path');
const fs = require('fs');

const sourceImage = process.argv[2];

if (!sourceImage) {
  console.error('Usage: node generate-icons.js <source-image.png>');
  console.error('Example: node generate-icons.js logo.png');
  process.exit(1);
}

if (!fs.existsSync(sourceImage)) {
  console.error(`Error: File not found: ${sourceImage}`);
  process.exit(1);
}

const iconsDir = path.join(__dirname, 'public', 'icons');

// Créer le dossier s'il n'existe pas
if (!fs.existsSync(iconsDir)) {
  fs.mkdirSync(iconsDir, { recursive: true });
}

const sizes = [
  { name: 'icon-192x192.png', size: 192 },
  { name: 'icon-512x512.png', size: 512 },
  { name: 'icon-maskable-192x192.png', size: 192 },
  { name: 'icon-maskable-512x512.png', size: 512 }
];

async function generateIcons() {
  try {
    console.log('Generating PWA icons...');
    
    for (const icon of sizes) {
      const outputPath = path.join(iconsDir, icon.name);
      
      await sharp(sourceImage)
        .resize(icon.size, icon.size, {
          fit: 'contain',
          background: { r: 255, g: 255, b: 255, alpha: 1 }
        })
        .png()
        .toFile(outputPath);
      
      console.log(`✓ Generated: ${icon.name} (${icon.size}×${icon.size})`);
    }
    
    console.log('\n✓ All icons generated successfully!');
    console.log(`Icons saved to: ${iconsDir}`);
    
  } catch (error) {
    console.error('Error generating icons:', error.message);
    process.exit(1);
  }
}

generateIcons();
