CREATE TABLE IF NOT EXISTS manual_park_proposal_meta (
    id BIGSERIAL PRIMARY KEY,
    annee INTEGER,
    insee VARCHAR(5),
    type_algo VARCHAR(30) DEFAULT 'MANUAL',
    number_of_parks INTEGER DEFAULT 0,
    total_surface_of_parks INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS manual_park_proposal (
    id BIGSERIAL PRIMARY KEY,
    id_meta BIGINT REFERENCES manual_park_proposal_meta(id),
    name VARCHAR(255),
    mode VARCHAR(10) NOT NULL,
    centre GEOMETRY(Point, 2154),
    contour GEOMETRY(Geometry, 2154),
    surface NUMERIC(12,2),
    description TEXT,
    created_date TIMESTAMP DEFAULT NOW()
);
