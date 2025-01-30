<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity6">
<head th:insert="~{fragments/fragment-head :: head}"></head>

<body class="sb-nav-fixed">
	<div th:insert="~{fragments/fragment-navUp :: navUp}"></div>
	<div id="layoutSidenav">
		<div th:insert="~{fragments/fragment-navLeft :: navLeft}"></div>


		<div id="layoutSidenav_content">
			<main>

				<div class="container">

<p class="d-inline-flex gap-1">
	<button id="btnNew" onclick="manageCreateDisabled()" type="button" class="btn btn-outline-success" th:text="#{park.new.create}">Create</button>
	<button id="btnUpdate" onclick="manageUpdateDisabled()" type="button" class="btn btn-outline-success" th:text="#{park.new.update}">Update</button>

	<button id="btnSave" onclick="savePark()" type="button" class="btn btn-outline-warning" th:text="#{park.new.save}">save</button>
	<div id="saveParkSpinner" class="spinner-border text-success" role="status" style="display:none;">
	  <span class="visually-hidden">Loading...</span>
	</div>

  <a th:text="#{park.new.help}" class="btn btn-danger" style="position:absolute;right:10px" data-bs-toggle="collapse" href="#collapseExample" role="button" aria-expanded="false" aria-controls="collapseExample">
    help
  </a>
</p>
<div class="collapse" id="collapseExample">
  <div class="card card-body" th:utext="#{park.new.helpDetail}">
		helpDetail
  </div>
</div>


<div class="row row-cols-1 row-cols-md-12  ">
	<div class="container-fluid px-4">
		<h4 class="mt-12" th:text="#{park.title}">title</h4>
	</div>
	
     <div class="row input-group border border-dark rounded-3">
      	<h6 class="col-12" th:text="#{parc.edit.chooseCity}">select city</h6>
         <div class="col-3">
             <div class="card" style="text-align: center">
               	<!-- region -->
				<form id="formSelectRegion" th:action="@{/mvc/park/new/region}" th:object="${newPark}" method="post" onchange="submit();">
					<div class="form-floating">
						<select class="form-select" id="idRegion" name="idRegion" th:field="*{idRegion}">
							<option value="" selected disabled th:text="#{entrance.region.placeholder}">Choose here</option>
							<option th:each="region : ${regions}" 
								th:value="${region.id}" 
								th:text="${region.name}" 
								th:selected="${region.id==idRegion}"></option>
						</select>
						<label for="idRegion" th:text="#{entrance.region}">region</label>
					</div>
				</form>
             </div>
         </div>
         <div class="col-3">
             <div class="card" style="text-align: center">
                	<!-- comm2co -->
					<form id="formSelectComm2co" th:action="@{/mvc/park/new/commDeCo}" th:object="${newPark}" method="post" onchange="submit();">
					<fieldset id="fsComm2co" disabled="disabled">
						<div class="form-floating">
							<select class="form-select" id="idCommunauteDeCommunes" name="idCommunauteDeCommunes" th:field="*{idCommunauteDeCommunes}">
								<option value="" th:text="#{entrance.communateDeCommunes.placeholder}">Choose here</option>
								<option th:each="commDeCo : ${communautesDeCommunes}" th:value="${commDeCo.id}" th:text="${commDeCo.name}" th:selected="${commDeCo.id==idCommunauteDeCommunes}"></option>
							</select>
							<label for="idCommunauteDeCommunes" th:text="#{entrance.communateDeCommunes}">Comm de Co</label>
							<input type="hidden" id="idRegion" name="idRegion" th:field="*{idRegion}" />
						</div>
					</fieldset>
					</form>
             </div>
         </div>
         <div class="col-6">
             <div class="card" style="text-align: center">
             	<!-- commune -->
				<form id="formSelectCity" th:action="@{/mvc/park/new/city}" method="POST" th:object="${newPark}">
				<fieldset id="fsCity" disabled="disabled">
					<div class="form-floating">
						<input class="form-select" list="communes" id="idCommune-filter" name="idCommune-filter" /> <label for="idCommune-filter" th:text="#{entrance.commune}">ville</label>
						<datalist id="communes" class="form-label">
							<option th:each="commune : ${communes}" th:value="${commune.name}" th:attr="data-value=${commune.id}" th:label="${commune.name}">
						</datalist>
						<input type="hidden" id="idCommune" name="idCommune" th:field="*{idCommune}" />
						<input type="hidden" id="idRegion" name="idRegion" th:field="*{idRegion}" />
						<input type="hidden" id="idCommunauteDeCommunes" name="idCommunauteDeCommunes" th:field="*{idCommunauteDeCommunes}" />
					</div>
				</fieldset>
				</form>
             </div>
         </div>
     </div>
     

<form id="formNewPark" th:action="@{/mvc/park/new/save}" th:object="${newPark}" method="post" enctype="multipart/form-data">
      <div class="row input-group border border-dark rounded-3">
      		<h6 class="col-12" th:text="#{parc.edit.locateOnMap}">locate on map</h6>
            <div class="col-5">
	           <div class="card" style="text-align: center">
	                      <label for="srcAdress" th:text="#{parc.edit.searchAdress}">adress</label>
	                      <select class="form-select select2-single" id="srcAdress" name="srcAdress">
	                      </select>
	           </div>
	       </div>
            <div class="col-2">
                <div class="card border" style="text-align: center">
					<button type="button" class="btn btn-secondary" th:text="#{entrance.map.locateMe}" onclick="locateMe()" >locate me</button>
                </div>
            </div>
            <div class="col-3">
                <div class="card border" style="text-align: center">
                    <label for="fileupload" th:text="#{entrance.map.upload}">upload photo</label>
					  <input class="form-control form-control-sm" id="fileupload" type="file" name="fileupload" onchange="return fileValidation()" />
                </div>
            </div>
            <div class="col-2">
                <div class="card border" style="text-align: center">
                    <button type="button" class="btn btn-secondary" th:text="#{entrance.map.uploadBtn}" onclick="uploadFile()">upload me</button>
                </div>
            </div>
      </div>
      

