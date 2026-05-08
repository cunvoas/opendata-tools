

CREATE TABLE filosofi_load (
    idcar_200m VARCHAR(30) not null,
    i_est_200 numeric(1),
    idcar_1km VARCHAR(31) not null,
    i_est_1km numeric(1),
    idcar_nat VARCHAR(36) not null,
    groupe numeric(16, 4),
    ind numeric(16, 4),
    men_1ind numeric(16, 4),
    men_5ind numeric(16, 4),
    men_prop numeric(16, 4),
    men_fmp numeric(16, 4),
    ind_snv numeric(16, 4),
    men_surf numeric(16, 4),
    men_coll numeric(16, 4),
    men_mais numeric(16, 4),
    log_av45 numeric(16, 4),
    log_45_70 numeric(16, 4),
    log_70_90 numeric(16, 4),
    log_ap90 numeric(16, 4),
    log_inc numeric(16, 4),
    log_soc numeric(16, 4),
    ind_0_3 numeric(16, 4),
    ind_4_5 numeric(16, 4),
    ind_6_10 numeric(16, 4),
    ind_11_17 numeric(16, 4),
    ind_18_24 numeric(16, 4),
    ind_25_39 numeric(16, 4),
    ind_40_54 numeric(16, 4),
    ind_55_64 numeric(16, 4),
    ind_65_79 numeric(16, 4),
    ind_80p numeric(16, 4),
    ind_inc numeric(16, 4),
    men_pauv numeric(16, 4),
    men numeric(16, 4),
    lcog_geo VARCHAR(50)

);

CREATE TABLE filosofi_200m (
    annee numeric(4) not null,
    
    idcar_200m VARCHAR(30) not null,
    i_est_200 numeric(1),
    idcar_1km VARCHAR(31) not null,
    i_est_1km numeric(1),
    idcar_nat VARCHAR(36) not null,
    groupe numeric(16, 4),
    ind numeric(16, 4),
    men_1ind numeric(16, 4),
    men_5ind numeric(16, 4),
    men_prop numeric(16, 4),
    men_fmp numeric(16, 4),
    ind_snv numeric(16, 4),
    men_surf numeric(16, 4),
    men_coll numeric(16, 4),
    men_mais numeric(16, 4),
    log_av45 numeric(16, 4),
    log_45_70 numeric(16, 4),
    log_70_90 numeric(16, 4),
    log_ap90 numeric(16, 4),
    log_inc numeric(16, 4),
    log_soc numeric(16, 4),
    ind_0_3 numeric(16, 4),
    ind_4_5 numeric(16, 4),
    ind_6_10 numeric(16, 4),
    ind_11_17 numeric(16, 4),
    ind_18_24 numeric(16, 4),
    ind_25_39 numeric(16, 4),
    ind_40_54 numeric(16, 4),
    ind_55_64 numeric(16, 4),
    ind_65_79 numeric(16, 4),
    ind_80p numeric(16, 4),
    ind_inc numeric(16, 4),
    men_pauv numeric(16, 4),
    men numeric(16, 4),
    lcog_geo VARCHAR(50),
    
    PRIMARY KEY (annee, idcar_200m)

) PARTiTiON BY LIST (annee);


CREATE TABLE filosofi_200m_2015 PARTITION OF filosofi_200m FOR VALUES IN (2015);
CREATE TABLE filosofi_200m_2017 PARTITION OF filosofi_200m FOR VALUES IN (2017);
CREATE TABLE filosofi_200m_2019 PARTITION OF filosofi_200m FOR VALUES IN (2019);

truncate public.filosofi_load;
copy public.filosofi_load (idcar_200m, i_est_200, idcar_1km, i_est_1km, idcar_nat, groupe, ind, men_1ind, men_5ind, men_prop, men_fmp, ind_snv, men_surf, men_coll, men_mais, log_av45, log_45_70, log_70_90, log_ap90, log_inc, log_soc, ind_0_3, ind_4_5, ind_6_10, ind_11_17, ind_18_24, ind_25_39, ind_40_54, ind_55_64, ind_65_79, ind_80p, ind_inc, men_pauv, men, lcog_geo) FROM '/work/PERSO/ASSO/data/Filosofi2017_carreaux_200m_csv/Filosofi2017_carreaux_200m_met.csv' DELIMITER ',' CSV HEADER ENCODING 'LATIN1' FORCE NOT NULL idcar_200m, idcar_1km, idcar_nat ;
insert into filosofi_200m select 2017, * from filosofi_load;


truncate public.filosofi_load;
copy public.filosofi_load (idcar_200m,idcar_1km,idcar_nat,i_est_200,i_est_1km,lcog_geo,ind,men,men_pauv,men_1ind,men_5ind,men_prop,men_fmp,ind_snv,men_surf,men_coll,men_mais,log_av45,log_45_70,log_70_90,log_ap90,log_inc,log_soc,ind_0_3,ind_4_5,ind_6_10,ind_11_17,ind_18_24,ind_25_39,ind_40_54,ind_55_64,ind_65_79,ind_80p,ind_inc) FROM '/tmp/carreaux_200m_met.csv' DELIMITER ',' CSV HEADER ENCODING 'LATIN1' FORCE NOT NULL idcar_200m, idcar_1km, idcar_nat ;
insert into filosofi_200m select 2019, * from filosofi_load;




insert into filosofi_200m (annee, idcar_200m, idcar_1km, idcar_nat, 
    groupe, lcog_geo,ind,
    men,men_pauv,men_1ind,men_5ind,men_prop,men_fmp,ind_snv,men_surf,men_coll,men_mais,log_av45,log_45_70,log_70_90,log_ap90,log_inc,log_soc,ind_0_3,ind_4_5,ind_6_10,ind_11_17,ind_18_24,ind_25_39,ind_40_54,ind_55_64,ind_65_79,ind_80p,ind_inc,i_est_1km)

    select 2015 as annee, id_inspire as idcar_200m, id_carr1km as idcar_1km,id_carr_n as idcar_nat,
    groupe,depcom as lcog_geo,ind,
    men,men_pauv,men_1ind,men_5ind,men_prop,men_fmp,ind_snv,men_surf,men_coll,men_mais,log_av45,log_45_70,log_70_90,log_ap90,log_inc,log_soc,ind_0_3,ind_4_5,ind_6_10,ind_11_17,ind_18_24,ind_25_39,ind_40_54,ind_55_64,ind_65_79,ind_80p,ind_inc,i_est_1km
    from filosofi_200_2015
;
    
    
    
    
    
    
    
    
    
    
    
    
