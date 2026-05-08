-- ###############
--  FIX collation after Postgres Engine updates
-- ###############
ALTER DATABASE postgres REFRESH COLLATION VERSION;
ALTER DATABASE template1 REFRESH COLLATION VERSION;
