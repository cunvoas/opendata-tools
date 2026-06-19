/*
 Used to order by block that represent on 1 km²
 */
CREATE OR REPLACE FUNCTION transform_grid_id(input_text TEXT)
RETURNS TEXT AS $$
  SELECT SUBSTRING(input_text FROM 15 FOR 5)
      || SUBSTRING(input_text FROM 23 FOR 5);
$$ LANGUAGE SQL IMMUTABLE;