<span class="row input-group border border-dark rounded-3">

    <div class="col-4">
        <fieldset id="fsEditPark" disabled="disabled">
        <input type="hidden" id="action_mode" name="action_mode" />
        <input type="hidden" id="idRegion" name="idRegion" th:field="*{idRegion}" />
        <input type="hidden" id="idCommunauteDeCommunes" name="idCommunauteDeCommunes" th:field="*{idCommunauteDeCommunes}" />
        <input type="hidden" id="idCommune" name="idCommune" th:field="*{idCommune}" />
        <input type="hidden" id="idPark" name="idPark" th:field="*{idPark}" />
        <input type="hidden" id="id" name="id" th:field="*{id}" />
        <input type="hidden" id="mapLat" name=mapLat th:field="*{mapLat}"/>
        <input type="hidden" id="mapLng" name="mapLng" th:field="*{mapLng}"/>
						
        <span style="display: none;"><!-- TODO hidden at the end -->
        <input type="text" id="hadGeometry" name="hadGeometry" th:field="*{hadGeometry}" />
        <input type="text" id="sGeometry" name="sGeometry" th:field="*{sGeometry}" />
        <input type="text" id="etat" name="etat" th:field="*{etat}" />
        <input type="text" id="etatAction" name="etatAction" th:field="*{etatAction}" />
        </span>
        
		<div class="row">
			<div class="container-fluid px-4">
				<h6 th:text="#{entrance.park.title}">title</h6>
			</div>
			<div class="col">
				<div class="card border" style="text-align: center">
					<div id="blkName" class="form-floating">
						<input class="form-control" type="text" id="name" name="name" th:field="*{name}" /> <label class="form-label" for="name" th:text="#{entrance.park}">parc</label>
					</div>
				</div>
				<div class="card border" style="text-align: center">
					<div class="form-floating">
						<input class="form-control" type="text" id="quartier" name="quartier" th:field="*{quartier}" /> <label class="form-label" for="quartier" th:text="#{entrance.park.block}">block</label>
					</div>
				</div>
				<div class="card border" style="text-align: center">
					<div class="form-floating">
						<input class="form-control" type="date" id="dateDebut" name="dateDebut" th:field="*{dateDebut}" />
						<label class="form-label" for="dateDebut" th:text="#{park.dateDebut}">dateDebut</label>
					</div>
				</div>
				<div class="card border" style="text-align: center">
					<div class="form-floating">
						<input class="form-control" type="date" id="dateFin" name="dateFin" th:field="*{dateFin}" />
						<label class="form-label" for="dateFin" th:text="#{park.dateFin}">dateFin</label>
					</div>
				</div>
				<div class="card border" style="text-align: center">
					<div class="form-floating">
						<input class="form-control" type="text" id="type" name="type" th:field="*{type}" /> <label class="form-label" for="type" th:text="#{entrance.type}">type</label>
					</div>
				</div>
				<div class="card border" style="text-align: center">
					<div class="form-floating">
						<input class="form-control" type="text" id="sousType" name="sousType" th:field="*{sousType}" /> <label class="form-label" for="sousType" th:text="#{entrance.sousType}">sousType</label>
					</div>
				</div>
                <div class="card border" style="text-align: center">
                    <div class="form-floating">
                        <input class="form-control" type="text" id="surface" name="surface" 
                        th:field="*{surface}"  />
                        
                        <label class="form-label" for="surface" th:text="#{entrance.surface}">surface</label>
                    </div>
                </div>
                <div class="card border" style="text-align: center">
                    <div class="form-floating">
                        <input class="form-control" type="text" id="surfaceContour" name="surfaceContour" 
                        th:field="*{surfaceContour}" 
                        disabled="disabled"  />
                        
                        <label class="form-label" for="surfaceContour" th:text="#{entrance.surfaceContour}">surfaceContour</label>
                    </div>
                </div>
                
                <div class="card border" style="text-align: center">
                    <div class="form-floating">
						<select onchange="changeType()" class="form-select" required="required" id="typeId" name="typeId" th:field="*{typeId}">
							<option value="" th:text="#{park.parkType.placeholder}">Choose here</option>
							<option th:each="parkType : ${parkTypes}" 
									th:value="${parkType.id}" 
									th:text="${parkType.label}" 
									th:selected="${parkType.id==typeId}"
									th:attr="data-oms=${parkType.oms},data-strict=${parkType.strict}"
									></option>
						</select>
						<label for="idAsso" th:text="#{park.typeId}">typeId</label>
                    </div>
                </div>
                
                <div class="card border" style="text-align: center">
                    <div class="form-floating">
                    	<label class="form-label" for="oms" th:text="#{park.oms}">source</label>
                    	<input class="form-radio" id="omsT" name="oms" type="radio" value="true" th:field="*{oms}" th:text="#{park.oms.yes}" /><br />
  						<input class="form-radio" id="omsF" name="oms" type="radio" value="false" th:field="*{oms}" th:text="#{park.oms.no}" />
                    </div>
                </div>
                
                <div class="card border" style="text-align: center">
                    <div class="form-floating">
                        <input class="form-control" type="text" id="source" name="source" th:field="*{source}"  disabled="disabled"  />
                        <label class="form-label" for="source" th:text="#{park.source}">source</label>
                    </div>
                </div>
                
                <div class="card border" style="text-align: center">
                    <div class="form-floating">
                        <input class="form-control"  type="text" id="status" name="status" th:field="*{status}" disabled="disabled" />
                        <label class="form-label" for="status" th:text="#{park.status}">idStatus</label>
                    </div>
                </div>
                
				
			</div>
		</div>


        <div class="row">
            <div class="col">
                <div class="card border" style="text-align: center">
                  
                </div>
            </div>
        </div>
        
        
            <!-- TODO: make the filter later -->
            <!--  
            <div class="col-6" style="display: none;">
                <div  style="text-align: left">
                 <h6 class="mt-12" th:text="#{park.filter}">title</h6>
	             <input type="radio" id="filterValid" name="filterPark" value="VALID" class="form-check-input" onchange="filterChange('VALID');" />
	             <label for="filterValid" th:text="#{park.filter.valid}" >VALID</label>
	             
	             <input type="radio" id="filterNoMatch" name="filterPark"  value="NO_MATCH" class="form-check-input"  onchange="filterChange('NO_MATCH');" checked="checked" />
	             <label for="caseMerge" th:text="#{park.filter.noMatch}" >NO_MATCH</label>
	            
	             <input type="radio" id="filterReject" name="filterPark" value="REJECT" class="form-check-input" onchange="filterChange('REJECT');"  />
	             <label for="filterReject" th:text="#{park.filter.reject}" >REJECT</label>
                </div>
            </div>
             -->


    </div>
    
    <div class="col-8">
        <div class="row">
            <div class="col-4"  id="blkMap">
                <h6 th:text="#{park.edit.title}">title</h6>
            </div>
        </div>
        <div class="row col-12 h-100">
                <div class="card " style="text-align: center">
                    <div id="mapid" style="width: 100%; height: 100%;"></div>
                </div>
        </div>
            </fieldset>
    </div>

