CREATE INDEX idx_200_insee_pop
    ON public.carre200onlyshape USING btree
    (code_insee ASC NULLS LAST, avec_pop ASC NULLS LAST)
    WITH (deduplicate_items=True)
;
