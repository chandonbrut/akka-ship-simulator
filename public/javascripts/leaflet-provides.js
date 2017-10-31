function onMessage(evt) {
    pos = $.parseJSON(evt.data);
    var ship = new Ship(pos.imoNumber,L.marker([pos.position.latitude,pos.position.longitude]))
    i = 0;
    var found = false;
    for (i = 0; i<window.map.ships.length; i++) {
        if (window.map.ships[i].imo === ship.imo) {
            window.map.ships[i].marker.setLatLng(ship.marker.getLatLng());
            found = true;
        }
    }
    if (!found) {
        ship.marker.addTo(map);
        ship.marker.bindPopup('<b>' + pos.imoNumber + '</b>');
        window.map.ships.push(ship);
    }
}

function addArea(simArea) {
    var simAreaGeoJSON = Terraformer.WKT.parse(simArea);
    L.geoJson(simAreaGeoJSON).addTo(window.map);
}

function loadMap() {
    function Position(lat,lon,timestamp) {
        this.lat = lat;
        this.lon = lon;
        this.timestamp = timestamp;
    }

    window.map = L.map('map', {
        center: [-22, -43],
        zoom: 6
    });

    window.map.ships = new Array();

    L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 20,
      minZoom: 2
    }).addTo(window.map);


}