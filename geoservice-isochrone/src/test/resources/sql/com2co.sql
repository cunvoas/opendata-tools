--https://fr.wikipedia.org/wiki/Liste_des_intercommunalit%C3%A9s_du_Nord

/*
INSERT INTO public.adm_com2commune(id, name, id_region)	VALUES (2, 'Communauté urbaine de Dunkerque', 9);
UPDATE public.city set id_comm2co=2 where insee_code in ('59155','59183','59272','59131','59271','59668','59340','59107','59273', '59588','59016','59576','59094','59359','59260','59159','59532');
	
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (3, 'CA Valenciennes Métropole', 9);
UPDATE public.city set id_comm2co=3 where insee_code in ('59616','59613','59610','59591','59559','59557','59544','59530','59515','59505','59484','59480','59479','59475','59471','59459','59447','59444','59407','59383','59369','59301','59253','59221','59215','59166','59160','59153','59112','59079','59032','59027','59019','59606','59014');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (4, 'CA de la Porte du Hainaut', 9);
UPDATE public.city set id_comm2co=4 where insee_code in ('59652','59651','59645','59632','59603','59594','59589','59554','59526','59519','59511','59504','59491','59446','59440','59434','59429','59418','59403','59393','59391','59387','59361','59348','59335','59564','59313','59302','59297','59292','59288','59285','59284','59238','59207','59205','59192','59179','59172','59144','59114','59109','59100','59092','59064','59002','59038');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (5, 'CA du Douaisis', 9);
UPDATE public.city set id_comm2co=5 where insee_code in ('59654','59620','59569','59513','59509','59489','59486','59379','59336','59334','59329','59327','59280','59276','59263','59254','59239','59234','59228','59224','59222','59214','59211','59199','59178','59170','59165','59156','59126','59117','59115','59028','59026','59007','59015');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (6, 'CA Maubeuge Val de Sambre', 9);
UPDATE public.city set id_comm2co=6 where insee_code in ('59627','59618','59617','59556','59543','59542','59514','59495','59483','59467','59442','59439','59424','59406','59392','59385','59370','59365','59351','59344','59324','59291','59264','59231','59230','59225','59190','59188','59187','59157','59151','59142','59104','59103','59101','59076','59072','59068','59058','59041','59033','59003','59021');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (7, 'CC de Flandre Intérieure', 9);
UPDATE public.city set id_comm2co=7 where insee_code in ('59669','59667','59662','59655','59634','59615','59590','59587','59582','59581','59580','59578','59577','59568','59536','59546','59535','59516','59497','59469','59454','59453','59443','59436','59431','59423','59416','59401','59399','59366','59180','59318','59308','59295','59282','59262','59237','59189','59184','59135','59120','59119','59091','59087','59086','59084','59073','59054','59018','59043');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (8, 'CC Pévèle-Carembault', 9);
UPDATE public.city set id_comm2co=8 where insee_code in ('59638','59630','59600','59592','59586','59551','59466','59462','59452','59449','59435','59419','59411','59408','59398','59364','59330','59427','59304','59266','59258','59197','59168','59158','59150','59145','59129','59124','59123','59105','59096','59080','59071','59042','59034','59029','59004','59022');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (9, 'CA de Cambrai', 9);
UPDATE public.city set id_comm2co=9 where insee_code in ('59635','59625','59623','59622','59597','59595','59593','59567','59552','59521','59520','59502','59500','59492','59488','59476','59455','59438','59432','59428','59422','59405','59389','59377','59341','59517','59322','59312','59300','59294','59269','59267','59255','59244','59236','59216','59219','59209','59206','59176','59167','59161','59141','59125','59122','59121','59097','59085','59049','59048','59047','59039','59023','59001','59010');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (10, 'CC Cœur d''Ostrevent', 9);
UPDATE public.city set id_comm2co=10 where insee_code in ('59642','59637','59629','59596','59574','59501','59456','59414','59409','59390','59375','59354','59345','59314','59227','59203','59185','59113','59008','59024');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (11, 'CA du Caudrésis - Catésis', 9);
UPDATE public.city set id_comm2co=11 where insee_code in ('59631','59624','59604','59547','59545','59533','59531','59528','59498','59496','59485','59465','59450','59430','59413','59412','59395','59394','59382','59372','59349','59136','59274','59321','59311','59287','59243','59213','59191','59171','59149','59140','59139','59138','59137','59132','59118','59108','59102','59081','59075','59074','59063','59059','59037','59055');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (12, 'CC des Hauts de Flandre', 9);
UPDATE public.city set id_comm2co=12 where insee_code in ('59666','59665','59664','59663','59657','59647','59641','59628','59605','59579','59570','59539','59538','59499','59478','59463','59448','59433','59402','59397','59358','59338','59337','59326','59319','59309','59307','59305','59210','59200','59182','59162','59130','59111','59110','59089','59083','59082','59046','59067');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (13, 'CC du Pays de Mormal', 9);
UPDATE public.city set id_comm2co=13 where insee_code in ('59640','59639','59626','59619','59607','59584','59565','59549','59548','59518','59503','59494','59473','59472','59468','59464','59451','59441','59425','59396','59384','59381','59363','59353','59481','59223','59331','59357','59232','59325','59323','59315','59310','59296','59283','59277','59265','59259','59251','59246','59242','59217','59194','59164','59116','59099','59077','59070','59065','59057','59053','59006','59031');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (14, 'CC Flandre Lys', 9);
UPDATE public.city set id_comm2co=14 where insee_code in ('62736','59400','62502','62491','59293','62338','59268','59212');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (15, 'CC du Cœur de l''Avesnois', 9);
UPDATE public.city set id_comm2co=15 where insee_code in ('59649','59583','59573','59572','59563','59562','59555','59534','59529','59525','59493','59490','59474','59461','59374','59347','59342','59333','59306','59290','59270','59241','59240','59233','59226','59218','59186','59181','59177','59175','59174','59169','59148','59147','59134','59093','59078','59066','59062','59061','59050','59036','59035');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (16, 'CC du Sud Avesnois', 9);
UPDATE public.city set id_comm2co=16 where insee_code in ('59661','59659','59633','59601','59445','59420','59261','59229','59198','59045','59249','59012');

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (17, 'CC du Pays Solesmois', 9);
UPDATE public.city set id_comm2co=17 where insee_code in ('59614','59612','59608','59575','59558','59541','59537','59506','59415','59289','59204','59127','59069','59571','59060');
*/



INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (1, 'Métropole Européenne de Lille', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (2, 'Communauté urbaine de Dunkerque', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (3, 'CA Valenciennes Métropole', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (4, 'CA de la Porte du Hainaut', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (5, 'CA du Douaisis', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (6, 'CA Maubeuge Val de Sambre', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (7, 'CC de Flandre Intérieure', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (8, 'CC Pévèle-Carembault', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (9, 'CA de Cambrai', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (10, 'CC Cœur d''Ostrevent', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (11, 'CA du Caudrésis - Catésis', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (12, 'CC des Hauts de Flandre', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (13, 'CC du Pays de Mormal', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (14, 'CC Flandre Lys', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (15, 'CC du Cœur de l''Avesnois', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (16, 'CC du Sud Avesnois', 9);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (17, 'CC du Pays Solesmois', 9);
COMMIT;

UPDATE public.city set id_comm2co=1 where insee_code in ('59011','59013','59017','59025','59044','59052','90030','59090','59098','59106','59128','59133','59146','59152','59163','59173','59670','59193','59195','59208','59252','59256','59257','59275','59279','59281','59286','59299','59303','59317','59320','59051','59368','59328','59332','59371','59339','59343','59346','59350','59352','59356','59360','59388','59421','59457','59470','59477','59507','59508','59512','59550','59553','59560','59566','59585','59598','59599','59602','59609','59611','59636','59643','59646','59648','59650','59653','59658','59660','59202','59143','59009','59527','59367','59378','59056','59196','59247','59250','59410','59201','59522','59220','59437','59523','59524','59005','59316','59088','59426','59458','59482','59278','59386','59487','59656');
UPDATE public.city set id_comm2co=2 where insee_code in ('59155','59183','59272','59131','59271','59668','59340','59107','59273', '59588','59016','59576','59094','59359','59260','59159','59532');
UPDATE public.city set id_comm2co=3 where insee_code in ('59616','59613','59610','59591','59559','59557','59544','59530','59515','59505','59484','59480','59479','59475','59471','59459','59447','59444','59407','59383','59369','59301','59253','59221','59215','59166','59160','59153','59112','59079','59032','59027','59019','59606','59014');
UPDATE public.city set id_comm2co=4 where insee_code in ('59652','59651','59645','59632','59603','59594','59589','59554','59526','59519','59511','59504','59491','59446','59440','59434','59429','59418','59403','59393','59391','59387','59361','59348','59335','59564','59313','59302','59297','59292','59288','59285','59284','59238','59207','59205','59192','59179','59172','59144','59114','59109','59100','59092','59064','59002','59038');
UPDATE public.city set id_comm2co=5 where insee_code in ('59654','59620','59569','59513','59509','59489','59486','59379','59336','59334','59329','59327','59280','59276','59263','59254','59239','59234','59228','59224','59222','59214','59211','59199','59178','59170','59165','59156','59126','59117','59115','59028','59026','59007','59015');
UPDATE public.city set id_comm2co=6 where insee_code in ('59627','59618','59617','59556','59543','59542','59514','59495','59483','59467','59442','59439','59424','59406','59392','59385','59370','59365','59351','59344','59324','59291','59264','59231','59230','59225','59190','59188','59187','59157','59151','59142','59104','59103','59101','59076','59072','59068','59058','59041','59033','59003','59021');
UPDATE public.city set id_comm2co=7 where insee_code in ('59669','59667','59662','59655','59634','59615','59590','59587','59582','59581','59580','59578','59577','59568','59536','59546','59535','59516','59497','59469','59454','59453','59443','59436','59431','59423','59416','59401','59399','59366','59180','59318','59308','59295','59282','59262','59237','59189','59184','59135','59120','59119','59091','59087','59086','59084','59073','59054','59018','59043');
UPDATE public.city set id_comm2co=8 where insee_code in ('59638','59630','59600','59592','59586','59551','59466','59462','59452','59449','59435','59419','59411','59408','59398','59364','59330','59427','59304','59266','59258','59197','59168','59158','59150','59145','59129','59124','59123','59105','59096','59080','59071','59042','59034','59029','59004','59022');
UPDATE public.city set id_comm2co=9 where insee_code in ('59635','59625','59623','59622','59597','59595','59593','59567','59552','59521','59520','59502','59500','59492','59488','59476','59455','59438','59432','59428','59422','59405','59389','59377','59341','59517','59322','59312','59300','59294','59269','59267','59255','59244','59236','59216','59219','59209','59206','59176','59167','59161','59141','59125','59122','59121','59097','59085','59049','59048','59047','59039','59023','59001','59010');
UPDATE public.city set id_comm2co=10 where insee_code in ('59642','59637','59629','59596','59574','59501','59456','59414','59409','59390','59375','59354','59345','59314','59227','59203','59185','59113','59008','59024');
UPDATE public.city set id_comm2co=11 where insee_code in ('59631','59624','59604','59547','59545','59533','59531','59528','59498','59496','59485','59465','59450','59430','59413','59412','59395','59394','59382','59372','59349','59136','59274','59321','59311','59287','59243','59213','59191','59171','59149','59140','59139','59138','59137','59132','59118','59108','59102','59081','59075','59074','59063','59059','59037','59055');
UPDATE public.city set id_comm2co=12 where insee_code in ('59666','59665','59664','59663','59657','59647','59641','59628','59605','59579','59570','59539','59538','59499','59478','59463','59448','59433','59402','59397','59358','59338','59337','59326','59319','59309','59307','59305','59210','59200','59182','59162','59130','59111','59110','59089','59083','59082','59046','59067');
UPDATE public.city set id_comm2co=13 where insee_code in ('59640','59639','59626','59619','59607','59584','59565','59549','59548','59518','59503','59494','59473','59472','59468','59464','59451','59441','59425','59396','59384','59381','59363','59353','59481','59223','59331','59357','59232','59325','59323','59315','59310','59296','59283','59277','59265','59259','59251','59246','59242','59217','59194','59164','59116','59099','59077','59070','59065','59057','59053','59006','59031');
UPDATE public.city set id_comm2co=14 where insee_code in ('62736','59400','62502','62491','59293','62338','59268','59212');
UPDATE public.city set id_comm2co=15 where insee_code in ('59649','59583','59573','59572','59563','59562','59555','59534','59529','59525','59493','59490','59474','59461','59374','59347','59342','59333','59306','59290','59270','59241','59240','59233','59226','59218','59186','59181','59177','59175','59174','59169','59148','59147','59134','59093','59078','59066','59062','59061','59050','59036','59035');
UPDATE public.city set id_comm2co=16 where insee_code in ('59661','59659','59633','59601','59445','59420','59261','59229','59198','59045','59249','59012');
UPDATE public.city set id_comm2co=17 where insee_code in ('59614','59612','59608','59575','59558','59541','59537','59506','59415','59289','59204','59127','59069','59571','59060');
COMMIT;

INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (18, 'CA Seine-Eure', 14);
COMMIT;
UPDATE public.city set id_comm2co=18 where insee_code in ('27375','27003','27005','27008','27013','27014','27015','27025','27053','27124','27142','27191','27168','27180','27184','27188','27196','27249','27275','27313','27321','27322','27330','27332','27335','27348','27351','27365','27382','27386','27394','27403','27456','27458','27469','27471','27474','27483','27517','27529','27534','27537','27539','27545','27553','27589','27598','27599','27616','27623','27624','27412','27676','27666','27022','27701','27528','27691','27697','27700');
COMMIT;


INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (19, 'Nantes Métropole', 17);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (20, 'Métropole de Lyon', 1);
INSERT INTO public.adm_com2commune(id, name, id_region) VALUES (21, 'Toulouse Métropole', 16);
COMMIT;

UPDATE public.city set id_comm2co=19 where insee_code in ('44109','44009','44018','44020','44024','44026','44035','44047','44074','44094','44101','44114','44120','44143','44150','44162','44166','44171','44190','44172','44194','44198','44204','44215');
UPDATE public.city set id_comm2co=20 where insee_code in ('69123','69003','69029','69033','69034','69040','69044','69046','69271','69063','69273','69068','69069','69071','69072','69275','69081','69276','69085','69087','69088','69089','69278','69091','69096','69100','69279','69116','69117','69127','69282','69283','69284','69142','69143','69149','69153','69163','69286','69168','69191','69194','69202','69199','69204','69205','69207','69290','69233','69292','69293','69296','69244','69250','69256','69259','69260','69266');
UPDATE public.city set id_comm2co=21 where insee_code in ('31555','31003','31022','31032','31044','31053','31056','31069','31088','31091','31116','31149','31150','31157','31163','31182','31184','31186','31205','31230','31282','31293','31351','31352','31355','31389','31417','31418','31445','31467','31488','31490','31506','31541','31557','31561','31588');
COMMIT;




