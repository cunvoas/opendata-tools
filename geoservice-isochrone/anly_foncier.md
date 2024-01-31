
curl 'https://wxs.ign.fr/calcul/look4/user/search?indices=portail_completion&types=toponyme%2Cposition%2Cw3w&method=prefix&nb=1&match%5Bfulltext%5D=41%20rue%20jean%20jaures%2029260%20helle' \
  -H 'authority: wxs.ign.fr' \
  -H 'accept: */*' \
  -H 'accept-language: fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7' \
  -H 'origin: https://www.geoportail.gouv.fr' \
  -H 'referer: https://www.geoportail.gouv.fr/' \
  -H 'sec-ch-ua: "Google Chrome";v="111", "Not(A:Brand";v="8", "Chromium";v="111"' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'sec-ch-ua-platform: "Linux"' \
  -H 'sec-fetch-dest: empty' \
  -H 'sec-fetch-mode: cors' \
  -H 'sec-fetch-site: cross-site' \
  -H 'user-agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36' \
  --compressed
  
  
  
  resp:
  {"type":"FeatureCollection","features":[{"type":"Feature","properties":{"nyme":"","postalCode":"","city":"D127PR2U (41)","nature":"pr","importance":"7","_type":"toponyme","_score":0.2958005583333333,"_id":"238077"},"geometry":{"type":"Point","coordinates":[1.174006945201015,47.29974236150358]}}]}
  
  
  
|||||||000001|03/01/2022|Vente|55000,00|13||RUE|2280|DE LA LIBERTE|1000|BOURG-EN-BRESSE|01|53||AM|102||7|24,10|||||||||1|2|Appartement||24|1|||


  13 RUE DE LA LIBERTE, 01000 BOURG-EN-BRESSE
  
  curl 'https://wxs.ign.fr/calcul/geoportail/geocodage/rest/0.1/completion?type=StreetAddress%2CPositionOfInterest&maximumResponses=1&text=13%20RUE%20DE%20LA%20LIBERTE%2C%2001000%20BOURG-EN-BRESSE' --compressed


{"status":"OK","results":[{"country":"StreetAddress","city":"Bourg-en-Bresse","oldcity":null,"zipcode":"01000","street":"13 rue de la liberte","metropole":true,"kind":null,"fulltext":"13 rue de la liberte, 01000 Bourg-en-Bresse","classification":7,"x":5.218608,"y":46.197901}]}

  
  
  5.094097637643327
