CREATE INDEX idx_200_insee_pop
    ON public.carre200onlyshape USING btree (
        code_insee ASC NULLS LAST, 
        avec_pop ASC NULLS LAST)
    WITH (deduplicate_items=True);

CREATE INDEX idx_pj_geom
  ON public.parc_jardin
  USING GIST (contour, coordonnee);
    
:  index row requires 23760 bytes, maximum size is 8191 
ALTER TABLE public.parc_jardin
  ALTER COLUMN coordonnee
   TYPE geometry(Point, 4326)
    USING ST_SetSRID(coordonnee, 4326);