</span>
    </form>

</div>
			</main>
		</div> <!-- layoutSidenav_content -->
	</div> <!-- layoutSidenav -->
	
	<div th:insert="~{fragments/fragment-footer :: footer}"></div>
    <div th:insert="~{fragments/fragment-jslibs :: newMap}"></div>
	<script th:src="@{/mvc/static/js/scripts.js}"></script>
    <script th:src="@{/mvc/static/js/newParkFunctions.js}"></script>
	
	<script th:inline="javascript">
	function changeType() {
		var slct = document.getElementById("typeId");
		var optSlct = slct.options[slct.selectedIndex];
		var dOms= optSlct.getAttribute('data-oms');
		var dStrict= optSlct.getAttribute('data-strict');
		
		console.log("changeType: oms="+dOms+", strict="+dStrict);
		var radioOmsT = document.getElementById("omsT");
		var radioOmsF = document.getElementById("omsF");
		
		if (dOms==='true') {
			radioOmsT.checked=true;
			radioOmsF.checked=false;
		} else {
			radioOmsT.checked=false;
			radioOmsF.checked=true;
		}
		
		if (dStrict==='true') {
			radioOmsT.disabled=true;
			radioOmsF.disabled=true;
		} else {
			radioOmsT.disabled=false;
			radioOmsF.disabled=false;
		}
	}
	

	// point icons
	var editEntranceIcon = L.icon({
	    iconUrl: [[@{/mvc/static/images/marker-icon-parkEdit-1x.png}]]
	    ,iconAnchor:   [12, 41] // point of the icon which will correspond to marker's location
	    ,popupAnchor:  [0, -20] // point from which the popup should open relative to the iconAnchor
	});
	
	 
	/* manage correct ID for COMMUNE SELECTION */
	// th-field broke the datalist work
	document.querySelector('#idCommune-filter').value=[[${newPark.nameCommune}]];
	document.querySelector('#idCommune-filter').addEventListener('input', function(e) {
	    var input = e.target,
	        list = input.getAttribute('list'),
	        options = document.querySelectorAll('#' + list + ' option'),
	        hiddenInput = document.querySelector("#"+input.getAttribute('id').split("-")[0]),
	        inputValue = input.value;
		    
	    	hiddenInput.value = inputValue;
		    
		    for(var i = 0; i < options.length; i++) {
		        var option = options[i];
		        if(option.value === inputValue) {
		        	hiddenInput.value = option.getAttribute('data-value');
		        	document.querySelector("#formSelectCity").submit();
		            break;
		        }
		    }
	});
	
	document.querySelector('#idCommune-filter').addEventListener('focusout', function(e) {
	    var input = e.target,
        list = input.getAttribute('list'),
        options = document.querySelectorAll('#' + list + ' option'),
        hiddenInput = document.querySelector("#"+input.getAttribute('id').split("-")[0]),
        inputValue = input.value;
	    
	    if (isNumericId(hiddenInput)) {
	    	document.querySelector("#formSelectCity").submit();
	    }
	});

	var photoIcon = L.icon({
	    iconUrl: [[@{/mvc/static/images/photo_picto.png}]]
		,iconSize: [50, 50]
	    ,iconAnchor:   [5, 5] // point of the icon which will correspond to marker's location
	    ,popupAnchor:  [5, 5] // point from which the popup should open relative to the iconAnchor
	});
    
    /////////////////////////////////////////////////////////////////////////
    // custom location on load
    function locatePlace(lat, lon) {
    	return L.latLng(lat, lon);
    }
    var locateWorkPlace= L.latLng(50.626419,3.0719121);
    var mLng = document.getElementById("mapLng").value;
    var mLat = document.getElementById("mapLat").value;
    if (mLng!="" && mLat!="") {
    	locateWorkPlace = locatePlace(mLat, mLng);
    }
    


    // map layers
    var mapMinZoom=11;
    var mapMaxZoom=19;
    
    // https://leaflet-extras.github.io/leaflet-providers/preview/
    // fonds de carte
    var layerEsriWorldStreetMap = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
	    	name: 'Carte WorldStreetMap',
	    	attribution: "ArcGIS World Street Map (Esri)",
	        maxZoom: mapMaxZoom,
	        minZoom: mapMinZoom
    });
     var layerSatellite = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
			name: "Satellite ArcGisOnline",
			attribution: '&copy; <a href="https://www.arcgis.com/">ArcGisOnline</a>',
			maxZoom: mapMaxZoom,
            minZoom: mapMinZoom
        });
    var layerStreetMap = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
			name: 'Carte OpenStreetMap',
			attribution: '&copy; <a target="_blank" href="http://osm.org/copyright">OpenStreetMap</a> contributors',
			maxZoom: mapMaxZoom,
			minZoom: mapMinZoom
        });
    var layerAeroIgnMap = L.tileLayer('https://data.geopf.fr/wmts?REQUEST=GetTile&SERVICE=WMTS&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/jpeg&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&LAYER=ORTHOIMAGERY.ORTHOPHOTOS', {
			name: 'Vues Aérienne IGN',
			attribution: '&copy; <a href="https://geoservices.ign.fr/services-geoplateforme-diffusion">IGN GeoPortail</a>',
			tileSize: 256,
			maxZoom: Math.min(18, mapMaxZoom), // 18 maxi
			minZoom: mapMinZoom
    });
    
    
    // groupes des fonds de carte
    const mapLayers = {
   		'Vues Aérienne IGN': layerAeroIgnMap,
        'Satellite ArcGisOnline': layerSatellite,
   		'Carte WorldStreetMap': layerEsriWorldStreetMap,
        'Carte OpenStreetMap': layerStreetMap,
   	};
    
    // extras data sur la carte
    const dataPrefecture = L.layerGroup();
    const dataAutmel = L.layerGroup();
    const dataCadastre = L.layerGroup();
    const dataLayers = {
    	"Données de la préfecture": dataPrefecture,
        "Cadastre des communes": dataCadastre,
    	"Contours des parcs": dataAutmel
    };
    
    var editMap = L.map('mapid', {
    	zoom: 16,
        layers: [ layerStreetMap, layerEsriWorldStreetMap, layerSatellite, layerAeroIgnMap]
    }).setView(locateWorkPlace);

    
	const zoomControl = L.control.scale().addTo(editMap);
    const layerControl = L.control.layers(mapLayers, dataLayers).addTo(editMap);
    

    const idPark = document.querySelector('#idPark').value;
    const idParkRegex = /[0-9]/g;
    const idParkFound = idPark.match(idParkRegex);
    if (mLng!="" && mLat!="" && idParkFound) {
    	var txt = document.querySelector('#name').value;
    	var editMarker = L.marker(L.latLng(mLat,mLng), {icon:editEntranceIcon,draggable:false}).addTo(editMap)
    		.bindPopup(txt).openPopup();
    }
    
    
    // event on click
    // https://stackoverflow.com/questions/11421127/how-to-trigger-events-on-leaflet-map-polygons
    
    function filterChange(filterType) {
    	console.log(geojsonPrefLayer);
    	//geojsonPrefLayer.redraw()
    }
    function customOnEachFeaturePref(feature, layer) {
    	//var filter=document.querySelector('input[name="filterPark"]:checked').value;
    	//console.log('radio='+filter);
    	
        layer.setStyle({
           "fillOpacity": 0.3,
            "opacity": 0.6,
            "color": "#1212ff",
            "fillColor": "#1212ff"
        });
        
        if (feature.properties.name) {
            var parcEdit = "<p>"+feature.properties.name+"</p>";
            layer.bindPopup(parcEdit);
        }
        
        // does this feature have a property named fillColor
        /*
        if (feature.properties) {
        	if (feature.properties.status === filterValid) {
        		if (feature.properties.fillColor) {
                //console.log("to display: feat>"+feature.properties.name+">"+feature.properties.status);
                 layer.setStyle({
                	 "fillColor": feature.properties.fillColor,
                     "fillOpacity": 0.30,
                     "color": feature.properties.fillColor,
                     "opacity": 0.6
                   });
            	}
        	}
        }
        */
    }
    
    function getGMapLink(feature) {
        //first point of the polygon
        clkLat = feature.geometry.coordinates[0][0][1];
        clkLng = feature.geometry.coordinates[0][0][0];
        gUrl="https://www.google.com/maps/@"+clkLat+","+clkLng+",18.03z";
        return "<br /><a href='"+gUrl+"' target='_blank'>GMap</a>";
    }
    
    function customOnEachFeatureParc(feature, layer) {
        layer.setStyle({
            "fillOpacity": 0.3,
             "opacity": 0.6,
             "color": "#78e826",
             "fillColor": "#78e826"
         });

        const sUrlEditPark= window.location.protocol + "//" + window.location.host + [[@{/mvc/park/new/check}]]+'?idPark=';
        if (feature.properties.name) {
        	var parcEdit = "<p>"+feature.properties.name+"</p>";
            if (feature.properties.id) {
                parcEdit= "<p>"+ feature.properties.name + "<br /><a href='"+ sUrlEditPark + feature.properties.id +"' >Edit</a></p>";
            }
            layer.bindPopup(parcEdit);
        }
    }
    
    function customOnEachFeatureCadastre(feature, layer) {
        layer.setStyle({
            "fillOpacity": 0.3,
             "opacity": 0.6,
             "color": "#eef127",
             "fillColor": "#eef127"
         });

        if (feature.properties && feature.properties.nom) {
           layer.bindPopup(feature.properties.nom);
        }
    }
    
    
    
    function fnParkPrefClicked(feat) {
    	//TODO detect CREATE MODE
    	// check function fnParkPrefClicked(feat) {k is idParcJardin is null
    	
 		manageLocationDisabled();
 		var props = feat.properties;
 		var geom = feat.geometry;
        var strPoly = JSON.stringify(geom);
        
        const curIdPark = document.getElementById("idPark").value;
        const curHadGeometry = document.getElementById("hadGeometry").value;
        
    	if (action_mode==="CREATE" || action_mode==="UPDATE") {
    		if (
   				(isNumericId(curIdPark) || curHadGeometry==="")
   				&&
   				(curHadGeometry==="false" || curHadGeometry==="")
    		   ) {
    			
    			var msg ="voulez vous recopier le détourage (bleu) du parc?";
    			let choice=confirm( msg );
                if (choice) {
                    document.getElementById("sGeometry").value=strPoly;
                    document.getElementById("etatAction").value="add";
                    document.getElementById("etat").value="pref";
                    
                    return;
                }
                
    		} else {
    			console.log("parc prefectur ignored");
    		}
    		return;
    	}
    	

        document.getElementById("sGeometry").value=strPoly;
        document.getElementById("etatAction").value="add";
    	document.getElementById("etat").value="pref";
        document.getElementById("id").value = props.id;
    	document.getElementById("idPark").value = props.idParcJardin;
        document.getElementById("name").value = props.name;
        document.getElementById("surface").value = props.surface;
        document.getElementById("quartier").value = props.quartier;
        document.getElementById("type").value = props.type;
        document.getElementById("sousType").value = props.sousType;
        document.getElementById("status").value = props.status;
        document.getElementById("source").value = props.source;
        
        document.getElementById('name').style.color = 'black';
        document.getElementById('name').style.fontWeight  = 'normal';
        
    }
    
    // manage click on geometry
    function fnParkGardenClicked(props) {
    	if (document.getElementById("etat").value!=='pref') {
    		// cleanUp
            document.getElementById("hadGeometry").value = "";
            document.getElementById("id").value = "";
            document.getElementById("idPark").value = "";
            document.getElementById("name").value = "";
            document.getElementById("surface").value = "";
            document.getElementById("quartier").value = "";
            document.getElementById("type").value = "";
            document.getElementById("sousType").value = "";
            document.getElementById("status").value = "";
            document.getElementById("source").value = "";
    	}
    	
        document.getElementById("idPark").value = props.id;
        document.getElementById("name").value = props.name;
        document.getElementById("source").value = props.source;
        document.getElementById("surface").value = props.surface;

        document.getElementById('name').style.color = 'green';
        document.getElementById('name').style.fontWeight  = 'bold';
        
        document.getElementById("etat").value="p&j";
        document.getElementById("etatAction").value="edit";
        
    }

    
    var geojsonPrefLayer = null;
    var geojsonCadastreLayer = null;
    var geojsonParkLayer = null;
    // relead parks with map fit
    function loadPrefParks() {
        
        // prepare map corners
        swLat=editMap.getBounds().getSouthWest().lat;
        swLng=editMap.getBounds().getSouthWest().lng;
        neLat=editMap.getBounds().getNorthEast().lat;
        neLng=editMap.getBounds().getNorthEast().lng;
        sPos="swLat="+ swLat +"&swLng="+swLng +"&neLat="+neLat +"&neLng="+neLng;
        
        
        // get shapes: parc prefecture
        if (geojsonPrefLayer!==null) {
        	dataPrefecture.removeLayer(geojsonPrefLayer);
        }
        var sUrlGeoPref= window.location.protocol + "//" + window.location.host + [[@{/mvc/geojson/parkPrefectureByCorner}]]+'?'+sPos;
        geojsonPrefLayer = new L.GeoJSON.AJAX(sUrlGeoPref, {onEachFeature: customOnEachFeaturePref});
        //geojsonPrefLayer.addTo(dataPrefecture);
        dataPrefecture.addLayer(geojsonPrefLayer);
        
        // get shapes: contours parc autmel  : parkGardenByCorner
        if (geojsonParkLayer!==null) {
        	dataAutmel.removeLayer(geojsonParkLayer);
        }
        var sUrlGeoAutmel= window.location.protocol + "//" + window.location.host + [[@{/mvc/geojson/parkGardenOutlineByCorner}]]+'?'+sPos;
        geojsonParkLayer = new L.GeoJSON.AJAX(sUrlGeoAutmel, {
        	   
        	   pointToLayer: function(feature, latlng) {
        		   if (feature.properties.name) {
	        		   const sUrlEditPark= window.location.protocol + "//" + window.location.host + [[@{/mvc/park/new/check}]]+'?idPark=';
	        	       var parcEdit = "<p>"+feature.properties.name+"</p>";
	       	            if (feature.properties.id) {
	       	                parcEdit= parcEdit +"<p><a href='"+ sUrlEditPark + feature.properties.id +"' >Edit</a>&nbsp(contour non défini)</p>";
	       	            }
	       	           const mark = L.marker(latlng).bindPopup(parcEdit).addTo(editMap);
	       	           //return mark;
        	        }
        	   },
        	   onEachFeature: customOnEachFeatureParc

        	});
        
        dataAutmel.addLayer(geojsonParkLayer);
        
        // get shapes: Cadastre
        if (geojsonCadastreLayer!==null) {
        	dataCadastre.removeLayer(geojsonCadastreLayer);
        }
        var sUrlGeoCadastre= window.location.protocol + "//" + window.location.host + [[@{/mvc/geojson/cadastreByCorner}]]+'?'+sPos;
        geojsonCadastreLayer = new L.GeoJSON.AJAX(sUrlGeoCadastre, {onEachFeature: customOnEachFeatureCadastre});
        dataCadastre.addLayer(geojsonCadastreLayer);
        
        
        // works! used when affect
        geojsonPrefLayer.on('click', function(e){
            console.log("click pref");
            console.log(e.sourceTarget.feature);
        	fnParkPrefClicked(e.sourceTarget.feature)
        });
        
        // works! used when delete
        geojsonPrefLayer.on("pm:remove", (e) => {
            console.log("pm:remove geom");
        	 console.log(e.layer.feature);
        	 document.getElementById("etatAction").value="remove";
          });


        
        editMap.on('pm:create', ({ layer}) => {  
            console.log("pm:create geom");
            document.getElementById("etatAction").value="create";
            
        	  layer.on('pm:edit', e => {
                console.log("pm:edit geom");
        	    console.log(e);

                self_drawn = editMap.pm.getGeomanDrawLayers(true);
                var strPoly = JSON.stringify(self_drawn.toGeoJSON());
                document.getElementById("sGeometry").value=strPoly;
                document.getElementById("etatAction").value="edit";
                
                console.log(strPoly);
        	    
        	    //var strPoly = JSON.stringify(e.layers.toGeoJSON());
        	  });
        	});
        
        // works! event after add poly
        editMap.on('pm:drawend', function (e) {
            console.log("pm:drawend");
            console.log(e);
            document.getElementById("etatAction").value="new";
            
            self_drawn = editMap.pm.getGeomanDrawLayers(true);
            var strPoly = JSON.stringify(self_drawn.toGeoJSON());
            console.log(strPoly);
            document.getElementById("sGeometry").value=strPoly;
            
              //want to check here whether the layerToDraw is valid
              //features.addLayer(layerToDraw);
            });
        
        // works! event after modify pref poly
        geojsonPrefLayer.on('pm:change', function (e) {
            console.log("pm:change ");
              var layerToDraw = e.layer;   
              console.log(layerToDraw);
              document.getElementById("etatAction").value="change";
              
              var strPoly = JSON.stringify(layerToDraw.toGeoJSON());
              document.getElementById("sGeometry").value=strPoly;
              //want to check here whether the layerToDraw is valid
              //features.addLayer(layerToDraw);
            });
        
        // works! event after modify autmel park poly
        geojsonParkLayer.on('pm:change', function (e) {
            console.log("pm:change ");
              var layerToDraw = e.layer;   
              console.log(layerToDraw);
              document.getElementById("etatAction").value="change";
              
              var strPoly = JSON.stringify(layerToDraw.toGeoJSON());
              document.getElementById("sGeometry").value=strPoly;
              //want to check here whether the layerToDraw is valid
              //features.addLayer(layerToDraw);
            });
        
        // used when affect
        geojsonParkLayer.on('click', function(e){
            console.log(e.sourceTarget.feature);
            fnParkGardenClicked(e.sourceTarget.feature.properties)
        });
        
    }
    loadPrefParks();
    editMap.on('zoomend',loadPrefParks);
    editMap.on('moveend',loadPrefParks);

    
    //geoman
    // https://geoman.io/docs/leaflet/category/modes
    editMap.pm.setLang("fr");
    editMap.pm.setGlobalOptions({
   	  allowSelfIntersection: false
   	});
    
    editMap.pm.addControls({
	  position: 'topleft',  
	  drawControls: true,
	  editControls: true,
	  optionsControls: true,
	  customControls: true,
	  oneBlock: true,
	  
	  drawMarker: false,
	  drawPolyline: false,
	  drawRectangle: false,
	  drawPolygon: true,
	  drawCircle: false,
	  drawCircleMarker: false,
	  drawText: false,
	  editMode: true,
	  dragMode: false,
	  cutPolygon: false,
	  removalMode: true,
	  rotateMode: false,
	}); 
    
    // alert(calcCrow(59.3293371,13.4877472,59.3225525,13.4619422).toFixed(1));


    
    function presave() {
    	var strPoly = JSON.stringify(L.PM.Utils.findLayers(map)[0].toGeoJSON());
    }
    var action_mode=null
    function manageCreateDisabled() {
    	
    	if (document.getElementById("idPark").value!=="") {
    		let msg =	[[#{park.new.confirmQuestion1}]] +
		    			document.getElementById("name").value +
		    			[[#{park.new.confirmQuestion2}]] + "\n\n" +
		    			[[#{park.new.confirmYesNo}]] ;
    		let choice=confirm( msg );
    		if (!choice) {
    			return;
    		}
    	}
    	
    	action_mode = "CREATE";
    	document.getElementById("fsEditPark").disabled = false;
    	var objBtnNew=document.getElementById("btnNew");
    	objBtnNew.classList.remove('btn-outline-secondary');
    	objBtnNew.classList.remove('btn-outline-success');
    	objBtnNew.classList.add('btn-success');

    	var objBtnUpdate=document.getElementById("btnUpdate");
    	objBtnUpdate.classList.remove('btn-outline-secondary');
    	objBtnUpdate.classList.remove('btn-outline-success');
    	objBtnUpdate.classList.add('btn-outline-secondary');
    	objBtnUpdate.disabled= true;
    	
    	// clean form
    	//document.getElementById("formNewPark").reset();
        document.getElementById("hadGeometry").value = "";
    	document.getElementById("idPark").value="";
    	document.getElementById("id").value="";
    	document.getElementById("sGeometry").value="";
    	document.getElementById("etat").value="";
    	document.getElementById("etatAction").value="";
    	document.getElementById("name").value="";
    	document.getElementById("quartier").value="";
    	document.getElementById("type").value="";
    	document.getElementById("sousType").value="";
    	document.getElementById("surface").value="";
    	document.getElementById("surfaceContour").value="";
    	//document.getElementById("typeId").value="";
    	document.getElementById("source").value="";
    	document.getElementById("status").value="";
    	
    }
    
    function manageUpdateDisabled() {
    	// can't enter in update mode without existing id
    	if (document.getElementById("idPark").value==="") {
  			return;
    	}
    	action_mode = "UPDATE";
    	document.getElementById("fsEditPark").disabled = false;
    	var objBtnUpdate=document.getElementById("btnUpdate");
    	objBtnUpdate.classList.remove('btn-outline-success');
    	objBtnUpdate.classList.add('btn-success');

    	var objBtnNew=document.getElementById("btnNew");
    	objBtnNew.classList.remove('btn-outline-success');
    	objBtnNew.classList.add('btn-outline-secondary');
    }
    if (document.getElementById("idPark").value!=="") {
    	manageUpdateDisabled();
    }
    
    function savePark() {
    	var hasError=false;
    	console.log("savePark");
    	// TODO continues
 	    if (document.getElementById("id").value==="" && document.getElementById("sGeometry").value==="") {
 	    	hasError=true;
 	    	var item=document.getElementById("blkName");
 	    	objBtnNew.classList.add("has-error");
    	}
    	
    	if (document.getElementById("id").value==="" && document.getElementById("sGeometry").value==="") {
    		hasError=true;
 	    	var item=document.getElementById("blkMap");
 	    	objBtnNew.classList.add("has-error");
    	}
    	
    	if (hasError) {
    		console.log("savePark hasError");
    		return;
    	}
    	
    	document.getElementById("saveParkSpinner").style = "display:block;";
    	var objBtnSave=document.getElementById("btnSave");
    	objBtnSave.classList.remove('btn-outline-warning');
    	objBtnSave.classList.add('btn-warning');
    	
    	console.log("savePark hasError");
    	
    	if (document.getElementById("dateDebut").value==="jj/mm/aaaa") {
    		document.getElementById("dateDebut").value="";
    	}
    	if (document.getElementById("dateFin").value==="jj/mm/aaaa") {
    		document.getElementById("dateFin").value="";
    	}
    	document.getElementById("formNewPark").submit();
    }
    
    function manageLocationDisabled() {
        console.log(document.getElementById("idRegion").value);
        
        if (document.getElementById("idRegion").value!="") {
            document.getElementById("fsComm2co").disabled = false;
            document.getElementById("fsCity").disabled = false;
        }
        if (document.getElementById("idCommunauteDeCommunes").value!="") {
            document.getElementById("fsCity").disabled = false;
        }
    }
    manageLocationDisabled();

    
    // center on map by navigator loc (require grand)
    function showPosition(position) {
        lat =position.coords.latitude;
        lon =position.coords.longitude;

    	// feedback on form
        document.getElementById("mapLng").value=lon;
        document.getElementById("mapLat").value=lat;
        
        editMap.setView(L.latLng(lat,lon));
    }
    
    // https://gis.stackexchange.com/questions/439672/checking-whether-a-polygon-drawn-with-leaflet-geoman-plugin-is-valid
    $(document).ready(function(){

        $("#srcAdress").select2(
        	{
        	  ajax: {
        		delay: 500,
        		closeOnSelect: true,
        		//selectOnClose: true,
        		allowClear: true,
        		language: "fr",
        		placeholder: 'Rechercher par adresse',
    	   		 minimumInputLength: 3,
    	         url: function (params) {
    	             console.log("change adresse select2");
    	             var idCommune=document.getElementById('idCommune');
    	             // var idCity = idCommune.options[idRegion.selectedIndex].value;
    	             var idCity = idCommune.value;
    	             console.log("idCity"+idCity);
    	             
    	             //var search = srcAdress.options[srcAdress.selectedIndex].value;
    	             var search = params.term;
    	             
    	             console.log('adress?idCity='+ idCity +'&q='+ search);
    	             return  [[@{/mvc/ajax/dropdown/search_adress}]] +'?idCity='+ idCity +'&q='+search;//+'&q1='+ params.term;
    	           },
    	           
                dataType: 'json' ,
                processResults: function (data) {
               		return {
                        "results":data
                      };  
                },
            },
            templateSelection: function (data, container) {
                	//console.log("data-point="+data.value);
                	//console.log("data-lat="+data.lat);
                	//console.log("data-lon="+data.lon);
                    $(data.element).attr('data-lat', data.lat);
                    $(data.element).attr('data-lon', data.lon);
                    return data.text;
            },
          }
        ); // select2 commune
	
        $('#srcAdress').on("change", function(e) { 
            //locate on the adress
            var selectedSelect2OptionSource = $("#srcAdress :selected");
        	var lon = selectedSelect2OptionSource.attr('data-lon');
        	var lat = selectedSelect2OptionSource.attr('data-lat');
        	
        	// feedback on form
            document.getElementById("mapLng").value=lon;
            document.getElementById("mapLat").value=lat;
            
            locateWorkPlace=locatePlace(lat, lon);
            editMap.setView(locateWorkPlace);
        });
    });

    
    
/*
 https://www.theserverside.com/blog/Coffee-Talk-Java-News-Stories-and-Opinions/file-upload-Spring-Boot-Ajax-example

	 <!-- HTML5 Input Form Elements -->
	 <input id="fileupload" type="file" name="fileupload" /> 
	 <button id="upload-button" onclick="uploadFile()"> Upload </button>

	 <!-- Ajax JavaScript File Upload to Spring Boot Logic -->
	 <script>
 */
    async function uploadFile() {
      let formData = new FormData(); 
      formData.append("file", fileupload.files[0]);
      let response = await fetch('/upload', {
        method: "POST", 
        body: formData
      }); 

      if (response.status == 200) {
        alert("File successfully uploaded.");
      }
    }


 // prévalidation du fichier d'upload (type, conhérence, exif)
 function fileValidation() {
     var fileInput =  document.getElementById('fileupload');
     var filePath = fileInput.value;
     
     // Allowing file type
     //var allowedExtensions =  /(\.jpg|\.jpeg|\.png|\.gif)$/i;
     var allowedExtensions =  /(\.jpg|\.jpeg)$/i;
     if (!allowedExtensions.exec(filePath)) {
    	 //console.log("fichier invalide");
         alert("Le fichier n'est pas une photo.");
         fileInput.value = '';
         return false;
         
     } else  {
         // Image preview
         if (fileInput.files && fileInput.files[0]) {
        	 var blob = fileInput.files[0]; // See step 1 above
        	 var extType=blob.type;
        	 //console.log("extType="+extType);

        	 var fileReader = new FileReader();
        	 fileReader.onloadend = function(e) {
		 	   var arr = (new Uint8Array(e.target.result)).subarray(0, 4);
		 	   var header = "";
		 	   for(var i = 0; i < arr.length; i++) {
		 	      header += arr[i].toString(16);
		 	   }
		 	   //console.log("header="+header);
		 	   
		 	   // Check the file signature against known types
		  	  var realType="unknown";
		 	  switch (header) {
		 	    case "89504e47":
		 	    	realType = "image/png";
		 	        break;
		 	    case "47494638":
		 	    	realType = "image/gif";
		 	        break;
		 	    case "ffd8ffe0":
		 	    case "ffd8ffe1":
		 	    case "ffd8ffe2":
		 	    case "ffd8ffe3":
		 	    case "ffd8ffe8":
		 	    	realType = "image/jpeg";
		 	        break;
		 	    case "52494646":
		 	    	realType = "image/webp";
		 	        break;
		 	    default:
		 	    	realType = "unknown"; // Or you can use the blob.type as fallback
		 	        break;
		 	  }
		 	  //console.log("realType="+realType);
       	  
				if (realType==="unknown") {
					fileInput.value = '';
					alert('Le format du fichier est inconnu.');
					return false;
				} else if (extType===realType) {
					//console.log("Le format du fichier ne correspond pas à l'extention.");
					var chkLoc=false;
					
					EXIF.getData(blob, function(chkLoc) {
				        var extLat = EXIF.getTag(this, "GPSLatitude");
				        var extLon = EXIF.getTag(this, "GPSLongitude");
				        console.log(extLat);
				        console.log(extLon);
				        if (!extLat && !extLon) {
				        	fileInput.value = '';
				        	alert("La photo n'a pas de données de localisation.");
				        	chkLoc= false;
				        } else {
				        	chkLoc= true;
				        	
				        	// get latitude from exif data and calculate latitude decimal
				            var latDegree = extLat[0].numerator;
				            var latMinute = extLat[1].numerator;
				            var latSecond = extLat[2].numerator;
				            var latDirection = EXIF.getTag(this, "GPSLatitudeRef");
				            var latFinal = ConvertDMSToDD(latDegree, latMinute, latSecond, latDirection);
				            
				            // get longitude from exif data and calculate longitude decimal
				            var lonDegree = extLon[0].numerator;
				            var lonMinute = extLon[1].numerator;
				            var lonSecond = extLon[2].numerator;
				            var lonDirection = EXIF.getTag(this, "GPSLongitudeRef");
				            var lonFinal = ConvertDMSToDD(lonDegree, lonMinute, lonSecond, lonDirection);
				            
				            // centrage sur la carte
				            locateWorkPlace=locatePlace(latFinal, lonFinal);
				            editMap.setView(locateWorkPlace);
				            
				            document.getElementById("mapLng").value=lonFinal;
				            document.getElementById("mapLat").value=latFinal;
				            
				            photoMarker = L.marker( L.latLng(latFinal,lonFinal), {
				            		icon:photoIcon, draggable:false
				            		})
				    			 // .bindPopup('Votre photo a été prise ici.')
				    			  .addTo(editMap);
				    		sleep(2000).then(() => { editMap.removeLayer(photoMarker); });
				    		
				        }
				    });
					
					/*
		        	 var previsuReader = new FileReader();
		        	 previsuReader.onloadend = function(e) {
		        		 document.getElementById('imagePreview').innerHTML = '<img width="300" height="200" src="'+ blob +'"/>';
		        	 }
		        	 previsuReader.readAsDataURL(blob);
		        	 */
		        	 
					return chkLoc;
				} else {
					fileInput.value = '';
					//console.log("fichier non conforme");
					alert("Le format du fichier ne correspond pas à l'extention.");
					return false;
				}
	        	 
        	 };
        	 fileReader.readAsArrayBuffer(blob);
        	 
         } else {
        	 console.log("pas de fichier");
        	 return false;
         }
     }
 }

</script>

</body>
</html>
