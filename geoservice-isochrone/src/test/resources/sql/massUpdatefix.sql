update public.parc_jardin 
 	set surface= ST_Area(contour, true)
	where surface is null and contour is not null;
	
update public.parc_jardin 
 	set coordonnee= ST_Centroid(contour, true)
	where coordonnee is null and contour is not null;	
	
	
update public.parc_jardin 
 	set coordonnee= ST_PointFromText( ST_asText( ST_Centroid(contour, true)))
	where coordonnee is null and contour is not null;	
	
	