CREATE OR REPLACE FUNCTION getColorDensite(zoneDense boolean, densite float) RETURNS varchar AS $$

        DECLARE
            color varchar = '';
            i_onecolor integer;
            s_onecolor varchar;
            
        BEGIN
            
        --RAISE NOTICE 'zoneDense=%', zoneDense;
        --RAISE NOTICE 'densite=%', densite;
        
            -- zone dense
            if zoneDense AND densite<10  then
                i_onecolor = 123+ROUND(densite)*10;
                s_onecolor = to_hex(i_onecolor);
                
               --RAISE NOTICE 'cas 1.1 (s_onecolor=%)', s_onecolor;
                color = '#' || s_onecolor || s_onecolor || s_onecolor;
           
            elseif zoneDense AND densite<12  then
            RAISE NOTICE 'cas 1.2';
                color = '#9ee88f';
            elseif zoneDense AND densite>=12  then
            --RAISE NOTICE 'cas 1.3';
              color = '#1a9900';
            
            -- zone non dense
            elseif not zoneDense AND densite<25  then
                i_onecolor = 123+ROUND(densite)*5;
                s_onecolor = to_hex(i_onecolor);
               --RAISE NOTICE 'cas 2.1 (s_onecolor=%)', s_onecolor;
                color = '#' || s_onecolor || s_onecolor || s_onecolor;
            
            elseif not zoneDense AND densite<45  then
                --RAISE NOTICE 'cas 2.2';
              color = '#9ee88f';
            elseif not zoneDense AND densite>=45  then
                --RAISE NOTICE 'cas 2.2';
              color = '#1a9900';
           
           else
              --RAISE NOTICE 'cas 3';
              color = '#7c7c7c';
            end if;
                
 
                RETURN color;
        END;
$$ LANGUAGE plpgsql;
-- select getColorDensite(true, 12)