-- Script SQL pour créer la table des refresh tokens

-- Table pour stocker les refresh tokens JWT
CREATE TABLE IF NOT EXISTS auth_refresh_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    contributeur_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_refresh_token_contributeur 
        FOREIGN KEY (contributeur_id) 
        REFERENCES adm_contrib(id) 
        ON DELETE CASCADE
);

-- Index pour améliorer les performances des recherches
CREATE INDEX IF NOT EXISTS idx_refresh_token_token ON auth_refresh_token(token);
CREATE INDEX IF NOT EXISTS idx_refresh_token_contributeur ON auth_refresh_token(contributeur_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry ON auth_refresh_token(expiry_date);

-- Commentaires sur la table et les colonnes
COMMENT ON TABLE auth_refresh_token IS 'Table pour stocker les refresh tokens JWT pour l''authentification mobile';
COMMENT ON COLUMN auth_refresh_token.id IS 'Identifiant unique du refresh token';
COMMENT ON COLUMN auth_refresh_token.token IS 'Valeur unique du refresh token';
COMMENT ON COLUMN auth_refresh_token.contributeur_id IS 'Référence au contributeur propriétaire du token';
COMMENT ON COLUMN auth_refresh_token.expiry_date IS 'Date d''expiration du refresh token';
COMMENT ON COLUMN auth_refresh_token.created_at IS 'Date de création du token';
COMMENT ON COLUMN auth_refresh_token.revoked IS 'Indique si le token a été révoqué';
