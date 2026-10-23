-- Code INSEE composite pour les parcs Overpass s'étendant sur plusieurs communes.
-- Format : codes INSEE triés séparés par des virgules, ex : '59100,59350'
ALTER TABLE public.park_overpass ALTER COLUMN insee TYPE varchar(255);
