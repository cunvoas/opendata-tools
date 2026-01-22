const CHECK_INTERVAL = 4 * 60 * 60 * 1000; // 4 heures en ms

async function checkForUpdates() {
  try {
    const response = await fetch('/parcs-et-jardins/version.json');
    const data = await response.json();
    const lastVersion = localStorage.getItem('app-version');
    
    if (lastVersion && lastVersion !== data.version) {
      console.log('Nouvelle version détectée, rechargement...');
      localStorage.setItem('app-version', data.version); // Sauvegarder AVANT le reload
      window.location.reload(true); // true = hard refresh, ignore le cache
    } else {
      localStorage.setItem('app-version', data.version);
    }
  } catch (error) {
    console.error('Erreur lors de la vérification de version:', error);
  }
}

export function initVersionCheck() {
  checkForUpdates();
  setInterval(checkForUpdates, CHECK_INTERVAL);
}
