const axios = require('axios');

const speedLimitMap = {
  otomobil: { residential: 50, trunk: 110, trunk_link: 110, primary: 90, secondary: 70, primary_link: 110, secondary_link: 90, tertiary: 90, tertiary_link: 90, motorway: 120, motorway_link: 120, service: 20 },
  minibus: { residential: 50, trunk: 90, trunk_link: 90, primary: 90, secondary: 80, primary_link: 90, secondary_link: 80, tertiary: 80, tertiary_link: 80, motorway: 100, motorway_link: 100, service: 20 },
  otobus: { residential: 50, trunk: 90, trunk_link: 90, primary: 90, secondary: 80, primary_link: 90, secondary_link: 80, tertiary: 80, tertiary_link: 80, motorway: 100, motorway_link: 100, service: 20 },
  kamyonet: { residential: 50, trunk: 85, trunk_link: 85, primary: 85, secondary: 80, primary_link: 85, secondary_link: 80, tertiary: 80, tertiary_link: 80, motorway: 95, motorway_link: 95, service: 20 },
  kamyon: { residential: 50, trunk: 85, trunk_link: 85, primary: 85, secondary: 80, primary_link: 85, secondary_link: 80, tertiary: 80, tertiary_link: 80, motorway: 90, motorway_link: 90, service: 20 },
  motosiklet: { residential: 50, trunk: 90, trunk_link: 90, primary: 90, secondary: 80, primary_link: 90, secondary_link: 80, tertiary: 80, tertiary_link: 80, motorway: 100, motorway_link: 100, service: 20 },
  bisiklet: { residential: 30, trunk: 45, trunk_link: 45, primary: 45, secondary: 45, primary_link: 45, secondary_link: 45, tertiary: 45, tertiary_link: 45, motorway: 0, motorway_link: 0, service: 20 },
};

const getSpeedLimit = async (req, res, next) => {
  const { lat, lng, vehicleType = 'otomobil' } = req.query;
  if (!lat || !lng) {
    next({ error: 'You must at least provide latitude and longitude values via \'lat\' and \'lng\' query params. ' });
  }
  const radius = req.query.radius || 20;
  const osmData = await axios
    .get(`http://overpass-api.de/api/interpreter?data=[out:json];way(around:${radius},${lat},${lng});out;`)
    .then(res => res.data);
  if (!osmData.elements.length) {
    // No speed limit data
    return res.json({ speedLimit: -1 });
  }
  const tags = osmData.elements[0].tags;
  const roadType = tags.highway;
  const speedLimit = tags.maxspeed // specific speed limit for the road
    || speedLimitMap[vehicleType][roadType] // default speed limit by road & vehicle type
    || speedLimitMap[vehicleType].default // residential area
    || -1; // unknown
  res.json({
    speedLimit,
    roadType,
    name: tags.name,
    oneway: tags.oneway && tags.oneway === 'yes',
    lanes: tags.lanes && parseInt(tags.lanes, 10),
  });
};

const getMetaData = (name) => {
  if (name.match(/opet/i)) {
    return { price: 5.5, fuelEfficiency: 110.5, lastRefuel: '4/10/2017 15:31' };
  }
  if (name.match(/petrol\sofisi|PO/i)) {
    return { price: 4.12, fuelEfficiency: 90.7, lastRefuel: '3/10/2017 17:44' };
  }
  if (name.match(/BP/i)) {
    return { price: 4.72, fuelEfficiency: 120.2, lastRefuel: '5/10/2017 09:44' };
  }
  if (name.match(/Shell/i)) {
    return { price: 5.5, fuelEfficiency: 112.0, lastRefuel: '5/10/2017 09:33' };
  }
  if (name.match(/LUKOIL|lukoil|luk/i)) {
    return { price: 5.52, fuelEfficiency: 108.0, lastRefuel: '3/10/2017 13:38' };
  }
  return { price: 5, fuelEfficiency: 93.5, lastRefuel: '8/9/2017 16:40' };
};

const googleKey = 'AIzaSyAW6j9RShc6zcERyzdeTDRwwLn_gwwAL30';
const getNearestFuelStation = async (req, res, next) => {
  const { lat, lng } = req.query;
  if (!lat || !lng) {
    next({ error: 'You must at least provide latitude and longitude values via \'lat\' and \'lng\' query params. ' });
  }
  const googleData = await axios
    .get(`https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=${googleKey}&location=${lat},${lng}&rankby=distance&type=gas_station`)
    .then(res => res.data);
  console.log(googleData);
  const reduced = googleData.results
    .map(res => ({ name: res.name, location: res.geometry.location, ...getMetaData(res.name) }))
    .filter(el => !!el.price)
    .filter((el, index, arr) => el === arr.find(e => e.name === el.name));
  res.json(reduced);
};

exports.configureRoutes = (app) => {
  app.get('/api/speed-limit', getSpeedLimit);
  app.get('/api/fuel-station', getNearestFuelStation);
};
