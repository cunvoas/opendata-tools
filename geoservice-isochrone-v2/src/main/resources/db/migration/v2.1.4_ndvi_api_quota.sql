-- Compteur mensuel d'appels à l'API Statistical CDSE (quota gratuit : 10 000 requêtes/mois).
CREATE TABLE IF NOT EXISTS public.ndvi_api_quota (
    quota_month VARCHAR(7) PRIMARY KEY,
    request_count INTEGER NOT NULL DEFAULT 0
);
