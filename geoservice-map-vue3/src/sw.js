import { precacheAndRoute } from 'workbox-precaching';
import { registerRoute } from 'workbox-routing';
import { NetworkFirst, CacheFirst } from 'workbox-strategies';
import { ExpirationPlugin } from 'workbox-expiration';

precacheAndRoute(self.__WB_MANIFEST);

const MATOMO_BASE = 'https://asso.autmel.ovh/audience';

// ——— matomo proxy ———
// Intercepts same-origin /matomo.js and /matomo.php,
// forwards to remote server. Brave/Firefox ETP see first-party requests.
self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);
  if (url.hostname !== self.location.hostname) return;

  let remoteUrl = null;
  if (url.pathname.endsWith('/matomo.js')) {
    remoteUrl = MATOMO_BASE + '/matomo.js';
  } else if (url.pathname.endsWith('/matomo.php')) {
    remoteUrl = MATOMO_BASE + '/matomo.php' + url.search;
  }
  if (!remoteUrl) return;

  console.log(`[SW/MATOMO] proxy ${event.request.method} ${url.pathname} → ${remoteUrl}`);
  event.respondWith(
    fetch(remoteUrl, {
      method: event.request.method,
      headers: event.request.headers,
      body: event.request.method === 'POST' ? event.request : undefined
    }).then((response) => {
      console.log(`[SW/MATOMO] ${response.status} ${url.pathname}`);
      return response;
    }).catch((err) => {
      console.error(`[SW/MATOMO] FAILED ${url.pathname}`, err);
      return new Response('', { status: 502 });
    })
  );
});

// ——— runtime caching ———
registerRoute(
  ({ url }) => /^https:\/\/asso\.autmel\.ovh\/audience\/matomo\.js$/i.test(url.href),
  new NetworkFirst({
    cacheName: 'matomo-tracker-remote',
    networkTimeoutSeconds: 5,
    plugins: [
      new ExpirationPlugin({ maxEntries: 1, maxAgeSeconds: 86400 })
    ]
  })
);

registerRoute(
  ({ url }) => /^https:\/\/api\..*/i.test(url.href),
  new NetworkFirst({
    cacheName: 'api-cache',
    plugins: [
      new ExpirationPlugin({ maxEntries: 100, maxAgeSeconds: 60 * 60 * 24 * 7 })
    ]
  })
);

registerRoute(
  ({ url }) => /^https:\/\/tile\..*/i.test(url.href),
  new CacheFirst({
    cacheName: 'tiles-cache',
    plugins: [
      new ExpirationPlugin({ maxEntries: 500, maxAgeSeconds: 60 * 60 * 24 * 30 })
    ]
  })
);
