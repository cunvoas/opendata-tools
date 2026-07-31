CREATE TABLE IF NOT EXISTS manual_eval_work (
    id BIGSERIAL PRIMARY KEY,
    id_meta BIGINT REFERENCES manual_park_proposal_meta(id),
    id_inspire VARCHAR(30) NOT NULL,
    is_dense BOOLEAN,
    local_pop NUMERIC(12,2),
    accessing_pop NUMERIC(12,2),
    accessing_surf NUMERIC(12,2),
    surf_per_capita NUMERIC(12,2),
    miss_surf NUMERIC(12,2),
    new_accessing_surf NUMERIC(12,2),
    new_surf_per_capita NUMERIC(12,2),
    new_miss_surf NUMERIC(12,2)
);
