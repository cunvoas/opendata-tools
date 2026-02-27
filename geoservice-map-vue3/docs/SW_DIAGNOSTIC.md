# 🔧 Diagnostic Service Worker - Guide Complet

## État de génération du Service Worker ✅

Les fichiers PWA sont **correctement générés** :

```
dist/
├── sw.js                      (1.8 kB) ✅
├── registerSW.js              (168 B)  ✅
├── workbox-354287e6.js        (22 kB)  ✅
├── manifest.webmanifest       (784 B)  ✅
└── index.html                 ✅ (avec tag <script> registerSW.js)
```

## Pourquoi le Service Worker n'apparaît pas

### 1. **Problème le plus courant : HTTPS obligatoire en production**

Les Service Workers ne fonctionnent qu'en :
- ✅ `https://` (production)
- ✅ `http://localhost:*` (développement local)
- ✅ `http://127.0.0.1:*` (développement local)
- ❌ `http://example.com` (autre domaine - ne fonctionne pas)
- ❌ `http://192.168.x.x` (IP locale - ne fonctionne pas toujours)

### 2. **Cache du navigateur bloque la détection**

Si vous aviez un ancien site sans SW :
- Les fichiers peuvent être cachés pendant des jours
- Solution : `Shift+Refresh` ou vider le cache

### 3. **Le navigateur ne supporte pas les SW**

Certains navigateurs/modes :
- Mode privée : Parfois bloqué
- Vieux navigateurs : IE, anciens Chrome/Firefox

## ✅ Vérification pas à pas

### Étape 1 : Servir en local correctement

**Option A : npm run preview (recommandé)**
```bash
npm run build
npm run preview
# Accédez à http://localhost:4173
```

**Option B : npm run dev (développement)**
```bash
npm run dev
# Accédez à http://localhost:5173
```

### Étape 2 : Ouvrir les DevTools

1. Appuyez sur **F12** (ou Ctrl+Shift+I)
2. Allez dans l'onglet **Application** (Chrome/Edge/Brave) ou **Storage** (Firefox)
3. Dans le menu de gauche, trouvez **Service Workers**

### Étape 3 : Vérifier le rapport de Service Worker

#### ✅ Le Service Worker est enregistré :
- Vous verrez : `sw.js — http://localhost:4173/parcs-et-jardins/sw.js`
- Status : "running" (vert)
- Affichage de la date d'enregistrement

#### ❌ Le Service Worker ne s'affiche pas :

**Vérifier la console :**
1. Onglet **Console** dans DevTools
2. Cherchez les messages d'erreur
3. Erreurs courants :

```javascript
// Erreur : HTTPS obligatoire en prod
"ServiceWorkerError: Failed to register a ServiceWorker"

// Erreur : Scope invalide
"SecurityError: The operation is insecure"

// Erreur : Fichier introuvable
"NetworkError: The operation is not supported"
```

## 🔍 Diagnostic avancé

### 1. Vérifier que registerSW.js est chargé

Console DevTools :
```javascript
// Devrait retourner true
'serviceWorker' in navigator
```

### 2. Vérifier que sw.js est accessible

Ouvrez dans votre navigateur :
```
http://localhost:4173/parcs-et-jardins/sw.js
```

Vous devriez voir du code JavaScript (minifié).

### 3. Vérifier le manifest

Ouvrez dans votre navigateur :
```
http://localhost:4173/parcs-et-jardins/manifest.webmanifest
```

Vous devriez voir du JSON valide.

### 4. Console JavaScript pour enregistrement manuel

```javascript
// Dans la console DevTools :
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/parcs-et-jardins/sw.js')
    .then(reg => console.log('SW enregistré:', reg))
    .catch(err => console.error('Erreur SW:', err));
}
```

## 🚀 Solutions par scénario

### Scénario 1 : Pas de SW en production (HTTPS)

**Cause probable** : Le site n'est pas en HTTPS

**Solution** :
1. Assurez-vous que le serveur est en HTTPS
2. Vérifiez que le certificat SSL est valide
3. Videz le cache du navigateur
4. Attendez 24h (les SW sont cachés longtemps)

### Scénario 2 : SW enregistré mais pas d'installation

**Cause probable** : Les icônes manquent

**Solution** :
```bash
# Générer les icônes
npm install --save-dev sharp
node generate-icons.js votre-logo.png
npm run build
```

### Scénario 3 : SW s'enregistre puis désapparaît

**Cause probable** : Erreur lors de la génération du precache

**Vérifier** :
1. Console DevTools > **Application** > **Cache Storage**
2. Cherchez les caches : `api-cache`, `tiles-cache`
3. S'ils sont vides : Erreur de configuration

### Scénario 4 : Mode privée/Incognito

**Le SW ne fonctionne souvent pas en mode privé**

**Solution** : Testez en mode normal (non-privé)

## 📊 Vérifier l'état du cache

Une fois le SW enregistré :

1. **Application > Cache Storage** : Doit afficher les caches
2. **Application > Service Workers** : Doit afficher l'entrée active
3. **Application > Manifest** : Doit afficher les métadonnées PWA

## 🆘 Si rien ne fonctionne

### Débogage complet :

```bash
# 1. Nettoyer complètement
rm -rf node_modules dist
npm install
npm run build
npm run preview

# 2. Forcer un hard refresh dans le navigateur
# Ctrl+Shift+Delete (Windows/Linux)
# Cmd+Shift+Delete (Mac)

# 3. Dans DevTools, onglet Application
# Cliquer sur "Clear site data" pour vider tout le cache

# 4. Recharger la page
```

### Vérifier la configuration :

```bash
# Vérifier que vite.config.js a le plugin PWA
cat vite.config.js | grep -A 5 "VitePWA"

# Vérifier que registerSW.js est dans index.html généré
grep registerSW dist/index.html
```

## 📱 Test sur mobile

### iOS (Safari)

1. Ouvrir le site en Safari
2. Appuyer sur le bouton "Partage" (carré avec flèche)
3. Sélectionner "Sur l'écran d'accueil"
4. Le SW s'enregistrera en arrière-plan

**Note** : iOS n'affiche pas le détail du SW dans les DevTools

### Android (Chrome)

1. Ouvrir le site en Chrome
2. Appuyer sur le menu (⋮)
3. Sélectionner "Installer l'application"
4. Ouvrir DevTools (Ctrl+Shift+I)
5. Onglet **Application** > **Service Workers**

## 🎯 Checklist finale

Avant de déclarer le SW "non trouvé", vérifiez :

- [ ] Vous êtes en HTTPS (production) ou http://localhost (dev)
- [ ] La page charge sans erreur
- [ ] registerSW.js est dans l'HTML généré
- [ ] Vous avez actualisé la page (Ctrl+F5)
- [ ] Vous avez vidé le cache du navigateur
- [ ] DevTools est ouvert dans le bon onglet (Application)
- [ ] Vous testez sur un navigateur moderne (Chrome 40+, Firefox 44+, Safari 11.1+)
- [ ] Vous n'êtes pas en mode privé/incognito
- [ ] Les icônes sont présentes dans `public/icons/`

## 📞 Ressources

- [MDN Service Workers](https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API)
- [Google: Service Workers](https://developers.google.com/web/tools/service-worker-libraries)
- [Can I use: Service Workers](https://caniuse.com/serviceworkers)
- [Vite PWA: Troubleshooting](https://vite-pwa-org.netlify.app/troubleshooting.html)

---

**Status des fichiers générés** : ✅ **PARFAIT**
**Le Service Worker n'est pas le problème - c'est la configuration du test**
