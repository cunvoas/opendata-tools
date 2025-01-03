/*
Only generic function here.
 */

// check if object is present
function isEmpty(obj) {
    return Object.keys(obj).length === 0;
}

// check if valueId is numeric
function isNumericId(valueId) {
    const regex = /[0-9]/g;
    return valueId.match(regex);
}

// set a timer
function sleep(ms) {
   return new Promise(resolve => setTimeout(resolve, ms));
}

// Converts numeric degrees to radians
function toRad(Value) {
    return Value * Math.PI / 180;
}

//This function takes in latitude and longitude of two location and returns the distance between them as the crow flies (in km)
function calcCrow(lat1, lon1, lat2, lon2) {
  var R = 6371; // km
  var dLat = toRad(lat2-lat1);
  var dLon = toRad(lon2-lon1);
  var lat1 = toRad(lat1);
  var lat2 = toRad(lat2);
  var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
  var d = R * c;
  return d;
}

// locate client from browser location feature
function locateMe() {
    if (navigator.geolocation) {
       navigator.geolocation.getCurrentPosition(showPosition);
    }
}

// conertion of degrees, minutes, seconds to Decimal
function ConvertDMSToDD(degrees, minutes, seconds, direction) {
    //console.log(degrees+" "+minutes+" "+seconds+" "+direction);
    var dd = degrees + (minutes/60) + (seconds/36000000);
    //console.log(degrees+" "+(minutes/60)+" "+(60000/36000000));                               
    if (direction == "S" || direction == "W") {
        dd = dd * -1;
    }
    //console.log(dd);
    return dd;
 }