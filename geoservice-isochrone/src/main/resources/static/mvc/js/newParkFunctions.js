/*
Only generic function here.
 */

// check if object is present
function isEmpty(obj) {
    return Object.keys(obj).length === 0;
}

// check if valueId is numeric
function isNumericId(valueId) {
    let regex = /[0-9]/g;
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
function calcCrow(ilat1, ilon1, ilat2, ilon2) {
  const R = 6371; // km
  const dLat = toRad(ilat2-ilat1);
  const dLon = toRad(ilon2-ilon1);
  const lat1 = toRad(ilat1);
  const lat2 = toRad(ilat2);
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
  const d = R * c;
  return d;
}


// conertion of degrees, minutes, seconds to Decimal
function ConvertDMSToDD(degrees, minutes, seconds, direction) {
    //console.log(degrees+" "+minutes+" "+seconds+" "+direction);
    let dd = degrees + (minutes/60) + (seconds/36000000);
    //console.log(degrees+" "+(minutes/60)+" "+(60000/36000000));                               
    if (direction == "S" || direction == "W") {
        dd = dd * -1;
    }
    //console.log(dd);
    return dd;
 }